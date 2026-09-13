# 한국어·영어 관광 챗봇 구현 및 검증

텍스트 요청과 음성 multipart의 `request` JSON에 `language: "ko" | "en"`을 전달한다. 생략/null은 `ko`, 다른 값은 HTTP 400이다. 응답 JSON 구조는 기존과 같다. 음성 응답은 STT 결과와 텍스트 답변이며, TTS 기능은 포함하지 않는다.

```json
{
  "sessionId": "english-session",
  "message": "What are the opening hours of Gyeongbokgung Palace?",
  "language": "en"
}
```

음성은 `/internal/chat/query/audio`에 `audio` 파일과 JSON `request` 파트를 전송한다. 기존 계약대로 음성 요청에는 `requestId`, `sessionId`, `userId`를 포함한다. `language=en`은 STT provider의 multipart `language=en`, 검색 조건, LLM 응답 언어까지 전달된다.

## 저장과 색인

- PostgreSQL `tourist_spot.language VARCHAR(2) NOT NULL DEFAULT 'ko'`를 V3 Flyway migration으로 추가한다. 기존 행은 `ko`가 된다.
- 유일성은 `(language, tour_api_content_id)`로 변경한다. 자체 `tourist_spot_id`는 언어별 행마다 고유하므로 Elasticsearch 문서 ID 충돌이 없다.
- 영문 관광 유형은 API 원본 코드 `75/76/78/79/80/82/85`를 유지한다. 영문 수집기와 정규화기가 이를 처리하고, 영문 소개 응답의 운영시간·요금 등 기존 대응 필드를 사용한다.
- Elasticsearch의 동일 alias `tourist-spots`에 두 언어를 저장하고 모든 검색에 `language` keyword term filter를 적용한다.
- `title`, `address`, `description`, `openingHours`, `admissionFee`에 `.en` multi-field와 Elasticsearch `english` analyzer를 추가한다. 한국어 검색은 기존 Nori 필드를 사용한다.
- 캐시 키에 언어가 포함된다. 같은 `requestId`를 다른 언어로 재사용하면 기존 답변을 반환하지 않고 요청 충돌로 처리한다.
- 영문 지역 코드가 빈 경우를 고려해 영어 질문의 지역명은 검색어에 유지한다. 영문 질문 분석은 지역명 번역에 의존하지 않는 규칙 기반 처리다.

## 적재와 배포

챗봇은 색인된 데이터를 검색하는 기존 구조를 유지한다. 채팅 요청마다 TourAPI를 호출하지 않는다. `language=en` 질의가 답변하려면 EngService2 데이터를 미리 적재·색인해야 한다.

API 키는 환경변수로만 제공한다. 키 값을 코드, 설정 기본값, 문서에 저장하지 않는다.

```text
TOUR_API_SERVICE_KEY=<발급받은 인증키>
OPENAI_API_KEY=<LLM API 키>
STT_OPENAI_API_KEY=<STT API 키>
```

수집 프로세스별 `TOUR_API_LANGUAGE`를 지정한다. `ko`가 기본값이며 KorService2, `en`이면 EngService2를 사용한다. 요청 간 공유 설정을 변경하지 않는다. 동일 DB에 두 언어의 수집 작업을 각각 실행할 수 있다.

```powershell
# 환경변수에 인증키와 DB/Elasticsearch 연결 설정을 먼저 제공한다.
$env:TOUR_API_LANGUAGE = 'en'
$env:TOUR_API_INGESTION_ENABLED = 'true'
$env:TOUR_API_INGESTION_MAX_ITEMS = '50'  # 검증용 제한; 전체 적재는 0
.\gradlew.bat bootRun --args='--spring.profiles.active=dev'
```

필요하면 `TOUR_API_ENGLISH_BASE_URL`로 영문 endpoint를 지정할 수 있으며 기본값은 `https://apis.data.go.kr/B551011/EngService2`이다. 국문 endpoint 설정은 기존 `TOUR_API_BASE_URL`이다. `TOUR_API_LANGUAGE`는 수집 언어이며 채팅 요청의 언어를 제한하지 않는다.

기존 Elasticsearch 인덱스에는 `language`와 `.en` 필드가 없으므로 **서비스 전환 전에 재색인이 필요하다**. 기존 인덱스 초기화만으로는 충분하지 않다.

