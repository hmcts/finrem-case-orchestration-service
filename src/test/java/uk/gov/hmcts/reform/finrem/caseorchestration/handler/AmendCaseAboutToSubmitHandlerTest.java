package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;

import java.io.InputStream;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
public class AmendCaseAboutToSubmitHandlerTest {
    private static final String AUTH_TOKEN = "4d73f8d4-2a8d-48e2-af91-11cbaa642345";
    private static final String PERIODIC_PAYMENT_CHILD_JSON = "/fixtures/updatecase/amend-periodic-payment-order.json";
    private static final String PERIODIC_PAYMENT_JSON = "/fixtures/updatecase/amend-periodic-payment-order-without"
        + "-agreement-with-valid-enums.json";
    private static final String PROPERTY_ADJ_JSON = "/fixtures/updatecase/amend-property-adjustment-details.json";
    private static final String PROPERTY_DETAILS_JSON = "/fixtures/updatecase/remove-property-adjustment-details.json";
    private static final String PAYMENT_UNCHECKED_JSON = "/fixtures/updatecase/amend-remove-periodic-payment-order.json";

    private AmendCaseAboutToSubmitHandler handler;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        FinremCaseDetailsMapper finremCaseDetailsMapper = new FinremCaseDetailsMapper(objectMapper);
        handler = new AmendCaseAboutToSubmitHandler(finremCaseDetailsMapper);
    }

    @Test
    void canHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_SUBMIT, CONSENTED, EventType.AMEND_CASE);
    }

    @Test
    void givenCase_whenCaseUpdated_thenShouldDeletePropertyDetails() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest(PROPERTY_DETAILS_JSON);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        final FinremCaseData responseData = response.getData();
        assertNull(responseData.getNatureApplicationWrapper().getNatureOfApplication3a());
        assertNull(responseData.getNatureApplicationWrapper().getNatureOfApplication3b());
    }

    @Test
    void givenCase_whenCaseUpdated_thenShouldNotRemovePropertyDetails() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest(PROPERTY_ADJ_JSON);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        final FinremCaseData responseData = response.getData();
        assertNotNull(responseData.getNatureApplicationWrapper().getNatureOfApplication3a());
        assertNotNull(responseData.getNatureApplicationWrapper().getNatureOfApplication3b());
    }

    @Test
    void givenCase_whenCaseUpdated_thenShouldDeletePeriodicPaymentDetailsWithOutWrittenAgreement() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest(PERIODIC_PAYMENT_JSON);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        final FinremCaseData responseData = response.getData();
        assertNotNull(responseData.getNatureApplicationWrapper().getNatureOfApplication6());
        assertNotNull(responseData.getNatureApplicationWrapper().getNatureOfApplication7());
    }

    @Test
    void givenCase_whenCaseUpdated_thenShouldDeletePeriodicPaymentDetailsWithWrittenAgreementForChildren() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest(PERIODIC_PAYMENT_CHILD_JSON);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        final FinremCaseData responseData = response.getData();
        assertNull(responseData.getNatureApplicationWrapper().getNatureOfApplication6());
        assertNull(responseData.getNatureApplicationWrapper().getNatureOfApplication7());
    }

    @Test
    void givenCase_whenCaseUpdated_thenShouldDeletePeriodicPaymentDetailsIfUnchecked() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest(PAYMENT_UNCHECKED_JSON);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        final FinremCaseData responseData = response.getData();
        assertNull(responseData.getNatureApplicationWrapper().getNatureOfApplication5());
        assertNull(responseData.getNatureApplicationWrapper().getNatureOfApplication6());
        assertNull(responseData.getNatureApplicationWrapper().getNatureOfApplication7());
        assertNull(responseData.getNatureApplicationWrapper().getOrderForChildrenQuestion1());
    }

    private FinremCallbackRequest buildCallbackRequest(final String path) {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(path)) {
            return objectMapper.readValue(resourceAsStream, FinremCallbackRequest.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
