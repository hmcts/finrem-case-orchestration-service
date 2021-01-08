package uk.gov.hmcts.reform.finrem.caseorchestration.service.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;

import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class CourtDetailsMigrationTest extends BaseServiceTest {

    @Autowired private ObjectMapper mapper;

    @Test
    public void shouldMigrateCase_newport() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-newport.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertThat(migratedCaseData.get("regionList"), is("wales"));
        assertThat(migratedCaseData.get("walesFRCList"), is("newport"));
        assertThat(migratedCaseData.get("newportCourtList"), is("FR_newport_hc_list_1"));
        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("walesFRCListSL"));
        assertNull(migratedCaseData.get("newportCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    public void shouldMigrateCase_newport_ac() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-newport-ac.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertThat(migratedCaseData.get("regionList"), is("wales"));
        assertThat(migratedCaseData.get("walesFRCList"), is("newport"));
        assertThat(migratedCaseData.get("newportCourtList"), is("FR_newport_hc_list_1"));
        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("walesFRCListSL"));
        assertNull(migratedCaseData.get("newportCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    public void shouldMigrateCase_newport_ga() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-newport-ga.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertThat(migratedCaseData.get("regionList"), is("wales"));
        assertThat(migratedCaseData.get("walesFRCList"), is("newport"));
        assertThat(migratedCaseData.get("newportCourtList"), is("FR_newport_hc_list_1"));
        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("walesFRCListSL"));
        assertNull(migratedCaseData.get("newportCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    public void shouldMigrateCase_swansea() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-swansea.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertThat(migratedCaseData.get("regionList"), is("wales"));
        assertThat(migratedCaseData.get("walesFRCList"), is("swansea"));
        assertThat(migratedCaseData.get("swanseaCourtList"), is("FR_swansea_hc_list_1"));
        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("walesFRCListSL"));
        assertNull(migratedCaseData.get("swanseaCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    public void shouldMigrateCase_swansea_ac() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-swansea-ac.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertThat(migratedCaseData.get("regionList"), is("wales"));
        assertThat(migratedCaseData.get("walesFRCList"), is("swansea"));
        assertThat(migratedCaseData.get("swanseaCourtList"), is("FR_swansea_hc_list_6"));
        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("walesFRCListSL"));
        assertNull(migratedCaseData.get("swanseaCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    public void shouldMigrateCase_swansea_ga() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-swansea-ga.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertThat(migratedCaseData.get("regionList"), is("wales"));
        assertThat(migratedCaseData.get("walesFRCList"), is("swansea"));
        assertThat(migratedCaseData.get("swanseaCourtList"), is("FR_swansea_hc_list_6"));
        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("walesFRCListSL"));
        assertNull(migratedCaseData.get("swanseaCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    public void shouldMigrateCase_cfc() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-cfc.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertThat(migratedCaseData.get("regionList"), is("london"));
        assertThat(migratedCaseData.get("londonFRCList"), is("cfc"));
        assertThat(migratedCaseData.get("cfcCourtList"), is("FR_s_CFCList_9"));
        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("londonFRCListSL"));
        assertNull(migratedCaseData.get("cfcCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    public void shouldMigrateCase_cfc_ac() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-cfc-ac.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertThat(migratedCaseData.get("regionList"), is("london"));
        assertThat(migratedCaseData.get("londonFRCList"), is("cfc"));
        assertThat(migratedCaseData.get("cfcCourtList"), is("FR_s_CFCList_9"));
        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("londonFRCListSL"));
        assertNull(migratedCaseData.get("cfcCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    public void shouldMigrateCase_cfc_ga() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-cfc-ac.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertThat(migratedCaseData.get("regionList"), is("london"));
        assertThat(migratedCaseData.get("londonFRCList"), is("cfc"));
        assertThat(migratedCaseData.get("cfcCourtList"), is("FR_s_CFCList_9"));
        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("londonFRCListSL"));
        assertNull(migratedCaseData.get("cfcCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    public void shouldMigrateCase_nottingham() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-nottingham.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertThat(migratedCaseData.get("regionList"), is("midlands"));
        assertThat(migratedCaseData.get("midlandsFRCList"), is("nottingham"));
        assertThat(migratedCaseData.get("nottinghamCourtList"), is("FR_nottingham_hc_list_1"));
        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("midlandsFRCListSL"));
        assertNull(migratedCaseData.get("nottinghamCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    public void shouldMigrateCase_nottingham_ac() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-nottingham-ac.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertThat(migratedCaseData.get("regionList"), is("midlands"));
        assertThat(migratedCaseData.get("midlandsFRCList"), is("nottingham"));
        assertThat(migratedCaseData.get("nottinghamCourtList"), is("FR_nottingham_hc_list_1"));
        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("midlandsFRCListSL"));
        assertNull(migratedCaseData.get("nottinghamCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    public void shouldMigrateCase_nottingham_ga() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-nottingham-ga.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertThat(migratedCaseData.get("regionList"), is("midlands"));
        assertThat(migratedCaseData.get("midlandsFRCList"), is("nottingham"));
        assertThat(migratedCaseData.get("nottinghamCourtList"), is("FR_nottingham_hc_list_1"));
        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("midlandsFRCListSL"));
        assertNull(migratedCaseData.get("nottinghamCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    public void shouldMigrateCase_birmingham() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-birmingham.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertThat(migratedCaseData.get("regionList"), is("midlands"));
        assertThat(migratedCaseData.get("midlandsFRCList"), is("birmingham"));
        assertThat(migratedCaseData.get("birminghamCourtList"), is("FR_birmingham_hc_list_1"));
        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("midlandsFRCListSL"));
        assertNull(migratedCaseData.get("birminghamCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    public void shouldMigrateCase_birmingham_ac() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-birmingham-ac.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertThat(migratedCaseData.get("regionList"), is("midlands"));
        assertThat(migratedCaseData.get("midlandsFRCList"), is("birmingham"));
        assertThat(migratedCaseData.get("birminghamCourtList"), is("FR_birmingham_hc_list_1"));
        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("midlandsFRCListSL"));
        assertNull(migratedCaseData.get("birminghamCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    public void shouldMigrateCase_birmingham_ga() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-birmingham-ga.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertThat(migratedCaseData.get("regionList"), is("midlands"));
        assertThat(migratedCaseData.get("midlandsFRCList"), is("birmingham"));
        assertThat(migratedCaseData.get("birminghamCourtList"), is("FR_birmingham_hc_list_1"));
        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("midlandsFRCListSL"));
        assertNull(migratedCaseData.get("birminghamCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    public void shouldMigrateCase_liverpool() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-liverpool.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertThat(migratedCaseData.get("regionList"), is("northwest"));
        assertThat(migratedCaseData.get("northWestFRCList"), is("liverpool"));
        assertThat(migratedCaseData.get("liverpoolCourtList"), is("FR_liverpool_hc_list_1"));
        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("northWestFRCListSL"));
        assertNull(migratedCaseData.get("liverpoolCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    public void shouldMigrateCase_liverpool_ac() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-liverpool-ac.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertThat(migratedCaseData.get("regionList"), is("northwest"));
        assertThat(migratedCaseData.get("northWestFRCList"), is("liverpool"));
        assertThat(migratedCaseData.get("liverpoolCourtList"), is("FR_liverpool_hc_list_1"));
        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("northWestFRCListSL"));
        assertNull(migratedCaseData.get("liverpoolCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    public void shouldMigrateCase_liverpool_ga() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-liverpool-ga.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertThat(migratedCaseData.get("regionList"), is("northwest"));
        assertThat(migratedCaseData.get("northWestFRCList"), is("liverpool"));
        assertThat(migratedCaseData.get("liverpoolCourtList"), is("FR_liverpool_hc_list_1"));
        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("northWestFRCListSL"));
        assertNull(migratedCaseData.get("liverpoolCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    public void shouldMigrateCase_manchester() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-manchester.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertThat(migratedCaseData.get("regionList"), is("northwest"));
        assertThat(migratedCaseData.get("northWestFRCList"), is("manchester"));
        assertThat(migratedCaseData.get("manchesterCourtList"), is("FR_manchester_hc_list_1"));
        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("northWestFRCListSL"));
        assertNull(migratedCaseData.get("manchesterCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    public void shouldMigrateCase_manchester_ac() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-manchester-ac.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertThat(migratedCaseData.get("regionList"), is("northwest"));
        assertThat(migratedCaseData.get("northWestFRCList"), is("manchester"));
        assertThat(migratedCaseData.get("manchesterCourtList"), is("FR_manchester_hc_list_1"));
        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("northWestFRCListSL"));
        assertNull(migratedCaseData.get("manchesterCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    public void shouldMigrateCase_manchester_ga() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-manchester-ga.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertThat(migratedCaseData.get("regionList"), is("northwest"));
        assertThat(migratedCaseData.get("northWestFRCList"), is("manchester"));
        assertThat(migratedCaseData.get("manchesterCourtList"), is("FR_manchester_hc_list_1"));
        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("northWestFRCListSL"));
        assertNull(migratedCaseData.get("manchesterCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    public void shouldMigrateCase_cleaveland() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-cleaveland.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertThat(migratedCaseData.get("regionList"), is("northeast"));
        assertThat(migratedCaseData.get("northEastFRCList"), is("cleaveland"));
        assertThat(migratedCaseData.get("cleavelandCourtList"), is("FR_cleaveland_hc_list_1"));
        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("northEastFRCListSL"));
        assertNull(migratedCaseData.get("cleavelandCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    public void shouldMigrateCase_cleaveland_ac() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-cleaveland-ac.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertThat(migratedCaseData.get("regionList"), is("northeast"));
        assertThat(migratedCaseData.get("northEastFRCList"), is("cleaveland"));
        assertThat(migratedCaseData.get("cleavelandCourtList"), is("FR_cleaveland_hc_list_1"));
        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("northEastFRCListSL"));
        assertNull(migratedCaseData.get("cleavelandCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    public void shouldMigrateCase_nwyorkshire() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-nwyorkshire.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertThat(migratedCaseData.get("regionList"), is("northeast"));
        assertThat(migratedCaseData.get("northEastFRCList"), is("nwyorkshire"));
        assertThat(migratedCaseData.get("nwyorkshireCourtList"), is("FR_nwyorkshire_hc_list_1"));
        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("northEastFRCListSL"));
        assertNull(migratedCaseData.get("nwyorkshireCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    public void shouldMigrateCase_nwyorkshire_ac() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-nwyorkshire-ac.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertThat(migratedCaseData.get("regionList"), is("northeast"));
        assertThat(migratedCaseData.get("northEastFRCList"), is("nwyorkshire"));
        assertThat(migratedCaseData.get("nwyorkshireCourtList"), is("FR_nwyorkshire_hc_list_1"));
        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("northEastFRCListSL"));
        assertNull(migratedCaseData.get("nwyorkshireCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    public void shouldMigrateCase_nwyorkshire_ga() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-nwyorkshire-ga.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertThat(migratedCaseData.get("regionList"), is("northeast"));
        assertThat(migratedCaseData.get("northEastFRCList"), is("nwyorkshire"));
        assertThat(migratedCaseData.get("nwyorkshireCourtList"), is("FR_nwyorkshire_hc_list_1"));
        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("northEastFRCListSL"));
        assertNull(migratedCaseData.get("nwyorkshireCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    public void shouldMigrateCase_hsyorkshire() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-hsyorkshire.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertThat(migratedCaseData.get("regionList"), is("northeast"));
        assertThat(migratedCaseData.get("northEastFRCList"), is("hsyorkshire"));
        assertThat(migratedCaseData.get("humberCourtList"), is("FR_hsyorkshire_hc_list_1"));
        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("northEastFRCListSL"));
        assertNull(migratedCaseData.get("humberCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    public void shouldMigrateCase_hsyorkshire_ac() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-hsyorkshire-ac.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertThat(migratedCaseData.get("regionList"), is("northeast"));
        assertThat(migratedCaseData.get("northEastFRCList"), is("hsyorkshire"));
        assertThat(migratedCaseData.get("humberCourtList"), is("FR_hsyorkshire_hc_list_1"));
        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("northEastFRCListSL"));
        assertNull(migratedCaseData.get("humberCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    public void shouldMigrateCase_hsyorkshire_ga() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-hsyorkshire-ga.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertThat(migratedCaseData.get("regionList"), is("northeast"));
        assertThat(migratedCaseData.get("northEastFRCList"), is("hsyorkshire"));
        assertThat(migratedCaseData.get("humberCourtList"), is("FR_hsyorkshire_hc_list_1"));
        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("northEastFRCListSL"));
        assertNull(migratedCaseData.get("humberCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    public void shouldMigrateCase_kent() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-kent.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertThat(migratedCaseData.get("regionList"), is("southeast"));
        assertThat(migratedCaseData.get("southEastFRCList"), is("kentfrc"));
        assertThat(migratedCaseData.get("kentSurreyCourtList"), is("FR_kent_surrey_hc_list_1"));
        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("northEastFRCListSL"));
        assertNull(migratedCaseData.get("kentSurreyCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    public void shouldMigrateCase_kent_ac() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-kent-ac.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertThat(migratedCaseData.get("regionList"), is("southeast"));
        assertThat(migratedCaseData.get("southEastFRCList"), is("kentfrc"));
        assertThat(migratedCaseData.get("kentSurreyCourtList"), is("FR_kent_surrey_hc_list_1"));
        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("northEastFRCListSL"));
        assertNull(migratedCaseData.get("kentSurreyCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    public void shouldMigrateCase_kent_ga() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-kent-ga.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertThat(migratedCaseData.get("regionList"), is("southeast"));
        assertThat(migratedCaseData.get("southEastFRCList"), is("kentfrc"));
        assertThat(migratedCaseData.get("kentSurreyCourtList"), is("FR_kent_surrey_hc_list_1"));
        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("northEastFRCListSL"));
        assertNull(migratedCaseData.get("kentSurreyCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    public void shouldMigrateCase_invalidRegion() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-invalid-region.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertNull(migratedCaseData.get("regionListSL"));
        assertThat(migratedCaseData.get("regionList"), is("error"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    public void shouldMigrateCase_invalidRegion_ac() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-invalid-region-ac.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertNull(migratedCaseData.get("regionListSL"));
        assertThat(migratedCaseData.get("regionList"), is("error"));
    }

    @Test
    public void shouldMigrateCase_invalidFrcWales() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-invalid-frc-wales.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("walesFRCListSL"));
        assertThat(migratedCaseData.get("regionList"), is("wales"));
        assertThat(migratedCaseData.get("walesFRCList"), is("birmingham"));
    }

    @Test
    public void shouldMigrateCase_invalidFrcWales_ac() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-invalid-frc-wales-ac.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("walesFRCListSL"));
        assertThat(migratedCaseData.get("regionList"), is("wales"));
        assertThat(migratedCaseData.get("walesFRCList"), is("birmingham"));
    }

    @Test
    public void shouldMigrateCase_invalidFrcMidlands() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-invalid-frc-midlands.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("midlandsFRCListSL"));
        assertThat(migratedCaseData.get("regionList"), is("midlands"));
        assertThat(migratedCaseData.get("midlandsFRCList"), is("london"));
    }

    @Test
    public void shouldMigrateCase_invalidFrcMidlands_ac() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-invalid-frc-midlands-ac.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("midlandsFRCListSL"));
        assertThat(migratedCaseData.get("regionList"), is("midlands"));
        assertThat(migratedCaseData.get("midlandsFRCList"), is("london"));
    }

    @Test
    public void shouldMigrateCase_invalidFrcLondon() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-invalid-frc-london.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("londonFRCListSL"));
        assertThat(migratedCaseData.get("regionList"), is("london"));
        assertThat(migratedCaseData.get("londonFRCList"), is("birmingham"));
    }

    @Test
    public void shouldMigrateCase_invalidFrcLondon_ac() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-invalid-frc-london-ac.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("londonFRCListSL"));
        assertThat(migratedCaseData.get("regionList"), is("london"));
        assertThat(migratedCaseData.get("londonFRCList"), is("birmingham"));
    }

    @Test
    public void shouldMigrateCase_invalidFrcNorthwest() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-invalid-frc-northwest.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("northWestFRCListSL"));
        assertThat(migratedCaseData.get("regionList"), is("northwest"));
        assertThat(migratedCaseData.get("northWestFRCList"), is("birmingham"));
    }

    @Test
    public void shouldMigrateCase_invalidFrcNorthwest_ac() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-invalid-frc-northwest-ac.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("northWestFRCListSL"));
        assertThat(migratedCaseData.get("regionList"), is("northwest"));
        assertThat(migratedCaseData.get("northWestFRCList"), is("birmingham"));
    }

    @Test
    public void shouldMigrateCase_invalidFrcNortheast() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-invalid-frc-northeast.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("northEastFRCListSL"));
        assertThat(migratedCaseData.get("regionList"), is("northeast"));
        assertThat(migratedCaseData.get("northEastFRCList"), is("birmingham"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    public void shouldMigrateCase_invalidFrcNortheast_ac() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-invalid-frc-northeast-ac.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("northEastFRCListSL"));
        assertThat(migratedCaseData.get("regionList"), is("northeast"));
        assertThat(migratedCaseData.get("northEastFRCList"), is("birmingham"));
    }

    @Test
    public void shouldMigrateCase_invalidFrcSoutheast() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-invalid-frc-southeast.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("southEastFRCListSL"));
        assertThat(migratedCaseData.get("regionList"), is("southeast"));
        assertThat(migratedCaseData.get("southEastFRCList"), is("birmingham"));
    }

    @Test
    public void shouldMigrateCase_invalidFrcSoutheast_ac() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-invalid-frc-southeast-ac.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("southEastFRCListSL"));
        assertThat(migratedCaseData.get("regionList"), is("southeast"));
        assertThat(migratedCaseData.get("southEastFRCList"), is("birmingham"));
    }

    @Test
    public void shouldNotMigrateCase() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-already-migrated.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertNull(migratedCaseData);
    }

    @Test
    public void shouldNotMigrateCaseConsented() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-consented.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertNull(migratedCaseData);
    }

    @Test
    public void shouldNotMigrateCaseInvalidCourtData() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-invalid-court-data.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertNull(migratedCaseData);
    }

    @Test
    public void shouldNotMigrateCaseInvalidCourtDataGA() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-invalid-court-data-ga.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertNull(migratedCaseData);
    }

    @Test
    public void shouldNotMigrateCaseNoCourtData() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-no-court-data.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertNull(migratedCaseData);
    }
}
