package com.ummetcivi.comeonassignment.data.entity;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
@Builder
@Getter
public class DatasetEntity {

    @Id
    private final String id;
    private final String batchId;
    private final List<String> emails;
    private final List<String> urls;
}
