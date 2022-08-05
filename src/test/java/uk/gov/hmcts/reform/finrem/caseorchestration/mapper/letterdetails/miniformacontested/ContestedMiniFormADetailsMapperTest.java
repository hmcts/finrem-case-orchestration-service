package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.miniformacontested;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AbstractLetterDetailsMapperTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.ContestedMiniFormADetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.DocumentTemplateDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.Address;
import uk.gov.hmcts.reform.finrem.ccd.domain.BenefitPayment;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.MiamDomesticViolence;
import uk.gov.hmcts.reform.finrem.ccd.domain.MiamExemption;
import uk.gov.hmcts.reform.finrem.ccd.domain.MiamOtherGrounds;
import uk.gov.hmcts.reform.finrem.ccd.domain.MiamPreviousAttendance;
import uk.gov.hmcts.reform.finrem.ccd.domain.MiamUrgencyReason;
import uk.gov.hmcts.reform.finrem.ccd.domain.NatureApplication;
import uk.gov.hmcts.reform.finrem.ccd.domain.PropertyAdjustmentOrder;
import uk.gov.hmcts.reform.finrem.ccd.domain.PropertyAdjustmentOrderCollection;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ContestedMiniFormADetailsMapperTest extends AbstractLetterDetailsMapperTest {

    private static final String TEST_JSON = "/fixtures/contested/contested-mini-form-a-details.json";

    @Autowired
    private ContestedMiniFormADetailsMapper contestedMiniFormADetailsMapper;

    @Before
    public void setUp() throws Exception {
        setCaseDetails(TEST_JSON);
    }

    @Test
    public void givenValidCaseData_whenBuildDocumentTemplateDetails_thenReturnExpectedTemplateDetails() {
        DocumentTemplateDetails expected = getExpectedContestedMiniFormADetails();

        DocumentTemplateDetails actual = contestedMiniFormADetailsMapper.buildDocumentTemplateDetails(caseDetails,
            caseDetails.getCaseData().getRegionWrapper().getDefaultCourtList());

        assertEquals(expected, actual);
    }

    @Test
    public void givenEmptyOrNullFields_whenBuildDocumentTemplateDetails_thenDoNotThrowException() {
        FinremCaseDetails emptyDetails = FinremCaseDetails.builder().caseData(FinremCaseData.builder().build()).build();

        DocumentTemplateDetails actual = contestedMiniFormADetailsMapper.buildDocumentTemplateDetails(emptyDetails,
            emptyDetails.getCaseData().getRegionWrapper().getDefaultCourtList());

        assertNotNull(actual);
    }

    private ContestedMiniFormADetails getExpectedContestedMiniFormADetails() {
        return ContestedMiniFormADetails.builder()
            .applicantFmName("Applicant")
            .applicantLName("Name")
            .applicantPhone("12345")
            .applicantEmail("applicant@gmail.com")
            .applicantAddress(getAddress("50 Applicant Street"))
            .applicantAddressConfidential("No")
            .applicantRepresented("Yes")
            .applicantSolicitorName("SolicitorName")
            .applicantSolicitorFirm("SolicitorFirm")
            .solicitorReference("SolicitorReference")
            .applicantSolicitorAddress(getAddress("50 ApplicantSolicitor Street"))
            .respondentFmName("Respondent")
            .respondentLName("Name")
            .respondentPhone("12345")
            .respondentEmail("respondent@gmail.com")
            .respondentRepresented("Yes")
            .respondentAddressConfidential("No")
            .respondentAddress(getAddress("50 Respondent Street"))
            .respondentSolicitorName("RespSolicitorName")
            .respondentSolicitorFirm("RespSolicitorFirm")
            .respondentSolicitorReference("RespSolicitorReference")
            .respondentSolicitorPhone("12345")
            .respondentSolicitorEmail("respsolicitor@gmail.com")
            .respondentSolicitorAddress(getAddress("50 RespondentSolicitor Street"))
            .divorceCaseNumber("DD12D12345")
            .issueDate("2022-01-01")
            .authorisationName("testAuthName")
            .authorisationFirm("testAuthFirm")
            .authorisation2b("testAuth2b")
            .authorisation3("2022-05-05")
            .fastTrackDecision("Yes")
            .natureOfApplicationChecklist(getNatureApplicationChecklist())
            .natureOfApplication7("testNature7")
            .mortgageDetail("test3b")
            .propertyAddress("test3a")
            .paymentForChildrenDecision("Yes")
            .benefitForChildrenDecision("No")
            .benefitPaymentChecklist(getBenefitPaymentChecklist())
            .claimingExemptionMiam("No")
            .familyMediatorMiam("No")
            .applicantAttendedMiam("Yes")
            .miamExemptionsChecklist(getMiamExemptionsChecklist())
            .miamDomesticViolenceChecklist(getMiamDomesticViolenceChecklist())
            .miamUrgencyReasonChecklist(getMiamUrgencyReasonChecklist())
            .miamPreviousAttendanceChecklist(MiamPreviousAttendance.FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_1.getText())
            .miamOtherGroundsChecklist(MiamOtherGrounds.FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_1.getText())
            .propertyAdjustmentOrderDetail(getPropertyAdjustmentOrderDetail())
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

    private List<String> getNatureApplicationChecklist() {
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

    private List<String> getBenefitPaymentChecklist() {
        return List.of(
            BenefitPayment.BENEFIT_CHECKLIST_VALUE_1.getText(),
            BenefitPayment.BENEFIT_CHECKLIST_VALUE_2.getText(),
            BenefitPayment.BENEFIT_CHECKLIST_VALUE_3.getText()
        );
    }

    private List<String> getMiamExemptionsChecklist() {
        return List.of(
            MiamExemption.DOMESTIC_VIOLENCE.getText(),
            MiamExemption.URGENCY.getText(),
            MiamExemption.PREVIOUS_MIAM_ATTENDANCE.getText(),
            MiamExemption.OTHER.getText()
        );
    }

    private List<String> getMiamDomesticViolenceChecklist() {
        return List.of(
            MiamDomesticViolence.FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_1.getText(),
            MiamDomesticViolence.FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_4.getText(),
            MiamDomesticViolence.FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_3.getText(),
            MiamDomesticViolence.FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_2.getText()
        );
    }

    private List<String> getMiamUrgencyReasonChecklist() {
        return List.of(
            MiamUrgencyReason.FR_MS_MIAM_URGENCY_REASON_CHECKLIST_VALUE_1.getText(),
            MiamUrgencyReason.FR_MS_MIAM_URGENCY_REASON_CHECKLIST_VALUE_3.getText(),
            MiamUrgencyReason.FR_MS_MIAM_URGENCY_REASON_CHECKLIST_VALUE_5.getText()
        );
    }

    private List<PropertyAdjustmentOrderCollection> getPropertyAdjustmentOrderDetail() {
        return List.of(
            PropertyAdjustmentOrderCollection.builder()
                .value(PropertyAdjustmentOrder.builder()
                    .nameForProperty("FirstPropertyName")
                    .propertyAddress("FirstAddress")
                    .build())
                .build(),
            PropertyAdjustmentOrderCollection.builder()
                .value(PropertyAdjustmentOrder.builder()
                    .propertyAddress("SecondAddress")
                    .nameForProperty("SecondPropertyName")
                    .build())
                .build()
        );
    }
}