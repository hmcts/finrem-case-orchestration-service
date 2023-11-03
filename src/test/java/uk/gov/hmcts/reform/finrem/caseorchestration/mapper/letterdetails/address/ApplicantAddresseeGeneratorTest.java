package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.address;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.AddresseeDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_REFERENCE;

@RunWith(MockitoJUnitRunner.class)
public class ApplicantAddresseeGeneratorTest {

    ApplicantLetterAddresseeGenerator applicantAddresseeGenerator;

    @Mock
    CaseDataService caseDataService;

    @org.junit.Before
    public void setUp() throws Exception {
        applicantAddresseeGenerator = new ApplicantLetterAddresseeGenerator(caseDataService);
    }

    @Test
    public void shouldGenerateAddresseeFromSolictorAddressWhenRepresentedAndContested() {
        CaseDetails contestedCaseDetails = getCaseDetails();
        Map<String, Object> solicitorAddress = buildAddress("123 Applicant Solicitor Street");
        contestedCaseDetails.getData().put(CONTESTED_SOLICITOR_ADDRESS, solicitorAddress);
        contestedCaseDetails.getData().put(CONTESTED_SOLICITOR_EMAIL, TEST_SOLICITOR_EMAIL);
        contestedCaseDetails.getData().put(CONTESTED_SOLICITOR_NAME, TEST_SOLICITOR_NAME);
        contestedCaseDetails.getData().put(SOLICITOR_REFERENCE, TEST_SOLICITOR_REFERENCE);

        when(caseDataService.isConsentedApplication(contestedCaseDetails)).thenReturn(Boolean.FALSE);
        when(caseDataService.isApplicantRepresentedByASolicitor(contestedCaseDetails.getData())).thenReturn(Boolean.TRUE);

        AddresseeDetails addresseeDetails = applicantAddresseeGenerator.generate(contestedCaseDetails);

        assertEquals(TEST_SOLICITOR_NAME, addresseeDetails.getAddresseeName());
        assertEquals("123 Applicant Solicitor Street", addresseeDetails.getAddressToSendTo().get("AddressLine1"));
        assertEquals("Second Address Line", addresseeDetails.getAddressToSendTo().get("AddressLine2"));
        assertEquals("Third Address Line", addresseeDetails.getAddressToSendTo().get("AddressLine3"));
        assertEquals("London", addresseeDetails.getAddressToSendTo().get("County"));
        assertEquals("England", addresseeDetails.getAddressToSendTo().get("Country"));
        assertEquals("London", addresseeDetails.getAddressToSendTo().get("PostTown"));
        assertEquals("SE1", addresseeDetails.getAddressToSendTo().get("PostCode"));

    }

    @Test
    public void shouldGenerateAddresseeFromSolictorAddressWhenRepresentedAndContestedFinrem() {
        FinremCaseDetails contestedCaseDetails = getFinremCaseDetails(CaseType.CONTESTED);
        Address solicitorAddress = buildFinremAddress("123 Applicant Solicitor Street");

        contestedCaseDetails.getData().getContactDetailsWrapper().setApplicantSolicitorAddress(solicitorAddress);
        contestedCaseDetails.getData().getContactDetailsWrapper().setSolicitorEmail(TEST_SOLICITOR_EMAIL);
        contestedCaseDetails.getData().getContactDetailsWrapper().setApplicantSolicitorName(TEST_SOLICITOR_NAME);
        contestedCaseDetails.getData().getContactDetailsWrapper().setSolicitorReference(TEST_SOLICITOR_REFERENCE);
        contestedCaseDetails.getData().getContactDetailsWrapper().setApplicantRepresented(YesOrNo.YES);

        AddresseeDetails addresseeDetails = applicantAddresseeGenerator.generate(contestedCaseDetails);

        assertEquals(TEST_SOLICITOR_NAME, addresseeDetails.getAddresseeName());
        assertEquals("123 Applicant Solicitor Street", addresseeDetails.getFinremAddressToSendTo().getAddressLine1());
        assertEquals("Second Address Line", addresseeDetails.getFinremAddressToSendTo().getAddressLine2());
        assertEquals("Third Address Line", addresseeDetails.getFinremAddressToSendTo().getAddressLine3());
        assertEquals("London", addresseeDetails.getFinremAddressToSendTo().getCounty());
        assertEquals("England", addresseeDetails.getFinremAddressToSendTo().getCountry());
        assertEquals("London", addresseeDetails.getFinremAddressToSendTo().getPostTown());
        assertEquals("SE1", addresseeDetails.getFinremAddressToSendTo().getPostCode());

    }

