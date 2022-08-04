package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.consentorderapproved.ConsentOrderApprovedLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.consentorderapproved.ConsentOrderApprovedLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.ccd.domain.CaseType;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ConsentOrderApprovedLetterDetailsMapperTest extends AbstractLetterDetailsMapperTest {

    public static final String TEST_JSON = "/fixtures/consent-order-approved-mapping.json";

    @Autowired
    private ConsentOrderApprovedLetterDetailsMapper consentOrderApprovedLetterDetailsMapper;

    @Before
    public void setUp() {
        setCaseDetails(TEST_JSON);
    }

    @Test
    public void givenValidCaseData_whenBuildDocumentTemplateDetails_thenReturnExpectedTemplateDetails() {
        DocumentTemplateDetails actual = consentOrderApprovedLetterDetailsMapper.buildDocumentTemplateDetails(caseDetails,
            caseDetails.getCaseData().getRegionWrapper().getDefaultCourtList());

        DocumentTemplateDetails expected = getExpectedConsentOrderApprovedLetterDetails();

        assertEquals(expected, actual);
    }

    @Test
    public void givenContestedApplication_whenBuildDocumentTemplateDetails_thenReturnExpectedDetails() {
        setContestedFields();

        DocumentTemplateDetails actual = consentOrderApprovedLetterDetailsMapper.buildDocumentTemplateDetails(caseDetails,
            caseDetails.getCaseData().getRegionWrapper().getDefaultCourtList());

        DocumentTemplateDetails expected = getExpectedConsentOrderApprovedLetterDetails();

        assertEquals(expected, actual);
    }

    @Test
    public void givenNullEnums_whenBuildDocumentTemplateDetails_thenReturnDetails() {
        setNullFields();

        DocumentTemplateDetails actual = consentOrderApprovedLetterDetailsMapper.buildDocumentTemplateDetails(caseDetails,
            caseDetails.getCaseData().getRegionWrapper().getDefaultCourtList());

        assertNotNull(actual);
    }

    private void setNullFields() {
        caseDetails.getCaseData().setCivilPartnership(null);
        caseDetails.getCaseData().setServePensionProvider(null);
        caseDetails.getCaseData().setServePensionProviderResponsibility(null);
        caseDetails.getCaseData().setOrderDirectionJudge(null);
    }

    private void setContestedFields() {
        caseDetails.getCaseData().setCcdCaseType(CaseType.CONTESTED);
        caseDetails.getCaseData().getConsentOrderWrapper().setConsentSelectJudge("Deputy District Judge");
        caseDetails.getCaseData().getConsentOrderWrapper().setConsentJudgeName("Judgey");
        caseDetails.getCaseData().getConsentOrderWrapper().setConsentDateOfOrder(LocalDate.of(2022, 7, 1));
        caseDetails.getCaseData().getContactDetailsWrapper().setRespondentFmName("test");
        caseDetails.getCaseData().getContactDetailsWrapper().setRespondentLname("Korivi");
        caseDetails.getCaseData().setOrderDirectionDate(null);
        caseDetails.getCaseData().setOrderDirectionJudge(null);
        caseDetails.getCaseData().setOrderDirectionJudgeName(null);
        caseDetails.getCaseData().getContactDetailsWrapper().setAppRespondentFmName(null);
        caseDetails.getCaseData().getContactDetailsWrapper().setAppRespondentLName(null);
    }

    private ConsentOrderApprovedLetterDetails getExpectedConsentOrderApprovedLetterDetails() {
        return ConsentOrderApprovedLetterDetails.builder()
            .divorceCaseNumber("AB01D23456")
            .applicantFirstName("Poor")
            .applicantLastName("Guy")
            .respondentFirstName("test")
            .respondentLastName("Korivi")
            .orderDirectionJudge("Deputy District Judge")
            .orderDirectionJudgeName("Judgey")
            .servePensionProviderOther("Other")
            .servePensionProviderResponsibility("applicantSolicitor")
            .servePensionProvider("Yes")
            .orderDirectionDate("2022-07-01")
            .civilPartnership("No")
            .orderType("consent")
            .build();
    }
}
