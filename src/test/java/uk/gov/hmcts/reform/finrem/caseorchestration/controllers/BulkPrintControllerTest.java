package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.bsp.common.utils.ResourceLoader;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.bulk.print.BulkPrintMetadata;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;

import java.util.UUID;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@WebMvcTest(BulkPrintController.class)
public class BulkPrintControllerTest extends BaseControllerTest {

    private static final String BULK_PRINT_URI = "/case-orchestration/bulk-print";
    private static final String BULK_PRINT_TO_APPLICANT_AND_RESPONDENT
        = "fixtures/contested/bulk_print_consent_order_not_approved.json";
    private static final String BULK_PRINT_TO__RESPONDENT_ONLY
            = "fixtures/contested/bulk-print-to-respondent-only.json";

    @MockBean
    private BulkPrintService bulkPrintService;

    @Test
    public void shouldSendForBulkPrintPackWithRespondentAndApplicantAddress() throws Exception {
        when(bulkPrintService.sendLetterToRespondent(eq(AUTH_TOKEN), any(CaseDetails.class)))
                .thenReturn(BulkPrintMetadata.builder().coverSheet(new CaseDocument()).letterId(UUID.randomUUID()).build());
        when(bulkPrintService.sendLetterToApplicant(eq(AUTH_TOKEN), any(CaseDetails.class)))
                .thenReturn(BulkPrintMetadata.builder().coverSheet(new CaseDocument()).letterId(UUID.randomUUID()).build());

        mvc.perform(
            post(BULK_PRINT_URI)
                    .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                    .content(ResourceLoader.loadResourceAsString(BULK_PRINT_TO_APPLICANT_AND_RESPONDENT))
                    .contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andDo(print());

        verify(bulkPrintService, times(1)).sendLetterToApplicant(eq(AUTH_TOKEN), any(CaseDetails.class));
        verify(bulkPrintService, times(1)).sendLetterToRespondent(eq(AUTH_TOKEN), any(CaseDetails.class));
        verifyNoMoreInteractions(bulkPrintService);
    }

    @Test
    public void shouldSendForBulkPrintPackWithRespondentOnly() throws Exception {
        when(bulkPrintService.sendLetterToRespondent(eq(AUTH_TOKEN), any(CaseDetails.class)))
                .thenReturn(BulkPrintMetadata.builder().coverSheet(new CaseDocument()).letterId(UUID.randomUUID()).build());

        mvc.perform(
                post(BULK_PRINT_URI)
                        .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                        .content(ResourceLoader.loadResourceAsString(BULK_PRINT_TO__RESPONDENT_ONLY))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());

        verify(bulkPrintService, times(0)).sendLetterToApplicant(eq(AUTH_TOKEN), any(CaseDetails.class));
        verify(bulkPrintService, times(1)).sendLetterToRespondent(eq(AUTH_TOKEN), any(CaseDetails.class));
        verifyNoMoreInteractions(bulkPrintService);
    }
}
