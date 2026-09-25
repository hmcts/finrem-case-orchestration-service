package uk.gov.hmcts.reform.finrem.caseorchestration.service.globalsearch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.caselocation.CaseManagementLocationService;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Strings.nullToEmpty;

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
     * @param caseDataMap -  the case data map to update
     * @param caseTypeId - the type of the case
     * @param caseId - the ID of the case
     */
    public void setGlobalSearchDataByMap(Map<String, Object> caseDataMap, String caseTypeId, Long caseId) {

        if (featureToggleService.isGlobalSearchEnabled() && isConsentedApplication(caseTypeId)) {
            log.info("setGlobalSearchDataByMap::Received request to set global search fields "
                + "for {} case type with CCD ID: {}", caseTypeId,  caseId);
            DynamicListElement element = DynamicListElement.builder().code(FINANCIAL_REMEDY).label(FINANCIAL_REMEDY).build();
            caseDataMap.put("caseManagementCategory", DynamicList.builder().value(element).listItems(List.of(element)).build());
            caseDataMap.put("caseNameHmctsInternal", getCaseNameHmctsInternal(caseDataMap));
            caseDataMap.put("caseManagementLocation", caseManagementLocationService.getCaseLocation(caseDataMap));
            log.info("setGlobalSearchDataByMap::global search fields are set for {} case type with CCD ID: {}",
                caseTypeId, caseId);
        }
    }

    private String getCaseNameHmctsInternal(Map<String, Object> caseDataMap) {
        if (StringUtils.isNotBlank((String) caseDataMap.get("applicantLName"))
            && StringUtils.isNotBlank((String) caseDataMap.get("appRespondentLName"))) {
            return String.format("%s vs %s",
                 caseDataMap.get("applicantLName"),  caseDataMap.get("appRespondentLName"));
        }
        return FINANCIAL_REMEDY;
    }

    private Boolean isConsentedApplication(String caseTypeId) {
        return CaseType.CONSENTED.getCcdType().equalsIgnoreCase(nullToEmpty(caseTypeId));
    }
}
