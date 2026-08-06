# klist-chatbot

관광 데이터를 PostgreSQL에 저장하고 Elasticsearch에서 검색한 결과를 기반으로 답변을 생성하는
관광 챗봇 서버다.

## Cloud Runtime

운영 애플리케이션은 Docker 또는 Testcontainers에 의존하지 않는다. `cloud` 프로필에서 클라우드가
제공하는 PostgreSQL과 Elasticsearch에 직접 연결한다.

필수 환경변수:

```text
SPRING_PROFILES_ACTIVE=cloud

DB_URL=jdbc:postgresql://<host>:5432/<database>
DB_USERNAME=<database-user>
DB_PASSWORD=<database-password>

ELASTICSEARCH_URIS=https://<elasticsearch-endpoint>
ELASTICSEARCH_USERNAME=<elasticsearch-user>
ELASTICSEARCH_PASSWORD=<elasticsearch-password>
```

TourAPI 수집 또는 Scheduler를 사용할 때 추가하는 환경변수:

```text
TOUR_API_SERVICE_KEY=<service-key>
TOUR_API_INGESTION_SCHEDULE_ENABLED=true
TOUR_API_INGESTION_SCHEDULE_CRON=0 0 3 * * *
TOUR_API_INGESTION_SCHEDULE_ZONE=Asia/Seoul
```

Elasticsearch에는 `analysis-nori` 지원이 필요하다. 신규 환경에서는 애플리케이션 트래픽을 받기 전에
버전 인덱스와 Alias를 만들고 PostgreSQL 원본 데이터를 전체 재색인해야 한다.

실행 예시:

```text
java -jar build/libs/klist-chatbot-0.0.1-SNAPSHOT.jar
```

Testcontainers는 `postgresqlTest`, `elasticsearchTest` 같은 통합 테스트에서만 사용한다.
