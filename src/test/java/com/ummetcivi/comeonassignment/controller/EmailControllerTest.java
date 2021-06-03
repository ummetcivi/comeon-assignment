package com.ummetcivi.comeonassignment.controller;

import com.ummetcivi.comeonassignment.TestConstants;
import com.ummetcivi.comeonassignment.domain.Batch;
import com.ummetcivi.comeonassignment.domain.Dataset;
import com.ummetcivi.comeonassignment.domain.EmailOccurrence;
import com.ummetcivi.comeonassignment.dto.DatasetDto;
import com.ummetcivi.comeonassignment.service.EmailService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

class EmailControllerTest {

    @Mock
    private EmailService emailService;
    @Mock
    private ConversionService conversionService;

    private EmailController underTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        underTest = new EmailController(emailService, conversionService);
    }

    @Test
    void shouldImportEmails() {
        // Given
        final DatasetDto dto = Mockito.mock(DatasetDto.class);
        final Dataset dataset = Mockito.mock(Dataset.class);
        final Batch batch = Mockito.mock(Batch.class);

        Mockito.when(conversionService.convert(dto, Dataset.class)).thenReturn(dataset);
        Mockito.when(emailService.importEmails(dataset)).thenReturn(batch);

        // When
        final ResponseEntity<Batch> response = underTest.importEmails(dto);

        // Then
        Mockito.verify(conversionService).convert(dto, Dataset.class);
        Mockito.verify(emailService).importEmails(dataset);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(batch, response.getBody());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldGetAllEmails() {
        // Given
        final List<EmailOccurrence> emailOccurrences = Mockito.mock(List.class);

        Mockito.when(emailService.getAll(null)).thenReturn(emailOccurrences);

        // When
        final ResponseEntity<List<EmailOccurrence>> response = underTest.getAll(null);

        // Then
        Mockito.verify(emailService).getAll(null);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(emailOccurrences, response.getBody());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldGetEmailsByBatchId() {
        // Given
        final List<EmailOccurrence> emailOccurrences = Mockito.mock(List.class);

        Mockito.when(emailService.getAll(TestConstants.ANY_BATCH_ID)).thenReturn(emailOccurrences);

        // When
        final ResponseEntity<List<EmailOccurrence>> response = underTest.getAll(TestConstants.ANY_BATCH_ID);

        // Then
        Mockito.verify(emailService).getAll(TestConstants.ANY_BATCH_ID);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(emailOccurrences, response.getBody());
    }

    @Test
    void shouldGetEmail() {
        // Given
        final EmailOccurrence emailOccurrence = Mockito.mock(EmailOccurrence.class);

        Mockito.when(emailService.getBy(TestConstants.ANY_EMAIL)).thenReturn(emailOccurrence);

        // When
        final ResponseEntity<EmailOccurrence> response = underTest.getEmail(TestConstants.ANY_EMAIL);

        // Then
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(emailOccurrence, response.getBody());
    }
}