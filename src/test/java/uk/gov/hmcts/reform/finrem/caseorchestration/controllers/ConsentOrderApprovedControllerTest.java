package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderApprovedDocumentService;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@WebMvcTest(ConsentOrderApprovedController.class)
public class ConsentOrderApprovedControllerTest extends BaseControllerTest {

    @MockBean
    private ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;
    @MockBean
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    public String contestedConsentOrderApprovedEndpoint() {
        return "/case-orchestration/consent-in-contested/consent-order-approved";
    }

    @Test
    public void consentInContestedConsentOrderApprovedShouldProcessDocuments() throws Exception {
        when(finremCaseDetailsMapper.mapToCaseDetails(any())).thenReturn(CaseDetails.builder().data(new HashMap<>()).build());
        when(finremCaseDetailsMapper.mapToFinremCaseDetails(any())).thenReturn(FinremCaseDetails.builder().data(
            FinremCaseData.builder().build()).build());
        when(consentOrderApprovedDocumentService.generateAndPopulateConsentOrderLetter(any(), any())).thenReturn(
            CaseDetails.builder().data(new HashMap<>()).build());
        doValidCaseDataSetUp();

        mvc.perform(post(contestedConsentOrderApprovedEndpoint())
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(consentOrderApprovedDocumentService, times(1))
            .stampAndPopulateContestedConsentApprovedOrderCollection(any(), eq(AUTH_TOKEN), anyString());
        verify(consentOrderApprovedDocumentService, times(1))
            .generateAndPopulateConsentOrderLetter(any(), eq(AUTH_TOKEN));
    }

    @Test
    public void consentInContestedConsentOrderApprovedShouldProcessPensionDocs() throws Exception {
        when(finremCaseDetailsMapper.mapToCaseDetails(any())).thenReturn(CaseDetails.builder().data(new HashMap<>()).build());
        when(finremCaseDetailsMapper.mapToFinremCaseDetails(any())).thenReturn(FinremCaseDetails.builder().data(
            FinremCaseData.builder().build()).build());
        when(consentOrderApprovedDocumentService.generateAndPopulateConsentOrderLetter(any(), any())).thenReturn(
            CaseDetails.builder().data(new HashMap<>()).build());
        doValidConsentInContestWithPensionData();

        mvc.perform(post(contestedConsentOrderApprovedEndpoint())
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(consentOrderApprovedDocumentService)
            .stampAndPopulateContestedConsentApprovedOrderCollection(any(), eq(AUTH_TOKEN), any());
    }
}
