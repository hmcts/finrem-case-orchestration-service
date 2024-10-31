package uk.gov.hmcts.reform.finrem.caseorchestration.helper;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MaskHelperTest {

    @Test
    void testMaskEmailInText() {
        String text = "Please contact user@example.com for support.";
        String email = "user@example.com";
        String expectedMaskedText = "Please contact ****@*********** for support.";

        String result = MaskHelper.maskEmail(text, email);

        assertEquals(expectedMaskedText, result);
    }

    @Test
    void testMaskEmailInTextWithMultipleOccurrences() {
        String text = "Please contact user@example.com or user@example.com for support.";
        String email = "user@example.com";
        String expectedMaskedText = "Please contact ****@*********** or ****@*********** for support.";

        String result = MaskHelper.maskEmail(text, email);

        assertEquals(expectedMaskedText, result);
    }

    @Test
    void testMaskEmailInTextWithNoEmailMatch() {
        String text = "Please contact user@example.com for support.";
        String email = "otheruser@example.com";

        String result = MaskHelper.maskEmail(text, email);

        assertEquals(text, result);
    }

    @Test
    void testMaskEmailWithNullText() {
        String email = "user@example.com";

        String result = MaskHelper.maskEmail(null, email);

        assertNull(result);
    }

    @Test
    void testMaskEmailDirectly() {
        String email = "user@example.com";
        String expectedMaskedEmail = "****@***********";

        String result = MaskHelper.maskEmail(email);

        assertEquals(expectedMaskedEmail, result);
    }

    @Test
    void testMaskEmailDirectlyWithNull() {
        String result = MaskHelper.maskEmail(null);

        assertEquals("", result);
    }
}
