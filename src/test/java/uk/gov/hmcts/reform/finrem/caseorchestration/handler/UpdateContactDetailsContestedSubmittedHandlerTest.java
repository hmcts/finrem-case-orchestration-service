package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnlineFormDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.UpdateRepresentationWorkflowService;

import java.io.InputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;

@ExtendWith(MockitoExtension.class)
class UpdateContactDetailsContestedSubmittedHandlerTest extends BaseHandlerTestSetup {
    private static final String AUTH_TOKEN = "token:)";

    private static final String FIXTURES_CONTESTED_AMEND_APPLICANT_SOLICITOR_DETAILS_JSON =
        "/fixtures/contested/amend-applicant-solicitor-details.json";
    private static final String FIXTURES_CONTESTED_AMEND_APPLICANT_SOLICITOR_DETAILS_RES_UNTOUCHED_JSON =
        "/fixtures/contested/amend-applicant-solicitor-details-res-untouched.json";
    private static final String FIXTURES_CONTESTED_AMEND_APPLICANT_SOLICITOR_DETAILS_APP_UNTOUCHED_JSON =
        "/fixtures/contested/amend-applicant-solicitor-details-app-untouched.json";
    private static final String FIXTURES_CONTESTED_AMEND_APPLICANT_SOLICITOR_DETAILS_BOTH_UNTOUCHED_JSON =
        "/fixtures/contested/amend-applicant-solicitor-details-both-untouched.json";
    private static final String FIXTURES_CONTESTED_AMEND_APPLICANT_SOLICITOR_DETAILS_REPRESENTATION_CHANGED_JSON =
        "/fixtures/contested/amend-applicant-solicitor-details-representation-changed.json";

    @InjectMocks
    private UpdateContactDetailsContestedSubmittedHandler handler;

    @Mock
    private UpdateRepresentationWorkflowService updateRepresentationWorkflowService;

