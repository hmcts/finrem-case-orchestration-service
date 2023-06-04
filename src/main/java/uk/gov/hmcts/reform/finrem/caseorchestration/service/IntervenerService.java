package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.NoSuchUserException;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerChangeDetails;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class IntervenerService {

    private final AssignCaseAccessService assignCaseAccessService;
    private final PrdOrganisationService organisationService;
    private final SystemUserService systemUserService;

    public IntervenerChangeDetails removeIntervenerDetails(IntervenerWrapper intervenerWrapper, FinremCaseData caseData, Long caseId) {
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.REMOVED);
        intervenerChangeDetails.setIntervenerType(intervenerWrapper.getIntervenerType());

        if (intervenerWrapper.getIntervenerRepresented().equals(YesOrNo.YES)) {
            log.info("revoke case role for {} for case {}", intervenerWrapper.getIntervenerSolicitorCaseRole(), caseId);
            String orgId = intervenerWrapper.getIntervenerOrganisation().getOrganisation().getOrganisationID();
            String email = intervenerWrapper.getIntervenerSolEmail();
            remokeIntervenerRole(caseId, email, orgId, intervenerWrapper.getIntervenerSolicitorCaseRole().getValue());
        }

        intervenerWrapper.removeIntervenerWrapperFromCaseData(caseData);
        return intervenerChangeDetails;
    }

    public IntervenerChangeDetails updateIntervenerDetails(IntervenerWrapper intervenerWrapper, FinremCallbackRequest callbackRequest) {
        FinremCaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
        Long caseId = callbackRequest.getCaseDetails().getId();

        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.ADDED);
        intervenerChangeDetails.setIntervenerType(intervenerWrapper.getIntervenerType());

        if (intervenerWrapper != null) {
            if (intervenerWrapper.getIntervenerDateAdded() == null) {
                log.info("{} date intervener added to case {}", intervenerWrapper.getIntervenerType(), caseId);
                intervenerWrapper.setIntervenerDateAdded(LocalDate.now());
            }

            final String caseRole = intervenerWrapper.getIntervenerSolicitorCaseRole().getValue();
            if (intervenerWrapper.getIntervenerRepresented().equals(YesOrNo.YES)) {
                log.info("Add {} case role for case {}", caseRole, caseId);
                String orgId = intervenerWrapper.getIntervenerOrganisation().getOrganisation().getOrganisationID();
                String email = intervenerWrapper.getIntervenerSolEmail();
                checkIfIntervenerSolicitorDetailsChanged(intervenerWrapper, caseDetailsBefore, orgId, email);
                addIntervenerRole(caseId, email, orgId, caseRole);
            } else {
                FinremCaseData beforeData = caseDetailsBefore.getData();
                IntervenerWrapper beforeIntv = intervenerWrapper.getIntervenerWrapperFromCaseData(beforeData);
                if (ObjectUtils.isNotEmpty(beforeIntv)
                    && beforeIntv.getIntervenerRepresented() != null
                    && beforeIntv.getIntervenerRepresented().equals(YesOrNo.YES)) {

                    log.info("{} now not represented for case {}", intervenerWrapper.getIntervenerType(), caseId);
                    remokeIntervenerRole(caseId, beforeIntv.getIntervenerSolEmail(),
                        beforeIntv.getIntervenerOrganisation().getOrganisation().getOrganisationID(),
                        intervenerWrapper.getIntervenerSolicitorCaseRole().getValue());
                    intervenerWrapper.setIntervenerSolEmail(null);
                    intervenerWrapper.setIntervenerSolName(null);
                    intervenerWrapper.setIntervenerSolPhone(null);
                    intervenerWrapper.setIntervenerSolicitorFirm(null);
                    intervenerWrapper.setIntervenerSolicitorReference(null);
                }
                log.info("{}} add default case role and organisation for case {}", intervenerWrapper.getIntervenerType(), caseId);
                setDefaultOrgForintervener(intervenerWrapper);
            }
        }
        intervenerChangeDetails.setIntervenerDetails(intervenerWrapper);
        return intervenerChangeDetails;
    }

    private void checkIfIntervenerSolicitorDetailsChanged(IntervenerWrapper intervenerWrapper, FinremCaseDetails caseDetailsBefore, String orgId,
                                                          String email) {
        FinremCaseData beforeData = caseDetailsBefore.getData();
        IntervenerWrapper beforeIntv = intervenerWrapper.getIntervenerWrapperFromCaseData(beforeData);

        if (ObjectUtils.isNotEmpty(beforeIntv)
            && beforeIntv.getIntervenerRepresented() != null
            && beforeIntv.getIntervenerRepresented().equals(YesOrNo.YES)) {
            String beforeOrgId = beforeIntv.getIntervenerOrganisation().getOrganisation().getOrganisationID();
            if (!beforeOrgId.equals(orgId) || !beforeIntv.getIntervenerSolEmail().equals(email)) {
                remokeIntervenerRole(caseDetailsBefore.getId(), beforeIntv.getIntervenerSolEmail(),
                    beforeOrgId,
                    intervenerWrapper.getIntervenerSolicitorCaseRole().getValue());
            }
        }
    }

    private boolean checkIfIntervenerOneSolicitorRemoved(FinremCaseData caseData, FinremCaseData caseDataBefore) {
        return YesOrNo.YES.equals(caseDataBefore.getIntervenerOneWrapper().getIntervenerRepresented())
            && (caseData.getIntervenerOneWrapper().getIntervenerRepresented() == null
            || YesOrNo.NO.equals(caseData.getIntervenerOneWrapper().getIntervenerRepresented()));
    }

    private boolean checkIfIntervenerTwoSolicitorRemoved(FinremCaseData caseData, FinremCaseData caseDataBefore) {
        return YesOrNo.YES.equals(caseDataBefore.getIntervenerTwoWrapper().getIntervenerRepresented())
            && (caseData.getIntervenerTwoWrapper().getIntervenerRepresented() == null
            || YesOrNo.NO.equals(caseData.getIntervenerTwoWrapper().getIntervenerRepresented()));
    }

    private boolean checkIfIntervenerThreeSolicitorRemoved(FinremCaseData caseData, FinremCaseData caseDataBefore) {
        return YesOrNo.YES.equals(caseDataBefore.getIntervenerThreeWrapper().getIntervenerRepresented())
            && (caseData.getIntervenerThreeWrapper().getIntervenerRepresented() == null
            || YesOrNo.NO.equals(caseData.getIntervenerThreeWrapper().getIntervenerRepresented()));
    }

    private boolean checkIfIntervenerFourSolicitorRemoved(FinremCaseData caseData, FinremCaseData caseDataBefore) {
        return YesOrNo.YES.equals(caseDataBefore.getIntervenerFourWrapper().getIntervenerRepresented())
            && (caseData.getIntervenerFourWrapper().getIntervenerRepresented() == null
            || YesOrNo.NO.equals(caseData.getIntervenerFourWrapper().getIntervenerRepresented()));
    }

    public boolean checkIfAnyIntervenerSolicitorRemoved(FinremCaseData caseData, FinremCaseData caseDataBefore) {
        return checkIfIntervenerOneSolicitorRemoved(caseData, caseDataBefore) || checkIfIntervenerTwoSolicitorRemoved(caseData, caseDataBefore)
            || checkIfIntervenerThreeSolicitorRemoved(caseData, caseDataBefore) || checkIfIntervenerFourSolicitorRemoved(caseData, caseDataBefore);
    }

    private void setDefaultOrgForintervener(IntervenerWrapper intervenerWrapper) {
        Organisation organisation = Organisation.builder().organisationID(null).organisationName(null).build();
        intervenerWrapper.getIntervenerOrganisation().setOrganisation(organisation);
        intervenerWrapper.getIntervenerOrganisation().setOrgPolicyCaseAssignedRole(intervenerWrapper.getIntervenerSolicitorCaseRole().getValue());
        intervenerWrapper.getIntervenerOrganisation().setOrgPolicyReference(null);
    }


    public IntervenerChangeDetails setIntervenerAddedChangeDetails(IntervenerWrapper intervenerWrapper) {
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.ADDED);
        intervenerChangeDetails.setIntervenerType(intervenerWrapper.getIntervenerType());
        intervenerChangeDetails.setIntervenerDetails(
            intervenerWrapper);

        return intervenerChangeDetails;
    }

    public IntervenerChangeDetails setIntervenerRemovedChangeDetails(IntervenerWrapper intervenerWrapper) {
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.REMOVED);
        intervenerChangeDetails.setIntervenerType(intervenerWrapper.getIntervenerType());
        intervenerChangeDetails.setIntervenerDetails(
            intervenerWrapper);
        return intervenerChangeDetails;
    }

    private void addIntervenerRole(Long caseId, String email, String orgId, String caseRole) {
        organisationService.findUserByEmail(email, systemUserService.getSysUserToken())
            .ifPresentOrElse(
                userId -> assignCaseAccessService.grantCaseRoleToUser(caseId, userId, caseRole, orgId),
                throwNoSuchUserException(email)
            );
    }

    private void remokeIntervenerRole(Long caseId, String email, String orgId, String caseRole) {
        organisationService.findUserByEmail(email, systemUserService.getSysUserToken())
            .ifPresentOrElse(
                userId -> assignCaseAccessService.removeCaseRoleToUser(caseId, userId, caseRole, orgId),
                throwNoSuchUserException(email)
            );
    }

    private Runnable throwNoSuchUserException(String email) {
        return () -> {
            throw new NoSuchUserException(String.format("Could not find the user with email %s", email));
        };
    }
}
