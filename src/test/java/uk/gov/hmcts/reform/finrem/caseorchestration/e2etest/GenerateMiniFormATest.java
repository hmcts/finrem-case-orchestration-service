package uk.gov.hmcts.reform.finrem.caseorchestration.e2etest;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentRequest;

import java.util.ArrayList;
import java.util.Collections;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.caseDocument;

public class GenerateMiniFormATest extends AbstractDocumentTest {

    private static final String API_URL = "/case-orchestration/documents/generate-mini-form-a";

    @Override
    protected DocumentRequest documentRequest() {
        return DocumentRequest.builder()
                .template(documentConfiguration.getMiniFormTemplate())
                .fileName(documentConfiguration.getMiniFormFileName())
                .values(Collections.singletonMap("caseDetails", request.getCaseDetails()))
                .build();
    }

    @Override
    protected String apiUrl() {
        return API_URL;
    }

    @Test
    public void generateMiniFormA() throws Exception {
        generateDocumentServiceSuccessStub();

        webClient.perform(MockMvcRequestBuilders.post(apiUrl())
                .content(objectMapper.writeValueAsString(request))
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedCaseData()));
    }

    private String expectedCaseData() throws JsonProcessingException {
        return objectMapper.writeValueAsString(new CCDCallbackResponse(caseData().getCaseData(),
                new ArrayList<>(), new ArrayList<>()));
    }

    private CaseDetails caseData() {
        CaseDetails caseDetails = request.getCaseDetails();
        caseDetails.getCaseData().setMiniFormA(caseDocument());

        return caseDetails;
    }
}
