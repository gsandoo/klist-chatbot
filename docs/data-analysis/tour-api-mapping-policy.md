# TourAPI Mapping Policy

Created: 2026-07-19

## 1. Used TourAPI Responses

- `areaBasedList2`: base identity, title, category codes, region codes, address, coordinates, image, source timestamps.
- `detailCommon2`: preferred common detail fields such as overview, homepage, address, coordinates, telephone, image, source timestamps.
- `detailIntro2`: content-type-specific opening hours, fee, event homepage, and reservation-like fields.
- `detailImage2`: fallback image URLs when common/list image fields are missing.
- `areaCode2`, `ldongCode2`: code/name DTO support only. No DB lookup or Region creation is performed in this step.

## 2. API Field Ownership

- Required identity: `contentid`, `contenttypeid`, `title`.
- Required sync timestamp for Entity creation: `modifiedtime`.
- Category source codes: `cat1`, `cat2`, `cat3`.
- Region source codes: `areacode`, `sigungucode`, `lDongRegnCd`, `lDongSignguCd`.
- Coordinates: `mapy` to latitude, `mapx` to longitude.
- Images: `firstimage` to `imageUrl`, `firstimage2` to `thumbnailImageUrl`; fallback to `detailImage2.originimgurl` and `smallimageurl`.

## 3. Opening Hours Mapping

| contentTypeId | Type | Source fields |
|---:|---|---|
| 12 | Tourist attraction | `usetime` |
| 14 | Cultural facility | `usetimeculture` |
| 15 | Event/festival | `eventstartdate`, `eventenddate`, `playtime` |
| 25 | Travel course | `schedule`, `taketime` |
| 28 | Leports | `openperiod`, `usetimeleports` |
| 32 | Lodging | `checkintime`, `checkouttime` |
| 38 | Shopping | `opentime` |
| 39 | Restaurant | `opentimefood` |

Multiple fields are joined as display text without semantic rewriting.

## 4. Admission Fee Mapping

| contentTypeId | Source fields |
|---:|---|
| 12 | None |
| 14 | `usefee`, `parkingfee` |
| 15 | `usetimefestival` |
| 25 | None |
| 28 | `usefeeleports`, `parkingfeeleports` |
| 32 | None |
| 38 | `saleitemcost` |
| 39 | None |

Fee values are stored as source text, not numeric values.

## 5. Reservation Handling

`reservationUrl` stores only clear `http` or `https` URLs. Guide text, phone instructions,
`javascript:` values, and invalid URLs are ignored. Lodging uses `reservationurl`. Other reservation-like
fields such as `bookingplace`, `reservation`, `reservationlodging`, and `reservationfood` are not forced
into the URL column.

## 6. HTML Cleanup

Descriptions are HTML-unescaped, parsed through Java's HTML parser, stripped of tags, and whitespace-normalized.
Homepage extraction prefers an anchor `href`; plain homepage values are accepted only when they are valid
`http` or `https` URLs. URL reachability is not checked over the network.

## 7. Date Conversion

TourAPI timestamps use `yyyyMMddHHmmss` and map to `LocalDateTime`.

- Blank `createdtime`: normalized to `null`.
- Invalid `createdtime`: issue is recorded, mapping can continue.
- Blank or invalid `modifiedtime`: mapping fails before Entity creation because `TouristSpot.sourceModifiedAt` is required.

## 8. Coordinate Conversion

Coordinates are parsed from strings to `BigDecimal`.

- `mapy` latitude must be between `-90` and `90`.
- `mapx` longitude must be between `-180` and `180`.
- Blank coordinates become `null`.
- Invalid coordinates record issues and become `null`; a single bad coordinate does not fail the whole item.
- `0` is preserved as a valid coordinate value.

## 9. Missing Required Values

Mapping fails when any of these are missing or invalid:

- `contentid`
- `contenttypeid`
- `title`
- `modifiedtime`
- unsupported `contentTypeId`

