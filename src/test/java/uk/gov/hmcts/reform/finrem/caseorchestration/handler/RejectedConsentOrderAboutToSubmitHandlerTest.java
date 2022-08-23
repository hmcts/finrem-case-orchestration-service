package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.RefusalOrderDocumentService;

import java.io.InputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class RejectedConsentOrderAboutToSubmitHandlerTest {

    @InjectMocks
    private RejectedConsentOrderAboutToSubmitHandler handler;
    @Mock
    private RefusalOrderDocumentService refusalOrderDocumentService;


    private final ObjectMapper objectMapper = new ObjectMapper();


    private static final String AUTH_TOKEN = "4d73f8d4-2a8d-48e2-af91-11cbaa642345";
    private static final String REJECT_ORDER_VALID_JSON = "/fixtures/fee-lookup.json";

    @Test
    public void given_case_whenEventRejectedOrder_thenCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.REJECT_ORDER),
            is(true));
    }

    @Test
    public void given_contested_case_whenEventConsentOrderNotApproved_thenCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.CONSENT_ORDER_NOT_APPROVED),
            is(true));
    }

    @Test
    public void given_case_when_wrong_callback_then_case_can_not_handle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.REJECT_ORDER),
            is(false));
    }

    @Test
    public void given_case_when_wrong_event_type_then_case_can_not_handle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.CLOSE),
            is(false));
    }

    @Test
    public void given_case_when_order_not_approved_then_reject_order() {
        CallbackRequest callbackRequest = doValidCaseDataSetUp();

        handler.handle(callbackRequest, AUTH_TOKEN);

        verify(refusalOrderDocumentService).generateConsentOrderNotApproved(any(), any());
    }

    private CallbackRequest doValidCaseDataSetUp()  {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(REJECT_ORDER_VALID_JSON)) {
            return objectMapper.readValue(resourceAsStream, CallbackRequest.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}