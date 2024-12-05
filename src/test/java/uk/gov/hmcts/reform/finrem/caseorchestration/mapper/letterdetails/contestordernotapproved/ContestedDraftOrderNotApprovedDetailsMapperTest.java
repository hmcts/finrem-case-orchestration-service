package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.contestordernotapproved;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CourtDetailsTemplateFields;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.ContestedDraftOrderNotApprovedDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.DocumentTemplateDetails;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.utils.TestUtils.buildCaseDetailsFromJson;
import static uk.gov.hmcts.reform.finrem.caseorchestration.utils.TestUtils.getCaseData;

@ExtendWith(MockitoExtension.class)
class ContestedDraftOrderNotApprovedDetailsMapperTest {

    public static final String TEST_JSON = "/fixtures/refusal-order-contested.json";

    @InjectMocks
    private ContestedDraftOrderNotApprovedDetailsMapper underTest;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Spy
    private FinremCaseDetailsMapper finremCaseDetailsMapper = new FinremCaseDetailsMapper(objectMapper.registerModule(new JavaTimeModule()));

    @Mock
    private CourtDetailsMapper courtDetailsMapper;

    @Test
    void givenValidCaseData_whenBuildDocumentTemplateDetails_thenReturnExpectedDetails() {
        FinremCaseDetails finremCaseDetails = readFinremCaseDetailsFromJson();
        stubCourtDetailsMapperGetCourtDetails(finremCaseDetails);

        DocumentTemplateDetails actual = underTest.buildDocumentTemplateDetails(finremCaseDetails,
            finremCaseDetails.getData().getRegionWrapper().getDefaultCourtList());

        DocumentTemplateDetails expected = getExpectedContestedDraftOrderNotApprovedDetails();

        assertEquals(expected, actual);
    }

    @Test
    void givenValidCaseData_whenGetDocumentTemplateDetailsAsMap_thenReturnExpectedMap() {
        FinremCaseDetails finremCaseDetails = readFinremCaseDetailsFromJson();
        stubCourtDetailsMapperGetCourtDetails(finremCaseDetails);

        Map<String, Object> actualData = getCaseData(underTest.getDocumentTemplateDetailsAsMap(finremCaseDetails,
            finremCaseDetails.getData().getRegionWrapper().getDefaultCourtList()));

        ContestedDraftOrderNotApprovedDetails expected = getExpectedContestedDraftOrderNotApprovedDetails();

        assertEquals(expected.getApplicantName(), actualData.get("ApplicantName"));
        assertEquals(expected.getJudgeDetails(), actualData.get("JudgeDetails"));
        assertEquals(expected.getRefusalOrderDate(), actualData.get("refusalOrderDate"));
        assertEquals(expected.getContestOrderNotApprovedRefusalReasons(), actualData.get("ContestOrderNotApprovedRefusalReasonsFormatted"));
    }

    private FinremCaseDetails readFinremCaseDetailsFromJson() {
        return finremCaseDetailsMapper.mapToFinremCaseDetails(buildCaseDetailsFromJson(objectMapper, TEST_JSON));
    }

    private void stubCourtDetailsMapperGetCourtDetails(FinremCaseDetails finremCaseDetails) {
        when(courtDetailsMapper.getCourtDetails(finremCaseDetails.getData().getRegionWrapper().getDefaultCourtList()))
            .thenReturn(CourtDetailsTemplateFields.builder().courtName("Nottingham County Court And Family Court").build());
    }

    private ContestedDraftOrderNotApprovedDetails getExpectedContestedDraftOrderNotApprovedDetails() {
        return ContestedDraftOrderNotApprovedDetails.builder()
            .caseNumber("1234567890")
            .judgeDetails("Her Honour Judge Contested")
            .court("Nottingham County Court And Family Court")
            .applicantName("Contested Applicant Name")
            .respondentName("Contested Respondent Name")
            .divorceCaseNumber("DD98D76543")
            .civilPartnership("No")
            .refusalOrderDate("2024-06-04T23:09:22.075")
            .contestOrderNotApprovedRefusalReasons("Refusal Reasons")
            .build();
    }
}
