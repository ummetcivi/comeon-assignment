package com.ummetcivi.comeonassignment.service;

import com.ummetcivi.comeonassignment.TestConstants;
import com.ummetcivi.comeonassignment.TestUtil;
import com.ummetcivi.comeonassignment.data.entity.BatchEntity;
import com.ummetcivi.comeonassignment.data.entity.DatasetEntity;
import com.ummetcivi.comeonassignment.data.entity.EmailEntity;
import com.ummetcivi.comeonassignment.data.jpa.BatchDatasetRepository;
import com.ummetcivi.comeonassignment.data.jpa.BatchRepository;
import com.ummetcivi.comeonassignment.data.jpa.EmailRepository;
import com.ummetcivi.comeonassignment.domain.Batch;
import com.ummetcivi.comeonassignment.domain.Dataset;
import com.ummetcivi.comeonassignment.domain.Email;
import com.ummetcivi.comeonassignment.enums.BatchStatus;
import com.ummetcivi.comeonassignment.exception.BadRequestException;
import com.ummetcivi.comeonassignment.exception.ResourceNotFoundException;
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
import java.util.Set;

class EmailServiceTest {

    @Mock
    private BatchRepository batchRepository;
    @Mock
    private BatchDatasetRepository batchDatasetRepository;
    @Mock
    private EmailRepository emailRepository;
    @Mock
    private ConversionService conversionService;

    private EmailService underTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        underTest = new EmailService(batchRepository, batchDatasetRepository, emailRepository,
                conversionService);

        ReflectionTestUtils.setField(underTest, "batchWindowInMinutes", 5);
        ReflectionTestUtils.setField(underTest, "allowedDomains", Set.of(TestConstants.ANY_ALLOWED_DOMAIN));
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

    @Test
    void shouldGetAllWithBatchId() {
        // Given
        final EmailEntity emailEntity = Mockito.mock(EmailEntity.class);
        final Email email = Mockito.mock(Email.class);
        final List<Email> emails = List.of(email);

        Mockito.when(emailRepository.findAllByBatchId(TestConstants.ANY_BATCH_ID)).thenReturn(List.of(emailEntity));
        Mockito.when(conversionService.convert(emailEntity, Email.class)).thenReturn(email);

        // When
        final List<Email> result = underTest.getAll(TestConstants.ANY_BATCH_ID);

        //Then
        Assertions.assertEquals(emails, result);
        Mockito.verify(emailRepository).findAllByBatchId(TestConstants.ANY_BATCH_ID);
        Mockito.verify(conversionService).convert(emailEntity, Email.class);
    }

    @Test
    void shouldGetAllAndSumAllOccurrences() {
        // Given
        final EmailEntity firstEmailEntity = TestUtil.createEmailEntity(TestConstants.ANY_EMAIL, 2);
        final EmailEntity secondEmailEntity = TestUtil.createEmailEntity(TestConstants.ANY_EMAIL, 3);
        final EmailEntity anotherEmailEntity = TestUtil.createEmailEntity(TestConstants.ANY_OTHER_EMAIL, 1);

        final List<EmailEntity> emailEntityList = List.of(firstEmailEntity, secondEmailEntity, anotherEmailEntity);

        Mockito.when(emailRepository.findAll()).thenReturn(emailEntityList);

        // When
        final List<Email> result = underTest.getAll(null);

        // Then
        Mockito.verify(emailRepository).findAll();

        result.forEach(email -> {
            if (TestConstants.ANY_EMAIL.equals(email.getEmail())) {
                Assertions.assertEquals(5, email.getOccurrence());
            } else if (TestConstants.ANY_OTHER_EMAIL.equals(email.getEmail())) {
                Assertions.assertEquals(1, email.getOccurrence());
            } else {
                Assertions.fail();
            }
        });
    }

    @Test
    void shouldDeleteEmail() {
        // Given
        final List<EmailEntity> emailEntityList = List.of(Mockito.mock(EmailEntity.class));

        Mockito.when(emailRepository.findAllByEmail(TestConstants.ANY_EMAIL)).thenReturn(emailEntityList);

        // When
        underTest.delete(TestConstants.ANY_EMAIL);

        // Then
        Mockito.verify(emailRepository).findAllByEmail(TestConstants.ANY_EMAIL);
        Mockito.verify(emailRepository).deleteAll(emailEntityList);
    }

    @Test
    void shouldCreateAndGetSum() {
        // Given
        final EmailEntity existingEmailEntity = TestUtil.createEmailEntity(TestConstants.ANY_EMAIL, 2);
        final EmailEntity existingSavedEntity = TestUtil.createEmailEntity(TestConstants.ANY_EMAIL, 1);

        Mockito.when(emailRepository.findAllByEmail(TestConstants.ANY_EMAIL))
                .thenReturn(List.of(existingEmailEntity, existingSavedEntity));

        // When
        final Email result = underTest.create(TestConstants.ANY_EMAIL);

        // Then
        Mockito.verify(emailRepository).findAllByEmail(TestConstants.ANY_EMAIL);

        Assertions.assertEquals(3, result.getOccurrence());
        Assertions.assertEquals(TestConstants.ANY_EMAIL, result.getEmail());

        ArgumentCaptor<EmailEntity> savedEmailEntityArgumentCaptor = ArgumentCaptor.forClass(EmailEntity.class);
        Mockito.verify(emailRepository).save(savedEmailEntityArgumentCaptor.capture());
        final EmailEntity savedEmailEntity = savedEmailEntityArgumentCaptor.getValue();

        Assertions.assertEquals(existingSavedEntity.getEmail(), savedEmailEntity.getEmail());
        Assertions.assertEquals(existingSavedEntity.getOccurrence(), savedEmailEntity.getOccurrence());
    }

    @Test
    void shouldNotCreateEmailWithNotAllowedDomain() {
        try {
            // When
            underTest.create(TestConstants.ANY_NOT_ALLOWED_EMAIL);
        } catch (BadRequestException exception) {
            // Then
            Mockito.verify(emailRepository, Mockito.never()).findAllByEmail(Mockito.anyString());
            Mockito.verify(emailRepository, Mockito.never()).save(Mockito.any());
        }
    }

    @Test
    void shouldSumOccurrencesAndGetEmail() {
        // Given
        final EmailEntity firstOccurrence = TestUtil.createEmailEntity(TestConstants.ANY_EMAIL, 2);
        final EmailEntity secondOccurrence = TestUtil.createEmailEntity(TestConstants.ANY_EMAIL, 3);

        Mockito.when(emailRepository.findAllByEmail(TestConstants.ANY_EMAIL))
                .thenReturn(List.of(firstOccurrence, secondOccurrence));

        // When
        final Email result = underTest.get(TestConstants.ANY_EMAIL);

        // Then
        Assertions.assertEquals(5, result.getOccurrence());
        Assertions.assertEquals(TestConstants.ANY_EMAIL, result.getEmail());

        Mockito.verify(emailRepository).findAllByEmail(TestConstants.ANY_EMAIL);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenEmailDoesNotExist() {
        try {
            // When
            underTest.get(TestConstants.ANY_EMAIL);
        } catch (ResourceNotFoundException e) { // Then
            Mockito.verify(emailRepository).findAllByEmail(TestConstants.ANY_EMAIL);
        }
    }
}