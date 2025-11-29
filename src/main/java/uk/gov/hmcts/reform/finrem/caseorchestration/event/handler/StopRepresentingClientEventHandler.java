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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NoticeOfChangeParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;

import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_POLICY;

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
        FinremCaseData finremCaseDataBefore = event.getCaseDetailsBefore().getData();

        // trying to revoke creator role if any
        assignCaseAccessService.findAndRevokeCreatorRole(finremCaseData.getCcdCaseId());

        if (!safeChangeOrganisationRequest(finremCaseData).isNoOrganisationsToAddOrRemove()) {
            revertOriginalOrgPolicyFromOriginalFinremCaseData(finremCaseData, finremCaseDataBefore);
            assignCaseAccessService.applyDecision(systemUserService.getSysUserToken(), finremCaseDetailsMapper
                .mapToCaseDetails(event.getCaseDetails()));
        }
    }

    // aac handles org policy modification based on the Change Organisation Request,
    // so we need to revert the org policies to their value before the event started
    private void revertOriginalOrgPolicyFromOriginalFinremCaseData(FinremCaseData finremCaseData,
                                                                   FinremCaseData originalFinremCaseData) {
        final boolean isApplicant = NoticeOfChangeParty.isApplicantForRepresentationChange(finremCaseData);
        final OrganisationPolicy litigantOrgPolicy = isApplicant
            ? originalFinremCaseData.getApplicantOrganisationPolicy()
            : originalFinremCaseData.getRespondentOrganisationPolicy();

        if (hasInvalidOrgPolicy(finremCaseData, isApplicant)) {
            if (isApplicant) {
                finremCaseData.setApplicantOrganisationPolicy(litigantOrgPolicy);
            } else {
                finremCaseData.setRespondentOrganisationPolicy(litigantOrgPolicy);
            }
        }
    }

    public boolean hasInvalidOrgPolicy(FinremCaseData finremCaseData, boolean isApplicant) {
        Optional<OrganisationPolicy> orgPolicy = Optional.ofNullable(isApplicant
            ? finremCaseData.getApplicantOrganisationPolicy()
            : finremCaseData.getRespondentOrganisationPolicy());

        return orgPolicy.isEmpty()
            || orgPolicy.get().getOrgPolicyCaseAssignedRole() == null
            || !orgPolicy.get().getOrgPolicyCaseAssignedRole().equalsIgnoreCase(
            isApplicant ? APP_SOLICITOR_POLICY : RESP_SOLICITOR_POLICY);
    }

    private ChangeOrganisationRequest safeChangeOrganisationRequest(FinremCaseData data) {
        return ofNullable(data.getChangeOrganisationRequestField())
            .orElseGet(() -> ChangeOrganisationRequest.builder().build());
    }
}
