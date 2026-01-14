package com.recomon.controller;

import com.recomon.repository.BookRepository;
import com.recomon.service.BookCollectorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 관리자용 컨트롤러 - 데이터 수집 및 확인
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final BookRepository bookRepository;
    private final BookCollectorService bookCollectorService;

    // 카테고리별 책 개수 확인
    @GetMapping("/book-count")
    public ResponseEntity<Map<String, Object>> getBookCount() {
        List<String> categories = Arrays.asList(
            "소설", "시", "에세이", "인문", "요리", "건강", "취미", "실용", "스포츠", 
            "경제", "경영", "자기계발", "정치", "사회", "역사", "문화", "종교", "기술", 
            "공학", "외국어", "과학", "여행", "컴퓨터", "IT", "만화", "대학교재"
        );

        Map<String, Long> countByCategory = new HashMap<>();
        long totalCount = 0;

        for (String category : categories) {
            long count = bookRepository.findByCategoryOrderByIsbn(category, 
                PageRequest.of(0, 1)).getTotalElements();
            countByCategory.put(category, count);
            totalCount += count;
        }

        Map<String, Object> response = new HashMap<>();
        response.put("countByCategory", countByCategory);
        response.put("totalCount", totalCount);

        return ResponseEntity.ok(response);
    }

    // 특정 카테고리 데이터 수집 (GET 또는 POST 모두 허용)
    @RequestMapping(value = "/collect/{category}", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<Map<String, Object>> collectBooks(@PathVariable String category) {
        try {
            int count = bookCollectorService.collectBooks(category);
            Map<String, Object> response = new HashMap<>();
            response.put("category", category);
            response.put("collectedCount", count);
            response.put("message", "데이터 수집 완료");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("데이터 수집 실패: {}", category, e);
            Map<String, Object> response = new HashMap<>();
            response.put("category", category);
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // 모든 카테고리 데이터 수집 (GET 또는 POST 모두 허용)
    @RequestMapping(value = "/collect-all", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<Map<String, Object>> collectAllBooks() {
        List<String> categories = Arrays.asList(
            "소설", "시", "에세이", "인문", "요리", "건강", "취미", "실용", "스포츠", 
            "경제", "경영", "자기계발", "정치", "사회", "역사", "문화", "종교", "기술", 
            "공학", "외국어", "과학", "여행", "컴퓨터", "IT", "만화", "대학교재"
        );

        Map<String, Integer> results = new HashMap<>();
        int totalCollected = 0;

        for (String category : categories) {
            try {
                int count = bookCollectorService.collectBooks(category);
                results.put(category, count);
                totalCollected += count;

                // API 호출 제한을 피하기 위해 대기
                Thread.sleep(500);
            } catch (Exception e) {
                log.error("데이터 수집 실패: {}", category, e);
                results.put(category, -1); // 에러 표시
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("results", results);
        response.put("totalCollected", totalCollected);
        response.put("message", "모든 카테고리 데이터 수집 완료");

        return ResponseEntity.ok(response);
    }

    // 기존 데이터의 이미지 URL 업데이트 (GET 또는 POST 모두 허용)
    @RequestMapping(value = "/update-images/{category}", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<Map<String, Object>> updateImages(@PathVariable String category) {
        try {
            int updatedCount = bookCollectorService.updateImageUrls(category);
            Map<String, Object> response = new HashMap<>();
            response.put("category", category);
            response.put("updatedCount", updatedCount);
            response.put("message", "이미지 URL 업데이트 완료");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("이미지 URL 업데이트 실패: {}", category, e);
            Map<String, Object> response = new HashMap<>();
            response.put("category", category);
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // 모든 카테고리의 이미지 URL 업데이트 (GET 또는 POST 모두 허용)
    @RequestMapping(value = "/update-all-images", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<Map<String, Object>> updateAllImages() {
        List<String> categories = Arrays.asList(
            "소설", "시", "에세이", "인문", "요리", "건강", "취미", "실용", "스포츠", 
            "경제", "경영", "자기계발", "정치", "사회", "역사", "문화", "종교", "기술", 
            "공학", "외국어", "과학", "여행", "컴퓨터", "IT", "만화", "대학교재"
        );

        Map<String, Integer> results = new HashMap<>();
        int totalUpdated = 0;

        for (String category : categories) {
            try {
                int count = bookCollectorService.updateImageUrls(category);
                results.put(category, count);
                totalUpdated += count;

                // API 호출 제한을 피하기 위해 대기
                Thread.sleep(500);
            } catch (Exception e) {
                log.error("이미지 URL 업데이트 실패: {}", category, e);
                results.put(category, -1); // 에러 표시
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("results", results);
        response.put("totalUpdated", totalUpdated);
        response.put("message", "모든 카테고리 이미지 URL 업데이트 완료");

        return ResponseEntity.ok(response);
    }
}
