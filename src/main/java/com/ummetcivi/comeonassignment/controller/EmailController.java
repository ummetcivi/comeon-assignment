package com.ummetcivi.comeonassignment.controller;

import com.ummetcivi.comeonassignment.domain.Batch;
import com.ummetcivi.comeonassignment.domain.Dataset;
import com.ummetcivi.comeonassignment.domain.Email;
import com.ummetcivi.comeonassignment.dto.CreateEmailDto;
import com.ummetcivi.comeonassignment.dto.DatasetDto;
import com.ummetcivi.comeonassignment.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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

    @GetMapping
    public ResponseEntity<List<Email>> getAll(@RequestParam(required = false) final String batchId) {
        return ResponseEntity.ok(emailService.getAll(batchId));
    }

    @GetMapping("/{email}")
    public ResponseEntity<Email> getEmail(@PathVariable final String email) {
        return ResponseEntity.ok(emailService.getBy(email));
    }

    @DeleteMapping("/{email}")
    public ResponseEntity<Void> delete(@PathVariable final String email) {
        emailService.delete(email);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<Email> create(@RequestBody final CreateEmailDto createEmailDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(emailService.create(createEmailDto.getEmail()));
    }
}
