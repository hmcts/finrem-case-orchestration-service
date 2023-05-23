package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.intervener;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOneWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerDetails;

import static org.junit.Assert.assertEquals;

public class IntervenerOneToIntervenerDetailsMapperTest {

    IntervenerOneToIntervenerDetailsMapper intervenerOneToIntervenerDetailsMapper;

    @Before
    public void testSetUp() {
        intervenerOneToIntervenerDetailsMapper = new IntervenerOneToIntervenerDetailsMapper();
    }

    @Test
    public void mapToIntervenerDetailsTest() {
        IntervenerOneWrapper intervenerOneWrapper = IntervenerOneWrapper.builder()
            .intervener1Name("intervener one")
            .build();

        IntervenerDetails convertedDetails = intervenerOneToIntervenerDetailsMapper.mapToIntervenerDetails(intervenerOneWrapper);

        assertEquals(intervenerOneWrapper.getIntervener1Name(), convertedDetails.getIntervenerName());
    }
}
