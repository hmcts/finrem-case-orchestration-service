package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ContestedOrderApprovedLetterService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DRAFT_DIRECTION_DETAILS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DRAFT_DIRECTION_DETAILS_COLLECTION_RO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LATEST_DRAFT_DIRECTION_ORDER;

@WebMvcTest(HearingOrderController.class)
public class HearingOrderControllerTest extends BaseControllerTest {

    @Autowired HearingOrderController hearingOrderController;

    @MockBean private HearingOrderService hearingOrderService;
    @MockBean private ContestedOrderApprovedLetterService contestedOrderApprovedLetterService;
    @MockBean private IdamService idamService;
    @MockBean private CaseDataService caseDataService;

    @Test
    public void whenStoreHearingOrder_expectedServicesAreInvoked() {
        hearingOrderController.storeHearingOrder(AUTH_TOKEN, buildCallbackRequest());

        verify(hearingOrderService).convertToPdfAndStampAndStoreLatestDraftHearingOrder(any(), eq(AUTH_TOKEN));
        verify(caseDataService).moveCollection(any(), eq(DRAFT_DIRECTION_DETAILS_COLLECTION), eq(DRAFT_DIRECTION_DETAILS_COLLECTION_RO));
    }

    @Test
    public void givenDraftDirectionOrderCollectionIsNotEmpty_whenStartingHearingOrderApproval_thenLatestDraftDirOrderIsPopulated() {
        DraftDirectionOrder draftDirectionOrder = DraftDirectionOrder.builder().build();
        when(hearingOrderService.draftDirectionOrderCollectionTail(any())).thenReturn(Optional.of(draftDirectionOrder));

        ResponseEntity<AboutToStartOrSubmitCallbackResponse> response = hearingOrderController.startHearingOrderApproval(AUTH_TOKEN,
            buildCallbackRequest());

        assertThat(response.getBody().getData().get(LATEST_DRAFT_DIRECTION_ORDER), is(draftDirectionOrder));
    }

    @Test
    public void givenDraftDirectionOrderCollectionIsEmpty_whenStartingHearingOrderApproval_thenLatestDraftDirOrderIsCleared() {
        when(hearingOrderService.draftDirectionOrderCollectionTail(any())).thenReturn(Optional.empty());

        CallbackRequest callbackRequest = buildCallbackRequest();
        callbackRequest.getCaseDetails().getData().put(LATEST_DRAFT_DIRECTION_ORDER, "any non-null value");
        ResponseEntity<AboutToStartOrSubmitCallbackResponse> response = hearingOrderController.startHearingOrderApproval(AUTH_TOKEN,
            callbackRequest);

        assertThat(response.getBody().getData().get(LATEST_DRAFT_DIRECTION_ORDER), is(nullValue()));
    }

    @Test
    public void givenLatestDraftDirectionOrderOverridesSolicitorCollection_whenStoringApprovedOrder_thenItIsAppendedToJudgesAmendedOrders() {
        when(hearingOrderService.latestDraftDirectionOrderOverridesSolicitorCollection(any())).thenReturn(true);

        hearingOrderController.storeApprovedHearingOrder(AUTH_TOKEN, buildCallbackRequest());

        verify(hearingOrderService).convertToPdfAndStampAndStoreLatestDraftHearingOrder(any(), eq(AUTH_TOKEN));
        verify(caseDataService).moveCollection(any(), eq(DRAFT_DIRECTION_DETAILS_COLLECTION), eq(DRAFT_DIRECTION_DETAILS_COLLECTION_RO));
        verify(hearingOrderService).appendLatestDraftDirectionOrderToJudgesAmendedDirectionOrders(any());
    }

    @Test
    public void givenLatestDraftDirectionOrderDoesntOverrideSolicitorCollection_whenStoringApprovedOrder_thenItIsNotAppendedToJudgesAmendedOrders() {
        when(hearingOrderService.latestDraftDirectionOrderOverridesSolicitorCollection(any())).thenReturn(false);

        hearingOrderController.storeApprovedHearingOrder(AUTH_TOKEN, buildCallbackRequest());

        verify(hearingOrderService, never()).appendLatestDraftDirectionOrderToJudgesAmendedDirectionOrders(any());
    }
}
