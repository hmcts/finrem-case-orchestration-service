package uk.gov.hmcts.reform.finrem.caseorchestration.handler.uploadapprovedorder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralApplicationDirectionsService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@RunWith(MockitoJUnitRunner.class)
public class UploadApprovedOrderSubmittedHandlerTest extends UploadApprovedOrderBaseHandlerTest {

    @InjectMocks
    UploadApprovedOrderSubmittedHandler uploadApprovedOrderSubmittedHandler;

    @Mock
    GeneralApplicationDirectionsService generalApplicationDirectionsService;

    @Test
    public void givenContestedCase_whenAboutToSubmitUploadApprovedOrder_thenCanHandle() {
        assertThat(uploadApprovedOrderSubmittedHandler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.UPLOAD_APPROVED_ORDER),
            is(true));
    }

    @Test
    public void givenContestedCase_whenSubmittedUploadApprovedOrder_thenCannotHandle() {
        assertThat(uploadApprovedOrderSubmittedHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.UPLOAD_APPROVED_ORDER),
            is(false));
    }

    @Test
    public void givenContestedCase_whenSubmittedUploadApprovedOrder_thenHandle() {
        uploadApprovedOrderSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(generalApplicationDirectionsService, times(1))
            .submitGeneralApplicationDirections(callbackRequest.getCaseDetails(), AUTH_TOKEN);
    }
}
