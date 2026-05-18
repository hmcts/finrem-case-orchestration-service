package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralLetterAddressToType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralLetterWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFour;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOne;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThree;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwo;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.AddresseeGeneratorUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.utils.AddresseeGeneratorUtils.ADDRESS_MAP;
import static uk.gov.hmcts.reform.finrem.caseorchestration.utils.AddresseeGeneratorUtils.NAME_MAP;

@ExtendWith(MockitoExtension.class)
class AddresseeGeneratorUtilsTest {

    @Mock
    private FinremCaseData caseData;
    @Mock
    private FinremCaseDetails caseDetails;
    @Mock
    private ContactDetailsWrapper contactDetailsWrapper;
    @Mock
    private IntervenerOne intervenerOne;
    @Mock
    private IntervenerTwo intervenerTwo;
    @Mock
    private IntervenerThree intervenerThree;
    @Mock
    private IntervenerFour intervenerFour;
    @Mock
    private GeneralLetterWrapper generalLetterWrapper;

    @Test
    void givenNullRecipient_whenGetAddressee_thenThrowException() {
        assertThrows(IllegalArgumentException.class, () ->
            AddresseeGeneratorUtils.generateAddressee(caseDetails, null));
    }

    @Test
    void givenNullCaseDetails_whenGetAddressee_thenThrowException() {
        assertThrows(IllegalArgumentException.class, () ->
            AddresseeGeneratorUtils.generateAddressee(null, DocumentHelper.PaperNotificationRecipient.APPLICANT));
    }

    @Test
    void givenRecipientIsMissingAddress_whenGetAddressee_thenReturnThrowException() {
        stubCaseDetailsWithCaseDataWithContactDetailsWrapper();
        when(caseData.getFullApplicantName()).thenReturn("Applicant Name");

        assertThrows(IllegalArgumentException.class, () ->
            AddresseeGeneratorUtils.generateAddressee(caseDetails, DocumentHelper.PaperNotificationRecipient.APPLICANT));
    }

    @Test
    void givenApplicantRecipientIsNotInternational_whenGetAddressee_thenReturnApplicantAddresseeWithoutCountry() {
        stubCaseDetailsWithCaseDataWithContactDetailsWrapper();
        when(caseData.isApplicantRepresentedByASolicitor()).thenReturn(false);
        when(caseData.getFullApplicantName()).thenReturn("Applicant Name");
        when(contactDetailsWrapper.getApplicantAddress()).thenReturn(
            address("1 Applicant Street", "Address Line 2", "County", "Country", "SW1 1AA"));

        assertAddressee(
            AddresseeGeneratorUtils.generateAddressee(caseDetails, DocumentHelper.PaperNotificationRecipient.APPLICANT),
            "Applicant Name",
            """
                1 Applicant Street
                Address Line 2
                County
                SW1 1AA"""
        );
    }

    @Test
    void givenApplicantRecipientIsInternational_whenGetAddressee_thenReturnApplicantAddresseeWithCountry() {
        stubCaseDetailsWithCaseDataWithContactDetailsWrapper();
        when(caseData.isApplicantRepresentedByASolicitor()).thenReturn(false);
        when(caseData.getFullApplicantName()).thenReturn("Applicant Name");
        when(contactDetailsWrapper.getApplicantResideOutsideUK()).thenReturn(YesOrNo.YES);
        when(contactDetailsWrapper.getApplicantAddress()).thenReturn(
            address("1 International Street", "Address Line 2", "Paris", "France", "123 FRA"));

        assertAddressee(
            AddresseeGeneratorUtils.generateAddressee(caseDetails, DocumentHelper.PaperNotificationRecipient.APPLICANT),
            "Applicant Name",
            """
                1 International Street
                Address Line 2
                Paris
                123 FRA
                France"""
        );
    }

    @Test
    void givenApplicantSolicitorRecipient_whenGetAddressee_thenReturnApplicantSolicitorAddressee() {
        stubCaseDetailsWithCaseDataWithContactDetailsWrapper();
        when(caseData.isApplicantRepresentedByASolicitor()).thenReturn(true);
        when(caseData.getAppSolicitorName()).thenReturn("AppSolName");
        when(caseData.getAppSolicitorAddress()).thenReturn(
            address("1 ApplicantSolicitor Street", "Address Line 2", null, null, null));

        assertAddressee(
            AddresseeGeneratorUtils.generateAddressee(caseDetails, DocumentHelper.PaperNotificationRecipient.APPLICANT),
            "AppSolName",
            """
                1 ApplicantSolicitor Street
                Address Line 2"""
        );
    }

