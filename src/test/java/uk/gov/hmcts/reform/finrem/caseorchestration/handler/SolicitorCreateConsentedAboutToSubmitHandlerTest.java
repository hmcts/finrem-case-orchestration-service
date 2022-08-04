package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.ccd.callback.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.ccd.domain.CaseType;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.EventType;
import uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SolicitorCreateConsentedAboutToSubmitHandlerTest extends BaseHandlerTest {

    private static final String AUTH_TOKEN = "4d73f8d4-2a8d-48e2-af91-11cbaa642345";
    private static final String APPROVE_ORDER_VALID_JSON = "/fixtures/pba-validate.json";

    @InjectMocks
    private SolicitorCreateConsentedAboutToSubmitHandler handler;
    @Mock
    private ConsentOrderService consentOrderService;
    @Mock
    private IdamService idamService;

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
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.AMENDED_CONSENT_ORDER),
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

        assertNotNull(response.getData().getLatestConsentOrder());
        assertEquals(YesOrNo.YES, response.getData().getContactDetailsWrapper().getApplicantRepresented());
        verify(idamService).isUserRoleAdmin(any());
        verify(consentOrderService).getLatestConsentOrderData(isA(CallbackRequest.class));
    }


    @Test
    public void givenCase_whenRequestToUpdateLatestConsentOrderAndUserDoHaveAdminRole_thenHandlerCanHandle() {
        when(consentOrderService.getLatestConsentOrderData(any(CallbackRequest.class))).thenReturn(getCaseDocument());
        CallbackRequest callbackRequest = doValidCaseDataSetUp();
        when(idamService.isUserRoleAdmin(any())).thenReturn(true);

        AboutToStartOrSubmitCallbackResponse response = handler.handle(callbackRequest, AUTH_TOKEN);

        assertNotNull(response.getData().getLatestConsentOrder());
        assertNull(response.getData().getContactDetailsWrapper().getApplicantRepresented());
        verify(idamService).isUserRoleAdmin(any());
        verify(consentOrderService).getLatestConsentOrderData(isA(CallbackRequest.class));
    }

    private CallbackRequest doValidCaseDataSetUp()  {
        try {
            return getCallbackRequestFromResource(APPROVE_ORDER_VALID_JSON);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Document getCaseDocument() {
        Document caseDocument = new Document();
        caseDocument.setUrl("http://doc1");
        caseDocument.setBinaryUrl("http://doc1/binary");
        caseDocument.setFilename("doc1");
        return caseDocument;
    }
}