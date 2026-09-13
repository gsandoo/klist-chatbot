package com.klist.chatbot.infrastructure.tourapi.mapper;

import com.klist.chatbot.infrastructure.tourapi.dto.TourApiDetailIntroItem;
import java.util.Arrays;
import java.util.Optional;

enum TourApiIntroFieldPolicy {
    TOURIST_ATTRACTION(12) {
        @Override
        String openingHours(TourApiDetailIntroItem intro) {
            return clean(intro.usetime());
        }
    },
    CULTURAL_FACILITY(14) {
        @Override
        String openingHours(TourApiDetailIntroItem intro) {
            return clean(intro.usetimeculture());
        }

        @Override
        String admissionFee(TourApiDetailIntroItem intro) {
            return join(clean(intro.usefee()), labeled("Parking", intro.parkingfee()));
        }
    },
    EVENT(15) {
        @Override
        String openingHours(TourApiDetailIntroItem intro) {
            return join(
                    range("Event period", intro.eventstartdate(), intro.eventenddate()),
                    labeled("Play time", intro.playtime())
            );
        }

        @Override
        String admissionFee(TourApiDetailIntroItem intro) {
            return clean(intro.usetimefestival());
        }

        @Override
        String officialUrl(TourApiDetailIntroItem intro) {
            return TourApiDataCleaner.extractUrl(intro.eventhomepage());
        }
    },
    TRAVEL_COURSE(25) {
        @Override
        String openingHours(TourApiDetailIntroItem intro) {
            return join(labeled("Schedule", intro.schedule()), labeled("Duration", intro.taketime()));
        }
    },
    LEPORTS(28) {
        @Override
        String openingHours(TourApiDetailIntroItem intro) {
            return join(labeled("Open period", intro.openperiod()), clean(intro.usetimeleports()));
        }

        @Override
        String admissionFee(TourApiDetailIntroItem intro) {
            return join(clean(intro.usefeeleports()), labeled("Parking", intro.parkingfeeleports()));
        }
    },
    LODGING(32) {
        @Override
        String openingHours(TourApiDetailIntroItem intro) {
            return join(labeled("Check-in", intro.checkintime()), labeled("Check-out", intro.checkouttime()));
        }

        @Override
        String reservationUrl(TourApiDetailIntroItem intro) {
            return TourApiDataCleaner.extractUrl(intro.reservationurl());
        }
    },
    SHOPPING(38) {
        @Override
        String openingHours(TourApiDetailIntroItem intro) {
            return clean(intro.opentime());
        }

        @Override
        String admissionFee(TourApiDetailIntroItem intro) {
            return clean(intro.saleitemcost());
        }
    },
    RESTAURANT(39) {
        @Override
        String openingHours(TourApiDetailIntroItem intro) {
            return clean(intro.opentimefood());
        }
    };

    private final int contentTypeId;

    TourApiIntroFieldPolicy(int contentTypeId) {
        this.contentTypeId = contentTypeId;
    }

    static Optional<TourApiIntroFieldPolicy> find(Integer contentTypeId, String language) {
        if (!"en".equals(language)) return find(contentTypeId);
        if (contentTypeId == null) return Optional.empty();
        return switch (contentTypeId) {
            case 76 -> Optional.of(TOURIST_ATTRACTION);
            case 78 -> Optional.of(CULTURAL_FACILITY);
            case 85 -> Optional.of(EVENT);
            case 75 -> Optional.of(LEPORTS);
            case 80 -> Optional.of(LODGING);
            case 79 -> Optional.of(SHOPPING);
            case 82 -> Optional.of(RESTAURANT);
            default -> Optional.empty();
        };
    }

    static Optional<TourApiIntroFieldPolicy> find(Integer contentTypeId) {
        if (contentTypeId == null) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(policy -> policy.contentTypeId == contentTypeId)
                .findFirst();
    }

    String openingHours(TourApiDetailIntroItem intro) {
        return null;
    }

    String admissionFee(TourApiDetailIntroItem intro) {
        return null;
    }

    String reservationUrl(TourApiDetailIntroItem intro) {
        return null;
    }

    String officialUrl(TourApiDetailIntroItem intro) {
        return null;
    }

    private static String clean(String value) {
        return TourApiDataCleaner.cleanHtmlText(value);
    }

    private static String labeled(String label, String value) {
        String cleaned = clean(value);
        if (cleaned == null) {
            return null;
        }
        return label + ": " + cleaned;
    }

    private static String range(String label, String start, String end) {
        String cleanedStart = clean(start);
        String cleanedEnd = clean(end);
        if (cleanedStart == null && cleanedEnd == null) {
            return null;
        }
        if (cleanedStart == null) {
            return label + ": " + cleanedEnd;
        }
        if (cleanedEnd == null) {
            return label + ": " + cleanedStart;
        }
        return label + ": " + cleanedStart + " ~ " + cleanedEnd;
    }

    private static String join(String... values) {
        StringBuilder builder = new StringBuilder();
        for (String value : values) {
            String cleaned = TourApiDataCleaner.normalizeText(value);
            if (cleaned == null) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(" / ");
            }
            builder.append(cleaned);
        }
        if (builder.isEmpty()) {
            return null;
        }
        return builder.toString();
    }
}
