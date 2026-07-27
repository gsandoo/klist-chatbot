# TODO

이 프로젝트는 Backend와 분리된 Chatbot 서버다.
Backend는 사용자 인증, 대화 세션 소유권, 대화 원본 저장과 Client 통신을 담당한다.
Chatbot 서버는 관광 데이터 검색, 대화 조율, LLM 호출과 근거 기반 답변 생성을 담당한다.

## 완료

- [x] 관광지, 카테고리, 지역, 대화 이력 Entity
- [x] PostgreSQL 연결
- [x] 관광 데이터 Repository 및 Upsert
- [x] TourAPI DTO 및 응답 모델
- [x] Spring RestClient 기반 실제 TourAPI HTTP Client
- [x] timeout, connection, HTTP, JSON, TourAPI 오류 처리
- [x] TourAPI Collector
- [x] TourAPI 관광 데이터 Normalizer
- [x] 단건 Import Orchestration
- [x] `areaBasedList2` 페이지 Ingestion
- [x] 페이지 및 관광지 처리 제한
- [x] Ingestion 실행 결과 Summary
- [x] 개별 관광지 실패 격리
- [x] 개발용 일회성 Ingestion Runner
- [x] 단일 인스턴스 Scheduler 및 중복 실행 방지
- [x] 실제 TourAPI → PostgreSQL 저장 검증
- [x] Flyway V1 초기 마이그레이션
- [x] PK, FK, Unique, Index 정의
- [x] Hibernate `ddl-auto=validate` 정책
- [x] H2 및 Testcontainers PostgreSQL 테스트
- [x] TourAPI 수집 파이프라인 테스트

## 앞으로 구현할 작업

### P0. Backend ↔ Chatbot 내부 API 계약

- [x] 요청 DTO 정의
  - `requestId`
  - `messageId`
  - `sessionId`
  - 사용자 식별자
  - 현재 질문
  - 최근 대화 문맥 또는 요약
  - 위치, 언어, 스트리밍 여부
- [x] 응답 DTO 정의
  - 자연어 답변
  - 추천 관광지 ID와 추천 이유
  - 검색 근거
  - 처리 상태와 안전한 오류 코드
- [ ] Backend와 Chatbot의 대화 저장 및 문맥 관리 책임 확정
- [ ] 서비스 간 인증 방식 확정
- [x] 일반 JSON 응답 계약
- [x] 공통 에러 응답 계약
- [x] timeout, traceId, source 계약
- [x] Backend ↔ Chatbot OpenAPI 문서
- [x] Mock 기반 HTTP 계약 테스트
- [ ] SSE 스트리밍 계약

### P1. Elasticsearch 관광 데이터 색인

- [x] 관광지 Search Document 설계
- [x] TouristSpot Entity → Search Document Mapper
- [x] Search Document 필드 타입 및 Mapper 단위 테스트
- [ ] 관광지 Index Mapping 작성
- [ ] 한국어 검색 Analyzer 정책 결정
- [ ] PostgreSQL → Elasticsearch 전체 재색인
- [ ] 관광지 단건 색인 및 갱신
- [ ] 삭제 또는 비활성 관광지 색인 제거
- [ ] 색인 결과 Summary
- [ ] 색인 실패 기록 및 재처리
- [ ] PostgreSQL 원본 수정 시각과 색인 버전 비교

### P2. Elasticsearch 관광지 검색

- [ ] 키워드 검색
- [ ] 관광지명, 카테고리, 지역, 설명 필드별 가중치
- [ ] 지역 필터
- [ ] 콘텐츠 유형 및 카테고리 필터
- [ ] 위도·경도 기반 거리 검색
- [ ] 검색 결과 개수 및 최소 점수 제한
- [ ] 검색 결과를 Chatbot 근거 DTO로 변환
- [ ] 대표 자연어 질문 기반 검색 품질 테스트
- [ ] 검색 결과 없음 처리

### P3. Chat Orchestrator

