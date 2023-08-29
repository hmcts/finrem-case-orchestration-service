package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDataConsented;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NatureApplication;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CONSENT_ORDER_CAMELCASE_LABEL_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CONSENT_ORDER_LOWERCASE_LABEL_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CONSENT_OTHER_DOC_LABEL_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CV_OTHER_DOC_LABEL_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.VARIATION_ORDER_CAMELCASE_LABEL_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.VARIATION_ORDER_LOWERCASE_LABEL_VALUE;

@RunWith(org.mockito.junit.MockitoJUnitRunner.class)
public class FinremConsentedApplicationHelperTest {

    @Mock
    private DocumentConfiguration documentConfigurationMock;

    @Test
    public void isVariationOrder() {
        FinremCallbackRequest<FinremCaseDataConsented> callbackRequest = callbackRequest();
        callbackRequest.getCaseDetails().getData().getNatureApplicationWrapper().setNatureOfApplication2(
            List.of(NatureApplication.VARIATION_ORDER, NatureApplication.PENSION_SHARING_ORDER, NatureApplication.LUMP_SUM_ORDER));
        ConsentedApplicationHelper helper = new ConsentedApplicationHelper(documentConfigurationMock);
        assertTrue(helper.isVariationOrder(callbackRequest.getCaseDetails().getData()));
    }

    @Test
    public void setConsentVariationOrderLabelField() {
        FinremCallbackRequest<FinremCaseDataConsented> callbackRequest = callbackRequest();
        FinremCaseDataConsented data = callbackRequest.getCaseDetails().getData();
        data.getNatureApplicationWrapper().setNatureOfApplication2(
            List.of(NatureApplication.VARIATION_ORDER, NatureApplication.PENSION_SHARING_ORDER, NatureApplication.LUMP_SUM_ORDER));

        ConsentedApplicationHelper helper = new ConsentedApplicationHelper(documentConfigurationMock);
        helper.setConsentVariationOrderLabelField(data);

        assertEquals(VARIATION_ORDER_CAMELCASE_LABEL_VALUE, data.getConsentVariationOrderLabelC());
        assertEquals(VARIATION_ORDER_LOWERCASE_LABEL_VALUE, data.getConsentVariationOrderLabelL());
        assertEquals(CV_OTHER_DOC_LABEL_VALUE, data.getOtherDocLabel());
    }

    @Test
    public void givenCase_whenEmptyNatureOfApplicationIsEmpty_thenReturnEmptyList() {
        FinremCallbackRequest<FinremCaseDataConsented> callbackRequest = callbackRequest();
        FinremCaseDataConsented data = callbackRequest.getCaseDetails().getData();

        ConsentedApplicationHelper helper = new ConsentedApplicationHelper(documentConfigurationMock);
        helper.setConsentVariationOrderLabelField(callbackRequest.getCaseDetails().getData());

        assertEquals(CONSENT_ORDER_CAMELCASE_LABEL_VALUE, data.getConsentVariationOrderLabelC());
        assertEquals(CONSENT_ORDER_LOWERCASE_LABEL_VALUE, data.getConsentVariationOrderLabelL());
        assertEquals(CONSENT_OTHER_DOC_LABEL_VALUE, data.getOtherDocLabel());
    }

    private FinremCallbackRequest<FinremCaseDataConsented> callbackRequest() {
        return FinremCallbackRequest
            .<FinremCaseDataConsented>builder()
            .caseDetails(FinremCaseDetails.<FinremCaseDataConsented>builder()
                .data(FinremCaseDataConsented.builder().build()).build())
            .build();
    }
}