package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.datamigration.controller;

import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.BaseControllerTest;

import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNot.not;
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
    public void shouldMigrateCase_newport() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(resourceContentAsString("/fixtures/ccd-migrate-request-migration-applicable-newport.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.regionList", is("wales")))
            .andExpect(jsonPath("$.data.walesFRCList", is("newport")))
            .andExpect(jsonPath("$.data.newportCourtList", is("FR_newport_hc_list_1")))
            .andExpect(jsonPath("$.data", not(hasItem("regionListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("walesFRCListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("newportCourtListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtList"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtListGA"))))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_newport_ac() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(resourceContentAsString("/fixtures/ccd-migrate-request-migration-applicable-newport-ac.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.regionList", is("wales")))
            .andExpect(jsonPath("$.data.walesFRCList", is("newport")))
            .andExpect(jsonPath("$.data.newportCourtList", is("FR_newport_hc_list_1")))
            .andExpect(jsonPath("$.data", not(hasItem("regionListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("walesFRCListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("newportCourtListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtList"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtListGA"))))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_newport_ga() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(resourceContentAsString("/fixtures/ccd-migrate-request-migration-applicable-newport-ga.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.regionList", is("wales")))
            .andExpect(jsonPath("$.data.walesFRCList", is("newport")))
            .andExpect(jsonPath("$.data.newportCourtList", is("FR_newport_hc_list_1")))
            .andExpect(jsonPath("$.data", not(hasItem("regionListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("walesFRCListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("newportCourtListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtList"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtListGA"))))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_swansea() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(resourceContentAsString("/fixtures/ccd-migrate-request-migration-applicable-swansea.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.regionList", is("wales")))
            .andExpect(jsonPath("$.data.walesFRCList", is("swansea")))
            .andExpect(jsonPath("$.data.swanseaCourtList", is("FR_swansea_hc_list_1")))
            .andExpect(jsonPath("$.data", not(hasItem("regionListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("walesFRCListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("swanseaCourtListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtList"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtListGA"))))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_swansea_ac() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(resourceContentAsString("/fixtures/ccd-migrate-request-migration-applicable-swansea-ac.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.regionList", is("wales")))
            .andExpect(jsonPath("$.data.walesFRCList", is("swansea")))
            .andExpect(jsonPath("$.data.swanseaCourtList", is("FR_swansea_hc_list_6")))
            .andExpect(jsonPath("$.data", not(hasItem("regionListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("walesFRCListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("swanseaCourtListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtList"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtListGA"))))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_swansea_ga() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(resourceContentAsString("/fixtures/ccd-migrate-request-migration-applicable-swansea-ga.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.regionList", is("wales")))
            .andExpect(jsonPath("$.data.walesFRCList", is("swansea")))
            .andExpect(jsonPath("$.data.swanseaCourtList", is("FR_swansea_hc_list_6")))
            .andExpect(jsonPath("$.data", not(hasItem("regionListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("walesFRCListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("swanseaCourtListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtList"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtListGA"))))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_cfc() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(resourceContentAsString("/fixtures/ccd-migrate-request-migration-applicable-cfc.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.regionList", is("london")))
            .andExpect(jsonPath("$.data.londonFRCList", is("cfc")))
            .andExpect(jsonPath("$.data.cfcCourtList", is("FR_s_CFCList_9")))
            .andExpect(jsonPath("$.data", not(hasItem("regionListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("londonFRCListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("cfcCourtListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtList"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtListGA"))))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_cfc_ac() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(resourceContentAsString("/fixtures/ccd-migrate-request-migration-applicable-cfc-ac.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.regionList", is("london")))
            .andExpect(jsonPath("$.data.londonFRCList", is("cfc")))
            .andExpect(jsonPath("$.data.cfcCourtList", is("FR_s_CFCList_9")))
            .andExpect(jsonPath("$.data", not(hasItem("regionListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("londonFRCListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("cfcCourtListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtList"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtListGA"))))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_cfc_ga() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(resourceContentAsString("/fixtures/ccd-migrate-request-migration-applicable-cfc-ga.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.regionList", is("london")))
            .andExpect(jsonPath("$.data.londonFRCList", is("cfc")))
            .andExpect(jsonPath("$.data.cfcCourtList", is("FR_s_CFCList_9")))
            .andExpect(jsonPath("$.data", not(hasItem("regionListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("londonFRCListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("cfcCourtListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtList"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtListGA"))))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_nottingham() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(resourceContentAsString("/fixtures/ccd-migrate-request-migration-applicable-nottingham.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.regionList", is("midlands")))
            .andExpect(jsonPath("$.data.midlandsFRCList", is("nottingham")))
            .andExpect(jsonPath("$.data.nottinghamCourtList", is("FR_nottingham_hc_list_1")))
            .andExpect(jsonPath("$.data", not(hasItem("regionListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("midlandsFRCListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("nottinghamCourtListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtList"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtListGA"))))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_nottingham_ac() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(resourceContentAsString("/fixtures/ccd-migrate-request-migration-applicable-nottingham-ac.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.regionList", is("midlands")))
            .andExpect(jsonPath("$.data.midlandsFRCList", is("nottingham")))
            .andExpect(jsonPath("$.data.nottinghamCourtList", is("FR_nottingham_hc_list_1")))
            .andExpect(jsonPath("$.data", not(hasItem("regionListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("midlandsFRCListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("nottinghamCourtListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtList"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtListGA"))))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_nottingham_ga() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(resourceContentAsString("/fixtures/ccd-migrate-request-migration-applicable-nottingham-ga.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.regionList", is("midlands")))
            .andExpect(jsonPath("$.data.midlandsFRCList", is("nottingham")))
            .andExpect(jsonPath("$.data.nottinghamCourtList", is("FR_nottingham_hc_list_1")))
            .andExpect(jsonPath("$.data", not(hasItem("regionListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("midlandsFRCListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("nottinghamCourtListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtList"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtListGA"))))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_birmingham() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(resourceContentAsString("/fixtures/ccd-migrate-request-migration-applicable-birmingham.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.regionList", is("midlands")))
            .andExpect(jsonPath("$.data.midlandsFRCList", is("birmingham")))
            .andExpect(jsonPath("$.data.birminghamCourtList", is("FR_birmingham_hc_list_1")))
            .andExpect(jsonPath("$.data", not(hasItem("regionListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("midlandsFRCListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("birminghamCourtListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtList"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtListGA"))))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_birmingham_ac() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(resourceContentAsString("/fixtures/ccd-migrate-request-migration-applicable-birmingham-ac.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.regionList", is("midlands")))
            .andExpect(jsonPath("$.data.midlandsFRCList", is("birmingham")))
            .andExpect(jsonPath("$.data.birminghamCourtList", is("FR_birmingham_hc_list_1")))
            .andExpect(jsonPath("$.data", not(hasItem("regionListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("midlandsFRCListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("birminghamCourtListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtList"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtListGA"))))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_birmingham_ga() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(resourceContentAsString("/fixtures/ccd-migrate-request-migration-applicable-birmingham-ga.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.regionList", is("midlands")))
            .andExpect(jsonPath("$.data.midlandsFRCList", is("birmingham")))
            .andExpect(jsonPath("$.data.birminghamCourtList", is("FR_birmingham_hc_list_1")))
            .andExpect(jsonPath("$.data", not(hasItem("regionListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("midlandsFRCListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("birminghamCourtListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtList"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtListGA"))))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_liverpool() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(resourceContentAsString("/fixtures/ccd-migrate-request-migration-applicable-liverpool.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.regionList", is("northwest")))
            .andExpect(jsonPath("$.data.northWestFRCList", is("liverpool")))
            .andExpect(jsonPath("$.data.liverpoolCourtList", is("FR_liverpool_hc_list_1")))
            .andExpect(jsonPath("$.data", not(hasItem("regionListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("northWestFRCListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("liverpoolCourtListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtList"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtListGA"))))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_liverpool_ac() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(resourceContentAsString("/fixtures/ccd-migrate-request-migration-applicable-liverpool-ac.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.regionList", is("northwest")))
            .andExpect(jsonPath("$.data.northWestFRCList", is("liverpool")))
            .andExpect(jsonPath("$.data.liverpoolCourtList", is("FR_liverpool_hc_list_1")))
            .andExpect(jsonPath("$.data", not(hasItem("regionListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("northWestFRCListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("liverpoolCourtListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtList"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtListGA"))))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_liverpool_ga() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(resourceContentAsString("/fixtures/ccd-migrate-request-migration-applicable-liverpool-ga.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.regionList", is("northwest")))
            .andExpect(jsonPath("$.data.northWestFRCList", is("liverpool")))
            .andExpect(jsonPath("$.data.liverpoolCourtList", is("FR_liverpool_hc_list_1")))
            .andExpect(jsonPath("$.data", not(hasItem("regionListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("northWestFRCListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("liverpoolCourtListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtList"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtListGA"))))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_manchester() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(resourceContentAsString("/fixtures/ccd-migrate-request-migration-applicable-manchester.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.regionList", is("northwest")))
            .andExpect(jsonPath("$.data.northWestFRCList", is("manchester")))
            .andExpect(jsonPath("$.data.manchesterCourtList", is("FR_manchester_hc_list_1")))
            .andExpect(jsonPath("$.data", not(hasItem("regionListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("northWestFRCListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("manchesterCourtListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtList"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtListGA"))))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_manchester_ac() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(resourceContentAsString("/fixtures/ccd-migrate-request-migration-applicable-manchester-ac.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.regionList", is("northwest")))
            .andExpect(jsonPath("$.data.northWestFRCList", is("manchester")))
            .andExpect(jsonPath("$.data.manchesterCourtList", is("FR_manchester_hc_list_1")))
            .andExpect(jsonPath("$.data", not(hasItem("regionListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("northWestFRCListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("manchesterCourtListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtList"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtListGA"))))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_manchester_ga() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(resourceContentAsString("/fixtures/ccd-migrate-request-migration-applicable-manchester-ga.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.regionList", is("northwest")))
            .andExpect(jsonPath("$.data.northWestFRCList", is("manchester")))
            .andExpect(jsonPath("$.data.manchesterCourtList", is("FR_manchester_hc_list_1")))
            .andExpect(jsonPath("$.data", not(hasItem("regionListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("northWestFRCListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("manchesterCourtListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtList"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtListGA"))))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_cleaveland() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(resourceContentAsString("/fixtures/ccd-migrate-request-migration-applicable-cleaveland.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.regionList", is("northeast")))
            .andExpect(jsonPath("$.data.northEastFRCList", is("cleaveland")))
            .andExpect(jsonPath("$.data.cleavelandCourtList", is("FR_cleaveland_hc_list_1")))
            .andExpect(jsonPath("$.data", not(hasItem("regionListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("northEastFRCListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("cleavelandCourtListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtList"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtListGA"))))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_cleaveland_ac() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(resourceContentAsString("/fixtures/ccd-migrate-request-migration-applicable-cleaveland-ac.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.regionList", is("northeast")))
            .andExpect(jsonPath("$.data.northEastFRCList", is("cleaveland")))
            .andExpect(jsonPath("$.data.cleavelandCourtList", is("FR_cleaveland_hc_list_1")))
            .andExpect(jsonPath("$.data", not(hasItem("regionListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("northEastFRCListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("cleavelandCourtListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtList"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtListGA"))))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_nwyorkshire() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(resourceContentAsString("/fixtures/ccd-migrate-request-migration-applicable-nwyorkshire.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.regionList", is("northeast")))
            .andExpect(jsonPath("$.data.northEastFRCList", is("nwyorkshire")))
            .andExpect(jsonPath("$.data.nwyorkshireCourtList", is("FR_nwyorkshire_hc_list_1")))
            .andExpect(jsonPath("$.data", not(hasItem("regionListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("northEastFRCListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("nwyorkshireCourtListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtList"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtListGA"))))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_nwyorkshire_ac() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(resourceContentAsString("/fixtures/ccd-migrate-request-migration-applicable-nwyorkshire-ac.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.regionList", is("northeast")))
            .andExpect(jsonPath("$.data.northEastFRCList", is("nwyorkshire")))
            .andExpect(jsonPath("$.data.nwyorkshireCourtList", is("FR_nwyorkshire_hc_list_1")))
            .andExpect(jsonPath("$.data", not(hasItem("regionListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("northEastFRCListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("nwyorkshireCourtListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtList"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtListGA"))))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_nwyorkshire_ga() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(resourceContentAsString("/fixtures/ccd-migrate-request-migration-applicable-nwyorkshire-ga.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.regionList", is("northeast")))
            .andExpect(jsonPath("$.data.northEastFRCList", is("nwyorkshire")))
            .andExpect(jsonPath("$.data.nwyorkshireCourtList", is("FR_nwyorkshire_hc_list_1")))
            .andExpect(jsonPath("$.data", not(hasItem("regionListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("northEastFRCListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("nwyorkshireCourtListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtList"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtListGA"))))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_hsyorkshire() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(resourceContentAsString("/fixtures/ccd-migrate-request-migration-applicable-hsyorkshire.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.regionList", is("northeast")))
            .andExpect(jsonPath("$.data.northEastFRCList", is("hsyorkshire")))
            .andExpect(jsonPath("$.data.humberCourtList", is("FR_hsyorkshire_hc_list_1")))
            .andExpect(jsonPath("$.data", not(hasItem("regionListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("northEastFRCListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("humberCourtListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtList"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtListGA"))))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_hsyorkshire_ac() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(resourceContentAsString("/fixtures/ccd-migrate-request-migration-applicable-hsyorkshire-ac.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.regionList", is("northeast")))
            .andExpect(jsonPath("$.data.northEastFRCList", is("hsyorkshire")))
            .andExpect(jsonPath("$.data.humberCourtList", is("FR_hsyorkshire_hc_list_1")))
            .andExpect(jsonPath("$.data", not(hasItem("regionListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("northEastFRCListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("humberCourtListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtList"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtListGA"))))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_hsyorkshire_ga() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(resourceContentAsString("/fixtures/ccd-migrate-request-migration-applicable-hsyorkshire-ga.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.regionList", is("northeast")))
            .andExpect(jsonPath("$.data.northEastFRCList", is("hsyorkshire")))
            .andExpect(jsonPath("$.data.humberCourtList", is("FR_hsyorkshire_hc_list_1")))
            .andExpect(jsonPath("$.data", not(hasItem("regionListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("northEastFRCListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("humberCourtListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtList"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtListGA"))))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_kent() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(resourceContentAsString("/fixtures/ccd-migrate-request-migration-applicable-kent.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.regionList", is("southeast")))
            .andExpect(jsonPath("$.data.southEastFRCList", is("kentfrc")))
            .andExpect(jsonPath("$.data.kentSurreyCourtList", is("FR_kent_surrey_hc_list_1")))
            .andExpect(jsonPath("$.data", not(hasItem("regionListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("northEastFRCListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("kentSurreyCourtListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtList"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtListGA"))))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_kent_ac() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(resourceContentAsString("/fixtures/ccd-migrate-request-migration-applicable-kent-ac.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.regionList", is("southeast")))
            .andExpect(jsonPath("$.data.southEastFRCList", is("kentfrc")))
            .andExpect(jsonPath("$.data.kentSurreyCourtList", is("FR_kent_surrey_hc_list_1")))
            .andExpect(jsonPath("$.data", not(hasItem("regionListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("northEastFRCListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("kentSurreyCourtListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtList"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtListGA"))))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_kent_ga() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(resourceContentAsString("/fixtures/ccd-migrate-request-migration-applicable-kent-ga.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.regionList", is("southeast")))
            .andExpect(jsonPath("$.data.southEastFRCList", is("kentfrc")))
            .andExpect(jsonPath("$.data.kentSurreyCourtList", is("FR_kent_surrey_hc_list_1")))
            .andExpect(jsonPath("$.data", not(hasItem("regionListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("northEastFRCListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("kentSurreyCourtListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtList"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtListGA"))))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_invalidRegion() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(resourceContentAsString("/fixtures/ccd-migrate-request-migration-applicable-invalid-region.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data", not(hasItem("regionListSL"))))
            .andExpect(jsonPath("$.data.regionList", is("error")))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtList"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtListGA"))))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_invalidRegion_ac() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(resourceContentAsString("/fixtures/ccd-migrate-request-migration-applicable-invalid-region-ac.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data", not(hasItem("regionListSL"))))
            .andExpect(jsonPath("$.data.regionList", is("error")))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtList"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtListGA"))))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_invalidFrcWales() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(resourceContentAsString("/fixtures/ccd-migrate-request-migration-applicable-invalid-frc-wales.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data", not(hasItem("regionListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("walesFRCListSL"))))
            .andExpect(jsonPath("$.data.regionList", is("wales")))
            .andExpect(jsonPath("$.data.walesFRCList", is("birmingham")))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtList"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtListGA"))))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_invalidFrcWales_ac() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(resourceContentAsString("/fixtures/ccd-migrate-request-migration-applicable-invalid-frc-wales-ac.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data", not(hasItem("regionListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("walesFRCListSL"))))
            .andExpect(jsonPath("$.data.regionList", is("wales")))
            .andExpect(jsonPath("$.data.walesFRCList", is("birmingham")))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtList"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtListGA"))))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_invalidFrcMidlands() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(resourceContentAsString("/fixtures/ccd-migrate-request-migration-applicable-invalid-frc-midlands.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data", not(hasItem("regionListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("midlandsFRCListSL"))))
            .andExpect(jsonPath("$.data.regionList", is("midlands")))
            .andExpect(jsonPath("$.data.midlandsFRCList", is("london")))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtList"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtListGA"))))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_invalidFrcMidlands_ac() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(resourceContentAsString("/fixtures/ccd-migrate-request-migration-applicable-invalid-frc-midlands-ac.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data", not(hasItem("regionListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("midlandsFRCListSL"))))
            .andExpect(jsonPath("$.data.regionList", is("midlands")))
            .andExpect(jsonPath("$.data.midlandsFRCList", is("london")))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtList"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtListGA"))))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_invalidFrcLondon() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(resourceContentAsString("/fixtures/ccd-migrate-request-migration-applicable-invalid-frc-london.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data", not(hasItem("regionListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("londonFRCListSL"))))
            .andExpect(jsonPath("$.data.regionList", is("london")))
            .andExpect(jsonPath("$.data.londonFRCList", is("birmingham")))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtList"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtListGA"))))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_invalidFrcLondon_ac() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(resourceContentAsString("/fixtures/ccd-migrate-request-migration-applicable-invalid-frc-london-ac.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data", not(hasItem("regionListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("londonFRCListSL"))))
            .andExpect(jsonPath("$.data.regionList", is("london")))
            .andExpect(jsonPath("$.data.londonFRCList", is("birmingham")))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtList"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtListGA"))))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_invalidFrcNorthwest() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(resourceContentAsString("/fixtures/ccd-migrate-request-migration-applicable-invalid-frc-northwest.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data", not(hasItem("regionListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("northWestFRCListSL"))))
            .andExpect(jsonPath("$.data.regionList", is("northwest")))
            .andExpect(jsonPath("$.data.northWestFRCList", is("birmingham")))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtList"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtListGA"))))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_invalidFrcNorthwest_ac() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(resourceContentAsString("/fixtures/ccd-migrate-request-migration-applicable-invalid-frc-northwest-ac.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data", not(hasItem("regionListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("northWestFRCListSL"))))
            .andExpect(jsonPath("$.data.regionList", is("northwest")))
            .andExpect(jsonPath("$.data.northWestFRCList", is("birmingham")))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtList"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtListGA"))))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_invalidFrcNortheast() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(resourceContentAsString("/fixtures/ccd-migrate-request-migration-applicable-invalid-frc-northeast.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data", not(hasItem("regionListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("northEastFRCListSL"))))
            .andExpect(jsonPath("$.data.regionList", is("northeast")))
            .andExpect(jsonPath("$.data.northEastFRCList", is("birmingham")))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtList"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtListGA"))))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_invalidFrcNortheast_ac() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(resourceContentAsString("/fixtures/ccd-migrate-request-migration-applicable-invalid-frc-northeast-ac.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data", not(hasItem("regionListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("northEastFRCListSL"))))
            .andExpect(jsonPath("$.data.regionList", is("northeast")))
            .andExpect(jsonPath("$.data.northEastFRCList", is("birmingham")))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtList"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtListGA"))))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_invalidFrcSoutheast() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(resourceContentAsString("/fixtures/ccd-migrate-request-migration-applicable-invalid-frc-southeast.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data", not(hasItem("regionListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("southEastFRCListSL"))))
            .andExpect(jsonPath("$.data.regionList", is("southeast")))
            .andExpect(jsonPath("$.data.southEastFRCList", is("birmingham")))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtList"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtListGA"))))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_invalidFrcSoutheast_ac() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(resourceContentAsString("/fixtures/ccd-migrate-request-migration-applicable-invalid-frc-southeast-ac.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data", not(hasItem("regionListSL"))))
            .andExpect(jsonPath("$.data", not(hasItem("southEastFRCListSL"))))
            .andExpect(jsonPath("$.data.regionList", is("southeast")))
            .andExpect(jsonPath("$.data.southEastFRCList", is("birmingham")))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtList"))))
            .andExpect(jsonPath("$.data", not(hasItem("allocatedCourtListGA"))))
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

    @Test
    public void shouldNotMigrateCaseConsented() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(resourceContentAsString("/fixtures/ccd-migrate-request-consented.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data", is(emptyOrNullString())))
            .andExpect(jsonPath("$.errors", is(emptyOrNullString())))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldNotMigrateCaseInvalidCourtData() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(resourceContentAsString("/fixtures/ccd-migrate-request-invalid-court-data.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data", is(emptyOrNullString())))
            .andExpect(jsonPath("$.errors", is(emptyOrNullString())))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldNotMigrateCaseInvalidCourtDataGA() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(resourceContentAsString("/fixtures/ccd-migrate-request-invalid-court-data-ga.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data", is(emptyOrNullString())))
            .andExpect(jsonPath("$.errors", is(emptyOrNullString())))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldNotMigrateCaseNoCourtData() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(resourceContentAsString("/fixtures/ccd-migrate-request-no-court-data.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data", is(emptyOrNullString())))
            .andExpect(jsonPath("$.errors", is(emptyOrNullString())))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }
}
