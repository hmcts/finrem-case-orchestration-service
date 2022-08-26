package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OldCallbackRequest;

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

    private ConsentedApplicationHelper helper;

    @Before
    public void setUp() {
        helper = new ConsentedApplicationHelper(new DocumentConfiguration());
    }

    @Test
    public void isVariationOrder() {
        OldCallbackRequest callbackRequest =  callbackRequest();
        callbackRequest.getCaseDetails().getData()
            .put(CONSENTED_NATURE_OF_APPLICATION, List.of("Variation Order","Pension document","Lump sum"));
        assertTrue(helper.isVariationOrder(callbackRequest.getCaseDetails().getData()));
    }

    @Test
    public void setConsentVariationOrderLabelField() {
        OldCallbackRequest callbackRequest =  callbackRequest();
        Map<String, Object> data = callbackRequest.getCaseDetails().getData();
        data.put(CONSENTED_NATURE_OF_APPLICATION, List.of("Variation Order","Pension document","Lump sum"));

        helper.setConsentVariationOrderLabelField(callbackRequest.getCaseDetails().getData());

        assertEquals(VARIATION_ORDER_CAMELCASE_LABEL_VALUE, data.get(CV_ORDER_CAMELCASE_LABEL_FIELD));
        assertEquals(VARIATION_ORDER_LOWERCASE_LABEL_VALUE, data.get(CV_LOWERCASE_LABEL_FIELD));
        assertEquals(CV_OTHER_DOC_LABEL_VALUE, data.get(CV_OTHER_DOC_LABEL_FIELD));
    }

    @Test
    public void givenCase_whenEmptyNatureOfApplicationIsEmpty_thenReturnEmptyList() {
        OldCallbackRequest callbackRequest =  callbackRequest();
        Map<String, Object> data = callbackRequest.getCaseDetails().getData();

        helper.setConsentVariationOrderLabelField(callbackRequest.getCaseDetails().getData());

        assertEquals(CONSENT_ORDER_CAMELCASE_LABEL_VALUE, data.get(CV_ORDER_CAMELCASE_LABEL_FIELD));
        assertEquals(CONSENT_ORDER_LOWERCASE_LABEL_VALUE, data.get(CV_LOWERCASE_LABEL_FIELD));
        assertEquals(CONSENT_OTHER_DOC_LABEL_VALUE, data.get(CV_OTHER_DOC_LABEL_FIELD));
    }

    private OldCallbackRequest callbackRequest() {
        return OldCallbackRequest
            .builder()
            .caseDetails(CaseDetails.builder()
                .data(new HashMap<>()).build())
            .build();
    }
}