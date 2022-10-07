package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseFlagsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import java.io.InputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LATEST_CONSENT_ORDER;

@RunWith(MockitoJUnitRunner.class)
public class SolicitorCreateConsentedAboutToSubmitHandlerTest {

    private static final String AUTH_TOKEN = "4d73f8d4-2a8d-48e2-af91-11cbaa642345";
    private static final String APPROVE_ORDER_VALID_JSON = "/fixtures/pba-validate.json";

    @InjectMocks
    private SolicitorCreateConsentedAboutToSubmitHandler handler;
    @Mock
    private ConsentOrderService consentOrderService;
    @Mock
    private IdamService idamService;
    @Mock
    private CaseFlagsService caseFlagsService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void given_case_whenEvent_type_is_solicitorCreate_thenCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.SOLICITOR_CREATE),
            is(true));
    }

    @Test
    public void given_case_whenEvent_type_is_respond_to_order_thenCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.RESPOND_TO_ORDER),
            is(true));
    }

    @Test
    public void given_case_whenEvent_type_is_amend_consent_order_thenCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.AMEND_CONSENT_ORDER),
            is(true));
    }

    @Test
    public void given_case_when_wrong_callback_then_case_can_not_handle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.SOLICITOR_CREATE),
            is(false));
    }

    @Test
    public void given_case_when_wrong_casetype_then_case_can_not_handle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.SOLICITOR_CREATE),
            is(false));
    }

    @Test
    public void given_case_when_wrong_eventtype_then_case_can_not_handle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.CLOSE),
            is(false));
    }


    @Test
    public void givenCase_whenRequestToUpdateLatestConsentOrderAndUserDoNotHaveAdminRole_thenHandlerCanHandle() {
        CallbackRequest callbackRequest = doValidCaseDataSetUp();
        when(idamService.isUserRoleAdmin(any())).thenReturn(false);
        when(consentOrderService.getLatestConsentOrderData(any(CallbackRequest.class))).thenReturn(getCaseDocument());

        AboutToStartOrSubmitCallbackResponse response = handler.handle(callbackRequest, AUTH_TOKEN);

        assertNotNull(response.getData().get(LATEST_CONSENT_ORDER));
        assertEquals(YES_VALUE, response.getData().get(APPLICANT_REPRESENTED));
        verify(idamService).isUserRoleAdmin(any());
        verify(consentOrderService).getLatestConsentOrderData(any());
    }


    @Test
    public void givenCase_whenRequestToUpdateLatestConsentOrderAndUserDoHaveAdminRole_thenHandlerCanHandle() {
        when(consentOrderService.getLatestConsentOrderData(any(CallbackRequest.class))).thenReturn(getCaseDocument());
        CallbackRequest callbackRequest = doValidCaseDataSetUp();
        when(idamService.isUserRoleAdmin(any())).thenReturn(true);

        AboutToStartOrSubmitCallbackResponse response = handler.handle(callbackRequest, AUTH_TOKEN);

        assertNotNull(response.getData().get(LATEST_CONSENT_ORDER));
        assertNull(response.getData().get(APPLICANT_REPRESENTED));
        verify(idamService).isUserRoleAdmin(any());
        verify(consentOrderService).getLatestConsentOrderData(any());
    }

    private CallbackRequest doValidCaseDataSetUp()  {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(APPROVE_ORDER_VALID_JSON)) {
            return objectMapper.readValue(resourceAsStream, CallbackRequest.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private CaseDocument getCaseDocument() {
        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setDocumentUrl("http://doc1");
        caseDocument.setDocumentBinaryUrl("http://doc1/binary");
        caseDocument.setDocumentFilename("doc1");
        return caseDocument;
    }
}