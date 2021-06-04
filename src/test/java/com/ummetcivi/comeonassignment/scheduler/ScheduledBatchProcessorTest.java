package com.ummetcivi.comeonassignment.scheduler;

import com.ummetcivi.comeonassignment.TestConstants;
import com.ummetcivi.comeonassignment.accessor.RemoteDatasetAccessor;
import com.ummetcivi.comeonassignment.data.entity.BatchEntity;
import com.ummetcivi.comeonassignment.data.entity.DatasetEntity;
import com.ummetcivi.comeonassignment.data.entity.EmailEntity;
import com.ummetcivi.comeonassignment.data.jpa.BatchDatasetRepository;
import com.ummetcivi.comeonassignment.data.jpa.BatchRepository;
import com.ummetcivi.comeonassignment.data.jpa.EmailRepository;
import com.ummetcivi.comeonassignment.domain.Dataset;
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
    private EmailRepository emailRepository;
    @Mock
    private RemoteDatasetAccessor remoteDatasetAccessor;

    private ScheduledBatchProcessor underTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        underTest = new ScheduledBatchProcessor(batchRepository, batchDatasetRepository, emailRepository,
                remoteDatasetAccessor);

        ReflectionTestUtils.setField(underTest, "allowedDomains", Set.of(TestConstants.ANY_ALLOWED_DOMAIN));
    }

    @Test
    void shouldProcessExpiredBatchesWithOnlyEmails() {
        // Given
        final BatchEntity batchEntity = Mockito.spy(BatchEntity.builder().build());
        final List<BatchEntity> expiredBatchList = List.of(batchEntity);
        final DatasetEntity datasetEntity = Mockito.mock(DatasetEntity.class);
        final List<DatasetEntity> datasetList = List.of(datasetEntity);

        Mockito.when(batchEntity.getId()).thenReturn(TestConstants.ANY_BATCH_ID);
        Mockito.when(batchEntity.getStatus()).thenReturn(BatchStatus.NOT_PROCESSED);
        Mockito.when(datasetEntity.getEmails()).thenReturn(List.of(TestConstants.ANY_EMAIL));

        Mockito.when(batchRepository
                .findAllByExpiresAtBeforeAndStatus(Mockito.any(Instant.class), Mockito.eq(BatchStatus.NOT_PROCESSED)))
                .thenReturn(expiredBatchList);
        Mockito.when(batchDatasetRepository.findAllByBatchId(TestConstants.ANY_BATCH_ID)).thenReturn(datasetList);

        // When
        underTest.processBatch();

        // Then
        Mockito.verify(batchRepository)
                .findAllByExpiresAtBeforeAndStatus(Mockito.any(Instant.class), Mockito.eq(BatchStatus.NOT_PROCESSED));
        Mockito.verify(batchEntity).setStatus(BatchStatus.IN_PROGRESS);

        Mockito.verify(batchDatasetRepository).deleteAll(datasetList);

        ArgumentCaptor<List<EmailEntity>> emailEntityArgumentCaptor = ArgumentCaptor.forClass(List.class);
        Mockito.verify(emailRepository).saveAll(emailEntityArgumentCaptor.capture());

        final List<EmailEntity> savedEmailEntityList = emailEntityArgumentCaptor.getValue();

        Assertions.assertNotNull(savedEmailEntityList);
        Assertions.assertEquals(1, savedEmailEntityList.size());

        final EmailEntity savedEmailEntity = savedEmailEntityList.get(0);

        Assertions.assertEquals(1, savedEmailEntity.getOccurrence());
        Assertions.assertEquals(TestConstants.ANY_EMAIL, savedEmailEntity.getEmail());
        Assertions.assertEquals(TestConstants.ANY_BATCH_ID, savedEmailEntity.getBatchId());
        Assertions.assertNotNull(savedEmailEntity.getId());
    }

    @Test
    void shouldProcessExpiredBatchesWithEmailsAndURLsAndDropNotAllowedDomains() {
        // Given
        final BatchEntity batchEntity = Mockito.spy(BatchEntity.builder().build());
        final List<BatchEntity> expiredBatchList = List.of(batchEntity);
        final DatasetEntity datasetEntity = Mockito.mock(DatasetEntity.class);
        final List<DatasetEntity> datasetList = List.of(datasetEntity);
        final Dataset remoteDataset = Mockito.mock(Dataset.class);

        Mockito.when(batchEntity.getId()).thenReturn(TestConstants.ANY_BATCH_ID);
        Mockito.when(batchEntity.getStatus()).thenReturn(BatchStatus.NOT_PROCESSED);
        Mockito.when(datasetEntity.getEmails()).thenReturn(List.of(TestConstants.ANY_EMAIL));
        Mockito.when(datasetEntity.getUrls()).thenReturn(List.of(TestConstants.ANY_URL));
        Mockito.when(remoteDataset.getEmails())
                .thenReturn(List.of(TestConstants.ANY_EMAIL, TestConstants.ANY_OTHER_EMAIL,
                        TestConstants.ANY_NOT_ALLOWED_EMAIL));

        Mockito.when(batchRepository
                .findAllByExpiresAtBeforeAndStatus(Mockito.any(Instant.class), Mockito.eq(BatchStatus.NOT_PROCESSED)))
                .thenReturn(expiredBatchList);
        Mockito.when(batchDatasetRepository.findAllByBatchId(TestConstants.ANY_BATCH_ID)).thenReturn(datasetList);
        Mockito.when(remoteDatasetAccessor.fetchDataset(TestConstants.ANY_URL)).thenReturn(remoteDataset);

        // When
        underTest.processBatch();

        // Then
        Mockito.verify(batchRepository)
                .findAllByExpiresAtBeforeAndStatus(Mockito.any(Instant.class), Mockito.eq(BatchStatus.NOT_PROCESSED));
        Mockito.verify(batchEntity).setStatus(BatchStatus.IN_PROGRESS);

        Mockito.verify(batchDatasetRepository).deleteAll(datasetList);

        ArgumentCaptor<List<EmailEntity>> emailEntityArgumentCaptor = ArgumentCaptor.forClass(List.class);
        Mockito.verify(emailRepository).saveAll(emailEntityArgumentCaptor.capture());

        final List<EmailEntity> savedEmailEntityList = emailEntityArgumentCaptor.getValue();

        Assertions.assertNotNull(savedEmailEntityList);
        Assertions.assertEquals(2, savedEmailEntityList.size());

        savedEmailEntityList.forEach(emailEntity -> {
            Assertions.assertEquals(TestConstants.ANY_BATCH_ID, emailEntity.getBatchId());
            Assertions.assertNotNull(emailEntity.getId());

            if (TestConstants.ANY_EMAIL.equals(emailEntity.getEmail())) {
                Assertions.assertEquals(2, emailEntity.getOccurrence());
            } else if (TestConstants.ANY_OTHER_EMAIL.equals(emailEntity.getEmail())) {
                Assertions.assertEquals(1, emailEntity.getOccurrence());
            } else {
                Assertions.fail();
            }
        });
    }

    @Test
    void shouldProcessExpiredBatchesWithURLsRecursively() {
        // Given
        final BatchEntity batchEntity = Mockito.spy(BatchEntity.builder().build());
        final List<BatchEntity> expiredBatchList = List.of(batchEntity);
        final DatasetEntity datasetEntity = Mockito.mock(DatasetEntity.class);
        final List<DatasetEntity> datasetList = List.of(datasetEntity);
        final Dataset remoteDataset = Mockito.mock(Dataset.class);
        final Dataset anotherRemoteDataset = Mockito.mock(Dataset.class);

        Mockito.when(batchEntity.getId()).thenReturn(TestConstants.ANY_BATCH_ID);
        Mockito.when(batchEntity.getStatus()).thenReturn(BatchStatus.NOT_PROCESSED);
        Mockito.when(datasetEntity.getUrls()).thenReturn(List.of(TestConstants.ANY_URL));

        Mockito.when(remoteDataset.getEmails()).thenReturn(List.of(TestConstants.ANY_EMAIL));
        Mockito.when(remoteDataset.getUrls()).thenReturn(List.of(TestConstants.ANY_OTHER_URL));
        Mockito.when(anotherRemoteDataset.getEmails()).thenReturn(
                List.of(TestConstants.ANY_EMAIL, TestConstants.ANY_OTHER_EMAIL, TestConstants.ANY_NOT_ALLOWED_EMAIL));

        Mockito.when(batchRepository
                .findAllByExpiresAtBeforeAndStatus(Mockito.any(Instant.class), Mockito.eq(BatchStatus.NOT_PROCESSED)))
                .thenReturn(expiredBatchList);
        Mockito.when(batchDatasetRepository.findAllByBatchId(TestConstants.ANY_BATCH_ID)).thenReturn(datasetList);
        Mockito.when(remoteDatasetAccessor.fetchDataset(TestConstants.ANY_URL)).thenReturn(remoteDataset);
        Mockito.when(remoteDatasetAccessor.fetchDataset(TestConstants.ANY_OTHER_URL)).thenReturn(anotherRemoteDataset);

        // When
        underTest.processBatch();

        // Then
        Mockito.verify(batchRepository)
                .findAllByExpiresAtBeforeAndStatus(Mockito.any(Instant.class), Mockito.eq(BatchStatus.NOT_PROCESSED));
        Mockito.verify(batchEntity).setStatus(BatchStatus.IN_PROGRESS);

        Mockito.verify(batchDatasetRepository).deleteAll(datasetList);

        ArgumentCaptor<List<EmailEntity>> emailEntityArgumentCaptor = ArgumentCaptor.forClass(List.class);
        Mockito.verify(emailRepository).saveAll(emailEntityArgumentCaptor.capture());

        final List<EmailEntity> savedEmailEntityList = emailEntityArgumentCaptor.getValue();

        Assertions.assertNotNull(savedEmailEntityList);
        Assertions.assertEquals(2, savedEmailEntityList.size());

        savedEmailEntityList.forEach(emailEntity -> {
            Assertions.assertEquals(TestConstants.ANY_BATCH_ID, emailEntity.getBatchId());
            Assertions.assertNotNull(emailEntity.getId());

            if (TestConstants.ANY_EMAIL.equals(emailEntity.getEmail())) {
                Assertions.assertEquals(2, emailEntity.getOccurrence());
            } else if (TestConstants.ANY_OTHER_EMAIL.equals(emailEntity.getEmail())) {
                Assertions.assertEquals(1, emailEntity.getOccurrence());
            } else {
                Assertions.fail();
            }
        });
    }
}