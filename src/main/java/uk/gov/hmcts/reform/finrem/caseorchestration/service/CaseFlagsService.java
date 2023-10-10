package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.caseflag.caseflag.CaseFlag;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_APPLICANT_FLAGS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_LEVEL_FLAGS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_LEVEL_ROLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_RESPONDENT_FLAGS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaseFlagsService {

    private final ObjectMapper objectMapper;
    private final CaseDataService caseDataService;

    public void setCaseFlagInformation(FinremCaseDetails caseDetails) {
        log.info("Received request to update case flags with Case ID: {}", caseDetails.getId());

        FinremCaseData caseData = caseDetails.getData();

        updateCaseFlagsForParty(caseData, CASE_APPLICANT_FLAGS, caseData.getFullApplicantName(), APPLICANT);
        updateCaseFlagsForParty(caseData, CASE_RESPONDENT_FLAGS, caseData.getRespondentFullName(), RESPONDENT);
        updateCaseFlagsAtCaseLevel(caseData);
    }

    public void setCaseFlagInformation(CaseDetails caseDetails) {
        log.info("Received request to update case flags with Case ID: {}", caseDetails.getId());

        Map<String, Object> caseData = caseDetails.getData();

        updateCaseFlagsForParty(caseData, CASE_APPLICANT_FLAGS, caseDataService.buildFullApplicantName(caseDetails), APPLICANT);
        updateCaseFlagsForParty(caseData, CASE_RESPONDENT_FLAGS, caseDataService.buildFullRespondentName(caseDetails), RESPONDENT);
        updateCaseFlagsAtCaseLevel(caseData);
    }

    private void updateCaseFlagsAtCaseLevel(FinremCaseData caseData) {
        CaseFlag caseFlagDetailsData = Optional.ofNullable(caseData.getCaseFlagsWrapper().getCaseFlags())
            .orElse(new CaseFlag());
        caseFlagDetailsData.setPartyName(CASE_LEVEL_ROLE);
        caseFlagDetailsData.setRoleOnCase(CASE_LEVEL_ROLE);
        caseData.getCaseFlagsWrapper().setCaseFlags(caseFlagDetailsData);
    }

    private void updateCaseFlagsAtCaseLevel(Map<String, Object> caseData) {
        CaseFlag caseFlagDetailsData = Optional.ofNullable(objectMapper.convertValue(caseData.get(CASE_LEVEL_FLAGS), CaseFlag.class))
            .orElse(new CaseFlag());
        caseFlagDetailsData.setPartyName(CASE_LEVEL_ROLE);
        caseFlagDetailsData.setRoleOnCase(CASE_LEVEL_ROLE);
        caseData.put(CASE_LEVEL_FLAGS, caseFlagDetailsData);
    }

    private void updateCaseFlagsForParty(FinremCaseData caseData,
                                         String flagLevel,
                                         String party,
                                         String roleOnCase) {
        if (flagLevel.equalsIgnoreCase(CASE_APPLICANT_FLAGS)) {
            CaseFlag caseFlag = Optional.ofNullable(caseData.getCaseFlagsWrapper().getApplicantFlags())
                .orElse(new CaseFlag());
            caseFlag.setPartyName(party);
            caseFlag.setRoleOnCase(roleOnCase);
            caseData.getCaseFlagsWrapper().setApplicantFlags(caseFlag);
        } else if (flagLevel.equalsIgnoreCase(CASE_RESPONDENT_FLAGS)) {
            CaseFlag caseFlag = Optional.ofNullable(caseData.getCaseFlagsWrapper().getRespondentFlags())
                .orElse(new CaseFlag());
            caseFlag.setPartyName(party);
            caseFlag.setRoleOnCase(roleOnCase);
            caseData.getCaseFlagsWrapper().setRespondentFlags(caseFlag);
        }
    }

    private void updateCaseFlagsForParty(Map<String, Object> caseData,
                                         String flagLevel,
                                         String party,
                                         String roleOnCase) {
        CaseFlag caseFlag = Optional.ofNullable(objectMapper.convertValue(caseData.get(flagLevel), CaseFlag.class))
            .orElse(new CaseFlag());
        caseFlag.setPartyName(party);
        caseFlag.setRoleOnCase(roleOnCase);
        caseData.put(flagLevel, caseFlag);
    }
}
