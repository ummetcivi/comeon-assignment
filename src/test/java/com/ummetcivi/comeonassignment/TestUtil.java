package com.ummetcivi.comeonassignment;

import com.ummetcivi.comeonassignment.data.entity.EmailEntity;

public class TestUtil {

    public static EmailEntity createEmailEntity(String email, long occurrence) {
        return EmailEntity.builder()
                .occurrence(occurrence)
                .email(email)
                .build();
    }
}
