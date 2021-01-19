package uk.gov.hmcts.reform.finrem.caseorchestration.service.migration;

import org.springframework.stereotype.Service;

import java.util.Map;


import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORGANISATION_POLICY_RESPONDENT;

@Service
public class RemoveRespondentSolOrg {

    public Map<String, Object> migrateCaseData(Map<String, Object> caseData) {
        if (caseData.containsKey(ORGANISATION_POLICY_RESPONDENT)) {
            caseData.remove(ORGANISATION_POLICY_RESPONDENT);
        }
        return caseData;
    }
}
