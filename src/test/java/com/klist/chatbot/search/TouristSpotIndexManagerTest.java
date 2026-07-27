package com.klist.chatbot.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.klist.chatbot.infrastructure.search.index.TouristSpotIndexInitializationResult;
import com.klist.chatbot.infrastructure.search.index.TouristSpotIndexManager;
import com.klist.chatbot.infrastructure.search.index.TouristSpotIndexOperation;
import com.klist.chatbot.infrastructure.search.index.TouristSpotIndexProperties;
import com.klist.chatbot.infrastructure.search.index.TouristSpotIndexResourceLoader;
import com.klist.chatbot.infrastructure.search.index.TouristSpotIndexingException;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.elasticsearch.ResourceNotFoundException;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.index.AliasAction;
import org.springframework.data.elasticsearch.core.index.AliasActions;
import org.springframework.data.elasticsearch.core.index.AliasData;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;

class TouristSpotIndexManagerTest {

    private final ElasticsearchOperations operations = mock(ElasticsearchOperations.class);
    private final IndexOperations indexOperations = mock(IndexOperations.class);
    private final IndexOperations aliasOperations = mock(IndexOperations.class);
    private TouristSpotIndexManager manager;

    @BeforeEach
    void setUp() {
        TouristSpotIndexProperties properties = properties();
        when(operations.indexOps(any(IndexCoordinates.class))).thenAnswer(invocation -> {
            IndexCoordinates coordinates = invocation.getArgument(0);
            return coordinates.getIndexName().endsWith("-*") ? aliasOperations : indexOperations;
        });
        manager = new TouristSpotIndexManager(
                operations,
                properties,
                new TouristSpotIndexResourceLoader(new ObjectMapper(), properties)
        );
    }

    @Test
    void createsVersionedIndexAndAtomicallyMovesWriteAlias() {
        when(indexOperations.exists()).thenReturn(false);
        when(indexOperations.create(any(Map.class), any())).thenReturn(true);
        when(aliasOperations.getAliases("tourist-spots")).thenReturn(Map.of(
                "tourist-spots-v0",
                Set.of(AliasData.of("tourist-spots", null, null, null, true, false))
        ));
        when(aliasOperations.alias(any(AliasActions.class))).thenReturn(true);

        TouristSpotIndexInitializationResult result = manager.initialize();

        assertThat(result).isEqualTo(new TouristSpotIndexInitializationResult(
                "tourist-spots-v1", "tourist-spots", true, true
        ));
        ArgumentCaptor<AliasActions> actionsCaptor = ArgumentCaptor.forClass(AliasActions.class);
        verify(aliasOperations).alias(actionsCaptor.capture());
        assertThat(actionsCaptor.getValue().getActions()).hasSize(2);
        AliasAction remove = actionsCaptor.getValue().getActions().get(0);
        AliasAction add = actionsCaptor.getValue().getActions().get(1);
        assertThat(remove).isInstanceOf(AliasAction.Remove.class);
        assertThat(remove.getParameters().getIndices()).containsExactly("tourist-spots-v0");
        assertThat(add).isInstanceOf(AliasAction.Add.class);
        assertThat(add.getParameters().getIndices()).containsExactly("tourist-spots-v1");
        assertThat(add.getParameters().getWriteIndex()).isTrue();
    }

    @Test
    void doesNothingWhenVersionAndWriteAliasAreReady() {
        when(indexOperations.exists()).thenReturn(true);
        when(aliasOperations.getAliases("tourist-spots")).thenReturn(Map.of(
                "tourist-spots-v1",
                Set.of(AliasData.of("tourist-spots", null, null, null, true, false))
        ));

        TouristSpotIndexInitializationResult result = manager.initialize();

        assertThat(result.indexCreated()).isFalse();
        assertThat(result.aliasUpdated()).isFalse();
        verify(indexOperations, never()).create(any(Map.class), any());
        verify(aliasOperations, never()).alias(any());
    }

    @Test
    void addsWriteAliasWhenAliasDoesNotExistYet() {
        when(indexOperations.exists()).thenReturn(true);
        when(aliasOperations.getAliases("tourist-spots"))
                .thenThrow(new ResourceNotFoundException("alias not found"));
        when(aliasOperations.alias(any(AliasActions.class))).thenReturn(true);

        TouristSpotIndexInitializationResult result = manager.initialize();

        assertThat(result.aliasUpdated()).isTrue();
        ArgumentCaptor<AliasActions> actionsCaptor = ArgumentCaptor.forClass(AliasActions.class);
        verify(aliasOperations).alias(actionsCaptor.capture());
        assertThat(actionsCaptor.getValue().getActions())
                .singleElement()
                .isInstanceOf(AliasAction.Add.class);
    }

    @Test
    void convertsElasticsearchFailureToApplicationException() {
        when(indexOperations.exists()).thenThrow(new IllegalStateException("cluster unavailable"));

        assertThatThrownBy(manager::initialize)
                .isInstanceOfSatisfying(TouristSpotIndexingException.class, exception -> {
                    assertThat(exception.operation()).isEqualTo(TouristSpotIndexOperation.CREATE_INDEX);
                    assertThat(exception.getMessage()).doesNotContain("cluster unavailable");
                });
    }

    private static TouristSpotIndexProperties properties() {
        TouristSpotIndexProperties properties = new TouristSpotIndexProperties();
        properties.setAlias("tourist-spots");
        properties.setVersion("v1");
        properties.setSettingsLocation(new ClassPathResource("elasticsearch/tourist-spots-settings.json"));
        properties.setMappingsLocation(new ClassPathResource("elasticsearch/tourist-spots-mappings.json"));
        return properties;
    }
}
