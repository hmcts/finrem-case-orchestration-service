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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_REFERENCE;

@RunWith(MockitoJUnitRunner.class)
public class RespondentAddresseeGeneratorTest {

    RespondentLetterAddresseeGenerator respondentAddresseeGenerator;

    @Mock
    CaseDataService caseDataService;

    @org.junit.Before
    public void setUp() throws Exception {
        respondentAddresseeGenerator = new RespondentLetterAddresseeGenerator(caseDataService);
    }

    @Test
    public void shouldGenerateAddresseeFromSolictorAddressWhenRepresentedAndContested() {
        CaseDetails contestedCaseDetails = getCaseDetails();
        Map<String, Object> solicitorAddress = buildAddress("123 Respondent Solicitor Street");
        contestedCaseDetails.getData().put(RESP_SOLICITOR_ADDRESS, solicitorAddress);
        contestedCaseDetails.getData().put(RESP_SOLICITOR_EMAIL, TEST_RESP_SOLICITOR_EMAIL);
        contestedCaseDetails.getData().put(RESP_SOLICITOR_NAME, TEST_RESP_SOLICITOR_NAME);
        contestedCaseDetails.getData().put(RESP_SOLICITOR_REFERENCE, TEST_SOLICITOR_REFERENCE);

        when(caseDataService.isConsentedApplication(contestedCaseDetails)).thenReturn(Boolean.FALSE);
        when(caseDataService.isRespondentRepresentedByASolicitor(contestedCaseDetails.getData())).thenReturn(Boolean.TRUE);

        AddresseeDetails addresseeDetails = respondentAddresseeGenerator.generate(contestedCaseDetails);

        assertEquals(TEST_RESP_SOLICITOR_NAME, addresseeDetails.getAddresseeName());
        assertEquals("123 Respondent Solicitor Street", addresseeDetails.getAddressToSendTo().get("AddressLine1"));
        assertOtherAddressDetails(addresseeDetails);

    }

    @Test
    public void shouldGenerateAddresseeFromSolictorAddressWhenRepresentedAndContestedFinrem() {
        FinremCaseDetails contestedCaseDetails = getFinremCaseDetails(CaseType.CONTESTED);
        Address solicitorAddress = buildFinremAddress("123 Respondent Solicitor Street");

        contestedCaseDetails.getData().getContactDetailsWrapper().setRespondentSolicitorAddress(solicitorAddress);
        contestedCaseDetails.getData().getContactDetailsWrapper().setSolicitorEmail(TEST_RESP_SOLICITOR_EMAIL);
        contestedCaseDetails.getData().getContactDetailsWrapper().setRespondentSolicitorName(TEST_RESP_SOLICITOR_NAME);
        contestedCaseDetails.getData().getContactDetailsWrapper().setRespondentSolicitorReference(TEST_RESP_SOLICITOR_REFERENCE);
        contestedCaseDetails.getData().getContactDetailsWrapper().setContestedRespondentRepresented(YesOrNo.YES);

        AddresseeDetails addresseeDetails = respondentAddresseeGenerator.generate(contestedCaseDetails);

        assertEquals(TEST_RESP_SOLICITOR_NAME, addresseeDetails.getAddresseeName());
        assertEquals("123 Respondent Solicitor Street", addresseeDetails.getFinremAddressToSendTo().getAddressLine1());
        assertOtherAddressDetailsFinrem(addresseeDetails);

    }

    private static void assertOtherAddressDetailsFinrem(AddresseeDetails addresseeDetails) {
        assertEquals("Second Address Line", addresseeDetails.getFinremAddressToSendTo().getAddressLine2());
        assertEquals("Third Address Line", addresseeDetails.getFinremAddressToSendTo().getAddressLine3());
        assertEquals("London", addresseeDetails.getFinremAddressToSendTo().getCounty());
        assertEquals("England", addresseeDetails.getFinremAddressToSendTo().getCountry());
        assertEquals("London", addresseeDetails.getFinremAddressToSendTo().getPostTown());
        assertEquals("SE1", addresseeDetails.getFinremAddressToSendTo().getPostCode());
    }

