package uk.gov.hmcts.reform.finrem.caseorchestration.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailUtilsTest {

    @ParameterizedTest
    @CsvSource(value = {
        "abc@abc.com,abc@abc.com,false",
        "abc@abc.com,abc@ABC.com,false",
        "ABC@abc.com,abc@abc.com,false",
        " abc@abc.com ,abc@ABC.com ,false",
        "def@abc.com,abc@abc.com,true",
        "NULL,abc@abc.com,true",
        "abc@abc.com,,true",
        "abc@abc.com,NULL,true",
        "NULL,NULL,false",
        ",NULL,false",
        "NULL,,false",
        ",,false"
    }, ignoreLeadingAndTrailingWhitespace = false, nullValues = "NULL")
    void testAreEmailsDifferent(String email1, String email2, boolean expected) {
        assertThat(EmailUtils.areEmailsDifferent(email1, email2)).isEqualTo(expected);

    }

    @Nested
    @DisplayName("Valid email addresses")
    class ValidEmails {

        @Test
        void shouldAcceptStandardEmail() {
            assertTrue(EmailUtils.isValidEmailAddress("john.doe@example.com"));
        }

        @Test
        void shouldAcceptEmailWithPlusTag() {
            assertTrue(EmailUtils.isValidEmailAddress("john+filter@sub.example.co.uk"));
        }

        @Test
        void shouldAcceptUpperCaseEmail() {
            assertTrue(EmailUtils.isValidEmailAddress("JOHN.DOE@EXAMPLE.COM"));
        }
    }

    @Nested
    @DisplayName("Invalid email addresses")
    class InvalidEmails {

        @Test
        void shouldRejectNullEmail() {
            assertFalse(EmailUtils.isValidEmailAddress(null));
        }

        @Test
        void shouldRejectBlankEmail() {
            assertFalse(EmailUtils.isValidEmailAddress(""));
        }

        @Test
        void shouldRejectWhitespaceOnly() {
            assertFalse(EmailUtils.isValidEmailAddress("   "));
        }

        @Test
        void shouldRejectEmailWithoutTld() {
            assertFalse(EmailUtils.isValidEmailAddress("user@domain"));
        }

        @Test
        void shouldRejectEmailWithConsecutiveDots() {
            assertFalse(EmailUtils.isValidEmailAddress("user..name@domain.com"));
        }

        @Test
        void shouldRejectEmailWithShortTld() {
            assertFalse(EmailUtils.isValidEmailAddress("user@domain.c"));
        }

        @Test
        void shouldRejectEmailWithoutAtSymbol() {
            assertFalse(EmailUtils.isValidEmailAddress("userdomain.com"));
        }

        @Test
        void shouldRejectLocalhostDomain() {
            assertFalse(EmailUtils.isValidEmailAddress("admin@localhost"));
        }

        @Test
        void shouldRejectCopiedAndPastedFromOutlook() {
            assertFalse(EmailUtils.isValidEmailAddress("Claire Mumford<claire.mumford@yahoo.com>"));
        }
    }
}
