package com.klist.chatbot.domain.region.service.result;

public record RegionResolution(
        Long id,
        boolean resolved,
        String warning
) {

    public static RegionResolution resolved(Long id) {
        return new RegionResolution(id, true, null);
    }

    public static RegionResolution unresolved(String warning) {
        return new RegionResolution(null, false, warning);
    }
}
