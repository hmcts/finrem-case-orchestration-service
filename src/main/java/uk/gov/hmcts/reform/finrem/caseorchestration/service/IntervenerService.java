package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.NoSuchUserException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFourWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOneWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThreeWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwoWrapper;

import java.time.LocalDate;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_FOUR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_ONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_THREE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_TWO;

@Service
@RequiredArgsConstructor
@Slf4j
public class IntervenerService {

    private final AssignCaseAccessService assignCaseAccessService;
    private final PrdOrganisationService organisationService;
    private final SystemUserService systemUserService;

    public void setIntvenerDateAddedAndDefaultOrgIfNotRepresented(FinremCaseData caseData, Long caseId) {
        String valueCode = caseData.getIntervenersList().getValueCode();
        log.info("selected intervener {} for caseId {}", valueCode, caseId);
        switch (valueCode) {
            case INTERVENER_ONE -> updateIntervenerOneDetails(caseData, caseId);
            case INTERVENER_TWO -> updateIntervenerTwoDetails(caseData, caseId);
            case INTERVENER_THREE -> updateIntervenerThreeDetails(caseData, caseId);
            case INTERVENER_FOUR -> updateIntervenerFourDetails(caseData, caseId);
            default -> throw new IllegalArgumentException("Invalid intervener selected");
        }
    }

    public void removeIntervenerOneDetails(FinremCaseData caseData, Long caseId) {
        IntervenerOneWrapper intervenerOneWrapper = caseData.getIntervenerOneWrapper();
        if (intervenerOneWrapper.getIntervener1Represented().equals(YesOrNo.YES)) {
            log.info("revoke case role for intervener1 for case {}", caseId);
            String orgId = intervenerOneWrapper.getIntervener1Organisation().getOrganisation().getOrganisationID();
            String email = intervenerOneWrapper.getIntervener1SolEmail();
            remokeIntervenerRole(caseId, email, orgId, CaseRole.INTVR_SOLICITOR_1.getValue());
        }
        caseData.setIntervenerOneWrapper(null);
    }

    public void removeIntervenerTwoDetails(FinremCaseData caseData, Long caseId) {
        IntervenerTwoWrapper wrapper = caseData.getIntervenerTwoWrapper();
        if (wrapper.getIntervener2Represented().equals(YesOrNo.YES)) {
            log.info("revoke case role for intervener2 for case {}", caseId);
            String orgId = wrapper.getIntervener2Organisation().getOrganisation().getOrganisationID();
            String email = wrapper.getIntervener2SolEmail();
            remokeIntervenerRole(caseId, email, orgId, CaseRole.INTVR_SOLICITOR_2.getValue());
        }
        caseData.setIntervenerTwoWrapper(null);
    }

    public void removeIntervenerThreeDetails(FinremCaseData caseData, Long caseId) {
        IntervenerThreeWrapper wrapper = caseData.getIntervenerThreeWrapper();
        if (wrapper.getIntervener3Represented().equals(YesOrNo.YES)) {
            log.info("revoke case role for intervener3 for case {}", caseId);
            String orgId = wrapper.getIntervener3Organisation().getOrganisation().getOrganisationID();
            String email = wrapper.getIntervener3SolEmail();
            remokeIntervenerRole(caseId, email, orgId, CaseRole.INTVR_SOLICITOR_3.getValue());
        }
        caseData.setIntervenerThreeWrapper(null);
    }

    public void removeIntervenerFourDetails(FinremCaseData caseData, Long caseId) {
        IntervenerFourWrapper wrapper = caseData.getIntervenerFourWrapper();
        if (wrapper.getIntervener4Represented().equals(YesOrNo.YES)) {
            log.info("revoke case role for intervener4 for case {}", caseId);
            String orgId = wrapper.getIntervener4Organisation().getOrganisation().getOrganisationID();
            String email = wrapper.getIntervener4SolEmail();
            remokeIntervenerRole(caseId, email, orgId, CaseRole.INTVR_SOLICITOR_4.getValue());
        }
        caseData.setIntervenerFourWrapper(null);
    }

    private void updateIntervenerFourDetails(FinremCaseData caseData, Long caseId) {
        IntervenerFourWrapper intervenerFourWrapper = caseData.getIntervenerFourWrapper();
        if (intervenerFourWrapper != null &&  intervenerFourWrapper.getIntervener4DateAdded() == null) {
            log.info("Intervener4 date intervener added to case {}", caseId);
            intervenerFourWrapper.setIntervener4DateAdded(LocalDate.now());

            final String caseRole = CaseRole.INTVR_SOLICITOR_4.getValue();
            if (intervenerFourWrapper.getIntervener4Represented().equals(YesOrNo.YES)) {
                log.info("Add Intervener3 case role for case {}", caseId);
                String orgId = intervenerFourWrapper.getIntervener4Organisation().getOrganisation().getOrganisationID();
                String email = intervenerFourWrapper.getIntervener4SolEmail();
                addIntervenerRole(caseId, email, orgId, caseRole);
            } else {
                log.info("Intervener4 add default case role and organisation for case {}", caseId);
                Organisation organisation = Organisation.builder().organisationID(null).organisationName(null).build();
                intervenerFourWrapper.getIntervener4Organisation().setOrganisation(organisation);
                intervenerFourWrapper.getIntervener4Organisation().setOrgPolicyCaseAssignedRole(CaseRole.INTVR_SOLICITOR_4.getValue());
                intervenerFourWrapper.getIntervener4Organisation().setOrgPolicyReference(null);
            }
        }
    }

