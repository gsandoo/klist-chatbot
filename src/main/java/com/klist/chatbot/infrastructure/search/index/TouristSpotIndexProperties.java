package com.klist.chatbot.infrastructure.search.index;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

@ConfigurationProperties(prefix = "search.tourist-spots")
public class TouristSpotIndexProperties {

    private String alias = "tourist-spots";
    private String version = "v1";
    private Resource settingsLocation;
    private Resource mappingsLocation;

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Resource getSettingsLocation() {
        return settingsLocation;
    }

    public void setSettingsLocation(Resource settingsLocation) {
        this.settingsLocation = settingsLocation;
    }

    public Resource getMappingsLocation() {
        return mappingsLocation;
    }

    public void setMappingsLocation(Resource mappingsLocation) {
        this.mappingsLocation = mappingsLocation;
    }

    public String versionedIndexName() {
        validateName(alias, "alias");
        validateName(version, "version");
        return alias + "-" + version;
    }

    public void validate() {
        versionedIndexName();
        if (settingsLocation == null || mappingsLocation == null) {
            throw new IllegalStateException("Index settings and mappings locations are required.");
        }
    }

    private void validateName(String value, String field) {
        if (value == null || !value.matches("[a-z0-9][a-z0-9._-]*")) {
            throw new IllegalStateException(field + " must be a valid lowercase Elasticsearch name.");
        }
    }
}
