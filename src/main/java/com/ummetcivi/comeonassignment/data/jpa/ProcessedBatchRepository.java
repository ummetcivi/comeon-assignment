package com.ummetcivi.comeonassignment.data.jpa;

import com.ummetcivi.comeonassignment.data.entity.ProcessedBatchEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcessedBatchRepository extends MongoRepository<ProcessedBatchEntity, String> {

    @Query(value = "{'emails.email': '?0'}", fields = "{'emails.$': 1}")
    List<ProcessedBatchEntity> findAllByEmail(String email);
}
