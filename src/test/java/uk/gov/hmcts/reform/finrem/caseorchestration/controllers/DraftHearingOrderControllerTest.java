package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.DraftHearingOrderService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DRAFT_DIRECTION_DETAILS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DRAFT_DIRECTION_DETAILS_COLLECTION_RO;

@RunWith(SpringRunner.class)
@WebMvcTest(DraftHearingOrderController.class)
public class DraftHearingOrderControllerTest extends BaseControllerTest {

    @Autowired DraftHearingOrderController draftHearingOrderController;

    @MockBean DraftHearingOrderService draftHearingOrderService;
    @MockBean CaseDataService caseDataService;

    @Test
    public void whenDraftHearingOrderBySolicitor_thenAppendToHearingOrderInvoked() {
        draftHearingOrderController.solicitorDraftHearingOrder(buildCallbackRequest());

        verify(draftHearingOrderService, times(1)).appendLatestDraftHearingOrderToHearingOrderCollection(any());
    }

    @Test
    public void whenDraftHearingOrderByJudge_thenCollectionMovedAndAppendToHearingOrderInvoked() {
        draftHearingOrderController.judgeDraftHearingOrder(buildCallbackRequest());

        verify(caseDataService, times(1)).moveCollection(any(), eq(DRAFT_DIRECTION_DETAILS_COLLECTION), eq(DRAFT_DIRECTION_DETAILS_COLLECTION_RO));
        verify(draftHearingOrderService, times(1)).appendLatestDraftHearingOrderToHearingOrderCollection(any());
    }
}
