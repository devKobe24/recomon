package com.recomon.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * packageName    : com.recomon.dto
 * fileName       : NaverBookSearchResponse
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
public class NaverBookSearchResponse {
    private List<NaverBookItem> items;
}
