package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.datamigration.controller;

import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.mapJsonToObject;

@WebMvcTest(CcdDataMigrationController.class)
public class CcdDataMigrationControllerTest extends BaseControllerTest {

    private static final String MIGRATE_URL = "/ccd-data-migration/migrate";

    @Test
    public void shouldMigrateCase_newport() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(objectMapper.writeValueAsString(
                mapJsonToObject("/fixtures/ccd-migrate-request-migration-applicable-newport.json", CallbackRequest.class)))
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
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_newport_ac() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(objectMapper.writeValueAsString(
                mapJsonToObject("/fixtures/ccd-migrate-request-migration-applicable-newport-ac.json", CallbackRequest.class)))
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
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_swansea() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(objectMapper.writeValueAsString(
                mapJsonToObject("/fixtures/ccd-migrate-request-migration-applicable-swansea.json", CallbackRequest.class)))
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
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_swansea_ac() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(objectMapper.writeValueAsString(
                mapJsonToObject("/fixtures/ccd-migrate-request-migration-applicable-swansea-ac.json", CallbackRequest.class)))
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
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_cfc() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(objectMapper.writeValueAsString(
                mapJsonToObject("/fixtures/ccd-migrate-request-migration-applicable-cfc.json", CallbackRequest.class)))
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
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_cfc_ac() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(objectMapper.writeValueAsString(
                mapJsonToObject("/fixtures/ccd-migrate-request-migration-applicable-cfc-ac.json", CallbackRequest.class)))
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
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_nottingham() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(objectMapper.writeValueAsString(
                mapJsonToObject("/fixtures/ccd-migrate-request-migration-applicable-nottingham.json", CallbackRequest.class)))
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
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_nottingham_ac() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(objectMapper.writeValueAsString(
                mapJsonToObject("/fixtures/ccd-migrate-request-migration-applicable-nottingham-ac.json", CallbackRequest.class)))
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
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_birmingham() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(objectMapper.writeValueAsString(
                mapJsonToObject("/fixtures/ccd-migrate-request-migration-applicable-birmingham.json", CallbackRequest.class)))
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
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_birmingham_ac() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(objectMapper.writeValueAsString(
                mapJsonToObject("/fixtures/ccd-migrate-request-migration-applicable-birmingham-ac.json", CallbackRequest.class)))
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
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_liverpool() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(objectMapper.writeValueAsString(
                mapJsonToObject("/fixtures/ccd-migrate-request-migration-applicable-liverpool.json", CallbackRequest.class)))
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
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_liverpool_ac() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(objectMapper.writeValueAsString(
                mapJsonToObject("/fixtures/ccd-migrate-request-migration-applicable-liverpool-ac.json", CallbackRequest.class)))
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
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_manchester() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(objectMapper.writeValueAsString(
                mapJsonToObject("/fixtures/ccd-migrate-request-migration-applicable-manchester.json", CallbackRequest.class)))
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
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_manchester_ac() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(objectMapper.writeValueAsString(
                mapJsonToObject("/fixtures/ccd-migrate-request-migration-applicable-manchester-ac.json", CallbackRequest.class)))
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
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_cleaveland() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(objectMapper.writeValueAsString(
                mapJsonToObject("/fixtures/ccd-migrate-request-migration-applicable-cleaveland.json", CallbackRequest.class)))
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
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_cleaveland_ac() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(objectMapper.writeValueAsString(
                mapJsonToObject("/fixtures/ccd-migrate-request-migration-applicable-cleaveland-ac.json", CallbackRequest.class)))
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
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_nwyorkshire() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(objectMapper.writeValueAsString(
                mapJsonToObject("/fixtures/ccd-migrate-request-migration-applicable-nwyorkshire.json", CallbackRequest.class)))
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
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_nwyorkshire_ac() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(objectMapper.writeValueAsString(
                mapJsonToObject("/fixtures/ccd-migrate-request-migration-applicable-nwyorkshire-ac.json", CallbackRequest.class)))
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
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_hsyorkshire() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(objectMapper.writeValueAsString(
                mapJsonToObject("/fixtures/ccd-migrate-request-migration-applicable-hsyorkshire.json", CallbackRequest.class)))
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
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_hsyorkshire_ac() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(objectMapper.writeValueAsString(
                mapJsonToObject("/fixtures/ccd-migrate-request-migration-applicable-hsyorkshire-ac.json", CallbackRequest.class)))
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
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_kent() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(objectMapper.writeValueAsString(
                mapJsonToObject("/fixtures/ccd-migrate-request-migration-applicable-kent.json", CallbackRequest.class)))
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
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_kent_ac() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(objectMapper.writeValueAsString(
                mapJsonToObject("/fixtures/ccd-migrate-request-migration-applicable-kent-ac.json", CallbackRequest.class)))
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
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldNotMigrateCase() throws Exception {
        mvc.perform(post(MIGRATE_URL)
            .content(objectMapper.writeValueAsString(
                mapJsonToObject("/fixtures/ccd-migrate-request-already-migrated.json", CallbackRequest.class)))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data", is(emptyOrNullString())))
            .andExpect(jsonPath("$.errors", is(emptyOrNullString())))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }
}
