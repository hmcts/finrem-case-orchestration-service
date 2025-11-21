package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NatureApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Region;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CONSENT_ORDER_CAMELCASE_LABEL_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CONSENT_ORDER_LOWERCASE_LABEL_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CONSENT_OTHER_DOC_LABEL_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CV_LOWERCASE_LABEL_FIELD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CV_ORDER_CAMELCASE_LABEL_FIELD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CV_OTHER_DOC_LABEL_FIELD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CV_OTHER_DOC_LABEL_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.VARIATION_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.VARIATION_ORDER_CAMELCASE_LABEL_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.VARIATION_ORDER_LOWERCASE_LABEL_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.CONSENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.VARIATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_NATURE_OF_APPLICATION;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConsentedApplicationHelper {

    public void setConsentVariationOrderLabelField(FinremCaseData caseData) {
        if (Boolean.TRUE.equals(isVariationOrder(caseData))) {
            caseData.getConsentOrderWrapper().setConsentVariationOrderLabelC(VARIATION_ORDER_CAMELCASE_LABEL_VALUE);
            caseData.getConsentOrderWrapper().setConsentVariationOrderLabelL(VARIATION_ORDER_LOWERCASE_LABEL_VALUE);
            caseData.getConsentOrderWrapper().setOtherDocLabel(CV_OTHER_DOC_LABEL_VALUE);
        } else {
            caseData.getConsentOrderWrapper().setConsentVariationOrderLabelC(CONSENT_ORDER_CAMELCASE_LABEL_VALUE);
            caseData.getConsentOrderWrapper().setConsentVariationOrderLabelL(CONSENT_ORDER_LOWERCASE_LABEL_VALUE);
            caseData.getConsentOrderWrapper().setOtherDocLabel(CONSENT_OTHER_DOC_LABEL_VALUE);
        }
    }

    public void setConsentVariationOrderLabelField(Map<String, Object> caseData) {
        if (Boolean.TRUE.equals(isVariationOrder(caseData))) {
            caseData.put(CV_ORDER_CAMELCASE_LABEL_FIELD, VARIATION_ORDER_CAMELCASE_LABEL_VALUE);
            caseData.put(CV_LOWERCASE_LABEL_FIELD, VARIATION_ORDER_LOWERCASE_LABEL_VALUE);
            caseData.put(CV_OTHER_DOC_LABEL_FIELD, CV_OTHER_DOC_LABEL_VALUE);
        } else {
            caseData.put(CV_ORDER_CAMELCASE_LABEL_FIELD, CONSENT_ORDER_CAMELCASE_LABEL_VALUE);
            caseData.put(CV_LOWERCASE_LABEL_FIELD, CONSENT_ORDER_LOWERCASE_LABEL_VALUE);
            caseData.put(CV_OTHER_DOC_LABEL_FIELD, CONSENT_OTHER_DOC_LABEL_VALUE);
        }
    }

    public boolean isVariationOrder(final FinremCaseData caseData) {
        List<NatureApplication> natureOfApplicationList = caseData.getNatureApplicationWrapper().getNatureOfApplication2();
        log.info("Nature list {}", natureOfApplicationList);
        return (!CollectionUtils.isEmpty(natureOfApplicationList)
            && natureOfApplicationList.contains(NatureApplication.VARIATION_ORDER));
    }

    public Boolean isVariationOrder(final Map<String, Object> caseData) {
        List<String> natureOfApplicationList = getNatureOfApplicationList(caseData);
        log.info("Nature list {}", natureOfApplicationList);
        return (!CollectionUtils.isEmpty(natureOfApplicationList) && natureOfApplicationList.contains(VARIATION_ORDER));
    }

    public List<String> getNatureOfApplicationList(final Map<String, Object> caseData) {
        Object obj = caseData.get(CONSENTED_NATURE_OF_APPLICATION);
        return obj == null ? new ArrayList<>() : convertToList(caseData.get(CONSENTED_NATURE_OF_APPLICATION));
    }

    public List<String> convertToList(Object object) {
        return new ObjectMapper().convertValue(object, new TypeReference<>() {
        });
    }

    public String getOrderType(FinremCaseData caseData) {
        if (caseData.isContestedApplication()) {
            return CONSENT;
        }

        return Boolean.TRUE.equals(isVariationOrder(caseData))
            ? VARIATION
            : CONSENT;
    }

    public List<String> validateRegionList(FinremCaseData caseData) {
        boolean isHighCourt =
            Region.HIGHCOURT.equals(
                caseData.getRegionWrapper()
                    .getAllocatedRegionWrapper()
                    .getRegionList()
            );

        return isHighCourt
            ? List.of("You cannot select the High Court for a consent application.")
            : List.of();
    }
}
