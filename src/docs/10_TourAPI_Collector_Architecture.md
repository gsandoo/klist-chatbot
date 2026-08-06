# TourAPI Collector Architecture

## 1. 목적과 범위

TourAPI Collector는 여러 외부 응답을 정규화 이전의 한 관광 데이터 묶음으로 조합한다.
초기 Collector 설계 범위에는 실제 HTTP, 인증키, Scheduler, Controller, Spring Batch가 포함되지 않았다.
현재는 이 설계를 기반으로 실제 HTTP Client, 외부 설정, Ingestion Service와 Scheduler까지 구현돼 있다.

표준 흐름은 다음과 같다.

```text
TourApiClient
    ↓
TourApiCollector
    ↓
TourApiCollectResult
    ↓
TourApiTouristSpotNormalizer
    ↓
TouristSpotImportData
    ↓
TouristSpotImporter
```

## 2. 책임 분리

### TourApiClient

- 외부 TourAPI endpoint 호출 계약을 정의한다.
- 성공, 빈 응답, 실패를 `TourApiClientResult<T>`로 반환한다.
- 향후 HTTP 상태, TourAPI header 오류, 역직렬화 오류를 Client 결과로 변환한다.
- Repository, Entity, Normalizer, ImportService를 참조하지 않는다.

### TourApiCollector

- API 호출 순서를 관리한다.
- 필수 및 선택 응답을 조합한다.
- 부분 실패 정책을 적용한다.
- `TourApiCollectResult`를 생성한다.
- Repository, Entity, Normalizer, ImportService를 참조하지 않는다.

### TourApiTouristSpotNormalizer

- Collector가 조합한 외부 DTO를 `TouristSpotImportData`로 변환한다.
- HTML, URL, 날짜, 좌표, 콘텐츠 유형별 필드를 정제한다.
- 변환 실패와 경고를 `TourApiMappingResult`로 반환한다.

### TouristSpotImporter

- 정규화 결과를 PostgreSQL에 생성 또는 갱신하는 도메인 입력 계약이다.
- Collector와 Client의 내부 구조를 알지 못한다.
- 현재 구현체는 `TouristSpotImportService`다.

### TourApiImportOrchestrator

- `collect → normalize → import` 순서만 연결한다.
- 수집 실패 시 Normalizer와 Importer를 호출하지 않는다.
- 수집 성공 또는 부분 성공이면 정규화 결과를 Importer에 전달한다.
- 실제 HTTP 구현이나 배치 실행 정책은 포함하지 않는다.

## 3. Client 결과 계약

`TourApiClientResult<T>` 상태:

| 상태 | 의미 |
|---|---|
| `SUCCESS` | DTO가 정상적으로 존재 |
| `EMPTY` | 호출은 처리됐지만 필요한 item이 없음 |
| `FAILURE` | 외부 오류, 응답 형식 오류 또는 역직렬화 실패 |

Client 구현은 예상 가능한 외부 실패를 이 결과로 변환한다. 프로그래밍 오류나 계약 위반까지
무조건 삼키지는 않는다.

## 4. CollectResult 구조

`TourApiCollectResult`는 정규화 전 외부 데이터이며 다음을 포함한다.

- `contentId`
- `contentTypeId`
- 수집 상태
- `areaBasedListItem`
- `detailCommonItem`
- `detailIntroItem`
- `detailImageItem`
- `regionCodeItem`
- 누락 endpoint 집합
- typed warning 목록

수집 상태:

| 상태 | 의미 |
|---|---|
| `SUCCESS` | 필수·선택 응답이 모두 존재 |
| `PARTIAL` | 필수 응답은 존재하지만 하나 이상의 선택 응답 누락 |
| `FAILED` | 필수 응답 실패 또는 지원하지 않는 콘텐츠 유형 |

## 5. 호출 및 실패 정책

| Endpoint | 필수 여부 | 실패 또는 빈 응답 정책 |
|---|---|---|
| `areaBasedList2` | 필수 | `FAILED`, 이후 상세 호출 중단 |
| `detailCommon2` | 선택 | `PARTIAL`, warning 후 정규화 계속 |
| `detailIntro2` | 선택 | `PARTIAL`, warning 후 정규화 계속 |
| `detailImage2` | 선택 | `PARTIAL`, warning 후 정규화 계속 |
| `areaCode2` 지역명 | 선택 | `PARTIAL`, warning 후 정규화 계속 |

`detailCommon2`가 누락돼도 목록 응답의 제목, 주소, 좌표, 이미지, 원본 시각을 사용할 수 있으므로
수집 전체를 실패시키지 않는다. Normalizer가 최종 필수값을 다시 검증한다.

지원하는 `contentTypeId`는 `12`, `14`, `15`, `25`, `28`, `32`, `38`, `39`다.
그 외 유형은 상세 API를 호출하지 않고 `FAILED`로 반환한다.

## 6. Warning 정책

Warning은 다음 정보를 보존한다.

- warning code
- 문제 endpoint
- Client가 제공한 원인 메시지

현재 warning code:

- `REQUIRED_API_EMPTY`
- `REQUIRED_API_FAILURE`
- `OPTIONAL_API_EMPTY`
- `OPTIONAL_API_FAILURE`
- `UNSUPPORTED_CONTENT_TYPE`

선택 API warning은 정규화와 적재를 막지 않는다. 필수 API warning 또는 지원하지 않는 유형은
Orchestrator가 후속 단계를 호출하지 않게 한다.

## 7. 테스트 전략

- Collector는 Fake `TourApiClient`를 사용한 순수 단위 테스트로 검증한다.
- 모든 API 성공, 선택 API 실패, 필수 API 실패, 빈 응답, 지원하지 않는 유형을 검증한다.
- Collector가 `TouristSpotImporter`에 의존하지 않는 구조를 검증한다.
- Orchestrator는 recording importer를 사용해 수집 실패 시 import가 호출되지 않는지 검증한다.
- Spring Context나 실제 네트워크는 사용하지 않는다.

## 8. 실제 Client 및 Ingestion 연결 현황

`RestTourApiClient`가 `TourApiClient`를 구현하며 다음 기능이 연결돼 있다.

- 공통 base URL과 요청 파라미터 구성
- 서비스 키와 timeout의 외부 설정
- TourAPI header `resultCode` 검증
- 빈 `items`와 단일 item/배열 응답 처리
- 연결, HTTP, 응답 형식 오류의 `TourApiClientResult` 변환
- endpoint별 DTO 반환
- `areaBasedList2` 페이지 순회와 수집 대상 `contentId` 공급

Spring 설정에서 Client, Collector, Orchestrator와 Ingestion Service가 등록된다.
개발용 일회성 Runner와 단일 인스턴스 Scheduler도 구현돼 있다. Controller와 Spring Batch는 현재 범위에
포함되지 않으며, 다중 인스턴스 운영 전에는 Scheduler 분산 잠금이 추가로 필요하다.

## 9. Flyway 연계

Collector와 Client는 DB 스키마를 알지 못한다. Flyway는 PostgreSQL의 Entity 대응 테이블과 제약을
관리하고, 적재는 기존 `TouristSpotImporter` 트랜잭션에서 수행한다.

향후 Flyway 도입 시에도 수집과 정규화 계층은 변경하지 않는다. PostgreSQL 저장이 성공한 후
Elasticsearch 동기화를 추가할 경우 Orchestrator 내부의 외부 호출이 아니라 after-commit 이벤트
또는 outbox 경계를 사용한다.
