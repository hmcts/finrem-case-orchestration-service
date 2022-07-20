package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest;

import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.newDocument;

public class RejectionOrderTest extends AbstractDocumentTest {

    private static final String API_URL = "/case-orchestration/documents/consent-order-not-approved";
    public static final String ORDER_TYPE = "generalOrder";

    @MockBean
    protected GenericDocumentService genericDocumentServiceMock;

    @Override
    protected String apiUrl() {
        return API_URL;
    }

    @Test
    public void generateConsentOrder() throws Exception {
        setUpMockContext();

        webClient.perform(MockMvcRequestBuilders.post(apiUrl())
            .content(objectMapper.writeValueAsString(newRequest))
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.uploadOrder[1].value.DocumentType", is(ORDER_TYPE)))
            .andExpect(jsonPath("$.data.uploadOrder[1].value.DocumentDateAdded", is(notNullValue())))
            .andExpect(jsonPath("$.data.uploadOrder[1].value.DocumentLink.document_url", is(DOC_URL)))
            .andExpect(jsonPath("$.data.uploadOrder[1].value.DocumentLink.document_filename", is(FILE_NAME)))
            .andExpect(jsonPath("$.data.uploadOrder[1].value.DocumentLink.document_binary_url", is(BINARY_URL)))
            .andExpect(jsonPath("$.errors", hasSize(0)))
            .andExpect(jsonPath("$.warnings", hasSize(0)));
    }

    private void setUpMockContext() {
        when(genericDocumentServiceMock.generateDocumentFromPlaceholdersMap(any(), any(),
            eq(documentConfiguration.getRejectedOrderTemplate()), eq(documentConfiguration.getRejectedOrderFileName())))
            .thenReturn(newDocument());
    }
}
