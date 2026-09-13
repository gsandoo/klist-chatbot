package com.klist.chatbot.infrastructure.tourapi.client;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tour-api")
public class TourApiProperties {

    private String baseUrl = "https://apis.data.go.kr/B551011/KorService2";
    private String serviceKey;
    private String language = "ko";
    private String englishBaseUrl = "https://apis.data.go.kr/B551011/EngService2";

    private String mobileOs = "ETC";
    private String mobileApp = "klist-chatbot";
    private String responseType = "json";
    private Duration connectTimeout = Duration.ofSeconds(3);
    private Duration responseTimeout = Duration.ofSeconds(5);
    private Duration requestInterval = Duration.ofMillis(100);
    private int retryMaxAttempts = 3;
    private Duration retryInitialBackoff = Duration.ofMillis(200);
    private Duration retryMaxBackoff = Duration.ofSeconds(2);

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        if (!java.util.Set.of("ko", "en").contains(language)) {
            throw new IllegalArgumentException("tour-api.language must be ko or en");
        }
        this.language = language;
    }

    public String getEnglishBaseUrl() {
        return englishBaseUrl;
    }

    public void setEnglishBaseUrl(String englishBaseUrl) {
        this.englishBaseUrl = englishBaseUrl;
    }

    public String getBaseUrl() {
        return "en".equals(language) ? englishBaseUrl : baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getServiceKey() {
        return serviceKey;
    }

    public void setServiceKey(String serviceKey) {
        this.serviceKey = serviceKey;
    }

    public String getMobileOs() {
        return mobileOs;
    }

    public void setMobileOs(String mobileOs) {
        this.mobileOs = mobileOs;
    }

    public String getMobileApp() {
        return mobileApp;
    }

    public void setMobileApp(String mobileApp) {
        this.mobileApp = mobileApp;
    }

    public String getResponseType() {
        return responseType;
    }

    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Duration getResponseTimeout() {
        return responseTimeout;
    }

    public void setResponseTimeout(Duration responseTimeout) {
        this.responseTimeout = responseTimeout;
    }

    public Duration getRequestInterval() {
        return requestInterval;
    }

    public void setRequestInterval(Duration requestInterval) {
        this.requestInterval = requestInterval;
    }

    public int getRetryMaxAttempts() {
        return retryMaxAttempts;
    }

    public void setRetryMaxAttempts(int retryMaxAttempts) {
        this.retryMaxAttempts = retryMaxAttempts;
    }

    public Duration getRetryInitialBackoff() {
        return retryInitialBackoff;
    }

    public void setRetryInitialBackoff(Duration retryInitialBackoff) {
        this.retryInitialBackoff = retryInitialBackoff;
    }

    public Duration getRetryMaxBackoff() {
        return retryMaxBackoff;
    }

    public void setRetryMaxBackoff(Duration retryMaxBackoff) {
        this.retryMaxBackoff = retryMaxBackoff;
    }
}
