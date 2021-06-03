package com.ummetcivi.comeonassignment.service;

import com.ummetcivi.comeonassignment.data.entity.BatchEntity;
import com.ummetcivi.comeonassignment.data.entity.DatasetEntity;
import com.ummetcivi.comeonassignment.data.entity.ProcessedBatchEntity;
import com.ummetcivi.comeonassignment.data.jpa.BatchDatasetRepository;
import com.ummetcivi.comeonassignment.data.jpa.BatchRepository;
import com.ummetcivi.comeonassignment.data.jpa.ProcessedBatchRepository;
import com.ummetcivi.comeonassignment.domain.Batch;
import com.ummetcivi.comeonassignment.domain.Dataset;
import com.ummetcivi.comeonassignment.domain.EmailOccurrence;
import com.ummetcivi.comeonassignment.enums.BatchStatus;
import com.ummetcivi.comeonassignment.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final BatchRepository batchRepository;
    private final BatchDatasetRepository batchDatasetRepository;
    private final ProcessedBatchRepository processedBatchRepository;
    private final ConversionService conversionService;

    @Value("${batch.windowInMinutes}")
    private int batchWindowInMinutes;

    public Batch importEmails(final Dataset dataset) {
        final BatchEntity currentBatch = getCurrentOrCreateBatch();

        batchDatasetRepository.save(DatasetEntity.builder()
                .batchId(currentBatch.getId())
                .emails(dataset.getEmails())
                .urls(dataset.getUrls())
                .id(generateId())
                .build());

        return conversionService.convert(currentBatch, Batch.class);
    }

    public List<EmailOccurrence> getAll(final String id) {
        if (StringUtils.hasText(id)) {
            final ProcessedBatchEntity processedBatchEntity = processedBatchRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Summary not found."));
            return processedBatchEntity.getEmails();
        }

        return processedBatchRepository.findAll().stream()
                .map(ProcessedBatchEntity::getEmails)
                .flatMap(Collection::stream)
                .collect(Collectors
                        .groupingBy(EmailOccurrence::getEmail, Collectors.summingLong(EmailOccurrence::getOccurrence)))
                .entrySet()
                .stream()
                .map(occurrenceEntry -> EmailOccurrence.builder()
                        .occurrence(occurrenceEntry.getValue())
                        .email(occurrenceEntry.getKey())
                        .build())
                .collect(Collectors.toList());
    }

    public EmailOccurrence getBy(String emailAddress) {
        final List<ProcessedBatchEntity> processedBatchEntities = processedBatchRepository.findAllByEmail(emailAddress);

        if(CollectionUtils.isEmpty(processedBatchEntities)){
            throw new ResourceNotFoundException("Email not found.");
        }

        final long occurrence = processedBatchEntities.stream()
                .map(ProcessedBatchEntity::getEmails)
                .flatMap(Collection::stream)
                .mapToLong(EmailOccurrence::getOccurrence)
                .sum();

        return EmailOccurrence.builder()
                .email(emailAddress)
                .occurrence(occurrence)
                .build();
    }

    private BatchEntity getCurrentOrCreateBatch() {
        final Instant now = Instant.now();
        return batchRepository.findByExpiresAtAfter(now).orElseGet(() -> batchRepository.save(BatchEntity.builder()
                .id(generateId())
                .status(BatchStatus.NOT_PROCESSED)
                .createdAt(now)
                .expiresAt(now.plus(batchWindowInMinutes, ChronoUnit.MINUTES))
                .build()));
    }

    private static String generateId() {
        return UUID.randomUUID().toString();
    }
}
