package com.ummetcivi.comeonassignment.domain;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class EmailOccurrence {

    private final String email;
    private final long occurrence;
}
