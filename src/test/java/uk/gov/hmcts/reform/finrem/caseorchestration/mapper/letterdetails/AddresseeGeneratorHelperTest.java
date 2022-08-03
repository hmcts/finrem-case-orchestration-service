package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails;

import org.junit.Test;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.ccd.domain.Address;
import uk.gov.hmcts.reform.finrem.ccd.domain.CaseType;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.GeneralLetterAddressToType;
import uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo;

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
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().caseType(CaseType.CONTESTED).caseData(caseData).build();

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
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().caseType(CaseType.CONTESTED).caseData(caseData).build();

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
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().caseType(CaseType.CONTESTED).caseData(caseData).build();

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
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().caseType(CaseType.CONTESTED).caseData(caseData).build();

        Addressee addressee = AddresseeGeneratorHelper.generateAddressee(caseDetails,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT);

        assertEquals("RespSolName", addressee.getName());
        assertEquals("1 RespondentSolicitor Street\nAddress Line 2", addressee.getFormattedAddress());
    }

    @Test
    public void givenAddress_whenFormatForLetterPrinting_thenFormat() {
        Address toFormat = Address.builder()
            .addressLine1("line1")
            .addressLine2("line2")
            .county("county")
            .build();

        String expectedFormattedAddress = "line1\nline2\ncounty";

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