package uk.gov.hmcts.reform.finrem.caseorchestration.handler.judgeapproval;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UuidCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.RefusedOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.RefusedOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.draftorders.RefusedOrderCorrespondenceRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.draftorders.RefusedOrderCorresponder;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class ApproveDraftOrdersSubmittedHandlerTest {
    @InjectMocks
    private ApproveDraftOrdersSubmittedHandler handler;
    @Mock
    private RefusedOrderCorresponder refusedOrderCorresponder;
    @Captor
    private ArgumentCaptor<RefusedOrderCorrespondenceRequest> requestCaptor;

    @Test
    void canHandle() {
        assertCanHandle(handler, CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.APPROVE_ORDERS);
    }

    @Test
    void givenCaseHasRefusedOrders_whenSubmittedHandled_thenSendsCorrespondence() {
        UUID uuidOne = UUID.randomUUID();
        UUID uuidTwo = UUID.randomUUID();

        FinremCaseData caseData = FinremCaseData.builder()
            .draftOrdersWrapper(DraftOrdersWrapper.builder()
                .refusedOrdersCollection(List.of(
                    RefusedOrderCollection.builder()
                        .id(uuidOne)
                        .value(RefusedOrder.builder().judgeFeedback("order 1").build())
                        .build(),
                    RefusedOrderCollection.builder()
                        .id(uuidOne)
                        .value(RefusedOrder.builder().judgeFeedback("order 2").build())
                        .build()
                ))
                .refusalOrderIdsToBeSent(List.of(
                    UuidCollection.builder().value(uuidOne).build(),
                    UuidCollection.builder().value(uuidTwo).build()
                ))
                .approveOrdersConfirmationBody("The draft orders have been reviewed and approved.")
                .build())
            .build();

        FinremCallbackRequest request = FinremCallbackRequestFactory.from(caseData);
        var response = handler.handle(request, AUTH_TOKEN);

        verifyHandlerResponse(response);
        verify(refusedOrderCorresponder, times(1)).sendRefusedOrder(requestCaptor.capture());

        RefusedOrderCorrespondenceRequest correspondenceRequest = requestCaptor.getValue();
        assertThat(correspondenceRequest.authorisationToken()).isEqualTo(AUTH_TOKEN);
        assertThat(correspondenceRequest.caseDetails()).isEqualTo(request.getCaseDetails());
        assertThat(correspondenceRequest.refusedOrders())
            .extracting(RefusedOrder::getJudgeFeedback)
            .containsExactlyInAnyOrder("order 1", "order 2");
    }

    @Test
    void givenCaseHasNoRefusedOrders_whenSubmittedHandled_thenDoesNotSendCorrespondence() {
        FinremCaseData caseData = FinremCaseData.builder()
            .draftOrdersWrapper(DraftOrdersWrapper.builder()
                .approveOrdersConfirmationBody("The draft orders have been reviewed and approved.")
                .build())
            .build();

        FinremCallbackRequest request = FinremCallbackRequestFactory.from(caseData);
        var response = handler.handle(request, AUTH_TOKEN);

        verifyHandlerResponse(response);
        verifyNoInteractions(refusedOrderCorresponder);
    }

    @Test
    void givenCaseHasRefusedOrderNotInToBeSent_whenSubmittedHandled_thenDoesNotSendCorrespondence() {
        UUID uuidOne = UUID.randomUUID();
        UUID uuidTwo = UUID.randomUUID();

        FinremCaseData caseData = FinremCaseData.builder()
            .draftOrdersWrapper(DraftOrdersWrapper.builder()
                .refusedOrdersCollection(List.of(
                    RefusedOrderCollection.builder()
                        .id(uuidOne)
                        .value(RefusedOrder.builder().judgeFeedback("order 1").build())
                        .build()
                ))
                .refusalOrderIdsToBeSent(List.of(
                    UuidCollection.builder().value(uuidTwo).build()
                ))
                .approveOrdersConfirmationBody("The draft orders have been reviewed and approved.")
                .build())
            .build();
        FinremCallbackRequest request = FinremCallbackRequestFactory.from(caseData);

        var response = handler.handle(request, AUTH_TOKEN);

        verifyHandlerResponse(response);
        verifyNoInteractions(refusedOrderCorresponder);
    }

    @Test
    void givenCaseHasRefusalOrdersIdsAlreadySent_whenSubmittedHandled_thenDoesNotSendCorrespondence() {
        UUID uuidOne = UUID.randomUUID();
        UUID uuidTwo = UUID.randomUUID();

        FinremCaseData caseData = FinremCaseData.builder()
            .draftOrdersWrapper(DraftOrdersWrapper.builder()
                .refusalOrderIdsToBeSent(List.of(
                    UuidCollection.builder().value(uuidOne).build(),
                    UuidCollection.builder().value(uuidTwo).build()
                ))
                .approveOrdersConfirmationBody("The draft orders have been reviewed and approved.")
                .build())
            .build();
        FinremCallbackRequest request = FinremCallbackRequestFactory.from(caseData);

        var response = handler.handle(request, AUTH_TOKEN);

        verifyHandlerResponse(response);
        verifyNoInteractions(refusedOrderCorresponder);
    }

    private void verifyHandlerResponse(GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response) {
        assertThat(response.getData()).isNotNull();
        assertThat(response.getConfirmationHeader()).isEqualTo("# Draft orders reviewed");
        assertThat(response.getConfirmationBody()).isEqualTo("The draft orders have been reviewed and approved.");
        assertThat(response.getErrors()).isEmpty();
        assertThat(response.getWarnings()).isEmpty();
    }
}
