package com.recomon.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * packageName    : com.recomon.domain
 * fileName       : Book
 * author         : kobe
 * date           : 2026. 1. 10.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026. 1. 10.        kobe       최초 생성
 */
@Entity
@Table(name = "books")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Book {

    @Id
    @Column(length = 20)
    private String isbn;

    private String title;
    private String author;
    private String imageUrl;

    private String category;

    private int publishedYear;
    private double rating;
    private int reviewCount;

    private boolean bestSeller;
    private boolean awardWinner;
}
