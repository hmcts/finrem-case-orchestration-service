package uk.gov.hmcts.reform.finrem.caseorchestration.service.globalsearch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.caselocation.CaseManagementLocationService;

import java.util.Map;

/**
 * Service responsible for populating global search related fields on case data.
 * It checks the feature toggle before mutating the provided case data map.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GlobalSearchService {

    private static final String FINANCIAL_REMEDY = "Financial Remedy";
    private final FeatureToggleService featureToggleService;
    private final CaseManagementLocationService caseManagementLocationService;

    /**
     * Sets the fields required for global search on the provided case data map.
     * If the global search feature is disabled, this method does nothing.
     *
     * @param caseData the case data map to update
     */
    public void setGlobalSearchDataByMap(Map<String, Object> caseData) {
        if (featureToggleService.isGlobalSearchEnabled()) {
            log.info("setGlobalSearchDataByMap::Received request to set global search fields for case with CCD ID: {}",
                caseData.get("ccdCaseId"));
            DynamicListElement element = DynamicListElement.builder().code("Financial Remedy").label("Financial Remedy").build();
            caseData.put("caseManagementCategory", DynamicList.builder().value(element).build());
            caseData.put("caseNameHmctsInternal", caseData.get("fullApplicantName"));
            caseData.put("caseManagementLocation", caseManagementLocationService.getCaseLocation(caseData));
        }
    }

}
