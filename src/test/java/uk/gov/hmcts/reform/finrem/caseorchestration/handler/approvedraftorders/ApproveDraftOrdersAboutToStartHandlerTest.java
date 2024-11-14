package uk.gov.hmcts.reform.finrem.caseorchestration.handler.approvedraftorders;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.CaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.ReviewableDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.ReviewablePsa;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocumentReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.PsaDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.PsaDocumentReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.spy;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus.APPROVED_BY_JUDGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus.PROCESSED_BY_ADMIN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus.TO_BE_REVIEWED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class ApproveDraftOrdersAboutToStartHandlerTest {

    private static final String NOT_AVAILABLE_ERROR_MESSAGE = "There are no draft orders or pension sharing annexes to review.";

    private static final String WARNING_MESSAGE = "This page is limited to showing only 5 draft orders/pension sharing annexes requiring review. "
        + "There are additional draft orders/pension sharing annexes requiring review that are not shown.";

    @Mock
    private HearingService hearingService;

    @InjectMocks
    private ApproveDraftOrdersAboutToStartHandler handler;

    @Test
    void canHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.APPROVE_ORDERS);
    }

    @Test
    void givenUserHasHearingsReadyToReview_whenHandle_thenReturnSortedHearings() {
        FinremCaseData caseData = spy(new FinremCaseData());

        DraftOrderDocumentReview document1 = DraftOrderDocumentReview.builder().orderStatus(TO_BE_REVIEWED)
            .build();
        DraftOrderDocumentReview document2 = DraftOrderDocumentReview.builder().orderStatus(APPROVED_BY_JUDGE)
            .build();

        DraftOrderDocReviewCollection collectionItem1 = new DraftOrderDocReviewCollection(document1);
        DraftOrderDocReviewCollection collectionItem2 = new DraftOrderDocReviewCollection(document2);

        DraftOrdersReview review1 = DraftOrdersReview.builder()
            .hearingDate(LocalDate.of(2024, 8, 6))
            .hearingType("Hearing Type 1")
            .hearingJudge("Judge 1")
            .hearingTime("09:00 A.M.")
            .draftOrderDocReviewCollection(List.of(collectionItem1, collectionItem2))
            .build();

        DraftOrdersReviewCollection reviewCollection1 = new DraftOrdersReviewCollection(review1);
        caseData.getDraftOrdersWrapper().setDraftOrdersReviewCollection(List.of(reviewCollection1));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(
            FinremCallbackRequestFactory.from(1727874196328932L, caseData), AUTH_TOKEN);

        assertThat(response).isNotNull();
        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void givenUserHasPsaReadyToReview_whenHandle_thenReturnSortedHearings() {
        FinremCaseData caseData = spy(new FinremCaseData());
        PsaDocumentReview document1 = PsaDocumentReview.builder().orderStatus(TO_BE_REVIEWED)
            .build();
        PsaDocReviewCollection psaCollectionItem1 = new PsaDocReviewCollection(document1);

        DraftOrdersReview review1 = DraftOrdersReview.builder()
            .hearingDate(LocalDate.of(2024, 8, 6))
            .hearingType("Hearing Type 1")
            .hearingJudge("Judge 1")
            .hearingTime("09:00 A.M.")
            .psaDocReviewCollection(List.of(psaCollectionItem1))
            .build();

        DraftOrdersReviewCollection reviewCollection1 = new DraftOrdersReviewCollection(review1);
        caseData.getDraftOrdersWrapper().setDraftOrdersReviewCollection(List.of(reviewCollection1));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(
            FinremCallbackRequestFactory.from(1727874196328932L, caseData), AUTH_TOKEN);

        assertThat(response).isNotNull();
        assertThat(response.getErrors()).isEmpty();
    }

    @SneakyThrows
    @Test
    void givenUserHasNoHearingsForReview_whenHandle_thenReturnError() {
        FinremCaseData caseData = spy(new FinremCaseData());

        DraftOrderDocumentReview document1 = DraftOrderDocumentReview.builder().orderStatus(APPROVED_BY_JUDGE)
            .build();
        DraftOrderDocumentReview document2 = DraftOrderDocumentReview.builder().orderStatus(PROCESSED_BY_ADMIN)
            .build();

        DraftOrderDocReviewCollection collectionItem1 = new DraftOrderDocReviewCollection(document1);
        DraftOrderDocReviewCollection collectionItem2 = new DraftOrderDocReviewCollection(document2);

        DraftOrdersReview review1 = DraftOrdersReview.builder()
            .hearingDate(LocalDate.of(2024, 8, 6))
            .hearingType("Hearing Type 1")
            .hearingJudge("Judge 1")
            .draftOrderDocReviewCollection(List.of(collectionItem1, collectionItem2))
            .build();

        DraftOrdersReviewCollection reviewCollection1 = new DraftOrdersReviewCollection(review1);
        caseData.getDraftOrdersWrapper().setDraftOrdersReviewCollection(List.of(reviewCollection1));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(
            FinremCallbackRequestFactory.from(1727874196328932L, caseData), AUTH_TOKEN);

        Assertions.assertEquals(1, response.getErrors().size());
        assertThat(response.getErrors()).contains(NOT_AVAILABLE_ERROR_MESSAGE);
        var judgeApproval = response.getData().getDraftOrdersWrapper().getJudgeApproval();
        for (int i = 1; i <= 5; i++) {
            assertThat(judgeApproval.getClass().getMethod("getReviewableDraftOrder" + i).invoke(judgeApproval)).isNull();
            assertThat(judgeApproval.getClass().getMethod("getReviewablePsa" + i).invoke(judgeApproval)).isNull();
        }
    }

    @SneakyThrows
    @Test
    void givenUserHasDraftOrders_whenHandle_thenReturnError() {
        FinremCaseData caseData = spy(new FinremCaseData());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(
            FinremCallbackRequestFactory.from(1727874196328932L, caseData), AUTH_TOKEN);

        Assertions.assertEquals(1, response.getErrors().size());
        assertThat(response.getErrors()).contains(NOT_AVAILABLE_ERROR_MESSAGE);
        var judgeApproval = response.getData().getDraftOrdersWrapper().getJudgeApproval();
        for (int i = 1; i <= 5; i++) {
            assertThat(judgeApproval.getClass().getMethod("getReviewableDraftOrder" + i).invoke(judgeApproval)).isNull();
            assertThat(judgeApproval.getClass().getMethod("getReviewablePsa" + i).invoke(judgeApproval)).isNull();
        }
    }

    @SneakyThrows
    @ParameterizedTest(name = "{index} => draftOrdersWrapper={0}, expectedReviewableDraftOrder1={1}, expectedReviewableDraftOrder2={2},"
        + "expectedReviewableDraftOrder3={3}, expectedReviewableDraftOrder4={4}, expectedReviewableDraftOrder5={5},"
        + "expectedReviewablePsa1={6}, expectedReviewablePsa2={7}, expectedReviewablePsa3={8}, expectedReviewablePsa4={9}"
        + "expectedReviewablePsa5={10}, expectedWarningMessageToJudge={11}")
    @MethodSource("provideDraftOrderData")
    @DisplayName("Test handle method with different DraftOrdersWrapper inputs")
    void handle_withVariousDraftOrdersWrapperData(
        DraftOrdersWrapper draftOrdersWrapper,
        ReviewableDraftOrder expectedReviewableDraftOrder1, ReviewableDraftOrder expectedReviewableDraftOrder2,
        ReviewableDraftOrder expectedReviewableDraftOrder3, ReviewableDraftOrder expectedReviewableDraftOrder4,
        ReviewableDraftOrder expectedReviewableDraftOrder5,
        ReviewablePsa expectedReviewablePsa1, ReviewablePsa expectedReviewablePsa2, ReviewablePsa expectedReviewablePsa3,
        ReviewablePsa expectedReviewablePsa4, ReviewablePsa expectedReviewablePsa5,
        String expectedWarningMessageToJudge) {

        // Arrange
        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder()
            .caseDetails(FinremCaseDetails.builder()
                .id(12345L)
                .data(FinremCaseData.builder()
                    .draftOrdersWrapper(draftOrdersWrapper)
                    .build())
                .build())
            .build();
        lenient().when(hearingService.formatHearingInfo("hearingType", LocalDate.of(2024, 10, 31), "09:00", "Mr. Judge"))
            .thenReturn("hearingServiceFormattedString1");
        lenient().when(hearingService.formatHearingInfo("hearingType", LocalDate.of(2024, 11, 30), "09:00", "Mr. Judge"))
            .thenReturn("hearingServiceFormattedString2");

        // Act
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        // Assert
        assertNotNull(response);
        FinremCaseData responseData = response.getData();
        DraftOrdersWrapper responseDraftOrdersWrapper = responseData.getDraftOrdersWrapper();


        var judgeApproval = responseDraftOrdersWrapper.getJudgeApproval();
        assertNotNull(judgeApproval);
        assertNotNull(judgeApproval);

        var expectedReviewableDraftOrders = List.of(
            Optional.ofNullable(expectedReviewableDraftOrder1), Optional.ofNullable(expectedReviewableDraftOrder2),
            Optional.ofNullable(expectedReviewableDraftOrder3), Optional.ofNullable(expectedReviewableDraftOrder4),
            Optional.ofNullable(expectedReviewableDraftOrder5)
        );

        var expectedReviewablePsas = List.of(
            Optional.ofNullable(expectedReviewablePsa1), Optional.ofNullable(expectedReviewablePsa2), Optional.ofNullable(expectedReviewablePsa3),
            Optional.ofNullable(expectedReviewablePsa4), Optional.ofNullable(expectedReviewablePsa5)
        );

        for (int i = 0; i < 5; i++) {
            assertEquals(expectedReviewableDraftOrders.get(i).orElse(null),
                judgeApproval.getClass().getMethod("getReviewableDraftOrder" + (i + 1)).invoke(judgeApproval));
            assertEquals(expectedReviewablePsas.get(i).orElse(null),
                judgeApproval.getClass().getMethod("getReviewablePsa" + (i + 1)).invoke(judgeApproval));
        }
        assertEquals(expectedWarningMessageToJudge, judgeApproval.getWarningMessageToJudge());
    }

    private static final CaseDocument DO_DOC_1 = CaseDocument.builder().documentFilename("sampleDocument").build();
    private static final CaseDocument DO_DOC_2 = CaseDocument.builder().documentFilename("sampleDocument2").build();
    private static final CaseDocument PSA_DOC_1 = CaseDocument.builder().documentFilename("samplePsaDocument1").build();
    private static final CaseDocument PSA_DOC_2 = CaseDocument.builder().documentFilename("samplePsaDocument2").build();
    private static final CaseDocument PSA_DOC_3 = CaseDocument.builder().documentFilename("samplePsaDocument3").build();
    private static final CaseDocument PSA_DOC_4 = CaseDocument.builder().documentFilename("samplePsaDocument4").build();
    private static final CaseDocument PSA_DOC_5 = CaseDocument.builder().documentFilename("samplePsaDocument5").build();
    private static final CaseDocument PSA_DOC_6 = CaseDocument.builder().documentFilename("samplePsaDocument6").build();
    private static final CaseDocumentCollection DO_ATTACHMENT_1 = CaseDocumentCollection.builder()
        .value(CaseDocument.builder().documentFilename("attachment1").build()).build();
    private static final CaseDocumentCollection DO_ATTACHMENT_2 = CaseDocumentCollection.builder()
        .value(CaseDocument.builder().documentFilename("attachment2").build()).build();

    private static DraftOrdersReview.DraftOrdersReviewBuilder applyHearingInfo1(DraftOrdersReview.DraftOrdersReviewBuilder builder) {
        return builder.hearingDate(LocalDate.of(2024, 10, 31))
            .hearingTime("09:00")
            .hearingType("hearingType")
            .hearingJudge("Mr. Judge");
    }

    private static DraftOrdersReview.DraftOrdersReviewBuilder applyHearingInfo2(DraftOrdersReview.DraftOrdersReviewBuilder builder) {
        return builder.hearingDate(LocalDate.of(2024, 11, 30))
            .hearingTime("09:00")
            .hearingType("hearingType")
            .hearingJudge("Mr. Judge");
    }

    private static DraftOrderDocReviewCollection buildDraftOrderDocumentReview(CaseDocument draftOrderDocument,
                                                                               List<CaseDocumentCollection> attachments) {
        return buildDraftOrderDocumentReview(draftOrderDocument, attachments, null);
    }

    private static DraftOrderDocReviewCollection buildDraftOrderDocumentReview(CaseDocument draftOrderDocument,
                                                                               List<CaseDocumentCollection> attachments, OrderStatus orderStatus) {
        return DraftOrderDocReviewCollection.builder()
            .value(DraftOrderDocumentReview.builder().draftOrderDocument(draftOrderDocument).attachments(attachments).orderStatus(orderStatus)
                .build()).build();
    }

    private static CaseDocument randomCaseDocument() {
        return CaseDocument.builder().documentFilename(LocalDateTime.now().toString()).build();
    }

    private static DraftOrderDocReviewCollection buildDraftOrderDocumentReview(OrderStatus orderStatus) {
        return DraftOrderDocReviewCollection.builder()
            .value(DraftOrderDocumentReview.builder().draftOrderDocument(randomCaseDocument()).orderStatus(orderStatus)
                .build()).build();
    }

    private static PsaDocReviewCollection buildPsaDocReviewCollection(CaseDocument psaDocument) {
        return buildPsaDocReviewCollection(psaDocument, null);
    }

    private static PsaDocReviewCollection buildPsaDocReviewCollection(CaseDocument psaDocument, OrderStatus orderStatus) {
        return PsaDocReviewCollection.builder()
            .value(PsaDocumentReview.builder().psaDocument(psaDocument).orderStatus(orderStatus).build())
            .build();
    }

    private static PsaDocReviewCollection buildPsaDocReviewCollection(OrderStatus orderStatus) {
        return PsaDocReviewCollection.builder()
            .value(PsaDocumentReview.builder().psaDocument(randomCaseDocument()).orderStatus(orderStatus).build())
            .build();
    }

    private static ReviewableDraftOrder buildReviewableDraftOrder(String hearingInfo, CaseDocument draftOrderDocument,
                                                           List<CaseDocumentCollection> attachments) {
        return ReviewableDraftOrder.builder().hearingInfo(hearingInfo)
            .document(draftOrderDocument)
            .attachments(attachments)
            .hasAttachment(YesOrNo.forValue(!attachments.isEmpty()))
            .build();
    }

    private static ReviewablePsa buildReviewablePsa(String hearingInfo, CaseDocument psaDocument) {
        return ReviewablePsa.builder().hearingInfo(hearingInfo)
            .document(psaDocument)
            .build();
    }

    private static Arguments withEmptyDraftOrdersWrapper() {
        return Arguments.of(
            DraftOrdersWrapper.builder().draftOrdersReviewCollection(Collections.emptyList()).build(),
            null, null, null, null, null,
            null, null, null, null, null,
            null
        );
    }

    private static Arguments withOneDraftOrderAndOnePsa() {
        return Arguments.of(
            DraftOrdersWrapper.builder()
                .draftOrdersReviewCollection(List.of(
                    DraftOrdersReviewCollection.builder()
                        .value(applyHearingInfo1(DraftOrdersReview.builder()
                            .draftOrderDocReviewCollection(List.of(
                                buildDraftOrderDocumentReview(DO_DOC_1, List.of(DO_ATTACHMENT_1))
                            ))
                            .psaDocReviewCollection(List.of(
                                buildPsaDocReviewCollection(PSA_DOC_1)
                            )))
                            .build())
                        .build()
                ))
                .build(),
            // expectedReviewableDraftOrder1
            buildReviewableDraftOrder("hearingServiceFormattedString1", DO_DOC_1, List.of(DO_ATTACHMENT_1)),
            null, null, null, null,
            // expectedReviewablePsa1
            buildReviewablePsa("hearingServiceFormattedString1", PSA_DOC_1),
            null, null, null, null,
            // expectedWarningMessageToJudge
            null);
    }

    private static Arguments withTwoDraftOrderAndZeroPsa() {
        return Arguments.of(
            DraftOrdersWrapper.builder()
                .draftOrdersReviewCollection(List.of(
                    DraftOrdersReviewCollection.builder()
                        .value(applyHearingInfo1(DraftOrdersReview.builder()
                            .draftOrderDocReviewCollection(List.of(
                                buildDraftOrderDocumentReview(DO_DOC_1, List.of(DO_ATTACHMENT_1)),
                                buildDraftOrderDocumentReview(DO_DOC_2, List.of(DO_ATTACHMENT_2))
                            )))
                            .build())
                        .build()
                ))
                .build(),
            // expectedReviewableDraftOrder1
            buildReviewableDraftOrder("hearingServiceFormattedString1", DO_DOC_1, List.of(DO_ATTACHMENT_1)),
            // expectedReviewableDraftOrder2
            buildReviewableDraftOrder("hearingServiceFormattedString1", DO_DOC_2, List.of(DO_ATTACHMENT_2)),
            null, null, null,
            null, null, null, null, null,
            // expectedWarningMessageToJudge
            null);
    }

    private static Arguments withDifferentHearingInfo() {
        return Arguments.of(
            DraftOrdersWrapper.builder()
                .draftOrdersReviewCollection(List.of(
                    DraftOrdersReviewCollection.builder()
                        .value(applyHearingInfo1(DraftOrdersReview.builder()
                            .draftOrderDocReviewCollection(List.of(
                                buildDraftOrderDocumentReview(DO_DOC_1, List.of(DO_ATTACHMENT_1))
                            ))
                            .psaDocReviewCollection(List.of(
                                buildPsaDocReviewCollection(PSA_DOC_1)
                            ))).build())
                        .build(),
                    DraftOrdersReviewCollection.builder()
                        .value(applyHearingInfo2(DraftOrdersReview.builder()
                            .draftOrderDocReviewCollection(List.of(
                                buildDraftOrderDocumentReview(DO_DOC_2, List.of(DO_ATTACHMENT_2))
                            ))
                        ).build())
                        .build()
                ))
                .build(),
            // expectedReviewableDraftOrder1
            buildReviewableDraftOrder("hearingServiceFormattedString1", DO_DOC_1, List.of(DO_ATTACHMENT_1)),
            // expectedReviewableDraftOrder2
            buildReviewableDraftOrder("hearingServiceFormattedString2", DO_DOC_2, List.of(DO_ATTACHMENT_2)),
            null, null, null,
            // expectedReviewablePsa1
            buildReviewablePsa("hearingServiceFormattedString1", PSA_DOC_1),
            null, null, null, null,
            // expectedWarningMessageToJudge
            null);
    }

    private static Arguments withProcessedDraftOrderAndPsa() {
        return Arguments.of(
            DraftOrdersWrapper.builder()
                .draftOrdersReviewCollection(List.of(
                    DraftOrdersReviewCollection.builder()
                        .value(applyHearingInfo1(DraftOrdersReview.builder()
                            .psaDocReviewCollection(List.of(
                                buildPsaDocReviewCollection(APPROVED_BY_JUDGE),
                                buildPsaDocReviewCollection(PROCESSED_BY_ADMIN),
                                buildPsaDocReviewCollection(PSA_DOC_2, TO_BE_REVIEWED)
                            ))
                            .draftOrderDocReviewCollection(List.of(
                                buildDraftOrderDocumentReview(APPROVED_BY_JUDGE),
                                buildDraftOrderDocumentReview(PROCESSED_BY_ADMIN),
                                buildDraftOrderDocumentReview(DO_DOC_2, List.of(DO_ATTACHMENT_2), TO_BE_REVIEWED)
                            )))
                            .build())
                        .build()
                ))
                .build(),
            // expectedReviewableDraftOrder(1-5)
            buildReviewableDraftOrder("hearingServiceFormattedString1", DO_DOC_2, List.of(DO_ATTACHMENT_2)),
            null, null, null, null,
            // expectedReviewablePsa(1-5)

            buildReviewablePsa("hearingServiceFormattedString1", PSA_DOC_2),
            null, null, null, null,
            // expectedWarningMessageToJudge
            null);
    }

    private static Arguments withSixPSAs() {
        return Arguments.of(
            DraftOrdersWrapper.builder()
                .draftOrdersReviewCollection(List.of(
                    DraftOrdersReviewCollection.builder()
                        .value(applyHearingInfo1(DraftOrdersReview.builder()
                            .psaDocReviewCollection(List.of(
                                buildPsaDocReviewCollection(PSA_DOC_1, TO_BE_REVIEWED),
                                buildPsaDocReviewCollection(PSA_DOC_2, TO_BE_REVIEWED),
                                buildPsaDocReviewCollection(PSA_DOC_3, TO_BE_REVIEWED),
                                buildPsaDocReviewCollection(PSA_DOC_4, TO_BE_REVIEWED),
                                buildPsaDocReviewCollection(PSA_DOC_5, TO_BE_REVIEWED),
                                buildPsaDocReviewCollection(PSA_DOC_6, TO_BE_REVIEWED)
                            ))
                            ).build())
                        .build()
                ))
                .build(),
            // expectedReviewableDraftOrder(1-5)
            null, null, null, null, null,
            // expectedReviewablePsa(1-5)
            buildReviewablePsa("hearingServiceFormattedString1", PSA_DOC_1),
            buildReviewablePsa("hearingServiceFormattedString1", PSA_DOC_2),
            buildReviewablePsa("hearingServiceFormattedString1", PSA_DOC_3),
            buildReviewablePsa("hearingServiceFormattedString1", PSA_DOC_4),
            buildReviewablePsa("hearingServiceFormattedString1", PSA_DOC_5),
            // expectedWarningMessageToJudge
            WARNING_MESSAGE);
    }

    private static Arguments withFiveReviewablePSAs() {
        return Arguments.of(
            DraftOrdersWrapper.builder()
                .draftOrdersReviewCollection(List.of(
                    DraftOrdersReviewCollection.builder()
                        .value(applyHearingInfo1(DraftOrdersReview.builder()
                            .psaDocReviewCollection(List.of(
                                buildPsaDocReviewCollection(PSA_DOC_1, TO_BE_REVIEWED),
                                buildPsaDocReviewCollection(PSA_DOC_2, TO_BE_REVIEWED),
                                buildPsaDocReviewCollection(PSA_DOC_3, TO_BE_REVIEWED),
                                buildPsaDocReviewCollection(PSA_DOC_4, TO_BE_REVIEWED),
                                buildPsaDocReviewCollection(PSA_DOC_5, TO_BE_REVIEWED),
                                buildPsaDocReviewCollection(APPROVED_BY_JUDGE)
                            ))
                        ).build())
                        .build()
                ))
                .build(),
            // expectedReviewableDraftOrder(1-5)
            null, null, null, null, null,
            // expectedReviewablePsa(1-5)
            buildReviewablePsa("hearingServiceFormattedString1", PSA_DOC_1),
            buildReviewablePsa("hearingServiceFormattedString1", PSA_DOC_2),
            buildReviewablePsa("hearingServiceFormattedString1", PSA_DOC_3),
            buildReviewablePsa("hearingServiceFormattedString1", PSA_DOC_4),
            buildReviewablePsa("hearingServiceFormattedString1", PSA_DOC_5),
            // expectedWarningMessageToJudge
            null);
    }

    private static Stream<Arguments> provideDraftOrderData() {
        return Stream.of(
            withEmptyDraftOrdersWrapper(), withOneDraftOrderAndOnePsa(), withTwoDraftOrderAndZeroPsa(),
            withDifferentHearingInfo(), withProcessedDraftOrderAndPsa(), withSixPSAs(),
            withFiveReviewablePSAs()
        );
    }
}