- [ ] Chat 요청 검증
- [ ] 질문 분석과 검색 조건 생성
- [ ] TouristSpot Retriever 연결
- [ ] 검색 근거 정리
- [ ] Prompt 생성
- [ ] LLM 호출
- [ ] 응답 검증
- [ ] 추천 관광지 ID가 검색 결과에 포함되는지 검증
- [ ] 처리 시간과 결과 메타데이터 집계

### P4. LLM Client와 Prompt

- [ ] LLM 설정 외부화
- [ ] LLM HTTP Client
- [ ] timeout, connection, HTTP, 모델 오류 구분
- [ ] 구조화 응답 파싱
- [ ] 토큰 사용량 수집
- [ ] 근거 기반 시스템 Prompt
- [ ] 검색 근거에 없는 사실 생성 방지
- [ ] URL, 운영시간, 가격 추측 방지
- [ ] 검색 결과 부족 시 조건 변경 요청
- [ ] LLM 응답 검증 실패 처리

### P5. Backend 전용 Chat API

- [ ] `POST /internal/v1/chat/completions`
- [ ] Backend 전용 서비스 인증
- [ ] 외부 Client 직접 접근 차단
- [ ] 요청 추적 ID 로깅
- [ ] API timeout 정책
- [ ] Chatbot 오류를 Backend 오류 계약으로 변환
- [ ] Backend 연동 통합 테스트

### P6. Redis

- [ ] `requestId` 기반 중복 요청 방지
- [ ] 처리 중 요청 상태
- [ ] 완료 응답 단기 캐시
- [ ] 최근 대화 문맥 캐시 여부 결정
- [ ] 정규화된 관광지 검색 결과 캐시
- [ ] TTL 정책
- [ ] Redis 장애 시 캐시 없이 처리하는 fallback

### P7. SSE 스트리밍

- [ ] Chatbot SSE 이벤트 계약
- [ ] `started`, `content`, `recommendation`, `completed`, `error` 이벤트
- [ ] Backend와 Gateway의 SSE 전달 검증
- [ ] 연결 종료와 취소 처리
- [ ] 중복 스트림 방지
- [ ] 스트리밍 timeout

### P8. 오류, Retry 및 안정성

- [ ] Chatbot 공통 오류 타입
- [ ] Elasticsearch 일시 장애 Retry
- [ ] LLM timeout, connection, 일부 5xx Retry
- [ ] Retry 비대상 오류 구분
- [ ] 지수 Backoff
- [ ] Circuit Breaker 필요성 검토
- [ ] TourAPI 호출 간격 및 Retry 정책
- [ ] 다중 인스턴스 Scheduler 분산 잠금

### P9. 실행 이력과 모니터링

- [ ] Ingestion 실행 이력 PostgreSQL 저장
- [ ] 마지막 성공 수집 시각
- [ ] Elasticsearch 색인 실행 이력
- [ ] 검색 응답 시간
- [ ] LLM 응답 시간
- [ ] 검색 결과 없음 비율
- [ ] LLM 오류율
- [ ] 토큰 사용량
- [ ] Actuator 및 Micrometer
- [ ] 구조화 로그와 요청 추적

### P10. 단계적 운영 검증

- [ ] 실제 TourAPI 50건 수집
- [ ] 실제 TourAPI 100건 수집
- [ ] 실제 TourAPI 500건 수집
- [ ] 콘텐츠 유형 및 지역별 데이터 품질 확인
- [ ] 전체 TourAPI 데이터 수집
- [ ] Elasticsearch 전체 색인
- [ ] Chatbot 품질 평가 질문 세트
- [ ] 근거 정확성 및 환각 평가
- [ ] 부하 및 장애 테스트

## 다음 시작점

1. Backend ↔ Chatbot 요청·응답 계약을 문서로 확정한다.
2. `TouristSpotSearchDocument`와 Elasticsearch Index Mapping을 설계한다.
3. PostgreSQL 관광 데이터를 Elasticsearch에 전체 재색인한다.
4. LLM 없이 관광지 검색 품질부터 검증한다.
