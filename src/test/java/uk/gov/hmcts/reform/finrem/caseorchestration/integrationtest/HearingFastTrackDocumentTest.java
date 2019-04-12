package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentRequest;

import java.util.Collections;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.caseDocument;

public class HearingFastTrackDocumentTest extends AbstractDocumentTest {

    private static final String API_URL = "/case-orchestration/documents/hearing";

    @Override
    protected DocumentRequest documentRequest() {
        return DocumentRequest.builder()
                .template(documentConfiguration.getFormCFastTrackTemplate())
                .fileName(documentConfiguration.getFormCFileName())
                .values(Collections.singletonMap("caseDetails", request.getCaseDetails()))
                .build();
    }

    @Override
    protected String apiUrl() {
        return API_URL;
    }

    @Override
    protected String getTestFixture() {
        return "/fixtures/contested/validate-hearing-with-fastTrackDecision.json";
    }

    @Test
    public void generateFormC() throws Exception {
        generateDocumentServiceSuccessStub();

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
                .content(objectMapper.writeValueAsString(request))
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedCaseData()));
    }

    private String expectedCaseData() throws JsonProcessingException {
        CaseDetails caseDetails = request.getCaseDetails();
        caseDetails.getData().put("formC", caseDocument());

        return objectMapper.writeValueAsString(
                AboutToStartOrSubmitCallbackResponse.builder().data(caseDetails.getData()).build());
    }
}