Failures are represented by `TourApiMappingResult` with `value == null` and explicit issues.

## 10. Entity Mapping Flow

`TourAPI DTO -> TourApiTouristSpotNormalizer -> TouristSpotImportData -> TouristSpotImportMapper -> TouristSpot`

DTOs do not create Entities. The Entity mapper does not call repositories, services, transactions, DB lookup,
external APIs, or Elasticsearch.

## 11. Currently Unsupported Data

- Real TourAPI client calls and key handling.
- Category and Region DB lookup/creation.
- Repository, Service, Controller, scheduler, or batch ingestion.
- Elasticsearch document/indexing.
- `Tag`, `SpotTag`, `TouristSpotImage`.
- Repeated `detailInfo2` rows and full image galleries.
- Reservation guide text storage separate from URL.

## 12. Loader Service Notes

Future ingestion should keep per-item failure records so one invalid content item does not stop the whole batch.
Category and Region IDs should be resolved in the loader/service layer using source codes prepared in
`TouristSpotImportData`. DB save/upsert and retry policy should be added only after repository and service
boundaries are defined.

## 13. Final Identity Keys

### Category

Current Entity constraint: `uk_category_content_type_small_code(content_type_id, small_category_code)`.

Final lookup key for the next loader step:

- `contentTypeId + smallCategoryCode`

`largeCategoryCode(cat1)` and `middleCategoryCode(cat2)` are retained for validation and update data, but they
are not the primary lookup key. This matches the current Entity and DB schema and avoids assuming that `cat3`
alone is globally stable across every content type. New classification fields `lclsSystm1/2/3` are preserved
in `TouristSpotImportData` for future migration or reconciliation, but the current `Category` Entity still uses
legacy `cat1/2/3`.

### Region

Current Entity stores a derived `regionCode` and has `uk_region_code(region_code)`.

Final lookup key for the next loader step:

- `areaCode + sigunguCode`
- Derived `regionCode`: `areaCode` when `sigunguCode` is null, otherwise `areaCode:sigunguCode`

Both codes stay as `String` so leading zeros are preserved. Blank `sigunguCode` is normalized to null. If
wide-area rows and city/county/district rows are stored together, keep `region_code` as the unique DB key rather
than a PostgreSQL unique constraint directly on `(area_code, sigungu_code)`, because PostgreSQL treats nulls as
distinct in normal unique constraints.

### TouristSpot

Current Entity constraint: `uk_tourist_spot_tour_api_content_id(tour_api_content_id)`.

Final lookup key:

- `tourApiContentId`

The normalized and Entity type is `Long`. TourAPI `contentid` is treated as numeric and no leading-zero
requirement was observed in the sample analysis. `contentTypeId` is not part of the identity key; if it changes
for the same `tourApiContentId`, the loader should treat it as a normal source update when `sourceModifiedAt`
is newer and should log the type change.

## 14. sourceModifiedAt Comparison Policy

Recommended loader policy:

| Scenario | Policy |
|---|---|
| New record, incoming `sourceModifiedAt` exists | Save when required fields are valid. |
| New record, incoming `sourceModifiedAt` is null | Current Entity cannot persist it; skip and record a required timestamp failure. |
| Incoming timestamp is newer than existing | Update. |
| Incoming timestamp equals existing | Do not update. Refreshing `lastSyncedAt` can be considered separately. |
| Incoming timestamp is older than existing | Do not update. |
| Existing timestamp is null, incoming exists | Update. This should be rare because current Entity is not nullable. |
| Existing timestamp exists, incoming null | Do not overwrite. Record a conservative skip. |
| Both timestamps are null | Do not auto-update by field comparison. Record as unresolved data quality. |

The current transformation layer enforces non-null `sourceModifiedAt` before Entity creation because the current
`TouristSpot.sourceModifiedAt` column is `nullable = false`.

## 15. Null Update Policy

