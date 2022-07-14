package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CONSENT_ORDER_CAMELCASE_LABEL_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CONSENT_ORDER_LOWERCASE_LABEL_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CONSENT_OTHER_DOC_LABEL_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CV_LOWERCASE_LABEL_FIELD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CV_ORDER_CAMELCASE_LABEL_FIELD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CV_OTHER_DOC_LABEL_FIELD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CV_OTHER_DOC_LABEL_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.VARIATION_ORDER_CAMELCASE_LABEL_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.VARIATION_ORDER_LOWERCASE_LABEL_VALUE;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConsentedApplicationHelper {

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

    public Boolean isVariationOrder(final Map<String, Object> caseData) {
        List<String> natureOfApplicationList = (List<String>) caseData.get("natureOfApplication2");
        return (!natureOfApplicationList.isEmpty() && natureOfApplicationList.contains("Variation Order"));
    }
}
