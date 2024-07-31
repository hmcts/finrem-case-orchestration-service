package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.solicitorcreatecase.SolicitorCreateContestedAboutToSubmitHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnlineFormDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.UpdateRepresentationWorkflowService;

import java.io.InputStream;
import java.util.LinkedHashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;

@ExtendWith(MockitoExtension.class)
public class UpdateContactDetailsContestedSubmittedHandlerTest extends BaseHandlerTestSetup {
    public static final String AUTH_TOKEN = "tokien:)";

    private static final String FIXTURES_CONTESTED_AMEND_APPLICANT_SOLICITOR_DETAILS_JSON = "/fixtures/contested/amend-applicant-solicitor-details.json";
    private static final String FIXTURES_CONTESTED_AMEND_APPLICANT_SOLICITOR_DETAILS_RES_UNTOUCHED_JSON = "/fixtures/contested/amend-applicant-solicitor-details-res-untouched.json";
    private static final String FIXTURES_CONTESTED_AMEND_APPLICANT_SOLICITOR_DETAILS_APP_UNTOUCHED_JSON = "/fixtures/contested/amend-applicant-solicitor-details-app-untouched.json";
    private static final String FIXTURES_CONTESTED_AMEND_APPLICANT_SOLICITOR_DETAILS_BOTH_UNTOUCHED_JSON = "/fixtures/contested/amend-applicant-solicitor-details-both-untouched.json";

    @InjectMocks
    private UpdateContactDetailsContestedSubmittedHandler handler;

    @Mock
    private UpdateRepresentationWorkflowService updateRepresentationWorkflowService;

    @Mock
    private OnlineFormDocumentService service;

    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;


    @Before
    public void setup() {
        handler = new UpdateContactDetailsContestedSubmittedHandler(
            finremCaseDetailsMapper,
            updateRepresentationWorkflowService,
            service);
    }


