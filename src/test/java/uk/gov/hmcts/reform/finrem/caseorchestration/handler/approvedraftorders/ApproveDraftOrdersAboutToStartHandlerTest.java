package uk.gov.hmcts.reform.finrem.caseorchestration.handler.approvedraftorders;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocumentReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.PsaDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.PsaDocumentReview;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus.APPROVED_BY_JUDGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus.PROCESSED_BY_ADMIN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus.TO_BE_REVIEWED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class ApproveDraftOrdersAboutToStartHandlerTest {

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

        DynamicList hearingsReadyForReview = caseData.getDraftOrdersWrapper().getHearingsReadyForReview();
        assertThat(hearingsReadyForReview).isNotNull();
        Assertions.assertEquals(1, hearingsReadyForReview.getListItems().size());
        Assertions.assertEquals("Hearing Type 1 on 2024-08-06 09:00 A.M. by Judge 1", hearingsReadyForReview.getListItems().get(0).getLabel());
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

        DynamicList hearingsReadyForReview = caseData.getDraftOrdersWrapper().getHearingsReadyForReview();
        assertThat(hearingsReadyForReview).isNotNull();
        Assertions.assertEquals(1, hearingsReadyForReview.getListItems().size());
        Assertions.assertEquals("Hearing Type 1 on 2024-08-06 09:00 A.M. by Judge 1", hearingsReadyForReview.getListItems().get(0).getLabel());
    }

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
        assertThat(response.getErrors()).contains("There are no draft orders or pension sharing annexes to review.");
    }

    @Test
    void givenUserHasDraftOrders_whenHandle_thenReturnError() {
        FinremCaseData caseData = spy(new FinremCaseData());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(
            FinremCallbackRequestFactory.from(1727874196328932L, caseData), AUTH_TOKEN);

        Assertions.assertEquals(1, response.getErrors().size());
        assertThat(response.getErrors()).contains("There are no draft orders or pension sharing annexes to review.");
    }

}
