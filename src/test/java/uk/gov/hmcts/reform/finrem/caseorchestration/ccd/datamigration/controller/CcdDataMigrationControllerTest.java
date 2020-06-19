package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.datamigration.controller;

import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.BaseControllerTest;

import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@WebMvcTest(CcdDataMigrationController.class)
public class CcdDataMigrationControllerTest extends BaseControllerTest {

    private static final String MIGRATE_URL = "/ccd-data-migration/migrate";

    @Test
    public void shouldMigrateCase() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(resourceContentAsString("/fixtures/ccd-migrate-request-migration-applicable.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.natureOfApplication2", hasItem("Property Adjustment Order")))
            .andExpect(jsonPath("$.errors", is(emptyOrNullString())))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldNotMigrateCase() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(resourceContentAsString("/fixtures/ccd-migrate-request-already-migrated.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data", is(emptyOrNullString())))
            .andExpect(jsonPath("$.errors", is(emptyOrNullString())))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }
}
