package com.ummetcivi.comeonassignment.scheduler;

import com.ummetcivi.comeonassignment.accessor.RemoteDatasetAccessor;
import com.ummetcivi.comeonassignment.data.entity.BatchEntity;
import com.ummetcivi.comeonassignment.data.entity.DatasetEntity;
import com.ummetcivi.comeonassignment.data.entity.EmailEntity;
import com.ummetcivi.comeonassignment.data.jpa.BatchDatasetRepository;
import com.ummetcivi.comeonassignment.data.jpa.BatchRepository;
import com.ummetcivi.comeonassignment.data.jpa.EmailRepository;
import com.ummetcivi.comeonassignment.domain.Dataset;
import com.ummetcivi.comeonassignment.enums.BatchStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class ScheduledBatchProcessor {

    private final BatchRepository batchRepository;
    private final BatchDatasetRepository batchDatasetRepository;
    private final EmailRepository emailRepository;
    private final RemoteDatasetAccessor remoteDatasetAccessor;

    @Value("${email.allowedDomains}")
    private Set<String> allowedDomains;

    @Scheduled(cron = "*/1 * * * * *")
    public void processBatch() {
        final List<BatchEntity> expiredBatchList = batchRepository
                .findAllByExpiresAtBeforeAndStatus(Instant.now(), BatchStatus.NOT_PROCESSED);

        if (CollectionUtils.isEmpty(expiredBatchList)) {
            return;
        }

        expiredBatchList.forEach(batchEntity -> {
            log.info("Batch '{}' has been expired. Creating summary.", batchEntity.getId());

            try {
                batchEntity.setStatus(BatchStatus.IN_PROGRESS);
                batchRepository.save(batchEntity);

                final List<DatasetEntity> datasetEntityList = batchDatasetRepository
                        .findAllByBatchId(batchEntity.getId());

                processAndSave(batchEntity.getId(), datasetEntityList);

                batchDatasetRepository.deleteAll(datasetEntityList);

                batchEntity.setStatus(BatchStatus.PROCESSED);
                batchRepository.save(batchEntity);
            } catch (OptimisticLockingFailureException e) {
                log.warn("BatchEntity has been changed by someone else, skipping.");
            }
        });
    }

    private void processAndSave(final String batchId, final List<DatasetEntity> datasetEntityList) {
        if (CollectionUtils.isEmpty(datasetEntityList)) {
            return;
        }

        final List<Dataset> datasetList = datasetEntityList.stream()
                .map(datasetEntity -> Dataset.builder()
                        .urls(datasetEntity.getUrls())
                        .emails(datasetEntity.getEmails())
                        .build())
                .collect(Collectors.toList());

        final List<EmailEntity> emailOccurrences = getAllEmails(batchId, datasetList);

        if (!CollectionUtils.isEmpty(emailOccurrences)) {
            emailRepository.saveAll(emailOccurrences);
        }
    }

    private List<EmailEntity> getAllEmails(final String batchId, final List<Dataset> dataset) {
        return dataset.stream()
                .map(this::getEmailsFromDataset)
                .filter(emails -> !CollectionUtils.isEmpty(emails))
                .flatMap(Collection::stream)
                .filter(StringUtils::hasText)
                .filter(email -> allowedDomains.stream().anyMatch(email::endsWith))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .map(occurrenceMap -> EmailEntity.builder()
                        .batchId(batchId)
                        .email(occurrenceMap.getKey())
                        .occurrence(occurrenceMap.getValue())
                        .id(UUID.randomUUID().toString())
                        .build())
                .collect(Collectors.toList());
    }

    private List<String> getEmailsFromDataset(final Dataset dataset) {
        if (CollectionUtils.isEmpty(dataset.getUrls())) {
            return dataset.getEmails() != null ? dataset.getEmails() : List.of();
        }

        final List<String> fromUrls = dataset.getUrls().stream()
                .map(remoteDatasetAccessor::fetchDataset)
                .filter(Objects::nonNull)
                .map(this::getEmailsFromDataset)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(dataset.getEmails())) {
            fromUrls.addAll(dataset.getEmails());
        }

        return fromUrls;
    }
}
