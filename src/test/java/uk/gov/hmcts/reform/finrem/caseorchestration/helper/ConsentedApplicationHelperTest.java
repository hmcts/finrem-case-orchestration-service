package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import org.junit.Test;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CONSENT_ORDER_CAMELCASE_LABEL_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CONSENT_ORDER_LOWERCASE_LABEL_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CONSENT_OTHER_DOC_LABEL_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CV_LOWERCASE_LABEL_FIELD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CV_ORDER_CAMELCASE_LABEL_FIELD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CV_OTHER_DOC_LABEL_FIELD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CV_OTHER_DOC_LABEL_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.VARIATION_ORDER_CAMELCASE_LABEL_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.VARIATION_ORDER_LOWERCASE_LABEL_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_NATURE_OF_APPLICATION;

public class ConsentedApplicationHelperTest {

    @Test
    public void isVariationOrder() {
        CallbackRequest callbackRequest =  callbackRequest();
        callbackRequest.getCaseDetails().getData()
            .put(CONSENTED_NATURE_OF_APPLICATION, List.of("Variation Order","Pension document","Lump sum"));
        ConsentedApplicationHelper helper = new ConsentedApplicationHelper();
        assertTrue(helper.isVariationOrder(callbackRequest.getCaseDetails().getData()));
    }

    @Test
    public void setConsentVariationOrderLabelField() {
        CallbackRequest callbackRequest =  callbackRequest();
        Map<String, Object> data = callbackRequest.getCaseDetails().getData();
        data.put(CONSENTED_NATURE_OF_APPLICATION, List.of("Variation Order","Pension document","Lump sum"));

        ConsentedApplicationHelper helper = new ConsentedApplicationHelper();
        helper.setConsentVariationOrderLabelField(callbackRequest.getCaseDetails().getData());

        assertEquals(VARIATION_ORDER_CAMELCASE_LABEL_VALUE, data.get(CV_ORDER_CAMELCASE_LABEL_FIELD));
        assertEquals(VARIATION_ORDER_LOWERCASE_LABEL_VALUE, data.get(CV_LOWERCASE_LABEL_FIELD));
        assertEquals(CV_OTHER_DOC_LABEL_VALUE, data.get(CV_OTHER_DOC_LABEL_FIELD));
    }

    @Test
    public void givenCase_whenEmptyNatureOfApplicationIsEmpty_thenReturnEmptyList() {
        CallbackRequest callbackRequest =  callbackRequest();
        Map<String, Object> data = callbackRequest.getCaseDetails().getData();

        ConsentedApplicationHelper helper = new ConsentedApplicationHelper();
        helper.setConsentVariationOrderLabelField(callbackRequest.getCaseDetails().getData());

        assertEquals(CONSENT_ORDER_CAMELCASE_LABEL_VALUE, data.get(CV_ORDER_CAMELCASE_LABEL_FIELD));
        assertEquals(CONSENT_ORDER_LOWERCASE_LABEL_VALUE, data.get(CV_LOWERCASE_LABEL_FIELD));
        assertEquals(CONSENT_OTHER_DOC_LABEL_VALUE, data.get(CV_OTHER_DOC_LABEL_FIELD));
    }

    private CallbackRequest callbackRequest() {
        return CallbackRequest
            .builder()
            .caseDetails(CaseDetails.builder()
                .data(new HashMap<>()).build())
            .build();
    }
}