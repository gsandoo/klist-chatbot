# AI 관광 챗봇 프로젝트

## 프로젝트 소개

본 프로젝트는 사용자가 자연어로 관광 정보를 질문하면 AI가 적절한 관광지를 추천하고,
관련 정보를 제공하는 관광 챗봇 서비스이다.

LLM(OpenAI)은 답변 생성 역할만 담당하며,
관광 데이터 검색은 Elasticsearch를 통해 수행한다.

즉,

Retrieval(검색)
→ Elasticsearch

Generation(생성)
→ OpenAI

구조를 사용하는 검색 기반 AI 챗봇(RAG Architecture)이다.

---

# 프로젝트 목표

사용자는 아래와 같은 질문을 할 수 있다.

- 서울 야경 명소 추천해줘
- 제주도 가족 여행지 알려줘
- 경복궁 입장료 알려줘
- 비 오는 날 실내 관광지 추천해줘
- 부산에서 데이트하기 좋은 곳 알려줘

AI는 자체 지식이 아닌
서비스에서 관리하는 관광 데이터를 기반으로 답변해야 한다.

---

# 프로젝트 아키텍처

User

↓

React

↓

Spring Boot

↓

Elasticsearch
(관광지 검색)

↓

PostgreSQL
(관광 데이터)

↓

OpenAI API
(답변 생성)

↓

Response

---

# 사용 기술

Backend

- Java 17
- Spring Boot 4.x

Database

- PostgreSQL

Search

- Elasticsearch

Cache

- Redis

AI

- OpenAI API

Build

- Gradle

Version Control

- GitLab

IDE

- IntelliJ

---

# 주요 기능

## 관광지 검색

사용자 질문에서

지역

카테고리

태그

를 추출하여 관광지를 검색한다.

예시

서울 + 야경

↓

남산타워
롯데월드타워
반포한강공원

---

## AI 관광 추천

검색된 관광지를 기반으로 자연스러운 답변 생성

예시

Q.
서울 야경 좋은 곳 추천해줘

A.
서울 야경 명소로는 남산타워,
롯데월드타워 전망대,
반포한강공원을 추천드립니다.

---

## 관광지 정보 조회

관광지

운영시간

입장료

주소

대표 이미지

공식 홈페이지

지도 링크

등을 제공한다.

---

# 데이터 출처

한국관광공사 TourAPI

↓

PostgreSQL 저장

↓

Elasticsearch Index 생성

↓

AI 답변 생성

---

# 프로젝트 원칙

AI는 관광지를 생성하지 않는다.

AI는 반드시 검색 결과를 기반으로 답변한다.

검색은 Elasticsearch가 담당한다.

관광 데이터는 PostgreSQL에서 관리한다.

Redis는 성능 최적화를 위해 사용한다.

---

# ERD

주요 테이블

- TOURIST_SPOT
- CATEGORY
- REGION
- TAG
- SPOT_TAG
- CHAT_SESSION
- CHAT_MESSAGE

---

# 향후 확장

- 여행 일정 생성
- 사용자 맞춤 추천
- 위치 기반 추천
- 다국어 지원
- 관광지 리뷰 분석
- Vector Search 기반 Semantic RAG