    @Test
    void givenACcdCallbackUpdateContactDetailsContestCase_WhenCanHandleCalled_thenHandlerCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.UPDATE_CONTACT_DETAILS),
            is(true));
    }

    @Test
    void shouldSuccessfullyRemoveApplicantSolicitorDetails() {
        when(service.generateDraftContestedMiniFormA(anyString(),
            any(FinremCaseDetails.class))).thenReturn(caseDocument());

        FinremCallbackRequest finremCallbackRequest = buildCaseDetailsWithPath(FIXTURES_CONTESTED_AMEND_APPLICANT_SOLICITOR_DETAILS_JSON);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);
        assertEquals(false, response.getData().isApplicantRepresentedByASolicitor());
        assertNotNull(response.getData().getContactDetailsWrapper().getApplicantAddress());
        assertEquals("89897876765", response.getData().getContactDetailsWrapper().getApplicantPhone());
        assertEquals("email01@email.com", response.getData().getContactDetailsWrapper().getApplicantEmail());
        assertEquals("Poor", response.getData().getContactDetailsWrapper().getApplicantFmName());
        assertEquals("Guy", response.getData().getContactDetailsWrapper().getApplicantLname());
        assertEquals(true, response.getData().isAppAddressConfidential());
        assertEquals(false, response.getData().isRespAddressConfidential());
        assertEquals(caseDocument(), response.getData().getMiniFormA());
        assertNull(response.getData().getAppSolicitorName());
        assertNull(response.getData().getAppSolicitorFirm());
        assertNull(response.getData().getContactDetailsWrapper().getSolicitorReference());
        assertNull(response.getData().getContactDetailsWrapper().getSolicitorAddress());
        assertNull(response.getData().getContactDetailsWrapper().getApplicantSolicitorPhone());
        assertNull(response.getData().getContactDetailsWrapper().getApplicantSolicitorEmail());
        assertNull(response.getData().getContactDetailsWrapper().getApplicantSolicitorDxNumber());
        assertNull(response.getData().getContactDetailsWrapper().getApplicantSolicitorConsentForEmails());
        verify(service).generateContestedMiniFormA(any(), any());
    }

    @Test
    void shouldSuccessfullyRemoveApplicantSolicitorDetails_respondentConfidentialAddressNotAmended() {
        FinremCallbackRequest finremCallbackRequest = buildCaseDetailsWithPath(FIXTURES_CONTESTED_AMEND_APPLICANT_SOLICITOR_DETAILS_RES_UNTOUCHED_JSON);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);
        assertEquals(false, response.getData().isApplicantRepresentedByASolicitor());
        assertNotNull(response.getData().getContactDetailsWrapper().getApplicantAddress());
        assertEquals("89897876765", response.getData().getContactDetailsWrapper().getApplicantPhone());
        assertEquals("email01@email.com", response.getData().getContactDetailsWrapper().getApplicantEmail());
        assertEquals("Poor", response.getData().getContactDetailsWrapper().getApplicantFmName());
        assertEquals("Guy", response.getData().getContactDetailsWrapper().getApplicantLname());
        assertEquals(true, response.getData().isAppAddressConfidential());
        assertEquals(false, response.getData().isRespAddressConfidential());
        assertEquals(LinkedHashMap.class, response.getData().getMiniFormA());
        assertNull(response.getData().getAppSolicitorName());
        assertNull(response.getData().getAppSolicitorFirm());
        assertNull(response.getData().getContactDetailsWrapper().getSolicitorReference());
        assertNull(response.getData().getContactDetailsWrapper().getSolicitorAddress());
        assertNull(response.getData().getContactDetailsWrapper().getApplicantSolicitorPhone());
        assertNull(response.getData().getContactDetailsWrapper().getApplicantSolicitorEmail());
        assertNull(response.getData().getContactDetailsWrapper().getApplicantSolicitorDxNumber());
        assertNull(response.getData().getContactDetailsWrapper().getApplicantSolicitorConsentForEmails());
        verify(service).generateContestedMiniFormA(any(), any());
    }

    @Test
    void shouldSuccessfullyRemoveApplicantSolicitorDetails_applicantConfidentialAddressNotAmended() throws Exception {
        FinremCallbackRequest finremCallbackRequest = buildCaseDetailsWithPath(FIXTURES_CONTESTED_AMEND_APPLICANT_SOLICITOR_DETAILS_APP_UNTOUCHED_JSON);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);
        assertEquals(false, response.getData().isApplicantRepresentedByASolicitor());
        assertNotNull(response.getData().getContactDetailsWrapper().getApplicantAddress());
        assertEquals("89897876765", response.getData().getContactDetailsWrapper().getApplicantPhone());
        assertEquals("email01@email.com", response.getData().getContactDetailsWrapper().getApplicantEmail());
        assertEquals("Poor", response.getData().getContactDetailsWrapper().getApplicantFmName());
        assertEquals("Guy", response.getData().getContactDetailsWrapper().getApplicantLname());
        assertNull(response.getData().isAppAddressConfidential());
        assertEquals(false, response.getData().isRespAddressConfidential());
        assertEquals(LinkedHashMap.class, response.getData().getMiniFormA());
        assertNull(response.getData().getAppSolicitorName());
        assertNull(response.getData().getAppSolicitorFirm());
        assertNull(response.getData().getContactDetailsWrapper().getSolicitorReference());
        assertNull(response.getData().getContactDetailsWrapper().getSolicitorAddress());
        assertNull(response.getData().getContactDetailsWrapper().getApplicantSolicitorPhone());
        assertNull(response.getData().getContactDetailsWrapper().getApplicantSolicitorEmail());
        assertNull(response.getData().getContactDetailsWrapper().getApplicantSolicitorDxNumber());
        assertNull(response.getData().getContactDetailsWrapper().getApplicantSolicitorConsentForEmails());
        verify(service).generateContestedMiniFormA(any(), any());
    }

    @Test
    void shouldSuccessfullyRemoveApplicantSolicitorDetails_bothConfidentialAddressNotAmended() throws Exception {
        FinremCallbackRequest finremCallbackRequest = buildCaseDetailsWithPath(FIXTURES_CONTESTED_AMEND_APPLICANT_SOLICITOR_DETAILS_BOTH_UNTOUCHED_JSON);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);
        assertEquals(false, response.getData().isApplicantRepresentedByASolicitor());
        assertNotNull(response.getData().getContactDetailsWrapper().getApplicantAddress());
        assertEquals("89897876765", response.getData().getContactDetailsWrapper().getApplicantPhone());
        assertEquals("email01@email.com", response.getData().getContactDetailsWrapper().getApplicantEmail());
        assertEquals("Poor", response.getData().getContactDetailsWrapper().getApplicantFmName());
        assertEquals("Guy", response.getData().getContactDetailsWrapper().getApplicantLname());
        assertNull(response.getData().isAppAddressConfidential());
        assertEquals(false, response.getData().isRespAddressConfidential());
        assertEquals(LinkedHashMap.class, response.getData().getMiniFormA());
        assertNull(response.getData().getAppSolicitorName());
        assertNull(response.getData().getAppSolicitorFirm());
        assertNull(response.getData().getContactDetailsWrapper().getSolicitorReference());
        assertNull(response.getData().getContactDetailsWrapper().getSolicitorAddress());
        assertNull(response.getData().getContactDetailsWrapper().getApplicantSolicitorPhone());
        assertNull(response.getData().getContactDetailsWrapper().getApplicantSolicitorEmail());
        assertNull(response.getData().getContactDetailsWrapper().getApplicantSolicitorDxNumber());
        assertNull(response.getData().getContactDetailsWrapper().getApplicantSolicitorConsentForEmails());
        verify(service).generateContestedMiniFormA(any(), any());
    }

    private FinremCallbackRequest buildCaseDetailsWithPath(String path) {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(path)) {
            FinremCaseDetails caseDetails =
                objectMapper.readValue(resourceAsStream, FinremCallbackRequest.class).getCaseDetails();

            return FinremCallbackRequest
                .builder()
                .eventType(EventType.UPDATE_CONTACT_DETAILS)
                .caseDetails(caseDetails)
                .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
