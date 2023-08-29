package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.consentorderapproved.ConsentOrderApprovedLetterDetailsMapperContested;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.ConsentOrderApprovedLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.DocumentTemplateDetails;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ConsentOrderApprovedLetterDetailsMapperTest extends ContestedAbstractLetterDetailsMapperTest {

    public static final String TEST_JSON = "/fixtures/consent-order-approved-mapping.json";

    @Autowired
    private ConsentOrderApprovedLetterDetailsMapperContested consentOrderApprovedLetterDetailsMapperContested;

    @Before
    public void setUp() {
        setCaseDetails(TEST_JSON);
    }

    @Test
    public void givenValidCaseData_whenBuildDocumentTemplateDetails_thenReturnExpectedTemplateDetails() {
        DocumentTemplateDetails actual = consentOrderApprovedLetterDetailsMapperContested.buildDocumentTemplateDetails(caseDetails,
            caseDetails.getData().getRegionWrapper().getDefaultCourtList());

        DocumentTemplateDetails expected = getExpectedConsentOrderApprovedLetterDetails();

        assertEquals(expected, actual);
    }

    @Test
    public void givenContestedApplication_whenBuildDocumentTemplateDetails_thenReturnExpectedDetails() {
        setContestedFields();

        DocumentTemplateDetails actual = consentOrderApprovedLetterDetailsMapperContested.buildDocumentTemplateDetails(caseDetails,
            caseDetails.getData().getRegionWrapper().getDefaultCourtList());

        DocumentTemplateDetails expected = getExpectedConsentOrderApprovedLetterDetails();

        assertEquals(expected, actual);
    }

    @Test
    public void givenNullEnums_whenBuildDocumentTemplateDetails_thenReturnDetails() {
        setNullFields();

        DocumentTemplateDetails actual = consentOrderApprovedLetterDetailsMapperContested.buildDocumentTemplateDetails(caseDetails,
            caseDetails.getData().getRegionWrapper().getDefaultCourtList());

        assertNotNull(actual);
    }

    private void setNullFields() {
        caseDetails.getData().setCivilPartnership(null);
        caseDetails.getData().setServePensionProvider(null);
        caseDetails.getData().setServePensionProviderResponsibility(null);
    }

    private void setContestedFields() {
        caseDetails.getData().setCcdCaseType(CaseType.CONTESTED);
        caseDetails.getData().getConsentOrderWrapper().setConsentSelectJudge("Deputy District Judge");
        caseDetails.getData().getConsentOrderWrapper().setConsentJudgeName("Judgey");
        caseDetails.getData().getConsentOrderWrapper().setConsentDateOfOrder(LocalDate.of(2022, 7, 1));
        caseDetails.getData().getContactDetailsWrapper().setRespondentFmName("test");
        caseDetails.getData().getContactDetailsWrapper().setRespondentLname("Korivi");
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