    @Test
    public void shouldGenerateAddresseeFromApplicantAddressWhenNotRepresented() {
        CaseDetails contestedCaseDetails = getCaseDetails();
        Map<String, Object> applicantAddress = buildAddress("123 Applicant Street");
        contestedCaseDetails.getData().put(APPLICANT_ADDRESS, applicantAddress);

        when(caseDataService.isConsentedApplication(contestedCaseDetails)).thenReturn(Boolean.FALSE);
        when(caseDataService.isApplicantRepresentedByASolicitor(contestedCaseDetails.getData())).thenReturn(Boolean.FALSE);
        String applicantFullName = APPLICANT_FIRST_MIDDLE_NAME + " " + APPLICANT_LAST_NAME;
        when(caseDataService.buildFullName(anyMap(), anyString(), anyString())).thenReturn(applicantFullName);
        AddresseeDetails addresseeDetails = applicantAddresseeGenerator.generate(contestedCaseDetails);

        assertEquals(applicantFullName, addresseeDetails.getAddresseeName());
        assertEquals("123 Applicant Street", addresseeDetails.getAddressToSendTo().get("AddressLine1"));
        assertEquals("Second Address Line", addresseeDetails.getAddressToSendTo().get("AddressLine2"));
        assertEquals("Third Address Line", addresseeDetails.getAddressToSendTo().get("AddressLine3"));
        assertEquals("London", addresseeDetails.getAddressToSendTo().get("County"));
        assertEquals("England", addresseeDetails.getAddressToSendTo().get("Country"));
        assertEquals("London", addresseeDetails.getAddressToSendTo().get("PostTown"));
        assertEquals("SE1", addresseeDetails.getAddressToSendTo().get("PostCode"));

    }

    @Test
    public void shouldGenerateAddresseeFromApplicantAddressWhenNotRepresentedFinrem() {
        FinremCaseDetails contestedCaseDetails = getFinremCaseDetails(CaseType.CONTESTED);
        Address applicantAddress = buildFinremAddress("123 Applicant Street");
        contestedCaseDetails.getData().getContactDetailsWrapper().setApplicantAddress(applicantAddress);
        contestedCaseDetails.getData().getContactDetailsWrapper().setApplicantFmName(APPLICANT_FIRST_MIDDLE_NAME);
        contestedCaseDetails.getData().getContactDetailsWrapper().setApplicantLname(APPLICANT_LAST_NAME);
        contestedCaseDetails.getData().getContactDetailsWrapper().setApplicantRepresented(YesOrNo.NO);

        AddresseeDetails addresseeDetails = applicantAddresseeGenerator.generate(contestedCaseDetails);
        String applicantFullName = APPLICANT_FIRST_MIDDLE_NAME + " " + APPLICANT_LAST_NAME;
        assertEquals(applicantFullName, addresseeDetails.getAddresseeName());
        assertEquals("123 Applicant Street", addresseeDetails.getFinremAddressToSendTo().getAddressLine1());
        assertEquals("Second Address Line", addresseeDetails.getFinremAddressToSendTo().getAddressLine2());
        assertEquals("Third Address Line", addresseeDetails.getFinremAddressToSendTo().getAddressLine3());
        assertEquals("London", addresseeDetails.getFinremAddressToSendTo().getCounty());
        assertEquals("England", addresseeDetails.getFinremAddressToSendTo().getCountry());
        assertEquals("London", addresseeDetails.getFinremAddressToSendTo().getPostTown());
        assertEquals("SE1", addresseeDetails.getFinremAddressToSendTo().getPostCode());

    }

