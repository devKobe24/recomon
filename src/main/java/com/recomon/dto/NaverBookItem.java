package com.recomon.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * packageName    : com.recomon.dto
 * fileName       : NaverBookItem
 * author         : kobe
 * date           : 2026. 1. 11.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026. 1. 11.        kobe       최초 생성
 */
@Getter
@Setter
@ToString
public class NaverBookItem {
    private String title;
    private String link;
    private String image;
    private String author;
    private String discount;
    private String publisher;
    private String isbn;
    private String description;
    private String pubdate;
}
