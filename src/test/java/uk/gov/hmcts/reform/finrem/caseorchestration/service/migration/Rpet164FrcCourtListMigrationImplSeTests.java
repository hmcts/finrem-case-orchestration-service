package uk.gov.hmcts.reform.finrem.caseorchestration.service.migration;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CASE_TYPE_ID_CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BEDFORD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BEDFORDSHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BEDFORDSHIRE_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BURY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CAMBRIDGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CHELMSFORD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HERTFORD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.IPSWICH;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.KENTFRC_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LUTON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MILTON_KEYNES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORWICH;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.OTHER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.OXFORD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PETERBOROUGH;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.READING;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REGION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SEOTHER_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SLOUGH;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHEAST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHEAST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHEND;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.THAMESVALLEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.THAMESVALLEY_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WATFORD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.BASILDON_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.BEDFORD_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.BRIGHTON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.BRIGHTON_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.BURY_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.CAMBRIDGE_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.CHELMSFORD_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.COLCHESTER_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.HERTFORD_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.HIGH_WYCOMBE_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.IPSWICH_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.KENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.LEWES_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.LONDON_TEMP;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.LUTON_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.MILTON_KEYNES_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.NORWICH_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.OXFORD_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.PETERBOROUGH_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.READING_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.SLOUGH_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.SOUTHEND_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.THANET_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.WATFORD_OLD;

public class Rpet164FrcCourtListMigrationImplSeTests {
    Map<String, Object> caseData = new HashMap<>();
    CaseDetails caseDetails = CaseDetails.builder().caseTypeId(CASE_TYPE_ID_CONSENTED).data(caseData).build();
    Rpet164FrcCourtListMigrationImpl classUnderTest = new Rpet164FrcCourtListMigrationImpl();

    @Before
    public void setup() {
        //Given
        caseData.clear();
        caseData.put(REGION, SOUTHEAST);
        caseData.put(SOUTHEAST_FRC_LIST, OTHER);
    }

