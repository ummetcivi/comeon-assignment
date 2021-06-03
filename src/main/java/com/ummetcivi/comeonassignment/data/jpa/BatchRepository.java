package com.ummetcivi.comeonassignment.data.jpa;

import com.ummetcivi.comeonassignment.data.entity.BatchEntity;
import com.ummetcivi.comeonassignment.enums.BatchStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface BatchRepository extends MongoRepository<BatchEntity, String> {

    Optional<BatchEntity> findByExpiresAtAfter(Instant now);

    List<BatchEntity> findAllByExpiresAtBeforeAndStatus(Instant now, BatchStatus status);
}
