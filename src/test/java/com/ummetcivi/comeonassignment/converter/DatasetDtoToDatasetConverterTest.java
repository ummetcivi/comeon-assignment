package com.ummetcivi.comeonassignment.converter;

import com.ummetcivi.comeonassignment.TestConstants;
import com.ummetcivi.comeonassignment.domain.Dataset;
import com.ummetcivi.comeonassignment.dto.DatasetDto;
import com.ummetcivi.comeonassignment.dto.EmailDto;
import com.ummetcivi.comeonassignment.dto.ResourceDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DatasetDtoToDatasetConverterTest {

    private DatasetDtoToDatasetConverter underTest;

    @BeforeEach
    void setUp() {
        underTest = new DatasetDtoToDatasetConverter();
    }

    @Test
    void shouldConvert() {
        // Given
        final DatasetDto dto = Mockito.mock(DatasetDto.class);

        Mockito.when(dto.getEmails()).thenReturn(List.of(new EmailDto(TestConstants.ANY_EMAIL)));
        Mockito.when(dto.getResources()).thenReturn(List.of(new ResourceDto(TestConstants.ANY_URL)));

        // When
        final Dataset result = underTest.convert(dto);

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertEquals(List.of(TestConstants.ANY_EMAIL), result.getEmails());
        Assertions.assertEquals(List.of(TestConstants.ANY_URL), result.getUrls());
    }
}