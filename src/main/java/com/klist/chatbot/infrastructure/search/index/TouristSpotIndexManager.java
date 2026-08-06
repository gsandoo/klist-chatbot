package com.klist.chatbot.infrastructure.search.index;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.data.elasticsearch.ResourceNotFoundException;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.index.AliasAction;
import org.springframework.data.elasticsearch.core.index.AliasActionParameters;
import org.springframework.data.elasticsearch.core.index.AliasActions;
import org.springframework.data.elasticsearch.core.index.AliasData;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;

public class TouristSpotIndexManager {

    private final ElasticsearchOperations operations;
    private final TouristSpotIndexProperties properties;
    private final TouristSpotIndexResourceLoader resourceLoader;

    public TouristSpotIndexManager(
            ElasticsearchOperations operations,
            TouristSpotIndexProperties properties,
            TouristSpotIndexResourceLoader resourceLoader
    ) {
        this.operations = operations;
        this.properties = properties;
        this.resourceLoader = resourceLoader;
    }

    public TouristSpotIndexInitializationResult initialize() {
        properties.validate();
        String indexName = properties.versionedIndexName();
        String alias = properties.getAlias();

        try {
            boolean created = createIndexIfAbsent(indexName);
            boolean aliasUpdated = switchAlias(indexName);
            return new TouristSpotIndexInitializationResult(indexName, alias, created, aliasUpdated);
        } catch (TouristSpotIndexingException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new TouristSpotIndexingException(
                    TouristSpotIndexOperation.CREATE_INDEX,
                    "Unable to initialize the tourist-spots index.",
                    exception
            );
        }
    }

    public String createNewVersionIndex() {
        properties.validate();
        String indexName = properties.versionedIndexName();
        IndexOperations indexOperations = operations.indexOps(IndexCoordinates.of(indexName));
        try {
            if (indexOperations.exists()) {
                throw new IllegalStateException("The target tourist-spots index already exists.");
            }
            create(indexOperations);
            return indexName;
        } catch (TouristSpotIndexingException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new TouristSpotIndexingException(
                    TouristSpotIndexOperation.CREATE_INDEX,
                    "Unable to create a new tourist-spots index.",
                    exception
            );
        }
    }

    public boolean switchAlias(String targetIndex) {
        properties.validate();
        if (!properties.versionedIndexName().equals(targetIndex)) {
            throw new IllegalArgumentException("targetIndex must be the configured versioned index.");
        }
        String alias = properties.getAlias();
        IndexOperations indexOperations = operations.indexOps(IndexCoordinates.of(alias + "-*"));
        try {
            Map<String, java.util.Set<AliasData>> currentAliases;
            try {
                currentAliases = indexOperations.getAliases(alias);
            } catch (ResourceNotFoundException exception) {
                currentAliases = Map.of();
            }
            List<AliasAction> actions = new ArrayList<>();
            currentAliases.forEach((indexName, aliases) -> {
                if (!targetIndex.equals(indexName) && aliases.stream()
                        .anyMatch(aliasData -> alias.equals(aliasData.getAlias()))) {
                    actions.add(new AliasAction.Remove(parameters(indexName, alias, null)));
                }
            });

            boolean targetIsWriteIndex = currentAliases.getOrDefault(targetIndex, java.util.Set.of()).stream()
                    .anyMatch(aliasData -> alias.equals(aliasData.getAlias())
                            && Boolean.TRUE.equals(aliasData.isWriteIndex()));
            if (!targetIsWriteIndex) {
                actions.add(new AliasAction.Add(parameters(targetIndex, alias, true)));
            }
            if (actions.isEmpty()) {
                return false;
            }
            if (!indexOperations.alias(new AliasActions(actions.toArray(AliasAction[]::new)))) {
                throw new IllegalStateException("Elasticsearch did not update the alias.");
            }
            return true;
        } catch (RuntimeException exception) {
            throw new TouristSpotIndexingException(
                    TouristSpotIndexOperation.SWITCH_ALIAS,
                    "Unable to switch the tourist-spots alias.",
                    exception
            );
        }
    }

    private boolean createIndexIfAbsent(String indexName) {
        IndexOperations indexOperations = operations.indexOps(IndexCoordinates.of(indexName));
        if (indexOperations.exists()) {
            return false;
        }
        create(indexOperations);
        return true;
    }

    private void create(IndexOperations indexOperations) {
        if (!indexOperations.create(resourceLoader.settings(), resourceLoader.mappings())) {
            throw new IllegalStateException("Elasticsearch did not create the index.");
        }
    }

    private AliasActionParameters parameters(String indexName, String alias, Boolean writeIndex) {
        AliasActionParameters.Builder builder = AliasActionParameters.builder()
                .withIndices(indexName)
                .withAliases(alias);
        if (writeIndex != null) {
            builder.withIsWriteIndex(writeIndex);
        }
        return builder.build();
    }
}
