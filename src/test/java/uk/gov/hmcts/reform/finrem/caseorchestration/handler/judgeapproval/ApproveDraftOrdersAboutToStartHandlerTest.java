package uk.gov.hmcts.reform.finrem.caseorchestration.handler.judgeapproval;

import lombok.SneakyThrows;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UUIDCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.CaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeApproval;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeApprovalDocType;
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
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.lenient;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo.NO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo.YES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeApprovalDocType.DRAFT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeApprovalDocType.PSA;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus.APPROVED_BY_JUDGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus.PROCESSED_BY_ADMIN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus.TO_BE_REVIEWED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class ApproveDraftOrdersAboutToStartHandlerTest {

    private static final String NOT_AVAILABLE_ERROR_MESSAGE = "There are no draft orders or pension sharing annexes to review.";

    private static final int NUMBER_OF_DOC_TO_BE_REVIEWED = 5;

    @Mock
    private HearingService hearingService;

    @InjectMocks
    private ApproveDraftOrdersAboutToStartHandler handler;

    @Test
    void canHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.APPROVE_ORDERS);
    }

    @SneakyThrows
    @Test
    void givenUserHasJudgeProcessedDraftOrders_thenReturnError() {
        FinremCaseData caseData = new FinremCaseData();

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

        assertEquals(1, response.getErrors().size());
        assertThat(response.getErrors()).contains(NOT_AVAILABLE_ERROR_MESSAGE);
        var draftOrdersWrapper = response.getData().getDraftOrdersWrapper();
        for (int i = 1; i <= NUMBER_OF_DOC_TO_BE_REVIEWED; i++) {
            assertThat(draftOrdersWrapper.getClass().getMethod("getJudgeApproval" + i).invoke(draftOrdersWrapper)).isNull();
        }
    }

    @SneakyThrows
    @Test
    void givenUserHasNoDraftOrders_whenHandle_thenReturnError() {
        FinremCaseData caseData = new FinremCaseData();

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(
            FinremCallbackRequestFactory.from(1727874196328932L, caseData), AUTH_TOKEN);

        assertEquals(1, response.getErrors().size());
        assertThat(response.getErrors()).contains(NOT_AVAILABLE_ERROR_MESSAGE);
        var draftOrdersWrapper = response.getData().getDraftOrdersWrapper();
        for (int i = 1; i <= NUMBER_OF_DOC_TO_BE_REVIEWED; i++) {
            assertThat(draftOrdersWrapper.getClass().getMethod("getJudgeApproval" + i).invoke(draftOrdersWrapper)).isNull();
        }
    }

    @Test
    void givenRefusalOrderIdsToBeSentWasSet_whenHandle_thenClearThem() {
        FinremCaseData caseData = new FinremCaseData();
        caseData.getDraftOrdersWrapper().setRefusalOrderIdsToBeSent(List.of(UUIDCollection.builder().value(UUID.randomUUID()).build()));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(
            FinremCallbackRequestFactory.from(1727874196328932L, caseData), AUTH_TOKEN);

        var draftOrdersWrapper = response.getData().getDraftOrdersWrapper();
        assertThat(draftOrdersWrapper.getRefusalOrderIdsToBeSent()).isNull();
    }

    @SneakyThrows
    @ParameterizedTest(name = "{index} => draftOrdersWrapper={0}, expectedJudgeApproval1={1}, expectedJudgeApproval2={2},"
        + "expectedJudgeApproval3={3}, expectedJudgeApproval4={4}, expectedJudgeApproval5={5},"
        + "expectedShowWarningMessageToJudge={6}")
    @MethodSource("provideDraftOrderData")
    @DisplayName("Test handle method with different DraftOrdersWrapper inputs")
    void handle_withVariousDraftOrdersWrapperData(
        DraftOrdersWrapper draftOrdersWrapper,
        JudgeApproval expectedJudgeApproval1, JudgeApproval expectedJudgeApproval2,
        JudgeApproval expectedJudgeApproval3, JudgeApproval expectedJudgeApproval4,
        JudgeApproval expectedJudgeApproval5,
        YesOrNo expectedShowWarningMessageToJudge) {

        // Arrange
        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder()
            .caseDetails(FinremCaseDetails.builder()
                .id(12345L)
                .data(FinremCaseData.builder()
                    .draftOrdersWrapper(draftOrdersWrapper)
                    .build())
                .build())
            .build();
        lenient().when(hearingService.formatHearingInfo("hearingType", LocalDate.of(2024, 10, 30), "09:00", "Mr. Judge"))
            .thenReturn("hearingServiceFormattedString1");
        lenient().when(hearingService.formatHearingInfo("hearingType", LocalDate.of(2024, 11, 30), "09:00", "Mr. Judge"))
            .thenReturn("hearingServiceFormattedString2");

        // Act
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        // Assert
        assertNotNull(response);
        FinremCaseData responseData = response.getData();
        draftOrdersWrapper = responseData.getDraftOrdersWrapper();

        var expectedJudgeApproval = List.of(
            Optional.ofNullable(expectedJudgeApproval1), Optional.ofNullable(expectedJudgeApproval2),
            Optional.ofNullable(expectedJudgeApproval3), Optional.ofNullable(expectedJudgeApproval4),
            Optional.ofNullable(expectedJudgeApproval5)
        );

        for (int i = 0; i < NUMBER_OF_DOC_TO_BE_REVIEWED; i++) {
            var actual = (JudgeApproval) draftOrdersWrapper.getClass().getMethod("getJudgeApproval" + (i + 1)).invoke(draftOrdersWrapper);
            var expected = expectedJudgeApproval.get(i).orElse(null);
            if (expected != null && actual != null) {
                assertEquals(expected.getTitle(), actual.getTitle());
                assertEquals(expected.getInlineDocType(), actual.getInlineDocType());
                assertEquals(expected.getDocument(), actual.getDocument());
                assertEquals(expected.getHearingInfo(), actual.getHearingInfo());
                assertEquals(expected.getHearingDate(), actual.getHearingDate());
                if (expected.getHasAttachment() == YES) {
                    assertEquals(expected.getAttachments(), actual.getAttachments());
                }
            } else {
                assertEquals(expected, actual);
            }
        }
        assertEquals(expectedShowWarningMessageToJudge, draftOrdersWrapper.getShowWarningMessageToJudge());
    }

    private static final CaseDocument DO_DOC_1 = CaseDocument.builder().documentFilename("sampleDocument1").build();
    private static final CaseDocument DO_DOC_2 = CaseDocument.builder().documentFilename("sampleDocument2").build();
    private static final CaseDocument DO_DOC_3 = CaseDocument.builder().documentFilename("sampleDocument3").build();
    private static final CaseDocument DO_DOC_4 = CaseDocument.builder().documentFilename("sampleDocument4").build();
    private static final CaseDocument DO_DOC_5 = CaseDocument.builder().documentFilename("sampleDocument5").build();
    private static final CaseDocument DO_DOC_6 = CaseDocument.builder().documentFilename("sampleDocument6").build();
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
        return builder.hearingDate(LocalDate.of(2024, 10, 30))
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

    private static CaseDocument randomCaseDocument() {
        return CaseDocument.builder().documentFilename(LocalDateTime.now().toString()).build();
    }

    private static DraftOrderDocReviewCollection buildDraftOrderDocumentReviewWithoutSubmittedDate(CaseDocument draftOrderDocument,
                                                                                                   List<CaseDocumentCollection> attachments) {
        return buildDraftOrderDocumentReview(draftOrderDocument, attachments, TO_BE_REVIEWED, null);
    }

    private static DraftOrderDocReviewCollection buildDraftOrderDocumentReview(CaseDocument draftOrderDocument,
                                                                               List<CaseDocumentCollection> attachments) {
        return buildDraftOrderDocumentReview(draftOrderDocument, attachments, TO_BE_REVIEWED, LocalDateTime.now());
    }

    private static DraftOrderDocReviewCollection buildDraftOrderDocumentReview(CaseDocument draftOrderDocument,
                                                                               List<CaseDocumentCollection> attachments, OrderStatus orderStatus) {
        return buildDraftOrderDocumentReview(draftOrderDocument, attachments, orderStatus, LocalDateTime.now());
    }

    private static DraftOrderDocReviewCollection buildDraftOrderDocumentReview(OrderStatus orderStatus) {
        return buildDraftOrderDocumentReview(randomCaseDocument(), null, orderStatus, LocalDateTime.now());
    }

    private static DraftOrderDocReviewCollection buildDraftOrderDocumentReview(CaseDocument draftOrderDocument,
                                                                               List<CaseDocumentCollection> attachments, OrderStatus orderStatus,
                                                                               LocalDateTime submittedDate) {
        return DraftOrderDocReviewCollection.builder()
            .value(DraftOrderDocumentReview.builder()
                .draftOrderDocument(draftOrderDocument)
                .attachments(attachments)
                .orderStatus(orderStatus)
                .submittedDate(submittedDate)
                .build())
            .build();
    }

    private static PsaDocReviewCollection buildPsaDocReviewCollectionWithoutSumittedDate(CaseDocument psaDocument) {
        return buildPsaDocReviewCollection(psaDocument, TO_BE_REVIEWED, null);
    }

    private static PsaDocReviewCollection buildPsaDocReviewCollection(CaseDocument psaDocument) {
        return buildPsaDocReviewCollection(psaDocument, TO_BE_REVIEWED, LocalDateTime.now());
    }

    private static PsaDocReviewCollection buildPsaDocReviewCollection(CaseDocument psaDocument, OrderStatus orderStatus) {
        return buildPsaDocReviewCollection(psaDocument, orderStatus, LocalDateTime.now());
    }

    private static PsaDocReviewCollection buildPsaDocReviewCollection(CaseDocument psaDocument, OrderStatus orderStatus,
                                                                      LocalDateTime submittedDate) {
        return PsaDocReviewCollection.builder()
            .value(PsaDocumentReview.builder().psaDocument(psaDocument).orderStatus(orderStatus).submittedDate(submittedDate).build())
            .build();
    }

    private static PsaDocReviewCollection buildPsaDocReviewCollection(OrderStatus orderStatus) {
        return PsaDocReviewCollection.builder()
            .value(PsaDocumentReview.builder().psaDocument(randomCaseDocument()).orderStatus(orderStatus).build())
            .build();
    }

    private static JudgeApproval buildJudgeApproval(JudgeApprovalDocType docType,
                                                    String hearingInfo, CaseDocument document,
                                                    List<CaseDocumentCollection> attachments) {
        return JudgeApproval.builder()
            .hearingInfo(hearingInfo)
            .hearingDate(LocalDate.of(2024, "hearingServiceFormattedString1".equals(hearingInfo) ? 10: 11 , 30))
            .title(docType.getTitle())
            .inlineDocType(docType.getDescription())
            .document(document)
            .attachments(attachments)
            .hasAttachment(YesOrNo.forValue(attachments != null && !attachments.isEmpty()))
            .build();
    }

    private static Arguments withEmptyDraftOrdersWrapper() {
        return Arguments.of(
            DraftOrdersWrapper.builder().draftOrdersReviewCollection(Collections.emptyList()).build(),
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
            buildJudgeApproval(DRAFT_ORDER, "hearingServiceFormattedString1", DO_DOC_1, List.of(DO_ATTACHMENT_1)),
            buildJudgeApproval(PSA, "hearingServiceFormattedString1", PSA_DOC_1, null),
            null, null, null,
            NO);
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
            buildJudgeApproval(DRAFT_ORDER, "hearingServiceFormattedString1", DO_DOC_1, List.of(DO_ATTACHMENT_1)),
            buildJudgeApproval(DRAFT_ORDER, "hearingServiceFormattedString1", DO_DOC_2, List.of(DO_ATTACHMENT_2)),
            null, null, null,
            NO);
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
            buildJudgeApproval(DRAFT_ORDER, "hearingServiceFormattedString1", DO_DOC_1, List.of(DO_ATTACHMENT_1)),
            buildJudgeApproval(PSA, "hearingServiceFormattedString1", PSA_DOC_1, null),
            buildJudgeApproval(DRAFT_ORDER, "hearingServiceFormattedString2", DO_DOC_2, List.of(DO_ATTACHMENT_2)),
            null, null,
            NO);
    }

    private static Arguments withSameHearingInfoAndDocumentShouldBeSortedBySubmittedDate() {
        DraftOrderDocReviewCollection doc1 = buildDraftOrderDocumentReview(DO_DOC_1, List.of(DO_ATTACHMENT_1));
        DraftOrderDocReviewCollection doc3 = buildDraftOrderDocumentReview(DO_DOC_2, List.of(DO_ATTACHMENT_2));
        PsaDocReviewCollection doc2 = buildPsaDocReviewCollection(PSA_DOC_1);

        return Arguments.of(
            DraftOrdersWrapper.builder()
                .draftOrdersReviewCollection(List.of(
                    DraftOrdersReviewCollection.builder()
                        .value(applyHearingInfo1(DraftOrdersReview.builder()
                            .draftOrderDocReviewCollection(List.of(doc1))
                            .psaDocReviewCollection(List.of(doc2))).build())
                        .build(),
                    DraftOrdersReviewCollection.builder()
                        .value(applyHearingInfo1(DraftOrdersReview.builder()
                            .draftOrderDocReviewCollection(List.of(doc3))
                        ).build())
                        .build()
                ))
                .build(),
            buildJudgeApproval(DRAFT_ORDER, "hearingServiceFormattedString1", DO_DOC_1, List.of(DO_ATTACHMENT_1)),
            buildJudgeApproval(DRAFT_ORDER, "hearingServiceFormattedString1", DO_DOC_2, List.of(DO_ATTACHMENT_2)),
            buildJudgeApproval(PSA, "hearingServiceFormattedString1", PSA_DOC_1, null),
            null, null,
            NO);
    }

    private static Arguments withSameHearingInfoAndDocumentShouldBeSortedBySubmittedDate2() {
        DraftOrderDocReviewCollection doc1 = buildDraftOrderDocumentReview(DO_DOC_1, List.of(DO_ATTACHMENT_1));
        PsaDocReviewCollection doc2 = buildPsaDocReviewCollection(PSA_DOC_1);
        DraftOrderDocReviewCollection doc3 = buildDraftOrderDocumentReview(DO_DOC_2, List.of(DO_ATTACHMENT_2));

        return Arguments.of(
            DraftOrdersWrapper.builder()
                .draftOrdersReviewCollection(List.of(
                    DraftOrdersReviewCollection.builder()
                        .value(applyHearingInfo1(DraftOrdersReview.builder()
                            .draftOrderDocReviewCollection(List.of(doc1))
                            .psaDocReviewCollection(List.of(doc2))).build())
                        .build(),
                    DraftOrdersReviewCollection.builder()
                        .value(applyHearingInfo1(DraftOrdersReview.builder()
                            .draftOrderDocReviewCollection(List.of(doc3))
                        ).build())
                        .build()
                ))
                .build(),
            buildJudgeApproval(DRAFT_ORDER, "hearingServiceFormattedString1", DO_DOC_1, List.of(DO_ATTACHMENT_1)),
            buildJudgeApproval(PSA, "hearingServiceFormattedString1", PSA_DOC_1, null),
            buildJudgeApproval(DRAFT_ORDER, "hearingServiceFormattedString1", DO_DOC_2, List.of(DO_ATTACHMENT_2)),
            null, null,
            NO);
    }

    private static Arguments withSameHearingInfoAndDocumentShouldBeSortedByDocumentFilename() {
        DraftOrderDocReviewCollection doc1 = buildDraftOrderDocumentReviewWithoutSubmittedDate(DO_DOC_1, List.of(DO_ATTACHMENT_1));
        PsaDocReviewCollection doc2 = buildPsaDocReviewCollectionWithoutSumittedDate(PSA_DOC_1);
        DraftOrderDocReviewCollection doc3 = buildDraftOrderDocumentReviewWithoutSubmittedDate(DO_DOC_2, List.of(DO_ATTACHMENT_2));

        return Arguments.of(
            DraftOrdersWrapper.builder()
                .draftOrdersReviewCollection(List.of(
                    DraftOrdersReviewCollection.builder()
                        .value(applyHearingInfo1(DraftOrdersReview.builder()
                            .draftOrderDocReviewCollection(List.of(doc1))
                            .psaDocReviewCollection(List.of(doc2))).build())
                        .build(),
                    DraftOrdersReviewCollection.builder()
                        .value(applyHearingInfo1(DraftOrdersReview.builder()
                            .draftOrderDocReviewCollection(List.of(doc3))
                        ).build())
                        .build()
                ))
                .build(),
            buildJudgeApproval(DRAFT_ORDER, "hearingServiceFormattedString1", DO_DOC_1, List.of(DO_ATTACHMENT_1)),
            buildJudgeApproval(DRAFT_ORDER, "hearingServiceFormattedString1", DO_DOC_2, List.of(DO_ATTACHMENT_2)),
            buildJudgeApproval(PSA, "hearingServiceFormattedString1", PSA_DOC_1, null),
            null, null,
            NO);
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
            buildJudgeApproval(PSA, "hearingServiceFormattedString1", PSA_DOC_2, null),
            buildJudgeApproval(DRAFT_ORDER, "hearingServiceFormattedString1", DO_DOC_2, List.of(DO_ATTACHMENT_2)),
            null, null, null,
            NO);
    }

    private static Arguments withSixPsas() {
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
            buildJudgeApproval(PSA, "hearingServiceFormattedString1", PSA_DOC_1, null),
            buildJudgeApproval(PSA, "hearingServiceFormattedString1", PSA_DOC_2, null),
            buildJudgeApproval(PSA, "hearingServiceFormattedString1", PSA_DOC_3, null),
            buildJudgeApproval(PSA, "hearingServiceFormattedString1", PSA_DOC_4, null),
            buildJudgeApproval(PSA, "hearingServiceFormattedString1", PSA_DOC_5, null),
            YES);
    }

    private static Arguments withSixDraftOrders() {
        return Arguments.of(
            DraftOrdersWrapper.builder()
                .draftOrdersReviewCollection(List.of(
                    DraftOrdersReviewCollection.builder()
                        .value(applyHearingInfo2(DraftOrdersReview.builder()
                            .draftOrderDocReviewCollection(List.of(
                                buildDraftOrderDocumentReview(DO_DOC_1, List.of(), TO_BE_REVIEWED),
                                buildDraftOrderDocumentReview(DO_DOC_2, List.of(), TO_BE_REVIEWED),
                                buildDraftOrderDocumentReview(DO_DOC_3, List.of(), TO_BE_REVIEWED),
                                buildDraftOrderDocumentReview(DO_DOC_4, List.of(), TO_BE_REVIEWED),
                                buildDraftOrderDocumentReview(DO_DOC_5, List.of(), TO_BE_REVIEWED),
                                buildDraftOrderDocumentReview(DO_DOC_6, List.of(), TO_BE_REVIEWED)
                            ))
                        ).build())
                        .build()
                ))
                .build(),
            buildJudgeApproval(DRAFT_ORDER, "hearingServiceFormattedString2", DO_DOC_1, List.of()),
            buildJudgeApproval(DRAFT_ORDER, "hearingServiceFormattedString2", DO_DOC_2, List.of()),
            buildJudgeApproval(DRAFT_ORDER, "hearingServiceFormattedString2", DO_DOC_3, List.of()),
            buildJudgeApproval(DRAFT_ORDER, "hearingServiceFormattedString2", DO_DOC_4, List.of()),
            buildJudgeApproval(DRAFT_ORDER, "hearingServiceFormattedString2", DO_DOC_5, List.of()),
            // expectedWarningMessageToJudge
            YES);
    }

    private static Arguments withFiveReviewablePsas() {
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
            buildJudgeApproval(PSA, "hearingServiceFormattedString1", PSA_DOC_1, null),
            buildJudgeApproval(PSA, "hearingServiceFormattedString1", PSA_DOC_2, null),
            buildJudgeApproval(PSA, "hearingServiceFormattedString1", PSA_DOC_3, null),
            buildJudgeApproval(PSA, "hearingServiceFormattedString1", PSA_DOC_4, null),
            buildJudgeApproval(PSA, "hearingServiceFormattedString1", PSA_DOC_5, null),
            NO);
    }

    private static Arguments withDraftOrdersAndPsaAndShouldOrderBySubmittedDate() {
        return Arguments.of(
            DraftOrdersWrapper.builder()
                .draftOrdersReviewCollection(List.of(
                    DraftOrdersReviewCollection.builder()
                        .value(applyHearingInfo1(DraftOrdersReview.builder()
                            .psaDocReviewCollection(List.of(
                                buildPsaDocReviewCollection(PSA_DOC_1, TO_BE_REVIEWED, LocalDateTime.now()),
                                buildPsaDocReviewCollection(PSA_DOC_2, TO_BE_REVIEWED, LocalDateTime.now().minusDays(1)),
                                buildPsaDocReviewCollection(PSA_DOC_3, TO_BE_REVIEWED, LocalDateTime.now()),
                                buildPsaDocReviewCollection(PSA_DOC_4, TO_BE_REVIEWED, LocalDateTime.now()),
                                buildPsaDocReviewCollection(PSA_DOC_5, TO_BE_REVIEWED, LocalDateTime.now())
                            ))
                            .draftOrderDocReviewCollection(List.of(
                                buildDraftOrderDocumentReview(DO_DOC_1, List.of()),
                                buildDraftOrderDocumentReview(DO_DOC_2, List.of()),
                                buildDraftOrderDocumentReview(DO_DOC_3, List.of()),
                                buildDraftOrderDocumentReview(DO_DOC_4, List.of(), TO_BE_REVIEWED, LocalDateTime.now().minusDays(1)),
                                buildDraftOrderDocumentReview(DO_DOC_5, List.of())
                            ))
                        ).build())
                        .build()
                ))
                .build(),
            buildJudgeApproval(PSA, "hearingServiceFormattedString1", PSA_DOC_2, List.of()),
            buildJudgeApproval(DRAFT_ORDER, "hearingServiceFormattedString1", DO_DOC_4, List.of()),
            buildJudgeApproval(PSA, "hearingServiceFormattedString1", PSA_DOC_1, List.of()),
            buildJudgeApproval(PSA, "hearingServiceFormattedString1", PSA_DOC_3, List.of()),
            buildJudgeApproval(PSA, "hearingServiceFormattedString1", PSA_DOC_4, List.of()),
            // expectedWarningMessageToJudge
            YES);
    }

    private static Arguments withDraftOrdersAndPsaAndShouldOrderByHearingAndThenSubmittedDate() {
        return Arguments.of(
            DraftOrdersWrapper.builder()
                .draftOrdersReviewCollection(List.of(
                    DraftOrdersReviewCollection.builder()
                        .value(applyHearingInfo2(DraftOrdersReview.builder()
                            .psaDocReviewCollection(List.of(
                                buildPsaDocReviewCollection(PSA_DOC_1, TO_BE_REVIEWED, LocalDateTime.now()),
                                buildPsaDocReviewCollection(PSA_DOC_2, TO_BE_REVIEWED, LocalDateTime.now().minusDays(1))
                            ))
                            .draftOrderDocReviewCollection(List.of(
                                buildDraftOrderDocumentReview(DO_DOC_5, List.of(), TO_BE_REVIEWED, LocalDateTime.now().minusDays(1)),
                                buildDraftOrderDocumentReview(DO_DOC_4, List.of())
                            ))
                        ).build())
                        .build(),
                    DraftOrdersReviewCollection.builder()
                        .value(applyHearingInfo1(DraftOrdersReview.builder()
                            .psaDocReviewCollection(List.of(
                                buildPsaDocReviewCollection(PSA_DOC_3, TO_BE_REVIEWED, LocalDateTime.now()),
                                buildPsaDocReviewCollection(PSA_DOC_4, TO_BE_REVIEWED, LocalDateTime.now())
                            ))
                            .draftOrderDocReviewCollection(List.of(
                                buildDraftOrderDocumentReview(DO_DOC_1, List.of()),
                                buildDraftOrderDocumentReview(DO_DOC_2, List.of())
                            ))
                        ).build())
                        .build()
                ))
                .build(),
            buildJudgeApproval(PSA, "hearingServiceFormattedString1", PSA_DOC_3, List.of()),
            buildJudgeApproval(PSA, "hearingServiceFormattedString1", PSA_DOC_4, List.of()),
            buildJudgeApproval(DRAFT_ORDER, "hearingServiceFormattedString1", DO_DOC_1, List.of()),
            buildJudgeApproval(DRAFT_ORDER, "hearingServiceFormattedString1", DO_DOC_2, List.of()),
            buildJudgeApproval(PSA, "hearingServiceFormattedString2", PSA_DOC_2, List.of()),
            // expectedWarningMessageToJudge
            YES);
    }

    private static Stream<Arguments> provideDraftOrderData() {
        return Stream.of(
            withEmptyDraftOrdersWrapper(), withOneDraftOrderAndOnePsa(), withTwoDraftOrderAndZeroPsa(),
            withDifferentHearingInfo(),
            withSameHearingInfoAndDocumentShouldBeSortedBySubmittedDate(),
            withSameHearingInfoAndDocumentShouldBeSortedBySubmittedDate2(),
            withSameHearingInfoAndDocumentShouldBeSortedByDocumentFilename(),
            withProcessedDraftOrderAndPsa(),
            withSixPsas(),
            withFiveReviewablePsas(),
            withDraftOrdersAndPsaAndShouldOrderBySubmittedDate(),
            withDraftOrdersAndPsaAndShouldOrderByHearingAndThenSubmittedDate(),
            withSixDraftOrders()
        );
    }
}
