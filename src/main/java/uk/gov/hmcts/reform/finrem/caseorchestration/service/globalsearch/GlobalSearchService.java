package uk.gov.hmcts.reform.finrem.caseorchestration.service.globalsearch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GlobalSearchService {

    private final FeatureToggleService featureToggleService;

    public void setGlobalSearchData(FinremCaseData caseData) {
        if (featureToggleService.isGlobalSearchEnabled()) {
            log.info("setGlobalSearchData::Received request to set global search fields for case with CCD ID: {}", caseData.getCcdCaseId());
            caseData.setCaseNameHmctsInternal(caseData.getFullApplicantName());
        }
    }

    public void setGlobalSearchDataByMap(Map<String, Object> caseData) {
        if (featureToggleService.isGlobalSearchEnabled()) {
            log.info("setGlobalSearchDataByMap::Received request to set global search fields for case with CCD ID: {}", caseData.get("ccdCaseId"));
            caseData.put("caseNameHmctsInternal", caseData.get("fullApplicantName"));
        }
    }

}
