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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;

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
    public void givenRespondentSolicitorRecipient_whenGetAddressee_thenReturnRespondentSolicitorAddressee() {
        FinremCaseData caseData = new FinremCaseData();
        caseData.setCcdCaseType(CaseType.CONTESTED);
        caseData.getContactDetailsWrapper().setContestedRespondentRepresented(YesOrNo.YES);
        caseData.getContactDetailsWrapper().setRespondentSolicitorName("RespSolName");
        caseData.getContactDetailsWrapper().setRespondentSolicitorAddress(Address.builder()
            .addressLine1("1 RespondentSolicitor Street")
            .addressLine2("Address Line 2")
            .build());
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(12343L).caseType(CaseType.CONTESTED).data(caseData).build();

        Addressee addressee = AddresseeGeneratorHelper.generateAddressee(caseDetails,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT);

        assertEquals("RespSolName", addressee.getName());
        assertEquals("1 RespondentSolicitor Street\nAddress Line 2", addressee.getFormattedAddress());
    }

    @Test
    public void givenIntervenerOneRecipient_whenGetAddressee_thenReturnIntervenerOneAddressee() {
        FinremCaseData caseData = new FinremCaseData();
        caseData.setCcdCaseType(CaseType.CONTESTED);
        IntervenerWrapper wrapper = caseData.getIntervenerOneWrapper();
        wrapper.setIntervenerName("Intervener Last");
        wrapper.setIntervenerAddress(Address.builder()
            .addressLine1("1 Intervener Street")
            .addressLine2("Address Line 2")
            .build());
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(12343L).caseType(CaseType.CONTESTED).data(caseData).build();

        Addressee addressee = AddresseeGeneratorHelper.generateAddressee(caseDetails,
            DocumentHelper.PaperNotificationRecipient.INTERVENER_ONE);

        assertEquals("Intervener Last", addressee.getName());
        assertEquals("1 Intervener Street\nAddress Line 2", addressee.getFormattedAddress());
    }

    @Test
    public void givenIntervenerTwoRecipient_whenGetAddressee_thenReturnIntervenerTwoAddressee() {
        FinremCaseData caseData = new FinremCaseData();
        caseData.setCcdCaseType(CaseType.CONTESTED);
        IntervenerWrapper wrapper = caseData.getIntervenerTwoWrapper();
        wrapper.setIntervenerName("Intervener Last");
        wrapper.setIntervenerAddress(Address.builder()
            .addressLine1("2 Intervener Street")
            .addressLine2("Address Line 2")
            .build());
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(22343L).caseType(CaseType.CONTESTED).data(caseData).build();

        Addressee addressee = AddresseeGeneratorHelper.generateAddressee(caseDetails,
            DocumentHelper.PaperNotificationRecipient.INTERVENER_TWO);

        assertEquals("Intervener Last", addressee.getName());
        assertEquals("2 Intervener Street\nAddress Line 2", addressee.getFormattedAddress());
    }

    @Test
    public void givenIntervenerThreeRecipient_whenGetAddressee_thenReturnIntervenerThreeAddressee() {
        FinremCaseData caseData = new FinremCaseData();
        caseData.setCcdCaseType(CaseType.CONTESTED);
        IntervenerWrapper wrapper = caseData.getIntervenerThreeWrapper();
        wrapper.setIntervenerName("Intervener Last");
        wrapper.setIntervenerAddress(Address.builder()
            .addressLine1("3 Intervener Street")
            .addressLine2("Address Line 2")
            .build());
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(33343L).caseType(CaseType.CONTESTED).data(caseData).build();

        Addressee addressee = AddresseeGeneratorHelper.generateAddressee(caseDetails,
            DocumentHelper.PaperNotificationRecipient.INTERVENER_THREE);

        assertEquals("Intervener Last", addressee.getName());
        assertEquals("3 Intervener Street\nAddress Line 2", addressee.getFormattedAddress());
    }

    @Test
    public void givenIntervenerFourRecipient_whenGetAddressee_thenReturnIntervenerFourAddressee() {
        FinremCaseData caseData = new FinremCaseData();
        caseData.setCcdCaseType(CaseType.CONTESTED);
        IntervenerWrapper wrapper = caseData.getIntervenerFourWrapper();
        wrapper.setIntervenerName("Intervener Last");
        wrapper.setIntervenerAddress(Address.builder()
            .addressLine1("4 Intervener Street")
            .addressLine2("Address Line 2")
            .build());
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(44444L).caseType(CaseType.CONTESTED).data(caseData).build();

        Addressee addressee = AddresseeGeneratorHelper.generateAddressee(caseDetails,
            DocumentHelper.PaperNotificationRecipient.INTERVENER_FOUR);

        assertEquals("Intervener Last", addressee.getName());
        assertEquals("4 Intervener Street\nAddress Line 2", addressee.getFormattedAddress());
    }

    @Test
    public void givenIntervenerOneSolicitorRecipient_whenGetAddressee_thenReturnIntervenerOneSolicitorAddressee() {
        FinremCaseData caseData = new FinremCaseData();
        caseData.setCcdCaseType(CaseType.CONTESTED);
        IntervenerOneWrapper wrapper = caseData.getIntervenerOneWrapper();
        wrapper.setIntervenerSolName("IntSolName");
        wrapper.setIntervenerSolEmail("i@gmail.com");
        wrapper.setIntervenerAddress(Address.builder()
            .addressLine1("4 Intervener Street")
            .addressLine2("Address Line 2")
            .build());
        caseData.setIntervenerOneWrapper(wrapper);
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(12343L).caseType(CaseType.CONTESTED).data(caseData).build();

        Addressee addressee = AddresseeGeneratorHelper.generateAddressee(caseDetails,
            DocumentHelper.PaperNotificationRecipient.INTERVENER_ONE);

        assertEquals("IntSolName", addressee.getName());
        assertEquals("4 Intervener Street\nAddress Line 2", addressee.getFormattedAddress());
    }

    @Test
    public void givenIntervenerTwoSolicitorRecipient_whenGetAddressee_thenReturnIntervenerTwoSolicitorAddressee() {
        FinremCaseData caseData = new FinremCaseData();
        caseData.setCcdCaseType(CaseType.CONTESTED);
        IntervenerTwoWrapper wrapper = caseData.getIntervenerTwoWrapper();
        wrapper.setIntervenerSolName("IntSolName");
        wrapper.setIntervenerSolEmail("i@gmail.com");
        wrapper.setIntervenerAddress(Address.builder()
            .addressLine1("4 Intervener Street")
            .addressLine2("Address Line 2")
            .build());
        caseData.setIntervenerTwoWrapper(wrapper);
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(12343L).caseType(CaseType.CONTESTED).data(caseData).build();

        Addressee addressee = AddresseeGeneratorHelper.generateAddressee(caseDetails,
            DocumentHelper.PaperNotificationRecipient.INTERVENER_TWO);

        assertEquals("IntSolName", addressee.getName());
        assertEquals("4 Intervener Street\nAddress Line 2", addressee.getFormattedAddress());
    }

    @Test
    public void givenIntervenerThreeSolicitorRecipient_whenGetAddressee_thenReturnIntervenerThreeSolicitorAddressee() {
        FinremCaseData caseData = new FinremCaseData();
        caseData.setCcdCaseType(CaseType.CONTESTED);
        IntervenerThreeWrapper wrapper = caseData.getIntervenerThreeWrapper();
        wrapper.setIntervenerSolName("IntSolName");
        wrapper.setIntervenerSolEmail("i@gmail.com");
        wrapper.setIntervenerAddress(Address.builder()
            .addressLine1("4 Intervener Street")
            .addressLine2("Address Line 2")
            .build());
        caseData.setIntervenerThreeWrapper(wrapper);
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(12343L).caseType(CaseType.CONTESTED).data(caseData).build();

        Addressee addressee = AddresseeGeneratorHelper.generateAddressee(caseDetails,
            DocumentHelper.PaperNotificationRecipient.INTERVENER_THREE);

        assertEquals("IntSolName", addressee.getName());
        assertEquals("4 Intervener Street\nAddress Line 2", addressee.getFormattedAddress());
    }

    @Test
    public void givenIntervenerFourSolicitorRecipient_whenGetAddressee_thenReturnIntervenerFourSolicitorAddressee() {
        FinremCaseData caseData = new FinremCaseData();
        caseData.setCcdCaseType(CaseType.CONTESTED);
        IntervenerFourWrapper wrapper = caseData.getIntervenerFourWrapper();
        wrapper.setIntervenerSolName("IntSolName");
        wrapper.setIntervenerSolEmail("i@gmail.com");
        wrapper.setIntervenerAddress(Address.builder()
            .addressLine1("4 Intervener Street")
            .addressLine2("Address Line 2")
            .build());
        caseData.setIntervenerFourWrapper(wrapper);
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(12343L).caseType(CaseType.CONTESTED).data(caseData).build();

        Addressee addressee = AddresseeGeneratorHelper.generateAddressee(caseDetails,
            DocumentHelper.PaperNotificationRecipient.INTERVENER_FOUR);

        assertEquals("IntSolName", addressee.getName());
        assertEquals("4 Intervener Street\nAddress Line 2", addressee.getFormattedAddress());
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

        assertEquals(addressMap.get(GeneralLetterAddressToType.APPLICANT_SOLICITOR).getAddressLine1(),
            "50 App Sol Street");
        assertEquals(addressMap.get(GeneralLetterAddressToType.RESPONDENT_SOLICITOR).getAddressLine1(),
            "50 Resp Sol Street");
        assertEquals(addressMap.get(GeneralLetterAddressToType.RESPONDENT), new Address());

        Map<GeneralLetterAddressToType, String> nameMap =
            (Map<GeneralLetterAddressToType, String>) addressToCaseDataMapping.get(NAME_MAP);

        assertEquals(nameMap.get(GeneralLetterAddressToType.APPLICANT_SOLICITOR), "AppSolName");
        assertEquals(nameMap.get(GeneralLetterAddressToType.RESPONDENT_SOLICITOR), "RespSolName");
        assertEquals(nameMap.get(GeneralLetterAddressToType.RESPONDENT), "");
        assertEquals(nameMap.get(GeneralLetterAddressToType.OTHER), "");
    }
}