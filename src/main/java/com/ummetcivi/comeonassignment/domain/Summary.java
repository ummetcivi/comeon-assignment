package com.ummetcivi.comeonassignment.domain;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class Summary {

    private final String batchId;
    private final Map<String, Long> emails;
}
