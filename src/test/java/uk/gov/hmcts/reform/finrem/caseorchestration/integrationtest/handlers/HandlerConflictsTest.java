package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.handlers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {HandlerConflictsTestConfiguration.class})
public class HandlerConflictsTest {

    @Autowired
    private List<CallbackHandler> handlers;

    @Test
    public void givenAllPossibleParams_whenCanHandle_thenOnlyOneHandlerCanHandle() {
        for (CallbackType callbackType : CallbackType.values()) {
            for (EventType eventType : EventType.values()) {
                for (CaseType caseType : CaseType.values()) {
                    List<String> couldBeHandled = handlers.stream()
                        .filter(handler -> handler.canHandle(callbackType, caseType, eventType))
                        .map(handler -> handler.getClass().getSimpleName()) //for debugging if there's a clash
                        .toList();

                    assertTrue(couldBeHandled.size() <= 1);
                }
            }
        }
    }
}
