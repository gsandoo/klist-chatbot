# 시스템 아키텍처

## 목적

본 문서는 관광 챗봇 시스템의 전체 구조와
각 컴포넌트의 역할 및 데이터 흐름을 정의한다.

---

# 전체 시스템 구성

```
                User
                  │
                  ▼
         React Frontend
                  │
                  ▼
        Spring Boot Backend
        ├──────────────────┐
        │                  │
        ▼                  ▼
 Elasticsearch         PostgreSQL
 (관광지 검색)        (관광 데이터)
        │                  │
        └────────┬─────────┘
                 ▼
          OpenAI API
         (답변 생성)
                 │
                 ▼
             Response
```

---

# 컴포넌트 역할

## React

사용자가 질문을 입력하고
챗봇의 응답을 화면에 출력한다.

---

## Spring Boot

전체 비즈니스 로직을 담당한다.

주요 역할

- 질문 분석
- 검색 요청
- OpenAI 호출
- 응답 생성
- 대화 저장

---

## PostgreSQL

서비스의 Master Data 저장

관리 데이터

- 관광지
- 지역
- 태그
- 카테고리
- 채팅

---

## Elasticsearch

검색 전용 엔진

검색 대상

- 관광지명
- 설명
- 태그
- 지역

검색 결과는 관광지 ID 목록이다.

---

## OpenAI API

검색된 관광지 정보를 기반으로
자연스러운 답변을 생성한다.

OpenAI는

관광지 검색을 수행하지 않는다.

---

## Redis

캐시 서버

캐시 대상

- 인기 관광지

- 인기 질문

- 관광지 상세정보

---

# 데이터 흐름

## STEP 1

사용자가 질문한다.

예시

서울 야경 좋은 곳 추천해줘

↓

## STEP 2

Spring Boot가 질문을 수신한다.

↓

## STEP 3

Elasticsearch 검색

검색 키워드

- 서울

- 야경

↓

검색 결과

- 관광지 ID

↓

## STEP 4

PostgreSQL 조회

검색된 관광지의

상세 정보를 조회한다.

↓

## STEP 5

OpenAI 호출

사용자 질문

+

관광지 정보

↓

자연어 답변 생성

↓

## STEP 6

Frontend 응답

↓

## STEP 7

CHAT_MESSAGE 저장

---

# AI 처리 구조

AI는

관광지를 직접 검색하지 않는다.

AI는

검색 결과를 기반으로

답변만 생성한다.

```
사용자 질문

↓

Elasticsearch

↓

관광지 검색

↓

PostgreSQL

↓

관광지 상세정보

↓

OpenAI

↓

답변 생성
```

---

# 설계 원칙

## Search와 Generation 분리

Search

↓

Elasticsearch

Generation

↓

OpenAI

---

## Single Source of Truth

관광 데이터는

항상 PostgreSQL에서 관리한다.

---

## 확장 가능한 구조

향후

- Vector Search

- 사용자 맞춤 추천

- 여행 일정 생성

- 다국어 지원

등을 추가할 수 있도록 설계한다.

---

# 향후 확장

현재

Elasticsearch 기반 검색

↓

향후

Elasticsearch + Embedding

↓

Vector Search

↓

Semantic RAG

구조로 확장 가능하다.