package com.ummetcivi.comeonassignment.data.entity;

import com.ummetcivi.comeonassignment.enums.BatchStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document
@Builder
@Getter
public class BatchEntity {

    @Id
    private final String id;
    private final Instant createdAt;
    @Indexed
    private final Instant expiresAt;

    @Setter
    private BatchStatus status;

    @Version
    @Setter
    private Long version;
}
