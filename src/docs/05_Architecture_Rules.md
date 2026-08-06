# Architecture Rules

## 기본 원칙

- 프로젝트는 계층형 패키지 구조가 아니라 도메인 중심(Domain-Driven) 패키지 구조를 사용한다.
- 각 도메인은 자신의 비즈니스 규칙, Entity, DTO, Repository를 내부에 가진다.
- 도메인 간 내부 구현을 직접 참조하지 않고 Service를 통해 협력한다.
- `global` 패키지는 공통 기능만 담당하며, 어떤 도메인도 알아서는 안 된다.

## 패키지 구조

```text
com.example.app
├── global
│   ├── config
│   ├── exception
│   ├── response
│   ├── security
│   └── util
└── domain
    └── {domain-name}
        ├── controller
        ├── service
        ├── domain
        │   ├── entity
        │   └── exception
        ├── dto
        │   ├── request
        │   └── response
        └── repository
            ├── {Domain}JpaRepository.java
            ├── {Domain}Repository.java
            └── {Domain}RepositoryImpl.java
```

## Global 규칙

- `global`은 전역 설정, 공통 예외, 공통 응답, 보안, 유틸리티만 포함한다.
- `global`에서 특정 도메인의 Entity, Repository, Service, DTO를 참조하지 않는다.
- 전역 예외 처리, 공통 응답 포맷, 인증/인가 설정은 `global`에 둔다.
- 도메인별 예외는 각 도메인의 `domain/exception`에 둔다.

## 도메인 간 의존성 규칙

- 다른 도메인의 `entity`, `repository`를 직접 import하지 않는다.
- 다른 도메인의 데이터가 필요하면 해당 도메인의 `Service`를 통해 접근한다.
- 도메인 간 응답 데이터는 Entity가 아니라 DTO 또는 명확한 조회 결과 모델로 전달한다.
- 순환 의존성이 생기면 별도 조합 도메인 또는 애플리케이션 서비스를 둔다.

## Repository 접근 규칙

- Repository는 같은 도메인의 Service에서만 접근한다.
- Controller에서 Repository를 직접 호출하지 않는다.
- 다른 도메인의 Repository를 직접 호출하지 않는다.
- Service는 `{Domain}JpaRepository`가 아니라 `{Domain}Repository`에 의존한다.

## Service 접근 규칙

- Service는 도메인의 비즈니스 로직을 담당한다.
- 다른 도메인의 기능이 필요하면 해당 도메인의 Service를 호출한다.
- 외부 API, 검색 엔진, 캐시 연동은 도메인 Service에 직접 섞지 않고 별도 컴포넌트로 분리한다.
- 여러 도메인을 조합하는 흐름은 필요 시 별도 도메인 또는 조합 Service에서 처리한다.