    @Mock
    private OnlineFormDocumentService onlineFormDocumentService;

    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Test
    void givenACcdCallbackUpdateContactDetailsContestCase_WhenCanHandleCalled_thenHandlerCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.UPDATE_CONTACT_DETAILS),
            is(true));
    }

    @Test
    void shouldSuccessfullyRemoveApplicantSolicitorDetails() {
        when(onlineFormDocumentService.generateContestedMiniFormA(any(), any())).thenReturn(TestSetUpUtils.caseDocument());

        FinremCallbackRequest finremCallbackRequest =
            buildCaseDetailsWithPath(FIXTURES_CONTESTED_AMEND_APPLICANT_SOLICITOR_DETAILS_JSON);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            handler.handle(finremCallbackRequest, AUTH_TOKEN);
        assertFalse(response.getData().isApplicantRepresentedByASolicitor());
        assertNotNull(response.getData().getContactDetailsWrapper().getApplicantAddress());
        assertEquals("89897876765", response.getData().getContactDetailsWrapper().getApplicantPhone());
        assertEquals("email01@email.com", response.getData().getContactDetailsWrapper().getApplicantEmail());
        assertEquals("Poor", response.getData().getContactDetailsWrapper().getApplicantFmName());
        assertEquals("Guy", response.getData().getContactDetailsWrapper().getApplicantLname());
        assertTrue(response.getData().isAppAddressConfidential());
        assertFalse(response.getData().isRespAddressConfidential());
        assertEquals(caseDocument(), response.getData().getMiniFormA());
        assertNull(response.getData().getAppSolicitorName());
        assertNull(response.getData().getAppSolicitorFirm());
        assertNull(response.getData().getContactDetailsWrapper().getSolicitorReference());
        assertNull(response.getData().getContactDetailsWrapper().getSolicitorAddress());
        assertNull(response.getData().getContactDetailsWrapper().getApplicantSolicitorPhone());
        assertNull(response.getData().getContactDetailsWrapper().getApplicantSolicitorEmail());
        assertNull(response.getData().getContactDetailsWrapper().getApplicantSolicitorDxNumber());
        assertNull(response.getData().getContactDetailsWrapper().getApplicantSolicitorConsentForEmails());
        verify(onlineFormDocumentService).generateContestedMiniFormA(any(), any());
    }

    @Test
    void shouldSuccessfullyRemoveApplicantSolicitorDetails_respondentConfidentialAddressNotAmended() {
        when(onlineFormDocumentService.generateContestedMiniFormA(any(), any())).thenReturn(TestSetUpUtils.caseDocument());

        FinremCallbackRequest finremCallbackRequest =
            buildCaseDetailsWithPath(FIXTURES_CONTESTED_AMEND_APPLICANT_SOLICITOR_DETAILS_RES_UNTOUCHED_JSON);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            handler.handle(finremCallbackRequest, AUTH_TOKEN);
        assertFalse(response.getData().isApplicantRepresentedByASolicitor());
        assertNotNull(response.getData().getContactDetailsWrapper().getApplicantAddress());
        assertEquals("89897876765", response.getData().getContactDetailsWrapper().getApplicantPhone());
        assertEquals("email01@email.com", response.getData().getContactDetailsWrapper().getApplicantEmail());
        assertEquals("Poor", response.getData().getContactDetailsWrapper().getApplicantFmName());
        assertEquals("Guy", response.getData().getContactDetailsWrapper().getApplicantLname());
        assertTrue(response.getData().isAppAddressConfidential());
        assertFalse(response.getData().isRespAddressConfidential());
        assertEquals(caseDocument(), response.getData().getMiniFormA());
        assertNull(response.getData().getAppSolicitorName());
        assertNull(response.getData().getAppSolicitorFirm());
        assertNull(response.getData().getContactDetailsWrapper().getSolicitorReference());
        assertNull(response.getData().getContactDetailsWrapper().getSolicitorAddress());
        assertNull(response.getData().getContactDetailsWrapper().getApplicantSolicitorPhone());
        assertNull(response.getData().getContactDetailsWrapper().getApplicantSolicitorEmail());
        assertNull(response.getData().getContactDetailsWrapper().getApplicantSolicitorDxNumber());
        assertNull(response.getData().getContactDetailsWrapper().getApplicantSolicitorConsentForEmails());
        verify(onlineFormDocumentService).generateContestedMiniFormA(any(), any());
    }

    @Test
    void shouldSuccessfullyRemoveApplicantSolicitorDetails_applicantConfidentialAddressNotAmended() {
        when(onlineFormDocumentService.generateContestedMiniFormA(any(), any())).thenReturn(TestSetUpUtils.caseDocument());

        FinremCallbackRequest finremCallbackRequest =
            buildCaseDetailsWithPath(FIXTURES_CONTESTED_AMEND_APPLICANT_SOLICITOR_DETAILS_APP_UNTOUCHED_JSON);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            handler.handle(finremCallbackRequest, AUTH_TOKEN);
        assertFalse(response.getData().isApplicantRepresentedByASolicitor());
        assertNotNull(response.getData().getContactDetailsWrapper().getApplicantAddress());
        assertEquals("89897876765", response.getData().getContactDetailsWrapper().getApplicantPhone());
        assertEquals("email01@email.com", response.getData().getContactDetailsWrapper().getApplicantEmail());
        assertEquals("Poor", response.getData().getContactDetailsWrapper().getApplicantFmName());
        assertEquals("Guy", response.getData().getContactDetailsWrapper().getApplicantLname());
        assertFalse(response.getData().isAppAddressConfidential());
        assertTrue(response.getData().isRespAddressConfidential());
        assertEquals(caseDocument(), response.getData().getMiniFormA());
        assertNull(response.getData().getAppSolicitorName());
        assertNull(response.getData().getAppSolicitorFirm());
        assertNull(response.getData().getContactDetailsWrapper().getSolicitorReference());
        assertNull(response.getData().getContactDetailsWrapper().getSolicitorAddress());
        assertNull(response.getData().getContactDetailsWrapper().getApplicantSolicitorPhone());
        assertNull(response.getData().getContactDetailsWrapper().getApplicantSolicitorEmail());
        assertNull(response.getData().getContactDetailsWrapper().getApplicantSolicitorDxNumber());
        assertNull(response.getData().getContactDetailsWrapper().getApplicantSolicitorConsentForEmails());
        verify(onlineFormDocumentService).generateContestedMiniFormA(any(), any());
    }

    @Test
    void shouldSuccessfullyRemoveApplicantSolicitorDetails_bothConfidentialAddressNotAmended() {
        FinremCallbackRequest finremCallbackRequest =
            buildCaseDetailsWithPath(FIXTURES_CONTESTED_AMEND_APPLICANT_SOLICITOR_DETAILS_BOTH_UNTOUCHED_JSON);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            handler.handle(finremCallbackRequest, AUTH_TOKEN);
        assertFalse(response.getData().isApplicantRepresentedByASolicitor());
        assertNotNull(response.getData().getContactDetailsWrapper().getApplicantAddress());
        assertEquals("89897876765", response.getData().getContactDetailsWrapper().getApplicantPhone());
        assertEquals("email01@email.com", response.getData().getContactDetailsWrapper().getApplicantEmail());
        assertEquals("Poor", response.getData().getContactDetailsWrapper().getApplicantFmName());
        assertEquals("Guy", response.getData().getContactDetailsWrapper().getApplicantLname());
        assertFalse(response.getData().isAppAddressConfidential());
        assertFalse(response.getData().isRespAddressConfidential());
        assertNull(response.getData().getAppSolicitorName());
        assertNull(response.getData().getAppSolicitorFirm());
        assertNull(response.getData().getContactDetailsWrapper().getSolicitorReference());
        assertNull(response.getData().getContactDetailsWrapper().getSolicitorAddress());
        assertNull(response.getData().getContactDetailsWrapper().getApplicantSolicitorPhone());
        assertNull(response.getData().getContactDetailsWrapper().getApplicantSolicitorEmail());
        assertNull(response.getData().getContactDetailsWrapper().getApplicantSolicitorDxNumber());
        assertNull(response.getData().getContactDetailsWrapper().getApplicantSolicitorConsentForEmails());
        verify(onlineFormDocumentService, never()).generateContestedMiniFormA(any(), any());
    }

    @Test
    void shouldHandleNoticeOfChangeWorkflow_WhenApplicantRepresentedChanged() {

        FinremCallbackRequest finremCallbackRequest =
            buildCaseDetailsWithPath(FIXTURES_CONTESTED_AMEND_APPLICANT_SOLICITOR_DETAILS_REPRESENTATION_CHANGED_JSON);
        when(updateRepresentationWorkflowService.handleNoticeOfChangeWorkflow(
            finremCaseDetailsMapper.mapToCaseDetails(finremCallbackRequest.getCaseDetails()),
            AUTH_TOKEN,
            finremCaseDetailsMapper.mapToCaseDetails(finremCallbackRequest.getCaseDetailsBefore())))
            .thenReturn(AboutToStartOrSubmitCallbackResponse.builder().build());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            handler.handle(finremCallbackRequest, AUTH_TOKEN);
        assertNotNull(response);
    }

    private FinremCallbackRequest buildCaseDetailsWithPath(String path) {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(path)) {
            FinremCallbackRequest finremCallbackRequest
                = objectMapper.readValue(resourceAsStream, FinremCallbackRequest.class);

            FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
            caseDetails.getData().setMiniFormA(caseDocument());

            return FinremCallbackRequest
                .builder()
                .eventType(EventType.UPDATE_CONTACT_DETAILS)
                .caseDetailsBefore(finremCallbackRequest.getCaseDetailsBefore())
                .caseDetails(caseDetails)
                .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
