package uk.gov.hmcts.reform.finrem.caseorchestration.service.globalsearch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
     * @param caseDataMap the case data map to update
     */
    public void setGlobalSearchDataByMap(Map<String, Object> caseDataMap) {

        if (featureToggleService.isGlobalSearchEnabled()) {
            log.info("setGlobalSearchDataByMap::Received request to set global search fields for case with CCD ID: {}",
                caseDataMap.get("ccdCaseId"));
            DynamicListElement element = DynamicListElement.builder().code(FINANCIAL_REMEDY).build();
            caseDataMap.put("caseManagementCategory", DynamicList.builder().value(element).build());
            caseDataMap.put("caseNameHmctsInternal", getCaseNameHmctsInternal(caseDataMap));
            caseDataMap.put("caseManagementLocation", caseManagementLocationService.getCaseLocation(caseDataMap));
            log.info("setGlobalSearchDataByMap::global search fields are set for case with CCD ID: {}",
                caseDataMap.get("ccdCaseId"));
        }
    }

    private String getCaseNameHmctsInternal(Map<String, Object> caseDataMap) {
        if (StringUtils.isNotBlank((String) caseDataMap.get("applicantLName"))
            && StringUtils.isNotBlank((String) caseDataMap.get("appRespondentLName"))) {
            return String.format("%s vs %s",
                 caseDataMap.get("applicantLName"),  caseDataMap.get("appRespondentLName"));
        }
        return null;
    }
}
