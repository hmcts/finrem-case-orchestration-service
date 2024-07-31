package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.uploadapprovedorder.UploadApprovedOrderContestedAboutToStartHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnlineFormDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.UpdateRepresentationWorkflowService;

import java.io.InputStream;
import java.util.LinkedHashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UpdateContactDetailsContestedSubmittedHandlerTest extends BaseHandlerTestSetup {
    public static final String AUTH_TOKEN = "tokien:)";

    private static final String FIXTURES_CONTESTED_AMEND_APPLICANT_SOLICITOR_DETAILS_JSON = "/fixtures/contested/amend-applicant-solicitor-details.json";
    private static final String FIXTURES_CONTESTED_AMEND_APPLICANT_SOLICITOR_DETAILS_RES_UNTOUCHED_JSON = "/fixtures/contested/amend-applicant-solicitor-details-res-untouched.json";
    private static final String FIXTURES_CONTESTED_AMEND_APPLICANT_SOLICITOR_DETAILS_APP_UNTOUCHED_JSON = "/fixtures/contested/amend-applicant-solicitor-details-app-untouched.json";
    private static final String FIXTURES_CONTESTED_AMEND_APPLICANT_SOLICITOR_DETAILS_BOTH_UNTOUCHED_JSON = "/fixtures/contested/amend-applicant-solicitor-details-both-untouched.json";

    private UpdateContactDetailsContestedSubmittedHandler handler;

    private UpdateRepresentationWorkflowService updateRepresentationWorkflowService;

    @Mock
    protected OnlineFormDocumentService service;

    @Mock
    private FeatureToggleService featureToggleService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        handler = new UpdateContactDetailsContestedSubmittedHandler(new FinremCaseDetailsMapper(new ObjectMapper()), updateRepresentationWorkflowService, service);
    }


    @Test
    public void givenACcdCallbackUpdateContactDetailsContestCase_WhenCanHandleCalled_thenHandlerCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.UPDATE_CONTACT_DETAILS),
            is(true));
    }

    @Test
    public void shouldSuccessfullyRemoveApplicantSolicitorDetails() {

        FinremCallbackRequest finremCallbackRequest = buildCaseDetailsWithPath(FIXTURES_CONTESTED_AMEND_APPLICANT_SOLICITOR_DETAILS_JSON);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertEquals(response.getData().isApplicantRepresentedByASolicitor(), "No");
        assertNotNull(response.getData().getContactDetailsWrapper().getApplicantAddress());
        assertEquals(response.getData().getContactDetailsWrapper().getApplicantPhone(), "89897876765");
        assertEquals(response.getData().getContactDetailsWrapper().getApplicantEmail(), "email01@email.com");
        assertEquals(response.getData().getContactDetailsWrapper().getApplicantFmName(), "Poor");
        assertEquals(response.getData().getContactDetailsWrapper().getApplicantLname(), "Guy");
        assertEquals(response.getData().isAppAddressConfidential(), "Yes");
        assertEquals(response.getData().isRespAddressConfidential(), "No");
        assertEquals(response.getData().getMiniFormA().getClass(), LinkedHashMap.class);
        assertEquals(response.getData().getAppSolicitorName(), null);
        assertEquals(response.getData().getAppSolicitorFirm(), null);
        assertEquals(response.getData().getContactDetailsWrapper().getSolicitorReference(), null);
        assertEquals(response.getData().getContactDetailsWrapper().getSolicitorAddress(), null);
        assertEquals(response.getData().getContactDetailsWrapper().getApplicantSolicitorPhone(), null);
        assertEquals(response.getData().getContactDetailsWrapper().getApplicantSolicitorEmail(), null);
        assertEquals(response.getData().getContactDetailsWrapper().getApplicantSolicitorDxNumber(), null);
        assertEquals(response.getData().getContactDetailsWrapper().getApplicantSolicitorConsentForEmails(), null);

        verify(service).generateContestedMiniFormA(any(), any());

    }

    @Test
    public void shouldSuccessfullyRemoveApplicantSolicitorDetails_respondentConfidentialAddressNotAmended() throws Exception {

        FinremCallbackRequest finremCallbackRequest = buildCaseDetailsWithPath(FIXTURES_CONTESTED_AMEND_APPLICANT_SOLICITOR_DETAILS_RES_UNTOUCHED_JSON);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);
        assertEquals(response.getData().isApplicantRepresentedByASolicitor(), "No");
        assertNotNull(response.getData().getContactDetailsWrapper().getApplicantAddress());
        assertEquals(response.getData().getContactDetailsWrapper().getApplicantPhone(), "89897876765");
        assertEquals(response.getData().getContactDetailsWrapper().getApplicantEmail(), "email01@email.com");
        assertEquals(response.getData().getContactDetailsWrapper().getApplicantFmName(), "Poor");
        assertEquals(response.getData().getContactDetailsWrapper().getApplicantLname(), "Guy");
        assertEquals(response.getData().isAppAddressConfidential(), "Yes");
        assertEquals(response.getData().isRespAddressConfidential(), null);
        assertEquals(response.getData().getMiniFormA().getClass(), LinkedHashMap.class);
        assertEquals(response.getData().getAppSolicitorName(), null);
        assertEquals(response.getData().getAppSolicitorFirm(), null);
        assertEquals(response.getData().getContactDetailsWrapper().getSolicitorReference(), null);
        assertEquals(response.getData().getContactDetailsWrapper().getSolicitorAddress(), null);
        assertEquals(response.getData().getContactDetailsWrapper().getApplicantSolicitorPhone(), null);
        assertEquals(response.getData().getContactDetailsWrapper().getApplicantSolicitorEmail(), null);
        assertEquals(response.getData().getContactDetailsWrapper().getApplicantSolicitorDxNumber(), null);
        assertEquals(response.getData().getContactDetailsWrapper().getApplicantSolicitorConsentForEmails(), null);

        verify(service).generateContestedMiniFormA(any(), any());
    }

    @Test
    public void shouldSuccessfullyRemoveApplicantSolicitorDetails_applicantConfidentialAddressNotAmended() throws Exception {
        FinremCallbackRequest finremCallbackRequest = buildCaseDetailsWithPath(FIXTURES_CONTESTED_AMEND_APPLICANT_SOLICITOR_DETAILS_APP_UNTOUCHED_JSON);
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);
        assertEquals(0, response.getErrors().size());

        assertEquals(response.getData().isApplicantRepresentedByASolicitor(), "No");
        assertNotNull(response.getData().getContactDetailsWrapper().getApplicantAddress());
        assertEquals(response.getData().getContactDetailsWrapper().getApplicantPhone(), "89897876765");
        assertEquals(response.getData().getContactDetailsWrapper().getApplicantEmail(), "email01@email.com");
        assertEquals(response.getData().getContactDetailsWrapper().getApplicantFmName(), "Poor");
        assertEquals(response.getData().getContactDetailsWrapper().getApplicantLname(), "Guy");
        assertEquals(response.getData().isAppAddressConfidential(), null);
        assertEquals(response.getData().isRespAddressConfidential(), "Yes");
        assertEquals(response.getData().getMiniFormA().getClass(), LinkedHashMap.class);
        assertEquals(response.getData().getAppSolicitorName(), null);
        assertEquals(response.getData().getAppSolicitorFirm(), null);
        assertEquals(response.getData().getContactDetailsWrapper().getSolicitorReference(), null);
        assertEquals(response.getData().getContactDetailsWrapper().getSolicitorAddress(), null);
        assertEquals(response.getData().getContactDetailsWrapper().getApplicantSolicitorPhone(), null);
        assertEquals(response.getData().getContactDetailsWrapper().getApplicantSolicitorEmail(), null);
        assertEquals(response.getData().getContactDetailsWrapper().getApplicantSolicitorDxNumber(), null);
        assertEquals(response.getData().getContactDetailsWrapper().getApplicantSolicitorConsentForEmails(), null);

        verify(service).generateContestedMiniFormA(any(), any());
    }

    @Test
    public void shouldSuccessfullyRemoveApplicantSolicitorDetails_bothConfidentialAddressNotAmended() throws Exception {
        FinremCallbackRequest finremCallbackRequest = buildCaseDetailsWithPath(FIXTURES_CONTESTED_AMEND_APPLICANT_SOLICITOR_DETAILS_BOTH_UNTOUCHED_JSON);
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);
        assertEquals(0, response.getErrors().size());

        assertEquals(response.getData().isApplicantRepresentedByASolicitor(), "No");
        assertNotNull(response.getData().getContactDetailsWrapper().getApplicantAddress());
        assertEquals(response.getData().getContactDetailsWrapper().getApplicantPhone(), "89897876765");
        assertEquals(response.getData().getContactDetailsWrapper().getApplicantEmail(), "email01@email.com");
        assertEquals(response.getData().getContactDetailsWrapper().getApplicantFmName(), "Poor");
        assertEquals(response.getData().getContactDetailsWrapper().getApplicantLname(), "Guy");
        assertEquals(response.getData().isAppAddressConfidential(), null);
        assertEquals(response.getData().isRespAddressConfidential(), null);
        assertEquals(response.getData().getAppSolicitorName(), null);
        assertEquals(response.getData().getAppSolicitorFirm(), null);
        assertEquals(response.getData().getContactDetailsWrapper().getSolicitorReference(), null);
        assertEquals(response.getData().getContactDetailsWrapper().getSolicitorAddress(), null);
        assertEquals(response.getData().getContactDetailsWrapper().getApplicantSolicitorPhone(), null);
        assertEquals(response.getData().getContactDetailsWrapper().getApplicantSolicitorEmail(), null);
        assertEquals(response.getData().getContactDetailsWrapper().getApplicantSolicitorDxNumber(), null);
        assertEquals(response.getData().getContactDetailsWrapper().getApplicantSolicitorConsentForEmails(), null);

        verify(service, never()).generateContestedMiniFormA(any(), any());
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