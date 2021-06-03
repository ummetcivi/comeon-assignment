package com.ummetcivi.comeonassignment.domain;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class Dataset {

    private final List<String> emails;
    private final List<String> urls;
}
