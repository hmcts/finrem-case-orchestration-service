package uk.gov.hmcts.reform.finrem.caseorchestration.event.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.event.StopRepresentingClientEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NoticeOfChangeParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
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
    // @Async
    public void handleEvent(StopRepresentingClientEvent event) {
        // @Async if success message screen needed
        FinremCaseData finremCaseData = event.getCaseDetails().getData();

        // trying to revoke creator role if any
        assignCaseAccessService.findAndRevokeCreatorRole(finremCaseData.getCcdCaseId());

        if (!safeChangeOrganisationRequest(finremCaseData).isNoOrganisationsToAddOrRemove()) {
            revertOrgPolicyToOriginalOrgPolicy(event.getCaseDetails().getData(), event.getCaseDetailsBefore().getData());
            assignCaseAccessService.applyDecision(systemUserService.getSysUserToken(), finremCaseDetailsMapper
                .mapToCaseDetails(event.getCaseDetails()));
        }
    }

    // aac handles org policy modification based on the Change Organisation Request,
    // so we need to revert the org policies to their value before the event started
    private void revertOrgPolicyToOriginalOrgPolicy(FinremCaseData finremCaseData,
                                                    FinremCaseData originalFinremCaseData) {
        final boolean isApplicant = NoticeOfChangeParty.isApplicantForRepresentationChange(finremCaseData);
        final OrganisationPolicy litigantOrgPolicy = isApplicant
            ? originalFinremCaseData.getApplicantOrganisationPolicy()
            : originalFinremCaseData.getRespondentOrganisationPolicy();

        if (isApplicant) {
            finremCaseData.setApplicantOrganisationPolicy(litigantOrgPolicy);
        } else {
            finremCaseData.setRespondentOrganisationPolicy(litigantOrgPolicy);
        }
    }

    private ChangeOrganisationRequest safeChangeOrganisationRequest(FinremCaseData data) {
        return ofNullable(data.getChangeOrganisationRequestField())
            .orElseGet(() -> ChangeOrganisationRequest.builder().build());
    }
}
