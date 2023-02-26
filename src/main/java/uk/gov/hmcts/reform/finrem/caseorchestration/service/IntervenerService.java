package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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

    public void removeIntervenerOneDetails(FinremCaseData caseData) {
        caseData.setIntervenerOneWrapper(null);
    }

    public void removeIntervenerTwoDetails(FinremCaseData caseData) {
        caseData.setIntervenerTwoWrapper(null);
    }

    public void removeIntervenerThreeDetails(FinremCaseData caseData) {
        caseData.setIntervenerThreeWrapper(null);
    }

    public void removeIntervenerFourDetails(FinremCaseData caseData) {
        caseData.setIntervenerFourWrapper(null);
    }

    private static void updateIntervenerFourDetails(FinremCaseData caseData, Long caseId) {
        IntervenerFourWrapper intervenerFourWrapper = caseData.getIntervenerFourWrapper();
        if (intervenerFourWrapper != null
            && intervenerFourWrapper.getIntervener4Name() != null
            &&  intervenerFourWrapper.getIntervener4DateAdded() == null) {
            log.info("Intervener4 date intervener added to case {}", caseId);
            intervenerFourWrapper.setIntervener4DateAdded(LocalDate.now());

            if (intervenerFourWrapper.getIntervener4Represented().equals(YesOrNo.NO)) {
                log.info("Intervener4 add default case role and organisation for case {}", caseId);
                Organisation organisation = Organisation.builder().organisationID(null).organisationName(null).build();
                intervenerFourWrapper.getIntervener4Organisation().setOrganisation(organisation);
                intervenerFourWrapper.getIntervener4Organisation().setOrgPolicyCaseAssignedRole(CaseRole.APP_SOLICITOR.getValue());
                intervenerFourWrapper.getIntervener4Organisation().setOrgPolicyReference(null);
            }
        }
    }

    private static void updateIntervenerThreeDetails(FinremCaseData caseData, Long caseId) {
        IntervenerThreeWrapper intervenerThreeWrapper = caseData.getIntervenerThreeWrapper();
        if (intervenerThreeWrapper != null
            &&  intervenerThreeWrapper.getIntervener3Name() != null
            &&  intervenerThreeWrapper.getIntervener3DateAdded() == null) {
            log.info("Intervener3 date intervener added to case {}", caseId);
            intervenerThreeWrapper.setIntervener3DateAdded(LocalDate.now());

            if (intervenerThreeWrapper.getIntervener3Represented().equals(YesOrNo.NO)) {
                log.info("Intervener3 add default case role and organisation for case {}", caseId);
                Organisation organisation = Organisation.builder().organisationID(null).organisationName(null).build();
                intervenerThreeWrapper.getIntervener3Organisation().setOrganisation(organisation);
                intervenerThreeWrapper.getIntervener3Organisation().setOrgPolicyCaseAssignedRole(CaseRole.APP_SOLICITOR.getValue());
                intervenerThreeWrapper.getIntervener3Organisation().setOrgPolicyReference(null);
            }
        }
    }

    private static void updateIntervenerTwoDetails(FinremCaseData caseData, Long caseId) {
        IntervenerTwoWrapper intervenerTwoWrapper = caseData.getIntervenerTwoWrapper();
        if (intervenerTwoWrapper != null
            &&  intervenerTwoWrapper.getIntervener2Name() != null
            &&  intervenerTwoWrapper.getIntervener2DateAdded() == null) {
            log.info("Intervener2 date intervener added to case {}", caseId);
            intervenerTwoWrapper.setIntervener2DateAdded(LocalDate.now());

            if (intervenerTwoWrapper.getIntervener2Represented().equals(YesOrNo.NO)) {
                log.info("Intervener2 add default case role and organisation for case {}", caseId);
                Organisation organisation = Organisation.builder().organisationID(null).organisationName(null).build();
                intervenerTwoWrapper.getIntervener2Organisation().setOrganisation(organisation);
                intervenerTwoWrapper.getIntervener2Organisation().setOrgPolicyCaseAssignedRole(CaseRole.APP_SOLICITOR.getValue());
                intervenerTwoWrapper.getIntervener2Organisation().setOrgPolicyReference(null);
            }
        }
    }

    private static void updateIntervenerOneDetails(FinremCaseData caseData, Long caseId) {
        IntervenerOneWrapper intervenerOneWrapper = caseData.getIntervenerOneWrapper();
        if (intervenerOneWrapper.getIntervener1DateAdded() == null) {
            log.info("Intervener1 date intervener added to case {}", caseId);
            intervenerOneWrapper.setIntervener1DateAdded(LocalDate.now());
        }

        if (intervenerOneWrapper.getIntervener1Represented().equals(YesOrNo.NO)) {
            log.info("Intervener1 add default case role and organisation for case {}", caseId);
            Organisation organisation = Organisation.builder().organisationID(null).organisationName(null).build();
            intervenerOneWrapper.getIntervener1Organisation().setOrganisation(organisation);
            intervenerOneWrapper.getIntervener1Organisation().setOrgPolicyCaseAssignedRole(CaseRole.APP_SOLICITOR.getValue());
            intervenerOneWrapper.getIntervener1Organisation().setOrgPolicyReference(null);
        }
    }


}
