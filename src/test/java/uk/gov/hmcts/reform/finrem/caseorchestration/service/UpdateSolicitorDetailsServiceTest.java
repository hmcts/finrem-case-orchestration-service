package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.OrganisationContactInformation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.OrganisationsResponse;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_DX_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_FIRM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_DX_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_FIRM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_DX_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_FIRM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_PHONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_PHONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_REFERENCE;

public class UpdateSolicitorDetailsServiceTest extends BaseServiceTest {

    public static final String ADDRESS_LINE_1 = "addressLine1";
    public static final String ADDRESS_LINE_2 = "addressLine2";
    public static final String ADDRESS_LINE_3 = "addressLine3";
    public static final String COUNTY = "county";
    public static final String COUNTRY = "country";
    public static final String TOWN_CITY = "townCity";
    public static final String POSTCODE = "postCode";

    @Autowired
    UpdateSolicitorDetailsService updateSolicitorDetailsService;
    @MockBean
    PrdOrganisationService prdOrganisationService;
    @MockBean
    CaseDataService caseDataService;

    OrganisationContactInformation organisationContactInformation = OrganisationContactInformation.builder()
        .addressLine1(ADDRESS_LINE_1)
        .addressLine2(ADDRESS_LINE_2)
        .addressLine3(ADDRESS_LINE_3)
        .county(COUNTY)
        .country(COUNTRY)
        .townCity(TOWN_CITY)
        .postcode(POSTCODE)
        .build();

    @Before
    public void setUp() {
        when(caseDataService.isContestedApplication(any())).thenReturn(true);
        when(prdOrganisationService.retrieveOrganisationsData(eq(AUTH_TOKEN))).thenReturn(OrganisationsResponse.builder()
            .contactInformation(Arrays.asList(organisationContactInformation))
            .name(TEST_SOLICITOR_NAME)
            .organisationIdentifier(TEST_SOLICITOR_REFERENCE)
            .build());
    }

    @Test
    public void shouldSuccessfullySetApplicantSolicitorOrganisationDetailsContested() {
        CaseDetails caseDetails = buildCaseDetails();

        updateSolicitorDetailsService.setApplicantSolicitorOrganisationDetails(AUTH_TOKEN, caseDetails);

        Map<String, Object> addressMap = (Map<String, Object>) caseDetails.getData().get(CONTESTED_SOLICITOR_ADDRESS);

        Assert.assertEquals(addressMap.get("AddressLine1"), organisationContactInformation.getAddressLine1());
        Assert.assertEquals(addressMap.get("AddressLine2"), organisationContactInformation.getAddressLine2());
        Assert.assertEquals(addressMap.get("AddressLine3"), organisationContactInformation.getAddressLine3());
        Assert.assertEquals(addressMap.get("County"), organisationContactInformation.getCounty());
        Assert.assertEquals(addressMap.get("Country"), organisationContactInformation.getCountry());
        Assert.assertEquals(addressMap.get("PostTown"), organisationContactInformation.getTownCity());
        Assert.assertEquals(addressMap.get("PostCode"), organisationContactInformation.getPostcode());
        Assert.assertEquals(caseDetails.getData().get(CONTESTED_SOLICITOR_FIRM), TEST_SOLICITOR_NAME);
        Assert.assertEquals(caseDetails.getData().get(SOLICITOR_REFERENCE), TEST_SOLICITOR_REFERENCE);
    }


    @Test
    public void shouldSuccessfullyConvertOrganisationAddress() {

        when(prdOrganisationService.findOrganisationByOrgId("organisationId")).thenReturn(OrganisationsResponse.builder()
            .contactInformation(Arrays.asList(organisationContactInformation))
            .name(TEST_SOLICITOR_NAME)
            .organisationIdentifier(TEST_SOLICITOR_REFERENCE)
            .build());

        Map<String, Object> addressMap = updateSolicitorDetailsService.convertOrganisationAddressToSolicitorAddress("organisationId");


        Assert.assertEquals(addressMap.get("AddressLine1"), organisationContactInformation.getAddressLine1());
        Assert.assertEquals(addressMap.get("AddressLine2"), organisationContactInformation.getAddressLine2());
        Assert.assertEquals(addressMap.get("AddressLine3"), organisationContactInformation.getAddressLine3());
        Assert.assertEquals(addressMap.get("County"), organisationContactInformation.getCounty());
        Assert.assertEquals(addressMap.get("Country"), organisationContactInformation.getCountry());
        Assert.assertEquals(addressMap.get("PostTown"), organisationContactInformation.getTownCity());
        Assert.assertEquals(addressMap.get("PostCode"), organisationContactInformation.getPostcode());

    }

