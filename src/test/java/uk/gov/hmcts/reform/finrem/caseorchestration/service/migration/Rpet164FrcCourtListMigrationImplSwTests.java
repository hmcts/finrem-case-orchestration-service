package uk.gov.hmcts.reform.finrem.caseorchestration.service.migration;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CASE_TYPE_ID_CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ALDERSHOT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BARNSTAPLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BASINGSTOKE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BATH;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BODMIN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BOURNEMOUTH;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BRISTOL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BRISTOLFRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BRISTOL_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DEVON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DEVON_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DORSET;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DORSET_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.EXETER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GLOUCESTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ISLE_OF_WIGHT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.OTHER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PLYMOUTH;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PORTSMOUTH;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REGION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SALISBURY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHAMPTON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHWEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHWEST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SWINDON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SWOTHER_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.TAUNTON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.TORQUAY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.TRURO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WESTON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WEYMOUTH;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WINCHESTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.YEOVIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.ALDERSHOT_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.BARNSTAPLE_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.BASINGSTOKE_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.BATH_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.BODMIN_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.BOURNEMOUTH_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.BRISTOL_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.EXETER_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.GLOUCESTER_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.LONDON_TEMP;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.NEWPORT_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.PLYMOUTH_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.PORTSMOUTH_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.SALISBURY_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.SOUTHAMPTON_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.SWINDON_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.TAUNTON_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.TORQUAY_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.TRURO_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.WESTON_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.WEYMOUTH_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.WINCHESTER_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.YEOVIL_OLD;

public class Rpet164FrcCourtListMigrationImplSwTests {
    Map<String, Object> caseData = new HashMap<>();
    CaseDetails caseDetails = CaseDetails.builder().caseTypeId(CASE_TYPE_ID_CONSENTED).data(caseData).build();
    Rpet164FrcCourtListMigrationImpl classUnderTest = new Rpet164FrcCourtListMigrationImpl();

    @Before
    public void setup() {
        //Given
        caseData.clear();
        caseData.put(REGION, SOUTHWEST);
        caseData.put(SOUTHWEST_FRC_LIST, OTHER);
    }

