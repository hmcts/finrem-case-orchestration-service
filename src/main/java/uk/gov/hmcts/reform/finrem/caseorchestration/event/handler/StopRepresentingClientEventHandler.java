package uk.gov.hmcts.reform.finrem.caseorchestration.event.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.event.StopRepresentingClientEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.BarristerChange;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IntervenerService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.BarristerChangeCaseAccessUpdater;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.ManageBarristerService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ccd.CoreCaseDataService;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.INTERNAL_CHANGE_UPDATE_CASE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CHANGE_ORGANISATION_REQUEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NoticeOfChangeParty.isApplicantForRepresentationChange;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NoticeOfChangeParty.isRespondentForRepresentationChange;

@Slf4j
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

    private final CoreCaseDataService coreCaseDataService;

    private final IntervenerService intervenerService;

    private static FinremCaseData getFinremCaseDataBeforeFromEvent(StopRepresentingClientEvent event) {
        return event.getCaseDetailsBefore().getData();
    }

    private static FinremCaseData getFinremCaseDataFromEvent(StopRepresentingClientEvent event) {
        return event.getCaseDetails().getData();
    }

    private static long getCaseId(StopRepresentingClientEvent event) {
        return Long.parseLong(getFinremCaseDataFromEvent(event).getCcdCaseId());
    }

    private static Map<String, Object> clearChangeOrganisationRequestField() {
        Map<String, Object> map = new HashMap<>();
        map.put(CHANGE_ORGANISATION_REQUEST, null);
        return map;
    }

    @EventListener
    // @Async
    public void handleEvent(StopRepresentingClientEvent event) {
        // Enable @Async to display the success page,
        // but only if EXUI-3746 allows hiding the "Close and return to case details" button.
        if (event.isInvokedByIntervener()) {
            handleIntervenerRepresentativeRequest(event);
        } else {
            handleApplicantOrRespondentRepresentativeRequest(event);
        }
    }

    private void handleApplicantOrRespondentRepresentativeRequest(StopRepresentingClientEvent event) {
        final FinremCaseData finremCaseData = getFinremCaseDataFromEvent(event);
        final CaseType caseType = finremCaseData.getCcdCaseType();
        final long caseId = getCaseId(event);

        sendAllBarristerChangeToCaseAssignmentService(event);
        boolean isNocRequestSent = sendNocRequestToCaseAssignmentService(event);

        if (isNocRequestSent) {
            // save a call if changeOrganisationRequestField is null
            clearChangeOrganisationRequestAfterThisEvent(caseType, caseId);
        }
    }

    private boolean isIntervenerOrganisationDifference(IntervenerWrapper intervenerWrapper,
                                                       IntervenerWrapper originalIntervenerWrapper) {
        return !Objects.equals(
            intervenerWrapper.getIntervenerOrganisation(),
            originalIntervenerWrapper.getIntervenerOrganisation()
        );
    }

    private void handleIntervenerRepresentativeRequest(StopRepresentingClientEvent event) {
        final FinremCaseData finremCaseData = getFinremCaseDataFromEvent(event);
        final FinremCaseData finremCaseDataBefore = getFinremCaseDataBeforeFromEvent(event);

        finremCaseDataBefore.getInterveners().forEach(originalWrapper -> {
            IntervenerType it = originalWrapper.getIntervenerType();
            if (it != null) {
                finremCaseData.getInterveners().stream()
                    .filter(wrapper -> it.equals(wrapper.getIntervenerType()))
                    .findAny()
                    .ifPresent(wrapper -> {
                        if (isIntervenerOrganisationDifference(wrapper, originalWrapper)) {
                            intervenerService.revokeIntervener(event.getCaseDetails().getId(), originalWrapper);
                        }
                    });
            }
        });

        sendAllBarristerChangeToCaseAssignmentService(event);
    }

    private void sendAllBarristerChangeToCaseAssignmentService(StopRepresentingClientEvent event) {
        sendBarristerChangesToCaseAssignmentService(event, BarristerParty.APPLICANT);
        sendBarristerChangesToCaseAssignmentService(event, BarristerParty.RESPONDENT);
        sendBarristerChangesToCaseAssignmentService(event, BarristerParty.INTERVENER1);
        sendBarristerChangesToCaseAssignmentService(event, BarristerParty.INTERVENER2);
        sendBarristerChangesToCaseAssignmentService(event, BarristerParty.INTERVENER3);
        sendBarristerChangesToCaseAssignmentService(event, BarristerParty.INTERVENER4);
    }

    private void sendBarristerChangesToCaseAssignmentService(StopRepresentingClientEvent event, BarristerParty barristerParty) {
        final long caseId = getCaseId(event);
        final FinremCaseData finremCaseDataBefore = getFinremCaseDataBeforeFromEvent(event);

        BarristerChange barristerChange = manageBarristerService
            .getBarristerChange(event.getCaseDetails(), finremCaseDataBefore, barristerParty);
        barristerChangeCaseAccessUpdater.executeBarristerChange(caseId, barristerChange);
    }

    private boolean sendNocRequestToCaseAssignmentService(StopRepresentingClientEvent event) {
        final FinremCaseData finremCaseData = getFinremCaseDataFromEvent(event);
        final FinremCaseData originalFinremCaseData = getFinremCaseDataBeforeFromEvent(event);

        // to check if ChangeOrganisationRequest populated, otherwise skip it
        if (finremCaseData.getChangeOrganisationRequestField() == null) {
            log.info("{} - Not sending request to case assignment service due to changeOrganisationRequestField is null",
                finremCaseData.getCcdCaseId());
            return false;
        }

        // aac handles org policy modification based on the Change Organisation Request,
        // so we need to revert the org policies to their value before the event started
        // Refer to NoticeOfChangeService.persistOriginalOrgPoliciesWhenRevokingAccess
        boolean isReverted = false;
        if (isApplicantForRepresentationChange(finremCaseData)) {
            finremCaseData.setApplicantOrganisationPolicy(originalFinremCaseData.getApplicantOrganisationPolicy());
            isReverted = true;
        } else if (isRespondentForRepresentationChange(finremCaseData)) {
            finremCaseData.setRespondentOrganisationPolicy(originalFinremCaseData.getRespondentOrganisationPolicy());
            isReverted = true;
        }

        // Going to apply decision
        if (isReverted) {
            assignCaseAccessService.applyDecision(systemUserService.getSysUserToken(),
                buildCaseDetailsFromEventCaseData(event));
            return true;
        }
        throw new IllegalStateException(format("%s - ChangeOrganisationRequest populated with unknown or null NOC Party : %s",
            finremCaseData.getContactDetailsWrapper().getNocParty(),
            finremCaseData.getCcdCaseId()));
    }

    private CaseDetails buildCaseDetailsFromEventCaseData(StopRepresentingClientEvent event) {
        return finremCaseDetailsMapper.mapToCaseDetails(event.getCaseDetails());
    }

    private void clearChangeOrganisationRequestAfterThisEvent(CaseType caseType, long caseId) {
        // to reset the targeted field by case id and case type only
        // coreCaseDataService loads the case data again in the internal event call.
        coreCaseDataService.performPostSubmitCallback(caseType, caseId,
            INTERNAL_CHANGE_UPDATE_CASE.getCcdType(), caseDetails -> clearChangeOrganisationRequestField());
    }
}
