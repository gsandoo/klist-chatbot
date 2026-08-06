package com.klist.chatbot.infrastructure.tourapi.mapper;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;
import org.springframework.web.util.HtmlUtils;

final class TourApiDataCleaner {

    private static final DateTimeFormatter TOUR_API_DATE_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private static final Pattern HREF = Pattern.compile("(?i)href\\s*=\\s*['\"]([^'\"]+)['\"]");

    private TourApiDataCleaner() {
    }

    static String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String unescaped = HtmlUtils.htmlUnescape(value).replace('\u00A0', ' ');
        String trimmed = WHITESPACE.matcher(unescaped.trim()).replaceAll(" ");
        if (trimmed.isBlank() || "null".equalsIgnoreCase(trimmed)) {
            return null;
        }
        return trimmed;
    }

    static String cleanHtmlText(String value) {
        String normalized = normalizeText(value);
        if (normalized == null) {
            return null;
        }
        String parsed = parseHtmlText(normalized);
        if (parsed == null) {
            return normalizeText(normalized.replaceAll("<[^>]+>", " "));
        }
        return parsed;
    }

    static String extractUrl(String value) {
        String normalized = normalizeText(value);
        if (normalized == null) {
            return null;
        }
        Matcher matcher = HREF.matcher(normalized);
        if (matcher.find()) {
            return validUrlOrNull(matcher.group(1));
        }
        return validUrlOrNull(normalized);
    }

    static LocalDateTime parseTourApiDateTime(String value) {
        String normalized = normalizeText(value);
        if (normalized == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(normalized, TOUR_API_DATE_TIME);
        } catch (DateTimeParseException exception) {
            return null;
        }
    }

    private static String validUrlOrNull(String value) {
        String normalized = normalizeText(value);
        if (normalized == null) {
            return null;
        }
        try {
            URI uri = new URI(normalized);
            String scheme = uri.getScheme();
            if (scheme == null) {
                return null;
            }
            String lowerScheme = scheme.toLowerCase(Locale.ROOT);
            if (!"http".equals(lowerScheme) && !"https".equals(lowerScheme)) {
                return null;
            }
            if (uri.getHost() == null || uri.getHost().isBlank()) {
                return null;
            }
            return uri.toString();
        } catch (URISyntaxException exception) {
            return null;
        }
    }

    private static String parseHtmlText(String html) {
        StringBuilder text = new StringBuilder();
        HTMLEditorKit.ParserCallback callback = new HTMLEditorKit.ParserCallback() {
            @Override
            public void handleText(char[] data, int pos) {
                text.append(data).append(' ');
            }

            @Override
            public void handleSimpleTag(HTML.Tag tag, MutableAttributeSet attributes, int pos) {
                if (HTML.Tag.BR.equals(tag)) {
                    text.append(' ');
                }
            }
        };
        try {
            new ParserDelegator().parse(new StringReader(html), callback, true);
        } catch (IOException runtimeParserException) {
            return null;
        }
        return normalizeText(text.toString());
    }
}