    @Test
    void givenRespondentRecipient_whenGetAddressee_thenReturnRespondentAddressee() {
        stubCaseDetailsWithCaseDataWithContactDetailsWrapper();
        when(caseData.isRespondentRepresentedByASolicitor()).thenReturn(false);
        when(caseData.getRespondentFullName()).thenReturn("Respondent Name");
        when(contactDetailsWrapper.getRespondentAddress()).thenReturn(
            address("1 Respondent Street", "Address Line 2", null, null, null));

        assertAddressee(
            AddresseeGeneratorUtils.generateAddressee(caseDetails, DocumentHelper.PaperNotificationRecipient.RESPONDENT),
            "Respondent Name",
            """
                1 Respondent Street
                Address Line 2"""
        );
    }

    @Test
    void givenIntervenerOneRecipient_whenGetAddressee_thenReturnIntervenerAddressee() {
        when(caseDetails.getData()).thenReturn(caseData);
        when(caseData.getIntervenerOne()).thenReturn(intervenerOne);
        when(intervenerOne.getIntervenerName()).thenReturn("Intervener Name");
        when(intervenerOne.getIntervenerAddress()).thenReturn(
            address("1 Intervener Street", "Address Line 2", null, null, "SW1 1AA"));

        assertAddressee(
            AddresseeGeneratorUtils.generateAddressee(caseDetails, DocumentHelper.PaperNotificationRecipient.INTERVENER_ONE),
            "Intervener Name",
            """
                1 Intervener Street
                Address Line 2
                SW1 1AA"""
        );
    }

    @Test
    void givenIntervenerTwoRecipient_whenGetAddressee_thenReturnIntervenerAddressee() {
        when(caseDetails.getData()).thenReturn(caseData);
        when(caseData.getIntervenerTwo()).thenReturn(intervenerTwo);
        when(intervenerTwo.getIntervenerName()).thenReturn("Intervener Name");
        when(intervenerTwo.getIntervenerAddress()).thenReturn(
            address("2 Intervener Street", "Address Line 2", null, null, "SW1 1AA"));

        assertAddressee(
            AddresseeGeneratorUtils.generateAddressee(caseDetails, DocumentHelper.PaperNotificationRecipient.INTERVENER_TWO),
            "Intervener Name",
            """
                2 Intervener Street
                Address Line 2
                SW1 1AA"""
        );
    }

    @Test
    void givenIntervenerThreeRecipient_whenGetAddressee_thenReturnIntervenerAddressee() {
        when(caseDetails.getData()).thenReturn(caseData);
        when(caseData.getIntervenerThree()).thenReturn(intervenerThree);
        when(intervenerThree.getIntervenerName()).thenReturn("Intervener Name");
        when(intervenerThree.getIntervenerAddress()).thenReturn(
            address("3 Intervener Street", "Address Line 2", null, null, "SW1 1AA"));

        assertAddressee(
            AddresseeGeneratorUtils.generateAddressee(caseDetails, DocumentHelper.PaperNotificationRecipient.INTERVENER_THREE),
            "Intervener Name",
            """
                3 Intervener Street
                Address Line 2
                SW1 1AA"""
        );
    }

    @Test
    void givenIntervenerFourRecipient_whenGetAddressee_thenReturnIntervenerAddressee() {
        when(caseDetails.getData()).thenReturn(caseData);
        when(caseData.getIntervenerFour()).thenReturn(intervenerFour);
        when(intervenerFour.getIntervenerName()).thenReturn("Intervener Name");
        when(intervenerFour.getIntervenerAddress()).thenReturn(
            address("4 Intervener Street", "Address Line 2", null, null, "SW1 1AA"));

        assertAddressee(
            AddresseeGeneratorUtils.generateAddressee(caseDetails, DocumentHelper.PaperNotificationRecipient.INTERVENER_FOUR),
            "Intervener Name",
            """
                4 Intervener Street
                Address Line 2
                SW1 1AA"""
        );
    }

    @Test
    void givenRespondentRecipientIsNotInternational_whenGetAddressee_thenReturnRespondentAddresseeWithoutCountry() {
        stubCaseDetailsWithCaseDataWithContactDetailsWrapper();
        when(caseData.isRespondentRepresentedByASolicitor()).thenReturn(false);
        when(caseData.getRespondentFullName()).thenReturn("Respondent Name");
        when(contactDetailsWrapper.getRespondentAddress()).thenReturn(
            address("1 Applicant Street", "Address Line 2", "County", "Country", "SW1 1AA"));

        assertAddressee(
            AddresseeGeneratorUtils.generateAddressee(caseDetails, DocumentHelper.PaperNotificationRecipient.RESPONDENT),
            "Respondent Name",
            """
                1 Applicant Street
                Address Line 2
                County
                SW1 1AA"""
        );
    }

