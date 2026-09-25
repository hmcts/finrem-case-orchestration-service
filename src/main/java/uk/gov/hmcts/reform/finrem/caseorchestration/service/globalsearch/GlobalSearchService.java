package uk.gov.hmcts.reform.finrem.caseorchestration.service.globalsearch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
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
     * @param caseDetails -  the case data map to update
     */
    public void setGlobalSearchDataByMap(CaseDetails caseDetails) {

        if (featureToggleService.isGlobalSearchEnabled() && isConsentedApplication(caseDetails)) {
            Map<String, Object> caseDataMap = caseDetails.getData();
            log.info("setGlobalSearchDataByMap::Received request to set global search fields "
                + "for {} case type with CCD ID: {}", caseDetails.getCaseTypeId(),  caseDetails.getId());
            DynamicListElement element = DynamicListElement.builder().code(FINANCIAL_REMEDY).build();
            caseDataMap.put("caseManagementCategory", DynamicList.builder().value(element).listItems(List.of(element)).build());
            caseDataMap.put("caseNameHmctsInternal", getCaseNameHmctsInternal(caseDataMap));
            caseDataMap.put("caseManagementLocation", caseManagementLocationService.getCaseLocation(caseDataMap));
            log.info("setGlobalSearchDataByMap::global search fields are set for {} case type with CCD ID: {}",
                caseDetails.getCaseTypeId(), caseDetails.getId());
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

    private Boolean isConsentedApplication(CaseDetails caseDetails) {
        return CaseType.CONSENTED.getCcdType().equalsIgnoreCase(nullToEmpty(caseDetails.getCaseTypeId()));
    }
}
