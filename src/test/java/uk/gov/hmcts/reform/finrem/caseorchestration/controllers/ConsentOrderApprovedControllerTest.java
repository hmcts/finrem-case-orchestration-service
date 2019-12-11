package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import org.junit.Test;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderApprovedDocumentService;

import javax.ws.rs.core.MediaType;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.PENSION_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.feignError;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.pensionDocumentData;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LATEST_CONSENT_ORDER;

@WebMvcTest(ConsentOrderApprovedController.class)
public class ConsentOrderApprovedControllerTest extends BaseControllerTest {

    @MockBean
    private ConsentOrderApprovedDocumentService service;

    public String endpoint() {
        return "/case-orchestration/documents/consent-order-approved";
    }

    @Test
    public void consentOrderApproved400Error() throws Exception {
        doEmtpyCaseDataSetUp();

        mvc.perform(post(endpoint())
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void consentOrderApproved500Error() throws Exception {
        doValidCaseDataSetUp();
        whenServiceGeneratesDocument().thenThrow(feignError());

        mvc.perform(post(endpoint())
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void latestConsentOrderIsMissing() throws Exception {
        doMissingLatestConsentOrder();
        whenServiceGeneratesDocument().thenReturn(caseDocument());
        whenAnnexStampingDocument().thenReturn(caseDocument());
        whenStampingDocument().thenReturn(caseDocument());
        whenStampingPensionDocuments().thenReturn(asList(pensionDocumentData()));

        ResultActions result = mvc.perform(post(endpoint())
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk());
        result.andExpect(jsonPath("$.data", not(hasKey(LATEST_CONSENT_ORDER))));
    }

    @Test
    public void consentOrderApprovedSuccess() throws Exception {
        doValidCaseDataSetUp();
        whenServiceGeneratesDocument().thenReturn(caseDocument());
        whenAnnexStampingDocument().thenReturn(caseDocument());
        whenStampingDocument().thenReturn(caseDocument());
        whenStampingPensionDocuments().thenReturn(asList(pensionDocumentData()));

        ResultActions result = mvc.perform(post(endpoint())
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk());
        assertLetter(result);
        assertConsentOrder(result);
        assertPensionDocs(result);
    }

    private OngoingStubbing<CaseDocument> whenServiceGeneratesDocument() {
        return when(service.generateApprovedConsentOrderLetter(isA(CaseDetails.class), anyString()));
    }

    private OngoingStubbing<CaseDocument> whenAnnexStampingDocument() {
        return when(service.annexStampDocument(isA(CaseDocument.class), anyString()));
    }

    private OngoingStubbing<CaseDocument> whenStampingDocument() {
        return when(service.stampDocument(isA(CaseDocument.class), anyString()));
    }

    private OngoingStubbing<List<PensionCollectionData>> whenStampingPensionDocuments() {
        return when(service.stampPensionDocuments(any(), anyString()));
    }

    private void assertLetter(ResultActions result) throws Exception {
        String path = "$.data.approvedOrderCollection[0].value.orderLetter.";
        result.andExpect(jsonPath(path + "document_url", is(DOC_URL)))
                .andExpect(jsonPath(path + "document_filename", is(FILE_NAME)))
                .andExpect(jsonPath(path + "document_binary_url", is(BINARY_URL)));
    }

    private void assertConsentOrder(ResultActions result) throws Exception {
        assertDocument(result, "$.data.approvedOrderCollection[0].value.consentOrder.");
    }

    private void assertPensionDocs(ResultActions result) throws Exception {
        String path = "$.data.approvedOrderCollection[0].value.pensionDocuments[0].value.";
        String docPath = "$.data.approvedOrderCollection[0].value.pensionDocuments[0].value.uploadedDocument.";
        result.andExpect(jsonPath(path + "typeOfDocument", is(PENSION_TYPE)));
        assertDocument(result, docPath);
    }

    private void assertDocument(ResultActions result, String path) throws Exception {
        result.andExpect(jsonPath(path + "document_url", is(DOC_URL)))
                .andExpect(jsonPath(path + "document_filename", is(FILE_NAME)))
                .andExpect(jsonPath(path + "document_binary_url", is(BINARY_URL)));
    }
}
