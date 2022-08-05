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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.serialisation.FinremCallbackRequestDeserializer;
import uk.gov.hmcts.reform.finrem.ccd.callback.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest;
import uk.gov.hmcts.reform.finrem.ccd.domain.DraftDirectionOrder;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.JudgeType;

import java.time.LocalDate;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@WebMvcTest(HearingOrderController.class)
public class HearingOrderControllerTest extends BaseControllerTest {

    @Autowired HearingOrderController hearingOrderController;

    @MockBean private HearingOrderService hearingOrderService;
    @MockBean private ContestedOrderApprovedLetterService contestedOrderApprovedLetterService;
    @MockBean private IdamService idamService;
    @MockBean private CaseDataService caseDataService;
    @MockBean private FinremCallbackRequestDeserializer deserializer;

    @Test
    public void whenStoreHearingOrder_expectedServicesAreInvoked() throws JsonProcessingException {
        loadRequestContentWith("/fixtures/bulkprint/bulk-print-additional-hearing.json");
        when(deserializer.deserialize(any()))
            .thenReturn(getCallbackRequest(requestContent.toString()));

        ResponseEntity<AboutToStartOrSubmitCallbackResponse> response =
            hearingOrderController.storeHearingOrder(AUTH_TOKEN, buildCallbackRequestString());

        assertThat(response.getBody().getData().getDraftDirectionWrapper().getDraftDirectionDetailsCollectionRO(),
            is(notNullValue()));

        verify(hearingOrderService).convertToPdfAndStampAndStoreLatestDraftHearingOrder(any(), eq(AUTH_TOKEN));
    }

    @Test
    public void givenDraftDirectionOrderCollectionIsNotEmpty_whenStartingHearingOrderApproval_thenLatestDraftDirOrderIsPopulated() throws JsonProcessingException {
        DraftDirectionOrder draftDirectionOrder = DraftDirectionOrder.builder().build();
        when(hearingOrderService.draftDirectionOrderCollectionTail(any())).thenReturn(Optional.of(draftDirectionOrder));
        when(deserializer.deserialize(any())).thenReturn(getCallbackRequest());

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

        when(deserializer.deserialize(any())).thenReturn(callbackRequest);

        ResponseEntity<AboutToStartOrSubmitCallbackResponse> response = hearingOrderController.startHearingOrderApproval(AUTH_TOKEN,
            objectMapper.writeValueAsString(callbackRequest));

        assertThat(response.getBody().getData().getDraftDirectionWrapper().getLatestDraftDirectionOrder(), is(nullValue()));
    }

    @Test
    public void givenLatestDraftDirectionOrderOverridesSolicitorCollection_whenStoringApprovedOrder_thenItIsAppendedToJudgesAmendedOrders()
        throws JsonProcessingException {
        when(hearingOrderService.latestDraftDirectionOrderOverridesSolicitorCollection(any())).thenReturn(true);
        when(deserializer.deserialize(any())).thenReturn(getCallbackRequest(buildCallbackRequestString()));

        hearingOrderController.storeApprovedHearingOrder(AUTH_TOKEN, buildCallbackRequestString());

        verify(hearingOrderService).convertToPdfAndStampAndStoreLatestDraftHearingOrder(any(), eq(AUTH_TOKEN));
        verify(hearingOrderService).appendLatestDraftDirectionOrderToJudgesAmendedDirectionOrders(any());
    }

    @Test
    public void givenLatestDraftDirectionOrderDoesntOverrideSolicitorCollection_whenStoringApprovedOrder_thenItIsNotAppendedToJudgesAmendedOrders()
        throws JsonProcessingException {
        when(hearingOrderService.latestDraftDirectionOrderOverridesSolicitorCollection(any())).thenReturn(false);
        when(deserializer.deserialize(any())).thenReturn(getCallbackRequest(buildCallbackRequestString()));

        hearingOrderController.storeApprovedHearingOrder(AUTH_TOKEN, buildCallbackRequestString());

        verify(hearingOrderService, never()).appendLatestDraftDirectionOrderToJudgesAmendedDirectionOrders(any());
    }

    @Test
    public void givenValidData_whenHearingOrderStart_thenRemoveFields() throws JsonProcessingException {
        CallbackRequest callbackRequest = getCallbackRequest(buildCallbackRequestString());
        callbackRequest.getCaseDetails().getCaseData().setOrderApprovedJudgeType(JudgeType.DISTRICT_JUDGE);
        callbackRequest.getCaseDetails().getCaseData().setOrderApprovedDate(LocalDate.now());
        when(idamService.getIdamFullName(eq(AUTH_TOKEN))).thenReturn("Test Judge Name");
        when(deserializer.deserialize(any())).thenReturn(callbackRequest);

        ResponseEntity<AboutToStartOrSubmitCallbackResponse> response =
            hearingOrderController.startHearingOrder(AUTH_TOKEN, buildCallbackRequestString());

        assertNotNull(response.getBody());
        FinremCaseData caseData = response.getBody().getData();

        assertThat(caseData.getOrderApprovedJudgeType(), is(nullValue()));
        assertThat(caseData.getOrderApprovedDate(), is(nullValue()));
        assertThat(caseData.getOrderApprovedJudgeName(), is("Test Judge Name"));
    }
}
