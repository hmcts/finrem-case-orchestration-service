package uk.gov.hmcts.reform.finrem.caseorchestration.service.migration;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CASE_TYPE_ID_CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CASE_TYPE_ID_CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BIRMINGHAM_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HUMBER_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.KENTFRC_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LIVERPOOL_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MANCHESTER_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIDLANDS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIDLANDS_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NEWPORT_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHEAST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHEAST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHWEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHWEST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTTINGHAM_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NWYORKSHIRE_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REGION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHEAST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHEAST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SWANSEA_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WALES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WALES_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164Phase2UnifyCodesMigrationImpl.BIRMINGHAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164Phase2UnifyCodesMigrationImpl.CLEAVELAND;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164Phase2UnifyCodesMigrationImpl.CLEAVELAND_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164Phase2UnifyCodesMigrationImpl.CLEAVELAND_COURT_LIST_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164Phase2UnifyCodesMigrationImpl.CLEAVELAND_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164Phase2UnifyCodesMigrationImpl.HSYORKSHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164Phase2UnifyCodesMigrationImpl.KENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164Phase2UnifyCodesMigrationImpl.KENT_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164Phase2UnifyCodesMigrationImpl.LIVERPOOL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164Phase2UnifyCodesMigrationImpl.MANCHESTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164Phase2UnifyCodesMigrationImpl.NEWPORT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164Phase2UnifyCodesMigrationImpl.NOTTINGHAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164Phase2UnifyCodesMigrationImpl.NWYORKSHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164Phase2UnifyCodesMigrationImpl.SWANSEA;

public class Rpet164Phase2UnifyCodesMigrationImplRegionsTest {
    Map<String, Object> caseData = new HashMap<>();
    CaseDetails caseDetails = CaseDetails.builder().caseTypeId(CASE_TYPE_ID_CONTESTED).data(caseData).build();
    Rpet164Phase2UnifyCodesMigrationImpl classUnderTest = new Rpet164Phase2UnifyCodesMigrationImpl();

    @Before
    public void setup() {
        //Given
        caseData.clear();
    }

