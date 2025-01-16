package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.CourtDetailsParseException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.processorder.ProcessOrderService;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.DIRECTION_UPLOAD_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus.APPROVED_BY_JUDGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus.PROCESSED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus.TO_BE_REVIEWED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class DirectionUploadOrderAboutToSubmitHandlerTest {

    private static final CaseDocument TARGET_DOCUMENT_1 = CaseDocument.builder().documentUrl("targetDoc1.docx").build();

    private static final CaseDocument TARGET_DOCUMENT_2 = CaseDocument.builder().documentUrl("targetDoc2.docx").build();

    private static final CaseDocument TARGET_DOCUMENT_3 = CaseDocument.builder().documentUrl("targetDoc3.docx").build();

    private static final CaseDocument TARGET_DOCUMENT_4 = CaseDocument.builder().documentUrl("targetDoc4.docx").build();

    @InjectMocks
    private DirectionUploadOrderAboutToSubmitHandler underTest;

    @Spy
    private HasApprovableCollectionReader hasApprovableCollectionReader;

    @Mock
    private AdditionalHearingDocumentService additionalHearingDocumentService;

    @Mock
    private ProcessOrderService processOrderService;

    @Test
    void testCanHandle() {
        assertCanHandle(underTest, ABOUT_TO_SUBMIT, CONTESTED, DIRECTION_UPLOAD_ORDER);
    }

    @Test
    void createAndStoreAdditionalHearingDocumentsHandleException() throws JsonProcessingException {
        FinremCallbackRequest finremCallbackRequest = FinremCallbackRequestFactory.fromId(123L);
        FinremCaseDetails finremCaseDetails = finremCallbackRequest.getCaseDetails();
        doThrow(new CourtDetailsParseException()).when(additionalHearingDocumentService)
            .createAndStoreAdditionalHearingDocuments(finremCaseDetails, AUTH_TOKEN);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> res = underTest.handle(finremCallbackRequest, AUTH_TOKEN);
        assertEquals(1, res.getErrors().size());
        assertEquals("There was an unexpected error", res.getErrors().get(0));
        verify(additionalHearingDocumentService).createAndStoreAdditionalHearingDocuments(finremCallbackRequest.getCaseDetails(), AUTH_TOKEN);
    }

    @Test
    void createAndStoreAdditionalHearingDocuments() throws JsonProcessingException {
        FinremCallbackRequest finremCallbackRequest = FinremCallbackRequestFactory.fromId(123L);
        underTest.handle(finremCallbackRequest, AUTH_TOKEN);
        verify(additionalHearingDocumentService).createAndStoreAdditionalHearingDocuments(finremCallbackRequest.getCaseDetails(), AUTH_TOKEN);
    }

    @Test
    void shouldHandleUploadHearingOrdersWithoutUnprocessedDraftDocuments() {
        List<DirectionOrderCollection> uploadHearingOrder = new ArrayList<>(List.of(
            DirectionOrderCollection.builder().value(DirectionOrder.builder().uploadDraftDocument(TARGET_DOCUMENT_4).build()).build()
        ));

        FinremCallbackRequest finremCallbackRequest = FinremCallbackRequestFactory.from(FinremCaseData.builder()
            .uploadHearingOrder(uploadHearingOrder)
            .build());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> res = underTest.handle(finremCallbackRequest, AUTH_TOKEN);

        assertThat(res.getData().getUploadHearingOrder()).hasSize(1);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldInsertNewDocumentFromUnprocessedApprovedDocumentsToUploadHearingOrders(boolean nullExistingUploadHearingOrder) {
        List<DirectionOrderCollection> uploadHearingOrder = nullExistingUploadHearingOrder ? null : new ArrayList<>();
        DirectionOrderCollection expectedNewDirectionOrderCollection = null;

        FinremCallbackRequest finremCallbackRequest = FinremCallbackRequestFactory.from(FinremCaseData.builder()
            .uploadHearingOrder(uploadHearingOrder)
            .draftOrdersWrapper(DraftOrdersWrapper.builder()
                .unprocessedApprovedDocuments(List.of(
                    DirectionOrderCollection.builder().value(DirectionOrder.builder().originalDocument(TARGET_DOCUMENT_1)
                        .uploadDraftDocument(TARGET_DOCUMENT_1).build()).build(),
                    DirectionOrderCollection.builder().value(DirectionOrder.builder().originalDocument(TARGET_DOCUMENT_2)
                        .uploadDraftDocument(TARGET_DOCUMENT_2).build()).build(),
                    expectedNewDirectionOrderCollection = DirectionOrderCollection.builder().value(DirectionOrder.builder()
                        .uploadDraftDocument(TARGET_DOCUMENT_3).build()).build()
                ))
                .build())
            .build());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> res =  underTest.handle(finremCallbackRequest, AUTH_TOKEN);

        assertThat(res.getData().getUploadHearingOrder()).hasSize(1);
        assertThat(res.getData().getUploadHearingOrder().get(0)).isEqualTo(expectedNewDirectionOrderCollection);
    }

    @Test
    void shouldInsertNewDocumentFromUnprocessedApprovedDocumentsToUploadHearingOrdersWithExistingUploadHearingOrder() {
        List<DirectionOrderCollection> uploadHearingOrder = new ArrayList<>(List.of(
            DirectionOrderCollection.builder().value(DirectionOrder.builder().uploadDraftDocument(TARGET_DOCUMENT_4).build()).build()
        ));
        DirectionOrderCollection expectedNewDirectionOrderCollection = null;

        FinremCallbackRequest finremCallbackRequest = FinremCallbackRequestFactory.from(FinremCaseData.builder()
            .uploadHearingOrder(uploadHearingOrder)
            .draftOrdersWrapper(DraftOrdersWrapper.builder()
                .unprocessedApprovedDocuments(List.of(
                    DirectionOrderCollection.builder().value(DirectionOrder.builder().originalDocument(TARGET_DOCUMENT_1)
                        .uploadDraftDocument(TARGET_DOCUMENT_1).build()).build(),
                    DirectionOrderCollection.builder().value(DirectionOrder.builder().originalDocument(TARGET_DOCUMENT_2)
                        .uploadDraftDocument(TARGET_DOCUMENT_2).build()).build(),
                    expectedNewDirectionOrderCollection = DirectionOrderCollection.builder().value(DirectionOrder.builder()
                        .uploadDraftDocument(TARGET_DOCUMENT_3).build()).build()
                ))
                .build())
            .build());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> res = underTest.handle(finremCallbackRequest, AUTH_TOKEN);

        assertThat(res.getData().getUploadHearingOrder()).hasSize(2);
        assertThat(res.getData().getUploadHearingOrder().get(1)).isEqualTo(expectedNewDirectionOrderCollection);
        assertThat(res.getData().getDraftOrdersWrapper().getUnprocessedApprovedDocuments()).isNull();
    }

    @Test
    void shouldMarkDraftOrdersReviewCollectionProcessed() {
        DraftOrderDocReviewCollection test1 = null;
        DraftOrderDocReviewCollection test2 = null;
        AgreedDraftOrderCollection test3 = null;
        AgreedDraftOrderCollection test4 = null;
        AgreedDraftOrderCollection test5 = null;
        AgreedDraftOrderCollection test6 = null;
        CaseDocument stampedDocument = CaseDocument.builder().build();

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

        when(processOrderService.convertToPdfAndStampDocument(any(FinremCaseDetails.class), any(CaseDocument.class),
            any(String.class))).thenReturn(stampedDocument);

        underTest.handle(finremCallbackRequest, AUTH_TOKEN);

        verify(processOrderService, times(2)).convertToPdfAndStampDocument(any(FinremCaseDetails.class), any(CaseDocument.class), any(String.class));
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

        CaseDocument stampedDocument = CaseDocument.builder().build();

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

        when(processOrderService.convertToPdfAndStampDocument(any(FinremCaseDetails.class), any(CaseDocument.class),
            any(String.class))).thenReturn(stampedDocument);

        underTest.handle(finremCallbackRequest, AUTH_TOKEN);

        verify(processOrderService, times(2)).convertToPdfAndStampDocument(any(FinremCaseDetails.class), any(CaseDocument.class), any(String.class));
        assertEquals(PROCESSED, test1.getValue().getOrderStatus());
        assertEquals(stampedDocument, test1.getValue().getPsaDocument());
        assertEquals(PROCESSED, test2.getValue().getOrderStatus());
        assertEquals(stampedDocument, test2.getValue().getPsaDocument());
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

    @Test
    void shouldStampNewUploadedDocumentsFromUnprocessedApprovedDocuments() throws JsonProcessingException {
        DirectionOrderCollection expectedNewDocument = DirectionOrderCollection.builder().value(DirectionOrder.builder()
            .uploadDraftDocument(TARGET_DOCUMENT_3).build()).build();

        FinremCallbackRequest finremCallbackRequest = FinremCallbackRequestFactory.from(FinremCaseData.builder()
            .uploadHearingOrder(new ArrayList<>(List.of(
                DirectionOrderCollection.builder().value(DirectionOrder.builder().uploadDraftDocument(TARGET_DOCUMENT_4).build()).build()
            )))
            .draftOrdersWrapper(DraftOrdersWrapper.builder()
                .unprocessedApprovedDocuments(List.of(
                    DirectionOrderCollection.builder().value(DirectionOrder.builder().originalDocument(TARGET_DOCUMENT_1)
                        .uploadDraftDocument(TARGET_DOCUMENT_1).build()).build(),
                    DirectionOrderCollection.builder().value(DirectionOrder.builder().originalDocument(TARGET_DOCUMENT_2)
                        .uploadDraftDocument(TARGET_DOCUMENT_2).build()).build(),
                    expectedNewDocument
                ))
                .build())
            .build());

        lenient().doThrow(new AssertionError("Expected two documents to be processed, but only one was found"))
            .when(additionalHearingDocumentService)
            .createAndStoreAdditionalHearingDocuments(
                any(FinremCaseDetails.class),
                eq(AUTH_TOKEN));
        // mocking the valid invocation on createAndStoreAdditionalHearingDocuments
        lenient().doNothing().when(additionalHearingDocumentService)
            .createAndStoreAdditionalHearingDocuments(
                argThat(a -> a.getData().getUploadHearingOrder().size() == 2 && a.getData().getUploadHearingOrder().contains(expectedNewDocument)),
                eq(AUTH_TOKEN));

        underTest.handle(finremCallbackRequest, AUTH_TOKEN);
    }
}
