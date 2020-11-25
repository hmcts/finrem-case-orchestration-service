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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BEDFORD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BEDFORDSHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BEDFORDSHIRE_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BRISTOLFRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DEVON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DORSET;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LANCASHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LANCASHIRE_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHWALES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHWEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHWEST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NWOTHER_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.OXFORD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PRESTATYN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PRESTON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SALISBURY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SEOTHER_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHEAST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHEAST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHWEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SWOTHER_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.THAMESVALLEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.TRURO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WALES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WALES_OTHER_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WINCHESTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.LONDON_TEMP;

@WebMvcTest(CcdDataMigrationController.class)
public class CcdDataMigrationControllerTest extends BaseControllerTest {

    private static final String MIGRATE_FRC_URL = "/ccd-data-migration/migrateFrc";
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

    @Test
    public void shouldMigrateCase_nw_preston() throws Exception {
        mvc.perform(post(MIGRATE_FRC_URL)
            .content(resourceContentAsString("/fixtures/migration/rpet-164-frc-updates/migrate-request-nw-preston.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.regionList", is(NORTHWEST)))
            .andExpect(jsonPath("$.data.northWestFRCList", is(LANCASHIRE)))
            .andExpect(jsonPath("$.data.lancashireCourtList", is(PRESTON)))
            .andExpect(jsonPath("$.data", not(hasItem(NWOTHER_COURTLIST))))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_nw_burnley_to_temp() throws Exception {
        mvc.perform(post(MIGRATE_FRC_URL)
            .content(resourceContentAsString("/fixtures/migration/rpet-164-frc-updates/migrate-request-nw-burnley-to-temp.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.regionList", is(LONDON)))
            .andExpect(jsonPath("$.data.londonFRCList", is(LONDON)))
            .andExpect(jsonPath("$.data.londonCourtList", is(LONDON_TEMP)))
            .andExpect(jsonPath("$.data", not(hasItem(NORTHWEST_FRC_LIST))))
            .andExpect(jsonPath("$.data", not(hasItem(LANCASHIRE_COURTLIST))))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_se_bedford() throws Exception {
        mvc.perform(post(MIGRATE_FRC_URL)
            .content(resourceContentAsString("/fixtures/migration/rpet-164-frc-updates/migrate-request-se-bedford.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.regionList", is(SOUTHEAST)))
            .andExpect(jsonPath("$.data.southEastFRCList", is(BEDFORDSHIRE)))
            .andExpect(jsonPath("$.data.bedfordshireCourtList", is(BEDFORD)))
            .andExpect(jsonPath("$.data", not(hasItem(SEOTHER_COURTLIST))))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_se_oxford() throws Exception {
        mvc.perform(post(MIGRATE_FRC_URL)
            .content(resourceContentAsString("/fixtures/migration/rpet-164-frc-updates/migrate-request-se-oxford.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.regionList", is(SOUTHEAST)))
            .andExpect(jsonPath("$.data.southEastFRCList", is(THAMESVALLEY)))
            .andExpect(jsonPath("$.data.thamesvalleyCourtList", is(OXFORD)))
            .andExpect(jsonPath("$.data", not(hasItem(SEOTHER_COURTLIST))))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_se_basildon_to_temp() throws Exception {
        mvc.perform(post(MIGRATE_FRC_URL)
            .content(resourceContentAsString("/fixtures/migration/rpet-164-frc-updates/migrate-request-se-basildon-to-temp.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.regionList", is(LONDON)))
            .andExpect(jsonPath("$.data.londonFRCList", is(LONDON)))
            .andExpect(jsonPath("$.data.londonCourtList", is(LONDON_TEMP)))
            .andExpect(jsonPath("$.data", not(hasItem(SOUTHEAST_FRC_LIST))))
            .andExpect(jsonPath("$.data", not(hasItem(BEDFORDSHIRE_COURTLIST))))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_sw_winchester() throws Exception {
        mvc.perform(post(MIGRATE_FRC_URL)
            .content(resourceContentAsString("/fixtures/migration/rpet-164-frc-updates/migrate-request-sw-winchester.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.regionList", is(SOUTHWEST)))
            .andExpect(jsonPath("$.data.southWestFRCList", is(DORSET)))
            .andExpect(jsonPath("$.data.dorsetCourtList", is(WINCHESTER)))
            .andExpect(jsonPath("$.data", not(hasItem(SWOTHER_COURTLIST))))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_sw_truro() throws Exception {
        mvc.perform(post(MIGRATE_FRC_URL)
            .content(resourceContentAsString("/fixtures/migration/rpet-164-frc-updates/migrate-request-sw-truro.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.regionList", is(SOUTHWEST)))
            .andExpect(jsonPath("$.data.southWestFRCList", is(DEVON)))
            .andExpect(jsonPath("$.data.devonCourtList", is(TRURO)))
            .andExpect(jsonPath("$.data", not(hasItem(SWOTHER_COURTLIST))))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_sw_salisbury() throws Exception {
        mvc.perform(post(MIGRATE_FRC_URL)
            .content(resourceContentAsString("/fixtures/migration/rpet-164-frc-updates/migrate-request-sw-salisbury.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.regionList", is(SOUTHWEST)))
            .andExpect(jsonPath("$.data.southWestFRCList", is(BRISTOLFRC)))
            .andExpect(jsonPath("$.data.bristolCourtList", is(SALISBURY)))
            .andExpect(jsonPath("$.data", not(hasItem(SWOTHER_COURTLIST))))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void shouldMigrateCase_w_prestatyn() throws Exception {
        mvc.perform(post(MIGRATE_FRC_URL)
            .content(resourceContentAsString("/fixtures/migration/rpet-164-frc-updates/migrate-request-w-prestatyn.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.regionList", is(WALES)))
            .andExpect(jsonPath("$.data.walesFRCList", is(NORTHWALES)))
            .andExpect(jsonPath("$.data.northWalesCourtList", is(PRESTATYN)))
            .andExpect(jsonPath("$.data", not(hasItem(WALES_OTHER_COURTLIST))))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }
}
