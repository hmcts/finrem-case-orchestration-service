package uk.gov.hmcts.reform.finrem.caseorchestration.event.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.event.StopRepresentingClientInfo;
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

@Service
@Slf4j
@RequiredArgsConstructor
public class StopRepresentingClientService {

    // This class is made to interact with AAC only
    // Given event.caseDetails is READ-ONLY

    private final AssignCaseAccessService assignCaseAccessService;

    private final SystemUserService systemUserService;

    private final FinremCaseDetailsMapper finremCaseDetailsMapper;

    private final ManageBarristerService manageBarristerService;

    private final BarristerChangeCaseAccessUpdater barristerChangeCaseAccessUpdater;

    private final CoreCaseDataService coreCaseDataService;

    private final IntervenerService intervenerService;

    private static FinremCaseData getFinremCaseDataBeforeFromInfo(StopRepresentingClientInfo info) {
        return info.getCaseDetailsBefore().getData();
    }

    private static FinremCaseData getFinremCaseDataFromInfo(StopRepresentingClientInfo info) {
        return info.getCaseDetails().getData();
    }

    private static long getCaseId(StopRepresentingClientInfo info) {
        return Long.parseLong(getFinremCaseDataFromInfo(info).getCcdCaseId());
    }

    private static Map<String, Object> clearChangeOrganisationRequestField() {
        Map<String, Object> map = new HashMap<>();
        map.put(CHANGE_ORGANISATION_REQUEST, null);
        return map;
    }

    /**
     * Applies case assignment based on who triggered the stop representing a client event.
     *
     * <p>
     * If the event is invoked by an intervener, the intervener representative request
     * is handled. Otherwise, the applicant or respondent representative request
     * is processed.
     *
     * @param info the stop representing client POJO containing the invocation context
     */
    public void applyCaseAssignment(StopRepresentingClientInfo info) {
        if (info.isInvokedByIntervener()) {
            handleIntervenerRepresentativeRequest(info);
        } else {
            handleApplicantOrRespondentRepresentativeRequest(info);
        }
    }

    private void handleApplicantOrRespondentRepresentativeRequest(StopRepresentingClientInfo info) {
        final FinremCaseData finremCaseData = getFinremCaseDataFromInfo(info);
        final CaseType caseType = finremCaseData.getCcdCaseType();
        final long caseId = getCaseId(info);

        sendAllBarristerChangeToCaseAssignmentService(info);
        boolean isNocRequestSent = sendNocRequestToCaseAssignmentService(info);

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

    private void handleIntervenerRepresentativeRequest(StopRepresentingClientInfo info) {
        final FinremCaseData finremCaseData = getFinremCaseDataFromInfo(info);
        final FinremCaseData finremCaseDataBefore = getFinremCaseDataBeforeFromInfo(info);

        finremCaseDataBefore.getInterveners().forEach(originalWrapper -> {
            IntervenerType it = originalWrapper.getIntervenerType();
            if (it != null) {
                finremCaseData.getInterveners().stream()
                    .filter(wrapper -> it.equals(wrapper.getIntervenerType()))
                    .findAny()
                    .ifPresent(wrapper -> {
                        if (isIntervenerOrganisationDifference(wrapper, originalWrapper)) {
                            intervenerService.revokeIntervener(info.getCaseDetails().getId(), originalWrapper);
                        }
                    });
            }
        });

        sendAllBarristerChangeToCaseAssignmentService(info);
    }

    private void sendAllBarristerChangeToCaseAssignmentService(StopRepresentingClientInfo info) {
        sendBarristerChangesToCaseAssignmentService(info, BarristerParty.APPLICANT);
        sendBarristerChangesToCaseAssignmentService(info, BarristerParty.RESPONDENT);
        sendBarristerChangesToCaseAssignmentService(info, BarristerParty.INTERVENER1);
        sendBarristerChangesToCaseAssignmentService(info, BarristerParty.INTERVENER2);
        sendBarristerChangesToCaseAssignmentService(info, BarristerParty.INTERVENER3);
        sendBarristerChangesToCaseAssignmentService(info, BarristerParty.INTERVENER4);
    }

    private void sendBarristerChangesToCaseAssignmentService(StopRepresentingClientInfo info, BarristerParty barristerParty) {
        final long caseId = getCaseId(info);
        final FinremCaseData finremCaseDataBefore = getFinremCaseDataBeforeFromInfo(info);

        BarristerChange barristerChange = manageBarristerService
            .getBarristerChange(info.getCaseDetails(), finremCaseDataBefore, barristerParty);
        barristerChangeCaseAccessUpdater.executeBarristerChange(caseId, barristerChange);
    }

    private boolean sendNocRequestToCaseAssignmentService(StopRepresentingClientInfo info) {
        final FinremCaseData finremCaseData = getFinremCaseDataFromInfo(info);
        final FinremCaseData originalFinremCaseData = getFinremCaseDataBeforeFromInfo(info);

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
                buildCaseDetailsFromEventCaseData(info));
            return true;
        }
        throw new IllegalStateException(format("%s - ChangeOrganisationRequest populated with unknown or null NOC Party : %s",
            finremCaseData.getContactDetailsWrapper().getNocParty(),
            finremCaseData.getCcdCaseId()));
    }

    private CaseDetails buildCaseDetailsFromEventCaseData(StopRepresentingClientInfo info) {
        return finremCaseDetailsMapper.mapToCaseDetails(info.getCaseDetails());
    }

    private void clearChangeOrganisationRequestAfterThisEvent(CaseType caseType, long caseId) {
        // to reset the targeted field by case id and case type only
        // coreCaseDataService loads the case data again in the internal event call.
        coreCaseDataService.performPostSubmitCallback(caseType, caseId,
            INTERNAL_CHANGE_UPDATE_CASE.getCcdType(), caseDetails -> clearChangeOrganisationRequestField());
    }
}
