package com.ummetcivi.comeonassignment.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Builder
@Getter
public class Batch {

    private final String id;
    private final Instant createdAt;
    private final Instant expiresAt;
}
