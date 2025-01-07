package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocumentReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.draftorders.HasApprovableCollectionReader;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.processorder.ProcessOrderService;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.DIRECTION_UPLOAD_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus.APPROVED_BY_JUDGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus.PROCESSED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus.TO_BE_REVIEWED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class DirectionUploadOrderAboutToStartHandlerTest {

    private static final CaseDocument TARGET_DOCUMENT_1 = CaseDocument.builder().documentUrl("targetDoc1.docx").build();

    private static final LocalDateTime APPROVAL_DATE = LocalDateTime.of(2024, 12, 24,  23, 0, 0);

    @InjectMocks
    private DirectionUploadOrderAboutToStartHandler underTest;

    @Spy
    private HasApprovableCollectionReader hasApprovableCollectionReader = new HasApprovableCollectionReader();

    @Spy
    private ProcessOrderService processOrderService = new ProcessOrderService(hasApprovableCollectionReader);

    @Test
    void testCanHandle() {
        assertCanHandle(underTest, ABOUT_TO_START, CONTESTED, DIRECTION_UPLOAD_ORDER);
    }

    @Test
    void shouldPopulateIsHavingOldDraftOrders() {
        FinremCallbackRequest finremCallbackRequest = FinremCallbackRequestFactory.from(FinremCaseData.builder()
            .uploadHearingOrder(List.of(DirectionOrderCollection.builder().build()))
            .build());
        assertEquals(YesOrNo.YES, underTest.handle(finremCallbackRequest, AUTH_TOKEN).getData().getDraftOrdersWrapper()
            .getIsLegacyApprovedOrderPresent());

        finremCallbackRequest = FinremCallbackRequestFactory.from(FinremCaseData.builder().build());
        assertEquals(YesOrNo.NO, underTest.handle(finremCallbackRequest, AUTH_TOKEN).getData().getDraftOrdersWrapper()
            .getIsLegacyApprovedOrderPresent());
        finremCallbackRequest = FinremCallbackRequestFactory.from(FinremCaseData.builder().uploadHearingOrder(List.of()).build());
        assertEquals(YesOrNo.NO, underTest.handle(finremCallbackRequest, AUTH_TOKEN).getData().getDraftOrdersWrapper()
            .getIsLegacyApprovedOrderPresent());
    }

    @Test
    void shouldPopulateUnprocessedApprovedDocuments() {
        FinremCallbackRequest finremCallbackRequest = FinremCallbackRequestFactory.from(FinremCaseData.builder()
            .draftOrdersWrapper(DraftOrdersWrapper.builder()
                .draftOrdersReviewCollection(List.of(
                    DraftOrdersReviewCollection.builder().value(
                        DraftOrdersReview.builder()
                            .draftOrderDocReviewCollection(List.of(
                               buildDraftOrderDocReviewCollection(APPROVED_BY_JUDGE, TARGET_DOCUMENT_1, APPROVAL_DATE),
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
        assertThat(result.getData().getDraftOrdersWrapper().getUnprocessedApprovedDocuments())
            .hasSize(1)
            .containsOnly(
                DirectionOrderCollection.builder().value(DirectionOrder.builder()
                    .isOrderStamped(YesOrNo.NO)
                    .uploadDraftDocument(TARGET_DOCUMENT_1)
                    .originalDocument(TARGET_DOCUMENT_1)
                        .orderDateTime(APPROVAL_DATE)
                    .build()).build());
        assertTrue(YesOrNo.isYes(result.getData().getDraftOrdersWrapper().getIsUnprocessedApprovedDocumentPresent()));
    }

    private DraftOrderDocReviewCollection buildDraftOrderDocReviewCollection(OrderStatus orderStatus) {
        return buildDraftOrderDocReviewCollection(orderStatus, null, null);
    }

    private DraftOrderDocReviewCollection buildDraftOrderDocReviewCollection(OrderStatus orderStatus, CaseDocument caseDocument,
                                                                             LocalDateTime approvalDate) {
        return DraftOrderDocReviewCollection.builder()
            .value(DraftOrderDocumentReview.builder()
                .orderStatus(orderStatus)
                .draftOrderDocument(caseDocument)
                .approvalDate(approvalDate)
                .build())
            .build();
    }
}
