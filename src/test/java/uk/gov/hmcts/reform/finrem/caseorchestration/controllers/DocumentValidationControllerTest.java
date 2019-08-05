package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentValidationResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.DocumentValidationService;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentValidationResponse.builder;


@RunWith(SpringRunner.class)
@WebMvcTest(DocumentValidationController.class)
public class DocumentValidationControllerTest extends BaseControllerTest {
    private static final String BEARER_TOKEN = "Bearer eyJhbGciOiJIUzI1NiJ9";
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentValidationService documentValidationService;

    @Test
    public void shouldReturnSuccessWhenFileUploadCheck() throws Exception {
        doRequestSetUp();
        DocumentValidationResponse response = builder()
                .mimeType("application/pdf").build();
        when(documentValidationService.validateDocument(any(CallbackRequest.class), anyString(), anyString()))
                .thenReturn(response);
        this.mockMvc.perform(MockMvcRequestBuilders
                .post("/case-orchestration/field/consentOrder/file-upload-check")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(javax.ws.rs.core.MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").doesNotExist());
    }

    @Test
    public void shouldReturnErrorsWhenFileUploadCheckIsFailed() throws Exception {
        doRequestSetUp();
        DocumentValidationResponse response = builder()
                .mimeType("application/json")
                .errors(singletonList("Invalid File Type"))
                .build();
        when(documentValidationService.validateDocument(any(CallbackRequest.class), anyString(), anyString()))
                .thenReturn(response);
        this.mockMvc.perform(MockMvcRequestBuilders
                .post("/case-orchestration/field/consentOrder/file-upload-check")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(javax.ws.rs.core.MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    public void shouldReturnSuccessWhenFileUploadCheckForInvalidField() throws Exception {
        doRequestSetUp();
        DocumentValidationResponse response = builder()
                .mimeType("application/pdf")
                .build();
        when(documentValidationService.validateDocument(any(CallbackRequest.class), anyString(), anyString()))
                .thenReturn(response);
        this.mockMvc.perform(MockMvcRequestBuilders
                .post("/case-orchestration/field/yyyyy/file-upload-check")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(javax.ws.rs.core.MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").doesNotExist());
    }

    private void doRequestSetUp() throws IOException, URISyntaxException {
        ObjectMapper objectMapper = new ObjectMapper();
        requestContent = objectMapper.readTree(new File(getClass()
                .getResource(jsonFixture()).toURI()));
    }

    private String jsonFixture() {
        return "/fixtures/latestConsentedConsentOrder/amend-consent-order-by-solicitor.json";
    }
}