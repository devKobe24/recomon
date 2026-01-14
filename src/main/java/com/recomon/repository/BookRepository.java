package com.recomon.repository;

import com.recomon.domain.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * packageName    : com.recomon.repository
 * fileName       : BookRepository
 * author         : kobe
 * date           : 2026. 1. 11.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026. 1. 11.        kobe       최초 생성
 */
@Repository
public interface BookRepository extends JpaRepository<Book, String> {

    // 1. 유사성 추천용: 내가 좋아하는 카테고리의 책들 찾기
    List<Book> findByCategoryIn(List<String> categories);

    // 2. 의외성 추천용: 내가 좋아하는 카테고리가 '아닌' 책들 찾기
    List<Book> findByCategoryNotIn(List<String> categories);
    
    // 3. 카테고리별로 최대 20권 조회 (Pageable 사용)
    Page<Book> findByCategoryOrderByIsbn(String category, Pageable pageable);
    
    // 4. 카테고리별로 최대 20권 조회 (간편 버전)
    List<Book> findTop20ByCategoryOrderByIsbnAsc(String category);
}
