package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DefaultsConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnlineFormDocumentService;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;

import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.feignError;

@WebMvcTest(MiniFormAController.class)
public class MiniFormAControllerTest extends BaseControllerTest {

    protected JsonNode requestContent;

    @MockitoBean
    protected OnlineFormDocumentService documentService;
    @MockitoBean
    protected IdamService idamService;
    @MockitoBean
    protected CaseDataService caseDataService;

    @MockitoBean
    protected DefaultsConfiguration defaultsConfiguration;

    protected String endpoint() {
        return "/case-orchestration/documents/generate-mini-form-a";
    }

    protected OngoingStubbing<CaseDocument> whenServiceGeneratesDocument() {
        return when(documentService.generateMiniFormA(eq(AUTH_TOKEN), isA(CaseDetails.class)));
    }

    protected OngoingStubbing<CaseDocument> whenServiceGeneratesConsentedInContestedMiniFormA() {
        return when(documentService.generateConsentedInContestedMiniFormA(isA(CaseDetails.class), eq(AUTH_TOKEN)));
    }

    private void doRequestSetUpConsented() throws IOException, URISyntaxException {
        ObjectMapper objectMapper = new ObjectMapper();
        requestContent = objectMapper.readTree(new File(getClass().getResource("/fixtures/fee-lookup.json").toURI()));
    }

    private void doRequestSetUpContested() throws IOException, URISyntaxException {
        ObjectMapper objectMapper = new ObjectMapper();
        requestContent = objectMapper.readTree(new File(getClass().getResource("/fixtures/mini-form-a-consent-in-contested.json").toURI()));
    }

    @Test
    public void generateMiniFormA() throws Exception {
        doRequestSetUpConsented();
        whenServiceGeneratesDocument().thenReturn(caseDocument());
        when(caseDataService.isConsentedApplication(any(CaseDetails.class))).thenReturn(true);

        mvc.perform(post(endpoint())
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.miniFormA.document_url", is(DOC_URL)))
            .andExpect(jsonPath("$.data.miniFormA.document_filename", is(FILE_NAME)))
            .andExpect(jsonPath("$.data.miniFormA.document_binary_url", is(BINARY_URL)))
            .andExpect(jsonPath("$.data.assignedToJudge", is(defaultsConfiguration.getAssignedToJudgeDefault())))
            .andExpect(jsonPath("$.data.assignedToJudgeReason", is(MiniFormAController.ASSIGNED_TO_JUDGE_REASON_DEFAULT)))
            .andExpect(jsonPath("$.data.referToJudgeText", is(MiniFormAController.REFER_TO_JUDGE_TEXT_DEFAULT)))
            .andExpect(jsonPath("$.data.referToJudgeDate", is(LocalDate.now().toString())))
            .andExpect(jsonPath("$.errors", is(emptyOrNullString())))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void generateMiniFormAHttpError400() throws Exception {
        doRequestSetUpConsented();
        mvc.perform(post(endpoint())
                .content("kwuilebge")
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void generateMiniFormAHttpError500() throws Exception {
        doRequestSetUpConsented();
        whenServiceGeneratesDocument().thenThrow(feignError());

        mvc.perform(post(endpoint())
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isInternalServerError());
    }

    @Test
    public void generateMiniFormAWhenConsentedInContested() throws Exception {
        doRequestSetUpContested();
        whenServiceGeneratesConsentedInContestedMiniFormA().thenReturn(caseDocument());
        when(caseDataService.isConsentedInContestedCase(any(CaseDetails.class))).thenReturn(true);

        mvc.perform(post(endpoint())
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.consentMiniFormA.document_url", is(DOC_URL)))
            .andExpect(jsonPath("$.data.consentMiniFormA.document_filename", is(FILE_NAME)))
            .andExpect(jsonPath("$.data.consentMiniFormA.document_binary_url", is(BINARY_URL)));
    }

    @Test
    public void generateMiniFormAWhenConsentedInContestedExpectContestedFieldToBePopulated() throws Exception {
        doRequestSetUpContested();
        whenServiceGeneratesConsentedInContestedMiniFormA().thenReturn(caseDocument());
        when(caseDataService.isConsentedInContestedCase(any(CaseDetails.class))).thenReturn(false);

        mvc.perform(post(endpoint())
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.assignedToJudge", is(defaultsConfiguration.getAssignedToJudgeDefault())));
    }
}
