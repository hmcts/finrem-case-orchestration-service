package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentGenerationRequest;

import java.io.IOException;
import java.util.Collections;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

public class GenerateMiniFormATest extends AbstractDocumentTest {

    private static final String API_URL = "/case-orchestration/documents/generate-mini-form-a";

    @Override
    protected String apiUrl() {
        return API_URL;
    }

    @Test
    public void generateMiniFormA() throws Exception {
        generateDocumentServiceSuccessStub();
        idamServiceStub();
        generateDocument();
    }

    void generateDocument() throws Exception {
        webClient.perform(MockMvcRequestBuilders.post(apiUrl())
                .content(objectMapper.writeValueAsString(request))
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedCaseData()));
    }

    String expectedCaseData() throws JsonProcessingException {
        CaseDetails caseDetails = request.getCaseDetails();
        caseDetails.getData().put("miniFormA", caseDocument());

        return objectMapper.writeValueAsString(
                AboutToStartOrSubmitCallbackResponse.builder().data(caseDetails.getData()).build());
    }

    private CaseDocument caseDocument() {
        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setDocumentBinaryUrl(BINARY_URL);
        caseDocument.setDocumentFilename(FILE_NAME);
        caseDocument.setDocumentUrl(DOC_URL);
        return caseDocument;
    }

    @Override
    protected DocumentGenerationRequest documentRequest() {
        return DocumentGenerationRequest.builder()
                .template(documentConfiguration.getMiniFormTemplate())
                .fileName(documentConfiguration.getMiniFormFileName())
                .values(Collections.singletonMap("caseDetails", request.getCaseDetails()))
                .build();
    }

    protected CaseDetails copyWithOptionValueTranslation(CaseDetails caseDetails) {
        try {
            CaseDetails deepCopy = objectMapper
                    .readValue(objectMapper.writeValueAsString(caseDetails), CaseDetails.class);

            optionIdToValueTranslator.translateFixedListOptions(deepCopy);
            return deepCopy;
        } catch (IOException e) {
            throw new IllegalStateException();
        }
    }
}