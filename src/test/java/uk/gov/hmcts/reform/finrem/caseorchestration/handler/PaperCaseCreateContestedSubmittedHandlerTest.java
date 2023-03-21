package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CreateCaseService;

import java.io.InputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@RunWith(MockitoJUnitRunner.class)
public class PaperCaseCreateContestedSubmittedHandlerTest {

    @InjectMocks
    private PaperCaseCreateContestedSubmittedHandler handler;

    @Mock
    private CreateCaseService createCaseService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void givenACcdCallbackSolicitorCreateContestedCase_WhenCanHandleCalled_thenHandlerCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.NEW_PAPER_CASE),
            is(true));
    }

    @Test
    public void givenACcdCallbackAboutToSubmit_WhenCanHandleCalled_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.NEW_PAPER_CASE),
            is(false));
    }

    @Test
    public void givenACcdCallbackSolicitorCreateContestedCase_WhenHandle_thenAddSupplementary() {
        CallbackRequest callbackRequest =
            CallbackRequest.builder().caseDetails(getCase()).build();

        handler.handle(callbackRequest, AUTH_TOKEN);

        verify(createCaseService, times(1)).setSupplementaryData(eq(callbackRequest), any());
    }

    private CaseDetails getCase() {
        try (InputStream resourceAsStream = getClass()
            .getResourceAsStream("/fixtures/contested/validate-hearing-with-fastTrackDecision.json")) {
            return objectMapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}