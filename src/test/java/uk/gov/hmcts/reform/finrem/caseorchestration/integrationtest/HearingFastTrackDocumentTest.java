package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.newDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService.DATE_BETWEEN_6_AND_10_WEEKS;

public class HearingFastTrackDocumentTest extends AbstractDocumentTest {

    private static final String API_URL = "/case-orchestration/documents/hearing";
    private static final String GENERATE_BULK_PRINT_CONTEXT_PATH = "/version/1/bulk-print";

    @MockBean
    protected GenericDocumentService genericDocumentServiceMock;

    @Captor
    ArgumentCaptor<Map<String, Object>> placeholdersMapCaptor;

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
        setUpMockContext();

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .content(objectMapper.writeValueAsString(newRequest))
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().json(getCaseDataAsString()));

        verifyDocumentServiceInteraction();
    }

    protected void setUpMockContext() {
        when(genericDocumentServiceMock.generateDocumentFromPlaceholdersMap(any(), any(),
            eq(documentConfiguration.getFormCFastTrackTemplate()),
            eq(documentConfiguration.getFormCFileName()))).thenReturn(newDocument());
    }

    protected void verifyDocumentServiceInteraction() {
        verify(genericDocumentServiceMock, times(1))
            .generateDocumentFromPlaceholdersMap(any(), placeholdersMapCaptor.capture(),
                eq(documentConfiguration.getFormCFastTrackTemplate()),  eq(documentConfiguration.getFormCFileName()));
    }

    private Map<String, Object> expectedCaseData() {
        FinremCaseDetails caseDetails = newRequest.getCaseDetails();
        caseDetails.getCaseData().setFormC(newDocument());

        return objectMapper.convertValue(
            uk.gov.hmcts.reform.finrem.ccd.callback.AboutToStartOrSubmitCallbackResponse.builder()
                .warnings(ImmutableList.of(DATE_BETWEEN_6_AND_10_WEEKS))
                .data(caseDetails.getCaseData()).build(),
            TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, Object.class));
    }

    private String getCaseDataAsString() throws JsonProcessingException {
        return objectMapper.writeValueAsString(expectedCaseData());
    }
}
