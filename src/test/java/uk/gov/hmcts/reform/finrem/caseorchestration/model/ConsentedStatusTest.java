package uk.gov.hmcts.reform.finrem.caseorchestration.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ConsentedStatus.APPLICATION_ISSUED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ConsentedStatus.APPLICATION_SUBMITTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ConsentedStatus.AWAITING_HWF_DECISION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ConsentedStatus.PREPARE_FOR_HEARING;

public class ConsentedStatusTest {
    @Test
    public void checkAllStatusValues() {
        assertEquals("awaitingHWFDecision", AWAITING_HWF_DECISION.toString());
        assertEquals("applicationSubmitted", APPLICATION_SUBMITTED.toString());
        assertEquals("applicationIssued", APPLICATION_ISSUED.toString());
        assertEquals("prepareForHearing", PREPARE_FOR_HEARING.toString());
    }
}