    @Test
    public void shouldNotMigrateCase_Kent_Canterbury() {
        //Given
        caseData.remove(SOUTHEAST_FRC_LIST);
        caseData.put(SOUTHEAST_FRC_LIST, "kent");
        caseData.put(SEOTHER_COURTLIST, "FR_kent_surrey_hc_list_1");

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertEquals(SOUTHEAST, migratedCaseData.get(REGION));
        assertEquals("kent", migratedCaseData.get(SOUTHEAST_FRC_LIST));
        assertEquals("FR_kent_surrey_hc_list_1", migratedCaseData.get(SEOTHER_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_Peterborough() {
        //Given
        caseData.put(SEOTHER_COURTLIST, PETERBOROUGH_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(SEOTHER_COURTLIST));
        assertEquals(SOUTHEAST, migratedCaseData.get(REGION));
        assertEquals(BEDFORDSHIRE, migratedCaseData.get(SOUTHEAST_FRC_LIST));
        assertEquals(PETERBOROUGH, migratedCaseData.get(BEDFORDSHIRE_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_Cambridge() {
        //Given
        caseData.put(SEOTHER_COURTLIST, CAMBRIDGE_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(SEOTHER_COURTLIST));
        assertEquals(SOUTHEAST, migratedCaseData.get(REGION));
        assertEquals(BEDFORDSHIRE, migratedCaseData.get(SOUTHEAST_FRC_LIST));
        assertEquals(CAMBRIDGE, migratedCaseData.get(BEDFORDSHIRE_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_Bury() {
        //Given
        caseData.put(SEOTHER_COURTLIST, BURY_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(SEOTHER_COURTLIST));
        assertEquals(SOUTHEAST, migratedCaseData.get(REGION));
        assertEquals(BEDFORDSHIRE, migratedCaseData.get(SOUTHEAST_FRC_LIST));
        assertEquals(BURY, migratedCaseData.get(BEDFORDSHIRE_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_Norwich() {
        //Given
        caseData.put(SEOTHER_COURTLIST, NORWICH_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(SEOTHER_COURTLIST));
        assertEquals(SOUTHEAST, migratedCaseData.get(REGION));
        assertEquals(BEDFORDSHIRE, migratedCaseData.get(SOUTHEAST_FRC_LIST));
        assertEquals(NORWICH, migratedCaseData.get(BEDFORDSHIRE_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_Ipswich() {
        //Given
        caseData.put(SEOTHER_COURTLIST, IPSWICH_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(SEOTHER_COURTLIST));
        assertEquals(SOUTHEAST, migratedCaseData.get(REGION));
        assertEquals(BEDFORDSHIRE, migratedCaseData.get(SOUTHEAST_FRC_LIST));
        assertEquals(IPSWICH, migratedCaseData.get(BEDFORDSHIRE_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_Chelmsford() {
        //Given
        caseData.put(SEOTHER_COURTLIST, CHELMSFORD_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(SEOTHER_COURTLIST));
        assertEquals(SOUTHEAST, migratedCaseData.get(REGION));
        assertEquals(BEDFORDSHIRE, migratedCaseData.get(SOUTHEAST_FRC_LIST));
        assertEquals(CHELMSFORD, migratedCaseData.get(BEDFORDSHIRE_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_Southend() {
        //Given
        caseData.put(SEOTHER_COURTLIST, SOUTHEND_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(SEOTHER_COURTLIST));
        assertEquals(SOUTHEAST, migratedCaseData.get(REGION));
        assertEquals(BEDFORDSHIRE, migratedCaseData.get(SOUTHEAST_FRC_LIST));
        assertEquals(SOUTHEND, migratedCaseData.get(BEDFORDSHIRE_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_Bedford() {
        //Given
        caseData.put(SEOTHER_COURTLIST, BEDFORD_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(SEOTHER_COURTLIST));
        assertEquals(SOUTHEAST, migratedCaseData.get(REGION));
        assertEquals(BEDFORDSHIRE, migratedCaseData.get(SOUTHEAST_FRC_LIST));
        assertEquals(BEDFORD, migratedCaseData.get(BEDFORDSHIRE_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_Luton() {
        //Given
        caseData.put(SEOTHER_COURTLIST, LUTON_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(SEOTHER_COURTLIST));
        assertEquals(SOUTHEAST, migratedCaseData.get(REGION));
        assertEquals(BEDFORDSHIRE, migratedCaseData.get(SOUTHEAST_FRC_LIST));
        assertEquals(LUTON, migratedCaseData.get(BEDFORDSHIRE_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_Hertford() {
        //Given
        caseData.put(SEOTHER_COURTLIST, HERTFORD_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(SEOTHER_COURTLIST));
        assertEquals(SOUTHEAST, migratedCaseData.get(REGION));
        assertEquals(BEDFORDSHIRE, migratedCaseData.get(SOUTHEAST_FRC_LIST));
        assertEquals(HERTFORD, migratedCaseData.get(BEDFORDSHIRE_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_Watford() {
        //Given
        caseData.put(SEOTHER_COURTLIST, WATFORD_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(SEOTHER_COURTLIST));
        assertEquals(SOUTHEAST, migratedCaseData.get(REGION));
        assertEquals(BEDFORDSHIRE, migratedCaseData.get(SOUTHEAST_FRC_LIST));
        assertEquals(WATFORD, migratedCaseData.get(BEDFORDSHIRE_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_Oxford() {
        //Given
        caseData.put(SEOTHER_COURTLIST, OXFORD_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(SEOTHER_COURTLIST));
        assertEquals(SOUTHEAST, migratedCaseData.get(REGION));
        assertEquals(THAMESVALLEY, migratedCaseData.get(SOUTHEAST_FRC_LIST));
        assertEquals(OXFORD, migratedCaseData.get(THAMESVALLEY_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_Reading() {
        //Given
        caseData.put(SEOTHER_COURTLIST, READING_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(SEOTHER_COURTLIST));
        assertEquals(SOUTHEAST, migratedCaseData.get(REGION));
        assertEquals(THAMESVALLEY, migratedCaseData.get(SOUTHEAST_FRC_LIST));
        assertEquals(READING, migratedCaseData.get(THAMESVALLEY_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_Milton_Keynes() {
        //Given
        caseData.put(SEOTHER_COURTLIST, MILTON_KEYNES_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(SEOTHER_COURTLIST));
        assertEquals(SOUTHEAST, migratedCaseData.get(REGION));
        assertEquals(THAMESVALLEY, migratedCaseData.get(SOUTHEAST_FRC_LIST));
        assertEquals(MILTON_KEYNES, migratedCaseData.get(THAMESVALLEY_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_Slough() {
        //Given
        caseData.put(SEOTHER_COURTLIST, SLOUGH_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(SEOTHER_COURTLIST));
        assertEquals(SOUTHEAST, migratedCaseData.get(REGION));
        assertEquals(THAMESVALLEY, migratedCaseData.get(SOUTHEAST_FRC_LIST));
        assertEquals(SLOUGH, migratedCaseData.get(THAMESVALLEY_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_Brighton_to_kent_surrey() {
        //Given
        caseData.put(SEOTHER_COURTLIST, BRIGHTON_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(BEDFORDSHIRE_COURTLIST));
        assertNull(migratedCaseData.get(SEOTHER_COURTLIST));
        assertEquals(SOUTHEAST, migratedCaseData.get(REGION));
        assertEquals(KENT, migratedCaseData.get(SOUTHEAST_FRC_LIST));
        assertEquals(BRIGHTON, migratedCaseData.get(KENTFRC_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_Basildon_to_temp() {
        //Given
        caseData.put(SEOTHER_COURTLIST, BASILDON_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(BEDFORDSHIRE_COURTLIST));
        assertNull(migratedCaseData.get(SEOTHER_COURTLIST));
        assertEquals(LONDON, migratedCaseData.get(REGION));
        assertEquals(LONDON, migratedCaseData.get(LONDON_FRC_LIST));
        assertEquals(LONDON_TEMP, migratedCaseData.get(LONDON_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_Colchester_to_temp() {
        //Given
        caseData.put(SEOTHER_COURTLIST, COLCHESTER_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(BEDFORDSHIRE_COURTLIST));
        assertNull(migratedCaseData.get(SEOTHER_COURTLIST));
        assertEquals(LONDON, migratedCaseData.get(REGION));
        assertEquals(LONDON, migratedCaseData.get(LONDON_FRC_LIST));
        assertEquals(LONDON_TEMP, migratedCaseData.get(LONDON_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_High_Wycombe_to_temp() {
        //Given
        caseData.put(SEOTHER_COURTLIST, HIGH_WYCOMBE_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(BEDFORDSHIRE_COURTLIST));
        assertNull(migratedCaseData.get(SEOTHER_COURTLIST));
        assertEquals(LONDON, migratedCaseData.get(REGION));
        assertEquals(LONDON, migratedCaseData.get(LONDON_FRC_LIST));
        assertEquals(LONDON_TEMP, migratedCaseData.get(LONDON_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_Lewes_to_temp() {
        //Given
        caseData.put(SEOTHER_COURTLIST, LEWES_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(BEDFORDSHIRE_COURTLIST));
        assertNull(migratedCaseData.get(SEOTHER_COURTLIST));
        assertEquals(LONDON, migratedCaseData.get(REGION));
        assertEquals(LONDON, migratedCaseData.get(LONDON_FRC_LIST));
        assertEquals(LONDON_TEMP, migratedCaseData.get(LONDON_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_Thanet_to_temp() {
        //Given
        caseData.put(SEOTHER_COURTLIST, THANET_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(BEDFORDSHIRE_COURTLIST));
        assertNull(migratedCaseData.get(SEOTHER_COURTLIST));
        assertEquals(LONDON, migratedCaseData.get(REGION));
        assertEquals(LONDON, migratedCaseData.get(LONDON_FRC_LIST));
        assertEquals(LONDON_TEMP, migratedCaseData.get(LONDON_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_unknown_to_temp() {
        //Given
        caseData.put(SEOTHER_COURTLIST, "some_unknown_code");

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(BEDFORDSHIRE_COURTLIST));
        assertNull(migratedCaseData.get(SEOTHER_COURTLIST));
        assertEquals(LONDON, migratedCaseData.get(REGION));
        assertEquals(LONDON, migratedCaseData.get(LONDON_FRC_LIST));
        assertEquals(LONDON_TEMP, migratedCaseData.get(LONDON_COURTLIST));
    }
}