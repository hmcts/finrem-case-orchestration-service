package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.NoSuchUserException;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.intervener.IntervenerFourToIntervenerDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.intervener.IntervenerOneToIntervenerDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.intervener.IntervenerThreeToIntervenerDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.intervener.IntervenerTwoToIntervenerDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFourWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOneWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThreeWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwoWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerChangeDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class IntervenerService {

    private final AssignCaseAccessService assignCaseAccessService;
    private final PrdOrganisationService organisationService;
    private final SystemUserService systemUserService;
    private final IntervenerOneToIntervenerDetailsMapper intervenerOneDetailsMapper;
    private final IntervenerTwoToIntervenerDetailsMapper intervenerTwoDetailsMapper;
    private final IntervenerThreeToIntervenerDetailsMapper intervenerThreeDetailsMapper;
    private final IntervenerFourToIntervenerDetailsMapper intervenerFourDetailsMapper;

    public IntervenerChangeDetails removeIntervenerOneDetails(FinremCaseData caseData, Long caseId) {
        IntervenerOneWrapper intervenerOneWrapper = caseData.getIntervenerOneWrapper();
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.REMOVED);
        intervenerChangeDetails.setIntervenerType(IntervenerType.INTERVENER_ONE);

        if (intervenerOneWrapper.getIntervener1Represented().equals(YesOrNo.YES)) {
            log.info("revoke case role for intervener1 for case {}", caseId);
            String orgId = intervenerOneWrapper.getIntervener1Organisation().getOrganisation().getOrganisationID();
            String email = intervenerOneWrapper.getIntervener1SolEmail();
            log.info("intervener1 has org id {} and email {} for case {}", orgId, email, caseId);
            remokeIntervenerRole(caseId, email, orgId, CaseRole.INTVR_SOLICITOR_1.getValue());
        }
        caseData.setIntervenerOneWrapper(null);
        return intervenerChangeDetails;
    }

    public IntervenerChangeDetails removeIntervenerTwoDetails(FinremCaseData caseData, Long caseId) {
        IntervenerTwoWrapper wrapper = caseData.getIntervenerTwoWrapper();
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.REMOVED);
        intervenerChangeDetails.setIntervenerType(IntervenerType.INTERVENER_TWO);
        if (wrapper.getIntervener2Represented().equals(YesOrNo.YES)) {
            log.info("revoke case role for intervener2 for case {}", caseId);
            String orgId = wrapper.getIntervener2Organisation().getOrganisation().getOrganisationID();
            String email = wrapper.getIntervener2SolEmail();
            remokeIntervenerRole(caseId, email, orgId, CaseRole.INTVR_SOLICITOR_2.getValue());
        }
        caseData.setIntervenerTwoWrapper(null);
        return intervenerChangeDetails;
    }

    public IntervenerChangeDetails removeIntervenerThreeDetails(FinremCaseData caseData, Long caseId) {
        IntervenerThreeWrapper wrapper = caseData.getIntervenerThreeWrapper();
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.REMOVED);
        intervenerChangeDetails.setIntervenerType(IntervenerType.INTERVENER_THREE);
        if (wrapper.getIntervener3Represented().equals(YesOrNo.YES)) {
            log.info("revoke case role for intervener3 for case {}", caseId);
            String orgId = wrapper.getIntervener3Organisation().getOrganisation().getOrganisationID();
            String email = wrapper.getIntervener3SolEmail();
            remokeIntervenerRole(caseId, email, orgId, CaseRole.INTVR_SOLICITOR_3.getValue());
        }
        caseData.setIntervenerThreeWrapper(null);
        return intervenerChangeDetails;
    }

    public IntervenerChangeDetails removeIntervenerFourDetails(FinremCaseData caseData, Long caseId) {
        IntervenerFourWrapper wrapper = caseData.getIntervenerFourWrapper();
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.REMOVED);
        intervenerChangeDetails.setIntervenerType(IntervenerType.INTERVENER_FOUR);
        if (wrapper.getIntervener4Represented().equals(YesOrNo.YES)) {
            log.info("revoke case role for intervener4 for case {}", caseId);
            String orgId = wrapper.getIntervener4Organisation().getOrganisation().getOrganisationID();
            String email = wrapper.getIntervener4SolEmail();
            remokeIntervenerRole(caseId, email, orgId, CaseRole.INTVR_SOLICITOR_4.getValue());
        }
        caseData.setIntervenerFourWrapper(null);
        return intervenerChangeDetails;
    }

    public IntervenerChangeDetails updateIntervenerFourDetails(FinremCallbackRequest callbackRequest) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
        FinremCaseData caseData = caseDetails.getData();
        Long caseId = callbackRequest.getCaseDetails().getId();
        IntervenerChangeDetails intervenerFourChangeDetails = new IntervenerChangeDetails();
        intervenerFourChangeDetails.setIntervenerType(IntervenerType.INTERVENER_FOUR);
        intervenerFourChangeDetails.setIntervenerAction(IntervenerAction.ADDED);

        IntervenerFourWrapper intervenerFourWrapper = caseData.getIntervenerFourWrapper();
        if (intervenerFourWrapper != null) {
            if (intervenerFourWrapper.getIntervener4DateAdded() == null) {
                log.info("Intervener4 date intervener added to case {}", caseId);
                intervenerFourWrapper.setIntervener4DateAdded(LocalDate.now());
            }

            final String caseRole = CaseRole.INTVR_SOLICITOR_4.getValue();
            if (intervenerFourWrapper.getIntervener4Represented().equals(YesOrNo.YES)) {
                log.info("Add Intervener3 case role for case {}", caseId);
                String orgId = intervenerFourWrapper.getIntervener4Organisation().getOrganisation().getOrganisationID();
                String email = intervenerFourWrapper.getIntervener4SolEmail();
                checkIfIntervenerFourSolDetailsChanged(caseDetailsBefore, orgId, email);
                addIntervenerRole(caseId, email, orgId, caseRole);
            } else {
                FinremCaseData beforeData = caseDetailsBefore.getData();
                IntervenerFourWrapper beforeIntv = beforeData.getIntervenerFourWrapper();
                if (ObjectUtils.isNotEmpty(beforeIntv)
                    && beforeIntv.getIntervener4Represented() != null
                    && beforeIntv.getIntervener4Represented().equals(YesOrNo.YES)) {
                    log.info("Intervener4 now not represented for case {}", caseId);
                    remokeIntervenerRole(caseId, beforeIntv.getIntervener4SolEmail(),
                        beforeIntv.getIntervener4Organisation().getOrganisation().getOrganisationID(),
                        CaseRole.INTVR_SOLICITOR_4.getValue());
                    intervenerFourWrapper.setIntervener4SolEmail(null);
                    intervenerFourWrapper.setIntervener4SolName(null);
                    intervenerFourWrapper.setIntervener4SolPhone(null);
                    intervenerFourWrapper.setIntervener4SolicitorFirm(null);
                    intervenerFourWrapper.setIntervener4SolicitorReference(null);
                }
                log.info("Intervener4 add default case role and organisation for case {}", caseId);
                setDefaultOrgForintervenerFour(intervenerFourWrapper);

            }
        }
        intervenerFourChangeDetails.setIntervenerDetails(
            intervenerFourDetailsMapper.mapToIntervenerDetails(intervenerFourWrapper));
        return intervenerFourChangeDetails;
    }

    public IntervenerChangeDetails updateIntervenerThreeDetails(FinremCallbackRequest callbackRequest) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
        FinremCaseData caseData = caseDetails.getData();
        Long caseId = callbackRequest.getCaseDetails().getId();

        IntervenerChangeDetails intervenerThreeChangeDetails = new IntervenerChangeDetails();
        intervenerThreeChangeDetails.setIntervenerType(IntervenerType.INTERVENER_THREE);
        intervenerThreeChangeDetails.setIntervenerAction(IntervenerAction.ADDED);

        IntervenerThreeWrapper intervenerThreeWrapper = caseData.getIntervenerThreeWrapper();
        if (intervenerThreeWrapper != null) {
            if (intervenerThreeWrapper.getIntervener3DateAdded() == null) {
                log.info("Intervener3 date intervener added to case {}", caseId);
                intervenerThreeWrapper.setIntervener3DateAdded(LocalDate.now());
            }

            final String caseRole = CaseRole.INTVR_SOLICITOR_3.getValue();
            if (intervenerThreeWrapper.getIntervener3Represented().equals(YesOrNo.YES)) {
                log.info("Add Intervener3 case role for case {}", caseId);
                String orgId = intervenerThreeWrapper.getIntervener3Organisation().getOrganisation().getOrganisationID();
                String email = intervenerThreeWrapper.getIntervener3SolEmail();
                checkIfIntervenerThreeSolDetailsChanged(caseDetailsBefore, orgId, email);
                addIntervenerRole(caseId, email, orgId, caseRole);
            } else {
                FinremCaseData beforeData = caseDetailsBefore.getData();
                IntervenerThreeWrapper beforeIntv = beforeData.getIntervenerThreeWrapper();
                if (ObjectUtils.isNotEmpty(beforeIntv)
                    && beforeIntv.getIntervener3Represented() != null
                    && beforeIntv.getIntervener3Represented().equals(YesOrNo.YES)) {

                    log.info("Intervener3 now not represented for case {}", caseId);
                    remokeIntervenerRole(caseId, beforeIntv.getIntervener3SolEmail(),
                        beforeIntv.getIntervener3Organisation().getOrganisation().getOrganisationID(),
                        CaseRole.INTVR_SOLICITOR_3.getValue());
                    intervenerThreeWrapper.setIntervener3SolEmail(null);
                    intervenerThreeWrapper.setIntervener3SolName(null);
                    intervenerThreeWrapper.setIntervener3SolPhone(null);
                    intervenerThreeWrapper.setIntervener3SolicitorFirm(null);
                    intervenerThreeWrapper.setIntervener3SolicitorReference(null);
                }
                log.info("Intervener3 add default case role and organisation for case {}", caseId);
                setDefaultOrgForintervenerThree(intervenerThreeWrapper);
            }
        }
        intervenerThreeChangeDetails.setIntervenerDetails(
            intervenerThreeDetailsMapper.mapToIntervenerDetails(intervenerThreeWrapper));
        return intervenerThreeChangeDetails;
    }

    public IntervenerChangeDetails updateIntervenerTwoDetails(FinremCallbackRequest callbackRequest) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
        FinremCaseData caseData = caseDetails.getData();
        Long caseId = callbackRequest.getCaseDetails().getId();

        IntervenerChangeDetails intervenerTwoChangeDetails = new IntervenerChangeDetails();
        intervenerTwoChangeDetails.setIntervenerType(IntervenerType.INTERVENER_TWO);
        intervenerTwoChangeDetails.setIntervenerAction(IntervenerAction.ADDED);

        IntervenerTwoWrapper intervenerTwoWrapper = caseData.getIntervenerTwoWrapper();
        if (intervenerTwoWrapper != null) {
            if (intervenerTwoWrapper.getIntervener2DateAdded() == null) {
                log.info("Intervener2 date intervener added to case {}", caseId);
                intervenerTwoWrapper.setIntervener2DateAdded(LocalDate.now());
            }

            final String caseRole = CaseRole.INTVR_SOLICITOR_2.getValue();
            if (intervenerTwoWrapper.getIntervener2Represented().equals(YesOrNo.YES)) {
                log.info("Add Intervener2 case role for case {}", caseId);
                String orgId = intervenerTwoWrapper.getIntervener2Organisation().getOrganisation().getOrganisationID();
                String email = intervenerTwoWrapper.getIntervener2SolEmail();
                checkIfIntervenerTwoSolDetailsChanged(caseDetailsBefore, orgId, email);
                addIntervenerRole(caseId, email, orgId, caseRole);
            } else {
                FinremCaseData beforeData = caseDetailsBefore.getData();
                IntervenerTwoWrapper beforeIntv = beforeData.getIntervenerTwoWrapper();
                if (ObjectUtils.isNotEmpty(beforeIntv)
                    && beforeIntv.getIntervener2Represented() != null
                    && beforeIntv.getIntervener2Represented().equals(YesOrNo.YES)) {

                    log.info("Intervener2 now not represented for case {}", caseId);
                    remokeIntervenerRole(caseId, beforeIntv.getIntervener2SolEmail(),
                        beforeIntv.getIntervener2Organisation().getOrganisation().getOrganisationID(),
                        CaseRole.INTVR_SOLICITOR_2.getValue());
                    intervenerTwoWrapper.setIntervener2SolEmail(null);
                    intervenerTwoWrapper.setIntervener2SolName(null);
                    intervenerTwoWrapper.setIntervener2SolPhone(null);
                    intervenerTwoWrapper.setIntervener2SolicitorFirm(null);
                    intervenerTwoWrapper.setIntervener2SolicitorReference(null);
                }
                log.info("Intervener2 add default case role and organisation for case {}", caseId);
                setDefaultOrgForintervenerTwo(intervenerTwoWrapper);

            }
        }
        intervenerTwoChangeDetails.setIntervenerDetails(
            intervenerTwoDetailsMapper.mapToIntervenerDetails(intervenerTwoWrapper));
        return intervenerTwoChangeDetails;
    }

    public IntervenerChangeDetails updateIntervenerOneDetails(FinremCallbackRequest callbackRequest) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
        FinremCaseData caseData = caseDetails.getData();
        Long caseId = callbackRequest.getCaseDetails().getId();

        IntervenerChangeDetails intervenerOneChangeDetails = new IntervenerChangeDetails();
        intervenerOneChangeDetails.setIntervenerAction(IntervenerAction.ADDED);
        intervenerOneChangeDetails.setIntervenerType(IntervenerType.INTERVENER_ONE);

        IntervenerOneWrapper intervenerOneWrapper = caseData.getIntervenerOneWrapper();
        if (intervenerOneWrapper != null) {
            if (intervenerOneWrapper.getIntervener1DateAdded() == null) {
                log.info("Intervener1 date intervener added to case {}", caseId);
                intervenerOneWrapper.setIntervener1DateAdded(LocalDate.now());
            }

            final String caseRole = CaseRole.INTVR_SOLICITOR_1.getValue();
            if (intervenerOneWrapper.getIntervener1Represented().equals(YesOrNo.YES)) {
                log.info("Add Intervener1 case role for case {}", caseId);
                String orgId = intervenerOneWrapper.getIntervener1Organisation().getOrganisation().getOrganisationID();
                String email = intervenerOneWrapper.getIntervener1SolEmail();
                checkIfIntervenerOneSolDetailsChanged(caseDetailsBefore, orgId, email);
                addIntervenerRole(caseId, email, orgId, caseRole);
            } else {
                FinremCaseData beforeData = caseDetailsBefore.getData();
                IntervenerOneWrapper beforeIntv = beforeData.getIntervenerOneWrapper();
                if (ObjectUtils.isNotEmpty(beforeIntv)
                    && beforeIntv.getIntervener1Represented() != null
                    && beforeIntv.getIntervener1Represented().equals(YesOrNo.YES)) {

                    log.info("Intervener1 now not represented for case {}", caseId);
                    remokeIntervenerRole(caseId, beforeIntv.getIntervener1SolEmail(),
                        beforeIntv.getIntervener1Organisation().getOrganisation().getOrganisationID(),
                        CaseRole.INTVR_SOLICITOR_1.getValue());
                    intervenerOneWrapper.setIntervener1SolEmail(null);
                    intervenerOneWrapper.setIntervener1SolName(null);
                    intervenerOneWrapper.setIntervener1SolPhone(null);
                    intervenerOneWrapper.setIntervener1SolicitorFirm(null);
                    intervenerOneWrapper.setIntervener1SolicitorReference(null);
                }
                log.info("Intervener1 add default case role and organisation for case {}", caseId);
                setDefaultOrgForintervenerOne(intervenerOneWrapper);
            }
        }
        intervenerOneChangeDetails.setIntervenerDetails(
            intervenerOneDetailsMapper.mapToIntervenerDetails(intervenerOneWrapper));
        return intervenerOneChangeDetails;
    }

    private void checkIfIntervenerFourSolDetailsChanged(FinremCaseDetails caseDetailsBefore, String orgId, String email) {
        FinremCaseData beforeData = caseDetailsBefore.getData();
        IntervenerFourWrapper beforeIntv = beforeData.getIntervenerFourWrapper();

        if (ObjectUtils.isNotEmpty(beforeIntv)
            && beforeIntv.getIntervener4Represented() != null
            && beforeIntv.getIntervener4Represented().equals(YesOrNo.YES)) {
            String beforeOrgId = beforeIntv.getIntervener4Organisation().getOrganisation().getOrganisationID();
            if (!beforeOrgId.equals(orgId) || !beforeIntv.getIntervener4SolEmail().equals(email)) {
                remokeIntervenerRole(caseDetailsBefore.getId(), beforeIntv.getIntervener4SolEmail(),
                    beforeOrgId,
                    CaseRole.INTVR_SOLICITOR_4.getValue());
            }
        }
    }

    private void checkIfIntervenerThreeSolDetailsChanged(FinremCaseDetails caseDetailsBefore, String orgId, String email) {
        FinremCaseData beforeData = caseDetailsBefore.getData();
        IntervenerThreeWrapper beforeIntv = beforeData.getIntervenerThreeWrapper();

        if (ObjectUtils.isNotEmpty(beforeIntv)
            && beforeIntv.getIntervener3Represented() != null
            && beforeIntv.getIntervener3Represented().equals(YesOrNo.YES)) {
            String beforeOrgId = beforeIntv.getIntervener3Organisation().getOrganisation().getOrganisationID();
            if (!beforeOrgId.equals(orgId) || !beforeIntv.getIntervener3SolEmail().equals(email)) {
                remokeIntervenerRole(caseDetailsBefore.getId(), beforeIntv.getIntervener3SolEmail(),
                    beforeOrgId,
                    CaseRole.INTVR_SOLICITOR_3.getValue());
            }
        }
    }

    private void checkIfIntervenerTwoSolDetailsChanged(FinremCaseDetails caseDetailsBefore, String orgId, String email) {
        FinremCaseData beforeData = caseDetailsBefore.getData();
        IntervenerTwoWrapper beforeIntv = beforeData.getIntervenerTwoWrapper();

        if (ObjectUtils.isNotEmpty(beforeIntv)
            && beforeIntv.getIntervener2Represented() != null
            && beforeIntv.getIntervener2Represented().equals(YesOrNo.YES)) {
            String beforeOrgId = beforeIntv.getIntervener2Organisation().getOrganisation().getOrganisationID();
            if (!beforeOrgId.equals(orgId) || !beforeIntv.getIntervener2SolEmail().equals(email)) {
                remokeIntervenerRole(caseDetailsBefore.getId(), beforeIntv.getIntervener2SolEmail(),
                    beforeOrgId,
                    CaseRole.INTVR_SOLICITOR_2.getValue());
            }
        }
    }

    private void checkIfIntervenerOneSolDetailsChanged(FinremCaseDetails caseDetailsBefore, String orgId, String email) {
        FinremCaseData beforeData = caseDetailsBefore.getData();
        IntervenerOneWrapper beforeIntv = beforeData.getIntervenerOneWrapper();

        if (ObjectUtils.isNotEmpty(beforeIntv)
            && beforeIntv.getIntervener1Represented() != null
            && beforeIntv.getIntervener1Represented().equals(YesOrNo.YES)) {
            String beforeOrgId = beforeIntv.getIntervener1Organisation().getOrganisation().getOrganisationID();
            if (!beforeOrgId.equals(orgId) || !beforeIntv.getIntervener1SolEmail().equals(email)) {
                remokeIntervenerRole(caseDetailsBefore.getId(), beforeIntv.getIntervener1SolEmail(),
                    beforeOrgId,
                    CaseRole.INTVR_SOLICITOR_1.getValue());
            }
        }
    }

    private boolean checkIfIntervenerOneSolicitorRemoved(FinremCaseData caseData, FinremCaseData caseDataBefore) {
        return YesOrNo.YES.equals(caseDataBefore.getIntervenerOneWrapper().getIntervener1Represented())
            && (caseData.getIntervenerOneWrapper().getIntervener1Represented() == null
            || YesOrNo.NO.equals(caseData.getIntervenerOneWrapper().getIntervener1Represented()));
    }

    private boolean checkIfIntervenerTwoSolicitorRemoved(FinremCaseData caseData, FinremCaseData caseDataBefore) {
        return YesOrNo.YES.equals(caseDataBefore.getIntervenerTwoWrapper().getIntervener2Represented())
            && (caseData.getIntervenerTwoWrapper().getIntervener2Represented() == null
            || YesOrNo.NO.equals(caseData.getIntervenerTwoWrapper().getIntervener2Represented()));
    }

    private boolean checkIfIntervenerThreeSolicitorRemoved(FinremCaseData caseData, FinremCaseData caseDataBefore) {
        return YesOrNo.YES.equals(caseDataBefore.getIntervenerThreeWrapper().getIntervener3Represented())
            && (caseData.getIntervenerThreeWrapper().getIntervener3Represented() == null
            || YesOrNo.NO.equals(caseData.getIntervenerThreeWrapper().getIntervener3Represented()));
    }

    private boolean checkIfIntervenerFourSolicitorRemoved(FinremCaseData caseData, FinremCaseData caseDataBefore) {
        return YesOrNo.YES.equals(caseDataBefore.getIntervenerFourWrapper().getIntervener4Represented())
            && (caseData.getIntervenerFourWrapper().getIntervener4Represented() == null
            || YesOrNo.NO.equals(caseData.getIntervenerFourWrapper().getIntervener4Represented()));
    }

    public boolean checkIfAnyIntervenerSolicitorRemoved(FinremCaseData caseData, FinremCaseData caseDataBefore) {
        return checkIfIntervenerOneSolicitorRemoved(caseData, caseDataBefore) || checkIfIntervenerTwoSolicitorRemoved(caseData, caseDataBefore)
            || checkIfIntervenerThreeSolicitorRemoved(caseData, caseDataBefore) || checkIfIntervenerFourSolicitorRemoved(caseData, caseDataBefore);
    }

    private void setDefaultOrgForintervenerOne(IntervenerOneWrapper intervenerOneWrapper) {
        Organisation organisation = Organisation.builder().organisationID(null).organisationName(null).build();
        intervenerOneWrapper.getIntervener1Organisation().setOrganisation(organisation);
        intervenerOneWrapper.getIntervener1Organisation().setOrgPolicyCaseAssignedRole(CaseRole.INTVR_SOLICITOR_1.getValue());
        intervenerOneWrapper.getIntervener1Organisation().setOrgPolicyReference(null);
    }

    private void setDefaultOrgForintervenerTwo(IntervenerTwoWrapper intervenerTwoWrapper) {
        Organisation organisation = Organisation.builder().organisationID(null).organisationName(null).build();
        intervenerTwoWrapper.getIntervener2Organisation().setOrganisation(organisation);
        intervenerTwoWrapper.getIntervener2Organisation().setOrgPolicyCaseAssignedRole(CaseRole.INTVR_SOLICITOR_2.getValue());
        intervenerTwoWrapper.getIntervener2Organisation().setOrgPolicyReference(null);
    }

    private void setDefaultOrgForintervenerThree(IntervenerThreeWrapper intervenerThreeWrapper) {
        Organisation organisation = Organisation.builder().organisationID(null).organisationName(null).build();
        intervenerThreeWrapper.getIntervener3Organisation().setOrganisation(organisation);
        intervenerThreeWrapper.getIntervener3Organisation().setOrgPolicyCaseAssignedRole(CaseRole.INTVR_SOLICITOR_3.getValue());
        intervenerThreeWrapper.getIntervener3Organisation().setOrgPolicyReference(null);
    }

    private void setDefaultOrgForintervenerFour(IntervenerFourWrapper intervenerFourWrapper) {
        Organisation organisation = Organisation.builder().organisationID(null).organisationName(null).build();
        intervenerFourWrapper.getIntervener4Organisation().setOrganisation(organisation);
        intervenerFourWrapper.getIntervener4Organisation().setOrgPolicyCaseAssignedRole(CaseRole.INTVR_SOLICITOR_4.getValue());
        intervenerFourWrapper.getIntervener4Organisation().setOrgPolicyReference(null);
    }

    public IntervenerChangeDetails setIntervenerOneAddedChangeDetails(FinremCaseData caseData) {
        IntervenerChangeDetails intervenerOneChangeDetails = new IntervenerChangeDetails();
        intervenerOneChangeDetails.setIntervenerAction(IntervenerAction.ADDED);
        intervenerOneChangeDetails.setIntervenerType(IntervenerType.INTERVENER_ONE);
        intervenerOneChangeDetails.setIntervenerDetails(
            intervenerOneDetailsMapper.mapToIntervenerDetails(caseData.getIntervenerOneWrapper()));

        return intervenerOneChangeDetails;
    }

    public IntervenerChangeDetails setIntervenerTwoAddedChangeDetails(FinremCaseData caseData) {
        IntervenerChangeDetails intervenerTwoChangeDetails = new IntervenerChangeDetails();
        intervenerTwoChangeDetails.setIntervenerAction(IntervenerAction.ADDED);
        intervenerTwoChangeDetails.setIntervenerType(IntervenerType.INTERVENER_TWO);
        intervenerTwoChangeDetails.setIntervenerDetails(
            intervenerTwoDetailsMapper.mapToIntervenerDetails(caseData.getIntervenerTwoWrapper()));

        return intervenerTwoChangeDetails;
    }

    public IntervenerChangeDetails setIntervenerThreeAddedChangeDetails(FinremCaseData caseData) {
        IntervenerChangeDetails intervenerThreeChangeDetails = new IntervenerChangeDetails();
        intervenerThreeChangeDetails.setIntervenerAction(IntervenerAction.ADDED);
        intervenerThreeChangeDetails.setIntervenerType(IntervenerType.INTERVENER_THREE);
        intervenerThreeChangeDetails.setIntervenerDetails(
            intervenerThreeDetailsMapper.mapToIntervenerDetails(caseData.getIntervenerThreeWrapper()));

        return intervenerThreeChangeDetails;
    }

    public IntervenerChangeDetails setIntervenerFourAddedChangeDetails(FinremCaseData caseData) {
        IntervenerChangeDetails intervenerFourChangeDetails = new IntervenerChangeDetails();
        intervenerFourChangeDetails.setIntervenerAction(IntervenerAction.ADDED);
        intervenerFourChangeDetails.setIntervenerType(IntervenerType.INTERVENER_FOUR);
        intervenerFourChangeDetails.setIntervenerDetails(
            intervenerFourDetailsMapper.mapToIntervenerDetails(caseData.getIntervenerFourWrapper()));

        return intervenerFourChangeDetails;
    }

    public IntervenerChangeDetails setIntervenerOneRemovedChangeDetails(FinremCaseData caseData) {
        IntervenerChangeDetails intervenerOneChangeDetails = new IntervenerChangeDetails();
        intervenerOneChangeDetails.setIntervenerAction(IntervenerAction.REMOVED);
        intervenerOneChangeDetails.setIntervenerType(IntervenerType.INTERVENER_ONE);
        intervenerOneChangeDetails.setIntervenerDetails(
            intervenerOneDetailsMapper.mapToIntervenerDetails(caseData.getIntervenerOneWrapper()));
        return intervenerOneChangeDetails;
    }

    public IntervenerChangeDetails setIntervenerTwoRemovedChangeDetails(FinremCaseData caseData) {
        IntervenerChangeDetails intervenerTwoChangeDetails = new IntervenerChangeDetails();
        intervenerTwoChangeDetails.setIntervenerAction(IntervenerAction.REMOVED);
        intervenerTwoChangeDetails.setIntervenerType(IntervenerType.INTERVENER_TWO);
        intervenerTwoChangeDetails.setIntervenerDetails(
            intervenerTwoDetailsMapper.mapToIntervenerDetails(caseData.getIntervenerTwoWrapper()));
        return intervenerTwoChangeDetails;
    }

    public IntervenerChangeDetails setIntervenerThreeRemovedChangeDetails(FinremCaseData caseData) {
        IntervenerChangeDetails intervenerThreeChangeDetails = new IntervenerChangeDetails();
        intervenerThreeChangeDetails.setIntervenerAction(IntervenerAction.REMOVED);
        intervenerThreeChangeDetails.setIntervenerType(IntervenerType.INTERVENER_THREE);
        intervenerThreeChangeDetails.setIntervenerDetails(
            intervenerThreeDetailsMapper.mapToIntervenerDetails(caseData.getIntervenerThreeWrapper()));
        return intervenerThreeChangeDetails;
    }

    public IntervenerChangeDetails setIntervenerFourRemovedChangeDetails(FinremCaseData caseData) {
        IntervenerChangeDetails intervenerFourChangeDetails = new IntervenerChangeDetails();
        intervenerFourChangeDetails.setIntervenerAction(IntervenerAction.REMOVED);
        intervenerFourChangeDetails.setIntervenerType(IntervenerType.INTERVENER_FOUR);
        intervenerFourChangeDetails.setIntervenerDetails(
            intervenerFourDetailsMapper.mapToIntervenerDetails(caseData.getIntervenerFourWrapper()));
        return intervenerFourChangeDetails;
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