package com.ummetcivi.comeonassignment.service;

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
import com.ummetcivi.comeonassignment.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final BatchRepository batchRepository;
    private final BatchDatasetRepository batchDatasetRepository;
    private final EmailRepository emailRepository;
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

    public List<Email> getAll(final String id) {
        if (StringUtils.hasText(id)) {
            return emailRepository.findAllByBatchId(id).stream()
                    .map(emailEntity -> conversionService.convert(emailEntity, Email.class))
                    .collect(Collectors.toList());
        }

        return emailRepository.findAll().stream()
                .collect(Collectors.groupingBy(EmailEntity::getEmail,
                        Collectors.summingLong(EmailEntity::getOccurrence)))
                .entrySet().stream()
                .map(occurrenceEntry -> Email.builder()
                        .occurrence(occurrenceEntry.getValue())
                        .email(occurrenceEntry.getKey())
                        .build())
                .collect(Collectors.toList());
    }

    public Email get(String emailAddress) {
        final List<EmailEntity> emailEntityList = emailRepository
                .findAllByEmail(emailAddress);

        if (CollectionUtils.isEmpty(emailEntityList)) {
            throw new ResourceNotFoundException("Email not found.");
        }

        final long occurrence = emailEntityList.stream()
                .mapToLong(EmailEntity::getOccurrence)
                .sum();

        return Email.builder()
                .email(emailAddress)
                .occurrence(occurrence)
                .build();
    }

    public void delete(String email) {
        final List<EmailEntity> emails = emailRepository.findAllByEmail(email);

        if (!CollectionUtils.isEmpty(emails)) {
            emailRepository.deleteAll(emails);
        }
    }

    public Email create(String email) {
        emailRepository.save(EmailEntity.builder()
                .id(generateId())
                .occurrence(1)
                .email(email)
                .build());

        return get(email);
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
