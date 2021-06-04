package com.ummetcivi.comeonassignment.data.jpa;

import com.ummetcivi.comeonassignment.data.entity.EmailEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmailRepository extends MongoRepository<EmailEntity, String> {

    List<EmailEntity> findAllByEmail(String email);

    List<EmailEntity> findAllByBatchId(String batchId);
}
