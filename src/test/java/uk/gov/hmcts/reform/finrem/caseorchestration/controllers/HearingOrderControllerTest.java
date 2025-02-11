package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ContestedOrderApprovedLetterService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.UploadedDraftOrderCategoriser;

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

    @Autowired
    HearingOrderController hearingOrderController;

    @MockitoBean
    private HearingOrderService hearingOrderService;
    @MockitoBean
    private ContestedOrderApprovedLetterService contestedOrderApprovedLetterService;
    @MockitoBean
    private IdamService idamService;
    @MockitoBean
    private CaseDataService caseDataService;
    @MockitoBean
    private FinremCaseDetailsMapper finremCaseDetailsMapper;
    @MockitoBean
    private UploadedDraftOrderCategoriser uploadedDraftOrderCategoriser;

    CallbackRequest request;
    CaseDetails caseDetails;
    FinremCaseDetails finremCaseDetails;

    @Before
    public void setup() {
        request = buildCallbackRequest();
        caseDetails = request.getCaseDetails();
        finremCaseDetails = buildFinremCallbackRequest().getCaseDetails();
    }

    @Test
    public void givenDraftDirectionOrderCollectionIsNotEmpty_whenStartingHearingOrderApproval_thenLatestDraftDirOrderIsPopulated() {
        DraftDirectionOrder draftDirectionOrder = DraftDirectionOrder.builder().build();
        when(hearingOrderService.draftDirectionOrderCollectionTail(any(), any())).thenReturn(Optional.of(draftDirectionOrder));

        ResponseEntity<AboutToStartOrSubmitCallbackResponse> response = hearingOrderController.startHearingOrderApproval(AUTH_TOKEN,
            buildCallbackRequest());

        assertThat(response.getBody().getData().get(LATEST_DRAFT_DIRECTION_ORDER), is(draftDirectionOrder));
    }

    @Test
    public void givenDraftDirectionOrderCollectionIsEmpty_whenStartingHearingOrderApproval_thenLatestDraftDirOrderIsCleared() {
        when(hearingOrderService.draftDirectionOrderCollectionTail(any(), any())).thenReturn(Optional.empty());

        CallbackRequest callbackRequest = buildCallbackRequest();
        callbackRequest.getCaseDetails().getData().put(LATEST_DRAFT_DIRECTION_ORDER, "any non-null value");
        ResponseEntity<AboutToStartOrSubmitCallbackResponse> response = hearingOrderController.startHearingOrderApproval(AUTH_TOKEN,
            callbackRequest);

        assertThat(response.getBody().getData().get(LATEST_DRAFT_DIRECTION_ORDER), is(nullValue()));
    }

    @Test
    public void givenLatestDraftDirectionOrderOverridesSolicitorCollection_whenStoringApprovedOrder_thenItIsAppendedToJudgesAmendedOrders() {
        when(finremCaseDetailsMapper.mapToCaseDetails(any())).thenReturn(caseDetails);
        when(finremCaseDetailsMapper.mapToFinremCaseDetails(any())).thenReturn(finremCaseDetails);
        when(hearingOrderService.latestDraftDirectionOrderOverridesSolicitorCollection(any(), any())).thenReturn(true);

        hearingOrderController.storeApprovedHearingOrder(AUTH_TOKEN, buildCallbackRequest());

        verify(hearingOrderService).convertToPdfAndStampAndStoreLatestDraftHearingOrder(any(), eq(AUTH_TOKEN));
        verify(caseDataService).moveCollection(any(), eq(DRAFT_DIRECTION_DETAILS_COLLECTION), eq(DRAFT_DIRECTION_DETAILS_COLLECTION_RO));
        verify(hearingOrderService).appendLatestDraftDirectionOrderToJudgesAmendedDirectionOrders(any(CaseDetails.class));
    }

    @Test
    public void givenLatestDraftDirectionOrderDoesntOverrideSolicitorCollection_whenStoringApprovedOrder_thenItIsNotAppendedToJudgesAmendedOrders() {
        when(finremCaseDetailsMapper.mapToCaseDetails(any())).thenReturn(caseDetails);
        when(finremCaseDetailsMapper.mapToFinremCaseDetails(any())).thenReturn(finremCaseDetails);
        when(hearingOrderService.latestDraftDirectionOrderOverridesSolicitorCollection(any(), any())).thenReturn(false);

        hearingOrderController.storeApprovedHearingOrder(AUTH_TOKEN, buildCallbackRequest());

        verify(hearingOrderService, never()).appendLatestDraftDirectionOrderToJudgesAmendedDirectionOrders(any(CaseDetails.class));
    }

    private FinremCallbackRequest buildFinremCallbackRequest() {
        FinremCaseData caseData = FinremCaseData.builder().build();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(123L).data(caseData).build();
        return FinremCallbackRequest.builder().eventType(EventType.APPROVE_ORDER).caseDetails(caseDetails).build();
    }
}
