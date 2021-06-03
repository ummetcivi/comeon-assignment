package com.ummetcivi.comeonassignment.converter;

import com.ummetcivi.comeonassignment.data.entity.BatchEntity;
import com.ummetcivi.comeonassignment.domain.Batch;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class BatchEntityToBatchConverter implements Converter<BatchEntity, Batch> {

    @Override
    public Batch convert(BatchEntity source) {
        return Batch.builder()
                .expiresAt(source.getExpiresAt())
                .createdAt(source.getCreatedAt())
                .id(source.getId())
                .build();
    }
}