    @Test
    public void shouldGenerateAddresseeFromRespondentAddressWhenNotRepresented() {
        CaseDetails contestedCaseDetails = getCaseDetails();
        Map<String, Object> respondentAddress = buildAddress("123 Respondent Street");
        contestedCaseDetails.getData().put(RESPONDENT_ADDRESS, respondentAddress);

        when(caseDataService.isConsentedApplication(contestedCaseDetails)).thenReturn(Boolean.FALSE);
        when(caseDataService.isRespondentRepresentedByASolicitor(contestedCaseDetails.getData())).thenReturn(Boolean.FALSE);
        String respondentFullName = CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME + " " + CONTESTED_RESPONDENT_LAST_NAME;
        when(caseDataService.buildFullName(anyMap(), anyString(), anyString())).thenReturn(respondentFullName);
        AddresseeDetails addresseeDetails = respondentAddresseeGenerator.generate(contestedCaseDetails);

        assertEquals(respondentFullName, addresseeDetails.getAddresseeName());
        assertEquals("123 Respondent Street", addresseeDetails.getAddressToSendTo().get("AddressLine1"));
        assertOtherAddressDetails(addresseeDetails);

    }

    @Test
    public void shouldGenerateAddresseeFromApplicantAddressWhenNotRepresentedFinrem() {
        FinremCaseDetails contestedCaseDetails = getFinremCaseDetails(CaseType.CONTESTED);
        Address respondentAddress = buildFinremAddress("123 Respondent Street");
        contestedCaseDetails.getData().getContactDetailsWrapper().setRespondentAddress(respondentAddress);
        contestedCaseDetails.getData().getContactDetailsWrapper().setRespondentFmName(CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME);
        contestedCaseDetails.getData().getContactDetailsWrapper().setRespondentLname(CONTESTED_RESPONDENT_LAST_NAME);
        contestedCaseDetails.getData().getContactDetailsWrapper().setContestedRespondentRepresented(YesOrNo.NO);

        AddresseeDetails addresseeDetails = respondentAddresseeGenerator.generate(contestedCaseDetails);
        String respondentFullName = CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME + " " + CONTESTED_RESPONDENT_LAST_NAME;
        assertEquals(respondentFullName, addresseeDetails.getAddresseeName());
        assertEquals("123 Respondent Street", addresseeDetails.getFinremAddressToSendTo().getAddressLine1());
        assertOtherAddressDetailsFinrem(addresseeDetails);

    }

    @Test
    public void shouldGenerateAddresseeFromSolictorAddressWhenRepresentedAndConsented() {
        CaseDetails consentedCaseDetails = getCaseDetails();
        Map<String, Object> solicitorAddress = buildAddress("123 Consented Respondent Solicitor Street");
        consentedCaseDetails.getData().put(RESP_SOLICITOR_NAME, TEST_RESP_SOLICITOR_NAME);
        consentedCaseDetails.getData().put(RESP_SOLICITOR_ADDRESS, solicitorAddress);
        consentedCaseDetails.getData().put(RESP_SOLICITOR_EMAIL, TEST_RESP_SOLICITOR_EMAIL);
        consentedCaseDetails.getData().put(SOLICITOR_REFERENCE, TEST_SOLICITOR_REFERENCE);

        when(caseDataService.isConsentedApplication(consentedCaseDetails)).thenReturn(Boolean.TRUE);
        when(caseDataService.isRespondentRepresentedByASolicitor(consentedCaseDetails.getData())).thenReturn(Boolean.TRUE);

        AddresseeDetails addresseeDetails = respondentAddresseeGenerator.generate(consentedCaseDetails);


        assertEquals(TEST_RESP_SOLICITOR_NAME, addresseeDetails.getAddresseeName());
        assertEquals("123 Consented Respondent Solicitor Street", addresseeDetails.getAddressToSendTo().get("AddressLine1"));
        assertOtherAddressDetails(addresseeDetails);

    }

    private static void assertOtherAddressDetails(AddresseeDetails addresseeDetails) {
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
        return CaseDetails.builder()
            .id(12345L)
            .data(caseData)
            .build();
    }

    protected FinremCaseDetails getFinremCaseDetails(CaseType caseType) {
        return FinremCaseDetails.builder()
            .data(FinremCaseData.builder()
                .ccdCaseType(caseType)
                .contactDetailsWrapper(
                    ContactDetailsWrapper.builder()
                        .respondentLname("Victoria")
                        .applicantLname("Goodman")
                        .build())
                .build())
            .caseType(caseType)
            .id(12345L)
            .build();
    }

}
