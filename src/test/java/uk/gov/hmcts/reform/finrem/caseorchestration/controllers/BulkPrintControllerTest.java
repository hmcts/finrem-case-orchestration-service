package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.bsp.common.utils.ResourceLoader;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@WebMvcTest(BulkPrintController.class)
public class BulkPrintControllerTest extends BaseControllerTest {

    private static final String BULK_PRINT_URI = "/case-orchestration/bulk-print";
    private static final String CONTESTED_BULK_PRINT_CONSENT_ORDER_NOT_APPROVED_JSON
        = "fixtures/contested/bulk_print_consent_order_not_approved.json";

    @MockBean
    private BulkPrintService bulkPrintService;

    @Test
    public void shouldSendForBulkPrintPackWithRespondentAndApplicantAddress() throws Exception {
        mvc.perform(
            post(BULK_PRINT_URI)
                    .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                    .content(ResourceLoader.loadResourceAsString(CONTESTED_BULK_PRINT_CONSENT_ORDER_NOT_APPROVED_JSON))
                    .contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andDo(print());

        verify(bulkPrintService, times(1)).sendLetterToApplicant(eq(AUTH_TOKEN), any(CaseDetails.class));
        verify(bulkPrintService, times(1)).sendLetterToRespondent(eq(AUTH_TOKEN), any(CaseDetails.class));
    }
}
