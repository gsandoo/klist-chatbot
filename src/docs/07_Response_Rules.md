# Response Rules

## 기본 원칙

- 모든 API 응답은 성공, 페이징, 실패 응답 구조를 명확히 구분한다.
- Controller는 `ResponseEntity`를 반환한다.
- 성공 응답은 `ApiResponse<T>`를 사용한다.
- 페이징 응답은 `PageResponse<T>`를 사용한다.
- 실패 응답은 `ErrorResponse`를 사용한다.
- 응답의 `code`와 `message`는 클라이언트 계약이므로 임의로 변경하지 않는다.

## 성공 응답: ApiResponse<T>

성공 응답은 다음 구조를 사용한다.

```json
{
  "code": "SUCCESS",
  "message": "요청이 성공했습니다.",
  "data": {}
}
```

| 필드 | 타입 | 설명 |
|---|---|---|
| `code` | `String` | 성공 코드 |
| `message` | `String` | 성공 메시지 |
| `data` | `T` | 응답 데이터 |

- 단건 조회, 생성, 수정 응답에 사용한다.
- 반환할 데이터가 없으면 `data`는 `null`로 둘 수 있다.
- 빈 객체를 의미 없이 반환하지 않는다.

## 페이징 응답: PageResponse<T>

페이징 응답은 다음 구조를 사용한다.

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 100,
  "totalPages": 5,
  "first": true,
  "last": false
}
```

| 필드 | 타입 | 설명 |
|---|---|---|
| `content` | `List<T>` | 현재 페이지 데이터 |
| `page` | `int` | 현재 페이지 번호, 0부터 시작 |
| `size` | `int` | 페이지 크기 |
| `totalElements` | `long` | 전체 데이터 수 |
| `totalPages` | `int` | 전체 페이지 수 |
| `first` | `boolean` | 첫 페이지 여부 |
| `last` | `boolean` | 마지막 페이지 여부 |

- `Page<T>` 기반 응답에 사용한다.
- `PageResponse<T>`는 `ApiResponse<PageResponse<T>>`의 `data`로 감싸서 반환한다.

## 실패 응답: ErrorResponse

실패 응답은 다음 구조를 사용한다.

```json
{
  "code": "INVALID_INPUT_VALUE",
  "message": "입력값이 올바르지 않습니다.",
  "errors": []
}
```

| 필드 | 타입 | 설명 |
|---|---|---|
| `code` | `String` | 에러 코드 |
| `message` | `String` | 에러 메시지 |
| `errors` | `List<FieldError>` | 입력값 검증 실패 상세 |

- 예외 응답은 `ErrorResponse`를 사용한다.
- 일반 예외에서는 `errors`를 빈 배열로 반환한다.
- 입력값 검증 실패 시에만 `errors`에 상세 내용을 담는다.

## 입력값 검증 실패 errors 규칙

입력값 검증 실패 시 `errors`는 다음 구조를 사용한다.

```json
{
  "field": "name",
  "value": "",
  "reason": "이름은 필수입니다."
}
```

| 필드 | 타입 | 설명 |
|---|---|---|
| `field` | `String` | 검증 실패 필드명 |
| `value` | `Object` | 거절된 값 |
| `reason` | `String` | 실패 사유 |

- `MethodArgumentNotValidException`은 `errors` 필드를 사용한다.
- 민감 정보는 `value`에 포함하지 않는다.
- 필드명이 없는 검증 실패는 `field`에 요청 객체명 또는 공통 이름을 사용한다.

## ErrorCode 인터페이스

에러 코드는 공통 인터페이스를 기준으로 정의한다.

```text
ErrorCode
├── getCode()
├── getMessage()
└── getStatus()
```

| 메서드 | 반환 타입 | 설명 |
|---|---|---|
| `getCode()` | `String` | 클라이언트가 식별할 에러 코드 |
| `getMessage()` | `String` | 사용자 또는 개발자에게 전달할 메시지 |
| `getStatus()` | `HttpStatus` | HTTP 상태 코드 |

- 도메인별 에러 코드는 `ErrorCode`를 구현한다.
- 공통 에러 코드는 `global/exception`에서 관리한다.
- 도메인 전용 에러 코드는 각 도메인의 `domain/exception`에서 관리한다.

## code/message 변경 규칙

- `code`는 API 계약이므로 한 번 배포한 뒤 의미를 바꾸지 않는다.
- `message`도 클라이언트 표시 또는 테스트에 사용될 수 있으므로 임의 변경하지 않는다.
- 기존 코드 의미가 바뀌면 새 `code`를 추가한다.
- 사용하지 않는 `code`는 즉시 삭제하지 않고 deprecated 상태로 관리한다.
- 내부 로그용 상세 메시지는 클라이언트 응답 `message`와 분리한다.

## HTTP Method별 응답 규칙

| Method | 상황 | 응답 규칙 |
|---|---|---|
| `GET` | 단건 조회 성공 | `ApiResponse<T>` 반환 |
| `GET` | 목록 조회 성공 | `ApiResponse<List<T>>` 또는 `ApiResponse<PageResponse<T>>` 반환 |
| `POST` | 생성 성공 | `201 Created`와 `ApiResponse<T>` 반환 |
| `POST` | 명령 처리 성공 | `200 OK`와 `ApiResponse<T>` 반환 |
| `PATCH` | 부분 수정 성공 | `200 OK`와 `ApiResponse<T>` 반환 |
| `DELETE` | 삭제 성공 | `204 No Content` 또는 `200 OK`와 `ApiResponse<Void>` 반환 |

- 생성 API는 가능하면 생성된 리소스 식별자를 응답에 포함한다.
- 삭제 후 반환 데이터가 없으면 `204 No Content`를 우선 검토한다.
- 클라이언트가 삭제 결과 메시지를 필요로 하면 `ApiResponse<Void>`를 사용할 수 있다.

## Page/Slice 응답 규칙

- 전체 개수가 필요한 화면은 `Page`를 사용한다.
- 무한 스크롤처럼 다음 페이지 여부만 필요하면 `Slice`를 사용한다.
- `Page` 응답은 `totalElements`, `totalPages`를 포함한다.
- `Slice` 응답은 전체 개수를 포함하지 않고 `hasNext`를 포함한다.
- 대용량 조회에서 전체 count 비용이 크면 `Slice`를 우선 검토한다.

## Pageable 요청 파라미터 규칙

페이징 요청 파라미터는 다음 이름을 사용한다.

| 파라미터 | 타입 | 기본값 | 설명 |
|---|---|---|---|
| `page` | `int` | `0` | 페이지 번호, 0부터 시작 |
| `size` | `int` | `20` | 페이지 크기 |
| `sort` | `String` | optional | 정렬 조건 |

- `page`는 0 이상이어야 한다.
- `size`는 1 이상이어야 한다.
- `size`의 최대값을 제한한다.
- 정렬 가능한 필드는 API별로 명시한다.
- 클라이언트 입력 정렬 필드를 Entity 필드명에 직접 연결하지 않는다.
