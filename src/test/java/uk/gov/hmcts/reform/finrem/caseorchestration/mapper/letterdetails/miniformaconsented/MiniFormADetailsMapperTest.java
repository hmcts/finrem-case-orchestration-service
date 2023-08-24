package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.miniformaconsented;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.ContestedContestedAbstractLetterDetailsMapperTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NatureApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.DocumentTemplateDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.MiniFormADetails;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MiniFormADetailsMapperTest extends ContestedContestedAbstractLetterDetailsMapperTest {
    public static final String TEST_JSON = "/fixtures/mini-form-a-details.json";

    @Autowired
    private MiniFormADetailsMapperConsented miniFormADetailsMapper;

    @Before
    public void setUp() throws Exception {
        setCaseDetails(TEST_JSON);
    }

    @Test
    public void givenValidCaseData_whenBuildDocumentTemplateDetails_thenReturnExpectedTemplateDetails() {
        DocumentTemplateDetails expected = getExpectedMiniFormADetails();

        DocumentTemplateDetails actual = miniFormADetailsMapper.buildDocumentTemplateDetails(caseDetails,
            caseDetails.getData().getRegionWrapper().getDefaultCourtList());

        assertEquals(expected, actual);
    }

    @Test
    public void givenEmptyOrNullFields_whenBuildDocumentTemplateDetails_thenDoNotThrowException() {
        FinremCaseDetails emptyDetails = FinremCaseDetails.builder().data(FinremCaseData.builder().build()).build();

        DocumentTemplateDetails actual = miniFormADetailsMapper.buildDocumentTemplateDetails(emptyDetails,
            emptyDetails.getData().getRegionWrapper().getDefaultCourtList());

        assertNotNull(actual);
    }

    private MiniFormADetails getExpectedMiniFormADetails() {
        return MiniFormADetails.builder()
            .applicantFmName("Applicant")
            .applicantLName("Name")
            .appRespondentFmName("Respondent")
            .appRespondentLName("Name")
            .issueDate("2022-01-01")
            .respondentAddressConfidential("No")
            .divorceCaseNumber("DD12D12345")
            .solicitorName("SolicitorName")
            .solicitorFirm("SolicitorFirm")
            .solicitorReference("SolicitorReference")
            .solicitorAddress(getAddress("50 ApplicantSolicitor Street"))
            .appRespondentRep("Yes")
            .respondentPhone("12345")
            .respondentEmail("respondent@gmail.com")
            .respondentAddress(getAddress("50 Respondent Street"))
            .respondentSolicitorName("RespSolicitorName")
            .respondentSolicitorEmail("respsolicitor@gmail.com")
            .respondentSolicitorFirm("RespSolicitorFirm")
            .respondentSolicitorReference("RespSolicitorReference")
            .respondentSolicitorPhone("12345")
            .respondentSolicitorAddress(getAddress("50 RespondentSolicitor Street"))
            .natureOfApplication2(getNatureApplication2())
            .natureOfApplication3a("test3a")
            .natureOfApplication3b("test3b")
            .orderForChildrenQuestion1("Yes")
            .natureOfApplication5("Yes")
            .natureOfApplication6(getNatureApplication6())
            .natureOfApplication7("testNature7")
            .authorisationFirm("testAuthFirm")
            .authorisationName("testAuthName")
            .authorisation2b("testAuth2b")
            .authorisation3("2022-05-05")
            .build();
    }

    private Address getAddress(String addressLine1) {
        return Address.builder()
            .addressLine1(addressLine1)
            .addressLine2("Line2")
            .addressLine3("Line3")
            .postTown("London")
            .postCode("SE12 9SE")
            .country("United Kingdom")
            .county("Greater London")
            .build();
    }

    private List<String> getNatureApplication2() {
        return List.of(
            NatureApplication.LUMP_SUM_ORDER.getText(),
            NatureApplication.PERIODICAL_PAYMENT_ORDER.getText(),
            NatureApplication.PENSION_SHARING_ORDER.getText(),
            NatureApplication.PENSION_ATTACHMENT_ORDER.getText(),
            NatureApplication.PENSION_COMPENSATION_SHARING_ORDER.getText(),
            NatureApplication.PENSION_COMPENSATION_ATTACHMENT_ORDER.getText(),
            NatureApplication.A_SETTLEMENT_OR_A_TRANSFER_OF_PROPERTY.getText(),
            NatureApplication.PROPERTY_ADJUSTMENT_ORDER.getText());
    }

    private List<String> getNatureApplication6() {
        return List.of(
            "Step Child or Step Children",
            "disability expenses",
            "In addition to child support",
            "training",
            "When not habitually resident"
        );
    }
}