1. V3 migration 적용 후 두 언어 데이터를 적재한다.
2. `TOURIST_SPOT_INDEX_VERSION`에 아직 존재하지 않는 버전(예: `v3`)을 지정하고, 기존 `TouristSpotFullReindexService.reindexAll()` 또는 `TOURIST_SPOT_INDEX_BOOTSTRAP_MODE=reindex`로 DB 전체를 새 인덱스에 색인한다. 대상 인덱스가 이미 있으면 재색인이 실패하므로 새 버전이 필요하다.
3. 재색인 성공 및 alias 전환을 확인한 후 채팅 트래픽을 새 버전에 연결한다. 재색인은 실패 행이 있으면 alias를 전환하지 않는다.
4. 일반 실행에서는 ingestion 활성화와 bootstrap reindex 설정을 다시 끈다. 이후 변경된 데이터는 기존 증분 색인 경로를 따른다.

## 실제 contentId 확인

2026-09-10, `detailCommon2` 실조회 결과:

| 서비스 | 요청 contentId | 결과 |
|---|---:|---|
| KorService2 | 126508 | 경복궁, contentTypeId=12 |
| EngService2 | 264337 | Gyeongbokgung Palace (경복궁), contentTypeId=76 |
| EngService2 | 126508 | 정상 응답, 빈 items |
| KorService2 | 264337 | 정상 응답, 빈 items |

두 레코드의 좌표는 `126.97672186606306, 37.576030700049394`로 같고 우편번호도 `03045`로 같았다. 응답에 직접적인 반대 언어 contentId 필드는 없었다. 이 표본에서는 ID 직접 결합이 불가능하며, 좌표·주소·명칭 등을 함께 검증한 별도 연결 테이블이 필요하다. 좌표만으로 자동 병합하지 않는다.

영문 `detailIntro2(contentId=264337, contentTypeId=76)`의 `usetime`에 영어 운영시간이 제공되는 것도 확인했다. 공통정보 `detailCommon2`에는 `contentTypeId`를 전달하지 않는다.

## 테스트

```powershell
.\gradlew.bat test postgresqlTest elasticsearchTest --no-daemon
# 실제 EngService2 → 임시 PostgreSQL 적재/중복 검증 (TOUR_API_SERVICE_KEY 필요)
.\gradlew.bat liveTourApiTest --tests '*EnglishTourApiLivePostgreSqlIntegrationTest' --no-daemon
```

`EnglishChatIntegrationTest`는 HTTP 텍스트·음성 요청부터 실제 분석·프롬프트·파싱·근거 검증까지 연결한다. 외부 STT, LLM, 검색 gateway는 테스트 대역을 사용하며 실제 녹음 인식률 또는 실제 LLM의 언어 준수율을 평가하는 테스트는 아니다. 별도 STT HTTP 계약 테스트는 provider multipart에 `en`이 들어가는지 확인한다. PostgreSQL/Elasticsearch 테스트는 Testcontainers로 실제 엔진에서 언어별 저장·검색을 검증한다.

## 최종 검증 결과 (2026-09-10)

| 검증 | 통과 | 실패 | 비고 |
|---|---:|---:|---|
| `test` | 332 | 0 | 영어 텍스트·음성 흐름, 언어 검증, 영어 질문 분석, STT HTTP 계약, 캐시 분리, 기존 한국어 회귀 포함 |
| `postgresqlTest` | 8 | 0 | 같은 contentId의 국문·영문 독립 저장 포함. 실 API opt-in 테스트 2개는 이 단계에서 제외 |
| `elasticsearchTest` | 7 | 0 | 언어 필터 실제 검색, 국문 검색 품질 및 전체 재색인 포함 |
| 영문 `liveTourApiTest` | 1 | 0 | 실제 EngService2 3건 → 임시 PostgreSQL 저장, 영어 설명 확인, 재수집 시 신규/중복 0건 |

전체 348개 테스트가 실행되어 통과했다. `git diff --check`도 통과했다. 실제 STT/LLM 유료 API 호출과 실제 영어 녹음 인식률 평가는 수행하지 않았다. 음성·답변 흐름은 테스트 대역과 provider HTTP 계약 검증으로 확인했다.

기존 Elasticsearch 회귀 테스트는 `v1` 인덱스를 가정하면서 애플리케이션 기본 설정 `v2`를 사용하고 있어, 테스트에서 `v1`을 명시하도록 수정했다. 배포 인덱스 버전은 새 환경변수 `TOURIST_SPOT_INDEX_VERSION`으로 지정할 수 있다.

## 변경 파일

