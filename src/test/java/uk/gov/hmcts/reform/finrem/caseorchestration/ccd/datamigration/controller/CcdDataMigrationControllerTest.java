package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.datamigration.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.CaseOrchestrationApplication;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@RunWith(SpringRunner.class)
@WebMvcTest(CcdDataMigrationController.class)
@ContextConfiguration(classes = CaseOrchestrationApplication.class)
public class CcdDataMigrationControllerTest {
    private static final String MIGRATE_URL = "/ccd-data-migration/migrate";
    private static final String BEARER_TOKEN = "Bearer eyJhbGciOiJIUzI1NiJ9";
    private static ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private WebApplicationContext applicationContext;

    private MockMvc mvc;
    private CallbackRequest ccdMigrationRequest;

    private CallbackRequest ccdMigrationRequest() throws IOException {
        String migrateRequestJson = "/fixtures/ccd-migrate-request.json";
        try (InputStream resourceAsStream = getClass().getResourceAsStream(migrateRequestJson)) {
            ccdMigrationRequest =  objectMapper.readValue(resourceAsStream, CallbackRequest.class);
        }
        return ccdMigrationRequest;
    }

    private CallbackRequest ccdAlreadyMigratedRequest() throws IOException {
        String alreadyMigratedRequestJson = "/fixtures/ccd-already-migrated-request.json";
        try (InputStream resourceAsStream = getClass().getResourceAsStream(alreadyMigratedRequestJson)) {
            return objectMapper.readValue(resourceAsStream, CallbackRequest.class);
        }
    }

    private String expectedCaseData() throws JsonProcessingException {
        CaseDetails caseDetails = ccdMigrationRequest.getCaseDetails();
        return objectMapper.writeValueAsString(AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDetails.getData()).build());
    }

    private void doMigrateSetup() {
        mvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
    }

    @Test
    public void shouldDoMigration() throws Exception {
        doMigrateSetup();

        mvc.perform(post(MIGRATE_URL)
                .content(objectMapper.writeValueAsString(ccdMigrationRequest()))
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedCaseData()))
                .andExpect(jsonPath("$.data.amountToPay", is("5000")))
                .andExpect(jsonPath("$.errors", isEmptyOrNullString()))
                .andExpect(jsonPath("$.warnings", isEmptyOrNullString()));
    }

    @Test
    public void shouldNotDoMigration() throws Exception {
        doMigrateSetup();

        mvc.perform(post(MIGRATE_URL)
                .content(objectMapper.writeValueAsString(ccdAlreadyMigratedRequest()))
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("{}"))
                .andExpect(jsonPath("$.data", isEmptyOrNullString()))
                .andExpect(jsonPath("$.errors", isEmptyOrNullString()))
                .andExpect(jsonPath("$.warnings", isEmptyOrNullString()));
    }


}