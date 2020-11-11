package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UploadContestedCaseDocumentsService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@WebMvcTest(UploadContestedCaseDocumentController.class)
public class UploadContestedCaseDocumentControllerTest extends BaseControllerTest {

    @MockBean
    private UploadContestedCaseDocumentsService service;

    protected String endpoint() {
        return "/case-orchestration/upload-contested-case-documents";
    }

    private static final String CONTESTED_UPLOAD_DOCUMENTS_DATA
        = "/fixtures/contested/contested-upload-case-documents.json";

    @Test
    public void shouldFilterDocumentsByParty() throws Exception {
        mvc.perform(
            post(endpoint())
                .content(resourceContentAsString(CONTESTED_UPLOAD_DOCUMENTS_DATA))
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print());
        verify(service, times(1)).filterDocumentsToRelevantParty(any());
    }
}