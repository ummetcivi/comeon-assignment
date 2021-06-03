package com.ummetcivi.comeonassignment;

import com.ummetcivi.comeonassignment.data.entity.BatchEntity;
import com.ummetcivi.comeonassignment.data.entity.DatasetEntity;
import com.ummetcivi.comeonassignment.domain.Dataset;
import com.ummetcivi.comeonassignment.enums.BatchStatus;

import java.util.List;

public class TestUtil {

    public static DatasetEntity createDatasetEntity(List<String> emails) {
        return DatasetEntity.builder()
                .id(TestConstants.ANY_DATASET_ID)
                .batchId(TestConstants.ANY_BATCH_ID)
                .emails(emails)
                .urls(List.of(TestConstants.ANY_URL))
                .build();
    }

    public static BatchEntity createNonProcessedBatchEntity() {
        return BatchEntity.builder()
                .id(TestConstants.ANY_BATCH_ID)
                .status(BatchStatus.NOT_PROCESSED)
                .build();
    }

    public static Dataset createDataset(List<String> emails) {
        return Dataset.builder()
                .emails(emails)
                .build();
    }
}
