package uk.gov.hmcts.reform.finrem.caseorchestration.service.globalsearch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.caselocation.CaseManagementLocationService;

import java.util.List;

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
    private final DynamicListElement element = DynamicListElement.builder().code(FINANCIAL_REMEDY).label(FINANCIAL_REMEDY).build();

    /**
     * Sets the fields required for global search on the provided case data map.
     * If the global search feature is disabled, this method does nothing.
     *
     * @param finremCaseData -  the case data map to update
     */
    public void setGlobalSearchDataByMap(FinremCaseData finremCaseData) {
        if (featureToggleService.isGlobalSearchEnabled() && finremCaseData.isConsentedApplication()) {
            log.info("setGlobalSearchDataByMap::Received request to set global search fields "
                + "for {} case type with CCD ID: {}", finremCaseData.getCcdCaseType(), finremCaseData.getCcdCaseId());
            finremCaseData.setCaseManagementCategory(DynamicList.builder().value(element).listItems(List.of(element)).build());
            finremCaseData.setCaseNameHmctsInternal(getCaseNameHmctsInternal(finremCaseData));
            finremCaseData.setCaseManagementLocation(caseManagementLocationService.getCaseLocation(finremCaseData));
            log.info("setGlobalSearchDataByMap::global search fields are set for {} case type with CCD ID: {}",
                finremCaseData.getCcdCaseType(), finremCaseData.getCcdCaseId());
        }
    }

    private String getCaseNameHmctsInternal(FinremCaseData finremCaseData) {
        if (StringUtils.isNotBlank(finremCaseData.getApplicantLastName())
            && StringUtils.isNotBlank(finremCaseData.getRespondentLastName())) {
            return String.format("%s vs %s",
                finremCaseData.getApplicantLastName(), finremCaseData.getRespondentLastName());
        }
        return FINANCIAL_REMEDY;
    }
}
