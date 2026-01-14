# 📚 Recomon - AI 기반 도서 추천 시스템

> Spring AI와 Vector Search를 활용한 지능형 도서 추천 플랫폼

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-brightgreen)
![Spring AI](https://img.shields.io/badge/Spring%20AI-1.0.0--M6-blue)
![License](https://img.shields.io/badge/License-MIT-yellow)

## 📋 목차

- [프로젝트 소개](#-프로젝트-소개)
- [주요 기능](#-주요-기능)
- [기술 스택](#-기술-스택)
- [시스템 아키텍처](#-시스템-아키텍처)
- [시작하기](#-시작하기)
- [API 문서](#-api-문서)
- [프로젝트 구조](#-프로젝트-구조)
- [추천 알고리즘](#-추천-알고리즘)
- [환경 설정](#-환경-설정)

---

## 🎯 프로젝트 소개

**Recomon**은 Spring AI와 벡터 검색 기술을 활용하여 사용자의 독서 취향을 분석하고 맞춤형 도서를 추천하는 지능형 플랫폼입니다.

### 핵심 가치

- 🤖 **AI 기반 추천**: OpenAI 임베딩 모델을 활용한 의미론적 유사도 분석
- 🎨 **개인화**: 사용자의 선택 패턴과 선호도를 반영한 맞춤형 추천
- 📊 **다양한 추천 기준**: 베스트셀러, 신작, 평점, 리뷰수, 수상작 등 다각적 분석
- 🚀 **실시간 데이터**: 네이버 도서 검색 API를 통한 최신 도서 정보 수집

---

## ✨ 주요 기능

### 1. 지능형 도서 추천
- **벡터 유사도 검색**: 사용자가 선택한 도서의 임베딩 벡터를 기반으로 유사한 도서 탐색
- **의미론적 분석**: 제목, 카테고리, 설명을 종합 분석하여 깊이 있는 추천 제공
- **다중 Intent 지원**: 5가지 추천 기준(베스트셀러, 신작, 리뷰수, 평점, 수상작)을 조합 가능

### 2. 도서 데이터 수집
- **자동화된 수집**: 26개 카테고리별 도서 데이터 자동 수집
- **중복 제거**: ISBN 기반 중복 데이터 필터링
- **이미지 관리**: 도서 표지 이미지 자동 수집 및 업데이트

### 3. 사용자 인터페이스
- **Netflix 스타일 UI**: 직관적이고 현대적인 카드 기반 인터페이스
- **반응형 디자인**: 모바일, 태블릿, 데스크톱 모든 환경 지원
- **실시간 선택**: 즉각적인 피드백과 부드러운 애니메이션

### 4. 관리자 기능
- **데이터 관리**: 카테고리별 도서 수집 및 통계 확인
- **이미지 업데이트**: 누락된 도서 이미지 일괄 업데이트
- **통계 대시보드**: 카테고리별 도서 수 및 전체 통계 조회

---

## 🛠 기술 스택

### Backend
- **Framework**: Spring Boot 3.4.1
- **Language**: Java 17
- **AI/ML**: Spring AI 1.0.0-M6
- **Embedding Model**: OpenAI text-embedding-ada-002

### Database
- **Primary DB**: MySQL (도서 메타데이터)
- **Vector DB**: PostgreSQL + PgVector (임베딩 벡터)
- **ORM**: Spring Data JPA, Hibernate

### External APIs
- **Naver Book Search API**: 도서 정보 수집
- **OpenAI API**: 텍스트 임베딩 생성

### Frontend
- **Template Engine**: Thymeleaf
- **Styling**: Custom CSS (Netflix-inspired)
- **JavaScript**: Vanilla JS

### Infrastructure
- **Cloud**: AWS (Secrets Manager)
- **Build Tool**: Gradle 8.5
- **Version Control**: Git

---

## 🏗 시스템 아키텍처

```
┌─────────────────┐
│   사용자 UI     │
│  (Thymeleaf)    │
└────────┬────────┘
         │
         ▼
┌─────────────────────────────────────┐
│      Spring Boot Application        │
│  ┌───────────────────────────────┐  │
│  │  RecommendationService        │  │
│  │  - Intent 기반 점수 계산     │  │
│  │  - 벡터 유사도 검색          │  │
│  └───────────────────────────────┘  │
│  ┌───────────────────────────────┐  │
│  │  BookCollectorService         │  │
│  │  - 네이버 API 연동           │  │
│  │  - 데이터 수집/저장          │  │
│  └───────────────────────────────┘  │
└─────────┬───────────────┬───────────┘
          │               │
          ▼               ▼
    ┌─────────┐    ┌──────────────┐
    │  MySQL  │    │ PostgreSQL   │
    │ (Books) │    │ + PgVector   │
    └─────────┘    │ (Embeddings) │
                   └──────────────┘
          │
          ▼
    ┌──────────────┐
    │  OpenAI API  │
    │ (Embedding)  │
    └──────────────┘
          │
          ▼
    ┌──────────────┐
    │  Naver API   │
    │ (Book Search)│
    └──────────────┘
```

### 데이터 흐름

1. **도서 수집**:
    - 네이버 API → BookCollectorService → MySQL (메타데이터)
    - 도서 설명/제목 → OpenAI API (임베딩) → PostgreSQL (벡터)

2. **추천 생성**:
    - 사용자 선택 → 쿼리 텍스트 생성 → OpenAI 임베딩
    - 벡터 유사도 검색 → 후보 도서 추출
    - Intent 기반 점수 계산 → 최종 추천 리스트

---

## 🚀 시작하기

### 사전 요구사항

- Java 17 이상
- Gradle 8.5 이상
- MySQL 8.0 이상
- PostgreSQL 14 이상 (PgVector 확장 설치)
- OpenAI API Key
- Naver API Key (Client ID, Secret)

### 1. 저장소 클론

```bash
git clone https://github.com/yourusername/recomon.git
cd recomon
```

### 2. 데이터베이스 설정

#### MySQL
```sql
CREATE DATABASE recomon CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

#### PostgreSQL + PgVector
```sql
CREATE DATABASE recomon_vector;
\c recomon_vector
CREATE EXTENSION vector;
```

### 3. 환경 변수 설정

`src/main/resources/application-dev.yml` 파일 생성:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/recomon
    username: your_username
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver

  ai:
    vectorstore:
      pgvector:
        datasource:
          url: jdbc:postgresql://localhost:5432/recomon_vector
          username: your_username
          password: your_password
        dimensions: 1536
        index-type: HNSW
        distance-type: COSINE_DISTANCE

    openai:
      api-key: your-openai-api-key
      embedding:
        options:
          model: text-embedding-ada-002

naver:
  client-id: your-naver-client-id
  client-secret: your-naver-client-secret
```

### 4. 빌드 및 실행

```bash
# 빌드
./gradlew clean build

# 실행
./gradlew bootRun
```

### 5. 초기 데이터 수집

애플리케이션 시작 시 `AppStartRunner`가 자동으로 26개 카테고리의 도서 데이터를 수집합니다.
수동으로 수집하려면:

```bash
# 특정 카테고리 수집
curl -X POST http://localhost:8080/admin/collect/소설

# 전체 카테고리 수집
curl -X POST http://localhost:8080/admin/collect-all
```

### 6. 애플리케이션 접속

```
http://localhost:8080
```

---

## 📡 API 문서

### 사용자 API

#### 1. 메인 페이지 (추천 기준 선택)
```
GET /
```

#### 2. 도서 선택 페이지
```
GET /books
```

#### 3. 추천 결과 조회
```
POST /recommend-view
Content-Type: application/x-www-form-urlencoded

isbns=9788936434267&isbns=9788937460777&isbns=9788970127248
```

### 관리자 API

#### 1. 도서 통계 조회
```
GET /admin/book-count

Response:
{
  "countByCategory": {
    "소설": 520,
    "에세이": 380,
    ...
  },
  "totalCount": 10400
}
```

#### 2. 카테고리별 도서 수집
```
POST /admin/collect/{category}

Response:
{
  "category": "소설",
  "collectedCount": 20,
  "message": "데이터 수집 완료"
}
```

#### 3. 전체 카테고리 수집
```
POST /admin/collect-all

Response:
{
  "results": {
    "소설": 20,
    "에세이": 20,
    ...
  },
  "totalCollected": 520,
  "message": "모든 카테고리 데이터 수집 완료"
}
```

#### 4. 이미지 URL 업데이트
```
POST /admin/update-images/{category}
POST /admin/update-all-images
```

---

## 📂 프로젝트 구조

```
recomon/
├── src/
│   ├── main/
│   │   ├── java/com/recomon/
│   │   │   ├── config/
│   │   │   │   ├── DataSourceConfig.java          # MySQL 설정
│   │   │   │   └── PgVectorStoreConfig.java       # PostgreSQL + PgVector 설정
│   │   │   ├── controller/
│   │   │   │   ├── AdminController.java           # 관리자 API
│   │   │   │   ├── RecommendationController.java  # 추천 REST API
│   │   │   │   └── ViewController.java            # 화면 컨트롤러
│   │   │   ├── domain/
│   │   │   │   ├── Book.java                      # 도서 엔티티
│   │   │   │   ├── Member.java                    # 회원 엔티티
│   │   │   │   ├── BookInteraction.java           # 상호작용 엔티티
│   │   │   │   └── InteractionType.java           # 상호작용 타입
│   │   │   ├── dto/
│   │   │   │   ├── NaverBookItem.java             # 네이버 도서 아이템
│   │   │   │   └── NaverBookSearchResponse.java   # 네이버 API 응답
│   │   │   ├── recommendation/
│   │   │   │   ├── RecommendationService.java     # 추천 서비스
│   │   │   │   ├── IntentWeightPolicy.java        # Intent 가중치 정책
│   │   │   │   ├── CategoryWeightCalculator.java  # 카테고리 가중치 계산
│   │   │   │   ├── RecommendIntent.java           # 추천 기준 Enum
│   │   │   │   └── dto/
│   │   │   │       └── ScoredBook.java            # 점수화된 도서 DTO
│   │   │   ├── repository/
│   │   │   │   └── BookRepository.java            # 도서 레포지토리
│   │   │   ├── service/
│   │   │   │   └── BookCollectorService.java      # 도서 수집 서비스
│   │   │   ├── AppStartRunner.java                # 초기 데이터 수집 러너
│   │   │   └── RecomonApplication.java            # 메인 애플리케이션
│   │   └── resources/
│   │       ├── templates/
│   │       │   ├── intent-selection.html          # 추천 기준 선택 화면
│   │       │   ├── index.html                     # 도서 선택 화면
│   │       │   └── result.html                    # 추천 결과 화면
│   │       └── application.yml                    # 기본 설정
│   └── test/
│       └── java/com/recomon/
│           ├── config/
│           │   └── TestAIConfig.java              # 테스트용 AI 설정
│           └── RecomonApplicationTests.java       # 통합 테스트
├── build.gradle                                    # Gradle 빌드 설정
└── README.md                                       # 프로젝트 문서
```

---

## 🧠 추천 알고리즘

### 1. 벡터 유사도 기반 추천

```java
// 1. 사용자 선택 도서의 텍스트 임베딩
String userPreferenceText = selectedBooks.stream()
    .map(book -> book.getCategory() + " " + book.getTitle())
    .collect(Collectors.joining(" "));

// 2. OpenAI API를 통한 임베딩 생성
// (Spring AI가 자동으로 처리)

// 3. PgVector를 이용한 코사인 유사도 검색
List<Document> similarDocuments = vectorStore.similaritySearch(
    SearchRequest.builder()
        .query(userPreferenceText)
        .topK(10)
        .build()
);
```

### 2. Intent 기반 점수 계산

사용자가 선택한 추천 기준(Intent)에 따라 각 도서에 가중치를 부여합니다.

| Intent | 가중치 | 계산 방식 |
|--------|--------|----------|
| **BEST_SELLER** | 0.3 | 베스트셀러 여부 (Boolean) |
| **NEW_RELEASE** | 0.3 | 최근 2년 이내 출간 여부 |
| **MANY_REVIEWS** | 0.3 | log(리뷰수 + 1) × 가중치 |
| **HIGH_RATING** | 0.3 | (평점 / 5.0) × 가중치 |
| **AWARD_WINNER** | 0.3 | 수상작 여부 (Boolean) |

```java
// 최종 점수 = Σ(각 Intent별 점수)
double finalScore = 
    (isBestSeller ? 0.3 : 0) +
    (isNewRelease ? 0.3 : 0) +
    (Math.log(reviewCount + 1) * 0.3) +
    ((rating / 5.0) * 0.3) +
    (isAwardWinner ? 0.3 : 0);
```

### 3. 카테고리 가중치 (미래 확장)

사용자가 선택한 도서의 카테고리 분포를 분석하여 선호도를 계산합니다.

| 비율 | 가중치 |
|------|--------|
| 0-10% | 0.0 (관심 없음) |
| 11-19% | 0.1 |
| 20-29% | 0.2 |
| 30-39% | 0.3 |
| 40-49% | 0.4 |
| 50%+ | 0.4 + 추가 가중치 (최대 1.0) |

---

## ⚙️ 환경 설정

### 프로필별 설정

#### Development (application-dev.yml)
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
```

#### Production (application-prod.yml)
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
  
  cloud:
    aws:
      secretsmanager:
        enabled: true
        region: ap-northeast-2
```

### AWS Secrets Manager 연동

프로덕션 환경에서는 민감한 정보를 AWS Secrets Manager에 저장합니다.

```json
{
  "spring.datasource.url": "jdbc:mysql://...",
  "spring.datasource.username": "...",
  "spring.datasource.password": "...",
  "spring.ai.openai.api-key": "...",
  "naver.client-id": "...",
  "naver.client-secret": "..."
}
```

### PgVector 인덱스 최적화

```sql
-- HNSW 인덱스 (권장)
CREATE INDEX vector_store_embedding_idx 
ON vector_store 
USING hnsw (embedding vector_cosine_ops);

-- IVFFlat 인덱스 (대안)
CREATE INDEX vector_store_embedding_idx 
ON vector_store 
USING ivfflat (embedding vector_cosine_ops) 
WITH (lists = 100);
```

---

## 📊 수집 가능한 도서 카테고리

총 **26개** 카테고리에서 도서 데이터를 수집합니다:

| 문학 | 실용 | 학문 | 기타 |
|------|------|------|------|
| 소설 | 요리 | 인문 | 컴퓨터 |
| 시 | 건강 | 정치 | IT |
| 에세이 | 취미 | 사회 | 만화 |
| | 실용 | 역사 | 대학교재 |
| | 스포츠 | 문화 | |
| | 경제 | 종교 | |
| | 경영 | 기술 | |
| | 자기계발 | 공학 | |
| | | 외국어 | |
| | | 과학 | |
| | | 여행 | |

각 카테고리당 **20권**의 도서를 수집하며, 총 **520권**의 도서가 초기 데이터로 구성됩니다.

---

## 🔒 보안 고려사항

1. **API Key 관리**
    - 환경 변수 또는 AWS Secrets Manager 사용
    - `.gitignore`에 설정 파일 등록

2. **데이터베이스 보안**
    - RDS 보안 그룹 설정
    - IAM 역할 기반 접근 제어

3. **Rate Limiting**
    - 네이버 API: 초당 10회 제한
    - OpenAI API: 요금 최적화를 위한 캐싱 고려

---

## 🐛 트러블슈팅

### 1. PgVector 확장 설치 실패
```sql
-- PostgreSQL 버전 확인 (14 이상 필요)
SELECT version();

-- 확장 설치
CREATE EXTENSION IF NOT EXISTS vector;
```

### 2. OpenAI API 비용 관리
- 임베딩 생성은 비용이 발생합니다
- 테스트 시에는 `TestAIConfig`의 `SimpleVectorStore` 사용
- 프로덕션에서는 캐싱 전략 구현 고려

### 3. 네이버 API 제한
- 초당 10회, 일일 25,000회 제한
- `BookCollectorService`에서 0.5초 대기 시간 적용

---

## 📈 향후 개선 계획

- [ ] 회원 시스템 구현 (Member 엔티티 활성화)
- [ ] 사용자 상호작용 추적 (BookInteraction)
- [ ] 개인화된 추천 알고리즘 개선
- [ ] 협업 필터링 알고리즘 추가
- [ ] 도서 리뷰 및 평점 시스템
- [ ] 소셜 기능 (친구 추천, 독서 모임)
- [ ] 모바일 앱 개발
- [ ] 추천 이유 설명 기능 (Explainable AI)

---

## 📄 라이선스

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 📧 문의

프로젝트 관련 문의사항이 있으시면 Issue를 등록해주세요.

---

- [Spring AI](https://docs.spring.io/spring-ai/reference/) - AI 통합 프레임워크
- [PgVector](https://github.com/pgvector/pgvector) - PostgreSQL 벡터 확장
- [Naver Developers](https://developers.naver.com/) - 도서 검색 API
- [OpenAI](https://openai.com/) - 임베딩 모델

---
