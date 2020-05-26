package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;

@WebMvcTest(FinalOrderController.class)
public class FinalOrderControllerTest extends BaseControllerTest {

    @MockBean
    private GenericDocumentService genericDocumentService;

    private static final String SEND_ORDER_ENDPOINT = "/case-orchestration/contested/send-order";

    private void doCaseDataSetUp() throws IOException, URISyntaxException {
        ObjectMapper objectMapper = new ObjectMapper();
        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/final-order-for-stamping.json").toURI()));
    }

    private void doCaseDataSetUpWithoutAnyHearingOrder() throws IOException, URISyntaxException {
        ObjectMapper objectMapper = new ObjectMapper();
        requestContent = objectMapper.readTree(new File(getClass()
              .getResource("/fixtures/final-order-for-stamping-without-hearing-order.json").toURI()));
    }

    private void doCaseDataSetUpWithoutAnyFinalOrder() throws IOException, URISyntaxException {
        ObjectMapper objectMapper = new ObjectMapper();
        requestContent = objectMapper.readTree(new File(getClass()
                  .getResource("/fixtures/final-order-for-stamping-without-existing-order.json").toURI()));
    }

    @Test
    public void finalOrder400Error() throws Exception {
        doEmptyCaseDataSetUp();

        mvc.perform(post(SEND_ORDER_ENDPOINT)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void finalOrderSuccess() throws Exception {
        doCaseDataSetUp();
        whenStampingDocument().thenReturn(caseDocument());

        ResultActions result = mvc.perform(post(SEND_ORDER_ENDPOINT)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE));

        result.andExpect(status().isOk());
        result.andDo(print());
        String path = "$.data.finalOrderCollection[1].value.uploadDraftDocument.";
        result.andExpect(jsonPath(path + "document_url", is(DOC_URL)))
            .andExpect(jsonPath(path + "document_filename", is(FILE_NAME)))
            .andExpect(jsonPath(path + "document_binary_url", is(BINARY_URL)));

    }

    @Test
    public void finalOrderSuccessWithoutAnyHearingOrder() throws Exception {
        doCaseDataSetUpWithoutAnyHearingOrder();
        whenStampingDocument().thenReturn(caseDocument());

        ResultActions result = mvc.perform(post(SEND_ORDER_ENDPOINT)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE));

        result.andExpect(status().isOk());
        result.andDo(print());
    }

    @Test
    public void finalOrderSuccessWithoutAnyFinalOrder() throws Exception {
        doCaseDataSetUpWithoutAnyFinalOrder();
        whenStampingDocument().thenReturn(caseDocument());

        ResultActions result = mvc.perform(post(SEND_ORDER_ENDPOINT)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE));

        result.andExpect(status().isOk());
        result.andDo(print());
        String path = "$.data.finalOrderCollection[0].value.uploadDraftDocument.";
        result.andExpect(jsonPath(path + "document_url", is(DOC_URL)))
            .andExpect(jsonPath(path + "document_filename", is(FILE_NAME)))
            .andExpect(jsonPath(path + "document_binary_url", is(BINARY_URL)));
    }

    private OngoingStubbing<CaseDocument> whenStampingDocument() {
        return when(genericDocumentService.stampDocument(isA(CaseDocument.class), anyString()));
    }
}
