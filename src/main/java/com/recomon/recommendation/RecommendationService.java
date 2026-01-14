package com.recomon.recommendation;

import com.recomon.domain.Book;
import com.recomon.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * packageName    : com.recomon.service
 * fileName       : RecommendationService
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
public class RecommendationService {

    private final BookRepository bookRepository;
    private final VectorStore vectorStore; // 백터 저장소

    @Transactional(readOnly = true)
    public List<Book> recommendBooks(List<String> userSelectedIsbns) {

        // 1. 유저가 선택한 책들의 정보를 DB에서 가져옴
        List<Book> selectedBooks = bookRepository.findAllById(userSelectedIsbns);

        if (selectedBooks.isEmpty()) {
            throw new IllegalArgumentException("선택된 도서가 없습니다.");
        }

        // 2. 검색 쿼리 만들기
        // 유저가 고른 책들의 제목과 카테고리를 합쳐서 하나의 긴 텍스트로 만듭니다.
        // AI는 이 텍스트와 "의미적으로 가장 가까운" 다른 책을 찾아줄 겁니다.
        // OpenAI embedding 모델의 최대 컨텍스트 길이는 8192 토큰입니다.
        // 한국어는 1자당 약 1-2 토큰으로 변환되므로, 안전하게 4000자로 제한합니다.
        String userPreferenceText = selectedBooks.stream()
                .map(book -> {
                    StringBuilder sb = new StringBuilder();
                    if (book.getCategory() != null && !book.getCategory().isEmpty()) {
                        sb.append(book.getCategory()).append(" ");
                    }
                    if (book.getTitle() != null && !book.getTitle().isEmpty()) {
                        String title = book.getTitle();
                        // 제목이 너무 길면 최대 50자로 제한
                        if (title.length() > 50) {
                            title = title.substring(0, 50);
                        }
                        sb.append(title);
                    }
                    return sb.toString().trim();
                })
                .filter(text -> !text.isEmpty())
                .collect(Collectors.joining(" "));
        
        // 전체 텍스트 길이를 4000자로 제한 (약 6000-8000 토큰, 안전 마진 포함)
        if (userPreferenceText.length() > 4000) {
            userPreferenceText = userPreferenceText.substring(0, 4000);
        }

        // 3. 백터 유사도 검색 (Similarity Search) - 핵심 로직!
        // 상위 10개를 검색합니다.
        try {
        List<Document> similarDocuments = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(userPreferenceText)
                        .topK(10) // 상위 10개
                        .build()
        );

        // 4. 검색된 Document에서 ISBN을 꺼내서 다시 Book 엔티티로 변환
        List<String> recommendedIsbns = similarDocuments.stream()
                .map(doc -> (String) doc.getMetadata().get("isbn"))
                .filter(isbn -> !userSelectedIsbns.contains(isbn)) // 이미 읽은 책 제외
                .collect(Collectors.toList());

        // 5. DB에서 상세 정보 조회 후 반환
        return bookRepository.findAllById(recommendedIsbns);
        } catch (Exception e) {
            log.error("벡터 유사도 검색 중 오류 발생: {}", e.getMessage(), e);
            // API 오류 시 빈 리스트 반환 (사용자에게는 다른 방식으로 추천 제공)
            return new ArrayList<>();
        }
    }

    // IntentWeightPolicy를 활용한 추천 (오버로드 메서드)
    @Transactional(readOnly = true)
    public List<Book> recommendBooks(List<String> userSelectedIsbns, List<RecommendIntent> intents) {
        // 1. 기본 벡터 유사도 검색으로 후보 도서 찾기
        List<Book> candidateBooks = recommendBooks(userSelectedIsbns);

        if (candidateBooks.isEmpty() || intents == null || intents.isEmpty()) {
            return candidateBooks;
        }

        // 2. IntentWeightPolicy 생성
        IntentWeightPolicy policy = IntentWeightPolicy.from(intents);

        // 3. 각 도서에 대해 점수 계산 및 정렬
        Map<Book, Double> bookScores = new HashMap<>();
        for (Book book : candidateBooks) {
            Double score = policy.score(book);
            if (score != null && score > 0) {
                bookScores.put(book, score);
            }
        }

        // 4. 점수 순으로 정렬 (내림차순)
        return candidateBooks.stream()
                .sorted((b1, b2) -> {
                    Double score1 = bookScores.getOrDefault(b1, 0.0);
                    Double score2 = bookScores.getOrDefault(b2, 0.0);
                    return Double.compare(score2, score1); // 내림차순
                })
                .collect(Collectors.toList());
    }
}
