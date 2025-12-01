package uk.gov.hmcts.reform.finrem.caseorchestration.event.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.event.StopRepresentingClientEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;

import static java.util.Optional.ofNullable;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StopRepresentingClientEventHandler {

    private final AssignCaseAccessService assignCaseAccessService;

    private final SystemUserService systemUserService;

    private final FinremCaseDetailsMapper finremCaseDetailsMapper;

    @EventListener
    @Async
    public void handleEvent(StopRepresentingClientEvent event) {
        // @Async if success message screen needed
        FinremCaseData finremCaseData = event.getCaseDetails().getData();
//        FinremCaseData finremCaseDataBefore = event.getCaseDetailsBefore().getData();

        // trying to revoke creator role if any
        assignCaseAccessService.findAndRevokeCreatorRole(finremCaseData.getCcdCaseId());

        if (!safeChangeOrganisationRequest(finremCaseData).isNoOrganisationsToAddOrRemove()) {
            assignCaseAccessService.applyDecision(systemUserService.getSysUserToken(), finremCaseDetailsMapper
                .mapToCaseDetails(event.getCaseDetails()));
        }
    }

    private ChangeOrganisationRequest safeChangeOrganisationRequest(FinremCaseData data) {
        return ofNullable(data.getChangeOrganisationRequestField())
            .orElseGet(() -> ChangeOrganisationRequest.builder().build());
    }
}
