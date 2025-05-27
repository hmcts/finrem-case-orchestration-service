package uk.gov.hmcts.reform.finrem.caseorchestration.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ApplicationType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ApplicationType.CONTESTED;

class ApplicationTypeTest {

    @Test
    void checkAllApplicationTypes() {
        assertEquals("consented", CONSENTED.toString());
        assertEquals("contested", CONTESTED.toString());
    }

    @Test
    void covertAllApplicationTypes() {
        assertEquals(CONSENTED, ApplicationType.from("consented"));
        assertEquals(CONTESTED, ApplicationType.from("contested"));

        assertEquals(CONSENTED, ApplicationType.from("CONSENTED"));
        assertEquals(CONTESTED, ApplicationType.from("CONTESTED"));
    }

    @Test
    void throwIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> ApplicationType.from("abcd"));
    }

    @Test
    void testFromCaseType() {
        assertEquals(CONSENTED, ApplicationType.from(CaseType.CONSENTED));
        assertEquals(CONTESTED, ApplicationType.from(CaseType.CONTESTED));
    }

    @Test
    void testFromCaseTypeThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> ApplicationType.from(CaseType.UNKNOWN));
    }
}
