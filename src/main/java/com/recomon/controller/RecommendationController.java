package com.recomon.controller;

import com.recomon.domain.Book;
import com.recomon.recommendation.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * packageName    : com.recomon.controller
 * fileName       : RecommendationController
 * author         : kobe
 * date           : 2026. 1. 11.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026. 1. 11.        kobe       최초 생성
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recommend")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @PostMapping
    public List<Book> getRecommendations(@RequestBody List<String> isbnList) {
        return recommendationService.recommendBooks(isbnList);
    }
}
