package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.datamigration.controller;

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
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.CaseOrchestrationApplication;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@RunWith(SpringRunner.class)
@WebMvcTest(CcdDataMigrationController.class)
@ContextConfiguration(classes = CaseOrchestrationApplication.class)
public class CcdDataMigrationControllerTest {

    private static final String MIGRATE_URL = "/ccd-data-migration/migrate";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private WebApplicationContext applicationContext;

    private MockMvc mvc;
    private CallbackRequest ccdMigrationRequest;

    private CallbackRequest ccdMigrationRequestType() throws IOException {
        final String migrateRequestJson = "/fixtures/ccd-migrate-request_1.json";
        try (final InputStream resourceAsStream = getClass().getResourceAsStream(migrateRequestJson)) {
            ccdMigrationRequest = objectMapper.readValue(resourceAsStream, CallbackRequest.class);
        }
        return ccdMigrationRequest;
    }


    private CallbackRequest ccdAlreadyMigratedRequest() throws IOException {
        final String alreadyMigratedRequestJson = "/fixtures/ccd-already-migrated-request.json";
        try (final InputStream resourceAsStream = getClass().getResourceAsStream(alreadyMigratedRequestJson)) {
            ccdMigrationRequest = objectMapper.readValue(resourceAsStream, CallbackRequest.class);
        }
        return ccdMigrationRequest;
    }

    private void doMigrateSetup() {
        mvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
    }

    @Test
    public void shouldDoMigrationType1() throws Exception {
        doMigrateSetup();

        mvc.perform(post(MIGRATE_URL)
                            .content(objectMapper.writeValueAsString(ccdMigrationRequestType()))
                            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                            .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.data.applicantRepresented", is("Yes")))
                .andExpect(jsonPath("$.errors", isEmptyOrNullString()))
                .andExpect(jsonPath("$.warnings", isEmptyOrNullString()));
    }

    @Test
    public void shouldNotMigrate() throws Exception {
        doMigrateSetup();

        mvc.perform(post(MIGRATE_URL)
                            .content(objectMapper.writeValueAsString(ccdAlreadyMigratedRequest()))
                            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                            .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.data", isEmptyOrNullString()))
                .andExpect(jsonPath("$.errors", isEmptyOrNullString()))
                .andExpect(jsonPath("$.warnings", isEmptyOrNullString()));
    }
}