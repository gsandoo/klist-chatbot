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
