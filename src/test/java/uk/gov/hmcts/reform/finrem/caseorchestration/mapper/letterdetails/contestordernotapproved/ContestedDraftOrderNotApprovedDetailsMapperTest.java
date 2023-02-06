package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.contestordernotapproved;


import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AbstractLetterDetailsMapperTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.ContestedDraftOrderNotApprovedDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.DocumentTemplateDetails;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ContestedDraftOrderNotApprovedDetailsMapperTest extends AbstractLetterDetailsMapperTest {

    public static final String TEST_JSON = "/fixtures/refusal-order-contested.json";

    @Autowired
    private ContestedDraftOrderNotApprovedDetailsMapper contestedDraftOrderNotApprovedDetailsMapper;

    @Before
    public void setUp() {
        setCaseDetails(TEST_JSON);
    }

    @Test
    public void givenValidCaseData_whenBuildDocumentTemplateDetails_thenReturnExpectedDetails() {
        DocumentTemplateDetails actual = contestedDraftOrderNotApprovedDetailsMapper.buildDocumentTemplateDetails(caseDetails,
            caseDetails.getData().getRegionWrapper().getDefaultCourtList());

        DocumentTemplateDetails expected = getExpectedContestedDraftOrderNotApprovedDetails();

        assertEquals(expected, actual);
    }

    @Test
    public void givenValidCaseData_whenGetDocumentTemplateDetailsAsMap_thenReturnExpectedMap() {
        Map<String, Object> placeholdersMap = contestedDraftOrderNotApprovedDetailsMapper.getDocumentTemplateDetailsAsMap(caseDetails,
            caseDetails.getData().getRegionWrapper().getDefaultCourtList());

        ContestedDraftOrderNotApprovedDetails expected = getExpectedContestedDraftOrderNotApprovedDetails();

        Map<String, Object> actualData = getCaseData(placeholdersMap);

        assertThat(actualData.get("ApplicantName"), is(expected.getApplicantName()));
        assertThat(actualData.get("JudgeDetails"), is(expected.getJudgeDetails()));
        assertThat(actualData.get("refusalOrderDate"), is(expected.getRefusalOrderDate()));
        assertThat(actualData.get("ContestOrderNotApprovedRefusalReasonsFormatted"),
            is(expected.getContestOrderNotApprovedRefusalReasons()));
    }

    private ContestedDraftOrderNotApprovedDetails getExpectedContestedDraftOrderNotApprovedDetails() {
        return ContestedDraftOrderNotApprovedDetails.builder()
            .judgeDetails("Her Honour Judge Contested")
            .court("Nottingham County Court And Family Court")
            .applicantName("Contested Applicant Name")
            .respondentName("Contested Respondent Name")
            .divorceCaseNumber("DD98D76543")
            .civilPartnership("No")
            .refusalOrderDate("2020-06-01")
            .contestOrderNotApprovedRefusalReasons(getRefusalReasons())
            .build();
    }

    private String getRefusalReasons() {
        return "- Test Reason 1\n- Test Reason 2";
    }
}