package com.recomon.recommendation;

/**
 * packageName    : com.recomon.recommendation
 * fileName       : RecommendIntent
 * author         : kobe
 * date           : 2026. 1. 13.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026. 1. 13.        kobe       최초 생성
 */
public enum RecommendIntent {
    BEST_SELLER, // 베스트 셀러 중심 추천
    NEW_RELEASE, // 최근 출간된 작품 중심 추천
    MANY_REVIEWS, // 리뷰 수가 많은 작품 중심 추천
    HIGH_RATING, // 평점이 높은 작품 중심 추천
    AWARD_WINNER // 문학상/전문상 수상작 중심 추천
}
