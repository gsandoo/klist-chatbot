package com.klist.chatbot.domain.category.service.result;

public record CategoryResolution(
        Long id,
        boolean resolved,
        String warning
) {

    public static CategoryResolution resolved(Long id) {
        return new CategoryResolution(id, true, null);
    }

    public static CategoryResolution unresolved(String warning) {
        return new CategoryResolution(null, false, warning);
    }
}
