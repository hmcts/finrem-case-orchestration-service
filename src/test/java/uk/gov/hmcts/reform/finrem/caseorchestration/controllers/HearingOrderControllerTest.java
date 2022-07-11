package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ContestedOrderApprovedLetterService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.ccd.callback.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest;
import uk.gov.hmcts.reform.finrem.ccd.domain.DraftDirectionOrder;

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

@WebMvcTest(HearingOrderController.class)
public class HearingOrderControllerTest extends BaseControllerTest {

    @Autowired HearingOrderController hearingOrderController;

    @MockBean private HearingOrderService hearingOrderService;
    @MockBean private ContestedOrderApprovedLetterService contestedOrderApprovedLetterService;
    @MockBean private IdamService idamService;
    @MockBean private CaseDataService caseDataService;

    @Test
    public void whenStoreHearingOrder_expectedServicesAreInvoked() throws JsonProcessingException {
        hearingOrderController.storeHearingOrder(AUTH_TOKEN, buildCallbackRequestString());

        verify(hearingOrderService).convertToPdfAndStampAndStoreLatestDraftHearingOrder(any(), eq(AUTH_TOKEN));
        verify(caseDataService).moveCollection(any(), eq(DRAFT_DIRECTION_DETAILS_COLLECTION), eq(DRAFT_DIRECTION_DETAILS_COLLECTION_RO));
    }

    @Test
    public void givenDraftDirectionOrderCollectionIsNotEmpty_whenStartingHearingOrderApproval_thenLatestDraftDirOrderIsPopulated() throws JsonProcessingException {
        DraftDirectionOrder draftDirectionOrder = DraftDirectionOrder.builder().build();
        when(hearingOrderService.draftDirectionOrderCollectionTail(any())).thenReturn(Optional.of(draftDirectionOrder));

        ResponseEntity<AboutToStartOrSubmitCallbackResponse> response = hearingOrderController.startHearingOrderApproval(AUTH_TOKEN,
            buildCallbackRequestString());

        assertThat(response.getBody().getData().getDraftDirectionWrapper().getLatestDraftDirectionOrder(),
            is(draftDirectionOrder));
    }

    @Test
    public void givenDraftDirectionOrderCollectionIsEmpty_whenStartingHearingOrderApproval_thenLatestDraftDirOrderIsCleared()
        throws JsonProcessingException {
        when(hearingOrderService.draftDirectionOrderCollectionTail(any())).thenReturn(Optional.empty());

        CallbackRequest callbackRequest = buildNewCallbackRequest();
        callbackRequest.getCaseDetails().getCaseData().getDraftDirectionWrapper().setLatestDraftDirectionOrder(DraftDirectionOrder.builder().build());
        ResponseEntity<AboutToStartOrSubmitCallbackResponse> response = hearingOrderController.startHearingOrderApproval(AUTH_TOKEN,
           objectMapper.writeValueAsString(callbackRequest));

        assertThat(response.getBody().getData().getDraftDirectionWrapper().getLatestDraftDirectionOrder(), is(nullValue()));
    }

    @Test
    public void givenLatestDraftDirectionOrderOverridesSolicitorCollection_whenStoringApprovedOrder_thenItIsAppendedToJudgesAmendedOrders()
        throws JsonProcessingException {
        when(hearingOrderService.latestDraftDirectionOrderOverridesSolicitorCollection(any())).thenReturn(true);

        hearingOrderController.storeApprovedHearingOrder(AUTH_TOKEN, buildCallbackRequestString());

        verify(hearingOrderService).convertToPdfAndStampAndStoreLatestDraftHearingOrder(any(), eq(AUTH_TOKEN));
        verify(caseDataService).moveCollection(any(), eq(DRAFT_DIRECTION_DETAILS_COLLECTION), eq(DRAFT_DIRECTION_DETAILS_COLLECTION_RO));
        verify(hearingOrderService).appendLatestDraftDirectionOrderToJudgesAmendedDirectionOrders(any());
    }

    @Test
    public void givenLatestDraftDirectionOrderDoesntOverrideSolicitorCollection_whenStoringApprovedOrder_thenItIsNotAppendedToJudgesAmendedOrders()
        throws JsonProcessingException {
        when(hearingOrderService.latestDraftDirectionOrderOverridesSolicitorCollection(any())).thenReturn(false);

        hearingOrderController.storeApprovedHearingOrder(AUTH_TOKEN, buildCallbackRequestString());

        verify(hearingOrderService, never()).appendLatestDraftDirectionOrderToJudgesAmendedDirectionOrders(any());
    }
}
