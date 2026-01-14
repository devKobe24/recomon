package com.recomon.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PostgreSQL 벡터 스토어 설정
 * Spring AI 1.0.0-M6에서 spring.ai.vectorstore.pgvector.datasource 설정이
 * 제대로 작동하지 않아 수동으로 구성합니다.
 */
@Slf4j
@Configuration
public class PgVectorStoreConfig {

    @Value("${spring.ai.vectorstore.pgvector.datasource.url}")
    private String pgVectorUrl;

    @Value("${spring.ai.vectorstore.pgvector.datasource.username}")
    private String pgVectorUsername;

    @Value("${spring.ai.vectorstore.pgvector.datasource.password}")
    private String pgVectorPassword;

    @Value("${spring.ai.vectorstore.pgvector.dimensions}")
    private int dimensions;

    @Value("${spring.ai.vectorstore.pgvector.index-type}")
    private String indexType;

    @Value("${spring.ai.vectorstore.pgvector.distance-type}")
    private String distanceType;


    @Bean
    @Qualifier("pgVectorDataSource")
    // @Primary가 없으므로 VectorStore 전용입니다
    public DataSource pgVectorDataSource() {
        // 데이터베이스가 없으면 생성
        createDatabaseIfNotExists();
        
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(pgVectorUrl);
        dataSource.setUsername(pgVectorUsername);
        dataSource.setPassword(pgVectorPassword);
        dataSource.setDriverClassName("org.postgresql.Driver");
        return dataSource;
    }

    private void createDatabaseIfNotExists() {
        try {
            // URL에서 데이터베이스 이름 추출
            String databaseName = extractDatabaseName(pgVectorUrl);
            if (databaseName == null) {
                log.warn("데이터베이스 이름을 추출할 수 없습니다. URL: {}", pgVectorUrl);
                return;
            }

            // URL에서 서버 URL 추출 (postgres 데이터베이스로 연결)
            String serverUrl = buildServerUrl(pgVectorUrl);
            if (serverUrl == null) {
                log.warn("서버 URL을 생성할 수 없습니다. URL: {}", pgVectorUrl);
                return;
            }

            log.info("PostgreSQL 서버에 연결하여 데이터베이스 '{}' 존재 여부 확인 중... (서버: {})", databaseName, serverUrl);
            
            try (var connection = java.sql.DriverManager.getConnection(serverUrl, pgVectorUsername, pgVectorPassword);
                 var statement = connection.createStatement()) {
                
                log.info("PostgreSQL 서버 연결 성공");
                
                // 데이터베이스 존재 여부 확인
                boolean dbExists = false;
                try (var rs = statement.executeQuery(
                        "SELECT 1 FROM pg_database WHERE datname = '" + databaseName + "'")) {
                    dbExists = rs.next();
                }

                if (!dbExists) {
                    log.info("PostgreSQL 데이터베이스 '{}'가 존재하지 않습니다. 생성합니다...", databaseName);
                    statement.executeUpdate("CREATE DATABASE \"" + databaseName + "\"");
                    log.info("PostgreSQL 데이터베이스 '{}'가 성공적으로 생성되었습니다.", databaseName);
                } else {
                    log.info("PostgreSQL 데이터베이스 '{}'가 이미 존재합니다.", databaseName);
                }
            }
        } catch (java.sql.SQLException e) {
            // 연결 관련 오류 확인
            Throwable cause = e.getCause();
            if (cause instanceof java.net.ConnectException || 
                e.getMessage() != null && (e.getMessage().contains("Connection refused") || 
                                           e.getMessage().contains("Connection timed out"))) {
                log.error("PostgreSQL 서버에 연결할 수 없습니다. RDS 보안 그룹 설정과 네트워크 연결을 확인하세요.");
                log.error("오류 메시지: {}", e.getMessage());
                log.warn("데이터베이스 자동 생성이 실패했습니다. 데이터베이스가 이미 존재하거나 수동으로 생성해야 할 수 있습니다.");
            } else {
                log.error("데이터베이스 생성 중 SQL 오류 발생: {}", e.getMessage(), e);
            }
            // 연결 실패 시에도 계속 진행 (데이터베이스가 이미 존재할 수 있음)
        } catch (Exception e) {
            log.error("PostgreSQL 데이터베이스 생성 중 오류 발생: {}", e.getMessage(), e);
            log.error("스택 트레이스:", e);
            log.warn("데이터베이스 자동 생성이 실패했지만 애플리케이션을 계속 시작합니다. 데이터베이스가 이미 존재하는지 확인하세요.");
        }
    }