Default loader behavior should preserve existing values when incoming optional values are null. TourAPI blanks are
normalized to null and do not mean "delete this value".

Keep existing value when incoming value is null:

- `description`
- `address`
- `detailAddress`
- `zipCode`
- `latitude`
- `longitude`
- `tel`
- `openingHours`
- `admissionFee`
- `officialUrl`
- `reservationUrl`
- `imageUrl`
- `thumbnailImageUrl`
- `categoryId`
- `regionId`

Allow null updates only after a future source explicitly represents deletion or removal. No current TourAPI field
in this mapping layer provides that signal. Invalid coordinates are also normalized to null with issues, so the
loader should keep existing coordinates.

## 16. Required Values and Skip Policy

Transformation failure that should skip persistence:

- missing or invalid `contentid`
- missing or invalid `contenttypeid`
- unsupported `contentTypeId`
- missing `title`
- missing or invalid required `modifiedtime`

Transformation success with warnings:

- invalid optional `createdtime`
- invalid or out-of-range coordinate
- invalid homepage or reservation URL
- optional field missing

Loader-stage warnings or skips:

- `Category` not found for `contentTypeId + smallCategoryCode`: save can proceed with `categoryId = null`, but
  keep source category codes and record a warning.
- `Region` not found for `areaCode + sigunguCode`: save can proceed with `regionId = null`, but keep source
  region codes and record a warning.

## 17. Mapping Result Contract

`TourApiMappingResult<T>` is the result object used by the transformation layer.

- `isSuccess() == true`: normalized or Entity value exists. Issues may still contain non-fatal warnings.
- `isSuccess() == false`: value is null and issues describe why the item must be skipped.
- `TourApiMappingIssueCode` distinguishes required-field failures, unsupported content type, invalid coordinates,
  and invalid dates.

This keeps failure reasons available to the future loader instead of losing them through `Optional.empty()`.

## 18. Repository and Loader Input Contract

The future loader should consume `TouristSpotImportData` and mapping issues, not raw TourAPI DTOs.

Expected lookup inputs:

- Category lookup: `contentTypeId`, `smallCategoryCode`; validate `largeCategoryCode` and `middleCategoryCode`
  when present.
- Region lookup: `areaCode`, `sigunguCode`; use `regionName` only when it came from a code API response.
- TouristSpot lookup: `tourApiContentId`.
- Update decision: `sourceModifiedAt`.

The loader should not treat null optional fields as delete requests, should not delete missing remote rows
immediately, and should not call the external TourAPI from inside repository code.

## 19. Entity and DB Schema Fit

Current Entity and `src/main/resources/db/dev/tour-seed.sql` are aligned for the columns used here:

- `TouristSpot.tourApiContentId`: `Long` / `BIGINT`, unique.
- `TouristSpot.contentTypeId`: `Integer` / `INTEGER`.
- Coordinates: `BigDecimal` / `NUMERIC(13, 10)`.
- Source timestamps: `LocalDateTime` / `TIMESTAMP`.
- URLs: `VARCHAR(2048)`.
- Long text fields: `TEXT`.
- `Region.region_code` is present in both Entity and SQL and is derived from area/sigungu.

No Entity change is required for the current loader preparation step. A future migration is only needed if the
project decides to make `Category` source-system aware, store `lclsSystm*` in the `Category` table, or add explicit
soft-delete/status fields for content that disappears from TourAPI lists.

## 20. Persistence Resolver Policy

`TouristSpotImportService` does not directly reference another domain's Entity or Repository.

- `CategoryPersistenceResolver` owns Category lookup and conditional creation.
- `RegionPersistenceResolver` owns Region lookup and conditional creation.
- Both resolvers return an ID-based resolution result so the tourist spot domain does not expose or depend on
  another domain's Entity.
- Category is created only when `contentTypeId` and the complete legacy category hierarchy are available. A name
  is never invented.
