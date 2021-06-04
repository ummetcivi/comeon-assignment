package com.ummetcivi.comeonassignment.data.entity;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Builder
@Getter
public class EmailEntity {

    @Id
    private final String id;
    private final String batchId;
    private final String email;
    private final long occurrence;
}
