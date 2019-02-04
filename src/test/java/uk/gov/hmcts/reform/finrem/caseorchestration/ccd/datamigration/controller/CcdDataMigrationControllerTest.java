package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.datamigration.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.finrem.caseorchestration.CaseOrchestrationApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.datamigration.model.prod.CCDMigrationCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.datamigration.model.prod.CCDMigrationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.datamigration.model.prod.CaseDetails;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;

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

    @Autowired
    private WebApplicationContext applicationContext;

    private MockMvc mvc;
    private ObjectMapper objectMapper = new ObjectMapper();
    private CCDMigrationRequest request;

    @Before
    public void setUp() throws IOException {
        try (InputStream resourceAsStream = getClass().getResourceAsStream("/fixtures/ccd-migrate-request.json")) {
            request = objectMapper.readValue(resourceAsStream, CCDMigrationRequest.class);
        }
    }

    private String expectedCaseData() throws JsonProcessingException {
        CaseDetails caseDetails = request.getCaseDetails();
        String response = objectMapper.writeValueAsString(CCDMigrationCallbackResponse.builder()
                .data(caseDetails.getCaseData()).build());
        return response;
    }

    private void doMigrateSetup() {
        mvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
    }

    @Test
    public void shouldDoMigration() throws Exception {
        doMigrateSetup();

        mvc.perform(post(MIGRATE_URL)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedCaseData()))
                .andExpect(jsonPath("$.errors", isEmptyOrNullString()))
                .andExpect(jsonPath("$.warnings", isEmptyOrNullString()));
    }


}