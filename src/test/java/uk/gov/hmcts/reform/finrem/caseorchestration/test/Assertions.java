package uk.gov.hmcts.reform.finrem.caseorchestration.test;

import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class Assertions {

    public static void assertCanHandle(CallbackHandler handler, CallbackType expectedCallbackType, CaseType expectedCaseType,
                                       EventType expectedEventType) {
        for (CallbackType callbackType : CallbackType.values()) {
            for (CaseType caseType : CaseType.values()) {
                for (EventType eventType : EventType.values()) {
                    boolean expectedOutcome = callbackType == expectedCallbackType
                        && caseType == expectedCaseType
                        && eventType == expectedEventType; // This condition will always be true
                    assertThat(handler.canHandle(callbackType, caseType, eventType), equalTo(expectedOutcome));
                }
            }
        }
    }
}