- Region is created only when `areaCode` and a source-provided `regionName` are available. A name is never derived
  from a code.
- An unresolved Category or Region produces a warning. Because both FK ID columns are nullable, it does not by
  itself skip a valid TouristSpot.

## 21. Import Result Status

| Status | Meaning |
|---|---|
| `CREATED` | A new TouristSpot was persisted. |
| `UPDATED` | A newer source record updated the existing TouristSpot. |
| `SKIPPED_NOT_MODIFIED` | Source timestamps are equal. |
| `SKIPPED_OLDER_SOURCE` | The incoming source record is older. |
| `SKIPPED_MISSING_REQUIRED_FIELD` | A required identity, type, or name is missing. |
| `SKIPPED_MISSING_SOURCE_TIMESTAMP` | The required source modification timestamp is missing. |
| `SKIPPED_UNSUPPORTED_CONTENT_TYPE` | The content type is not supported by the normalizer. |
| `FAILED` | Reserved for exceptional persistence or programming failures. |

Normal mapping and freshness skips are returned as results rather than exceptions. Database failures remain
exceptions and participate in transaction rollback.

## 22. Transaction Boundary

- `importOne` performs lookup, Category/Region resolution, and TouristSpot creation or update in one transaction.
- `importAll` currently processes its input sequentially in one transaction and aggregates the results.
- A database exception during `importAll` can roll back the whole import call.
- Parallel processing, `REQUIRES_NEW`, automatic retry, explicit flush/clear, and bulk insertion are not used.
- If per-item failure isolation becomes necessary, introduce a separate orchestration Service so calls to
  transactional `importOne` pass through a Spring proxy instead of relying on self-invocation.

## 23. Database Constraint and Migration Policy

Current uniqueness rules are:

- Category: `UNIQUE(content_type_id, small_category_code)`
- Region: `UNIQUE(region_code)`, where the derived value is `areaCode` or `areaCode:sigunguCode`
- TouristSpot: `UNIQUE(tour_api_content_id)`

The derived Region key avoids the normal PostgreSQL behavior that permits multiple null values in a composite
unique constraint. FK ID fields and optional source fields are nullable; `source_modified_at` is not nullable in
the current schema. URL columns use `VARCHAR(2048)`, coordinates use `NUMERIC(13,10)`, and long descriptions use
`TEXT`.

The project does not currently use Flyway or Liquibase. Development uses Hibernate `ddl-auto=update`, while
production uses `ddl-auto=validate`. The Entity mapping and development seed SQL are aligned, so this step does
not add a migration. A versioned migration tool is required before production schema changes are deployed.

## 24. External Client Contract and Elasticsearch Handoff

A future TourAPI Client must provide `TourApiMappingResult<TouristSpotImportData>`, not persist raw external DTOs.
It must preserve mapping issues and must not treat missing remote rows as deletion requests.

Elasticsearch is not called by the persistence Service. Future indexing should run only after a `CREATED` or
`UPDATED` database transaction commits successfully. Prefer an after-commit event or outbox-style handoff rather
than making an Elasticsearch call inside the database transaction. Skip results are not indexing triggers.

## 25. PostgreSQL Repository Integration Test

`TourismRepositoryTest` uses Testcontainers PostgreSQL 17 and is tagged `postgresql`.

- `gradlew.bat test`: runs unit and service tests, excluding the PostgreSQL-tagged Repository integration test.
- `gradlew.bat postgresqlTest`: starts `postgres:17-alpine` and runs the Repository integration test against the
  real PostgreSQL engine.
- Docker must be running for `postgresqlTest`. If no Repository test can run, the task fails instead of silently
  reporting success.
- H2 remains a test runtime dependency for the other JPA service slice tests. It is not used by
  `TourismRepositoryTest`.

The PostgreSQL integration test verifies Category, Region, and TouristSpot lookup behavior, nullable
`sigunguCode` lookup, and the three database unique constraints.
