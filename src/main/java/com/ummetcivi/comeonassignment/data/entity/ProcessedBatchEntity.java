package com.ummetcivi.comeonassignment.data.entity;

import com.ummetcivi.comeonassignment.domain.EmailOccurrence;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
@Builder
@Getter
public class ProcessedBatchEntity {

    @Id
    private final String id;
    private final List<EmailOccurrence> emails;
}
