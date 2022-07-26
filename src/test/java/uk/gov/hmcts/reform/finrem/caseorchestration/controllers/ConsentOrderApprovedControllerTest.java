package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderApprovedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderPrintService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;

@WebMvcTest(ConsentOrderApprovedController.class)
public class ConsentOrderApprovedControllerTest extends BaseControllerTest {

    @MockBean private ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;
    @MockBean private ConsentOrderPrintService consentOrderPrintService;

    public String contestedConsentOrderApprovedEndpoint() {
        return "/case-orchestration/consent-in-contested/consent-order-approved";
    }

    public String contestedConsentSendOrderEndpoint() {
        return "/case-orchestration/consent-in-contested/send-order";
    }

    @Test
    public void consentInContestedConsentOrderApprovedShouldProcessDocuments() throws Exception {
        doValidCaseDataSetUp();

        mvc.perform(post(contestedConsentOrderApprovedEndpoint())
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(consentOrderApprovedDocumentService, times(1)).stampAndPopulateContestedConsentApprovedOrderCollection(any(), eq(AUTH_TOKEN));
        verify(consentOrderApprovedDocumentService, times(1)).generateAndPopulateConsentOrderLetter(any(), eq(AUTH_TOKEN));
    }

    @Test
    public void consentInContestedConsentOrderApprovedShouldProcessPensionDocs() throws Exception {
        doValidConsentInContestWithPensionData();

        mvc.perform(post(contestedConsentOrderApprovedEndpoint())
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(consentOrderApprovedDocumentService).stampAndPopulateContestedConsentApprovedOrderCollection(any(), eq(AUTH_TOKEN));
    }

    @Test
    public void consentInContestedSendOrderShouldPrintDocsWhenNotApproved() throws Exception {
        doValidCaseDataSetUp();

        mvc.perform(post(contestedConsentSendOrderEndpoint())
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(consentOrderApprovedDocumentService, never()).generateApprovedConsentOrderLetter(any(), eq(AUTH_TOKEN));
        verify(consentOrderPrintService).sendConsentOrderToBulkPrint(any(), eq(AUTH_TOKEN));
    }

    @Test
    public void consentInContestedSendOrderShouldPrintDocsWhenApproved() throws Exception {
        doValidConsentOrderApprovedSetup();
        when(consentOrderApprovedDocumentService.generateApprovedConsentOrderLetter(any(), eq(AUTH_TOKEN)))
            .thenReturn(caseDocument());

        ResultActions result = mvc.perform(post(contestedConsentSendOrderEndpoint())
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE));

        result.andExpect(status().isOk());
        verify(consentOrderPrintService).sendConsentOrderToBulkPrint(any(), eq(AUTH_TOKEN));
    }
}