    @Test
    public void shouldMigrateCase_Plymouth() {
        //Given
        caseData.put(SWOTHER_COURTLIST, PLYMOUTH_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(SWOTHER_COURTLIST));
        assertEquals(SOUTHWEST, migratedCaseData.get(REGION));
        assertEquals(DEVON, migratedCaseData.get(SOUTHWEST_FRC_LIST));
        assertEquals(PLYMOUTH, migratedCaseData.get(DEVON_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_Exeter() {
        //Given
        caseData.put(SWOTHER_COURTLIST, EXETER_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(SWOTHER_COURTLIST));
        assertEquals(SOUTHWEST, migratedCaseData.get(REGION));
        assertEquals(DEVON, migratedCaseData.get(SOUTHWEST_FRC_LIST));
        assertEquals(EXETER, migratedCaseData.get(DEVON_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_Taunton() {
        //Given
        caseData.put(SWOTHER_COURTLIST, TAUNTON_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(SWOTHER_COURTLIST));
        assertEquals(SOUTHWEST, migratedCaseData.get(REGION));
        assertEquals(DEVON, migratedCaseData.get(SOUTHWEST_FRC_LIST));
        assertEquals(TAUNTON, migratedCaseData.get(DEVON_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_Torwuay() {
        //Given
        caseData.put(SWOTHER_COURTLIST, TORQUAY_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(SWOTHER_COURTLIST));
        assertEquals(SOUTHWEST, migratedCaseData.get(REGION));
        assertEquals(DEVON, migratedCaseData.get(SOUTHWEST_FRC_LIST));
        assertEquals(TORQUAY, migratedCaseData.get(DEVON_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_Barnstaple() {
        //Given
        caseData.put(SWOTHER_COURTLIST, BARNSTAPLE_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(SWOTHER_COURTLIST));
        assertEquals(SOUTHWEST, migratedCaseData.get(REGION));
        assertEquals(DEVON, migratedCaseData.get(SOUTHWEST_FRC_LIST));
        assertEquals(BARNSTAPLE, migratedCaseData.get(DEVON_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_Truro() {
        //Given
        caseData.put(SWOTHER_COURTLIST, TRURO_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(SWOTHER_COURTLIST));
        assertEquals(SOUTHWEST, migratedCaseData.get(REGION));
        assertEquals(DEVON, migratedCaseData.get(SOUTHWEST_FRC_LIST));
        assertEquals(TRURO, migratedCaseData.get(DEVON_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_Yeovil() {
        //Given
        caseData.put(SWOTHER_COURTLIST, YEOVIL_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(SWOTHER_COURTLIST));
        assertEquals(SOUTHWEST, migratedCaseData.get(REGION));
        assertEquals(DEVON, migratedCaseData.get(SOUTHWEST_FRC_LIST));
        assertEquals(YEOVIL, migratedCaseData.get(DEVON_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_Bodmin() {
        //Given
        caseData.put(SWOTHER_COURTLIST, BODMIN_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(SWOTHER_COURTLIST));
        assertEquals(SOUTHWEST, migratedCaseData.get(REGION));
        assertEquals(DEVON, migratedCaseData.get(SOUTHWEST_FRC_LIST));
        assertEquals(BODMIN, migratedCaseData.get(DEVON_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_Bournemouth() {
        //Given
        caseData.put(SWOTHER_COURTLIST, BOURNEMOUTH_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(SWOTHER_COURTLIST));
        assertEquals(SOUTHWEST, migratedCaseData.get(REGION));
        assertEquals(DORSET, migratedCaseData.get(SOUTHWEST_FRC_LIST));
        assertEquals(BOURNEMOUTH, migratedCaseData.get(DORSET_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_Weymouth() {
        //Given
        caseData.put(SWOTHER_COURTLIST, WEYMOUTH_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(SWOTHER_COURTLIST));
        assertEquals(SOUTHWEST, migratedCaseData.get(REGION));
        assertEquals(DORSET, migratedCaseData.get(SOUTHWEST_FRC_LIST));
        assertEquals(WEYMOUTH, migratedCaseData.get(DORSET_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_Winchester() {
        //Given
        caseData.put(SWOTHER_COURTLIST, WINCHESTER_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(SWOTHER_COURTLIST));
        assertEquals(SOUTHWEST, migratedCaseData.get(REGION));
        assertEquals(DORSET, migratedCaseData.get(SOUTHWEST_FRC_LIST));
        assertEquals(WINCHESTER, migratedCaseData.get(DORSET_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_Portsmouth() {
        //Given
        caseData.put(SWOTHER_COURTLIST, PORTSMOUTH_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(SWOTHER_COURTLIST));
        assertEquals(SOUTHWEST, migratedCaseData.get(REGION));
        assertEquals(DORSET, migratedCaseData.get(SOUTHWEST_FRC_LIST));
        assertEquals(PORTSMOUTH, migratedCaseData.get(DORSET_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_Southampton() {
        //Given
        caseData.put(SWOTHER_COURTLIST, SOUTHAMPTON_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(SWOTHER_COURTLIST));
        assertEquals(SOUTHWEST, migratedCaseData.get(REGION));
        assertEquals(DORSET, migratedCaseData.get(SOUTHWEST_FRC_LIST));
        assertEquals(SOUTHAMPTON, migratedCaseData.get(DORSET_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_Aldershot() {
        //Given
        caseData.put(SWOTHER_COURTLIST, ALDERSHOT_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(SWOTHER_COURTLIST));
        assertEquals(SOUTHWEST, migratedCaseData.get(REGION));
        assertEquals(DORSET, migratedCaseData.get(SOUTHWEST_FRC_LIST));
        assertEquals(ALDERSHOT, migratedCaseData.get(DORSET_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_Basingstoke() {
        //Given
        caseData.put(SWOTHER_COURTLIST, BASINGSTOKE_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(SWOTHER_COURTLIST));
        assertEquals(SOUTHWEST, migratedCaseData.get(REGION));
        assertEquals(DORSET, migratedCaseData.get(SOUTHWEST_FRC_LIST));
        assertEquals(BASINGSTOKE, migratedCaseData.get(DORSET_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_Isle_of_Wight() {
        //Given
        caseData.put(SWOTHER_COURTLIST, NEWPORT_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(SWOTHER_COURTLIST));
        assertEquals(SOUTHWEST, migratedCaseData.get(REGION));
        assertEquals(DORSET, migratedCaseData.get(SOUTHWEST_FRC_LIST));
        assertEquals(ISLE_OF_WIGHT, migratedCaseData.get(DORSET_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_Bristol() {
        //Given
        caseData.put(SWOTHER_COURTLIST, BRISTOL_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(SWOTHER_COURTLIST));
        assertEquals(SOUTHWEST, migratedCaseData.get(REGION));
        assertEquals(BRISTOLFRC, migratedCaseData.get(SOUTHWEST_FRC_LIST));
        assertEquals(BRISTOL, migratedCaseData.get(BRISTOL_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_Gloucester() {
        //Given
        caseData.put(SWOTHER_COURTLIST, GLOUCESTER_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(SWOTHER_COURTLIST));
        assertEquals(SOUTHWEST, migratedCaseData.get(REGION));
        assertEquals(BRISTOLFRC, migratedCaseData.get(SOUTHWEST_FRC_LIST));
        assertEquals(GLOUCESTER, migratedCaseData.get(BRISTOL_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_Swindon() {
        //Given
        caseData.put(SWOTHER_COURTLIST, SWINDON_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(SWOTHER_COURTLIST));
        assertEquals(SOUTHWEST, migratedCaseData.get(REGION));
        assertEquals(BRISTOLFRC, migratedCaseData.get(SOUTHWEST_FRC_LIST));
        assertEquals(SWINDON, migratedCaseData.get(BRISTOL_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_Salisbury() {
        //Given
        caseData.put(SWOTHER_COURTLIST, SALISBURY_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(SWOTHER_COURTLIST));
        assertEquals(SOUTHWEST, migratedCaseData.get(REGION));
        assertEquals(BRISTOLFRC, migratedCaseData.get(SOUTHWEST_FRC_LIST));
        assertEquals(SALISBURY, migratedCaseData.get(BRISTOL_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_Bath() {
        //Given
        caseData.put(SWOTHER_COURTLIST, BATH_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(SWOTHER_COURTLIST));
        assertEquals(SOUTHWEST, migratedCaseData.get(REGION));
        assertEquals(BRISTOLFRC, migratedCaseData.get(SOUTHWEST_FRC_LIST));
        assertEquals(BATH, migratedCaseData.get(BRISTOL_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_Weston() {
        //Given
        caseData.put(SWOTHER_COURTLIST, WESTON_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(SWOTHER_COURTLIST));
        assertEquals(SOUTHWEST, migratedCaseData.get(REGION));
        assertEquals(BRISTOLFRC, migratedCaseData.get(SOUTHWEST_FRC_LIST));
        assertEquals(WESTON, migratedCaseData.get(BRISTOL_COURTLIST));
    }

    @Test
    public void shouldNotMigrateCase_unknown_to_temp() {
        //Given
        caseData.put(SWOTHER_COURTLIST, "some_unknown_code");

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(BRISTOL_COURTLIST));
        assertNull(migratedCaseData.get(DORSET_COURTLIST));
        assertNull(migratedCaseData.get(DEVON_COURTLIST));
        assertNull(migratedCaseData.get(SWOTHER_COURTLIST));
        assertEquals(LONDON, migratedCaseData.get(REGION));
        assertEquals(LONDON, migratedCaseData.get(LONDON_FRC_LIST));
        assertEquals(LONDON_TEMP, migratedCaseData.get(LONDON_COURTLIST));
    }
}