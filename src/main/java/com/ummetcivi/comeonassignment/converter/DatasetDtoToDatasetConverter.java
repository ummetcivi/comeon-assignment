package com.ummetcivi.comeonassignment.converter;

import com.ummetcivi.comeonassignment.domain.Dataset;
import com.ummetcivi.comeonassignment.dto.DatasetDto;
import com.ummetcivi.comeonassignment.dto.EmailDto;
import com.ummetcivi.comeonassignment.dto.ResourceDto;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DatasetDtoToDatasetConverter implements Converter<DatasetDto, Dataset> {

    @Override
    public Dataset convert(DatasetDto source) {
        final List<String> emails = source.getEmails().stream()
                .map(EmailDto::getEmail)
                .collect(Collectors.toList());
        final List<String> urls = source.getResources().stream()
                .map(ResourceDto::getUrl)
                .collect(Collectors.toList());

        return Dataset.builder()
                .emails(emails)
                .urls(urls)
                .build();
    }
}
