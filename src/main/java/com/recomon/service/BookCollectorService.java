package com.recomon.service;

import com.recomon.domain.Book;
import com.recomon.dto.NaverBookItem;
import com.recomon.dto.NaverBookSearchResponse;
import com.recomon.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.ai.document.Document;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * packageName    : com.recomon.service
 * fileName       : BookCollectorService
 * author         : kobe
 * date           : 2026. 1. 11.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026. 1. 11.        kobe       최초 생성
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class BookCollectorService {

    private final BookRepository bookRepository;
    private final VectorStore vectorStore; // AI 저장소 주입
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.client-secret}")
    private String clientSecret;

    // 핵심 동작: 특정 검색어(query)로 책 데이터를 가져와 DB에 저장
    // 각 카테고리별로 20권 수집 (중복 제외, 여러 페이지 호출)
    @Transactional
    public int collectBooks(String query) {
        log.info(">>> '{}' 관련 도서 수집 시작...", query);

        // 헤더 설정 ID, Secret 추가
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", clientId);
        headers.set("X-Naver-Client-Secret", clientSecret);
        HttpEntity<String> request = new HttpEntity<>(headers);

        int totalSaveCount = 0;
        int targetCount = 20; // 각 카테고리별 목표 수집 개수 (중복 제외)
        int maxPages = 5; // 최대 5페이지까지 시도 (500권)
        int display = 100; // 한 페이지당 최대 100개
        
        try {
            // 여러 페이지를 순차적으로 호출하여 목표 개수까지 수집
            for (int page = 1; page <= maxPages && totalSaveCount < targetCount; page++) {
                int start = (page - 1) * display + 1; // 1, 101, 201, ...
                
                // 네이버 API 호출 URL 설정
                URI uri = UriComponentsBuilder
                        .fromUriString("https://openapi.naver.com")
                        .path("/v1/search/book.json")
                        .queryParam("query", query)
                        .queryParam("display", display)
                        .queryParam("start", start)
                        .queryParam("sort", "sim") // 정확도순 정렬
                        .encode()
                        .build()
                        .toUri();

                // API 요청
            ResponseEntity<NaverBookSearchResponse> response = restTemplate.exchange(
                    uri, HttpMethod.GET, request, NaverBookSearchResponse.class
            );

            NaverBookSearchResponse body = response.getBody();
                if (body == null || body.getItems() == null || body.getItems().isEmpty()) {
                    log.info(">>> '{}' {}페이지: 더 이상 데이터가 없습니다.", query, page);
                    break; // 더 이상 데이터가 없으면 종료
            }

            List<NaverBookItem> items = body.getItems();
                log.info(">>> '{}' {}페이지: API에서 {}권 반환됨", query, page, items.size());
                int pageSaveCount = 0;
                int duplicateCount = 0;

                // 데이터 가공 및 DB 저장
            for (NaverBookItem item : items) {
                    String cleanedDesc = cleanHtml(item.getDescription());
                    String cleanedTitle = cleanHtml(item.getTitle());
                    String imageUrl = item.getImage();
                    
                    // 이미 저장된 책인지 확인
                    boolean isNewBook = !bookRepository.existsById(item.getIsbn());
                    
                    if (isNewBook) {
                        // 출판년도 파싱 (pubdate 형식: "20240101" 또는 "2024-01-01")
                        int publishedYear = 0;
                        try {
                            String pubdate = item.getPubdate();
                            if (pubdate != null && !pubdate.isEmpty()) {
                                if (pubdate.length() >= 4) {
                                    publishedYear = Integer.parseInt(pubdate.substring(0, 4));
                                }
                            }
                        } catch (NumberFormatException e) {
                            // 출판년도 파싱 실패 시 기본값 0 사용
                        }

                        // 1. RDB 저장 (신규 책)
                    Book book = Book.builder()
                            .isbn(item.getIsbn())
                                .title(cleanedTitle)
                                .author(cleanHtml(item.getAuthor()))
                                .imageUrl(imageUrl) // 이미지 URL 저장
                                .category(query) // 검색어를 카테고리로 저장
                                .publishedYear(publishedYear)
                                .rating(0.0) // 기본값
                                .reviewCount(0) // 기본값
                                .bestSeller(false) // 기본값
                                .awardWinner(false) // 기본값
                            .build();
                    bookRepository.save(book);

                    // 2. Vector Store (AI) 저장
                    String contentToEmbed = cleanedDesc.length() > 10 ? cleanedDesc : cleanedTitle;

                    Document document = new Document(contentToEmbed, Map.of(
                            "isbn", item.getIsbn(),
                            "title", cleanedTitle,
                            "category", query
                    ));

                        vectorStore.add(List.of(document)); // OpenAI API 호출 발생 (비용 발생)

                        pageSaveCount++;
                        totalSaveCount++;
                        
                        // 목표 개수에 도달하면 종료
                        if (totalSaveCount >= targetCount) {
                            break;
                        }
                    } else {
                        // 기존 책: imageUrl이 null이거나 비어있으면 업데이트
                        Book existingBook = bookRepository.findById(item.getIsbn()).orElse(null);
                        if (existingBook != null && (existingBook.getImageUrl() == null || existingBook.getImageUrl().isEmpty()) 
                                && imageUrl != null && !imageUrl.isEmpty()) {
                            // Builder 패턴으로 업데이트 (기존 필드 유지)
                            Book updatedBook = Book.builder()
                                    .isbn(existingBook.getIsbn())
                                    .title(existingBook.getTitle())
                                    .author(existingBook.getAuthor())
                                    .imageUrl(imageUrl) // 이미지 URL 업데이트
                                    .category(existingBook.getCategory())
                                    .publishedYear(existingBook.getPublishedYear())
                                    .rating(existingBook.getRating())
                                    .reviewCount(existingBook.getReviewCount())
                                    .bestSeller(existingBook.isBestSeller())
                                    .awardWinner(existingBook.isAwardWinner())
                                    .build();
                            bookRepository.save(updatedBook);
                            log.debug(">>> 기존 책 이미지 URL 업데이트: ISBN={}, ImageUrl={}", item.getIsbn(), imageUrl);
                        }
                        duplicateCount++;
                }
            }
                
                log.info(">>> '{}' {}페이지: API 반환 {}권, 신규 저장 {}권, 중복 {}권 (누적: {}권)", 
                        query, page, items.size(), pageSaveCount, duplicateCount, totalSaveCount);
                
                // 목표 개수에 도달하면 종료
                if (totalSaveCount >= targetCount) {
                    break;
                }
                
                // API 호출 제한을 피하기 위해 페이지 간 대기
                Thread.sleep(200);
            }
            
            log.info(">>> '{}' 관련 도서 총 {}권 저장 및 임베딩 완료", query, totalSaveCount);
            return totalSaveCount;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("스레드 인터럽트 발생", e);
            return totalSaveCount;
        } catch (Exception e) {
            log.error("API 호출 중 오류 발생", e);
            return totalSaveCount;
        }
    }
    // 기존 데이터의 이미지 URL 업데이트 (imageUrl이 null이거나 비어있는 경우)
    @Transactional
    public int updateImageUrls(String query) {
        log.info(">>> '{}' 관련 도서 이미지 URL 업데이트 시작...", query);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", clientId);
        headers.set("X-Naver-Client-Secret", clientSecret);
        HttpEntity<String> request = new HttpEntity<>(headers);

        int updatedCount = 0;
        int maxPages = 5;
        int display = 100;
        
        try {
            for (int page = 1; page <= maxPages; page++) {
                int start = (page - 1) * display + 1;
                
                URI uri = UriComponentsBuilder
                        .fromUriString("https://openapi.naver.com")
                        .path("/v1/search/book.json")
                        .queryParam("query", query)
                        .queryParam("display", display)
                        .queryParam("start", start)
                        .queryParam("sort", "sim")
                        .encode()
                        .build()
                        .toUri();

                ResponseEntity<NaverBookSearchResponse> response = restTemplate.exchange(
                        uri, HttpMethod.GET, request, NaverBookSearchResponse.class
                );

                NaverBookSearchResponse body = response.getBody();
                if (body == null || body.getItems() == null || body.getItems().isEmpty()) {
                    log.info(">>> '{}' {}페이지: 더 이상 데이터가 없습니다.", query, page);
                    break;
                }

                List<NaverBookItem> items = body.getItems();
                log.info(">>> '{}' {}페이지: API에서 {}권 반환됨", query, page, items.size());

                for (NaverBookItem item : items) {
                    String imageUrl = item.getImage();
                    if (imageUrl == null || imageUrl.isEmpty()) {
                        continue;
                    }

                    Book existingBook = bookRepository.findById(item.getIsbn()).orElse(null);
                    if (existingBook != null && 
                            (existingBook.getImageUrl() == null || existingBook.getImageUrl().isEmpty())) {
                        // Builder 패턴으로 업데이트
                        Book updatedBook = Book.builder()
                                .isbn(existingBook.getIsbn())
                                .title(existingBook.getTitle())
                                .author(existingBook.getAuthor())
                                .imageUrl(imageUrl)
                                .category(existingBook.getCategory())
                                .publishedYear(existingBook.getPublishedYear())
                                .rating(existingBook.getRating())
                                .reviewCount(existingBook.getReviewCount())
                                .bestSeller(existingBook.isBestSeller())
                                .awardWinner(existingBook.isAwardWinner())
                                .build();
                        bookRepository.save(updatedBook);
                        updatedCount++;
                        log.debug(">>> 이미지 URL 업데이트: ISBN={}, ImageUrl={}", item.getIsbn(), imageUrl);
                    }
                }
                
                Thread.sleep(200);
            }
            
            log.info(">>> '{}' 관련 도서 이미지 URL {}건 업데이트 완료", query, updatedCount);
            return updatedCount;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("스레드 인터럽트 발생", e);
            return updatedCount;
        } catch (Exception e) {
            log.error("이미지 URL 업데이트 중 오류 발생", e);
            return updatedCount;
        }
    }

    private String cleanHtml(String input) {
        if (input == null) return "";
        return input.replaceAll("<[^>]*>",""); // 정규식으로 태그 제거
    }
}
