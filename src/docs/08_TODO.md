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
- [x] 관광지 Index Mapping 작성
- [x] Nori 기반 한국어 검색 Analyzer 정책 결정
- [x] PostgreSQL → Elasticsearch 전체 재색인
- [x] 버전 인덱스 생성 및 전체 성공 후 Alias 전환
- [x] 관광지 생성·수정 트랜잭션 커밋 후 단건 색인 및 갱신
- [ ] 삭제 또는 비활성 관광지 색인 제거
- [x] 전체 재색인 결과 Summary
- [x] Elasticsearch 통합 테스트가 모두 Skip되면 빌드 실패
- [x] Testcontainers PostgreSQL·Elasticsearch 실제 통합 테스트
- [x] Cloud 배포용 인덱스 초기화 및 전체 재색인 실행 모드
- [ ] 색인 실패 기록 및 재처리
- [x] PostgreSQL 원본 수정 시각과 색인 버전 비교

### P2. Elasticsearch 관광지 검색

- [x] 키워드 검색
- [x] 관광지명, 주소, 설명, 운영정보 필드별 가중치
- [x] 지역 ID 및 지역 코드 필터
- [x] 콘텐츠 유형 및 카테고리 필터
- [x] 위도·경도 기반 거리 검색
- [x] 검색 결과 개수 및 최소 점수 제한
- [x] 검색 결과를 Chatbot 근거 DTO로 변환
- [x] 대표 자연어 질문 기반 검색 Top-1 품질 테스트
- [x] TourAPI 표본 기반 품질 지표: 14개 질문, Top-1 92.86%, Top-3 100%, 지역·콘텐츠 유형별 집계
- [x] 검색 결과 없음 처리
- [ ] 지역명·카테고리명 데이터 확보 후 자연어 검색 필드와 가중치 추가

### P3. Chat Orchestrator

- [x] Chat 요청 필수값·길이·timeout 경계 검증
- [x] 규칙 기반 질문 분석과 Elasticsearch 검색 조건 생성
- [x] TouristSpot Retriever 연결
- [x] 검색 근거 정리
- [x] Prompt 생성
- [x] LLM 호출
- [x] 응답 검증
- [x] 추천 관광지 ID가 검색 결과에 포함되는지 검증
- [x] 검색·LLM·전체 처리 시간과 결과 메타데이터 집계

### P4. LLM Client와 Prompt

- [x] LLM 설정 외부화
- [x] LLM Client 인터페이스와 요청·결과 모델
- [x] Responses API 기반 LLM HTTP Client
- [x] timeout, connection, HTTP, 모델 오류 구분
- [x] 요청 잔여 timeout을 LLM 네트워크 연결·응답 timeout에 적용
- [x] 구조화 응답 파싱
- [x] LLM 입력·출력 토큰 사용량 수집
- [x] 근거 기반 시스템 Prompt
- [x] 검색 근거에 없는 사실 생성 방지
- [x] URL, 운영시간, 가격 추측 방지
- [ ] 검색 결과 부족 시 조건 변경 요청
- [x] LLM 응답 검증 실패 처리

### P5. Backend 전용 Chat API

- [x] Backend 전용 JSON Chat API를 실제 Completion 처리 흐름에 연결
- [ ] Backend 전용 서비스 인증
- [ ] 외부 Client 직접 접근 차단
- [x] 요청 traceId MDC 로깅
- [x] API timeout 정책
- [x] Chatbot 오류를 Backend 오류 계약으로 변환
- [x] 인증 제외 Backend JSON Chat 연동 통합 테스트

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

- [x] Docker 비의존 Cloud Runtime 프로필과 환경변수 계약
- [ ] Chatbot 공통 오류 타입
- [x] Elasticsearch 일시 장애 Retry와 지수 Backoff
- [x] LLM timeout, connection, 일부 5xx Retry
- [x] Retry 비대상 오류 구분
- [x] LLM 지수 Backoff와 요청 timeout 예산 제한
- [ ] Circuit Breaker 필요성 검토
- [ ] TourAPI 호출 간격 및 Retry 정책
- [ ] 다중 인스턴스 Scheduler 분산 잠금

### P9. 실행 이력과 모니터링

- [ ] Ingestion 실행 이력 PostgreSQL 저장
- [ ] 마지막 성공 수집 시각
- [ ] Elasticsearch 색인 실행 이력
- [x] 검색 응답 시간
- [x] LLM 응답 시간
- [x] 검색 결과 없음 비율
- [x] LLM 오류율
- [x] 검색·LLM·전체 처리 시간 및 토큰 사용량 Micrometer 지표
- [x] Actuator 및 Micrometer 기반 외부 연동 재시도·소진 지표
- [x] Cloud JSON 구조화 로그와 Chat 요청 완료 추적

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

## 현재 우선순위

### 1순위. Elasticsearch 검색 구현 및 품질 검증

기본 검색 구현은 완료됐다. 다음 단계에서는 대표 자연어 질문 세트로 LLM 없이 검색 품질을
검증하고 필드 가중치와 최소 점수를 조정한다. 현재 검색 문서에는 지역명과 카테고리명이 없으므로,
해당 기준정보를 확보한 뒤 자연어 검색 필드에 추가한다.

### 2순위. PostgreSQL과 Elasticsearch 증분 동기화

생성·수정 후 커밋 기반 단건 색인은 완료됐다. 다음으로 삭제 또는 비활성 관광지의 색인을 제거하고,
색인 실패를 영속적으로 기록해 재처리할 수 있게 한다. 이후 PostgreSQL 원본 수정 시각과 색인 버전을
비교하고 전체 재색인을 운영 환경과 유사한 설정으로 검증한다.

### 3순위. Backend ↔ Chatbot 운영 계약 확정

1. 대화 원본 저장과 최근 문맥 관리 책임을 확정한다.
2. 서비스 간 인증 방식을 확정한다.
3. 일반 JSON 요청의 timeout 및 오류 변환 정책을 확정한다.
4. SSE는 일반 JSON 기반 Chat 흐름이 완성된 뒤 별도 단계로 진행한다.

### 4순위. Chat Orchestrator와 LLM 연결

1. 질문을 검색 조건으로 변환하고 TouristSpot Retriever를 연결한다.
2. 검색 결과를 Chatbot 근거 DTO와 Prompt로 변환한다.
3. LLM Client, 구조화 응답 파싱, 오류 처리를 구현한다.
4. 추천 관광지 ID와 답변의 사실이 검색 근거 안에 있는지 검증한다.
5. `POST /internal/v1/chat/completions`를 실제 처리 흐름에 연결한다.

### 5순위. 안정성 및 운영 기능

1. Redis 기반 중복 요청 방지와 완료 응답 캐시를 적용한다.
2. Retry, Backoff, 다중 인스턴스 Scheduler 잠금을 구현한다.
3. 실행 이력, 검색·LLM 지표, 구조화 로그와 요청 추적을 추가한다.
4. SSE 스트리밍과 연결 취소·중복 스트림 방지를 구현한다.
5. 단계별 실데이터 검증, 품질 평가, 부하 및 장애 테스트를 수행한다.
