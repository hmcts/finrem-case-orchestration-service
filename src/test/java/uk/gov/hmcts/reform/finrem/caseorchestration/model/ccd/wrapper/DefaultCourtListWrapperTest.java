package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ClevelandCourt;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

class DefaultCourtListWrapperTest {

    @Test
    void returnsClevelandCourtList_WhenNotNull() {
        ClevelandCourt court1 = mock(ClevelandCourt.class);
        DefaultCourtListWrapper wrapper = new DefaultCourtListWrapper();
        wrapper.setClevelandCourtList(court1);
        wrapper.setCleavelandCourtList(null);
        assertSame(court1, wrapper.getClevelandCourt());
    }

    @Test
    void returnsCleavelandCourtList_WhenClevelandCourtListIsNull() {
        ClevelandCourt court2 = mock(ClevelandCourt.class);
        DefaultCourtListWrapper wrapper = new DefaultCourtListWrapper();
        wrapper.setClevelandCourtList(null);
        wrapper.setCleavelandCourtList(court2);
        assertSame(court2, wrapper.getClevelandCourt());
    }

    @Test
    void returnsNull_WhenBothAreNull() {
        DefaultCourtListWrapper wrapper = new DefaultCourtListWrapper();
        wrapper.setClevelandCourtList(null);
        wrapper.setCleavelandCourtList(null);
        assertNull(wrapper.getClevelandCourt());
    }
}