    @Test
    public void shouldSuccessfullySetApplicantSolicitorOrganisationDetailsConsented() {
        CaseDetails caseDetails = buildCaseDetails();

        when(caseDataService.isContestedApplication(caseDetails)).thenReturn(false);

        updateSolicitorDetailsService.setApplicantSolicitorOrganisationDetails(AUTH_TOKEN, caseDetails);

        Map<String, Object> addressMap = (Map<String, Object>) caseDetails.getData().get(CONSENTED_SOLICITOR_ADDRESS);

        Assert.assertEquals(addressMap.get("AddressLine1"), organisationContactInformation.getAddressLine1());
        Assert.assertEquals(addressMap.get("AddressLine2"), organisationContactInformation.getAddressLine2());
        Assert.assertEquals(addressMap.get("AddressLine3"), organisationContactInformation.getAddressLine3());
        Assert.assertEquals(addressMap.get("County"), organisationContactInformation.getCounty());
        Assert.assertEquals(addressMap.get("Country"), organisationContactInformation.getCountry());
        Assert.assertEquals(addressMap.get("PostTown"), organisationContactInformation.getTownCity());
        Assert.assertEquals(addressMap.get("PostCode"), organisationContactInformation.getPostcode());
        Assert.assertEquals(caseDetails.getData().get(CONSENTED_SOLICITOR_FIRM), TEST_SOLICITOR_NAME);
        Assert.assertEquals(caseDetails.getData().get(SOLICITOR_REFERENCE), TEST_SOLICITOR_REFERENCE);
    }

    @Test
    public void shouldNotSetApplicantSolicitorOrganisationDetails_orgRespNull() {
        when(prdOrganisationService.retrieveOrganisationsData(eq(AUTH_TOKEN))).thenReturn(null);

        CaseDetails caseDetails = buildCaseDetails();

        updateSolicitorDetailsService.setApplicantSolicitorOrganisationDetails(AUTH_TOKEN, caseDetails);

        Assert.assertFalse(caseDetails.getData().containsKey(CONTESTED_SOLICITOR_ADDRESS));
        Assert.assertFalse(caseDetails.getData().containsKey(CONTESTED_SOLICITOR_FIRM));
        Assert.assertFalse(caseDetails.getData().containsKey(SOLICITOR_REFERENCE));
    }

    @Test
    public void shouldSetNewApplicantSolicitorDetailsContested() {

        Map<String, Object> caseData = new HashMap<>();

        ChangedRepresentative addedSolicitor = ChangedRepresentative.builder()
            .name("Sir Solicitor")
            .email("sirsolicitor1@gmail.com")
            .organisation(Organisation.builder()
                .organisationID("A31PTVA")
                .organisationName("FRApplicantSolicitorFirm")
                .build())
            .build();

        boolean isConsented = false;
        boolean isApplicant = true;

        caseData = updateSolicitorDetailsService.updateSolicitorContactDetails(addedSolicitor,
            caseData,
            isConsented,
            isApplicant);

        assertEquals(caseData.get(CONTESTED_SOLICITOR_NAME), "Sir Solicitor");
        assertEquals(caseData.get(CONTESTED_SOLICITOR_EMAIL), "sirsolicitor1@gmail.com");
        assertEquals(caseData.get(CONTESTED_SOLICITOR_FIRM), "FRApplicantSolicitorFirm");
    }

