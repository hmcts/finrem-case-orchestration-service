package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(MockitoJUnitRunner.class)
public class CaseFlagAboutToSubmitHandlerTest {

    private static final String AUTH_TOKEN = "4d73f8d4-2a8d-48e2-af91-11cbaa642345";


    @InjectMocks
    private CaseFlagAboutToSubmitHandler handler;
    @Mock
    private CaseDataService caseDataService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void given_case_when_EventCreateCaseFlag_thenCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.CASE_FLAG_CREATE),
            is(true));
    }

    @Test
    public void given_case_when_wrong_callback_then_case_can_not_handle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.CASE_FLAG_CREATE),
            is(false));
    }

    @Test
    public void given_case_when_EventCManageCaseFlag_thenCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.CASE_FLAG_MANAGE),
            is(true));
    }

    @Test
    public void given_case_when_wrong_eventtype_then_case_can_not_handle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.CLOSE),
            is(false));
    }

}