    private String extractDatabaseName(String url) {
        // jdbc:postgresql://host:port/database 형식에서 데이터베이스 이름 추출
        Pattern pattern = Pattern.compile("jdbc:postgresql://[^/]+/([^?]+)");
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
            // jdbc:postgresql://host:port/database?params 형식 파싱
            Pattern pattern = Pattern.compile("jdbc:postgresql://([^/]+)(?:/([^?]+))?(?:\\?(.+))?");
            Matcher matcher = pattern.matcher(url);
            
            if (matcher.find()) {
                String hostPort = matcher.group(1); // host:port
                String queryParams = matcher.group(3); // 쿼리 파라미터
                
                // 서버 URL 생성 (postgres 데이터베이스로 연결)
                String serverUrl = "jdbc:postgresql://" + hostPort + "/postgres";
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

    @Bean
    @Qualifier("pgVectorJdbcTemplate")
    public JdbcTemplate pgVectorJdbcTemplate(@Qualifier("pgVectorDataSource") DataSource pgVectorDataSource) {
        return new JdbcTemplate(pgVectorDataSource);
    }


    private void initializeVectorStoreTableIfNeeded(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        try {
            // pgvector 확장이 있는지 확인하고 없으면 생성
            jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS vector");
            log.info("pgvector 확장이 준비되었습니다.");
            
            // vector_store 테이블이 있는지 확인
            boolean tableExists = false;
            try (var connection = dataSource.getConnection()) {
                DatabaseMetaData metaData = connection.getMetaData();
                ResultSet tables = metaData.getTables(null, "public", "vector_store", null);
                tableExists = tables.next();
            }
            
            if (!tableExists) {
                log.info("vector_store 테이블이 존재하지 않습니다. 테이블을 생성합니다.");
                
                // vector_store 테이블 생성
                String createTableSql = String.format("""
                    CREATE TABLE IF NOT EXISTS vector_store (
                        id VARCHAR(255) PRIMARY KEY,
                        content TEXT,
                        metadata JSONB,
                        embedding vector(%d)
                    )
                    """, dimensions);
                
                jdbcTemplate.execute(createTableSql);
                
                // 인덱스 생성 (HNSW 또는 IVFFlat)
                String indexTypeSql = indexType.equals("HNSW") 
                    ? "CREATE INDEX IF NOT EXISTS vector_store_embedding_idx ON vector_store USING hnsw (embedding vector_cosine_ops)" 
                    : "CREATE INDEX IF NOT EXISTS vector_store_embedding_idx ON vector_store USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100)";
                
                jdbcTemplate.execute(indexTypeSql);
                
                log.info("vector_store 테이블과 인덱스가 성공적으로 생성되었습니다.");
            } else {
                log.info("vector_store 테이블이 이미 존재합니다.");
            }
        } catch (Exception e) {
            log.error("vector_store 테이블 초기화 중 오류가 발생했습니다: {}", e.getMessage(), e);
            throw new RuntimeException("vector_store 테이블 초기화 실패", e);
        }
    }

    @Bean
    @Primary
    public VectorStore vectorStore(EmbeddingModel embeddingModel,
                                   @Qualifier("pgVectorJdbcTemplate") JdbcTemplate jdbcTemplate,
                                   @Qualifier("pgVectorDataSource") DataSource pgVectorDataSource) {
        // VectorStore Bean 생성 전에 테이블 초기화
        initializeVectorStoreTableIfNeeded(jdbcTemplate, pgVectorDataSource);
        
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .dimensions(dimensions)
                .distanceType(PgVectorStore.PgDistanceType.valueOf(distanceType))
                .indexType(PgVectorStore.PgIndexType.valueOf(indexType))
                .build();
    }
}
