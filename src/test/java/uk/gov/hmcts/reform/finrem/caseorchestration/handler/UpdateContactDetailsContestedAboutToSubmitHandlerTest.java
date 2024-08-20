package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnlineFormDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.UpdateRepresentationWorkflowService;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class UpdateContactDetailsContestedAboutToSubmitHandlerTest extends BaseHandlerTestSetup {
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
    private static final String FIXTURES_CONTESTED_AMEND_RESPONDENT_SOLICITOR_DETAILS_REPRESENTATION_CHANGED_JSON =
        "/fixtures/contested/amend-respondent-solicitor-details-representation-changed.json";

    @InjectMocks
    private UpdateContactDetailsContestedAboutToSubmitHandler handler;

    @Mock
    private UpdateRepresentationWorkflowService updateRepresentationWorkflowService;

    @Mock
    private OnlineFormDocumentService onlineFormDocumentService;

    @Spy
    private FinremCaseDetailsMapper finremCaseDetailsMapper = new FinremCaseDetailsMapper(
        new ObjectMapper().registerModule(new JavaTimeModule()));

    @Test
    void givenACcdCallbackUpdateContactDetailsContestCase_WhenCanHandleCalled_thenHandlerCanHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_SUBMIT, CONTESTED, EventType.UPDATE_CONTACT_DETAILS);
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
    void shouldHandleNoticeOfChangeWorkflow_WhenIsUpdateIncludesRepresentativeChange() {
        FinremCallbackRequest finremCallbackRequest =
            buildCaseDetailsWithPath(FIXTURES_CONTESTED_AMEND_APPLICANT_SOLICITOR_DETAILS_REPRESENTATION_CHANGED_JSON);
        finremCallbackRequest.getCaseDetails().getData().getContactDetailsWrapper()
            .setUpdateIncludesRepresentativeChange(YesOrNo.YES);
        AboutToStartOrSubmitCallbackResponse nocResponse = AboutToStartOrSubmitCallbackResponse.builder()
            .data(finremCaseDetailsMapper.mapToCaseDetails(finremCallbackRequest.getCaseDetails()).getData())
            .build();
        when(updateRepresentationWorkflowService.handleNoticeOfChangeWorkflow(
            any(CaseDetails.class), anyString(), any(CaseDetails.class))).thenReturn(nocResponse);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertNotNull(response);
        verify(updateRepresentationWorkflowService, times(1)).handleNoticeOfChangeWorkflow(
            any(CaseDetails.class), anyString(), any(CaseDetails.class));
    }

    @Test
    void shouldHandleNoticeOfChangeWorkflow_WhenApplicantRepresentedChanged() {
        FinremCallbackRequest finremCallbackRequest =
            buildCaseDetailsWithPath(FIXTURES_CONTESTED_AMEND_APPLICANT_SOLICITOR_DETAILS_REPRESENTATION_CHANGED_JSON);
        AboutToStartOrSubmitCallbackResponse nocResponse = AboutToStartOrSubmitCallbackResponse.builder()
            .data(finremCaseDetailsMapper.mapToCaseDetails(finremCallbackRequest.getCaseDetails()).getData())
            .build();
        when(updateRepresentationWorkflowService.handleNoticeOfChangeWorkflow(
            any(CaseDetails.class), anyString(), any(CaseDetails.class))).thenReturn(nocResponse);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertNotNull(response);
        verify(updateRepresentationWorkflowService, times(1)).handleNoticeOfChangeWorkflow(
            any(CaseDetails.class), anyString(), any(CaseDetails.class));
    }

    @Test
    void shouldHandleNoticeOfChangeWorkflow_WhenRespondentRepresentedChanged() {
        FinremCallbackRequest finremCallbackRequest =
            buildCaseDetailsWithPath(FIXTURES_CONTESTED_AMEND_RESPONDENT_SOLICITOR_DETAILS_REPRESENTATION_CHANGED_JSON);
        AboutToStartOrSubmitCallbackResponse nocResponse = AboutToStartOrSubmitCallbackResponse.builder()
            .data(finremCaseDetailsMapper.mapToCaseDetails(finremCallbackRequest.getCaseDetails()).getData())
            .build();
        when(updateRepresentationWorkflowService.handleNoticeOfChangeWorkflow(
            any(CaseDetails.class), anyString(), any(CaseDetails.class))).thenReturn(nocResponse);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertNotNull(response);
        verify(updateRepresentationWorkflowService, times(1)).handleNoticeOfChangeWorkflow(
            any(CaseDetails.class), anyString(), any(CaseDetails.class));
    }

    private FinremCallbackRequest buildCaseDetailsWithPath(String path) {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(path)) {
            FinremCallbackRequest finremCallbackRequest =
                objectMapper.readValue(resourceAsStream, FinremCallbackRequest.class);

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
