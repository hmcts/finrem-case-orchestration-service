package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderPrintService;

import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.feignError;

@WebMvcTest(BulkPrintController.class)
public class BulkPrintControllerTest extends BaseControllerTest {

    private ObjectMapper mapper = new ObjectMapper();

    private static final String BULK_PRINT_URI = "/case-orchestration/bulk-print";
    private static final String CONTESTED_HWF_JSON = "/fixtures/contested/hwf.json";
    private static final String AFTER_BULK_PRINT = "/fixtures/bulkprint/after-bulk-print.json";
    private static final String CONSENTED_BULK_PRINT_CONSENT_ORDER_NOT_APPROVED_JSON
        = "/fixtures/contested/bulk_print_consent_order_not_approved.json";

    @MockBean
    private BulkPrintService bulkPrintService;

    @MockBean
    private ConsentOrderPrintService consentOrderPrintService;

    @Test
    public void shouldSendForBulkPrint() throws Exception {
        when(consentOrderPrintService.sendConsentOrderToBulkPrint(any(), any())).thenReturn(caseData());

        mvc.perform(
            post(BULK_PRINT_URI)
                .content(resourceContentAsString(CONSENTED_BULK_PRINT_CONSENT_ORDER_NOT_APPROVED_JSON))
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.data.bulkPrintCoverSheetRes", is(1)))
                .andExpect(jsonPath("$.data.bulkPrintLetterIdRes", is(1)))
                .andExpect(jsonPath("$.data.bulkPrintLetterIdApp", is(1)));
        verify(consentOrderPrintService, times(1)).sendConsentOrderToBulkPrint(any(), any());
    }

    @Test
    public void shouldThrowExceptionOnSendForBulkPrint() throws Exception {
        when(consentOrderPrintService.sendConsentOrderToBulkPrint(any(), any())).thenThrow(feignError());
        mvc.perform(
            post(BULK_PRINT_URI)
                .content(resourceContentAsString(CONTESTED_HWF_JSON))
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isInternalServerError());
        verify(consentOrderPrintService).sendConsentOrderToBulkPrint(any(), any());
    }

    private Map<String, Object> caseData() {
        return TestSetUpUtils.caseDetailsFromResource(AFTER_BULK_PRINT, mapper).getData();
    }
}
