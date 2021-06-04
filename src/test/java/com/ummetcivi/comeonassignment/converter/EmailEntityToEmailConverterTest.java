package com.ummetcivi.comeonassignment.converter;

import com.ummetcivi.comeonassignment.TestConstants;
import com.ummetcivi.comeonassignment.data.entity.EmailEntity;
import com.ummetcivi.comeonassignment.domain.Email;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class EmailEntityToEmailConverterTest {

    private EmailEntityToEmailConverter underTest;

    @BeforeEach
    void setUp() {
        underTest = new EmailEntityToEmailConverter();
    }

    @Test
    void shouldConvert() {
        // Given
        final EmailEntity emailEntity = Mockito.mock(EmailEntity.class);

        Mockito.when(emailEntity.getEmail()).thenReturn(TestConstants.ANY_EMAIL);
        Mockito.when(emailEntity.getOccurrence()).thenReturn(TestConstants.ANY_OCCURRENCE);

        // When
        final Email result = underTest.convert(emailEntity);

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertEquals(TestConstants.ANY_EMAIL, result.getEmail());
        Assertions.assertEquals(TestConstants.ANY_OCCURRENCE, result.getOccurrence());
    }
}