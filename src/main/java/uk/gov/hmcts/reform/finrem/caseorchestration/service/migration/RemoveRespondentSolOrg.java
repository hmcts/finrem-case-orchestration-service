package uk.gov.hmcts.reform.finrem.caseorchestration.service.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;


import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORGANISATION_POLICY_RESPONDENT;

@Slf4j
@Service
public class RemoveRespondentSolOrg {

    public Map<String, Object> migrateCaseData(Map<String, Object> caseData) {
        if (caseData.containsKey(ORGANISATION_POLICY_RESPONDENT)) {
            log.info("Attempting to remove respondent organisation policy");
            caseData.remove(ORGANISATION_POLICY_RESPONDENT);
            log.info("Removed respondent organisation policy");
        }
        return caseData;
    }
}