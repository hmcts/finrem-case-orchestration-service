package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.finrem.caseorchestration.CaseOrchestrationApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.DocumentService;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.feignError;

@RunWith(SpringRunner.class)
@WebMvcTest(DocumentController.class)
@ContextConfiguration(classes = CaseOrchestrationApplication.class)
public class DocumentControllerTest {

    private static final String AUTH_TOKEN = "Bearer eyJhbGJbpjciOiJIUzI1NiJ9";
    private static final String GEN_DOC_URL = "/case-orchestration/generate-mini-form-a";

    @Autowired
    private WebApplicationContext applicationContext;

    @MockBean
    private DocumentService documentService;

    private MockMvc mvc;
    private JsonNode requestContent;

    @Before
    public void setUp() throws Exception {
        mvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
        doRequestSetUp();
    }

    private void doRequestSetUp() throws IOException, URISyntaxException {
        ObjectMapper objectMapper = new ObjectMapper();
        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/fee-lookup.json").toURI()));
    }

    @Test
    public void generateMiniFormA() throws Exception {
        when(documentService.generateMiniFormA(eq(AUTH_TOKEN), isA(CaseDetails.class))).thenReturn(caseDocument());

        mvc.perform(post(GEN_DOC_URL)
                .content(requestContent.toString())
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.miniFormA.document_url", is(URL)))
                .andExpect(jsonPath("$.data.miniFormA.document_filename", is(FILE_NAME)))
                .andExpect(jsonPath("$.data.miniFormA.document_binary_url", is(BINARY_URL)))
                .andExpect(jsonPath("$.errors", hasSize(0)))
                .andExpect(jsonPath("$.warnings", hasSize(0)));
    }

    @Test
    public void generateMiniFormAHttpError400() throws Exception {
        mvc.perform(post(GEN_DOC_URL)
                .content("kwuilebge")
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void generateMiniFormAHttpError500() throws Exception {
        when(documentService.generateMiniFormA(eq(AUTH_TOKEN), isA(CaseDetails.class))).thenThrow(feignError());

        mvc.perform(post(GEN_DOC_URL)
                .content(requestContent.toString())
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }
}