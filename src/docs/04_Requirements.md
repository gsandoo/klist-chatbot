# 관광 챗봇 요구사항

## 목표
사용자가 자연어로 관광지 관련 질문을 하면 Elasticsearch로 관광지를 검색하고,
검색 결과를 OpenAI API에 전달하여 자연스러운 답변을 생성한다.

## 주요 기능
- 관광지 검색
- 태그 기반 추천
- 지역 기반 추천
- 관광지 운영정보 답변
- 챗봇 대화 이력 저장

## 주요 테이블
- TOURIST_SPOT
- CATEGORY
- REGION
- TAG
- SPOT_TAG
- CHAT_HISTORY

## 기술 스택
- Spring Boot
- PostgreSQL
- Elasticsearch
- Redis
- OpenAI API