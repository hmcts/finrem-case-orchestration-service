package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.datamigration.controller;

import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.BaseControllerTest;

import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNot.not;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
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
    public void shouldRemove_nottinghamCourtListGA_fromCase() throws Exception {
        String resourcePath = "/fixtures/migration/removeNottinghamCourtListGAMigration/ccd-migrate-remove-nottingham-court-list-ga.json";

        mvc.perform(post(MIGRATE_URL)
            .content(resourceContentAsString(resourcePath))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data", not(hasItem("nottinghamCourtListGA"))))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }
}
