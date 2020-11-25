package uk.gov.hmcts.reform.finrem.caseorchestration.service.migration;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CASE_TYPE_ID_CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MOLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHWALES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTH_WALES_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.OTHER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PRESTATYN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REGION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SWOTHER_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WALES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WALES_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WALES_OTHER_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WELSHPOOL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WREXHAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.LONDON_TEMP;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.MOLD_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.PRESTATYN_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.WELSHPOOL_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.WREXHAM_OLD;

public class Rpet164FrcCourtListMigrationImplWalesTests {
    Map<String, Object> caseData = new HashMap<>();
    CaseDetails caseDetails = CaseDetails.builder().caseTypeId(CASE_TYPE_ID_CONSENTED).data(caseData).build();
    Rpet164FrcCourtListMigrationImpl classUnderTest = new Rpet164FrcCourtListMigrationImpl();

    @Before
    public void setup() {
        //Given
        caseData.clear();
        caseData.put(REGION, WALES);
        caseData.put(WALES_FRC_LIST, OTHER);
    }

    @Test
    public void shouldMigrateCase_Wrexham() {
        //Given
        caseData.put(WALES_OTHER_COURTLIST, WREXHAM_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(WALES_OTHER_COURTLIST));
        assertEquals(WALES, migratedCaseData.get(REGION));
        assertEquals(NORTHWALES, migratedCaseData.get(WALES_FRC_LIST));
        assertEquals(WREXHAM, migratedCaseData.get(NORTH_WALES_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_Prestatyn() {
        //Given
        caseData.put(WALES_OTHER_COURTLIST, PRESTATYN_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(WALES_OTHER_COURTLIST));
        assertEquals(WALES, migratedCaseData.get(REGION));
        assertEquals(NORTHWALES, migratedCaseData.get(WALES_FRC_LIST));
        assertEquals(PRESTATYN, migratedCaseData.get(NORTH_WALES_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_Welshpool() {
        //Given
        caseData.put(WALES_OTHER_COURTLIST, WELSHPOOL_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(WALES_OTHER_COURTLIST));
        assertEquals(WALES, migratedCaseData.get(REGION));
        assertEquals(NORTHWALES, migratedCaseData.get(WALES_FRC_LIST));
        assertEquals(WELSHPOOL, migratedCaseData.get(NORTH_WALES_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_Mold() {
        //Given
        caseData.put(WALES_OTHER_COURTLIST, MOLD_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(WALES_OTHER_COURTLIST));
        assertEquals(WALES, migratedCaseData.get(REGION));
        assertEquals(NORTHWALES, migratedCaseData.get(WALES_FRC_LIST));
        assertEquals(MOLD, migratedCaseData.get(NORTH_WALES_COURTLIST));
    }

    @Test
    public void shouldNotMigrateCase_unknown_to_temp() {
        //Given
        caseData.put(WALES_OTHER_COURTLIST, "some_unknown_code");

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(NORTH_WALES_COURTLIST));
        assertNull(migratedCaseData.get(SWOTHER_COURTLIST));
        assertEquals(LONDON, migratedCaseData.get(REGION));
        assertEquals(LONDON, migratedCaseData.get(LONDON_FRC_LIST));
        assertEquals(LONDON_TEMP, migratedCaseData.get(LONDON_COURTLIST));
    }
}