package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderApprovedDocumentService;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.caseDocument;

@WebMvcTest(FinalOrderController.class)
public class FinalOrderControllerTest extends BaseControllerTest {

    @MockBean
    private ConsentOrderApprovedDocumentService service;

    public String endpoint() {
        return "/case-orchestration/contested/send-order";
    }

    void doCaseDataSetUp() throws IOException, URISyntaxException {
        ObjectMapper objectMapper = new ObjectMapper();
        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/final-order-for-stamping.json").toURI()));
    }

    @Test
    public void finalOrder400Error() throws Exception {
        doEmtpyCaseDataSetUp();

        mvc.perform(post(endpoint())
                .content(requestContent.toString())
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void finalOrderSuccess() throws Exception {
        doCaseDataSetUp();
        whenStampingDocument().thenReturn(caseDocument());

        ResultActions result = mvc.perform(post(endpoint())
                .content(requestContent.toString())
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk());
        assertResult(result);

    }

    private OngoingStubbing<CaseDocument> whenStampingDocument() {
        return when(service.stampDocument(isA(CaseDocument.class), anyString()));
    }

    private void assertResult(ResultActions result) throws Exception {
        String path = "$.data.finalOrder.";
        result.andExpect(jsonPath(path + "document_url", is(DOC_URL)))
                .andExpect(jsonPath(path + "document_filename", is(FILE_NAME)))
                .andExpect(jsonPath(path + "document_binary_url", is(BINARY_URL)));
    }


}
