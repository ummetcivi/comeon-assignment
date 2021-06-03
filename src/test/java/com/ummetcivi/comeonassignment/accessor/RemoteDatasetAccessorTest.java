package com.ummetcivi.comeonassignment.accessor;

import com.ummetcivi.comeonassignment.TestConstants;
import com.ummetcivi.comeonassignment.domain.Dataset;
import com.ummetcivi.comeonassignment.dto.DatasetDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.core.convert.ConversionService;
import org.springframework.web.client.RestTemplate;

class RemoteDatasetAccessorTest {

    @Mock
    private RestTemplate restTemplate;
    @Mock
    private ConversionService conversionService;

    private RemoteDatasetAccessor underTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        underTest = new RemoteDatasetAccessor(restTemplate, conversionService);
    }

    @Test
    void shouldFetchDataset() {
        // Given
        final Dataset dataset = Mockito.mock(Dataset.class);
        final DatasetDto dto = Mockito.mock(DatasetDto.class);

        Mockito.when(restTemplate.getForObject(TestConstants.ANY_URL, DatasetDto.class)).thenReturn(dto);
        Mockito.when(conversionService.convert(dto, Dataset.class)).thenReturn(dataset);

        // When
        final Dataset result = underTest.fetchDataset(TestConstants.ANY_URL);

        // Then
        Mockito.verify(restTemplate).getForObject(TestConstants.ANY_URL, DatasetDto.class);
        Mockito.verify(conversionService).convert(dto, Dataset.class);

        Assertions.assertEquals(dataset, result);
    }

    @Test
    void shouldReturnNullDatasetAsFallback() {
        // Given
        final Exception exception = Mockito.mock(Exception.class);

        // When
        final Dataset dataset = underTest.fallback(exception, TestConstants.ANY_URL);

        // Then
        Assertions.assertNull(dataset);
    }
}