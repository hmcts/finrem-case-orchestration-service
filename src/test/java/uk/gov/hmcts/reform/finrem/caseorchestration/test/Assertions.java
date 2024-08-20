package uk.gov.hmcts.reform.finrem.caseorchestration.test;

import org.junit.jupiter.params.provider.Arguments;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class Assertions {

    public static void assertCanHandle(CallbackHandler handler, CallbackType expectedCallbackType, CaseType expectedCaseType,
                                       EventType expectedEventType) {
        assertCanHandle(handler, Arguments.of(expectedCallbackType, expectedCaseType, expectedEventType));
    }

    public static void assertCanHandle(CallbackHandler handler, Arguments... combination) {
        for (CallbackType callbackType : CallbackType.values()) {
            for (CaseType caseType : CaseType.values()) {
                for (EventType eventType : EventType.values()) {
                    boolean expectedOutcome = Arrays.stream(combination).anyMatch(c ->
                        callbackType == c.get()[0]
                            && caseType == c.get()[1]
                            && eventType == c.get()[2] // This condition will always be true
                    );
                    assertThat(handler.canHandle(callbackType, caseType, eventType), equalTo(expectedOutcome));
                }
            }
        }
    }
}