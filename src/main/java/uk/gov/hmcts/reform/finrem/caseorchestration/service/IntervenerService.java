package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerChangeDetails;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class IntervenerService {

    private final AssignCaseAccessService assignCaseAccessService;
    private final PrdOrganisationService organisationService;
    private final SystemUserService systemUserService;

    public IntervenerChangeDetails removeIntervenerDetails(IntervenerWrapper intervenerWrapper,
                                                           List<String> errors,
                                                           FinremCaseData caseData,
                                                           Long caseId) {
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.REMOVED);
        intervenerChangeDetails.setIntervenerType(intervenerWrapper.getIntervenerType());
        intervenerChangeDetails.setIntervenerDetails(intervenerWrapper);

        if (intervenerWrapper.getIntervenerRepresented().equals(YesOrNo.YES)) {
            log.info("Revoke case role for {} for Case ID: {}", intervenerWrapper.getIntervenerSolicitorCaseRole(), caseId);
            String orgId = intervenerWrapper.getIntervenerOrganisation().getOrganisation().getOrganisationID();
            String email = intervenerWrapper.getIntervenerSolEmail();
            remokeIntervenerRole(caseId, email, orgId,
                intervenerWrapper.getIntervenerSolicitorCaseRole().getCcdCode(), errors);
        }

        intervenerWrapper.removeIntervenerWrapperFromCaseData(caseData);
        return intervenerChangeDetails;
    }

    public IntervenerChangeDetails updateIntervenerDetails(IntervenerWrapper intervenerWrapper,
                                                           List<String> errors,
                                                           FinremCallbackRequest callbackRequest) {
        FinremCaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
        Long caseId = callbackRequest.getCaseDetails().getId();
        validateIntervenerCountryOfResident(intervenerWrapper, errors);
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.ADDED);
        intervenerChangeDetails.setIntervenerType(intervenerWrapper.getIntervenerType());

        if (intervenerWrapper != null) {
            if (intervenerWrapper.getIntervenerDateAdded() == null) {
                log.info("{} date intervener added to Case ID: {}", intervenerWrapper.getIntervenerType(), caseId);
                intervenerWrapper.setIntervenerDateAdded(LocalDate.now());
            }

            final String caseRole = intervenerWrapper.getIntervenerSolicitorCaseRole().getCcdCode();
            if (intervenerWrapper.getIntervenerRepresented().equals(YesOrNo.YES)) {
                log.info("Add {} case role for Case ID: {}", caseRole, caseId);
                String orgId = intervenerWrapper.getIntervenerOrganisation().getOrganisation().getOrganisationID();
                String email = intervenerWrapper.getIntervenerSolEmail();
                checkIfIntervenerSolicitorDetailsChanged(intervenerWrapper, caseDetailsBefore, orgId, email, errors);
                addIntervenerRole(caseId, email, orgId, caseRole, errors);
            } else {
                FinremCaseData beforeData = caseDetailsBefore.getData();
                IntervenerWrapper beforeIntv = intervenerWrapper.getIntervenerWrapperFromCaseData(beforeData);
                if (ObjectUtils.isNotEmpty(beforeIntv)
                    && beforeIntv.getIntervenerRepresented() != null
                    && beforeIntv.getIntervenerRepresented().equals(YesOrNo.YES)) {

                    log.info("{} now not represented for Case ID: {}", intervenerWrapper.getIntervenerType(), caseId);
                    remokeIntervenerRole(caseId, beforeIntv.getIntervenerSolEmail(),
                        beforeIntv.getIntervenerOrganisation().getOrganisation().getOrganisationID(),
                        intervenerWrapper.getIntervenerSolicitorCaseRole().getCcdCode(), errors);
                    intervenerWrapper.setIntervenerSolEmail(null);
                    intervenerWrapper.setIntervenerSolName(null);
                    intervenerWrapper.setIntervenerSolPhone(null);
                    intervenerWrapper.setIntervenerSolicitorFirm(null);
                    intervenerWrapper.setIntervenerSolicitorReference(null);
                }
                log.info("{}} add default case role and organisation for Case ID: {}", intervenerWrapper.getIntervenerType(), caseId);
                setDefaultOrgForintervener(intervenerWrapper);
            }
        }
        intervenerChangeDetails.setIntervenerDetails(intervenerWrapper);
        return intervenerChangeDetails;
    }

    private void validateIntervenerCountryOfResident(IntervenerWrapper intervenerWrapper, List<String> errors) {
        if (intervenerWrapper.getIntervenerRepresented().equals(YesOrNo.NO)) {
            YesOrNo intervenerResideOutsideUK = intervenerWrapper.getIntervenerResideOutsideUK();
            String country = intervenerWrapper.getIntervenerAddress() != null
                ? intervenerWrapper.getIntervenerAddress().getCountry() : "";
            if (ObjectUtils.isNotEmpty(intervenerResideOutsideUK) && intervenerResideOutsideUK.equals(YesOrNo.YES) && ObjectUtils.isEmpty(country)) {
                errors.add("If intervener resides outside of UK, please provide the country of residence.");
            }
        }
    }

    private void checkIfIntervenerSolicitorDetailsChanged(IntervenerWrapper intervenerWrapper, FinremCaseDetails caseDetailsBefore, String orgId,
                                                          String email, List<String> errors) {
        FinremCaseData beforeData = caseDetailsBefore.getData();
        IntervenerWrapper beforeIntv = intervenerWrapper.getIntervenerWrapperFromCaseData(beforeData);

        if (ObjectUtils.isNotEmpty(beforeIntv)
            && beforeIntv.getIntervenerRepresented() != null
            && beforeIntv.getIntervenerRepresented().equals(YesOrNo.YES)) {
            String beforeOrgId = beforeIntv.getIntervenerOrganisation().getOrganisation().getOrganisationID();
            if (!beforeOrgId.equals(orgId) || !beforeIntv.getIntervenerSolEmail().equals(email)) {
                remokeIntervenerRole(caseDetailsBefore.getId(), beforeIntv.getIntervenerSolEmail(),
                    beforeOrgId,
                    intervenerWrapper.getIntervenerSolicitorCaseRole().getCcdCode(), errors);
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
        intervenerWrapper.getIntervenerOrganisation().setOrgPolicyCaseAssignedRole(intervenerWrapper.getIntervenerSolicitorCaseRole().getCcdCode());
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

    private void addIntervenerRole(Long caseId, String email, String orgId, String caseRole, List<String> errors) {
        Optional<String> userId = organisationService.findUserByEmail(email, systemUserService.getSysUserToken());
        if (userId.isPresent()) {
            assignCaseAccessService.grantCaseRoleToUser(caseId, userId.get(), caseRole, orgId);
        } else {
            logError(email, caseId, errors);
        }
    }

    private void remokeIntervenerRole(Long caseId, String email, String orgId, String caseRole, List<String> errors) {
        Optional<String> userId = organisationService.findUserByEmail(email, systemUserService.getSysUserToken());
        if (userId.isPresent()) {
            assignCaseAccessService.removeCaseRoleToUser(caseId, userId.get(), caseRole, orgId);
        } else {
            logError(email, caseId, errors);
        }
    }

    private void logError(String email, Long caseId, List<String> errors) {
        String error = String.format("Could not find the user with email %s for caseId %s", email, caseId);
        log.error(error);
        errors.add(error);
    }
}
