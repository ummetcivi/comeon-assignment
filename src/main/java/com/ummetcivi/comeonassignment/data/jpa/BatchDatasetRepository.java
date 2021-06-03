package com.ummetcivi.comeonassignment.data.jpa;

import com.ummetcivi.comeonassignment.data.entity.DatasetEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BatchDatasetRepository extends MongoRepository<DatasetEntity, String> {

    List<DatasetEntity> findAllByBatchId(String batchId);
}