    @Test
    public void shouldSetNewApplicantSolicitorDetailsConsented() {

        Map<String, Object> caseData = new HashMap<>();

        ChangedRepresentative addedSolicitor = ChangedRepresentative.builder()
            .name("Sir Solicitor")
            .email("sirsolicitor1@gmail.com")
            .organisation(Organisation.builder()
                .organisationID("A31PTVA")
                .organisationName("FRApplicantSolicitorFirm")
                .build())
            .build();

        boolean isConsented = true;
        boolean isApplicant = true;

        caseData = updateSolicitorDetailsService.updateSolicitorContactDetails(addedSolicitor,
            caseData,
            isConsented,
            isApplicant);

        assertEquals(caseData.get(CONSENTED_SOLICITOR_NAME), "Sir Solicitor");
        assertEquals(caseData.get(SOLICITOR_EMAIL), "sirsolicitor1@gmail.com");
        assertEquals(caseData.get(CONSENTED_SOLICITOR_FIRM), "FRApplicantSolicitorFirm");
    }

    @Test
    public void shouldSetNewRespondentSolicitorDetails() {

        Map<String, Object> caseData = new HashMap<>();

        ChangedRepresentative addedSolicitor = ChangedRepresentative.builder()
            .name("Sir Solicitor")
            .email("sirsolicitor1@gmail.com")
            .organisation(Organisation.builder()
                .organisationID("A31PTVA")
                .organisationName("FRRespondentSolicitorFirm")
                .build())
            .build();

        boolean isConsented = true;
        boolean isApplicant = false;

        caseData = updateSolicitorDetailsService.updateSolicitorContactDetails(addedSolicitor,
            caseData,
            isConsented,
            isApplicant);

        assertEquals(caseData.get(RESP_SOLICITOR_NAME), "Sir Solicitor");
        assertEquals(caseData.get(RESP_SOLICITOR_EMAIL), "sirsolicitor1@gmail.com");
        assertEquals(caseData.get(RESP_SOLICITOR_FIRM), "FRRespondentSolicitorFirm");

    }

    @Test
    public void shouldRemoveConsentedAppSolicitorDetails() {
        Map<String, Object> caseData = new HashMap<>();

        caseData.put(SOLICITOR_PHONE, "123456789");
        caseData.put(CONSENTED_SOLICITOR_DX_NUMBER, "DummyDX");
        caseData.put(APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONSENTED, YES_VALUE);
        boolean consentedCase = true;
        boolean isApplicant = true;

        updateSolicitorDetailsService.removeSolicitorFields(caseData, consentedCase, isApplicant);

        assertFalse(caseData.containsKey(SOLICITOR_PHONE));
        assertFalse(caseData.containsKey(CONSENTED_SOLICITOR_DX_NUMBER));
        assertFalse(caseData.containsKey(APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONSENTED));

    }

    @Test
    public void shouldRemoveContestedAppSolicitorDetails() {
        Map<String, Object> caseData = new HashMap<>();

        caseData.put(SOLICITOR_PHONE, "123456789");
        caseData.put(CONTESTED_SOLICITOR_DX_NUMBER, "DummyDX");
        caseData.put(APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONTESTED, YES_VALUE);
        boolean consentedCase = false;
        boolean isApplicant = true;

        updateSolicitorDetailsService.removeSolicitorFields(caseData, consentedCase, isApplicant);

        assertFalse(caseData.containsKey(SOLICITOR_PHONE));
        assertFalse(caseData.containsKey(CONSENTED_SOLICITOR_DX_NUMBER));
        assertFalse(caseData.containsKey(APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONSENTED));
    }

    @Test
    public void shouldRemoveRespSolicitorDetails() {
        Map<String, Object> caseData = new HashMap<>();

        caseData.put(RESP_SOLICITOR_PHONE, "123456789");
        caseData.put(RESP_SOLICITOR_DX_NUMBER, "DummyDX");
        caseData.put(RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT, YES_VALUE);
        boolean consentedCase = true;
        boolean isApplicant = false;

        updateSolicitorDetailsService.removeSolicitorFields(caseData, consentedCase, isApplicant);

        assertFalse(caseData.containsKey(RESP_SOLICITOR_PHONE));
        assertFalse(caseData.containsKey(CONSENTED_SOLICITOR_DX_NUMBER));
        assertFalse(caseData.containsKey(APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONSENTED));
    }
}
