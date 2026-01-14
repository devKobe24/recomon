package com.recomon.recommendation;

import com.recomon.domain.Book;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * packageName    : com.recomon.recommendation
 * fileName       : CategoryWeightCalculator
 * author         : kobe
 * date           : 2026. 1. 13.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026. 1. 13.        kobe       최초 생성
 */
@Component
public class CategoryWeightCalculator {

    // 전체 카테고리 수
    private static final int TOTAL_CATEGORY_COUNT = 26;

    // 50% 이상일 때 추가 가중치 계수
    private static final double EXTRA_ALPHA = 0.5;

    // 추가 가중치 최대값(독점 방지)
    private static final double MAX_EXTRA_WEIGHT = 0.6;

    // 카테고리 가중치 계산 메인 메서드
    public Map<String, Double> calculate(List<Book> selectedBooks) {

        if (selectedBooks == null || selectedBooks.isEmpty()) {
            return Map.of(); // 선택 없음
        }

        // 카테고리별 선택 개수 집계
        Map<String, Long> categoryCountMap = selectedBooks.stream().collect(Collectors.groupingBy(Book::getCategory, Collectors.counting()));
        int totalSelected = selectedBooks.size();
        Map<String, Double> categoryWeights = new HashMap<>();

        // 카테고리별 비율 -> 가중치 변환
        for (Map.Entry<String, Long> entry : categoryCountMap.entrySet()) {
            String category = entry.getKey();
            long count = entry.getValue();

            double ratio = (double) count / totalSelected;
            double weight = calculateWeightByRatio(ratio);
        }

        return categoryWeights;
    }

    private double calculateWeightByRatio(double ratio) {

        // 10% 이하 선호하지 않음
        if (ratio <= 0.10) {
            return 0.0;
        }

        // 11% ~ 19%
        if (ratio <= 0.19) {
            return 0.1;
        }

        // 20% ~ 29%
        if (ratio <= 0.29) {
            return 0.2;
        }

        // 30% ~ 39%
        if (ratio <= 0.39) {
            return 0.3;
        }

        // 40% ~ 49%
        if (ratio <= 0.49) {
            return 0.4;
        }

        // 50% 이상 -> 평균 대비 초과분 + 상한선
        double averageRatio = 1.0 / TOTAL_CATEGORY_COUNT;

        double extraWeight = Math.min((ratio - averageRatio) * EXTRA_ALPHA, MAX_EXTRA_WEIGHT);

        return 0.4 + extraWeight;
    }
}