    @Test
    void givenRespondentSolicitorRecipient_whenGetAddressee_thenReturnRespondentSolicitorAddressee() {
        stubCaseDetailsWithCaseDataWithContactDetailsWrapper();
        when(caseData.isRespondentRepresentedByASolicitor()).thenReturn(true);
        when(caseData.getRespondentSolicitorName()).thenReturn("RespSol Name");
        when(contactDetailsWrapper.getRespondentSolicitorAddress()).thenReturn(
            address("1 RespondentSolicitor Street", "Address Line 2", "County", null, "SW1 1AA"));

        assertAddressee(
            AddresseeGeneratorUtils.generateAddressee(caseDetails, DocumentHelper.PaperNotificationRecipient.RESPONDENT),
            "RespSol Name",
            """
                1 RespondentSolicitor Street
                Address Line 2
                County
                SW1 1AA"""
        );
    }

    @Test
    void givenAddress_whenFormatForLetterPrinting_thenFormat() {
        Address toFormat = address("Line1", "Line2", "County", null, null);

        assertEquals("""
            Line1
            Line2
            County""", AddresseeGeneratorUtils.formatAddressForLetterPrinting(toFormat, false));
    }

    @Test
    void givenValidCaseData_whenGetAddressToCaseDataMapping_thenReturnCorrectMapping() {
        when(caseData.getContactDetailsWrapper()).thenReturn(contactDetailsWrapper);
        when(caseData.getGeneralLetterWrapper()).thenReturn(generalLetterWrapper);

        when(caseData.getAppSolicitorAddress()).thenReturn(
            address("50 App Sol Street", null, null, null, null));
        when(contactDetailsWrapper.getRespondentSolicitorAddress()).thenReturn(
            address("50 Resp Sol Street", null, null, null, null));
        when(contactDetailsWrapper.getRespondentAddress()).thenReturn(null);
        when(caseData.getAppSolicitorName()).thenReturn("AppSolName");
        when(caseData.getRespondentSolicitorName()).thenReturn("RespSolName");
        when(caseData.getRespondentFullName()).thenReturn("");
        when(generalLetterWrapper.getGeneralLetterRecipientAddress()).thenReturn(null);
        when(generalLetterWrapper.getGeneralLetterRecipient()).thenReturn("");

        Map<String, Map<GeneralLetterAddressToType, ?>> addressToCaseDataMapping =
            AddresseeGeneratorUtils.getAddressToCaseDataMapping(caseData);

        Map<GeneralLetterAddressToType, Address> addressMap =
            (Map<GeneralLetterAddressToType, Address>) addressToCaseDataMapping.get(ADDRESS_MAP);

        assertEquals("50 App Sol Street", addressMap.get(GeneralLetterAddressToType.APPLICANT_SOLICITOR).getAddressLine1());
        assertEquals("50 Resp Sol Street", addressMap.get(GeneralLetterAddressToType.RESPONDENT_SOLICITOR).getAddressLine1());
        assertEquals(new Address(), addressMap.get(GeneralLetterAddressToType.RESPONDENT));

        Map<GeneralLetterAddressToType, String> nameMap =
            (Map<GeneralLetterAddressToType, String>) addressToCaseDataMapping.get(NAME_MAP);

        assertEquals("AppSolName", nameMap.get(GeneralLetterAddressToType.APPLICANT_SOLICITOR));
        assertEquals("RespSolName", nameMap.get(GeneralLetterAddressToType.RESPONDENT_SOLICITOR));
        assertEquals("", nameMap.get(GeneralLetterAddressToType.RESPONDENT));
        assertEquals("", nameMap.get(GeneralLetterAddressToType.OTHER));
    }

    private void stubCaseDetailsWithCaseDataWithContactDetailsWrapper() {
        when(caseDetails.getData()).thenReturn(caseData);
        when(caseData.getContactDetailsWrapper()).thenReturn(contactDetailsWrapper);
    }

    private Address address(String line1, String line2, String county, String country, String postCode) {
        return Address.builder()
            .addressLine1(line1)
            .addressLine2(line2)
            .county(county)
            .country(country)
            .postCode(postCode)
            .build();
    }

    private void assertAddressee(Addressee addressee, String expectedName, String expectedAddress) {
        assertEquals(expectedName, addressee.getName());
        assertEquals(expectedAddress, addressee.getFormattedAddress());
    }
}
