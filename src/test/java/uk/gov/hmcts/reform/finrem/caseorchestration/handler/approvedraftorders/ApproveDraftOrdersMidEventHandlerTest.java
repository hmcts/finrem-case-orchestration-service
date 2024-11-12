//package uk.gov.hmcts.reform.finrem.caseorchestration.handler.approvedraftorders;
//
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.junit.jupiter.params.ParameterizedTest;
//import org.junit.jupiter.params.provider.Arguments;
//import org.junit.jupiter.params.provider.MethodSource;
//import org.mockito.InjectMocks;
//import org.mockito.junit.jupiter.MockitoExtension;
//import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
//import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
//import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
//import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
//import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
//import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
//import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
//import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
//import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
//import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
//import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.CaseDocumentCollection;
//import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.ReviewableDraftOrder;
//import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.ReviewablePsa;
//import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocReviewCollection;
//import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocumentReview;
//import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReview;
//import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReviewCollection;
//import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.PsaDocReviewCollection;
//import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.PsaDocumentReview;
//import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
//
//import java.time.LocalDate;
//import java.util.Collections;
//import java.util.List;
//import java.util.stream.Stream;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
//import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;
//
//@ExtendWith(MockitoExtension.class)
//class ApproveDraftOrdersMidEventHandlerTest {
//
//    @InjectMocks
//    private ApproveDraftOrdersMidEventHandler handler;
//
//    @Test
//    void canHandle() {
//        assertCanHandle(handler, CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.APPROVE_ORDERS);
//    }
//
//    @ParameterizedTest(name = "{index} => draftOrdersWrapper={0}, expectedReviewableDraftOrders={1}, expectedReviewablePsa={2}")
//    @MethodSource("provideDraftOrderData")
//    @DisplayName("Test handle method with different DraftOrdersWrapper inputs")
//    void handle_withVariousDraftOrdersWrapperData(
//        DraftOrdersWrapper draftOrdersWrapper,
//        List<ReviewableDraftOrderCollection> expectedReviewableDraftOrders,
//        List<ReviewablePsaCollection> expectedReviewablePsa) {
//
//        // Arrange
//        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder()
//            .caseDetails(FinremCaseDetails.builder()
//                .id(12345L)
//                .data(FinremCaseData.builder()
//                    .draftOrdersWrapper(draftOrdersWrapper)
//                    .build())
//                .build())
//            .build();
//
//        // Act
//        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);
//
//        // Assert
//        assertNotNull(response);
//        FinremCaseData responseData = response.getData();
//        DraftOrdersWrapper responseDraftOrdersWrapper = responseData.getDraftOrdersWrapper();
//
//        assertNotNull(responseDraftOrdersWrapper.getJudgeApproval());
//        assertEquals(expectedReviewableDraftOrders, responseDraftOrdersWrapper.getJudgeApproval().getReviewableDraftOrderCollection());
//        assertEquals(expectedReviewablePsa, responseDraftOrdersWrapper.getJudgeApproval().getReviewablePsaCollection());
//    }
//
//    private static final String HEARING_ID_MATCH = "2024-10-31$$09:00$$hearingType$$Mr. Judge";
//
//    private static Stream<Arguments> provideDraftOrderData() {
//        CaseDocument sampleDraftOrderDocument = CaseDocument.builder().documentFilename("sampleDocument").build();
//        CaseDocument samplePsaDocument = CaseDocument.builder().documentFilename("samplePsaDocument").build();
//        CaseDocument sampleDraftOrderDocument2 = CaseDocument.builder().documentFilename("sampleDocument2").build();
//        CaseDocument samplePsaDocument2 = CaseDocument.builder().documentFilename("samplePsaDocument2").build();
//
//        return Stream.of(
//            // Test with empty DraftOrdersWrapper
//            Arguments.of(
//                DraftOrdersWrapper.builder().draftOrdersReviewCollection(Collections.emptyList()).build(),
//                Collections.emptyList(),
//                Collections.emptyList()
//            ),
//
//            // Test with non-empty DraftOrdersWrapper with one draft order and one PSA
//            Arguments.of(
//                DraftOrdersWrapper.builder()
//                    .hearingsReadyForReview(DynamicList.builder()
//                        .value(DynamicListElement.builder().code(HEARING_ID_MATCH).build())
//                        .build())
//                    .draftOrdersReviewCollection(List.of(
//                        DraftOrdersReviewCollection.builder()
//                            .value(DraftOrdersReview.builder()
//                                .hearingDate(LocalDate.of(2024, 10, 31))
//                                .hearingTime("09:00")
//                                .hearingType("hearingType")
//                                .hearingJudge("Mr. Judge")
//                                .draftOrderDocReviewCollection(List.of(
//                                    DraftOrderDocReviewCollection.builder()
//                                        .value(DraftOrderDocumentReview.builder()
//                                            .draftOrderDocument(sampleDraftOrderDocument)
//                                            .attachments(List.of(
//                                                CaseDocumentCollection.builder().value(CaseDocument.builder().documentFilename("attachment1").build())
//                                                    .build()))
//                                            .build())
//                                        .build()
//                                ))
//                                .psaDocReviewCollection(List.of(
//                                    PsaDocReviewCollection.builder()
//                                        .value(PsaDocumentReview.builder()
//                                            .psaDocument(samplePsaDocument)
//                                            .build())
//                                        .build()
//                                ))
//                                .build())
//                            .build()
//                    ))
//                    .build(),
//                List.of(ReviewableDraftOrderCollection.builder()
//                    .value(ReviewableDraftOrder.builder()
//                        .document(sampleDraftOrderDocument)
//                        .attachments(List.of(
//                            CaseDocumentCollection.builder().value(CaseDocument.builder().documentFilename("attachment1").build())
//                                .build()))
//                        .build())
//                    .build()),
//                List.of(ReviewablePsaCollection.builder()
//                    .value(ReviewablePsa.builder()
//                        .document(samplePsaDocument)
//                        .build())
//                    .build())
//            ),
//
//            // Test with multiple draft orders and PSAs
//            Arguments.of(
//                DraftOrdersWrapper.builder()
//                    .hearingsReadyForReview(DynamicList.builder()
//                        .value(DynamicListElement.builder().code(HEARING_ID_MATCH).build())
//                        .build())
//                    .draftOrdersReviewCollection(List.of(
//                        DraftOrdersReviewCollection.builder()
//                            .value(DraftOrdersReview.builder()
//                                .hearingDate(LocalDate.of(2024, 10, 31))
//                                .hearingTime("09:00")
//                                .hearingType("hearingType")
//                                .hearingJudge("Mr. Judge")
//                                .draftOrderDocReviewCollection(List.of(
//                                    DraftOrderDocReviewCollection.builder()
//                                        .value(DraftOrderDocumentReview.builder()
//                                            .draftOrderDocument(sampleDraftOrderDocument)
//                                            .attachments(List.of(
//                                                CaseDocumentCollection.builder().value(CaseDocument.builder().documentFilename("attachment1").build())
//                                                    .build(),
//                                                CaseDocumentCollection.builder().value(CaseDocument.builder().documentFilename("attachment2").build())
//                                                    .build()
//                                            ))
//                                            .build())
//                                        .build()
//                                ))
//                                .psaDocReviewCollection(List.of(
//                                    PsaDocReviewCollection.builder()
//                                        .value(PsaDocumentReview.builder()
//                                            .psaDocument(samplePsaDocument)
//                                            .build())
//                                        .build()
//                                ))
//                                .build())
//                            .build(),
//
//                        DraftOrdersReviewCollection.builder()
//                            .value(DraftOrdersReview.builder()
//                                .hearingDate(LocalDate.of(2024, 11, 30))
//                                .hearingTime("09:00")
//                                .hearingType("hearingType")
//                                .hearingJudge("Mr. Judge")
//                                .draftOrderDocReviewCollection(List.of(
//                                    DraftOrderDocReviewCollection.builder()
//                                        .value(DraftOrderDocumentReview.builder()
//                                            .draftOrderDocument(sampleDraftOrderDocument2)
//                                            .attachments(List.of(
//                                                CaseDocumentCollection.builder().value(CaseDocument.builder().documentFilename("attachment3").build())
//                                                    .build()
//                                            ))
//                                            .build())
//                                        .build()
//                                ))
//                                .psaDocReviewCollection(List.of(
//                                    PsaDocReviewCollection.builder()
//                                        .value(PsaDocumentReview.builder()
//                                            .psaDocument(samplePsaDocument2)
//                                            .build())
//                                        .build()
//                                ))
//                                .build())
//                            .build()
//                    ))
//                    .build(),
//                List.of(ReviewableDraftOrderCollection.builder()
//                    .value(ReviewableDraftOrder.builder()
//                        .document(sampleDraftOrderDocument)
//                        .attachments(List.of(
//                            CaseDocumentCollection.builder().value(CaseDocument.builder().documentFilename("attachment1").build())
//                                .build(),
//                            CaseDocumentCollection.builder().value(CaseDocument.builder().documentFilename("attachment2").build())
//                                .build()
//                        ))
//                        .build())
//                    .build()),
//                List.of(ReviewablePsaCollection.builder()
//                    .value(ReviewablePsa.builder()
//                        .document(samplePsaDocument)
//                        .build())
//                    .build())
//            ),
//
//            // Test with a draft order and without PSAs
//            Arguments.of(
//                DraftOrdersWrapper.builder()
//                    .hearingsReadyForReview(DynamicList.builder()
//                        .value(DynamicListElement.builder().code(HEARING_ID_MATCH).build())
//                        .build())
//                    .draftOrdersReviewCollection(List.of(
//                        DraftOrdersReviewCollection.builder()
//                            .value(DraftOrdersReview.builder()
//                                .hearingDate(LocalDate.of(2024, 10, 31))
//                                .hearingTime("09:00")
//                                .hearingType("hearingType")
//                                .hearingJudge("Mr. Judge")
//                                .draftOrderDocReviewCollection(List.of(
//                                    DraftOrderDocReviewCollection.builder()
//                                        .value(DraftOrderDocumentReview.builder()
//                                            .draftOrderDocument(sampleDraftOrderDocument)
//                                            .attachments(List.of(
//                                                CaseDocumentCollection.builder().value(CaseDocument.builder().documentFilename("attachment1").build())
//                                                    .build(),
//                                                CaseDocumentCollection.builder().value(CaseDocument.builder().documentFilename("attachment2").build())
//                                                    .build()
//                                            ))
//                                            .build())
//                                        .build()
//                                ))
//                                .build())
//                            .build()
//                    ))
//                    .build(),
//                List.of(ReviewableDraftOrderCollection.builder()
//                    .value(ReviewableDraftOrder.builder()
//                        .document(sampleDraftOrderDocument)
//                        .attachments(List.of(
//                            CaseDocumentCollection.builder().value(CaseDocument.builder().documentFilename("attachment1").build())
//                                .build(),
//                            CaseDocumentCollection.builder().value(CaseDocument.builder().documentFilename("attachment2").build())
//                                .build()
//                        ))
//                        .build())
//                    .build()),
//                Collections.emptyList()
//            ),
//
//            // Test with a psa and without draft order
//            Arguments.of(
//                DraftOrdersWrapper.builder()
//                    .hearingsReadyForReview(DynamicList.builder()
//                        .value(DynamicListElement.builder().code(HEARING_ID_MATCH).build())
//                        .build())
//                    .draftOrdersReviewCollection(List.of(
//                        DraftOrdersReviewCollection.builder()
//                            .value(DraftOrdersReview.builder()
//                                .hearingDate(LocalDate.of(2024, 10, 31))
//                                .hearingTime("09:00")
//                                .hearingType("hearingType")
//                                .hearingJudge("Mr. Judge")
//                                .psaDocReviewCollection(List.of(
//                                    PsaDocReviewCollection.builder()
//                                        .value(PsaDocumentReview.builder()
//                                            .psaDocument(samplePsaDocument2)
//                                            .build())
//                                        .build()
//                                ))
//                                .build())
//                            .build()
//                    ))
//                    .build(),
//                Collections.emptyList(),
//                List.of(ReviewablePsaCollection.builder()
//                    .value(ReviewablePsa.builder()
//                        .document(samplePsaDocument2)
//                        .build())
//                    .build())
//            )
//        );
//    }
//}
