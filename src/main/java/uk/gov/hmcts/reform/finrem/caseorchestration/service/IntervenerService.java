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

    public void setIntvenerDateAddedAndDefaultOrgIfNotRepresented(FinremCaseData caseData, String auth, Long caseId) {
        String valueCode = caseData.getIntervenersList().getValueCode();
        log.info("selected intervener {} for caseId {}", valueCode, caseId);
        switch (valueCode) {
            case INTERVENER_ONE -> updateIntervenerOneDetails(caseData, auth, caseId);
            case INTERVENER_TWO -> updateIntervenerTwoDetails(caseData, auth, caseId);
            case INTERVENER_THREE -> updateIntervenerThreeDetails(caseData, auth, caseId);
            case INTERVENER_FOUR -> updateIntervenerFourDetails(caseData, auth, caseId);
            default -> throw new IllegalArgumentException("Invalid intervener selected");
        }
    }

    public void removeIntervenerOneDetails(FinremCaseData caseData, String auth, Long caseId) {
        IntervenerOneWrapper intervenerOneWrapper = caseData.getIntervenerOneWrapper();
        if (intervenerOneWrapper.getIntervener1Represented().equals(YesOrNo.YES)) {
            log.info("revoke case role for intervener1 for case {}", caseId);
            String orgId = intervenerOneWrapper.getIntervener1Organisation().getOrganisation().getOrganisationID();
            String email = intervenerOneWrapper.getIntervener1SolEmail();
            remokeIntervenerRole(caseId, email, orgId, CaseRole.INTVR_SOLICITOR_1.getValue(), auth);
        }
        caseData.setIntervenerOneWrapper(null);
    }

    public void removeIntervenerTwoDetails(FinremCaseData caseData, String auth, Long caseId) {
        IntervenerTwoWrapper wrapper = caseData.getIntervenerTwoWrapper();
        if (wrapper.getIntervener2Represented().equals(YesOrNo.YES)) {
            log.info("revoke case role for intervener2 for case {}", caseId);
            String orgId = wrapper.getIntervener2Organisation().getOrganisation().getOrganisationID();
            String email = wrapper.getIntervener2SolEmail();
            remokeIntervenerRole(caseId, email, orgId, CaseRole.INTVR_SOLICITOR_2.getValue(), auth);
        }
        caseData.setIntervenerTwoWrapper(null);
    }

    public void removeIntervenerThreeDetails(FinremCaseData caseData, String auth, Long caseId) {
        IntervenerThreeWrapper wrapper = caseData.getIntervenerThreeWrapper();
        if (wrapper.getIntervener3Represented().equals(YesOrNo.YES)) {
            log.info("revoke case role for intervener3 for case {}", caseId);
            String orgId = wrapper.getIntervener3Organisation().getOrganisation().getOrganisationID();
            String email = wrapper.getIntervener3SolEmail();
            remokeIntervenerRole(caseId, email, orgId, CaseRole.INTVR_SOLICITOR_3.getValue(), auth);
        }
        caseData.setIntervenerThreeWrapper(null);
    }

    public void removeIntervenerFourDetails(FinremCaseData caseData, String auth, Long caseId) {
        IntervenerFourWrapper wrapper = caseData.getIntervenerFourWrapper();
        if (wrapper.getIntervener4Represented().equals(YesOrNo.YES)) {
            log.info("revoke case role for intervener4 for case {}", caseId);
            String orgId = wrapper.getIntervener4Organisation().getOrganisation().getOrganisationID();
            String email = wrapper.getIntervener4SolEmail();
            remokeIntervenerRole(caseId, email, orgId, CaseRole.INTVR_SOLICITOR_4.getValue(), auth);
        }
        caseData.setIntervenerFourWrapper(null);
    }

    private void updateIntervenerFourDetails(FinremCaseData caseData, String auth, Long caseId) {
        IntervenerFourWrapper intervenerFourWrapper = caseData.getIntervenerFourWrapper();
        if (intervenerFourWrapper != null &&  intervenerFourWrapper.getIntervener4DateAdded() == null) {
            log.info("Intervener4 date intervener added to case {}", caseId);
            intervenerFourWrapper.setIntervener4DateAdded(LocalDate.now());

            final String caseRole = CaseRole.INTVR_SOLICITOR_4.getValue();
            if (intervenerFourWrapper.getIntervener4Represented().equals(YesOrNo.YES)) {
                log.info("Add Intervener3 case role for case {}", caseId);
                String orgId = intervenerFourWrapper.getIntervener4Organisation().getOrganisation().getOrganisationID();
                String email = intervenerFourWrapper.getIntervener4SolEmail();
                addIntervenerRole(caseId, email, orgId, caseRole, auth);
            } else {
                log.info("Intervener4 add default case role and organisation for case {}", caseId);
                Organisation organisation = Organisation.builder().organisationID(null).organisationName(null).build();
                intervenerFourWrapper.getIntervener4Organisation().setOrganisation(organisation);
                intervenerFourWrapper.getIntervener4Organisation().setOrgPolicyCaseAssignedRole(CaseRole.INTVR_SOLICITOR_4.getValue());
                intervenerFourWrapper.getIntervener4Organisation().setOrgPolicyReference(null);
            }
        }
    }

    private void updateIntervenerThreeDetails(FinremCaseData caseData, String auth, Long caseId) {
        IntervenerThreeWrapper intervenerThreeWrapper = caseData.getIntervenerThreeWrapper();
        if (intervenerThreeWrapper != null &&  intervenerThreeWrapper.getIntervener3DateAdded() == null) {
            log.info("Intervener3 date intervener added to case {}", caseId);
            intervenerThreeWrapper.setIntervener3DateAdded(LocalDate.now());

            final String caseRole = CaseRole.INTVR_SOLICITOR_3.getValue();
            if (intervenerThreeWrapper.getIntervener3Represented().equals(YesOrNo.YES)) {
                log.info("Add Intervener3 case role for case {}", caseId);
                String orgId = intervenerThreeWrapper.getIntervener3Organisation().getOrganisation().getOrganisationID();
                String email = intervenerThreeWrapper.getIntervener3SolEmail();
                addIntervenerRole(caseId, email, orgId, caseRole, auth);
            } else {
                log.info("Intervener3 add default case role and organisation for case {}", caseId);
                Organisation organisation = Organisation.builder().organisationID(null).organisationName(null).build();
                intervenerThreeWrapper.getIntervener3Organisation().setOrganisation(organisation);
                intervenerThreeWrapper.getIntervener3Organisation().setOrgPolicyCaseAssignedRole(CaseRole.INTVR_SOLICITOR_3.getValue());
                intervenerThreeWrapper.getIntervener3Organisation().setOrgPolicyReference(null);
            }
        }
    }

    private void updateIntervenerTwoDetails(FinremCaseData caseData, String auth, Long caseId) {
        IntervenerTwoWrapper intervenerTwoWrapper = caseData.getIntervenerTwoWrapper();
        if (intervenerTwoWrapper != null &&  intervenerTwoWrapper.getIntervener2DateAdded() == null) {
            log.info("Intervener2 date intervener added to case {}", caseId);
            intervenerTwoWrapper.setIntervener2DateAdded(LocalDate.now());

            final String caseRole = CaseRole.INTVR_SOLICITOR_2.getValue();
            if (intervenerTwoWrapper.getIntervener2Represented().equals(YesOrNo.YES)) {
                log.info("Add Intervener2 case role for case {}", caseId);
                String orgId = intervenerTwoWrapper.getIntervener2Organisation().getOrganisation().getOrganisationID();
                String email = intervenerTwoWrapper.getIntervener2SolEmail();
                addIntervenerRole(caseId, email, orgId, caseRole, auth);
            } else {
                log.info("Intervener2 add default case role and organisation for case {}", caseId);
                Organisation organisation = Organisation.builder().organisationID(null).organisationName(null).build();
                intervenerTwoWrapper.getIntervener2Organisation().setOrganisation(organisation);
                intervenerTwoWrapper.getIntervener2Organisation().setOrgPolicyCaseAssignedRole(CaseRole.INTVR_SOLICITOR_2.getValue());
                intervenerTwoWrapper.getIntervener2Organisation().setOrgPolicyReference(null);
            }
        }
    }

    private void updateIntervenerOneDetails(FinremCaseData caseData, String auth,  Long caseId) {
        IntervenerOneWrapper intervenerOneWrapper = caseData.getIntervenerOneWrapper();
        if (intervenerOneWrapper != null && intervenerOneWrapper.getIntervener1DateAdded() == null) {
            log.info("Intervener1 date intervener added to case {}", caseId);
            intervenerOneWrapper.setIntervener1DateAdded(LocalDate.now());

            final String caseRole = CaseRole.INTVR_SOLICITOR_1.getValue();
            if (intervenerOneWrapper.getIntervener1Represented().equals(YesOrNo.YES)) {
                log.info("Add Intervener1 case role for case {}", caseId);
                String orgId = intervenerOneWrapper.getIntervener1Organisation().getOrganisation().getOrganisationID();
                String email = intervenerOneWrapper.getIntervener1SolEmail();
                addIntervenerRole(caseId, email, orgId, caseRole, auth);
            } else {
                log.info("Intervener1 add default case role and organisation for case {}", caseId);
                Organisation organisation = Organisation.builder().organisationID(null).organisationName(null).build();
                intervenerOneWrapper.getIntervener1Organisation().setOrganisation(organisation);
                intervenerOneWrapper.getIntervener1Organisation().setOrgPolicyCaseAssignedRole(CaseRole.INTVR_SOLICITOR_1.getValue());
                intervenerOneWrapper.getIntervener1Organisation().setOrgPolicyReference(null);
            }
        }
    }

    private void addIntervenerRole(Long caseId, String email, String orgId, String caseRole, String auth) {
        organisationService.findUserByEmail(email, systemUserService.getSysUserToken())
            .ifPresentOrElse(
                userId -> assignCaseAccessService.grantCaseRoleToUser(caseId, userId, caseRole, orgId),
                throwNoSuchUserException(email)
            );
    }

    private void remokeIntervenerRole(Long caseId, String email, String orgId, String caseRole, String auth) {
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