    @Test
    public void shouldGenerateAddresseeFromSolictorAddressWhenRepresentedAndConsented() {
        CaseDetails consentedCaseDetails = getCaseDetails();
        Map<String, Object> solicitorAddress = buildAddress("123 Consented Applicant Solicitor Street");
        consentedCaseDetails.getData().put(CONSENTED_SOLICITOR_ADDRESS, solicitorAddress);
        consentedCaseDetails.getData().put(SOLICITOR_EMAIL, TEST_SOLICITOR_EMAIL);
        consentedCaseDetails.getData().put(CONSENTED_SOLICITOR_NAME, TEST_SOLICITOR_NAME);
        consentedCaseDetails.getData().put(SOLICITOR_REFERENCE, TEST_SOLICITOR_REFERENCE);

        when(caseDataService.isConsentedApplication(consentedCaseDetails)).thenReturn(Boolean.TRUE);
        when(caseDataService.isApplicantRepresentedByASolicitor(consentedCaseDetails.getData())).thenReturn(Boolean.TRUE);

        AddresseeDetails addresseeDetails = applicantAddresseeGenerator.generate(consentedCaseDetails);


        assertEquals(TEST_SOLICITOR_NAME, addresseeDetails.getAddresseeName());
        assertEquals("123 Consented Applicant Solicitor Street", addresseeDetails.getAddressToSendTo().get("AddressLine1"));
        assertEquals("Second Address Line", addresseeDetails.getAddressToSendTo().get("AddressLine2"));
        assertEquals("Third Address Line", addresseeDetails.getAddressToSendTo().get("AddressLine3"));
        assertEquals("London", addresseeDetails.getAddressToSendTo().get("County"));
        assertEquals("England", addresseeDetails.getAddressToSendTo().get("Country"));
        assertEquals("London", addresseeDetails.getAddressToSendTo().get("PostTown"));
        assertEquals("SE1", addresseeDetails.getAddressToSendTo().get("PostCode"));

    }

    private static Map<String, Object> buildAddress(String addressLine1) {
        Map<String, Object> solicitorAddress = new HashMap<>();
        solicitorAddress.put("AddressLine1", addressLine1);
        solicitorAddress.put("AddressLine2", "Second Address Line");
        solicitorAddress.put("AddressLine3", "Third Address Line");
        solicitorAddress.put("County", "London");
        solicitorAddress.put("Country", "England");
        solicitorAddress.put("PostTown", "London");
        solicitorAddress.put("PostCode", "SE1");
        return solicitorAddress;
    }

    private static Address buildFinremAddress(String addressLine1) {
        return Address.builder()
            .addressLine1(addressLine1).addressLine2("Second Address Line")
            .addressLine3("Third Address Line")
            .county("London")
            .country("England")
            .postTown("London")
            .postCode("SE1")
            .build();
    }

    protected CaseDetails getCaseDetails() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(APPLICANT_FIRST_MIDDLE_NAME, "Victoria");
        caseData.put(APPLICANT_LAST_NAME, "Goodman");
        return CaseDetails.builder()
            .id(12345L)
            .data(caseData)
            .build();
    }

    protected FinremCaseDetails getFinremCaseDetails(CaseType caseType) {
        return FinremCaseDetails.builder()
            .data(FinremCaseData.builder()
                .contactDetailsWrapper(
                    ContactDetailsWrapper.builder()
                        .applicantFmName("Victoria")
                        .applicantLname("Goodman")
                        .build())
                .build())
            .caseType(caseType)
            .id(12345L)
            .build();
    }

}
