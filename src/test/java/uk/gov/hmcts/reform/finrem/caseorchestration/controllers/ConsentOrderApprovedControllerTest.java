package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import org.junit.Test;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OrderApprovedDocumentService;

import javax.ws.rs.core.MediaType;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.feignError;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.FILE_NAME;

@WebMvcTest(ConsentOrderApprovedController.class)
public class ConsentOrderApprovedControllerTest extends BaseControllerTest {

    @MockBean
    private OrderApprovedDocumentService documentService;

    public String endpoint() {
        return "/case-orchestration/documents/consent-order-approved";
    }

    @Test
    public void consentOrderLetterSuccess() throws Exception {
        doValidCaseDataSetUp();
        whenServiceGeneratesDocument().thenReturn(caseDocument());

        mvc.perform(post(endpoint())
                .content(requestContent.toString())
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.data.approvedConsentOrderLetter.document_url", is(DOC_URL)))
                .andExpect(
                        jsonPath("$.data.approvedConsentOrderLetter.document_filename",
                                is(FILE_NAME)))
                .andExpect(
                        jsonPath("$.data.approvedConsentOrderLetter.document_binary_url",
                                is(BINARY_URL)));
    }

    @Test
    public void consentOrderLetter400Error() throws Exception {
        doEmtpyCaseDataSetUp();

        mvc.perform(post(endpoint())
                .content(requestContent.toString())
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void consentOrderLetter500Error() throws Exception {
        doValidCaseDataSetUp();
        whenServiceGeneratesDocument().thenThrow(feignError());

        mvc.perform(post(endpoint())
                .content(requestContent.toString())
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    private OngoingStubbing<CaseDocument> whenServiceGeneratesDocument() {
        return when(
                documentService.generateApprovedConsentOrderLetter(isA(CaseDetails.class), eq(AUTH_TOKEN)));
    }
}
