package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.formc;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.ContestedAbstractLetterDetailsMapperTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BristolCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDataContested;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DefaultCourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.FrcCourtDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.DocumentTemplateDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.FormCLetterDetails;

import java.time.LocalDate;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FormCLetterDetailsMapperTest extends ContestedAbstractLetterDetailsMapperTest {

    private static final LocalDate HEARING_DATE = LocalDate.of(2022, 1, 1);

    @Autowired
    private FormCLetterDetailsMapperContested formCLetterDetailsMapper;

    @Before
    public void setUp() {
        setCaseDetails();
    }

    @Test
    public void givenValidCaseData_whenBuildDocumentTemplateDetailsFormC_thenReturnExpectedTemplateDetails() {
        DocumentTemplateDetails actual = formCLetterDetailsMapper.buildDocumentTemplateDetails(caseDetails,
            caseDetails.getData().getRegionWrapper().getDefaultCourtList());

        DocumentTemplateDetails expected = getExpectedFormCLetterDetails();

        assertEquals(expected, actual);
    }

    @Test
    public void givenValidCaseData_whenGetDocumentTemplateDetailsAsMap_thenReturnExpectedMap() {
        Map<String, Object> placeholdersMap = formCLetterDetailsMapper.getDocumentTemplateDetailsAsMap(caseDetails,
            caseDetails.getData().getRegionWrapper().getDefaultCourtList());

        FormCLetterDetails expected = getExpectedFormCLetterDetails();

        Map<String, Object> actualData = getCaseData(placeholdersMap);

        assertThat(actualData.get("applicantLName"), is(expected.getApplicantLName()));
        assertThat(actualData.get("hearingDate"), is(expected.getHearingDate()));
        assertThat(actualData.get("formCCreatedDate"), is(expected.getFormCCreatedDate()));
        assertThat(actualData.get("rSolicitorReference"), is(expected.getRespondentSolicitorReference()));
    }

    private FormCLetterDetails getExpectedFormCLetterDetails() {
        return FormCLetterDetails.builder()
            .applicantFmName("Test")
            .applicantLName("Applicant")
            .respondentFmName("Test")
            .respondentLName("Respondent")
            .divorceCaseNumber("DD12D12345")
            .courtDetails(getCourtDetails())
            .hearingDate(String.valueOf(HEARING_DATE))
            .hearingDateLess35Days(String.valueOf(HEARING_DATE.minusDays(35)))
            .hearingDateLess14Days(String.valueOf(HEARING_DATE.minusDays(14)))
            .solicitorReference("Test Sol Reference")
            .respondentSolicitorReference("Test Resp Sol Ref")
            .additionalInformationAboutHearing("Test")
            .hearingTime("1pm")
            .timeEstimate("1 hour")
            .formCCreatedDate(String.valueOf(LocalDate.now()))
            .eventDatePlus21Days(String.valueOf(LocalDate.now().plusDays(21)))
            .build();
    }

    private FrcCourtDetails getCourtDetails() {
        DefaultCourtListWrapper courtListWrapper = new DefaultCourtListWrapper();
        courtListWrapper.setBristolCourtList(BristolCourt.BRISTOL_CIVIL_AND_FAMILY_JUSTICE_CENTRE);
        return new CourtDetailsMapper(new ObjectMapper()).getCourtDetails(courtListWrapper);
    }

    private void setCaseDetails() {
        FinremCaseDataContested caseData = new FinremCaseDataContested();
        caseData.setCcdCaseType(CaseType.CONTESTED);
        caseData.getContactDetailsWrapper().setApplicantFmName("Test");
        caseData.getContactDetailsWrapper().setApplicantLname("Applicant");
        caseData.getContactDetailsWrapper().setRespondentFmName("Test");
        caseData.getContactDetailsWrapper().setRespondentLname("Respondent");
        caseData.setDivorceCaseNumber("DD12D12345");
        caseData.getRegionWrapper().getDefaultCourtList().setBristolCourtList(BristolCourt.BRISTOL_CIVIL_AND_FAMILY_JUSTICE_CENTRE);
        caseData.setHearingDate(HEARING_DATE);
        caseData.getContactDetailsWrapper().setSolicitorReference("Test Sol Reference");
        caseData.getContactDetailsWrapper().setRespondentSolicitorReference("Test Resp Sol Ref");
        caseData.setAdditionalInformationAboutHearing("Test");
        caseData.setHearingTime("1pm");
        caseData.setTimeEstimate("1 hour");

        caseDetails = FinremCaseDetails.<FinremCaseDataContested>builder()
            .id(12345L).caseType(CaseType.CONTESTED).data(caseData).build();
    }

}