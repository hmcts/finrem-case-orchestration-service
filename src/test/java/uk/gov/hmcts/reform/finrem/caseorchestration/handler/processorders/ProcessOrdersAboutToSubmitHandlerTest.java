package uk.gov.hmcts.reform.finrem.caseorchestration.handler.processorders;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed.AgreedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed.AgreedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocumentReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.PsaDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.PsaDocumentReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AdditionalHearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.draftorders.HasApprovableCollectionReader;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus.APPROVED_BY_JUDGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus.PROCESSED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus.TO_BE_REVIEWED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class ProcessOrdersAboutToSubmitHandlerTest {

    private static final CaseDocument TARGET_DOCUMENT_1 = CaseDocument.builder().documentUrl("targetDoc1.docx").build();

    private static final CaseDocument TARGET_DOCUMENT_2 = CaseDocument.builder().documentUrl("targetDoc2.docx").build();

    private static final CaseDocument TARGET_DOCUMENT_3 = CaseDocument.builder().documentUrl("targetDoc3.docx").build();

    private static final CaseDocument TARGET_DOCUMENT_4 = CaseDocument.builder().documentUrl("targetDoc4.docx").build();

    @InjectMocks
    private ProcessOrdersAboutToSubmitHandler underTest;

    @Spy
    private HasApprovableCollectionReader hasApprovableCollectionReader;

    @Mock
    private AdditionalHearingDocumentService additionalHearingDocumentService;

    @Test
    void testCanHandle() {
        assertCanHandle(underTest, CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.PROCESS_ORDER);
    }

    @Test
    void shouldMarkDraftOrdersReviewCollectionProcessed() {
        DraftOrderDocReviewCollection test1 = null;
        DraftOrderDocReviewCollection test2 = null;
        AgreedDraftOrderCollection test3 = null;
        AgreedDraftOrderCollection test4 = null;
        AgreedDraftOrderCollection test5 = null;
        AgreedDraftOrderCollection test6 = null;

        FinremCallbackRequest finremCallbackRequest = FinremCallbackRequestFactory.from(FinremCaseData.builder()
            .draftOrdersWrapper(DraftOrdersWrapper.builder()
                .unprocessedApprovedDocuments(List.of(
                    DirectionOrderCollection.builder().value(DirectionOrder.builder().originalDocument(TARGET_DOCUMENT_1)
                        .uploadDraftDocument(TARGET_DOCUMENT_1).build()).build(),
                    DirectionOrderCollection.builder().value(DirectionOrder.builder().originalDocument(TARGET_DOCUMENT_2)
                        .uploadDraftDocument(TARGET_DOCUMENT_2).build()).build()
                ))
                .agreedDraftOrderCollection(List.of(
                    test3 = AgreedDraftOrderCollection.builder().value(AgreedDraftOrder.builder().draftOrder(TARGET_DOCUMENT_1)
                        .orderStatus(APPROVED_BY_JUDGE).build()).build(),
                    test4 = AgreedDraftOrderCollection.builder().value(AgreedDraftOrder.builder().draftOrder(TARGET_DOCUMENT_2)
                        .orderStatus(APPROVED_BY_JUDGE).build()).build(),
                    test5 = AgreedDraftOrderCollection.builder().value(AgreedDraftOrder.builder().draftOrder(TARGET_DOCUMENT_3)
                        .orderStatus(APPROVED_BY_JUDGE).build()).build(),
                    test6 = AgreedDraftOrderCollection.builder().value(AgreedDraftOrder.builder().pensionSharingAnnex(TARGET_DOCUMENT_4)
                        .orderStatus(TO_BE_REVIEWED).build()).build()
                ))
                .draftOrdersReviewCollection(List.of(
                    DraftOrdersReviewCollection.builder().value(
                        DraftOrdersReview.builder()
                            .draftOrderDocReviewCollection(List.of(
                                test1 = buildDraftOrderDocReviewCollection(APPROVED_BY_JUDGE, TARGET_DOCUMENT_1),
                                buildDraftOrderDocReviewCollection(TO_BE_REVIEWED)
                            ))
                            .build()).build(),
                    DraftOrdersReviewCollection.builder().value(
                        DraftOrdersReview.builder()
                            .draftOrderDocReviewCollection(List.of(
                                buildDraftOrderDocReviewCollection(PROCESSED),
                                test2 = buildDraftOrderDocReviewCollection(APPROVED_BY_JUDGE, TARGET_DOCUMENT_2)
                            ))
                            .build()).build()
                ))
                .build())
            .build());

        underTest.handle(finremCallbackRequest, AUTH_TOKEN);

        assertEquals(PROCESSED, test1.getValue().getOrderStatus());
        assertEquals(PROCESSED, test2.getValue().getOrderStatus());
        assertEquals(PROCESSED, test3.getValue().getOrderStatus());
        assertEquals(PROCESSED, test4.getValue().getOrderStatus());
        assertEquals(APPROVED_BY_JUDGE, test5.getValue().getOrderStatus());
        assertEquals(TO_BE_REVIEWED, test6.getValue().getOrderStatus());
    }

    @Test
    void shouldMarkPsaCollectionProcessed() {
        PsaDocReviewCollection test1 = null;
        PsaDocReviewCollection test2 = null;
        AgreedDraftOrderCollection test3 = null;
        AgreedDraftOrderCollection test4 = null;
        AgreedDraftOrderCollection test5 = null;

        FinremCallbackRequest finremCallbackRequest = FinremCallbackRequestFactory.from(FinremCaseData.builder()
            .draftOrdersWrapper(DraftOrdersWrapper.builder()
                .unprocessedApprovedDocuments(List.of(
                    DirectionOrderCollection.builder().value(DirectionOrder.builder()
                        .originalDocument(TARGET_DOCUMENT_1)
                        .uploadDraftDocument(TARGET_DOCUMENT_1).build()).build(),
                    DirectionOrderCollection.builder().value(DirectionOrder.builder()
                        .originalDocument(TARGET_DOCUMENT_2)
                        .uploadDraftDocument(TARGET_DOCUMENT_2).build()).build()
                ))
                .agreedDraftOrderCollection(List.of(
                    test3 = AgreedDraftOrderCollection.builder().value(AgreedDraftOrder.builder().pensionSharingAnnex(TARGET_DOCUMENT_1)
                        .orderStatus(APPROVED_BY_JUDGE).build()).build(),
                    test4 = AgreedDraftOrderCollection.builder().value(AgreedDraftOrder.builder().pensionSharingAnnex(TARGET_DOCUMENT_2)
                        .orderStatus(APPROVED_BY_JUDGE).build()).build(),
                    test5 = AgreedDraftOrderCollection.builder().value(AgreedDraftOrder.builder().pensionSharingAnnex(TARGET_DOCUMENT_3)
                        .orderStatus(APPROVED_BY_JUDGE).build()).build()
                ))
                .draftOrdersReviewCollection(List.of(
                    DraftOrdersReviewCollection.builder().value(
                        DraftOrdersReview.builder()
                            .psaDocReviewCollection(List.of(
                                test1 = buildPsaDocReviewCollection(APPROVED_BY_JUDGE, TARGET_DOCUMENT_1),
                                buildPsaDocReviewCollection(TO_BE_REVIEWED)
                            ))
                            .build()).build(),
                    DraftOrdersReviewCollection.builder().value(
                        DraftOrdersReview.builder()
                            .psaDocReviewCollection(List.of(
                                buildPsaDocReviewCollection(PROCESSED),
                                test2 = buildPsaDocReviewCollection(APPROVED_BY_JUDGE, TARGET_DOCUMENT_2)
                            ))
                            .build()).build()
                ))
                .build())
            .build());

        underTest.handle(finremCallbackRequest, AUTH_TOKEN);

        assertEquals(PROCESSED, test1.getValue().getOrderStatus());
        assertEquals(PROCESSED, test2.getValue().getOrderStatus());
        assertEquals(PROCESSED, test3.getValue().getOrderStatus());
        assertEquals(PROCESSED, test4.getValue().getOrderStatus());
        assertEquals(APPROVED_BY_JUDGE, test5.getValue().getOrderStatus());
    }

    @Test
    void shouldReplaceApprovedDocumentAndMarkAsProcessed() {
        PsaDocReviewCollection test1 = null;
        PsaDocReviewCollection test2 = null;
        AgreedDraftOrderCollection test3 = null;
        AgreedDraftOrderCollection test4 = null;

        FinremCallbackRequest finremCallbackRequest = FinremCallbackRequestFactory.from(FinremCaseData.builder()
            .draftOrdersWrapper(DraftOrdersWrapper.builder()
                .unprocessedApprovedDocuments(List.of(
                    DirectionOrderCollection.builder().value(DirectionOrder.builder().originalDocument(TARGET_DOCUMENT_1)
                        .uploadDraftDocument(TARGET_DOCUMENT_3).build()).build(),
                    DirectionOrderCollection.builder().value(DirectionOrder.builder().originalDocument(TARGET_DOCUMENT_2)
                        .uploadDraftDocument(TARGET_DOCUMENT_4).build()).build()
                ))
                .agreedDraftOrderCollection(List.of(
                    test3 = AgreedDraftOrderCollection.builder().value(AgreedDraftOrder.builder().pensionSharingAnnex(TARGET_DOCUMENT_1)
                        .orderStatus(APPROVED_BY_JUDGE).build()).build(),
                    test4 = AgreedDraftOrderCollection.builder().value(AgreedDraftOrder.builder().pensionSharingAnnex(TARGET_DOCUMENT_2)
                        .orderStatus(APPROVED_BY_JUDGE).build()).build()
                ))
                .draftOrdersReviewCollection(List.of(
                    DraftOrdersReviewCollection.builder().value(
                        DraftOrdersReview.builder()
                            .psaDocReviewCollection(List.of(
                                test1 = buildPsaDocReviewCollection(APPROVED_BY_JUDGE, TARGET_DOCUMENT_1),
                                buildPsaDocReviewCollection(TO_BE_REVIEWED)
                            ))
                            .build()).build(),
                    DraftOrdersReviewCollection.builder().value(
                        DraftOrdersReview.builder()
                            .psaDocReviewCollection(List.of(
                                buildPsaDocReviewCollection(PROCESSED),
                                test2 = buildPsaDocReviewCollection(APPROVED_BY_JUDGE, TARGET_DOCUMENT_2)
                            ))
                            .build()).build()
                ))
                .build())
            .build());

        underTest.handle(finremCallbackRequest, AUTH_TOKEN);

        assertEquals(PROCESSED, test1.getValue().getOrderStatus());
        assertEquals(TARGET_DOCUMENT_3, test1.getValue().getPsaDocument());
        assertEquals(PROCESSED, test2.getValue().getOrderStatus());
        assertEquals(TARGET_DOCUMENT_4, test2.getValue().getPsaDocument());
        assertEquals(PROCESSED, test3.getValue().getOrderStatus());
        assertEquals(PROCESSED, test4.getValue().getOrderStatus());
    }

    private DraftOrderDocReviewCollection buildDraftOrderDocReviewCollection(OrderStatus orderStatus) {
        return buildDraftOrderDocReviewCollection(orderStatus, null);
    }

    private DraftOrderDocReviewCollection buildDraftOrderDocReviewCollection(OrderStatus orderStatus, CaseDocument document) {
        return DraftOrderDocReviewCollection.builder()
            .value(DraftOrderDocumentReview.builder().draftOrderDocument(document).orderStatus(orderStatus).build())
            .build();
    }

    private PsaDocReviewCollection buildPsaDocReviewCollection(OrderStatus orderStatus) {
        return buildPsaDocReviewCollection(orderStatus, null);
    }

    private PsaDocReviewCollection buildPsaDocReviewCollection(OrderStatus orderStatus, CaseDocument document) {
        return PsaDocReviewCollection.builder()
            .value(PsaDocumentReview.builder().psaDocument(document).orderStatus(orderStatus).build())
            .build();
    }
}
