package uk.gov.hmcts.reform.finrem.caseorchestration.handler.processorder;

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
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed.AgreedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed.AgreedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocumentReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.PsaDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.PsaDocumentReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.WorkingHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AdditionalHearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.StampType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.draftorders.HasApprovableCollectionReader;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.ManageHearingActionService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus.APPROVED_BY_JUDGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus.PROCESSED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus.TO_BE_REVIEWED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.StampType.FAMILY_COURT_STAMP;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class ProcessOrderAboutToSubmitHandlerTest {

    private static final CaseDocument TARGET_DOCUMENT_1 = CaseDocument.builder().documentUrl("targetDoc1.docx").build();
    private static final CaseDocument TARGET_DOCUMENT_2 = CaseDocument.builder().documentUrl("targetDoc2.docx").build();
    private static final CaseDocument TARGET_DOCUMENT_3 = CaseDocument.builder().documentUrl("targetDoc3.docx").build();
    private static final CaseDocument TARGET_DOCUMENT_4 = CaseDocument.builder().documentUrl("targetDoc4.docx").build();
    private static final CaseDocument STAMPED_DOCUMENT_1 = CaseDocument.builder().documentFilename("stamped1.pdf").build();
    private static final CaseDocument STAMPED_DOCUMENT_2 = CaseDocument.builder().documentFilename("stamped2.pdf").build();
    private static final CaseDocument STAMPED_DOCUMENT_3 = CaseDocument.builder().documentFilename("stamped3.pdf").build();

    private static final CaseDocument ADDITIONAL_DOCUMENT_1 = CaseDocument.builder().documentUrl("additionalDoc1.docx").build();

    private static final CaseDocument CONVERTED_DOCUMENT_1 = CaseDocument.builder().documentFilename("additionalDoc1.pdf").build();

    private static final long CASE_ID = 12345678L;

    @InjectMocks
    private ProcessOrderAboutToSubmitHandler underTest;
    @Spy
    private HasApprovableCollectionReader hasApprovableCollectionReader;
    @Mock
    private AdditionalHearingDocumentService additionalHearingDocumentService;
    @Mock
    private GenericDocumentService genericDocumentService;
    @Mock
    private DocumentHelper documentHelper;
    @Mock
    private ManageHearingActionService manageHearingActionService;

    @Test
    void testCanHandle() {
        assertCanHandle(underTest, CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.PROCESS_ORDER);
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
        when(documentHelper.getStampType(any(FinremCaseData.class))).thenReturn(FAMILY_COURT_STAMP);
        mockDocumentStamping(TARGET_DOCUMENT_1, STAMPED_DOCUMENT_1);
        mockDocumentStamping(TARGET_DOCUMENT_2, STAMPED_DOCUMENT_2);

        List<DirectionOrderCollection> uploadHearingOrder = nullExistingUploadHearingOrder ? null : new ArrayList<>();
        DirectionOrderCollection expectedNewDirectionOrderCollection;

        FinremCallbackRequest finremCallbackRequest = FinremCallbackRequestFactory.from(CASE_ID, FinremCaseData.builder()
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

        assertThat(res.getData().getUploadHearingOrder()).containsExactly(expectedNewDirectionOrderCollection);
    }

    @Test
    void shouldInsertNewDocumentFromUnprocessedApprovedDocumentsToUploadHearingOrdersWithExistingUploadHearingOrder() {
        when(documentHelper.getStampType(any(FinremCaseData.class))).thenReturn(FAMILY_COURT_STAMP);
        mockDocumentStamping(TARGET_DOCUMENT_1, STAMPED_DOCUMENT_1);
        mockDocumentStamping(TARGET_DOCUMENT_2, STAMPED_DOCUMENT_2);

        List<DirectionOrderCollection> uploadHearingOrder = new ArrayList<>(List.of(
            DirectionOrderCollection.builder().value(DirectionOrder.builder().uploadDraftDocument(TARGET_DOCUMENT_4).build()).build()
        ));
        DirectionOrderCollection expectedNewDirectionOrderCollection;

        FinremCallbackRequest finremCallbackRequest = FinremCallbackRequestFactory.from(12345678L, FinremCaseData.builder()
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

        assertThat(res.getData().getUploadHearingOrder())
            .hasSize(2)
            .contains(expectedNewDirectionOrderCollection);
        assertThat(res.getData().getDraftOrdersWrapper().getUnprocessedApprovedDocuments()).isNull();
        assertThat(res.getData().getDraftOrdersWrapper().getIsLegacyApprovedOrderPresent()).isNull();
        assertThat(res.getData().getDraftOrdersWrapper().getIsUnprocessedApprovedDocumentPresent()).isNull();
    }

    @Test
    void givenUnprocessedOrdersWithAdditionalAttachments_whenHandle_shouldProcessAndConvertDocumentsToPdf() {
        when(documentHelper.getStampType(any(FinremCaseData.class))).thenReturn(FAMILY_COURT_STAMP);
        mockDocumentStamping(TARGET_DOCUMENT_1, STAMPED_DOCUMENT_1);
        mockDocumentStamping(TARGET_DOCUMENT_2, STAMPED_DOCUMENT_2);
        mockDocumentStamping(TARGET_DOCUMENT_3, STAMPED_DOCUMENT_3);

        when(genericDocumentService.convertDocumentIfNotPdfAlready(ADDITIONAL_DOCUMENT_1, AUTH_TOKEN, String.valueOf(CASE_ID)))
            .thenReturn(CONVERTED_DOCUMENT_1);

        DraftOrderDocReviewCollection test1;
        DraftOrderDocReviewCollection test2;
        AgreedDraftOrderCollection test3;
        AgreedDraftOrderCollection test4;
        AgreedDraftOrderCollection test5;
        AgreedDraftOrderCollection test6;
        AgreedDraftOrderCollection test7;

        FinremCallbackRequest finremCallbackRequest = FinremCallbackRequestFactory.from(CASE_ID, FinremCaseData.builder()
            .draftOrdersWrapper(DraftOrdersWrapper.builder()
                .unprocessedApprovedDocuments(List.of(
                    DirectionOrderCollection.builder().value(DirectionOrder.builder().originalDocument(TARGET_DOCUMENT_1)
                        .uploadDraftDocument(TARGET_DOCUMENT_1).build()).build(),
                    DirectionOrderCollection.builder().value(DirectionOrder.builder().originalDocument(TARGET_DOCUMENT_2)
                        .uploadDraftDocument(TARGET_DOCUMENT_2).build()).build(),
                    DirectionOrderCollection.builder().value(DirectionOrder.builder().originalDocument(TARGET_DOCUMENT_3)
                        .uploadDraftDocument(TARGET_DOCUMENT_3).build()).build()
                ))
                .agreedDraftOrderCollection(List.of(
                    test3 = AgreedDraftOrderCollection.builder().value(AgreedDraftOrder.builder()
                        .draftOrder(TARGET_DOCUMENT_1)
                        .attachments(List.of(DocumentCollectionItem.builder().value(ADDITIONAL_DOCUMENT_1).build()))
                        .orderStatus(APPROVED_BY_JUDGE).build()).build(),
                    test4 = AgreedDraftOrderCollection.builder().value(AgreedDraftOrder.builder().draftOrder(TARGET_DOCUMENT_2)
                        .orderStatus(APPROVED_BY_JUDGE).build()).build(),
                    test5 = AgreedDraftOrderCollection.builder().value(AgreedDraftOrder.builder().draftOrder(TARGET_DOCUMENT_3)
                        .orderStatus(APPROVED_BY_JUDGE).build()).build(),
                    test6 = AgreedDraftOrderCollection.builder().value(AgreedDraftOrder.builder().pensionSharingAnnex(TARGET_DOCUMENT_4)
                        .orderStatus(TO_BE_REVIEWED).build()).build()
                ))
                .intvAgreedDraftOrderCollection(List.of(
                    test7 = AgreedDraftOrderCollection.builder().value(AgreedDraftOrder.builder().draftOrder(TARGET_DOCUMENT_3)
                        .orderStatus(APPROVED_BY_JUDGE).build()).build()
                ))
                .draftOrdersReviewCollection(List.of(
                    DraftOrdersReviewCollection.builder().value(
                        DraftOrdersReview.builder()
                            .draftOrderDocReviewCollection(List.of(
                                test1 = buildDraftOrderDocReviewCollection(APPROVED_BY_JUDGE, TARGET_DOCUMENT_1, ADDITIONAL_DOCUMENT_1),
                                buildDraftOrderDocReviewCollection(TO_BE_REVIEWED)
                            ))
                            .build()).build(),
                    DraftOrdersReviewCollection.builder().value(
                        DraftOrdersReview.builder()
                            .draftOrderDocReviewCollection(List.of(
                                buildDraftOrderDocReviewCollection(PROCESSED),
                                test2 = buildDraftOrderDocReviewCollection(APPROVED_BY_JUDGE, TARGET_DOCUMENT_2, CONVERTED_DOCUMENT_1)
                            ))
                            .build()).build()
                ))
                .build())
            .build());

        underTest.handle(finremCallbackRequest, AUTH_TOKEN);

        verify(genericDocumentService, times(3)).stampDocument(any(CaseDocument.class), eq(AUTH_TOKEN),
            eq(StampType.FAMILY_COURT_STAMP), eq(String.valueOf(CASE_ID)));

        //check DraftOrderDocReviewCollection is updated
        assertEquals(STAMPED_DOCUMENT_1, test1.getValue().getDraftOrderDocument());
        assertEquals(PROCESSED, test1.getValue().getOrderStatus());
        assertEquals(CONVERTED_DOCUMENT_1, test1.getValue().getAttachments().get(0).getValue());
        assertEquals(STAMPED_DOCUMENT_2, test2.getValue().getDraftOrderDocument());
        assertEquals(PROCESSED, test2.getValue().getOrderStatus());

        //check AgreedDraftOrderCollection is updated
        assertEquals(STAMPED_DOCUMENT_1, test3.getValue().getDraftOrder());
        assertEquals(PROCESSED, test3.getValue().getOrderStatus());
        assertEquals(CONVERTED_DOCUMENT_1, test3.getValue().getAttachments().get(0).getValue());
        assertEquals(STAMPED_DOCUMENT_2, test4.getValue().getDraftOrder());
        assertEquals(PROCESSED, test4.getValue().getOrderStatus());
        assertEquals(STAMPED_DOCUMENT_3, test5.getValue().getDraftOrder());
        assertEquals(PROCESSED, test5.getValue().getOrderStatus());

        assertEquals(TO_BE_REVIEWED, test6.getValue().getOrderStatus());

        //check Intervener AgreedDraftOrderCollection is updated
        assertEquals(STAMPED_DOCUMENT_3, test7.getValue().getDraftOrder());
        assertEquals(PROCESSED, test7.getValue().getOrderStatus());
    }

    @Test
    void shouldMarkPsaCollectionProcessed() {
        when(documentHelper.getStampType(any(FinremCaseData.class))).thenReturn(FAMILY_COURT_STAMP);
        mockDocumentStamping(TARGET_DOCUMENT_1, STAMPED_DOCUMENT_1);
        mockDocumentStamping(TARGET_DOCUMENT_2, STAMPED_DOCUMENT_2);

        PsaDocReviewCollection test1;
        PsaDocReviewCollection test2;
        AgreedDraftOrderCollection test3;
        AgreedDraftOrderCollection test4;
        AgreedDraftOrderCollection test5;
        FinremCallbackRequest finremCallbackRequest = FinremCallbackRequestFactory.from(CASE_ID, FinremCaseData.builder()
            .draftOrdersWrapper(DraftOrdersWrapper.builder()
                .unprocessedApprovedDocuments(List.of(
                    DirectionOrderCollection.builder().value(DirectionOrder.builder().originalDocument(TARGET_DOCUMENT_1)
                        .uploadDraftDocument(TARGET_DOCUMENT_1).build()).build(),
                    DirectionOrderCollection.builder().value(DirectionOrder.builder().originalDocument(TARGET_DOCUMENT_2)
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

        verify(genericDocumentService, times(2)).stampDocument(any(CaseDocument.class), eq(AUTH_TOKEN),
            eq(StampType.FAMILY_COURT_STAMP), eq(String.valueOf(CASE_ID)));
        //Check PsaDocReviewCollection is updated
        assertEquals(STAMPED_DOCUMENT_1, test1.getValue().getPsaDocument());
        assertEquals(PROCESSED, test1.getValue().getOrderStatus());
        assertEquals(STAMPED_DOCUMENT_2, test2.getValue().getPsaDocument());
        assertEquals(PROCESSED, test2.getValue().getOrderStatus());

        //Check AgreedDraftOrderCollection is updated
        assertEquals(STAMPED_DOCUMENT_1, test3.getValue().getPensionSharingAnnex());
        assertEquals(PROCESSED, test3.getValue().getOrderStatus());
        assertEquals(STAMPED_DOCUMENT_2, test4.getValue().getPensionSharingAnnex());
        assertEquals(PROCESSED, test4.getValue().getOrderStatus());

        assertEquals(APPROVED_BY_JUDGE, test5.getValue().getOrderStatus());
    }

    @Test
    void shouldReplaceApprovedDocumentAndMarkAsProcessed() {
        when(documentHelper.getStampType(any(FinremCaseData.class))).thenReturn(FAMILY_COURT_STAMP);
        mockDocumentStamping(TARGET_DOCUMENT_3, STAMPED_DOCUMENT_1);
        mockDocumentStamping(TARGET_DOCUMENT_4, STAMPED_DOCUMENT_2);

        PsaDocReviewCollection test1;
        PsaDocReviewCollection test2;
        AgreedDraftOrderCollection test3;
        AgreedDraftOrderCollection test4;

        FinremCallbackRequest finremCallbackRequest = FinremCallbackRequestFactory.from(CASE_ID, FinremCaseData.builder()
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

        verify(genericDocumentService, times(2)).stampDocument(any(CaseDocument.class), eq(AUTH_TOKEN),
            eq(StampType.FAMILY_COURT_STAMP), any(String.class));
        //Check PsaDocReviewCollection is updated
        assertEquals(PROCESSED, test1.getValue().getOrderStatus());
        assertEquals(STAMPED_DOCUMENT_1, test1.getValue().getPsaDocument());
        assertEquals(PROCESSED, test2.getValue().getOrderStatus());
        assertEquals(STAMPED_DOCUMENT_2, test2.getValue().getPsaDocument());

        assertEquals(PROCESSED, test3.getValue().getOrderStatus());
        assertEquals(STAMPED_DOCUMENT_1, test3.getValue().getPensionSharingAnnex());
        assertEquals(PROCESSED, test4.getValue().getOrderStatus());
        assertEquals(STAMPED_DOCUMENT_2, test4.getValue().getPensionSharingAnnex());
    }

    @Test
    void shouldStampNewUploadedDocumentsFromUnprocessedApprovedDocuments() throws JsonProcessingException {
        when(documentHelper.getStampType(any(FinremCaseData.class))).thenReturn(FAMILY_COURT_STAMP);
        mockDocumentStamping(TARGET_DOCUMENT_1, STAMPED_DOCUMENT_1);
        mockDocumentStamping(TARGET_DOCUMENT_2, STAMPED_DOCUMENT_2);

        DirectionOrderCollection expectedNewDocument = DirectionOrderCollection.builder().value(DirectionOrder.builder()
            .uploadDraftDocument(TARGET_DOCUMENT_3).build()).build();

        FinremCallbackRequest finremCallbackRequest = FinremCallbackRequestFactory.from(CASE_ID, FinremCaseData.builder()
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
            .stampAndCollectOrderCollection(
                any(FinremCaseDetails.class),
                eq(AUTH_TOKEN));
        // mocking the valid invocation on createAndStoreAdditionalHearingDocuments
        lenient().doNothing().when(additionalHearingDocumentService)
            .stampAndCollectOrderCollection(
                argThat(a -> a.getData().getUploadHearingOrder().size() == 2 && a.getData().getUploadHearingOrder().contains(expectedNewDocument)),
                eq(AUTH_TOKEN));

        underTest.handle(finremCallbackRequest, AUTH_TOKEN);

        verify(genericDocumentService, times(2)).stampDocument(any(CaseDocument.class), eq(AUTH_TOKEN),
            eq(StampType.FAMILY_COURT_STAMP), eq(String.valueOf(CASE_ID)));
    }

    @Test
    void shouldCallManageHearingServiceForProcessOrderEvent() throws JsonProcessingException {
        FinremCaseData caseData = FinremCaseData.builder()
            .manageHearingsWrapper(ManageHearingsWrapper
                .builder()
                .isAddHearingChosen(YesOrNo.YES)
                .workingHearing(WorkingHearing
                    .builder()
                    .hearingTime("12:40")
                    .build())
                .build())
            .build();
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(EventType.PROCESS_ORDER, FinremCaseDetails.builder()
            .caseType(CaseType.CONTESTED)
            .data(caseData));

        final var response = underTest.handle(callbackRequest, AUTH_TOKEN);

        verify(additionalHearingDocumentService).stampAndCollectOrderCollection(callbackRequest.getCaseDetails(), AUTH_TOKEN);
        verify(additionalHearingDocumentService, never()).storeHearingNotice(callbackRequest.getCaseDetails(), AUTH_TOKEN);
        verify(manageHearingActionService).performAddHearing(callbackRequest.getCaseDetails(), AUTH_TOKEN);
        verify(manageHearingActionService).updateTabData(caseData);
        assertEquals(ManageHearingsAction.ADD_HEARING,
            response.getData().getManageHearingsWrapper().getManageHearingsActionSelection());
    }

    @Test
    void shouldNotCallManageHearingServiceForProcessOrderEvent_ifNoWorkingHearing() throws JsonProcessingException {
        FinremCaseData caseData = FinremCaseData.builder()
            .manageHearingsWrapper(ManageHearingsWrapper
                .builder()
                .isAddHearingChosen(YesOrNo.NO)
                .workingHearing(WorkingHearing
                    .builder()
                    .hearingTime("12:40")
                    .build())
                .build())
            .build();
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(EventType.PROCESS_ORDER, FinremCaseDetails.builder()
            .caseType(CaseType.CONTESTED)
            .data(caseData));

        final var response = underTest.handle(callbackRequest, AUTH_TOKEN);

        verify(additionalHearingDocumentService).stampAndCollectOrderCollection(callbackRequest.getCaseDetails(), AUTH_TOKEN);
        verify(additionalHearingDocumentService, never()).storeHearingNotice(callbackRequest.getCaseDetails(), AUTH_TOKEN);
        verify(manageHearingActionService, never()).performAddHearing(callbackRequest.getCaseDetails(), AUTH_TOKEN);
        verify(manageHearingActionService, never()).updateTabData(caseData);
        assertThat(response.getData().getManageHearingsWrapper().getWorkingHearing()).isNull();
    }

    private void mockDocumentStamping(CaseDocument originalDocument, CaseDocument stampedDocument) {
        when(genericDocumentService.stampDocument(originalDocument, AUTH_TOKEN, FAMILY_COURT_STAMP, String.valueOf(CASE_ID)))
            .thenReturn(stampedDocument);
    }

    private DraftOrderDocReviewCollection buildDraftOrderDocReviewCollection(OrderStatus orderStatus) {
        return buildDraftOrderDocReviewCollection(orderStatus, null, null);
    }

    private DraftOrderDocReviewCollection buildDraftOrderDocReviewCollection(
        OrderStatus orderStatus, CaseDocument document, CaseDocument additionalDocument) {

        return DraftOrderDocReviewCollection.builder()
            .value(DraftOrderDocumentReview.builder()
                .draftOrderDocument(document)
                .orderStatus(orderStatus)
                .attachments(additionalDocument != null
                    ? List.of(DocumentCollectionItem.builder().value(additionalDocument).build())
                    : Collections.emptyList())
                .build())
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
