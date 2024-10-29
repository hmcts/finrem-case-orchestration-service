package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;


import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class YesOrNoTest {

    @Test
    void testForValueTrue() {
        // When the input is true, it should return YES
        assertEquals(YesOrNo.YES, YesOrNo.forValue(true));
    }

    @Test
    void testForValueFalse() {
        // When the input is false, it should return NO
        assertEquals(YesOrNo.NO, YesOrNo.forValue(false));
    }
}
