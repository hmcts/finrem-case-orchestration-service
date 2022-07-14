package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.RefusalOrderDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.serialisation.FinremCallbackRequestDeserializer;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.UploadOrderDocumentType;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesRegex;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.feignError;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.finremCaseDataWithPreviewOrder;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.finremCaseDataWithUploadOrder;

@RunWith(SpringRunner.class)
@WebMvcTest(RejectedOrderDocumentController.class)
public class RejectedOrderDocumentControllerTest extends BaseControllerTest {

    private static final String API_URL = "/case-orchestration/documents/consent-order-not-approved";
    private static final String PREVIEW_API_URL = "/case-orchestration/documents/preview-consent-order-not-approved";

    private static final Pattern DATE_WITH_OPTIONAL_TIMEZONE_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}(\\+.{5})?$");

    @MockBean private RefusalOrderDocumentService documentService;

    @MockBean private FinremCallbackRequestDeserializer deserializer;

    private void doRequestSetUp() throws IOException, URISyntaxException {
        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/fee-lookup.json").toURI()));
    }

    @Test
    public void generateConsentOrderNotApprovedSuccess() throws Exception {
        doRequestSetUp();
        when(documentService.generateConsentOrderNotApproved(eq(AUTH_TOKEN), isA(FinremCaseDetails.class)))
                .thenReturn(finremCaseDataWithUploadOrder());
        when(deserializer.deserialize(any())).thenReturn(getCallbackRequest(requestContent.toString()));

        mvc.perform(post(API_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.uploadOrder[0].value.DocumentType", is(UploadOrderDocumentType.GENERAL_ORDER.getValue())))
                .andExpect(jsonPath("$.data.uploadOrder[0].value.DocumentDateAdded", matchesRegex(DATE_WITH_OPTIONAL_TIMEZONE_PATTERN)))
                .andExpect(jsonPath("$.data.uploadOrder[0].value.DocumentLink.document_url", is(DOC_URL)))
                .andExpect(jsonPath("$.data.uploadOrder[0].value.DocumentLink.document_filename", is(FILE_NAME)))
                .andExpect(jsonPath("$.data.uploadOrder[0].value.DocumentLink.document_binary_url", is(BINARY_URL)))
                .andExpect(jsonPath("$.errors", hasSize(0)))
                .andExpect(jsonPath("$.warnings", hasSize(0)));
    }

    @Test
    public void generateConsentOrderNotApproved400() throws Exception {
        when(deserializer.deserialize(any())).thenReturn(getCallbackRequestEmptyCaseData());

        mvc.perform(post(API_URL)
                .content("kwuilebge")
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void generateConsentOrderNotApproved500() throws Exception {
        doRequestSetUp();
        when(deserializer.deserialize(any())).thenReturn(getCallbackRequest());
        when(documentService.generateConsentOrderNotApproved(eq(AUTH_TOKEN), isA(FinremCaseDetails.class)))
                .thenThrow(feignError());

        mvc.perform(post(API_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void previewConsentOrderNotApprovedSuccess() throws Exception {
        doRequestSetUp();
        when(deserializer.deserialize(any())).thenReturn(getCallbackRequest(requestContent.toString()));
        when(documentService.previewConsentOrderNotApproved(eq(AUTH_TOKEN), isA(FinremCaseDetails.class)))
                .thenReturn(finremCaseDataWithPreviewOrder());

        mvc.perform(post(PREVIEW_API_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderRefusalPreviewDocument", is(notNullValue())))
                .andExpect(jsonPath("$.data.orderRefusalPreviewDocument.document_url", is(DOC_URL)))
                .andExpect(jsonPath("$.data.orderRefusalPreviewDocument.document_filename", is(FILE_NAME)))
                .andExpect(jsonPath("$.data.orderRefusalPreviewDocument.document_binary_url", is(BINARY_URL)))
                .andExpect(jsonPath("$.errors", hasSize(0)))
                .andExpect(jsonPath("$.warnings", hasSize(0)));
    }

    @Test
    public void previewConsentOrderNotApproved400() throws Exception {
        when(deserializer.deserialize(any())).thenReturn(getCallbackRequestEmptyCaseData());
        mvc.perform(post(PREVIEW_API_URL)
                .content("kwuilebge")
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void previewConsentOrderNotApproved500() throws Exception {
        doRequestSetUp();
        when(deserializer.deserialize(any())).thenReturn(getCallbackRequest());
        when(documentService.previewConsentOrderNotApproved(eq(AUTH_TOKEN), isA(FinremCaseDetails.class)))
                .thenThrow(feignError());

        mvc.perform(post(PREVIEW_API_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isInternalServerError());
    }
}