```text
.gitignore
README.md
src/docs/18_English_Support.md
src/main/java/com/klist/chatbot/chat/application/analysis/ChatQuestionAnalyzer.java
src/main/java/com/klist/chatbot/chat/application/ChatCompletionOrchestrator.java
src/main/java/com/klist/chatbot/chat/application/ChatFallbackResponses.java
src/main/java/com/klist/chatbot/chat/application/ChatQuestionDispositionClassifier.java
src/main/java/com/klist/chatbot/chat/application/ChatSearchOrchestrator.java
src/main/java/com/klist/chatbot/chat/application/InternalChatQueryService.java
src/main/java/com/klist/chatbot/chat/application/prompt/ChatPromptFactory.java
src/main/java/com/klist/chatbot/chat/presentation/dto/InternalAudioChatQueryRequest.java
src/main/java/com/klist/chatbot/chat/presentation/dto/InternalChatQueryRequest.java
src/main/java/com/klist/chatbot/chat/presentation/InternalAudioChatQueryController.java
src/main/java/com/klist/chatbot/domain/touristspot/domain/entity/TouristSpot.java
src/main/java/com/klist/chatbot/domain/touristspot/repository/TouristSpotJpaRepository.java
src/main/java/com/klist/chatbot/domain/touristspot/repository/TouristSpotRepository.java
src/main/java/com/klist/chatbot/domain/touristspot/repository/TouristSpotRepositoryImpl.java
src/main/java/com/klist/chatbot/domain/touristspot/service/TouristSpotImportService.java
src/main/java/com/klist/chatbot/infrastructure/chat/idempotency/IdempotentInternalChatQueryService.java
src/main/java/com/klist/chatbot/infrastructure/search/document/TouristSpotSearchDocument.java
src/main/java/com/klist/chatbot/infrastructure/search/mapper/TouristSpotSearchDocumentMapper.java
src/main/java/com/klist/chatbot/infrastructure/search/query/ElasticsearchTouristSpotSearchGateway.java
src/main/java/com/klist/chatbot/infrastructure/speech/openai/OpenAiSpeechToTextClient.java
src/main/java/com/klist/chatbot/infrastructure/tourapi/client/TourApiProperties.java
src/main/java/com/klist/chatbot/infrastructure/tourapi/collector/TourApiCollector.java
src/main/java/com/klist/chatbot/infrastructure/tourapi/ingestion/TourApiIngestionConfiguration.java
src/main/java/com/klist/chatbot/infrastructure/tourapi/mapper/TourApiIntroFieldPolicy.java
src/main/java/com/klist/chatbot/infrastructure/tourapi/mapper/TourApiTouristSpotNormalizer.java
src/main/java/com/klist/chatbot/infrastructure/tourapi/mapper/TouristSpotImportData.java
src/main/java/com/klist/chatbot/search/application/TouristSpotSearchCriteria.java
src/main/java/com/klist/chatbot/speech/application/SpeechToTextClient.java
src/main/java/com/klist/chatbot/speech/application/SpeechTranscriptionService.java
src/main/resources/application.yml
src/main/resources/db/migration/V3__tourist_spot_language.sql
src/main/resources/elasticsearch/tourist-spots-mappings.json
src/main/resources/openapi/internal-chat-api.yaml
src/test/java/com/klist/chatbot/chat/application/analysis/EnglishQuestionAnalyzerTest.java
src/test/java/com/klist/chatbot/chat/presentation/EnglishChatIntegrationTest.java
src/test/java/com/klist/chatbot/infrastructure/chat/idempotency/IdempotentInternalChatQueryServiceTest.java
src/test/java/com/klist/chatbot/infrastructure/search/query/CachingTouristSpotSearchGatewayTest.java
src/test/java/com/klist/chatbot/infrastructure/search/query/ElasticsearchTouristSpotSearchGatewayTest.java
src/test/java/com/klist/chatbot/infrastructure/speech/openai/OpenAiSpeechToTextClientTest.java
src/test/java/com/klist/chatbot/search/TouristSpotIndexingIntegrationTest.java
src/test/java/com/klist/chatbot/tourapi/EnglishTourApiIngestionTest.java
src/test/java/com/klist/chatbot/tourapi/EnglishTourApiLivePostgreSqlIntegrationTest.java
src/test/java/com/klist/chatbot/tourism/TourismRepositoryTest.java
src/test/java/com/klist/chatbot/tourism/TouristSpotImportServiceTest.java
```

로컬 전용으로 Git에서 제외된 application-dev.yml의 TourAPI/LLM/STT API 키 기본값도 제거했다. 이 파일의 키는 환경변수로 공급한다.
