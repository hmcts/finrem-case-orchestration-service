package uk.gov.hmcts.reform.finrem.caseorchestration.event.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.event.StopRepresentingClientEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.BarristerChange;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NoticeOfChangeParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.BarristerChangeCaseAccessUpdater;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.ManageBarristerService;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StopRepresentingClientEventHandler {

    // This class is made to interact with AAC only
    // Given event.caseDetails is READ-ONLY

    private final AssignCaseAccessService assignCaseAccessService;

    private final SystemUserService systemUserService;

    private final FinremCaseDetailsMapper finremCaseDetailsMapper;

    private final ManageBarristerService manageBarristerService;

    private final BarristerChangeCaseAccessUpdater barristerChangeCaseAccessUpdater;

    @EventListener
    // @Async
    public void handleEvent(StopRepresentingClientEvent event) {
        // Enable @Async to display the success page,
        // but only if EXUI-3746 allows hiding the "Close and return to case details" button.
        final FinremCaseData finremCaseData = event.getCaseDetails().getData();
        final FinremCaseData finremCaseDataBefore = event.getCaseDetailsBefore().getData();
        final long caseId = event.getCaseDetails().getId();

        // trying to revoke creator role if any
        assignCaseAccessService.findAndRevokeCreatorRole(String.valueOf(caseId));

        BarristerChange applicantBarristerChange = manageBarristerService
            .getBarristerChange(event.getCaseDetails(), finremCaseDataBefore, CaseRole.APP_SOLICITOR);
        barristerChangeCaseAccessUpdater.executeBarristerChange(caseId, applicantBarristerChange);

        BarristerChange respondentBarristerChange = manageBarristerService
            .getBarristerChange(event.getCaseDetails(), finremCaseDataBefore, CaseRole.RESP_SOLICITOR);
        barristerChangeCaseAccessUpdater.executeBarristerChange(caseId, respondentBarristerChange);

        revertOrgPolicyToOriginalOrgPolicy(finremCaseData, finremCaseDataBefore);
        assignCaseAccessService.applyDecision(systemUserService.getSysUserToken(), finremCaseDetailsMapper
            .mapToCaseDetails(event.getCaseDetails()));
    }

    // aac handles org policy modification based on the Change Organisation Request,
    // so we need to revert the org policies to their value before the event started
    // Refer to NoticeOfChangeService.persistOriginalOrgPoliciesWhenRevokingAccess
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
}
