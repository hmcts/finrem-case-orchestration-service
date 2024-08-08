package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentValidationResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.DocumentValidationService;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@WebMvcTest(DocumentValidationController.class)
public class DocumentValidationControllerTest extends BaseControllerTest {

    private static final String AMEND_CONSENT_ORDER_BY_SOL_JSON
        = "/fixtures/latestConsentedConsentOrder/amend-consent-order-by-solicitor.json";
    private static final String AMEND_CONTESTED_CONSENT_ORDER_BY_SOL_JSON
        = "/fixtures/latestConsentedConsentOrder/amend-consent-order-by-solicitor-contested.json";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentValidationService documentValidationService;

    @MockBean
    private ConsentedApplicationHelper helper;

    @Test
    public void whenCaseIsContestedShouldReturnSuccessWhenFileUploadCheckButNotToSetLabelField() throws Exception {
        doRequestSetUp(AMEND_CONTESTED_CONSENT_ORDER_BY_SOL_JSON);
        DocumentValidationResponse response = DocumentValidationResponse.builder()
            .mimeType("application/pdf").build();
        when(documentValidationService.validateDocument(any(CallbackRequest.class), anyString(), anyString()))
            .thenReturn(response);
        this.mockMvc.perform(MockMvcRequestBuilders
                .post("/case-orchestration/field/consentOrder/file-upload-check")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors").doesNotExist());
        verify(helper, never()).setConsentVariationOrderLabelField(anyMap());
    }

    @Test
    public void whenCaseIsConsentedShouldReturnSuccessWhenFileUploadCheck() throws Exception {
        doRequestSetUp(AMEND_CONSENT_ORDER_BY_SOL_JSON);
        DocumentValidationResponse response = DocumentValidationResponse.builder()
            .mimeType("application/pdf").build();
        when(documentValidationService.validateDocument(any(CallbackRequest.class), anyString(), anyString()))
            .thenReturn(response);
        this.mockMvc.perform(MockMvcRequestBuilders
                .post("/case-orchestration/field/consentOrder/file-upload-check")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors").doesNotExist());
        verify(helper).setConsentVariationOrderLabelField(anyMap());
    }

    @Test
    public void shouldReturnSuccessWhenFileUploadCheckForInvalidField() throws Exception {
        doRequestSetUp(AMEND_CONSENT_ORDER_BY_SOL_JSON);
        DocumentValidationResponse response = DocumentValidationResponse.builder()
            .mimeType("application/pdf")
            .build();
        when(documentValidationService.validateDocument(any(CallbackRequest.class), anyString(), anyString()))
            .thenReturn(response);
        this.mockMvc.perform(MockMvcRequestBuilders
                .post("/case-orchestration/field/yyyyy/file-upload-check")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors").doesNotExist());
    }

    private void doRequestSetUp(String path) throws IOException, URISyntaxException {
        ObjectMapper objectMapper = new ObjectMapper();
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource(path).toURI()));
    }
}