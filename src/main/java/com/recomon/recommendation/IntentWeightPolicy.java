package com.recomon.recommendation;

import com.recomon.domain.Book;

import java.time.Year;
import java.util.List;

/**
 * packageName    : com.recomon.recommendation
 * fileName       : IntentWeightPolicy
 * author         : kobe
 * date           : 2026. 1. 13.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026. 1. 13.        kobe       최초 생성
 */
public class IntentWeightPolicy {

    private Double bestSellerWeight = 0.0;
    private Double newReleasesWeight = 0.0;
    private Double reviewWeight = 0.0;
    private Double ratingWeight = 0.0;
    private Double awardWeight = 0.0;

    private IntentWeightPolicy() {
        // 외부 생성 차단
    }

    // 팩토리 메서드
    public static IntentWeightPolicy from(List<RecommendIntent> intents) {
        IntentWeightPolicy policy = new IntentWeightPolicy();

        if (intents == null || intents.isEmpty()) {
            return policy; // 모든 가중치 0
        }

        for (RecommendIntent intent : intents) {
            switch (intent) {
                case BEST_SELLER -> policy.bestSellerWeight += 0.3;
                case NEW_RELEASE -> policy.newReleasesWeight += 0.3;
                case MANY_REVIEWS -> policy.reviewWeight += 0.3;
                case HIGH_RATING -> policy.ratingWeight += 0.3;
                case AWARD_WINNER -> policy.awardWeight += 0.3;
            }
        }

        return policy;
    }

    // 측정 도서에 대해 추천 목적 기반 점수를 계산
    public Double score(Book book) {

        double score = 0;

        // 1. 베스트샐러
        if (book.isBestSeller()) {
            score += bestSellerWeight;
        }

        // 2. 신작 (최근 2년)
        if (isNewRelease(book)) {
            score += newReleasesWeight;
        }

        // 3. 리뷰 수 (로그 스케일)
        score += Math.log(book.getReviewCount() + 1)
                * reviewWeight;

        // 4. 평정 (0.0 ~ 5.0 -> 0.0 ~ 1.0 정규화)
        score += (book.getRating() / 5.0)
                * ratingWeight;

        // 5. 수상작
        if (book.isAwardWinner()) {
            score += awardWeight;
        }

        return score;
    }

    private boolean isNewRelease(Book book) {
        int currentYear = Year.now().getValue();
        return book.getPublishedYear() >= currentYear - 2;
    }
}
