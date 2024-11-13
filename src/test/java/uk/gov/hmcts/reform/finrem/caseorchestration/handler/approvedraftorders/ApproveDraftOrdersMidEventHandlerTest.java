package uk.gov.hmcts.reform.finrem.caseorchestration.handler.approvedraftorders;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.PsaDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.PsaDocumentReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingService;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.lenient;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class ApproveDraftOrdersMidEventHandlerTest {

    @Mock
    private HearingService hearingService;

    @InjectMocks
    private ApproveDraftOrdersMidEventHandler handler;

    @Test
    void canHandle() {
        assertCanHandle(handler, Arguments.of(CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.APPROVE_ORDERS),
            Arguments.of(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.APPROVE_ORDERS));
    }

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

        assertNotNull(responseDraftOrdersWrapper.getJudgeApproval());
        assertEquals(expectedReviewableDraftOrder1, responseDraftOrdersWrapper.getJudgeApproval().getReviewableDraftOrder1());
        assertEquals(expectedReviewablePsa1, responseDraftOrdersWrapper.getJudgeApproval().getReviewablePsa1());
        assertEquals(expectedReviewableDraftOrder2, responseDraftOrdersWrapper.getJudgeApproval().getReviewableDraftOrder2());
        assertEquals(expectedReviewablePsa2, responseDraftOrdersWrapper.getJudgeApproval().getReviewablePsa2());
        assertEquals(expectedReviewableDraftOrder3, responseDraftOrdersWrapper.getJudgeApproval().getReviewableDraftOrder3());
        assertEquals(expectedReviewablePsa3, responseDraftOrdersWrapper.getJudgeApproval().getReviewablePsa3());
        assertEquals(expectedReviewableDraftOrder4, responseDraftOrdersWrapper.getJudgeApproval().getReviewableDraftOrder4());
        assertEquals(expectedReviewablePsa4, responseDraftOrdersWrapper.getJudgeApproval().getReviewablePsa4());
        assertEquals(expectedReviewableDraftOrder5, responseDraftOrdersWrapper.getJudgeApproval().getReviewableDraftOrder5());
        assertEquals(expectedReviewablePsa5, responseDraftOrdersWrapper.getJudgeApproval().getReviewablePsa5());
        assertEquals(expectedWarningMessageToJudge, responseDraftOrdersWrapper.getJudgeApproval().getWarningMessageToJudge());
    }

    private static Stream<Arguments> provideDraftOrderData() {
        CaseDocument sampleDraftOrderDocument = CaseDocument.builder().documentFilename("sampleDocument").build();
        CaseDocument samplePsaDocument = CaseDocument.builder().documentFilename("samplePsaDocument").build();
        CaseDocument sampleDraftOrderDocument2 = CaseDocument.builder().documentFilename("sampleDocument2").build();
        CaseDocument samplePsaDocument2 = CaseDocument.builder().documentFilename("samplePsaDocument2").build();
        CaseDocument attachment1 = CaseDocument.builder().documentFilename("attachment1").build();

        String warningMessage = "This page is limited to showing only 5 draft orders/pension sharing annexes requiring review. "
            + "There are additional draft orders/pension sharing annexes requiring review that are not shown.";

        return Stream.of(
            // Test with empty DraftOrdersWrapper
            Arguments.of(
                DraftOrdersWrapper.builder().draftOrdersReviewCollection(Collections.emptyList()).build(),
                null, null, null, null, null,
                null, null, null, null, null,
                null
            ),
            // Test with non-empty DraftOrdersWrapper with one draft order and one PSA
            Arguments.of(
                DraftOrdersWrapper.builder()
                    .draftOrdersReviewCollection(List.of(
                        DraftOrdersReviewCollection.builder()
                            .value(DraftOrdersReview.builder()
                                .hearingDate(LocalDate.of(2024, 10, 31))
                                .hearingTime("09:00")
                                .hearingType("hearingType")
                                .hearingJudge("Mr. Judge")
                                .draftOrderDocReviewCollection(List.of(
                                    DraftOrderDocReviewCollection.builder()
                                        .value(DraftOrderDocumentReview.builder()
                                            .draftOrderDocument(sampleDraftOrderDocument)
                                            .attachments(List.of(
                                                CaseDocumentCollection.builder().value(attachment1)
                                                    .build()))
                                            .build())
                                        .build()
                                ))
                                .psaDocReviewCollection(List.of(
                                    PsaDocReviewCollection.builder()
                                        .value(PsaDocumentReview.builder()
                                            .psaDocument(samplePsaDocument)
                                            .build())
                                        .build()
                                ))
                                .build())
                            .build()
                    ))
                    .build(),
                // expectedReviewableDraftOrder1
                ReviewableDraftOrder.builder()
                    .hearingInfo("hearingServiceFormattedString1")
                    .document(sampleDraftOrderDocument)
                    .attachments(List.of(CaseDocumentCollection.builder().value(attachment1).build()))
                    .hasAttachment(YesOrNo.YES)
                    .build(),
                null, null, null, null,
                // expectedReviewablePsa1
                ReviewablePsa.builder()
                    .hearingInfo("hearingServiceFormattedString1")
                    .document(samplePsaDocument)
                    .build(),
                null, null, null, null,
                // expectedWarningMessageToJudge
                null)
        );
    }
}
