package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.DraftHearingOrderService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@WebMvcTest(DraftHearingOrderController.class)
public class DraftHearingOrderControllerTest extends BaseControllerTest {

    @Autowired DraftHearingOrderController draftHearingOrderController;

    @MockBean DraftHearingOrderService draftHearingOrderService;

    @Test
    public void whenDraftHearingOrderBySolicitor_thenAppendToHearingOrderInvoked() {
        draftHearingOrderController.solicitorDraftHearingOrder(buildCallbackRequest());

        verify(draftHearingOrderService, times(1)).appendLatestDraftHearingOrderToHearingOrderCollection(any());
    }
}