    @Test
    public void shouldMigrateCase_birmingham() {
        //Given
        caseData.put(REGION, MIDLANDS);
        caseData.put(MIDLANDS_FRC_LIST, BIRMINGHAM);
        caseData.put(BIRMINGHAM_COURTLIST, "FR_birmingham_hc_list_1");

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertEquals(MIDLANDS, migratedCaseData.get(REGION));
        assertEquals(BIRMINGHAM, migratedCaseData.get(MIDLANDS_FRC_LIST));
        assertEquals("FR_birminghamList_1", migratedCaseData.get(BIRMINGHAM_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_nottingham() {
        //Given
        caseData.put(REGION, MIDLANDS);
        caseData.put(MIDLANDS_FRC_LIST, NOTTINGHAM);
        caseData.put(NOTTINGHAM_COURTLIST, "FR_s_NottinghamList_4");

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertEquals(MIDLANDS, migratedCaseData.get(REGION));
        assertEquals(NOTTINGHAM, migratedCaseData.get(MIDLANDS_FRC_LIST));
        assertEquals("FR_nottinghamList_4", migratedCaseData.get(NOTTINGHAM_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_liverpool() {
        //Given
        caseData.put(REGION, NORTHWEST);
        caseData.put(NORTHWEST_FRC_LIST, LIVERPOOL);
        caseData.put(LIVERPOOL_COURTLIST, "FR_liverpool_hc_list_5");

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertEquals(NORTHWEST, migratedCaseData.get(REGION));
        assertEquals(LIVERPOOL, migratedCaseData.get(NORTHWEST_FRC_LIST));
        assertEquals("FR_liverpoolList_5", migratedCaseData.get(LIVERPOOL_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_manchester() {
        //Given
        caseData.put(REGION, NORTHWEST);
        caseData.put(NORTHWEST_FRC_LIST, MANCHESTER);
        caseData.put(MANCHESTER_COURTLIST, "FR_manchester_hc_list_3");

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertEquals(NORTHWEST, migratedCaseData.get(REGION));
        assertEquals(MANCHESTER, migratedCaseData.get(NORTHWEST_FRC_LIST));
        assertEquals("FR_manchesterList_3", migratedCaseData.get(MANCHESTER_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_cleaveland() {
        //Given
        caseData.put(REGION, NORTHEAST);
        caseData.put(NORTHEAST_FRC_LIST, CLEAVELAND_OLD);
        caseData.put(CLEAVELAND_COURT_LIST_OLD, "FR_cleaveland_hc_list_7");

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(CLEAVELAND_OLD));
        assertNull(migratedCaseData.get(CLEAVELAND_COURT_LIST_OLD));
        assertEquals(NORTHEAST, migratedCaseData.get(REGION));
        assertEquals(CLEAVELAND, migratedCaseData.get(NORTHEAST_FRC_LIST));
        assertEquals("FR_clevelandList_7", migratedCaseData.get(CLEAVELAND_COURT_LIST));
    }

    @Test
    public void shouldMigrateCase_nw_yorkshire() {
        //Given
        caseData.put(REGION, NORTHEAST);
        caseData.put(NORTHEAST_FRC_LIST, NWYORKSHIRE);
        caseData.put(NWYORKSHIRE_COURTLIST, "FR_nw_yorkshire_hc_list_8");

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertEquals(NORTHEAST, migratedCaseData.get(REGION));
        assertEquals(NWYORKSHIRE, migratedCaseData.get(NORTHEAST_FRC_LIST));
        assertEquals("FR_nw_yorkshireList_8", migratedCaseData.get(NWYORKSHIRE_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_hs_yorkshire() {
        //Given
        caseData.put(REGION, NORTHEAST);
        caseData.put(NORTHEAST_FRC_LIST, HSYORKSHIRE);
        caseData.put(HUMBER_COURTLIST, "FR_nw_yorkshire_hc_list_4");

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertEquals(NORTHEAST, migratedCaseData.get(REGION));
        assertEquals(HSYORKSHIRE, migratedCaseData.get(NORTHEAST_FRC_LIST));
        assertEquals("FR_nw_yorkshireList_4", migratedCaseData.get(HUMBER_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_kent() {
        //Given
        caseData.put(REGION, SOUTHEAST);
        caseData.put(SOUTHEAST_FRC_LIST, KENT_OLD);
        caseData.put(KENTFRC_COURTLIST, "FR_kent_surrey_hc_list_10");

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertEquals(SOUTHEAST, migratedCaseData.get(REGION));
        assertEquals(KENT, migratedCaseData.get(SOUTHEAST_FRC_LIST));
        assertEquals("FR_kent_surreyList_10", migratedCaseData.get(KENTFRC_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_newport() {
        //Given
        caseData.put(REGION, WALES);
        caseData.put(WALES_FRC_LIST, NEWPORT);
        caseData.put(NEWPORT_COURTLIST, "FR_newport_hc_list_3");

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertEquals(WALES, migratedCaseData.get(REGION));
        assertEquals(NEWPORT, migratedCaseData.get(WALES_FRC_LIST));
        assertEquals("FR_newportList_3", migratedCaseData.get(NEWPORT_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_swansea() {
        //Given
        caseData.put(REGION, WALES);
        caseData.put(WALES_FRC_LIST, SWANSEA);
        caseData.put(SWANSEA_COURTLIST, "FR_newport_hc_list_3");

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertEquals(WALES, migratedCaseData.get(REGION));
        assertEquals(SWANSEA, migratedCaseData.get(WALES_FRC_LIST));
        assertEquals("FR_newportList_3", migratedCaseData.get(SWANSEA_COURTLIST));
    }

    @Test
    public void shouldNotMigrateDivorceCase() {
        //Given
        Map<String, Object> divCaseData = new HashMap<>();
        CaseDetails divCase = CaseDetails.builder().caseTypeId(CASE_TYPE_ID_CONSENTED).data(divCaseData).build();
        divCaseData.put(REGION, MIDLANDS);
        divCaseData.put(MIDLANDS_FRC_LIST, BIRMINGHAM);
        divCaseData.put(BIRMINGHAM_COURTLIST, "FR_birmingham_hc_list_1");

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(divCase);

        //Then
        assertEquals(MIDLANDS, migratedCaseData.get(REGION));
        assertEquals(BIRMINGHAM, migratedCaseData.get(MIDLANDS_FRC_LIST));
        assertEquals("FR_birmingham_hc_list_1", migratedCaseData.get(BIRMINGHAM_COURTLIST));
    }
}