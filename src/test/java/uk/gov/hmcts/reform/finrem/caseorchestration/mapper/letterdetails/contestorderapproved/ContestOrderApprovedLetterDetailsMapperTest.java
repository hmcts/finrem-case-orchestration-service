package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.contestorderapproved;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AbstractLetterDetailsMapperTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CfcCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.JudgeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.ContestOrderApprovedLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.DocumentTemplateDetails;

import java.time.LocalDate;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ContestOrderApprovedLetterDetailsMapperTest extends AbstractLetterDetailsMapperTest {

    @Autowired
    private ContestOrderApprovedLetterDetailsMapper contestOrderApprovedLetterDetailsMapper;

    @Test
    public void givenValidCaseDataContested_whenBuildDocumentTemplateDetails_thenReturnExpectedDetails() {
        FinremCaseDetails caseDetails = contestedCaseDetails();
        DocumentTemplateDetails actual = contestOrderApprovedLetterDetailsMapper.buildDocumentTemplateDetails(caseDetails,
            caseDetails.getData().getRegionWrapper().getDefaultCourtList());

        DocumentTemplateDetails expected = getExpectedContestOrderApprovedLetterDetails();

        assertEquals(expected, actual);
    }

    @Test
    public void givenValidCaseData_whenGetDocumentTemplateDetailsAsMap_thenReturnExpectedMap() {
        FinremCaseDetails caseDetails = contestedCaseDetails();

        Map<String, Object> placeholdersMap = contestOrderApprovedLetterDetailsMapper.getDocumentTemplateDetailsAsMap(caseDetails,
            caseDetails.getData().getRegionWrapper().getDefaultCourtList());

        ContestOrderApprovedLetterDetails expected = getExpectedContestOrderApprovedLetterDetails();
        Map<String, Object> dataMap = getCaseData(placeholdersMap);

        assertThat(dataMap.get("ApplicantName"), is(expected.getApplicantName()));
        assertThat(dataMap.get("Court"), is(expected.getCourt()));
        assertThat(dataMap.get("JudgeDetails"), is(expected.getJudgeDetails()));
        assertThat(dataMap.get("letterDate"), is(expected.getLetterDate()));
    }

    private ContestOrderApprovedLetterDetails getExpectedContestOrderApprovedLetterDetails() {
        return ContestOrderApprovedLetterDetails.builder()
            .applicantName("Poor Guy")
            .respondentName("Test Korivi")
            .judgeDetails("District Judge Details")
            .letterDate(String.valueOf(LocalDate.now()))
            .orderApprovedDate(String.valueOf(LocalDate.of(2022, 2, 2)))
            .civilPartnership(YesOrNo.NO.getYesOrNo())
            .divorceCaseNumber("DD12D12345")
            .court("Bromley County Court And Family Court")
            .build();
    }

    private FinremCaseDetails contestedCaseDetails() {
        FinremCaseData caseData = FinremCaseData.builder().build();
        caseData.getContactDetailsWrapper().setApplicantFmName("Poor");
        caseData.getContactDetailsWrapper().setApplicantLname("Guy");
        caseData.getContactDetailsWrapper().setRespondentFmName("Test");
        caseData.getContactDetailsWrapper().setRespondentLname("Korivi");
        caseData.setDivorceCaseNumber("DD12D12345");
        caseData.setCivilPartnership(YesOrNo.NO);
        caseData.setOrderApprovedJudgeType(JudgeType.DISTRICT_JUDGE);
        caseData.setOrderApprovedJudgeName("Details");
        caseData.setOrderApprovedDate(LocalDate.of(2022, 2, 2));
        caseData.getRegionWrapper().getDefaultCourtList().setCfcCourtList(CfcCourt.BROMLEY_COUNTY_COURT_AND_FAMILY_COURT);
        caseData.setCcdCaseType(CaseType.CONTESTED);

        return FinremCaseDetails.builder().id(1234L).caseType(CaseType.CONTESTED).data(caseData).build();
    }
}