package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails;

import org.junit.Test;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralLetterAddressToType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFourWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOneWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThreeWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwoWrapper;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AddresseeGeneratorHelper.ADDRESS_MAP;
import static uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AddresseeGeneratorHelper.NAME_MAP;

public class AddresseeGeneratorHelperTest {

    @Test
    public void givenApplicantRecipient_whenGetAddressee_thenReturnApplicantAddressee() {
        FinremCaseData caseData = new FinremCaseData();
        caseData.setCcdCaseType(CaseType.CONTESTED);
        caseData.getContactDetailsWrapper().setApplicantFmName("Applicant");
        caseData.getContactDetailsWrapper().setApplicantLname("Name");
        caseData.getContactDetailsWrapper().setApplicantAddress(Address.builder()
            .addressLine1("1 Applicant Street")
            .addressLine2("Address Line 2")
            .build());
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(12343L).caseType(CaseType.CONTESTED).data(caseData).build();

        Addressee addressee = AddresseeGeneratorHelper.generateAddressee(caseDetails,
            DocumentHelper.PaperNotificationRecipient.APPLICANT);

        assertEquals("Applicant Name", addressee.getName());
        assertEquals("1 Applicant Street\nAddress Line 2", addressee.getFormattedAddress());
    }

    @Test
    public void givenApplicantSolicitorRecipient_whenGetAddressee_thenReturnApplicantSolicitorAddressee() {
        FinremCaseData caseData = new FinremCaseData();
        caseData.setCcdCaseType(CaseType.CONTESTED);
        caseData.getContactDetailsWrapper().setApplicantSolicitorName("AppSolName");
        caseData.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.YES);
        caseData.getContactDetailsWrapper().setApplicantSolicitorAddress(Address.builder()
            .addressLine1("1 ApplicantSolicitor Street")
            .addressLine2("Address Line 2")
            .build());
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(12343L).caseType(CaseType.CONTESTED).data(caseData).build();

        Addressee addressee = AddresseeGeneratorHelper.generateAddressee(caseDetails,
            DocumentHelper.PaperNotificationRecipient.APPLICANT);

