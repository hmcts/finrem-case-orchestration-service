package uk.gov.hmcts.reform.finrem.caseorchestration.service.globalsearch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

import java.util.Map;

/**
 * Service responsible for populating global search related fields on case data.
 * It checks the feature toggle before mutating the provided case data map.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GlobalSearchService {

    private final FeatureToggleService featureToggleService;

    /**
     * Sets fields required for global search on the provided case data map.
     * No changes are made if the global search feature is disabled.
     *
     * @param caseData
     *
     */
    public void setGlobalSearchDataByMap(Map<String, Object> caseData) {
        if (featureToggleService.isGlobalSearchEnabled()) {
            log.info("setGlobalSearchDataByMap::Received request to set global search fields for case with CCD ID: {}", caseData.get("ccdCaseId"));
            caseData.put("caseNameHmctsInternal", caseData.get("fullApplicantName"));
        }
    }

}