    private void updateIntervenerThreeDetails(FinremCaseData caseData, Long caseId) {
        IntervenerThreeWrapper intervenerThreeWrapper = caseData.getIntervenerThreeWrapper();
        if (intervenerThreeWrapper != null &&  intervenerThreeWrapper.getIntervener3DateAdded() == null) {
            log.info("Intervener3 date intervener added to case {}", caseId);
            intervenerThreeWrapper.setIntervener3DateAdded(LocalDate.now());

            final String caseRole = CaseRole.INTVR_SOLICITOR_3.getValue();
            if (intervenerThreeWrapper.getIntervener3Represented().equals(YesOrNo.YES)) {
                log.info("Add Intervener3 case role for case {}", caseId);
                String orgId = intervenerThreeWrapper.getIntervener3Organisation().getOrganisation().getOrganisationID();
                String email = intervenerThreeWrapper.getIntervener3SolEmail();
                addIntervenerRole(caseId, email, orgId, caseRole);
            } else {
                log.info("Intervener3 add default case role and organisation for case {}", caseId);
                Organisation organisation = Organisation.builder().organisationID(null).organisationName(null).build();
                intervenerThreeWrapper.getIntervener3Organisation().setOrganisation(organisation);
                intervenerThreeWrapper.getIntervener3Organisation().setOrgPolicyCaseAssignedRole(CaseRole.INTVR_SOLICITOR_3.getValue());
                intervenerThreeWrapper.getIntervener3Organisation().setOrgPolicyReference(null);
            }
        }
    }

    private void updateIntervenerTwoDetails(FinremCaseData caseData, Long caseId) {
        IntervenerTwoWrapper intervenerTwoWrapper = caseData.getIntervenerTwoWrapper();
        if (intervenerTwoWrapper != null &&  intervenerTwoWrapper.getIntervener2DateAdded() == null) {
            log.info("Intervener2 date intervener added to case {}", caseId);
            intervenerTwoWrapper.setIntervener2DateAdded(LocalDate.now());

            final String caseRole = CaseRole.INTVR_SOLICITOR_2.getValue();
            if (intervenerTwoWrapper.getIntervener2Represented().equals(YesOrNo.YES)) {
                log.info("Add Intervener2 case role for case {}", caseId);
                String orgId = intervenerTwoWrapper.getIntervener2Organisation().getOrganisation().getOrganisationID();
                String email = intervenerTwoWrapper.getIntervener2SolEmail();
                String orgPolicyCaseAssignedRole = intervenerTwoWrapper.getIntervener2Organisation().getOrgPolicyCaseAssignedRole();
                log.info("Intervener2 before current case role {} for case {}", caseId, orgPolicyCaseAssignedRole);
                addIntervenerRole(caseId, email, orgId, caseRole);
                String orgPolicyCaseAssignedRole2 = intervenerTwoWrapper.getIntervener2Organisation().getOrgPolicyCaseAssignedRole();
                log.info("Intervener2 before after case role {} for case {}", caseId, orgPolicyCaseAssignedRole2);
            } else {
                log.info("Intervener2 add default case role and organisation for case {}", caseId);
                Organisation organisation = Organisation.builder().organisationID(null).organisationName(null).build();
                intervenerTwoWrapper.getIntervener2Organisation().setOrganisation(organisation);
                intervenerTwoWrapper.getIntervener2Organisation().setOrgPolicyCaseAssignedRole(CaseRole.INTVR_SOLICITOR_2.getValue());
                intervenerTwoWrapper.getIntervener2Organisation().setOrgPolicyReference(null);
            }
        }
    }

    private void updateIntervenerOneDetails(FinremCaseData caseData, Long caseId) {
        IntervenerOneWrapper intervenerOneWrapper = caseData.getIntervenerOneWrapper();
        if (intervenerOneWrapper != null && intervenerOneWrapper.getIntervener1DateAdded() == null) {
            log.info("Intervener1 date intervener added to case {}", caseId);
            intervenerOneWrapper.setIntervener1DateAdded(LocalDate.now());

            final String caseRole = CaseRole.INTVR_SOLICITOR_1.getValue();
            if (intervenerOneWrapper.getIntervener1Represented().equals(YesOrNo.YES)) {
                log.info("Add Intervener1 case role for case {}", caseId);
                String orgId = intervenerOneWrapper.getIntervener1Organisation().getOrganisation().getOrganisationID();
                String email = intervenerOneWrapper.getIntervener1SolEmail();
                String orgPolicyCaseAssignedRole = intervenerOneWrapper.getIntervener1Organisation().getOrgPolicyCaseAssignedRole();
                log.info("Intervener1 current case role {} for case {}", caseId, orgPolicyCaseAssignedRole);
                addIntervenerRole(caseId, email, orgId, caseRole);
                String orgPolicyCaseAssignedRole2 = intervenerOneWrapper.getIntervener1Organisation().getOrgPolicyCaseAssignedRole();
                log.info("Intervener1 after case role {} for case {}", caseId, orgPolicyCaseAssignedRole2);
            } else {
                log.info("Intervener1 add default case role and organisation for case {}", caseId);
                Organisation organisation = Organisation.builder().organisationID(null).organisationName(null).build();
                intervenerOneWrapper.getIntervener1Organisation().setOrganisation(organisation);
                intervenerOneWrapper.getIntervener1Organisation().setOrgPolicyCaseAssignedRole(CaseRole.INTVR_SOLICITOR_1.getValue());
                intervenerOneWrapper.getIntervener1Organisation().setOrgPolicyReference(null);
            }
        }
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
