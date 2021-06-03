package com.ummetcivi.comeonassignment.accessor;

import com.ummetcivi.comeonassignment.domain.Dataset;
import com.ummetcivi.comeonassignment.dto.DatasetDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionService;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class RemoteDatasetAccessor {

    private final RestTemplate restTemplate;
    private final ConversionService conversionService;

    @Retryable(value = Exception.class, backoff = @Backoff(delayExpression = "${accessor.retry.maxDelay:100}"),
            maxAttemptsExpression = "${accessor.retry.maxRetry:3}", recover = "emptyDataset")
    public Dataset fetchDataset(final String url) {
        final DatasetDto datasetDto = restTemplate.getForObject(url, DatasetDto.class);
        return conversionService.convert(datasetDto, Dataset.class);
    }

    @Recover
    protected Dataset fallback(final Exception e, final String url) {
        log.error("Could not access to {}.", url, e);
        return null;
    }
}
