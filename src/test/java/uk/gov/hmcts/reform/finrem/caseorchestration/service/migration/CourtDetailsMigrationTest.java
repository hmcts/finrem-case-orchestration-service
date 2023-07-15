package uk.gov.hmcts.reform.finrem.caseorchestration.service.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;


class CourtDetailsMigrationTest extends BaseServiceTest {

    private ObjectMapper mapper;

    @BeforeEach
    void setup() {
        mapper = new ObjectMapper();
    }


    @Test
    void shouldMigrateCase_newport() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-newport.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertEquals("wales", migratedCaseData.get("regionList"));
        assertEquals("newport", migratedCaseData.get("walesFRCList"));
        assertEquals("FR_newport_hc_list_1", migratedCaseData.get("newportCourtList"));

        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("walesFRCListSL"));
        assertNull(migratedCaseData.get("newportCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    void shouldMigrateCase_newport_ac() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-newport-ac.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertEquals("wales", migratedCaseData.get("regionList"));
        assertEquals("newport", migratedCaseData.get("walesFRCList"));
        assertEquals("FR_newport_hc_list_1", migratedCaseData.get("newportCourtList"));

        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("walesFRCListSL"));
        assertNull(migratedCaseData.get("newportCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    void shouldMigrateCase_newport_ga() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-newport-ga.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);


        assertEquals("wales", migratedCaseData.get("regionList"));
        assertEquals("newport", migratedCaseData.get("walesFRCList"));
        assertEquals("FR_newport_hc_list_1", migratedCaseData.get("newportCourtList"));
        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("walesFRCListSL"));
        assertNull(migratedCaseData.get("newportCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    void shouldMigrateCase_swansea() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-swansea.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertEquals("wales", migratedCaseData.get("regionList"));
        assertEquals("swansea", migratedCaseData.get("walesFRCList"));
        assertEquals("FR_swansea_hc_list_1", migratedCaseData.get("swanseaCourtList"));

        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("walesFRCListSL"));
        assertNull(migratedCaseData.get("swanseaCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    void shouldMigrateCase_swansea_ac() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-swansea-ac.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertEquals("wales", migratedCaseData.get("regionList"));
        assertEquals("swansea", migratedCaseData.get("walesFRCList"));
        assertEquals("FR_swansea_hc_list_6", migratedCaseData.get("swanseaCourtList"));

        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("walesFRCListSL"));
        assertNull(migratedCaseData.get("swanseaCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    void shouldMigrateCase_swansea_ga() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-swansea-ga.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertEquals("wales", migratedCaseData.get("regionList"));
        assertEquals("swansea", migratedCaseData.get("walesFRCList"));
        assertEquals("FR_swansea_hc_list_6", migratedCaseData.get("swanseaCourtList"));

        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("walesFRCListSL"));
        assertNull(migratedCaseData.get("swanseaCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    void shouldMigrateCase_cfc() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-cfc.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertEquals("london", migratedCaseData.get("regionList"));
        assertEquals("cfc", migratedCaseData.get("londonFRCList"));
        assertEquals("FR_s_CFCList_9", migratedCaseData.get("cfcCourtList"));

        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("londonFRCListSL"));
        assertNull(migratedCaseData.get("cfcCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    void shouldMigrateCase_cfc_ac() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-cfc-ac.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertEquals("london", migratedCaseData.get("regionList"));
        assertEquals("cfc", migratedCaseData.get("londonFRCList"));
        assertEquals("FR_s_CFCList_9", migratedCaseData.get("cfcCourtList"));
        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("londonFRCListSL"));
        assertNull(migratedCaseData.get("cfcCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    void shouldMigrateCase_cfc_ga() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-cfc-ac.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertEquals("london", migratedCaseData.get("regionList"));
        assertEquals("cfc", migratedCaseData.get("londonFRCList"));
        assertEquals("FR_s_CFCList_9", migratedCaseData.get("cfcCourtList"));

        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("londonFRCListSL"));
        assertNull(migratedCaseData.get("cfcCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    void shouldMigrateCase_nottingham() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-nottingham.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertEquals("midlands", migratedCaseData.get("regionList"));
        assertEquals("nottingham", migratedCaseData.get("midlandsFRCList"));
        assertEquals("FR_nottingham_hc_list_1", migratedCaseData.get("nottinghamCourtList"));


        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("midlandsFRCListSL"));
        assertNull(migratedCaseData.get("nottinghamCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    void shouldMigrateCase_nottingham_ac() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-nottingham-ac.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertEquals("midlands", migratedCaseData.get("regionList"));
        assertEquals("nottingham", migratedCaseData.get("midlandsFRCList"));
        assertEquals("FR_nottingham_hc_list_1", migratedCaseData.get("nottinghamCourtList"));

        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("midlandsFRCListSL"));
        assertNull(migratedCaseData.get("nottinghamCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    void shouldMigrateCase_nottingham_ga() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-nottingham-ga.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertEquals("midlands", migratedCaseData.get("regionList"));
        assertEquals("nottingham", migratedCaseData.get("midlandsFRCList"));
        assertEquals("FR_nottingham_hc_list_1", migratedCaseData.get("nottinghamCourtList"));

        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("midlandsFRCListSL"));
        assertNull(migratedCaseData.get("nottinghamCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    void shouldMigrateCase_birmingham() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-birmingham.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertEquals("midlands", migratedCaseData.get("regionList"));
        assertEquals("birmingham", migratedCaseData.get("midlandsFRCList"));
        assertEquals("FR_birmingham_hc_list_1", migratedCaseData.get("birminghamCourtList"));


        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("midlandsFRCListSL"));
        assertNull(migratedCaseData.get("birminghamCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    void shouldMigrateCase_birmingham_ac() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-birmingham-ac.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertEquals("midlands", migratedCaseData.get("regionList"));
        assertEquals("birmingham", migratedCaseData.get("midlandsFRCList"));
        assertEquals("FR_birmingham_hc_list_1", migratedCaseData.get("birminghamCourtList"));

        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("midlandsFRCListSL"));
        assertNull(migratedCaseData.get("birminghamCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    void shouldMigrateCase_birmingham_ga() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-birmingham-ga.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertEquals("midlands", migratedCaseData.get("regionList"));
        assertEquals("birmingham", migratedCaseData.get("midlandsFRCList"));
        assertEquals("FR_birmingham_hc_list_1", migratedCaseData.get("birminghamCourtList"));

        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("midlandsFRCListSL"));
        assertNull(migratedCaseData.get("birminghamCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    void shouldMigrateCase_liverpool() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-liverpool.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertEquals("northwest", migratedCaseData.get("regionList"));
        assertEquals("liverpool", migratedCaseData.get("northWestFRCList"));
        assertEquals("FR_liverpool_hc_list_1", migratedCaseData.get("liverpoolCourtList"));

        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("northWestFRCListSL"));
        assertNull(migratedCaseData.get("liverpoolCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    void shouldMigrateCase_liverpool_ac() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-liverpool-ac.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertEquals("northwest", migratedCaseData.get("regionList"));
        assertEquals("liverpool", migratedCaseData.get("northWestFRCList"));
        assertEquals("FR_liverpool_hc_list_1", migratedCaseData.get("liverpoolCourtList"));

        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("northWestFRCListSL"));
        assertNull(migratedCaseData.get("liverpoolCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    void shouldMigrateCase_liverpool_ga() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-liverpool-ga.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertEquals("northwest", migratedCaseData.get("regionList"));
        assertEquals("liverpool", migratedCaseData.get("northWestFRCList"));
        assertEquals("FR_liverpool_hc_list_1", migratedCaseData.get("liverpoolCourtList"));

        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("northWestFRCListSL"));
        assertNull(migratedCaseData.get("liverpoolCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    void shouldMigrateCase_manchester() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-manchester.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertEquals("northwest", migratedCaseData.get("regionList"));
        assertEquals("manchester", migratedCaseData.get("northWestFRCList"));
        assertEquals("FR_manchester_hc_list_1", migratedCaseData.get("manchesterCourtList"));

        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("northWestFRCListSL"));
        assertNull(migratedCaseData.get("manchesterCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    void shouldMigrateCase_manchester_ac() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-manchester-ac.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertEquals("northwest", migratedCaseData.get("regionList"));
        assertEquals("manchester", migratedCaseData.get("northWestFRCList"));
        assertEquals("FR_manchester_hc_list_1", migratedCaseData.get("manchesterCourtList"));

        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("northWestFRCListSL"));
        assertNull(migratedCaseData.get("manchesterCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    void shouldMigrateCase_manchester_ga() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-manchester-ga.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertEquals("northwest", migratedCaseData.get("regionList"));
        assertEquals("manchester", migratedCaseData.get("northWestFRCList"));
        assertEquals("FR_manchester_hc_list_1", migratedCaseData.get("manchesterCourtList"));

        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("northWestFRCListSL"));
        assertNull(migratedCaseData.get("manchesterCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    void shouldMigrateCase_cleaveland() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-cleaveland.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);


        assertEquals("northeast", migratedCaseData.get("regionList"));
        assertEquals("cleaveland", migratedCaseData.get("northEastFRCList"));
        assertEquals("FR_cleaveland_hc_list_1", migratedCaseData.get("cleavelandCourtList"));


        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("northEastFRCListSL"));
        assertNull(migratedCaseData.get("cleavelandCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    void shouldMigrateCase_cleaveland_ac() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-cleaveland-ac.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertEquals("northeast", migratedCaseData.get("regionList"));
        assertEquals("cleaveland", migratedCaseData.get("northEastFRCList"));
        assertEquals("FR_cleaveland_hc_list_1", migratedCaseData.get("cleavelandCourtList"));

        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("northEastFRCListSL"));
        assertNull(migratedCaseData.get("cleavelandCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    void shouldMigrateCase_nwyorkshire() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-nwyorkshire.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertEquals("northeast", migratedCaseData.get("regionList"));
        assertEquals("nwyorkshire", migratedCaseData.get("northEastFRCList"));
        assertEquals("FR_nwyorkshire_hc_list_1", migratedCaseData.get("nwyorkshireCourtList"));

        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("northEastFRCListSL"));
        assertNull(migratedCaseData.get("nwyorkshireCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    void shouldMigrateCase_nwyorkshire_ac() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-nwyorkshire-ac.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertEquals("northeast", migratedCaseData.get("regionList"));
        assertEquals("nwyorkshire", migratedCaseData.get("northEastFRCList"));
        assertEquals("FR_nwyorkshire_hc_list_1", migratedCaseData.get("nwyorkshireCourtList"));

        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("northEastFRCListSL"));
        assertNull(migratedCaseData.get("nwyorkshireCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    void shouldMigrateCase_nwyorkshire_ga() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-nwyorkshire-ga.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertEquals("northeast", migratedCaseData.get("regionList"));
        assertEquals("nwyorkshire", migratedCaseData.get("northEastFRCList"));
        assertEquals("FR_nwyorkshire_hc_list_1", migratedCaseData.get("nwyorkshireCourtList"));

        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("northEastFRCListSL"));
        assertNull(migratedCaseData.get("nwyorkshireCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    void shouldMigrateCase_hsyorkshire() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-hsyorkshire.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertEquals("northeast", migratedCaseData.get("regionList"));
        assertEquals("hsyorkshire", migratedCaseData.get("northEastFRCList"));
        assertEquals("FR_hsyorkshire_hc_list_1", migratedCaseData.get("humberCourtList"));

        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("northEastFRCListSL"));
        assertNull(migratedCaseData.get("humberCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    void shouldMigrateCase_hsyorkshire_ac() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-hsyorkshire-ac.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertEquals("northeast", migratedCaseData.get("regionList"));
        assertEquals("hsyorkshire", migratedCaseData.get("northEastFRCList"));
        assertEquals("FR_hsyorkshire_hc_list_1", migratedCaseData.get("humberCourtList"));

        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("northEastFRCListSL"));
        assertNull(migratedCaseData.get("humberCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    void shouldMigrateCase_hsyorkshire_ga() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-hsyorkshire-ga.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertEquals("northeast", migratedCaseData.get("regionList"));
        assertEquals("hsyorkshire", migratedCaseData.get("northEastFRCList"));
        assertEquals("FR_hsyorkshire_hc_list_1", migratedCaseData.get("humberCourtList"));
        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("northEastFRCListSL"));
        assertNull(migratedCaseData.get("humberCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    void shouldMigrateCase_kent() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-kent.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertEquals("southeast", migratedCaseData.get("regionList"));
        assertEquals("kentfrc", migratedCaseData.get("southEastFRCList"));
        assertEquals("FR_kent_surrey_hc_list_1", migratedCaseData.get("kentSurreyCourtList"));
        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("northEastFRCListSL"));
        assertNull(migratedCaseData.get("kentSurreyCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    void shouldMigrateCase_kent_ac() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-kent-ac.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertEquals("southeast", migratedCaseData.get("regionList"));
        assertEquals("kentfrc", migratedCaseData.get("southEastFRCList"));
        assertEquals("FR_kent_surrey_hc_list_1", migratedCaseData.get("kentSurreyCourtList"));

        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("northEastFRCListSL"));
        assertNull(migratedCaseData.get("kentSurreyCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    void shouldMigrateCase_kent_ga() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-kent-ga.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertEquals("southeast", migratedCaseData.get("regionList"));
        assertEquals("kentfrc", migratedCaseData.get("southEastFRCList"));
        assertEquals("FR_kent_surrey_hc_list_1", migratedCaseData.get("kentSurreyCourtList"));

        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("northEastFRCListSL"));
        assertNull(migratedCaseData.get("kentSurreyCourtListSL"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    void shouldMigrateCase_invalidRegion() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-invalid-region.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertNull(migratedCaseData.get("regionListSL"));
        assertEquals("error", migratedCaseData.get("regionList"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    void shouldMigrateCase_invalidRegion_ac() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-invalid-region-ac.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertNull(migratedCaseData.get("regionListSL"));
        assertEquals("error", migratedCaseData.get("regionList"));
    }

    @Test
    void shouldMigrateCase_invalidFrcWales() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-invalid-frc-wales.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("walesFRCListSL"));
        assertEquals("wales", migratedCaseData.get("regionList"));
        assertEquals("birmingham", migratedCaseData.get("walesFRCList"));
    }

    @Test
    void shouldMigrateCase_invalidFrcWales_ac() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-invalid-frc-wales-ac.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("walesFRCListSL"));
        assertEquals("wales", migratedCaseData.get("regionList"));
        assertEquals("birmingham", migratedCaseData.get("walesFRCList"));
    }

    @Test
    void shouldMigrateCase_invalidFrcMidlands() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-invalid-frc-midlands.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("midlandsFRCListSL"));

        assertEquals("midlands", migratedCaseData.get("regionList"));
        assertEquals("london", migratedCaseData.get("midlandsFRCList"));
    }

    @Test
    void shouldMigrateCase_invalidFrcMidlands_ac() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-invalid-frc-midlands-ac.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("midlandsFRCListSL"));
        assertEquals("midlands", migratedCaseData.get("regionList"));
        assertEquals("london", migratedCaseData.get("midlandsFRCList"));
    }

    @Test
    void shouldMigrateCase_invalidFrcLondon() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-invalid-frc-london.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("londonFRCListSL"));
        assertEquals("london", migratedCaseData.get("regionList"));
        assertEquals("birmingham", migratedCaseData.get("londonFRCList"));
    }

    @Test
    void shouldMigrateCase_invalidFrcLondon_ac() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-invalid-frc-london-ac.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("londonFRCListSL"));

        assertEquals("london", migratedCaseData.get("regionList"));
        assertEquals("birmingham", migratedCaseData.get("londonFRCList"));
    }

    @Test
    void shouldMigrateCase_invalidFrcNorthwest() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-invalid-frc-northwest.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("northWestFRCListSL"));
        assertEquals("northwest", migratedCaseData.get("regionList"));
        assertEquals("birmingham", migratedCaseData.get("northWestFRCList"));
    }

    @Test
    void shouldMigrateCase_invalidFrcNorthwest_ac() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-invalid-frc-northwest-ac.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("northWestFRCListSL"));
        assertEquals("northwest", migratedCaseData.get("regionList"));
        assertEquals("birmingham", migratedCaseData.get("northWestFRCList"));
    }

    @Test
    void shouldMigrateCase_invalidFrcNortheast() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-invalid-frc-northeast.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("northEastFRCListSL"));
        assertEquals("northeast", migratedCaseData.get("regionList"));
        assertEquals("birmingham", migratedCaseData.get("northEastFRCList"));
        assertNull(migratedCaseData.get("allocatedCourtList"));
        assertNull(migratedCaseData.get("allocatedCourtListGA"));
        assertNull(migratedCaseData.get("warnings"));
    }

    @Test
    void shouldMigrateCase_invalidFrcNortheast_ac() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-invalid-frc-northeast-ac.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("northEastFRCListSL"));
        assertEquals("northeast", migratedCaseData.get("regionList"));
        assertEquals("birmingham", migratedCaseData.get("northEastFRCList"));
    }

    @Test
    void shouldMigrateCase_invalidFrcSoutheast() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-invalid-frc-southeast.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("southEastFRCListSL"));
        assertEquals("southeast", migratedCaseData.get("regionList"));
        assertEquals("birmingham", migratedCaseData.get("southEastFRCList"));
    }

    @Test
    void shouldMigrateCase_invalidFrcSoutheast_ac() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-migration-applicable-invalid-frc-southeast-ac.json",
            mapper
        );
        CourtDetailsMigration classUnderTest = new CourtDetailsMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertNull(migratedCaseData.get("regionListSL"));
        assertNull(migratedCaseData.get("southEastFRCListSL"));
        assertEquals("southeast", migratedCaseData.get("regionList"));
        assertEquals("birmingham", migratedCaseData.get("southEastFRCList"));
    }

    @Test
    void migrateWithDifferentSenarios() {
        List<String> jsonData = List.of("/fixtures/migration/courtDetailsMigration/ccd-migrate-request-already-migrated.json",
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-consented.json",
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-invalid-court-data.json",
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-invalid-court-data-ga.json",
            "/fixtures/migration/courtDetailsMigration/ccd-migrate-request-no-court-data.json");
        jsonData.forEach(data -> {
            CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(data, mapper);
            CourtDetailsMigration classUnderTest = new CourtDetailsMigration();
            Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);
            assertTrue(migratedCaseData.isEmpty());
        });
    }
}
