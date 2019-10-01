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

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(CcdDataMigrationController.class)
@ContextConfiguration(classes = CaseOrchestrationApplication.class)
public class CcdDataMigrationControllerTest {
    private static final String MIGRATE_URL = "/ccd-data-migration/migrate";
    private static final String BEARER_TOKEN = "Bearer eyJhbGciOiJIUzI1NiJ9";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private WebApplicationContext applicationContext;

    private MockMvc mvc;
    private CallbackRequest ccdMigrationRequest;

    private CallbackRequest ccdMigrationRequestType1() throws IOException {
        final String migrateRequestJson = "/fixtures/ccd-migrate-request_1.json";
        try (final InputStream resourceAsStream = getClass().getResourceAsStream(migrateRequestJson)) {
            ccdMigrationRequest = objectMapper.readValue(resourceAsStream, CallbackRequest.class);
        }
        return ccdMigrationRequest;
    }

    private CallbackRequest ccdMigrationRequestType2() throws IOException {
        final String migrateRequestJson = "/fixtures/ccd-migrate-request_2.json";
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

    private CallbackRequest ccdRequestWithoutField() throws IOException {
        final String withoutFieldJson = "/fixtures/ccd-request-without-field.json";
        try (final InputStream resourceAsStream = getClass().getResourceAsStream(withoutFieldJson)) {
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
                            .content(objectMapper.writeValueAsString(ccdMigrationRequestType1()))
                            .header("Authorization", BEARER_TOKEN)
                            .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.data.judgeAllocated", contains("FR_judgeAllocatedList_3")))
                .andExpect(jsonPath("$.data.allocatedCourtList.region", is("midlands")))
                .andExpect(jsonPath("$.data.allocatedCourtList.midlandsList", is("nottingham")))
                .andExpect(jsonPath("$.data.allocatedCourtList.nottinghamCourtList",
                        is("FR_s_NottinghamList_8")))
                .andExpect(jsonPath("$.data.nottinghamCourtList", isEmptyOrNullString()))
                .andExpect(jsonPath("$.data.allocatedCourtListSL.region", is("midlands")))
                .andExpect(jsonPath("$.data.allocatedCourtListSL.midlandsList", is("nottingham")))
                .andExpect(jsonPath("$.data.allocatedCourtListSL.nottinghamCourtList",
                        is("FR_s_NottinghamList_1")))
                .andExpect(jsonPath("$.data.nottinghamCourtListSL", isEmptyOrNullString()))
                .andExpect(jsonPath("$.data.allocatedCourtListGA.region", is("midlands")))
                .andExpect(jsonPath("$.data.allocatedCourtListGA.midlandsList", is("nottingham")))
                .andExpect(jsonPath("$.data.allocatedCourtListGA.nottinghamCourtList",
                        is("FR_s_NottinghamList_2")))
                .andExpect(jsonPath("$.data.nottinghamCourtListGA", isEmptyOrNullString()))

                .andExpect(jsonPath("$.errors", isEmptyOrNullString()))
                .andExpect(jsonPath("$.warnings", isEmptyOrNullString()));
    }

    @Test
    public void shouldDoMigrationType2() throws Exception {
        doMigrateSetup();

        mvc.perform(post(MIGRATE_URL)
                            .content(objectMapper.writeValueAsString(ccdMigrationRequestType2()))
                            .header("Authorization", BEARER_TOKEN)
                            .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.data.judgeAllocated", contains("FR_judgeAllocatedList_3")))
                .andExpect(jsonPath("$.data.allocatedCourtList.region", is("london")))
                .andExpect(jsonPath("$.data.allocatedCourtList.londonList", is("cfc")))
                .andExpect(jsonPath("$.data.allocatedCourtList.cfcCourtList",
                        is("FR_s_CFCList_1")))
                .andExpect(jsonPath("$.data.cfcCourtList", isEmptyOrNullString()))

                .andExpect(jsonPath("$.data.allocatedCourtListSL.region", is("london")))
                .andExpect(jsonPath("$.data.allocatedCourtListSL.londonList", is("cfc")))
                .andExpect(jsonPath("$.data.allocatedCourtListSL.cfcCourtList",
                        is("FR_s_CFCList_2")))
                .andExpect(jsonPath("$.data.cfcCourtListSL", isEmptyOrNullString()))

                .andExpect(jsonPath("$.data.allocatedCourtListGA.region", is("london")))
                .andExpect(jsonPath("$.data.allocatedCourtListGA.londonList", is("cfc")))
                .andExpect(jsonPath("$.data.allocatedCourtListGA.cfcCourtList",
                        is("FR_s_CFCList_3")))
                .andExpect(jsonPath("$.data.cfcCourtListGA", isEmptyOrNullString()))

                .andExpect(jsonPath("$.errors", isEmptyOrNullString()))
                .andExpect(jsonPath("$.warnings", isEmptyOrNullString()));
    }

}