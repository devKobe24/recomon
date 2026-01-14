package com.recomon.recommendation.dto;

import com.recomon.domain.Book;

/**
 * packageName    : com.recomon.recommendation.dto
 * fileName       : ScoredBook
 * author         : kobe
 * date           : 2026. 1. 13.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026. 1. 13.        kobe       최초 생성
 */
public class ScoredBook {

    private final Book book;
    private final double score;

    private ScoredBook(Book book, double score) {
        this.book = book;
        this.score = score;
    }

    public static ScoredBook of(Book book, double score) {
        return new ScoredBook(book, score);
    }

    public Book book() {
        return book;
    }

    public double score() {
        return score;
    }
}
