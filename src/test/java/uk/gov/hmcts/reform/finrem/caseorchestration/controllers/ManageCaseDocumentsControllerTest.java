package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UploadCaseFilesAboutToSubmitHandler;

import java.io.File;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@WebMvcTest(ManageCaseDocumentsController.class)
public class ManageCaseDocumentsControllerTest extends BaseControllerTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    private JsonNode requestContent;

    private static final String MANAGE_CASE_DOCUMENTS = "/case-orchestration/manage-case-documents";
    private static final String MANAGE_CASE_DOCUMENTS_SUBMITTED = "/case-orchestration/manage-case-documents-submit";
    private static final String RESOURCE = "/fixtures/upload-case-files.json";
    private static final String CASE_DETAILS_KEY = "case_details";

    @Autowired
    private ManageCaseDocumentsController controller;

    @MockBean
    private UploadCaseFilesAboutToSubmitHandler handler;

    @Test
    public void controllerShouldFilterDocumentsByParty() throws Exception {

        requestContent = objectMapper.readTree(new File(getClass()
            .getResource(RESOURCE).toURI()));

        CaseDetails caseDetails = objectMapper.convertValue(requestContent.get(CASE_DETAILS_KEY), CaseDetails.class);

        mvc.perform(post(MANAGE_CASE_DOCUMENTS)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print());

        verify(handler).handle(caseDetails.getData());
    }

    @Test
    public void verifyRemoveDeletedFilesFromCaseDataCalledOnce() throws Exception {

        requestContent = objectMapper.readTree(new File(getClass()
            .getResource(RESOURCE).toURI()));

        CaseDetails caseDetails = objectMapper.convertValue(requestContent.get(CASE_DETAILS_KEY), CaseDetails.class);

        mvc.perform(post(MANAGE_CASE_DOCUMENTS_SUBMITTED)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.appChronologiesCollection").exists())
            .andExpect(jsonPath("$.data.applicantDocuments").exists())
            .andExpect(jsonPath("$.data.respondentDocuments").exists());

        verify(handler, times(1)).removeDeletedFilesFromCaseData(caseDetails.getData());
    }
}