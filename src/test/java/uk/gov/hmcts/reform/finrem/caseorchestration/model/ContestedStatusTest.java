package uk.gov.hmcts.reform.finrem.caseorchestration.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ContestedStatus.APPLICATION_ISSUED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ContestedStatus.APPLICATION_SUBMITTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ContestedStatus.GATE_KEEPING_AND_ALLOCATION;

public class ContestedStatusTest {
    @Test
    public void checkAllStatusValues() {
        assertEquals("applicationSubmitted", APPLICATION_SUBMITTED.toString());
        assertEquals("applicationIssued", APPLICATION_ISSUED.toString());
        assertEquals("gateKeepingAndAllocation", GATE_KEEPING_AND_ALLOCATION.toString());
    }
}
