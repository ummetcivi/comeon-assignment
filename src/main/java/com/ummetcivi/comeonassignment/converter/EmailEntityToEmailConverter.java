package com.ummetcivi.comeonassignment.converter;

import com.ummetcivi.comeonassignment.data.entity.EmailEntity;
import com.ummetcivi.comeonassignment.domain.Email;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class EmailEntityToEmailConverter implements Converter<EmailEntity, Email> {

    @Override
    public Email convert(EmailEntity source) {
        return Email.builder()
                .occurrence(source.getOccurrence())
                .email(source.getEmail())
                .build();
    }
}
