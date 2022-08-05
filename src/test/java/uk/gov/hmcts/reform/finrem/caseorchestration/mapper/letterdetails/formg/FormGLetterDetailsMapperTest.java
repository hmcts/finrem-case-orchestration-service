package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.formg;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AbstractLetterDetailsMapperTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.FrcCourtDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.DocumentTemplateDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.FormGLetterDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.BristolCourt;
import uk.gov.hmcts.reform.finrem.ccd.domain.CaseType;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.DefaultCourtListWrapper;

import java.time.LocalDate;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FormGLetterDetailsMapperTest extends AbstractLetterDetailsMapperTest {

    private static final LocalDate HEARING_DATE = LocalDate.of(2022, 1, 1);

    @Autowired
    private FormGLetterDetailsMapper formGLetterDetailsMapper;

    @Before
    public void setUp() throws Exception {
        setCaseDetails();
    }

    @Test
    public void givenValidCaseData_whenBuildDocumentTemplateDetails_thenReturnExpectedDocumentTemplateDetails() {
        DocumentTemplateDetails actual = formGLetterDetailsMapper.buildDocumentTemplateDetails(caseDetails,
            caseDetails.getCaseData().getRegionWrapper().getDefaultCourtList());

        DocumentTemplateDetails expected = getExpectedFormGLetterDetails();

        assertEquals(expected, actual);
    }

    @Test
    public void givenValidCaseData_whenGetDocumentTemplateDetailsAsMap_thenReturnExpectedPlaceholdersMap() {
        Map<String, Object> placeholdersMap = formGLetterDetailsMapper.getDocumentTemplateDetailsAsMap(caseDetails,
            caseDetails.getCaseData().getRegionWrapper().getDefaultCourtList());

        FormGLetterDetails expected = getExpectedFormGLetterDetails();

        Map<String, Object> actualData = getCaseData(placeholdersMap);

        assertThat(actualData.get("applicantLName"), is(expected.getApplicantLName()));
        assertThat(actualData.get("hearingDate"), is(expected.getHearingDate()));
        assertThat(actualData.get("rSolicitorReference"), is(expected.getRespondentSolicitorReference()));
        assertThat(actualData.get("respondentFMName"), is(expected.getRespondentFmName()));
    }

    private FormGLetterDetails getExpectedFormGLetterDetails() {
        return FormGLetterDetails.builder()
            .applicantFmName("Test")
            .applicantLName("Applicant")
            .respondentFmName("Test")
            .respondentLName("Respondent")
            .divorceCaseNumber("DD12D12345")
            .courtDetails(getCourtDetails())
            .hearingDate(String.valueOf(HEARING_DATE))
            .solicitorReference("Test Sol Reference")
            .respondentSolicitorReference("Test Resp Sol Ref")
            .hearingTime("1pm")
            .build();
    }

    private void setCaseDetails() {
        FinremCaseData caseData = new FinremCaseData();
        caseData.setCcdCaseType(CaseType.CONTESTED);
        caseData.getContactDetailsWrapper().setApplicantFmName("Test");
        caseData.getContactDetailsWrapper().setApplicantLname("Applicant");
        caseData.getContactDetailsWrapper().setRespondentFmName("Test");
        caseData.getContactDetailsWrapper().setRespondentLname("Respondent");
        caseData.setDivorceCaseNumber("DD12D12345");
        caseData.getRegionWrapper().getDefaultCourtList().setBristolCourtList(BristolCourt.FR_bristolList_1);
        caseData.setHearingDate(HEARING_DATE);
        caseData.getContactDetailsWrapper().setSolicitorReference("Test Sol Reference");
        caseData.getContactDetailsWrapper().setRespondentSolicitorReference("Test Resp Sol Ref");
        caseData.setHearingTime("1pm");

        caseDetails = FinremCaseDetails.builder().caseType(CaseType.CONTESTED).caseData(caseData).build();
    }

    private FrcCourtDetails getCourtDetails() {
        DefaultCourtListWrapper courtListWrapper = new DefaultCourtListWrapper();
        courtListWrapper.setBristolCourtList(BristolCourt.FR_bristolList_1);
        return new CourtDetailsMapper(new ObjectMapper()).getCourtDetails(courtListWrapper);
    }
}