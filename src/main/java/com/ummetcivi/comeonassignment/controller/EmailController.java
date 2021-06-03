package com.ummetcivi.comeonassignment.controller;

import com.ummetcivi.comeonassignment.domain.Batch;
import com.ummetcivi.comeonassignment.domain.Dataset;
import com.ummetcivi.comeonassignment.domain.EmailOccurrence;
import com.ummetcivi.comeonassignment.dto.DatasetDto;
import com.ummetcivi.comeonassignment.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/emails")
public class EmailController {

    private final EmailService emailService;
    private final ConversionService conversionService;

    @PostMapping(path = "/import", consumes = MediaType.APPLICATION_XML_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Batch> importEmails(@RequestBody final DatasetDto dto) {
        final Dataset dataset = conversionService.convert(dto, Dataset.class);
        return ResponseEntity.ok(emailService.importEmails(dataset));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<EmailOccurrence>> getAll(@RequestParam(required = false) final String batchId) {
        return ResponseEntity.ok(emailService.getAll(batchId));
    }

    @GetMapping("/{email}")
    public ResponseEntity<EmailOccurrence> getEmail(@PathVariable final String email) {
        return ResponseEntity.ok(emailService.getBy(email));
    }
}
