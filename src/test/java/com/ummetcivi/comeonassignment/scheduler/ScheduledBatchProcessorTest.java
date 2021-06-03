package com.ummetcivi.comeonassignment.scheduler;

import com.ummetcivi.comeonassignment.TestConstants;
import com.ummetcivi.comeonassignment.TestUtil;
import com.ummetcivi.comeonassignment.accessor.RemoteDatasetAccessor;
import com.ummetcivi.comeonassignment.data.entity.BatchEntity;
import com.ummetcivi.comeonassignment.data.entity.DatasetEntity;
import com.ummetcivi.comeonassignment.data.entity.ProcessedBatchEntity;
import com.ummetcivi.comeonassignment.data.jpa.BatchDatasetRepository;
import com.ummetcivi.comeonassignment.data.jpa.BatchRepository;
import com.ummetcivi.comeonassignment.data.jpa.ProcessedBatchRepository;
import com.ummetcivi.comeonassignment.domain.Dataset;
import com.ummetcivi.comeonassignment.domain.EmailOccurrence;
import com.ummetcivi.comeonassignment.enums.BatchStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Set;

class ScheduledBatchProcessorTest {

    @Mock
    private BatchRepository batchRepository;
    @Mock
    private BatchDatasetRepository batchDatasetRepository;
    @Mock
    private ProcessedBatchRepository processedBatchRepository;
    @Mock
    private RemoteDatasetAccessor remoteDatasetAccessor;

    private ScheduledBatchProcessor underTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        underTest = new ScheduledBatchProcessor(batchRepository, batchDatasetRepository, processedBatchRepository,
                remoteDatasetAccessor);

        ReflectionTestUtils.setField(underTest, "allowedDomains", Set.of(TestConstants.ANY_ALLOWED_DOMAIN));
    }

    @Test
    void shouldDiscardEmailsWithNotAllowedDomains() {
        // Given
        final BatchEntity expiredBatchEntity = TestUtil.createNonProcessedBatchEntity();
        final DatasetEntity datasetEntity = TestUtil
                .createDatasetEntity(List.of(TestConstants.ANY_EMAIL, TestConstants.ANY_NOT_ALLOWED_EMAIL));

        final List<BatchEntity> expiredBatches = List.of(expiredBatchEntity);
        final List<DatasetEntity> datasetEntities = List.of(datasetEntity);

        Mockito.when(batchRepository
                .findAllByExpiresAtBeforeAndStatus(Mockito.any(Instant.class), Mockito.eq(BatchStatus.NOT_PROCESSED)))
                .thenReturn(expiredBatches);

        Mockito.when(batchDatasetRepository.findAllByBatchId(TestConstants.ANY_BATCH_ID)).thenReturn(datasetEntities);

        // When
        underTest.processBatch();

        // Then
        ArgumentCaptor<ProcessedBatchEntity> processedBatchEntityArgumentCaptor = ArgumentCaptor
                .forClass(ProcessedBatchEntity.class);
        Mockito.verify(processedBatchRepository).save(processedBatchEntityArgumentCaptor.capture());

        final ProcessedBatchEntity savedProcessedBatchEntity = processedBatchEntityArgumentCaptor.getValue();
        final List<EmailOccurrence> emails = savedProcessedBatchEntity.getEmails();

        Assertions.assertEquals(1, emails.size());
        Assertions.assertEquals(TestConstants.ANY_EMAIL, emails.get(0).getEmail());
        Assertions.assertEquals(1, emails.get(0).getOccurrence());
    }

    @Test
    void shouldProcessRemoteDataset() {
        // Given
        final BatchEntity expiredBatchEntity = TestUtil.createNonProcessedBatchEntity();
        final DatasetEntity datasetEntity = TestUtil
                .createDatasetEntity(List.of(TestConstants.ANY_EMAIL, TestConstants.ANY_NOT_ALLOWED_EMAIL));
        final Dataset dataset = TestUtil.createDataset(List.of(TestConstants.ANY_EMAIL, TestConstants.ANY_OTHER_EMAIL));

        final List<BatchEntity> expiredBatches = List.of(expiredBatchEntity);
        final List<DatasetEntity> datasetEntities = List.of(datasetEntity);

        Mockito.when(batchRepository
                .findAllByExpiresAtBeforeAndStatus(Mockito.any(Instant.class), Mockito.eq(BatchStatus.NOT_PROCESSED)))
                .thenReturn(expiredBatches);
        Mockito.when(batchDatasetRepository.findAllByBatchId(TestConstants.ANY_BATCH_ID)).thenReturn(datasetEntities);
        Mockito.when(remoteDatasetAccessor.fetchDataset(TestConstants.ANY_URL)).thenReturn(dataset);

        // When
        underTest.processBatch();

        // Then
        ArgumentCaptor<ProcessedBatchEntity> processedBatchEntityArgumentCaptor = ArgumentCaptor
                .forClass(ProcessedBatchEntity.class);
        Mockito.verify(processedBatchRepository).save(processedBatchEntityArgumentCaptor.capture());

        final ProcessedBatchEntity savedProcessedBatchEntity = processedBatchEntityArgumentCaptor.getValue();
        final List<EmailOccurrence> emails = savedProcessedBatchEntity.getEmails();

        Assertions.assertEquals(2, emails.size());
        emails.forEach(emailOccurrence -> {
            if (TestConstants.ANY_EMAIL.equals(emailOccurrence.getEmail())) {
                Assertions.assertEquals(2, emailOccurrence.getOccurrence());
            } else if (TestConstants.ANY_OTHER_EMAIL.equals(emailOccurrence.getEmail())) {
                Assertions.assertEquals(1, emailOccurrence.getOccurrence());
            } else {
                Assertions.fail();
            }
        });
    }
}