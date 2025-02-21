package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.AMEND_CASE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class AmendCaseConsentedMidHandlerTest {

    private AmendCaseConsentedMidHandler handler;
    @Mock
    private ConsentOrderService consentOrderService;

    @Mock
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        FinremCaseDetailsMapper finremCaseDetailsMapper = new FinremCaseDetailsMapper(objectMapper);
        handler = new AmendCaseConsentedMidHandler(finremCaseDetailsMapper, consentOrderService, objectMapper);
    }

    @Test
    void canHandle() {
        assertCanHandle(handler, CallbackType.MID_EVENT, CONSENTED, EventType.AMEND_CASE);
    }

    @Test
    void givenCase_whenServiceReturnsNoErrors_handleMidEvent() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        when(consentOrderService.performCheck(any(), anyString())).thenReturn(Collections.emptyList());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response.getErrors().size(), is(0));
    }

    @Test
    void givenCase_whenServiceReturnsMultipleErrors_handleMidEvent() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        List<String> errors = Arrays.asList("Error 1", "Error 2");
        when(consentOrderService.performCheck(any(), anyString())).thenReturn(errors);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response.getErrors(), is(errors));
    }

    private FinremCallbackRequest buildCallbackRequest() {
        return FinremCallbackRequest
            .builder()
            .eventType(AMEND_CASE)
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONSENTED)
                .data(FinremCaseData.builder().ccdCaseType(CONSENTED).build()).build())
            .caseDetailsBefore(FinremCaseDetails.builder().id(123L).caseType(CONSENTED)
                .data(FinremCaseData.builder().ccdCaseType(CONSENTED).build()).build())
            .build();
    }
}
