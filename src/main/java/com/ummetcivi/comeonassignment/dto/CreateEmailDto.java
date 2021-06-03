package com.ummetcivi.comeonassignment.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CreateEmailDto {

    private final String email;
}
