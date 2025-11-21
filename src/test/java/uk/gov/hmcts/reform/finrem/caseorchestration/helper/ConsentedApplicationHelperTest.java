package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Region;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.AllocatedRegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.RegionWrapper;

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

@ExtendWith(MockitoExtension.class)
class ConsentedApplicationHelperTest {

    @InjectMocks
    private ConsentedApplicationHelper consentedApplicationHelper;

    @Test
    void isVariationOrder() {
        CallbackRequest callbackRequest = callbackRequest();
        callbackRequest.getCaseDetails().getData()
            .put(CONSENTED_NATURE_OF_APPLICATION, List.of("Variation Order", "Pension document", "Lump sum"));
        assertTrue(consentedApplicationHelper.isVariationOrder(callbackRequest.getCaseDetails().getData()));
    }

    @Test
    void setConsentVariationOrderLabelField() {
        CallbackRequest callbackRequest = callbackRequest();
        Map<String, Object> data = callbackRequest.getCaseDetails().getData();
        data.put(CONSENTED_NATURE_OF_APPLICATION, List.of("Variation Order", "Pension document", "Lump sum"));

        consentedApplicationHelper.setConsentVariationOrderLabelField(callbackRequest.getCaseDetails().getData());

        assertEquals(VARIATION_ORDER_CAMELCASE_LABEL_VALUE, data.get(CV_ORDER_CAMELCASE_LABEL_FIELD));
        assertEquals(VARIATION_ORDER_LOWERCASE_LABEL_VALUE, data.get(CV_LOWERCASE_LABEL_FIELD));
        assertEquals(CV_OTHER_DOC_LABEL_VALUE, data.get(CV_OTHER_DOC_LABEL_FIELD));
    }

    @Test
    void givenCase_whenEmptyNatureOfApplicationIsEmpty_thenReturnEmptyList() {
        CallbackRequest callbackRequest = callbackRequest();
        Map<String, Object> data = callbackRequest.getCaseDetails().getData();

        consentedApplicationHelper = new ConsentedApplicationHelper();
        consentedApplicationHelper.setConsentVariationOrderLabelField(callbackRequest.getCaseDetails().getData());

        assertEquals(CONSENT_ORDER_CAMELCASE_LABEL_VALUE, data.get(CV_ORDER_CAMELCASE_LABEL_FIELD));
        assertEquals(CONSENT_ORDER_LOWERCASE_LABEL_VALUE, data.get(CV_LOWERCASE_LABEL_FIELD));
        assertEquals(CONSENT_OTHER_DOC_LABEL_VALUE, data.get(CV_OTHER_DOC_LABEL_FIELD));
    }

    @Test
    void shouldReturnErrorWhenHighCourtSelected() {
        // given
        FinremCaseData caseData = buildCaseDataWithRegion(Region.HIGHCOURT);

        // when
        List<String> result = consentedApplicationHelper.validateRegionList(caseData);

        // then
        assertEquals(
            List.of("You cannot select the High Court for a consent application."),
            result
        );
    }

    @Test
    void shouldReturnEmptyListWhenRegionIsNotHighCourt() {
        // given
        FinremCaseData caseData = buildCaseDataWithRegion(Region.MIDLANDS); // any non-HC region

        // when
        List<String> result = consentedApplicationHelper.validateRegionList(caseData);

        // then
        assertTrue(result.isEmpty());
    }

    private CallbackRequest callbackRequest() {
        return CallbackRequest
            .builder()
            .caseDetails(CaseDetails.builder()
                .data(new HashMap<>()).build())
            .build();
    }

    private FinremCaseData buildCaseDataWithRegion(Region region) {
        AllocatedRegionWrapper allocatedRegion = AllocatedRegionWrapper.builder()
            .regionList(region)
            .build();

        RegionWrapper regionWrapper = RegionWrapper.builder()
            .allocatedRegionWrapper(allocatedRegion)
            .build();

        return FinremCaseData.builder()
            .regionWrapper(regionWrapper)
            .build();
    }

}
