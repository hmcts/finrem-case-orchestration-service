package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.CaseOrchestrationApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.RefusalOrderDocumentService;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.REJECTED_ORDER_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDataWithPreviewOrder;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDataWithUploadOrder;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.feignError;

@RunWith(SpringRunner.class)
@WebMvcTest(RejectedOrderDocumentController.class)
@ContextConfiguration(classes = CaseOrchestrationApplication.class)
public class RejectedOrderDocumentControllerTest {

    private static final String API_URL = "/case-orchestration/documents/consent-order-not-approved";
    private static final String PREVIEW_API_URL = "/case-orchestration/documents/preview-consent-order-not-approved";

    @Autowired
    private WebApplicationContext applicationContext;

    @MockBean private RefusalOrderDocumentService documentService;
    @MockBean private BulkPrintService bulkPrintService;

    private MockMvc mvc;
    private JsonNode requestContent;

    @Before
    public void setUp() throws Exception {
        mvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
        doRequestSetUp();
    }

    private void doRequestSetUp() throws IOException, URISyntaxException {
        ObjectMapper objectMapper = new ObjectMapper();
        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/fee-lookup.json").toURI()));
    }

    @Test
    public void generateConsentOrderNotApprovedSuccess() throws Exception {
        when(documentService.generateConsentOrderNotApproved(eq(AUTH_TOKEN), isA(CaseDetails.class)))
                .thenReturn(caseDataWithUploadOrder(UUID.randomUUID().toString()));

        mvc.perform(post(API_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.uploadOrder[0].id", is(notNullValue())))
                .andExpect(jsonPath("$.data.uploadOrder[0].value.DocumentType", is(REJECTED_ORDER_TYPE)))
                .andExpect(jsonPath("$.data.uploadOrder[0].value.DocumentDateAdded", is(notNullValue())))
                .andExpect(jsonPath("$.data.uploadOrder[0].value.DocumentLink.document_url", is(DOC_URL)))
                .andExpect(
                        jsonPath("$.data.uploadOrder[0].value.DocumentLink.document_filename", is(FILE_NAME)))
                .andExpect(
                        jsonPath("$.data.uploadOrder[0].value.DocumentLink.document_binary_url",
                                is(BINARY_URL)))
                .andExpect(jsonPath("$.errors", hasSize(0)))
                .andExpect(jsonPath("$.warnings", hasSize(0)));
    }

    @Test
    public void generateConsentOrderNotApproved400() throws Exception {
        mvc.perform(post(API_URL)
                .content("kwuilebge")
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void generateConsentOrderNotApproved500() throws Exception {
        when(documentService.generateConsentOrderNotApproved(eq(AUTH_TOKEN), isA(CaseDetails.class)))
                .thenThrow(feignError());

        mvc.perform(post(API_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void previewConsentOrderNotApprovedSuccess() throws Exception {
        when(documentService.previewConsentOrderNotApproved(eq(AUTH_TOKEN), isA(CaseDetails.class)))
                .thenReturn(caseDataWithPreviewOrder());

        mvc.perform(post(PREVIEW_API_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderRefusalPreviewDocument", is(notNullValue())))
                .andExpect(jsonPath("$.data.orderRefusalPreviewDocument.document_url", is(DOC_URL)))
                .andExpect(
                        jsonPath("$.data.orderRefusalPreviewDocument.document_filename", is(FILE_NAME)))
                .andExpect(
                        jsonPath("$.data.orderRefusalPreviewDocument.document_binary_url",
                                is(BINARY_URL)))
                .andExpect(jsonPath("$.errors", hasSize(0)))
                .andExpect(jsonPath("$.warnings", hasSize(0)));
    }

    @Test
    public void previewConsentOrderNotApproved400() throws Exception {
        mvc.perform(post(PREVIEW_API_URL)
                .content("kwuilebge")
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void previewConsentOrderNotApproved500() throws Exception {
        when(documentService.previewConsentOrderNotApproved(eq(AUTH_TOKEN), isA(CaseDetails.class)))
                .thenThrow(feignError());

        mvc.perform(post(PREVIEW_API_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isInternalServerError());
    }
}