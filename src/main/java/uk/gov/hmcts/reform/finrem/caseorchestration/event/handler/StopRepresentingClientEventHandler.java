package uk.gov.hmcts.reform.finrem.caseorchestration.event.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.event.StopRepresentingClientEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.UpdateRepresentationWorkflowService;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StopRepresentingClientEventHandler {

    private final UpdateRepresentationWorkflowService nocWorkflowService;

    @EventListener
    public void handleEvent(StopRepresentingClientEvent event) {
        // Acc @Async if success message screen needed
        nocWorkflowService.handleNoticeOfChangeWorkflow(event.getData(), event.getOriginalData(),
            event.getUserAuthorisation());
    }
}
