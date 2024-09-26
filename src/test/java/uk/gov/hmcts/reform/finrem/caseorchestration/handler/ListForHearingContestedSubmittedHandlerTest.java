package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing.ContestedListForHearingCorrespondenceService;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class ListForHearingContestedSubmittedHandlerTest extends BaseHandlerTestSetup {

    @Mock
    private ContestedListForHearingCorrespondenceService contestedListForHearingCorrespondenceService;

    @InjectMocks
    private ListForHearingContestedSubmittedHandler submittedHandler;

    private static final String NON_FAST_TRACK_HEARING_JSON = "/fixtures/contested/validate-hearing-withoutfastTrackDecision.json";

    @Test
    void testHandlerCanHandle() {
        assertCanHandle(submittedHandler, CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.LIST_FOR_HEARING);
    }

    @Test
    void givenContestedCase_whenNotFastTrackDecision_thenShouldSendHearingCorrespondence() {
        FinremCallbackRequest finremCallbackRequest = buildFinremCallbackRequest(NON_FAST_TRACK_HEARING_JSON);
        submittedHandler.handle(finremCallbackRequest, AUTH_TOKEN);
        verify(contestedListForHearingCorrespondenceService).sendHearingCorrespondence(finremCallbackRequest, AUTH_TOKEN);
    }
}
