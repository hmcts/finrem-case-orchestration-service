package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.intervener;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFourWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerDetails;

import static org.junit.Assert.assertEquals;

public class IntervenerFourToIntervenerDetailsMapperTest {

    IntervenerFourToIntervenerDetailsMapper intervenerFourToIntervenerDetailsMapper;

    @Before
    public void testSetUp() {
        intervenerFourToIntervenerDetailsMapper = new IntervenerFourToIntervenerDetailsMapper();
    }

    @Test
    public void mapToIntervenerDetailsTest() {
        IntervenerFourWrapper intervenerFourWrapper = IntervenerFourWrapper.builder()
            .intervener4Name("intervener Four")
            .build();

        IntervenerDetails convertedDetails = intervenerFourToIntervenerDetailsMapper.mapToIntervenerDetails(intervenerFourWrapper);

        assertEquals(intervenerFourWrapper.getIntervener4Name(), convertedDetails.getIntervenerName());
    }
}
