package com.recomon.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MySQL 데이터소스 설정 (JPA용)
 * @Primary로 설정하여 JPA/Hibernate가 이 DataSource를 기본으로 사용합니다.
 */
@Slf4j
@Configuration
public class DataSourceConfig {

    @Value("${spring.datasource.url}")
    private String mysqlUrl;

    @Value("${spring.datasource.username}")
    private String mysqlUsername;

    @Value("${spring.datasource.password}")
    private String mysqlPassword;

    @Bean
    @Primary
    public DataSource dataSource() {
        // 데이터베이스가 없으면 생성
        createDatabaseIfNotExists();
        
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(mysqlUrl);
        dataSource.setUsername(mysqlUsername);
        dataSource.setPassword(mysqlPassword);
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        return dataSource;
    }

    private void createDatabaseIfNotExists() {
        try {
            // URL에서 데이터베이스 이름 추출
            String databaseName = extractDatabaseName(mysqlUrl);
            if (databaseName == null) {
                log.warn("데이터베이스 이름을 추출할 수 없습니다. URL: {}", mysqlUrl);
                return;
            }

            // URL에서 서버 URL 추출 (데이터베이스 이름과 쿼리 파라미터 제거)
            String serverUrl = buildServerUrl(mysqlUrl);
            if (serverUrl == null) {
                log.warn("서버 URL을 생성할 수 없습니다. URL: {}", mysqlUrl);
                return;
            }

            log.info("MySQL 서버에 연결하여 데이터베이스 '{}' 존재 여부 확인 중... (서버: {})", databaseName, serverUrl);
            
            try (Connection connection = DriverManager.getConnection(serverUrl, mysqlUsername, mysqlPassword);
                 Statement statement = connection.createStatement()) {
                
                log.info("MySQL 서버 연결 성공");
                
                // 데이터베이스 존재 여부 확인
                boolean dbExists = false;
                try (var rs = statement.executeQuery(
                        "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = '" + databaseName + "'")) {
                    dbExists = rs.next();
                }

                if (!dbExists) {
                    log.info("데이터베이스 '{}'가 존재하지 않습니다. 생성합니다...", databaseName);
                    statement.executeUpdate("CREATE DATABASE `" + databaseName + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
                    log.info("데이터베이스 '{}'가 성공적으로 생성되었습니다.", databaseName);
                } else {
                    log.info("데이터베이스 '{}'가 이미 존재합니다.", databaseName);
                }
            }
        } catch (java.sql.SQLException e) {
            // 연결 관련 오류 확인
            Throwable cause = e.getCause();
            if (cause instanceof java.net.ConnectException || 
                e.getMessage() != null && (e.getMessage().contains("Connection refused") || 
                                           e.getMessage().contains("Communications link failure"))) {
                log.error("MySQL 서버에 연결할 수 없습니다. RDS 보안 그룹 설정과 네트워크 연결을 확인하세요.");
                log.error("오류 메시지: {}", e.getMessage());
                log.warn("데이터베이스 자동 생성이 실패했습니다. 데이터베이스가 이미 존재하거나 수동으로 생성해야 할 수 있습니다.");
            } else {
                log.error("데이터베이스 생성 중 SQL 오류 발생: {}", e.getMessage(), e);
            }
            // 연결 실패 시에도 계속 진행 (데이터베이스가 이미 존재할 수 있음)
        } catch (Exception e) {
            log.error("데이터베이스 생성 중 오류 발생: {}", e.getMessage(), e);
            log.error("스택 트레이스:", e);
            // 기타 오류는 경고만 남기고 계속 진행 (데이터베이스가 이미 존재할 수 있음)
            log.warn("데이터베이스 자동 생성이 실패했지만 애플리케이션을 계속 시작합니다. 데이터베이스가 이미 존재하는지 확인하세요.");
        }
    }

    private String extractDatabaseName(String url) {
        // jdbc:mysql://host:port/database?params 형식에서 데이터베이스 이름 추출
        Pattern pattern = Pattern.compile("jdbc:mysql://[^/]+/([^?]+)");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            String dbName = matcher.group(1);
            log.debug("추출된 데이터베이스 이름: {}", dbName);
            return dbName;
        }
        return null;
    }

    private String buildServerUrl(String url) {
        try {
            // jdbc:mysql://host:port/database?params 형식 파싱
            Pattern pattern = Pattern.compile("jdbc:mysql://([^/]+)(?:/([^?]+))?(?:\\?(.+))?");
            Matcher matcher = pattern.matcher(url);
            
            if (matcher.find()) {
                String hostPort = matcher.group(1); // host:port
                String queryParams = matcher.group(3); // 쿼리 파라미터
                
                // 서버 URL 생성 (데이터베이스 이름 없이)
                String serverUrl = "jdbc:mysql://" + hostPort;
                if (queryParams != null && !queryParams.isEmpty()) {
                    serverUrl += "?" + queryParams;
                }
                
                log.debug("생성된 서버 URL: {}", serverUrl);
                return serverUrl;
            }
        } catch (Exception e) {
            log.error("URL 파싱 중 오류 발생: {}", e.getMessage(), e);
        }
        return null;
    }
}
