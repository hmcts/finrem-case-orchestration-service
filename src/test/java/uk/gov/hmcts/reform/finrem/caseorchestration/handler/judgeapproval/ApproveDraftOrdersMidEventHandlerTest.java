//package uk.gov.hmcts.reform.finrem.caseorchestration.handler.judgeapproval;
//
//import lombok.SneakyThrows;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.junit.jupiter.params.ParameterizedTest;
//import org.junit.jupiter.params.provider.Arguments;
//import org.junit.jupiter.params.provider.MethodSource;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
//import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
//import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
//import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
//import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
//import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
//import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
//import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
//import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
//import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.AnotherHearingRequest;
//import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.AnotherHearingRequestCollection;
//import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeApproval;
//import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.ReviewableDraftOrder;
//import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.ReviewablePsa;
//import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
//import uk.gov.hmcts.reform.finrem.caseorchestration.service.judgeapproval.ApproveOrderService;
//
//import java.util.List;
//import java.util.stream.Stream;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertNull;
//import static org.mockito.Mockito.when;
//import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
//import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo.NO;
//import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo.YES;
//import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeDecision.JUDGE_NEEDS_TO_MAKE_CHANGES;
//import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeDecision.LEGAL_REP_NEEDS_TO_MAKE_CHANGE;
//import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeDecision.READY_TO_BE_SEALED;
//import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeDecision.REVIEW_LATER;
//import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;
//
//@ExtendWith(MockitoExtension.class)
//class ApproveDraftOrdersMidEventHandlerTest {
//
//    @InjectMocks
//    private ApproveDraftOrdersMidEventHandler handler;
//
//    @Mock
//    private ApproveOrderService approveOrderService;
//
//    @Test
//    void canHandle() {
//        assertCanHandle(handler, CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.APPROVE_ORDERS);
//    }
//
//    @SneakyThrows
//    @ParameterizedTest(name = "{index} => reviewableDraftOrder1={0}"
//        + "reviewableDraftOrder2={1}"
//        + "reviewableDraftOrder3={2}"
//        + "reviewableDraftOrder4={3}"
//        + "reviewableDraftOrder5={4}"
//        + "reviewablePsa1={5}"
//        + "reviewablePsa2={6}"
//        + "reviewablePsa3={7}"
//        + "reviewablePsa4={8}"
//        + "reviewablePsa5={9}"
//        + "expectedShowRequireAnotherHearingQuestion={10}")
//    @MethodSource("provideDraftOrderOrPsaData")
//    @DisplayName("Test handle method with different DraftOrdersWrapper inputs")
//    void handle_withVariousDraftOrdersWrapperData(ReviewableDraftOrder reviewableDraftOrder1, ReviewableDraftOrder reviewableDraftOrder2,
//                                                  ReviewableDraftOrder reviewableDraftOrder3, ReviewableDraftOrder reviewableDraftOrder4,
//                                                  ReviewableDraftOrder reviewableDraftOrder5,
//
//                                                  ReviewablePsa reviewablePsa1, ReviewablePsa reviewablePsa2,
//                                                  ReviewablePsa reviewablePsa3, ReviewablePsa reviewablePsa4, ReviewablePsa reviewablePsa5,
//                                                  YesOrNo expectedShowRequireAnotherHearingQuestion) {
//
//        // Arrange
//        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder()
//            .caseDetails(FinremCaseDetails.builder()
//                .id(12345L)
//                .data(FinremCaseData.builder()
//                    .draftOrdersWrapper(DraftOrdersWrapper.builder().judgeApproval(JudgeApproval.builder()
//                        .reviewableDraftOrder1(reviewableDraftOrder1)
//                        .reviewableDraftOrder2(reviewableDraftOrder2)
//                        .reviewableDraftOrder3(reviewableDraftOrder3)
//                        .reviewableDraftOrder4(reviewableDraftOrder4)
//                        .reviewableDraftOrder5(reviewableDraftOrder5)
//                        .reviewablePsa1(reviewablePsa1)
//                        .reviewablePsa2(reviewablePsa2)
//                        .reviewablePsa3(reviewablePsa3)
//                        .reviewablePsa4(reviewablePsa4)
//                        .reviewablePsa5(reviewablePsa5)
//                        .build()).build())
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
//        var hearingInstruction = responseDraftOrdersWrapper.getHearingInstruction();
//        assertNotNull(hearingInstruction);
//
//        assertEquals(expectedShowRequireAnotherHearingQuestion, hearingInstruction.getShowRequireAnotherHearingQuestion());
//    }
//
//    private static Stream<Arguments> provideDraftOrderOrPsaData() {
//        return Stream.of(
//            Arguments.of(
//                null, null, null, null, null, null, null, null, null, null, NO
//            ),
//            Arguments.of(
//                ReviewableDraftOrder.builder().judgeDecision(READY_TO_BE_SEALED).build(), null, null, null, null,
//                null, null, null, null, null, YES
//            ),
//            Arguments.of(
//                ReviewableDraftOrder.builder().judgeDecision(JUDGE_NEEDS_TO_MAKE_CHANGES).build(), null, null, null, null,
//                null, null, null, null, null, YES
//            ),
//            Arguments.of(
//                ReviewableDraftOrder.builder().judgeDecision(LEGAL_REP_NEEDS_TO_MAKE_CHANGE).build(), null, null, null, null,
//                null, null, null, null, null, NO
//            ),
//            Arguments.of(
//                ReviewableDraftOrder.builder().judgeDecision(REVIEW_LATER).build(), null, null, null, null,
//                null, null, null, null, null, NO
//            ),
//            Arguments.of(
//                ReviewableDraftOrder.builder().judgeDecision(READY_TO_BE_SEALED).build(),
//                ReviewableDraftOrder.builder().judgeDecision(READY_TO_BE_SEALED).build(),
//                ReviewableDraftOrder.builder().judgeDecision(READY_TO_BE_SEALED).build(),
//                ReviewableDraftOrder.builder().judgeDecision(READY_TO_BE_SEALED).build(),
//                ReviewableDraftOrder.builder().judgeDecision(READY_TO_BE_SEALED).build(),
//                null, null, null, null, null,
//                YES
//            ),
//            Arguments.of(
//                ReviewableDraftOrder.builder().judgeDecision(REVIEW_LATER).build(),
//                ReviewableDraftOrder.builder().judgeDecision(READY_TO_BE_SEALED).build(),
//                ReviewableDraftOrder.builder().judgeDecision(READY_TO_BE_SEALED).build(),
//                ReviewableDraftOrder.builder().judgeDecision(READY_TO_BE_SEALED).build(),
//                ReviewableDraftOrder.builder().judgeDecision(READY_TO_BE_SEALED).build(),
//                null, null, null, null, null,
//                YES
//            ),
//            Arguments.of(
//                ReviewableDraftOrder.builder().judgeDecision(REVIEW_LATER).build(),
//                ReviewableDraftOrder.builder().judgeDecision(REVIEW_LATER).build(),
//                ReviewableDraftOrder.builder().judgeDecision(REVIEW_LATER).build(),
//                ReviewableDraftOrder.builder().judgeDecision(REVIEW_LATER).build(),
//                ReviewableDraftOrder.builder().judgeDecision(READY_TO_BE_SEALED).build(),
//                null, null, null, null, null,
//                YES
//            ),
//            Arguments.of(
//                ReviewableDraftOrder.builder().judgeDecision(REVIEW_LATER).build(),
//                ReviewableDraftOrder.builder().judgeDecision(REVIEW_LATER).build(),
//                ReviewableDraftOrder.builder().judgeDecision(REVIEW_LATER).build(),
//                ReviewableDraftOrder.builder().judgeDecision(REVIEW_LATER).build(),
//                ReviewableDraftOrder.builder().judgeDecision(REVIEW_LATER).build(),
//                ReviewablePsa.builder().judgeDecision(READY_TO_BE_SEALED).build(),
//                null, null, null, null,
//                YES
//            ),
//            Arguments.of(
//                null, null, null, null, null,
//                ReviewablePsa.builder().judgeDecision(READY_TO_BE_SEALED).build(),
//                null, null, null, null,
//                YES
//            )
//        );
//    }
//
//    @Test
//    void shouldPopulateAnEmptyAnotherHearingRequestEntry() {
//        // Arrange
//        JudgeApproval judgeApproval = null;
//        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder()
//            .caseDetails(FinremCaseDetails.builder()
//                .id(12345L)
//                .data(FinremCaseData.builder()
//                    .draftOrdersWrapper(DraftOrdersWrapper.builder().judgeApproval(judgeApproval = JudgeApproval.builder()
//                        .reviewableDraftOrder1(ReviewableDraftOrder.builder().judgeDecision(REVIEW_LATER).build())
//                        .build()).build())
//                    .build())
//                .build())
//            .build();
//
//        DynamicList expectedDynamicList = DynamicList.builder().build();
//        when(approveOrderService.buildWhichOrderDynamicList(judgeApproval)).thenReturn(expectedDynamicList);
//
//        // Act
//        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);
//
//        // Assert
//        assertNotNull(response);
//        FinremCaseData responseData = response.getData();
//        DraftOrdersWrapper responseDraftOrdersWrapper = responseData.getDraftOrdersWrapper();
//
//        List<AnotherHearingRequestCollection> actualCollection =
//            responseDraftOrdersWrapper.getHearingInstruction().getAnotherHearingRequestCollection();
//
//        assertNotNull(actualCollection, "anotherHearingRequestCollection should not be null");
//        assertEquals(1, actualCollection.size(), "anotherHearingRequestCollection should contain exactly one element");
//        AnotherHearingRequest actualRequest = actualCollection.get(0).getValue();
//        assertNotNull(actualRequest, "The AnotherHearingRequest object should not be null");
//        assertEquals(actualRequest.getWhichOrder(), expectedDynamicList);
//        assertNull(actualRequest.getTypeOfHearing(), "typeOfHearing should be null");
//        assertNull(actualRequest.getTimeEstimate(), "timeEstimate should be null");
//        assertNull(actualRequest.getAdditionalTime(), "additionalTime should be null");
//        assertNull(actualRequest.getAnyOtherListingInstructions(), "anyOtherListingInstructions should be null");
//    }
//}
