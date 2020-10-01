package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIDLANDS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIDLANDS_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTTINGHAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTTINGHAM_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REGION;

public class CaseHearingFunctionsTest {

    @Test
    public void givenMidlandsNottinghamCourtDetailsInCaseData_whenGettingSelectedCourtList_thenNottinghamCourtListIsReturned() {
        Map<String, Object> caseData = prepareCaseDataWithCourtDetails("", MIDLANDS, NOTTINGHAM);

        String courtList = CaseHearingFunctions.getSelectedCourt(caseData);
        assertThat(courtList, is(NOTTINGHAM_COURTLIST));
    }

    @Test
    public void givenMidlandsNottinghamCourtDetailsPrefixedInCaseData_whenGettingSelectedCourtList_thenPrefixedNottinghamCourtListIsReturned() {
        String prefix = "any-prefix-";
        Map<String, Object> caseData = prepareCaseDataWithCourtDetails(prefix, MIDLANDS, NOTTINGHAM);

        String courtList = CaseHearingFunctions.getSelectedCourt(caseData, prefix);
        assertThat(courtList, is(prefix + NOTTINGHAM_COURTLIST));
    }

    private Map<String, Object> prepareCaseDataWithCourtDetails(String prefix, String region, String frcList) {
        Map<String, Object> caseData = ImmutableMap.of(
            prefix + REGION, region,
            prefix + MIDLANDS_FRC_LIST, frcList);
        return caseData;
    }


}
