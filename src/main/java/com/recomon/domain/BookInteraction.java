package com.recomon.domain;

import jakarta.persistence.*;

/**
 * packageName    : com.recomon.domain
 * fileName       : BookInteraction
 * author         : kobe
 * date           : 2026. 1. 11.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026. 1. 11.        kobe       최초 생성
 */
@Entity
public class BookInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    private Book book;

    @Enumerated(EnumType.STRING)
    private InteractionType type;
}
