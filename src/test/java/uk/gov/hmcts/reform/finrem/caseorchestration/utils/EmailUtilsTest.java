package uk.gov.hmcts.reform.finrem.caseorchestration.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailUtilsTest {

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
