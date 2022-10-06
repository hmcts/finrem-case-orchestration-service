package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BIRMINGHAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BIRMINGHAM_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BRISTOLFRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BRISTOL_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HSYORKSHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HSYORKSHIRE_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LANCASHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LANCASHIRE_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIDLANDS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIDLANDS_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHEAST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHEAST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHWALES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHWEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHWEST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTH_WALES_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REGION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHEAST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHEAST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHWEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHWEST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.THAMESVALLEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.THAMESVALLEY_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WALES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WALES_FRC_LIST;

public class ConsentedCaseHearingFunctionsTest {

    public static final String BRISTOL_1 = "bristol_1";
    public static final String BIRMINGHAM_1 = "birmingham_1";
    public static final String THAMES_1 = "thames_1";
    public static final String LANCASHIRE_1 = "lancashire_1";
    public static final String HS_YORKSHIRE_1 = "hs_yorkshire_1";
    public static final String NORTHWALES_1 = "northwales_1";
    private Map<String, Object> caseData;

    @Before
    public void setUp() {
        caseData = new HashMap<>();
    }

    @Test
    public void shouldReturnCorrectSouthWestList() {
        caseData.put(REGION, SOUTHWEST);
        caseData.put(SOUTHWEST_FRC_LIST, BRISTOLFRC);
        caseData.put(BRISTOL_COURTLIST, BRISTOL_1);

        assertThat(ConsentedCaseHearingFunctions.getSelectedCourt(caseData), is(BRISTOL_1));
    }

    @Test
    public void shouldReturnCorrectMidlandsList() {
        caseData.put(REGION, MIDLANDS);
        caseData.put(MIDLANDS_FRC_LIST, BIRMINGHAM);
        caseData.put(BIRMINGHAM_COURTLIST, BIRMINGHAM_1);

        assertThat(ConsentedCaseHearingFunctions.getSelectedCourt(caseData), is(BIRMINGHAM_1));
    }

    @Test
    public void shouldReturnCorrectSoutheastList() {
        caseData.put(REGION, SOUTHEAST);
        caseData.put(SOUTHEAST_FRC_LIST, THAMESVALLEY);
        caseData.put(THAMESVALLEY_COURTLIST, THAMES_1);

        assertThat(ConsentedCaseHearingFunctions.getSelectedCourt(caseData), is(THAMES_1));
    }

    @Test
    public void shouldReturnCorrectNorthwestList() {
        caseData.put(REGION, NORTHWEST);
        caseData.put(NORTHWEST_FRC_LIST, LANCASHIRE);
        caseData.put(LANCASHIRE_COURTLIST, LANCASHIRE_1);

        assertThat(ConsentedCaseHearingFunctions.getSelectedCourt(caseData), is(LANCASHIRE_1));
    }

    @Test
    public void shouldReturnCorrectNortheastList() {
        caseData.put(REGION, NORTHEAST);
        caseData.put(NORTHEAST_FRC_LIST, HSYORKSHIRE);
        caseData.put(HSYORKSHIRE_COURTLIST, HS_YORKSHIRE_1);

        assertThat(ConsentedCaseHearingFunctions.getSelectedCourt(caseData), is(HS_YORKSHIRE_1));
    }

    @Test
    public void shouldReturnCorrectWalesList() {
        caseData.put(REGION, WALES);
        caseData.put(WALES_FRC_LIST, NORTHWALES);
        caseData.put(NORTH_WALES_COURTLIST, NORTHWALES_1);

        assertThat(ConsentedCaseHearingFunctions.getSelectedCourt(caseData), is(NORTHWALES_1));
    }
}