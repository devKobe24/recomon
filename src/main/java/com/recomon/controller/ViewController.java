package com.recomon.controller;

import com.recomon.domain.Book;
import com.recomon.repository.BookRepository;
import com.recomon.recommendation.RecommendIntent;
import com.recomon.recommendation.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

/**
 * packageName    : com.recomon.controller
 * fileName       : ViewController
 * author         : kobe
 * date           : 2026. 1. 12.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026. 1. 12.        kobe       최초 생성
 */
@Controller
@RequiredArgsConstructor
public class ViewController {

    private final BookRepository bookRepository;
    private final RecommendationService recommendationService;

    // 기준 선택 페이지 (메인 화면)
    @GetMapping("/")
    public String intentSelection() {
        return "intent-selection";
    }

    // 기준 선택 처리
    @PostMapping("/intent-selection")
    public String handleIntentSelection(@RequestParam(value = "intents", required = false) List<String> intentStrings, HttpSession session) {
        List<RecommendIntent> intents = new ArrayList<>();
        
        // 선택한 기준이 있으면 변환하여 저장
        if (intentStrings != null && !intentStrings.isEmpty()) {
            intents = intentStrings.stream()
                    .map(RecommendIntent::valueOf)
                    .collect(Collectors.toList());
        }
        
        // 세션에 저장 (비어있어도 저장)
        session.setAttribute("selectedIntents", intents);
        
        // 도서 선택 페이지로 리다이렉트
        return "redirect:/books";
    }

    // 도서 선택 페이지: DB에 있는 책 목록을 보여줌 (유저가 고를 수 있게)
    @GetMapping("/books")
    public String index(Model model, HttpSession session) {
        // 세션에서 선택한 기준 확인 (없으면 빈 리스트로 처리)
        @SuppressWarnings("unchecked")
        List<RecommendIntent> selectedIntents = (List<RecommendIntent>) session.getAttribute("selectedIntents");
        if (selectedIntents == null) {
            // 세션이 없으면 빈 리스트로 초기화
            selectedIntents = new ArrayList<>();
            session.setAttribute("selectedIntents", selectedIntents);
        }
        // 카테고리 목록
        List<String> categories = Arrays.asList(
            "소설", "시", "에세이", "인문", "요리", "건강", "취미", "실용", "스포츠", 
            "경제", "경영", "자기계발", "정치", "사회", "역사", "문화", "종교", "기술", 
            "공학", "외국어", "과학", "여행", "컴퓨터", "IT", "만화", "대학교재"
        );
        
        // 카테고리별로 최대 20권씩 조회하여 모두 합치기
        List<Book> allBooks = new ArrayList<>();
        Pageable pageable = PageRequest.of(0, 20, Sort.by("isbn").ascending());
        
        for (String category : categories) {
            List<Book> books = bookRepository.findByCategoryOrderByIsbn(category, pageable).getContent();
            allBooks.addAll(books);
        }
        
        // 무작위로 섞기
        Collections.shuffle(allBooks);
        
        // 20개씩 나누어서 List<List<Book>> 형태로 생성
        List<List<Book>> bookRows = new ArrayList<>();
        int itemsPerRow = 20;
        for (int i = 0; i < allBooks.size(); i += itemsPerRow) {
            int end = Math.min(i + itemsPerRow, allBooks.size());
            bookRows.add(allBooks.subList(i, end));
        }
        
        model.addAttribute("bookRows", bookRows);
        return "index"; // index.html을 보여줘라
    }

    // 결과 화면: 유저가 선택한 책(ISBN)을 받아서 추천 결과를 보여줌
    @PostMapping("/recommend-view")
    public String recommend(@RequestParam("isbns") List<String> isbns, Model model, HttpSession session) {
        // 세션에서 선택한 기준 가져오기 (없으면 빈 리스트)
        @SuppressWarnings("unchecked")
        List<RecommendIntent> selectedIntents = (List<RecommendIntent>) session.getAttribute("selectedIntents");
        if (selectedIntents == null) {
            selectedIntents = new ArrayList<>();
        }
        
        String errorMessage = null;
        List<Book> recommendations = new ArrayList<>();
        
        try {
            // 1. 추천 서비스 호출 (선택한 기준 전달)
            recommendations = recommendationService.recommendBooks(isbns, selectedIntents);
            
            // 추천 결과가 없으면 대체 추천 제공
            if (recommendations.isEmpty()) {
                // 선택한 책들의 카테고리와 다른 카테고리의 책들을 추천
                List<Book> selectedBooks = bookRepository.findAllById(isbns);
                if (!selectedBooks.isEmpty()) {
                    Set<String> selectedCategories = selectedBooks.stream()
                            .map(Book::getCategory)
                            .filter(cat -> cat != null && !cat.isEmpty())
                            .collect(java.util.stream.Collectors.toSet());
                    
                    recommendations = bookRepository.findByCategoryNotIn(new ArrayList<>(selectedCategories))
                            .stream()
                            .limit(10)
                            .collect(java.util.stream.Collectors.toList());
                }
                
                if (recommendations.isEmpty()) {
                    errorMessage = "AI 추천 서비스에 일시적인 문제가 발생했습니다. 잠시 후 다시 시도해주세요.";
                }
            }
        } catch (Exception e) {
            errorMessage = "추천 서비스 처리 중 오류가 발생했습니다: " + e.getMessage();
        }

        // 2. 결과 데이터를 모델에 담음
        model.addAttribute("recommendations", recommendations);
        model.addAttribute("errorMessage", errorMessage);

        return "result"; // result.html을 보여줘라
    }
}
