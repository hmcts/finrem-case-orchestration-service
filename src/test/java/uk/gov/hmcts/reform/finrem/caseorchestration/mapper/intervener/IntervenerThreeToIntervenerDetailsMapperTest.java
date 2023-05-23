package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.intervener;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThreeWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerDetails;

import static org.junit.Assert.assertEquals;

public class IntervenerThreeToIntervenerDetailsMapperTest {

    IntervenerThreeToIntervenerDetailsMapper intervenerThreeToIntervenerDetailsMapper;

    @Before
    public void testSetUp() {
        intervenerThreeToIntervenerDetailsMapper = new IntervenerThreeToIntervenerDetailsMapper();
    }

    @Test
    public void mapToIntervenerDetailsTest() {
        IntervenerThreeWrapper intervenerThreeWrapper = IntervenerThreeWrapper.builder()
            .intervener3Name("intervener Three")
            .build();

        IntervenerDetails convertedDetails = intervenerThreeToIntervenerDetailsMapper.mapToIntervenerDetails(intervenerThreeWrapper);

        assertEquals(intervenerThreeWrapper.getIntervener3Name(), convertedDetails.getIntervenerName());
    }
}
