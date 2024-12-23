package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocumentReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.draftorders.HasApprovableCollectionReader;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus.APPROVED_BY_JUDGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus.PROCESSED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus.TO_BE_REVIEWED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class DirectionUploadOrderAboutToStartHandlerTest {

    @InjectMocks
    private DirectionUploadOrderAboutToStartHandler underTest;

    @Spy
    private HasApprovableCollectionReader hasApprovableCollectionReader;

    @Test
    void testCanHandle() {
        assertCanHandle(underTest, CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.DIRECTION_UPLOAD_ORDER);
    }

    @Test
    void shouldPopulateIsHavingOldDraftOrders() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest(FinremCaseData.builder()
            .uploadHearingOrder(List.of(DirectionOrderCollection.builder().build()))
            .build());
        assertEquals(YesOrNo.YES, underTest.handle(finremCallbackRequest, AUTH_TOKEN).getData().getDraftOrdersWrapper()
            .getIsLegacyApprovedOrderPresent());

        finremCallbackRequest = buildCallbackRequest(FinremCaseData.builder().build());
        assertEquals(YesOrNo.NO, underTest.handle(finremCallbackRequest, AUTH_TOKEN).getData().getDraftOrdersWrapper()
            .getIsLegacyApprovedOrderPresent());
        finremCallbackRequest = buildCallbackRequest(FinremCaseData.builder().uploadHearingOrder(List.of()).build());
        assertEquals(YesOrNo.NO, underTest.handle(finremCallbackRequest, AUTH_TOKEN).getData().getDraftOrdersWrapper()
            .getIsLegacyApprovedOrderPresent());
    }

    @Test
    void shouldPopulateUnprocessedApprovedDocuments() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest(FinremCaseData.builder()
            .draftOrdersWrapper(DraftOrdersWrapper.builder()
                .draftOrdersReviewCollection(List.of(
                    DraftOrdersReviewCollection.builder().value(
                        DraftOrdersReview.builder()
                            .draftOrderDocReviewCollection(List.of(
                               buildDraftOrderDocReviewCollection(APPROVED_BY_JUDGE),
                                buildDraftOrderDocReviewCollection(TO_BE_REVIEWED)
                            ))
                        .build()).build(),
                    DraftOrdersReviewCollection.builder().value(
                        DraftOrdersReview.builder()
                            .draftOrderDocReviewCollection(List.of(
                                buildDraftOrderDocReviewCollection(PROCESSED),
                                buildDraftOrderDocReviewCollection(TO_BE_REVIEWED)
                            ))
                            .build()).build()
                ))
                .build())
            .build());
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> result = underTest.handle(finremCallbackRequest, AUTH_TOKEN);
        assertThat(result.getData().getDraftOrdersWrapper().getUnprocessedApprovedDocuments()).hasSize(1);
        assertTrue(YesOrNo.isYes(result.getData().getDraftOrdersWrapper().getIsUnprocessedApprovedDocumentPresent()));
    }

    private DraftOrderDocReviewCollection buildDraftOrderDocReviewCollection(OrderStatus orderStatus) {
        return DraftOrderDocReviewCollection.builder()
            .value(DraftOrderDocumentReview.builder().orderStatus(orderStatus).build())
            .build();
    }

    private FinremCallbackRequest buildCallbackRequest(FinremCaseData finremCaseData) {
        return FinremCallbackRequest
            .builder()
            .eventType(EventType.DIRECTION_UPLOAD_ORDER)
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(finremCaseData).build())
            .build();
    }
}
