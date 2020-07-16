package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import org.mockito.stubbing.OngoingStubbing;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;

@WebMvcTest(DraftOnlineDocumentController.class)
public class DraftOnlineDocumentControllerTest extends MiniFormAControllerTest {

    @Override
    public String endpoint() {
        return "/case-orchestration/documents/draft-contested-mini-form-a";
    }

    @Override
    public OngoingStubbing<CaseDocument> whenServiceGeneratesDocument() {
        return when(documentService.generateDraftContestedMiniFormA(eq(AUTH_TOKEN), isA(CaseDetails.class)));
    }

    @Override
    public void generateMiniFormA() throws Exception {
        whenServiceGeneratesDocument().thenReturn(caseDocument());

        mvc.perform(post(endpoint())
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.miniFormA.document_url", is(DOC_URL)))
            .andExpect(jsonPath("$.data.miniFormA.document_filename", is(FILE_NAME)))
            .andExpect(jsonPath("$.data.miniFormA.document_binary_url", is(BINARY_URL)))
            .andExpect(jsonPath("$.errors", is(emptyOrNullString())))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Override
    String jsonFixture() {
        return "/fixtures/contested/validate-hearing-with-fastTrackDecision.json";
    }
}