        assertEquals("AppSolName", addressee.getName());
        assertEquals("1 ApplicantSolicitor Street\nAddress Line 2", addressee.getFormattedAddress());
    }

    @Test
    public void givenRespondentRecipient_whenGetAddressee_thenReturnRespondentAddressee() {
        FinremCaseData caseData = new FinremCaseData();
        caseData.setCcdCaseType(CaseType.CONTESTED);
        caseData.getContactDetailsWrapper().setRespondentFmName("Respondent");
        caseData.getContactDetailsWrapper().setRespondentLname("Name");
        caseData.getContactDetailsWrapper().setRespondentAddress(Address.builder()
            .addressLine1("1 Respondent Street")
            .addressLine2("Address Line 2")
            .build());
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(12343L).caseType(CaseType.CONTESTED).data(caseData).build();

        Addressee addressee = AddresseeGeneratorHelper.generateAddressee(caseDetails,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT);

        assertEquals("Respondent Name", addressee.getName());
        assertEquals("1 Respondent Street\nAddress Line 2", addressee.getFormattedAddress());
    }


    @Test
    public void givenIntervenerOneRecipient_whenGetAddressee_thenReturnRespondentAddressee() {
        FinremCaseData caseData = new FinremCaseData();
        caseData.setCcdCaseType(CaseType.CONTESTED);
        caseData.setIntervenerTwoWrapper(IntervenerTwoWrapper.builder().intervenerName("Intervener Name").intervenerAddress(Address.builder()
            .addressLine1("2 Intervener Street")
            .addressLine2("Address Line 2")
            .postCode("SW1 1AA")
            .build()).build());

        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(12343L).caseType(CaseType.CONTESTED).data(caseData).build();

        Addressee addressee = AddresseeGeneratorHelper.generateAddressee(caseDetails,
            DocumentHelper.PaperNotificationRecipient.INTERVENER_TWO);

        assertEquals("Intervener Name", addressee.getName());
        assertEquals("2 Intervener Street\nAddress Line 2\nSW1 1AA", addressee.getFormattedAddress());
    }


    @Test
    public void givenIntervenerTwoRecipient_whenGetAddressee_thenReturnRespondentAddressee() {
        FinremCaseData caseData = new FinremCaseData();
        caseData.setCcdCaseType(CaseType.CONTESTED);
        caseData.setIntervenerOneWrapper(IntervenerOneWrapper.builder().intervenerName("Intervener Name").intervenerAddress(Address.builder()
            .addressLine1("1 Intervener Street")
            .addressLine2("Address Line 2")
            .postCode("SW1 1AA")
            .build()).build());

        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(12343L).caseType(CaseType.CONTESTED).data(caseData).build();

        Addressee addressee = AddresseeGeneratorHelper.generateAddressee(caseDetails,
            DocumentHelper.PaperNotificationRecipient.INTERVENER_ONE);

        assertEquals("Intervener Name", addressee.getName());
        assertEquals("1 Intervener Street\nAddress Line 2\nSW1 1AA", addressee.getFormattedAddress());
    }


    @Test
    public void givenIntervenerThreeRecipient_whenGetAddressee_thenReturnRespondentAddressee() {
        FinremCaseData caseData = new FinremCaseData();
        caseData.setCcdCaseType(CaseType.CONTESTED);
        caseData.setIntervenerThreeWrapper(IntervenerThreeWrapper.builder().intervenerName("Intervener Name").intervenerAddress(Address.builder()
            .addressLine1("3 Intervener Street")
            .addressLine2("Address Line 2")
            .postCode("SW1 1AA")
            .build()).build());

        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(12343L).caseType(CaseType.CONTESTED).data(caseData).build();

        Addressee addressee = AddresseeGeneratorHelper.generateAddressee(caseDetails,
            DocumentHelper.PaperNotificationRecipient.INTERVENER_THREE);

        assertEquals("Intervener Name", addressee.getName());
        assertEquals("3 Intervener Street\nAddress Line 2\nSW1 1AA", addressee.getFormattedAddress());
    }


    @Test
    public void givenIntervenerFourRecipient_whenGetAddressee_thenReturnRespondentAddressee() {
        FinremCaseData caseData = new FinremCaseData();
        caseData.setCcdCaseType(CaseType.CONTESTED);
        caseData.setIntervenerFourWrapper(IntervenerFourWrapper.builder().intervenerName("Intervener Name").intervenerAddress(Address.builder()
            .addressLine1("4 Intervener Street")
            .addressLine2("Address Line 2")
            .postCode("SW1 1AA")
            .build()).build());

        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(12343L).caseType(CaseType.CONTESTED).data(caseData).build();

        Addressee addressee = AddresseeGeneratorHelper.generateAddressee(caseDetails,
            DocumentHelper.PaperNotificationRecipient.INTERVENER_FOUR);

        assertEquals("Intervener Name", addressee.getName());
        assertEquals("4 Intervener Street\nAddress Line 2\nSW1 1AA", addressee.getFormattedAddress());
    }


    @Test
    public void givenRespondentSolicitorRecipient_whenGetAddressee_thenReturnRespondentSolicitorAddressee() {
        FinremCaseData caseData = new FinremCaseData();
        caseData.setCcdCaseType(CaseType.CONTESTED);
        caseData.getContactDetailsWrapper().setContestedRespondentRepresented(YesOrNo.YES);
        caseData.getContactDetailsWrapper().setRespondentSolicitorName("RespSol Name");
        caseData.getContactDetailsWrapper().setRespondentSolicitorAddress(Address.builder()
            .addressLine1("1 RespondentSolicitor Street")
            .addressLine2("Address Line 2")
            .build());
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(12343L).caseType(CaseType.CONTESTED).data(caseData).build();

        Addressee addressee = AddresseeGeneratorHelper.generateAddressee(caseDetails,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT);

        assertEquals("RespSol Name", addressee.getName());
        assertEquals("1 RespondentSolicitor Street\nAddress Line 2", addressee.getFormattedAddress());
    }

    @Test
    public void givenAddress_whenFormatForLetterPrinting_thenFormat() {
        Address toFormat = Address.builder()
            .addressLine1("Line1")
            .addressLine2("Line2")
            .county("County")
            .build();

        String expectedFormattedAddress = """
            Line1
            Line2
            County""";

        assertEquals(expectedFormattedAddress, AddresseeGeneratorHelper.formatAddressForLetterPrinting(toFormat));
    }

    @Test
    public void givenValidCaseData_whenGetAddressToCaseDataMapping_thenReturnCorrectMapping() {
        FinremCaseData caseData = new FinremCaseData();
        caseData.getContactDetailsWrapper().setApplicantSolicitorAddress(Address.builder()
            .addressLine1("50 App Sol Street").build());
        caseData.getContactDetailsWrapper().setRespondentSolicitorAddress(Address.builder()
            .addressLine1("50 Resp Sol Street").build());
        caseData.getContactDetailsWrapper().setApplicantSolicitorName("AppSolName");
        caseData.getContactDetailsWrapper().setRespondentSolicitorName("RespSolName");
        Map<String, Map<GeneralLetterAddressToType, ?>> addressToCaseDataMapping =
            AddresseeGeneratorHelper.getAddressToCaseDataMapping(caseData);

        Map<GeneralLetterAddressToType, Address> addressMap =
            (Map<GeneralLetterAddressToType, Address>) addressToCaseDataMapping.get(ADDRESS_MAP);

        assertEquals("50 App Sol Street",
            addressMap.get(GeneralLetterAddressToType.APPLICANT_SOLICITOR).getAddressLine1());
        assertEquals("50 Resp Sol Street",
            addressMap.get(GeneralLetterAddressToType.RESPONDENT_SOLICITOR).getAddressLine1());
        assertEquals(addressMap.get(GeneralLetterAddressToType.RESPONDENT), new Address());

        Map<GeneralLetterAddressToType, String> nameMap =
            (Map<GeneralLetterAddressToType, String>) addressToCaseDataMapping.get(NAME_MAP);

        assertEquals("AppSolName", nameMap.get(GeneralLetterAddressToType.APPLICANT_SOLICITOR));
        assertEquals("RespSolName", nameMap.get(GeneralLetterAddressToType.RESPONDENT_SOLICITOR));
        assertEquals("", nameMap.get(GeneralLetterAddressToType.RESPONDENT));
        assertEquals("", nameMap.get(GeneralLetterAddressToType.OTHER));
    }
}