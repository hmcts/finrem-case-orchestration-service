package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_HEARING_REGION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_MIDLANDS_FRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_NOTTINGHAM_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIDLANDS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIDLANDS_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIDLANDS_FRC_LIST_CT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTTINGHAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTTINGHAM_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REGION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REGION_CT;

public class CaseHearingFunctionsTest {

    @Test
    public void givenMidlandsNottinghamCourtDetailsInCaseData_whenGettingSelectedCourtList_thenNottinghamCourtListIsReturned() {
        Map<String, Object> caseData = ImmutableMap.of(
            REGION, MIDLANDS,
            MIDLANDS_FRC_LIST, NOTTINGHAM);

        String courtList = CaseHearingFunctions.getSelectedCourt(caseData);
        assertThat(courtList, is(NOTTINGHAM_COURTLIST));
    }

    @Test
    public void givenMidlandsNottinghamCourtDetailsInCaseData_whenGettingSelectedCourtListGa_thenNottinghamCourtListGaIsReturned() {
        Map<String, Object> caseData = ImmutableMap.of(
            GENERAL_APPLICATION_DIRECTIONS_HEARING_REGION, MIDLANDS,
            GENERAL_APPLICATION_DIRECTIONS_MIDLANDS_FRC, NOTTINGHAM);

        String courtList = CaseHearingFunctions.getSelectedCourtGA(caseData);
        assertThat(courtList, is(GENERAL_APPLICATION_DIRECTIONS_NOTTINGHAM_COURT));
    }

    @Test
    public void givenMidlandsNottinghamCourtDetailsInCaseData_whenGettingSelectedCourtListCt_thenNottinghamCourtListCtIsReturned() {
        Map<String, Object> caseData = ImmutableMap.of(
            REGION_CT, MIDLANDS,
            MIDLANDS_FRC_LIST_CT, NOTTINGHAM);

        String courtList = CaseHearingFunctions.getSelectedCourtComplexType(caseData);
        assertThat(courtList, is(NOTTINGHAM_COURTLIST));
    }
}
