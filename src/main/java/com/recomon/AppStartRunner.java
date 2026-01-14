package com.recomon;

import com.recomon.service.BookCollectorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * packageName    : com.recomon
 * fileName       : AppStartRunner
 * author         : kobe
 * date           : 2026. 1. 11.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026. 1. 11.        kobe       최초 생성
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AppStartRunner implements CommandLineRunner {

    private final BookCollectorService bookCollectorService;

    @Override
    public void run(String... args) throws Exception {
        log.info(">>> [초기 데이터 수집] 서버 시작 시 도서 데이터를 수집합니다...");

        // 1. 수집하고 싶은 카테고리(키워드) 목록 정의
        List<String> keywords = Arrays.asList(
                "소설", "시", "에세이", "인문", "요리", "건강", "취미", "실용", "스포츠", "경제", "경영", "자기계발", "정치", "사회",
                "역사", "문화", "종교", "기술", "공학", "외국어", "과학", "여행", "컴퓨터", "IT", "만화", "대학교재"
        );

        // 2. 각 키워드별로 API 호출하여 저장
        for (String keyword : keywords) {
            int count = bookCollectorService.collectBooks(keyword);

            // 네이버 API 호출 제한(초당 10회 등)을 피하기 위해 0.5초 쉬어줍니다.
            Thread.sleep(500);
        }

        log.info(">>> [초기 데이터 수집] 모든 작업이 완료되었습니다!");
    }
}
