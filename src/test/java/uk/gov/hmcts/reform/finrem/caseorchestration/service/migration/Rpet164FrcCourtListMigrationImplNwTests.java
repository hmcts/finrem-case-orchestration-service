package uk.gov.hmcts.reform.finrem.caseorchestration.service.migration;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BARROW;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BLACKBURN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BLACKPOOL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CARLISLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LANCASHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LANCASHIRE_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LANCASTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHWEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHWEST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NWOTHER_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.OTHER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PRESTON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REGION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WEST_CUMBRIA;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.BARROW_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.BLACKBURN_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.BLACKPOOL_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.BURNLEY_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.CARLISLE_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.LANCASTER_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.LONDON_TEMP;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.PRESTON_OLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl.WEST_CUMBRIA_OLD;

class Rpet164FrcCourtListMigrationImplNwTests {
    Map<String, Object> caseData = new HashMap<>();
    CaseDetails caseDetails = CaseDetails.builder().caseTypeId(CaseType.CONSENTED.getCcdType()).data(caseData).build();
    Rpet164FrcCourtListMigrationImpl classUnderTest = new Rpet164FrcCourtListMigrationImpl();

    @BeforeEach
    void setup() {
        //Given
        caseData.clear();
        caseData.put(REGION, NORTHWEST);
        caseData.put(NORTHWEST_FRC_LIST, OTHER);
    }

    @Test
    void shouldMigrateCase_preston() {
        //Given
        caseData.put(NWOTHER_COURTLIST, PRESTON_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(NWOTHER_COURTLIST));
        assertEquals(NORTHWEST, migratedCaseData.get(REGION));
        assertEquals(LANCASHIRE, migratedCaseData.get(NORTHWEST_FRC_LIST));
        assertEquals(PRESTON, migratedCaseData.get(LANCASHIRE_COURTLIST));
    }

    @Test
    void shouldMigrateCase_blackburn() {
        //Given
        caseData.put(NWOTHER_COURTLIST, BLACKBURN_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(NWOTHER_COURTLIST));
        assertEquals(NORTHWEST, migratedCaseData.get(REGION));
        assertEquals(LANCASHIRE, migratedCaseData.get(NORTHWEST_FRC_LIST));
        assertEquals(BLACKBURN, migratedCaseData.get(LANCASHIRE_COURTLIST));
    }

    @Test
    void shouldMigrateCase_blackpool() {
        //Given
        caseData.put(NWOTHER_COURTLIST, BLACKPOOL_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(NWOTHER_COURTLIST));
        assertEquals(NORTHWEST, migratedCaseData.get(REGION));
        assertEquals(LANCASHIRE, migratedCaseData.get(NORTHWEST_FRC_LIST));
        assertEquals(BLACKPOOL, migratedCaseData.get(LANCASHIRE_COURTLIST));
    }

    @Test
    void shouldMigrateCase_lancaster() {
        //Given
        caseData.put(NWOTHER_COURTLIST, LANCASTER_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(NWOTHER_COURTLIST));
        assertEquals(NORTHWEST, migratedCaseData.get(REGION));
        assertEquals(LANCASHIRE, migratedCaseData.get(NORTHWEST_FRC_LIST));
        assertEquals(LANCASTER, migratedCaseData.get(LANCASHIRE_COURTLIST));
    }

    @Test
    void shouldMigrateCase_barrow() {
        //Given
        caseData.put(NWOTHER_COURTLIST, BARROW_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(NWOTHER_COURTLIST));
        assertEquals(NORTHWEST, migratedCaseData.get(REGION));
        assertEquals(LANCASHIRE, migratedCaseData.get(NORTHWEST_FRC_LIST));
        assertEquals(BARROW, migratedCaseData.get(LANCASHIRE_COURTLIST));
    }

    @Test
    void shouldMigrateCase_carlisle() {
        //Given
        caseData.put(NWOTHER_COURTLIST, CARLISLE_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(NWOTHER_COURTLIST));
        assertEquals(NORTHWEST, migratedCaseData.get(REGION));
        assertEquals(LANCASHIRE, migratedCaseData.get(NORTHWEST_FRC_LIST));
        assertEquals(CARLISLE, migratedCaseData.get(LANCASHIRE_COURTLIST));
    }

    @Test
    void shouldMigrateCase_westCumbria() {
        //Given
        caseData.put(NWOTHER_COURTLIST, WEST_CUMBRIA_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(NWOTHER_COURTLIST));
        assertEquals(NORTHWEST, migratedCaseData.get(REGION));
        assertEquals(LANCASHIRE, migratedCaseData.get(NORTHWEST_FRC_LIST));
        assertEquals(WEST_CUMBRIA, migratedCaseData.get(LANCASHIRE_COURTLIST));
    }

    @Test
    void shouldMigrateCase_burnley_to_temp() {
        //Given
        caseData.put(NWOTHER_COURTLIST, BURNLEY_OLD);

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(LANCASHIRE_COURTLIST));
        assertNull(migratedCaseData.get(NWOTHER_COURTLIST));
        assertNull(migratedCaseData.get(NORTHWEST_FRC_LIST));
        assertEquals(LONDON, migratedCaseData.get(REGION));
        assertEquals(LONDON, migratedCaseData.get(LONDON_FRC_LIST));
        assertEquals(LONDON_TEMP, migratedCaseData.get(LONDON_COURTLIST));
    }

    @Test
    void shouldNotMigrateCase_unknown_to_temp() {
        //Given
        caseData.put(NWOTHER_COURTLIST, "some_unknown_code");

        //When
        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        //Then
        assertNull(migratedCaseData.get(LANCASHIRE_COURTLIST));
        assertNull(migratedCaseData.get(NWOTHER_COURTLIST));
        assertNull(migratedCaseData.get(NORTHWEST_FRC_LIST));
        assertEquals(LONDON, migratedCaseData.get(REGION));
        assertEquals(LONDON, migratedCaseData.get(LONDON_FRC_LIST));
        assertEquals(LONDON_TEMP, migratedCaseData.get(LONDON_COURTLIST));
    }
}