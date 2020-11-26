package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderPrintService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.feignError;

@WebMvcTest(BulkPrintController.class)
public class BulkPrintControllerTest extends BaseControllerTest {

    private static final String BULK_PRINT_URI = "/case-orchestration/bulk-print";
    private static final String CONTESTED_HWF_JSON = "/fixtures/contested/hwf.json";
    private static final String CONSENTED_BULK_PRINT_CONSENT_ORDER_NOT_APPROVED_JSON
        = "/fixtures/contested/bulk_print_consent_order_not_approved.json";

    @MockBean private ConsentOrderPrintService consentOrderPrintService;

    @Test
    public void shouldSendForBulkPrint() throws Exception {
        mvc.perform(
            post(BULK_PRINT_URI)
                .content(resourceContentAsString(CONSENTED_BULK_PRINT_CONSENT_ORDER_NOT_APPROVED_JSON))
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(consentOrderPrintService, times(1)).sendConsentOrderToBulkPrint(any(), any());
    }

    @Test
    public void shouldThrowExceptionOnSendForBulkPrint() throws Exception {
        doThrow(feignError()).when(consentOrderPrintService).sendConsentOrderToBulkPrint(any(), any());

        mvc.perform(
            post(BULK_PRINT_URI)
                .content(resourceContentAsString(CONTESTED_HWF_JSON))
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isInternalServerError());
        verify(consentOrderPrintService).sendConsentOrderToBulkPrint(any(), any());
    }
}
