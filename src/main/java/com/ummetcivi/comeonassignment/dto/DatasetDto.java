package com.ummetcivi.comeonassignment.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@JacksonXmlRootElement(localName = "dataset")
@RequiredArgsConstructor
@Getter
public class DatasetDto {

    @JacksonXmlElementWrapper(localName = "emails")
    private final List<EmailDto> emails;
    @JacksonXmlElementWrapper(localName = "resources")
    private final List<ResourceDto> resources;
}
