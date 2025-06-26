package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HearingTypeTest {

    @Test
    void shouldReturnHearingType() {
        assertEquals(HearingType.FDA, HearingTypeDirection.FDA.toHearingType());
        assertEquals(HearingType.FDR, HearingTypeDirection.FDR.toHearingType());
        assertEquals(HearingType.FH, HearingTypeDirection.FH.toHearingType());
        assertEquals(HearingType.DIR, HearingTypeDirection.DIR.toHearingType());
    }

}
