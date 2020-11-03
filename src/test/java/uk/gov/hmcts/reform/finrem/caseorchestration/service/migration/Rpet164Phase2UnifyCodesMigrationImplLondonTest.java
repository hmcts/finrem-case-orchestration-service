package uk.gov.hmcts.reform.finrem.caseorchestration.service.migration;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CASE_TYPE_ID_CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CFC_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REGION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164Phase2UnifyCodesMigrationImpl.BARNET;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164Phase2UnifyCodesMigrationImpl.BARNET_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164Phase2UnifyCodesMigrationImpl.BRENTFORD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164Phase2UnifyCodesMigrationImpl.BRENTFORD_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164Phase2UnifyCodesMigrationImpl.BROMLEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164Phase2UnifyCodesMigrationImpl.BROMLEY_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164Phase2UnifyCodesMigrationImpl.CENTRAL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164Phase2UnifyCodesMigrationImpl.CENTRAL_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164Phase2UnifyCodesMigrationImpl.CFC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164Phase2UnifyCodesMigrationImpl.CROYDON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164Phase2UnifyCodesMigrationImpl.CROYDON_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164Phase2UnifyCodesMigrationImpl.EAST_LONDON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164Phase2UnifyCodesMigrationImpl.EAST_LONDON_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164Phase2UnifyCodesMigrationImpl.EDMONTON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164Phase2UnifyCodesMigrationImpl.EDMONTON_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164Phase2UnifyCodesMigrationImpl.KINGSTON_UPON_THAMES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164Phase2UnifyCodesMigrationImpl.KINGSTON_UPON_THAMES_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164Phase2UnifyCodesMigrationImpl.LONDONFRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164Phase2UnifyCodesMigrationImpl.ROMFORD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164Phase2UnifyCodesMigrationImpl.ROMFORD_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164Phase2UnifyCodesMigrationImpl.UXBRIDGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164Phase2UnifyCodesMigrationImpl.UXBRIDGE_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164Phase2UnifyCodesMigrationImpl.WILLESDEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164Phase2UnifyCodesMigrationImpl.WILLESDEN_OLD;

public class Rpet164Phase2UnifyCodesMigrationImplLondonTest {
    Map<String, Object> caseData = new HashMap<>();
    CaseDetails caseDetails = CaseDetails.builder().caseTypeId(CASE_TYPE_ID_CONTESTED).data(caseData).build();
    Rpet164Phase2UnifyCodesMigrationImpl classUnderTest = new Rpet164Phase2UnifyCodesMigrationImpl();

    @Before
    public void setup() {
        //Given
        caseData.clear();
        caseData.put(REGION, LONDON);
    }

    @Test
    public void shouldMigrateCase_central() {
        //Given
        caseData.put(LONDON_FRC_LIST, CFC);
        caseData.put(CFC_COURTLIST, CENTRAL_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(CFC_COURTLIST));
        assertEquals(LONDON, migratedCaseData.get(REGION));
        assertEquals(LONDONFRC, migratedCaseData.get(LONDON_FRC_LIST));
        assertEquals(CENTRAL, migratedCaseData.get(LONDON_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_willesden() {
        //Given
        caseData.put(LONDON_FRC_LIST, CFC);
        caseData.put(CFC_COURTLIST, WILLESDEN_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(CFC_COURTLIST));
        assertEquals(LONDON, migratedCaseData.get(REGION));
        assertEquals(LONDONFRC, migratedCaseData.get(LONDON_FRC_LIST));
        assertEquals(WILLESDEN, migratedCaseData.get(LONDON_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_uxbridge() {
        //Given
        caseData.put(LONDON_FRC_LIST, CFC);
        caseData.put(CFC_COURTLIST, UXBRIDGE_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertEquals(LONDON, migratedCaseData.get(REGION));
        assertEquals(LONDONFRC, migratedCaseData.get(LONDON_FRC_LIST));
        assertEquals(UXBRIDGE, migratedCaseData.get(LONDON_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_east_london() {
        //Given
        caseData.put(LONDON_FRC_LIST, CFC);
        caseData.put(CFC_COURTLIST, EAST_LONDON_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertEquals(LONDON, migratedCaseData.get(REGION));
        assertEquals(LONDONFRC, migratedCaseData.get(LONDON_FRC_LIST));
        assertEquals(EAST_LONDON, migratedCaseData.get(LONDON_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_brentford() {
        //Given
        caseData.put(LONDON_FRC_LIST, CFC);
        caseData.put(CFC_COURTLIST, BRENTFORD_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertEquals(LONDON, migratedCaseData.get(REGION));
        assertEquals(LONDONFRC, migratedCaseData.get(LONDON_FRC_LIST));
        assertEquals(BRENTFORD, migratedCaseData.get(LONDON_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_barnet() {
        //Given
        caseData.put(LONDON_FRC_LIST, CFC);
        caseData.put(CFC_COURTLIST, BARNET_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertEquals(LONDON, migratedCaseData.get(REGION));
        assertEquals(LONDONFRC, migratedCaseData.get(LONDON_FRC_LIST));
        assertEquals(BARNET, migratedCaseData.get(LONDON_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_romford() {
        //Given
        caseData.put(LONDON_FRC_LIST, CFC);
        caseData.put(CFC_COURTLIST, ROMFORD_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertEquals(LONDON, migratedCaseData.get(REGION));
        assertEquals(LONDONFRC, migratedCaseData.get(LONDON_FRC_LIST));
        assertEquals(ROMFORD, migratedCaseData.get(LONDON_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_kingston_upon_thames() {
        //Given
        caseData.put(LONDON_FRC_LIST, CFC);
        caseData.put(CFC_COURTLIST, KINGSTON_UPON_THAMES_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertEquals(LONDON, migratedCaseData.get(REGION));
        assertEquals(LONDONFRC, migratedCaseData.get(LONDON_FRC_LIST));
        assertEquals(KINGSTON_UPON_THAMES, migratedCaseData.get(LONDON_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_edmonton() {
        //Given
        caseData.put(LONDON_FRC_LIST, CFC);
        caseData.put(CFC_COURTLIST, EDMONTON_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertEquals(LONDON, migratedCaseData.get(REGION));
        assertEquals(LONDONFRC, migratedCaseData.get(LONDON_FRC_LIST));
        assertEquals(EDMONTON, migratedCaseData.get(LONDON_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_croydon() {
        //Given
        caseData.put(LONDON_FRC_LIST, CFC);
        caseData.put(CFC_COURTLIST, CROYDON_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertEquals(LONDON, migratedCaseData.get(REGION));
        assertEquals(LONDONFRC, migratedCaseData.get(LONDON_FRC_LIST));
        assertEquals(CROYDON, migratedCaseData.get(LONDON_COURTLIST));
    }

    @Test
    public void shouldMigrateCase_bromley() {
        //Given
        caseData.put(LONDON_FRC_LIST, CFC);
        caseData.put(CFC_COURTLIST, BROMLEY_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertEquals(LONDON, migratedCaseData.get(REGION));
        assertEquals(LONDONFRC, migratedCaseData.get(LONDON_FRC_LIST));
        assertEquals(BROMLEY, migratedCaseData.get(LONDON_COURTLIST));
    }
}