package com.ummetcivi.comeonassignment.converter;

import com.ummetcivi.comeonassignment.TestConstants;
import com.ummetcivi.comeonassignment.data.entity.BatchEntity;
import com.ummetcivi.comeonassignment.domain.Batch;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;

class BatchEntityToBatchConverterTest {

    private BatchEntityToBatchConverter underTest;

    @BeforeEach
    void setUp() {
        underTest = new BatchEntityToBatchConverter();
    }

    @Test
    void shouldConvert() {
        // Given
        final BatchEntity batchEntity = Mockito.mock(BatchEntity.class);
        final Instant createdAt = Instant.now();
        final Instant expiresAt = Instant.now().plusSeconds(5);

        Mockito.when(batchEntity.getId()).thenReturn(TestConstants.ANY_BATCH_ID);
        Mockito.when(batchEntity.getExpiresAt()).thenReturn(expiresAt);
        Mockito.when(batchEntity.getCreatedAt()).thenReturn(createdAt);

        // When
        final Batch result = underTest.convert(batchEntity);

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertEquals(createdAt, result.getCreatedAt());
        Assertions.assertEquals(expiresAt, result.getExpiresAt());
        Assertions.assertEquals(TestConstants.ANY_BATCH_ID, result.getId());
    }
}