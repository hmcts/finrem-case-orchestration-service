package uk.gov.hmcts.reform.finrem.caseorchestration.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ApplicationType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ApplicationType.CONTESTED;

public class ApplicationTypeTest {

    @Test
    public void checkAllApplicationTypes() {
        assertEquals("consented", CONSENTED.toString());
        assertEquals("contested", CONTESTED.toString());
    }

    @Test
    public void covertAllApplicationTypes() {
        assertEquals(CONSENTED, ApplicationType.from("consented"));
        assertEquals(CONTESTED, ApplicationType.from("contested"));

        assertEquals(CONSENTED, ApplicationType.from("CONSENTED"));
        assertEquals(CONTESTED, ApplicationType.from("CONTESTED"));
    }

    @Test
    public void checkToString() {
        assertEquals("consented", CONSENTED.toString());
        assertEquals("contested", CONTESTED.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwIllegalArgumentException() {
        ApplicationType.from("abcd");
    }
}