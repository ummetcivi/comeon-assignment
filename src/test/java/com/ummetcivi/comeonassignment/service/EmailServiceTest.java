package com.ummetcivi.comeonassignment.service;

import com.ummetcivi.comeonassignment.TestConstants;
import com.ummetcivi.comeonassignment.data.entity.BatchEntity;
import com.ummetcivi.comeonassignment.data.entity.DatasetEntity;
import com.ummetcivi.comeonassignment.data.jpa.BatchDatasetRepository;
import com.ummetcivi.comeonassignment.data.jpa.BatchRepository;
import com.ummetcivi.comeonassignment.data.jpa.EmailRepository;
import com.ummetcivi.comeonassignment.domain.Batch;
import com.ummetcivi.comeonassignment.domain.Dataset;
import com.ummetcivi.comeonassignment.enums.BatchStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.core.convert.ConversionService;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

class EmailServiceTest {

    @Mock
    private BatchRepository batchRepository;
    @Mock
    private BatchDatasetRepository batchDatasetRepository;
    @Mock
    private EmailRepository processedBatchRepository;
    @Mock
    private ConversionService conversionService;

    private EmailService underTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        underTest = new EmailService(batchRepository, batchDatasetRepository, processedBatchRepository,
                conversionService);

        ReflectionTestUtils.setField(underTest, "batchWindowInMinutes", 5);
    }

    @Test
    void shouldImportEmailsToCurrentBatch() {
        // Given
        final BatchEntity batchEntity = Mockito.mock(BatchEntity.class);
        final Dataset dataset = Mockito.mock(Dataset.class);
        final Batch batch = Mockito.mock(Batch.class);

        final List<String> emails = List.of(TestConstants.ANY_EMAIL);
        final List<String> urls = List.of(TestConstants.ANY_URL);

        Mockito.when(dataset.getEmails()).thenReturn(emails);
        Mockito.when(dataset.getUrls()).thenReturn(urls);

        Mockito.when(batchEntity.getId()).thenReturn(TestConstants.ANY_BATCH_ID);

        Mockito.when(batchRepository.findByExpiresAtAfter(Mockito.any(Instant.class)))
                .thenReturn(Optional.of(batchEntity));
        Mockito.when(conversionService.convert(batchEntity, Batch.class)).thenReturn(batch);

        // When
        final Batch result = underTest.importEmails(dataset);

        // Then
        Assertions.assertEquals(batch, result);

        ArgumentCaptor<DatasetEntity> datasetEntityArgumentCaptor = ArgumentCaptor.forClass(DatasetEntity.class);
        Mockito.verify(batchDatasetRepository).save(datasetEntityArgumentCaptor.capture());

        final DatasetEntity savedDataset = datasetEntityArgumentCaptor.getValue();

        Assertions.assertNotNull(savedDataset);
        Assertions.assertEquals(TestConstants.ANY_BATCH_ID, savedDataset.getBatchId());
        Assertions.assertEquals(emails, savedDataset.getEmails());
        Assertions.assertEquals(urls, savedDataset.getUrls());

        Mockito.verify(batchRepository).findByExpiresAtAfter(Mockito.any(Instant.class));
        Mockito.verify(conversionService).convert(batchEntity, Batch.class);
    }

    @Test
    void shouldImportEmailsToNewBatch() {
        // Given
        final Dataset dataset = Mockito.mock(Dataset.class);
        final Batch batch = Mockito.mock(Batch.class);

        final List<String> emails = List.of(TestConstants.ANY_EMAIL);
        final List<String> urls = List.of(TestConstants.ANY_URL);

        Mockito.when(dataset.getEmails()).thenReturn(emails);
        Mockito.when(dataset.getUrls()).thenReturn(urls);

        Mockito.when(conversionService.convert(Mockito.any(BatchEntity.class), Mockito.eq(Batch.class)))
                .thenReturn(batch);
        Mockito.when(batchRepository.save(Mockito.any(BatchEntity.class)))
                .then(invocation -> invocation.getArgument(0));

        // When
        final Batch result = underTest.importEmails(dataset);

        // Then
        Assertions.assertEquals(batch, result);

        ArgumentCaptor<BatchEntity> batchEntityArgumentCaptor = ArgumentCaptor.forClass(BatchEntity.class);
        Mockito.verify(batchRepository).save(batchEntityArgumentCaptor.capture());

        final BatchEntity savedBatchEntity = batchEntityArgumentCaptor.getValue();
        Assertions.assertEquals(savedBatchEntity.getCreatedAt(),
                savedBatchEntity.getExpiresAt().minus(5, ChronoUnit.MINUTES));
        Assertions.assertEquals(BatchStatus.NOT_PROCESSED, savedBatchEntity.getStatus());
        Assertions.assertNotNull(savedBatchEntity.getId());

        ArgumentCaptor<DatasetEntity> datasetEntityArgumentCaptor = ArgumentCaptor.forClass(DatasetEntity.class);
        Mockito.verify(batchDatasetRepository).save(datasetEntityArgumentCaptor.capture());

        final DatasetEntity savedDataset = datasetEntityArgumentCaptor.getValue();

        Assertions.assertNotNull(savedDataset);
        Assertions.assertEquals(savedBatchEntity.getId(), savedDataset.getBatchId());
        Assertions.assertEquals(emails, savedDataset.getEmails());
        Assertions.assertEquals(urls, savedDataset.getUrls());
    }
}