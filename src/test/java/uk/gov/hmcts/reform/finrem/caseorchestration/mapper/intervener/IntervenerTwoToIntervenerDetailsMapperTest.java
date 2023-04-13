package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.intervener;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOneWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwoWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerDetails;

import static org.junit.Assert.assertEquals;

public class IntervenerTwoToIntervenerDetailsMapperTest {

    IntervenerTwoToIntervenerDetailsMapper intervenerTwoToIntervenerDetailsMapper;

    @Before
    public void testSetUp() {
        intervenerTwoToIntervenerDetailsMapper = new IntervenerTwoToIntervenerDetailsMapper();
    }

    @Test
    public void mapToIntervenerDetailsTest() {
        IntervenerTwoWrapper intervenerTwoWrapper = IntervenerTwoWrapper.builder()
            .intervener2Name("intervener two")
            .build();

        IntervenerDetails convertedDetails = intervenerTwoToIntervenerDetailsMapper.mapToIntervenerDetails(intervenerTwoWrapper);

        assertEquals(intervenerTwoWrapper.getIntervener2Name(), convertedDetails.getIntervenerName());
    }
}
