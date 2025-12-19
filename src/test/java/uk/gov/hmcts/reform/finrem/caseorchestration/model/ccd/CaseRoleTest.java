package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CaseRoleTest {

    @Test
    void testGetIntervenerSolicitorByIndex() {
        assertEquals(CaseRole.INTVR_SOLICITOR_1, CaseRole.getIntervenerSolicitorByIndex(1));
        assertEquals(CaseRole.INTVR_SOLICITOR_2, CaseRole.getIntervenerSolicitorByIndex(2));
        assertEquals(CaseRole.INTVR_SOLICITOR_3, CaseRole.getIntervenerSolicitorByIndex(3));
        assertEquals(CaseRole.INTVR_SOLICITOR_4, CaseRole.getIntervenerSolicitorByIndex(4));
    }

    @Test
    void testGetIntervenerBarristerByIndex() {
        assertEquals(CaseRole.INTVR_BARRISTER_1, CaseRole.getIntervenerBarristerByIndex(1));
        assertEquals(CaseRole.INTVR_BARRISTER_2, CaseRole.getIntervenerBarristerByIndex(2));
        assertEquals(CaseRole.INTVR_BARRISTER_3, CaseRole.getIntervenerBarristerByIndex(3));
        assertEquals(CaseRole.INTVR_BARRISTER_4, CaseRole.getIntervenerBarristerByIndex(4));
    }
}
