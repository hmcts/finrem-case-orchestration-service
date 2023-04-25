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
        updateApplicantFlags(caseData);
        updateRespondentFlags(caseData);
        updateCaseFlagsAtCaseLevel(caseData);
    }

    private void updateCaseFlagsAtCaseLevel(FinremCaseData caseData) {
        CaseFlag caseFlag = Optional.ofNullable(caseData.getCaseFlagsWrapper().getCaseFlags())
            .orElse(new CaseFlag());
        caseFlag.setPartyName(CASE_LEVEL_ROLE);
        caseFlag.setRoleOnCase(CASE_LEVEL_ROLE);
        caseData.getCaseFlagsWrapper().setCaseFlags(caseFlag);
    }

    private void updateApplicantFlags(FinremCaseData caseData) {

        CaseFlag caseFlag = Optional.ofNullable(caseData.getCaseFlagsWrapper().getApplicantFlags())
            .orElse(new CaseFlag());
        caseFlag.setPartyName(caseData.getFullApplicantName());
        caseFlag.setRoleOnCase(APPLICANT);
        caseData.getCaseFlagsWrapper().setApplicantFlags(caseFlag);
    }

    private void updateRespondentFlags(FinremCaseData caseData) {

        CaseFlag caseFlag = Optional.ofNullable(caseData.getCaseFlagsWrapper().getRespondentFlags())
            .orElse(new CaseFlag());
        caseFlag.setPartyName(caseData.getRespondentFullName());
        caseFlag.setRoleOnCase(RESPONDENT);
        caseData.getCaseFlagsWrapper().setRespondentFlags(caseFlag);
    }
}
