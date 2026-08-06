# Java Convention

## Controller 규칙

- REST API 형태로 작성한다.
- 응답은 `ResponseEntity`로 반환한다.
- 응답 포맷은 `07_Response_Rules.md`를 따른다.
- Controller는 요청 검증, Service 호출, 응답 반환만 담당한다.
- Controller에서 Repository를 직접 호출하지 않는다.
- Controller에 비즈니스 로직을 작성하지 않는다.
- 요청 값 검증은 Bean Validation을 사용한다.

## Service 구현 규칙

- Service는 비즈니스 로직을 담당한다.
- Service 인터페이스는 기본적으로 만들지 않는다.
- Service는 같은 도메인의 Repository 인터페이스에 의존한다.
- 다른 도메인의 Entity나 Repository를 직접 참조하지 않는다.
- 다른 도메인 데이터가 필요하면 해당 도메인의 Service를 통해 조회한다.
- 트랜잭션 경계는 Service 계층에서 관리한다.

## Entity 규칙

- JPA를 사용한다.
- Entity에는 `@Entity`, `@Table`을 명시한다.
- PK는 `Long` 타입을 기본으로 사용한다.
- PK 생성 전략은 `GenerationType.IDENTITY`를 기본으로 사용한다.
- 연관관계는 필요한 경우에만 정의한다.
- 불필요한 양방향 연관관계는 만들지 않는다.
- N+1 문제가 발생하지 않도록 조회 방식을 설계한다.
- Entity를 Controller 응답으로 직접 반환하지 않는다.

## Repository 구현 규칙

- Repository는 다음 3단 구조를 사용한다.

```text
{Domain}JpaRepository.java
{Domain}Repository.java
{Domain}RepositoryImpl.java
```

- `{Domain}JpaRepository`는 Spring Data JPA 전용 인터페이스다.
- `{Domain}Repository`는 도메인 Service가 의존하는 Repository 인터페이스다.
- `{Domain}RepositoryImpl`은 `{Domain}Repository`를 구현하고 내부에서 `{Domain}JpaRepository`를 사용한다.
- QueryDSL은 사용하지 않는다.
- 단순 조회는 Spring Data JPA Query Method를 우선 사용한다.
- 복잡한 조회가 필요하면 JPQL 사용을 검토한다.

## DTO 규칙

- DTO는 Java `record` 사용을 기본으로 한다.
- 요청 DTO는 `dto/request`에 둔다.
- 응답 DTO는 `dto/response`에 둔다.
- Entity를 외부에 직접 노출하지 않기 위해 항상 DTO를 사용한다.
- 요청 DTO와 응답 DTO를 하나의 클래스로 공유하지 않는다.
- 공통 응답 DTO 구조는 `07_Response_Rules.md`를 따른다.

## 테스트 규칙

- Service Layer 테스트를 작성한다.
- Controller 테스트를 작성한다.
- Controller 테스트는 MockMvc 사용을 기본으로 한다.
- 외부 API, Elasticsearch, Redis 의존성은 테스트에서 Mock 또는 Testcontainers 사용을 검토한다.
