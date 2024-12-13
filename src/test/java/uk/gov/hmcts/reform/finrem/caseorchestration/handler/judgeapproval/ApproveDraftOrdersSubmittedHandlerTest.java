package uk.gov.hmcts.reform.finrem.caseorchestration.handler.judgeapproval;

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
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.DraftOrdersNotificationRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UuidCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.RefusedOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.RefusedOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class ApproveDraftOrdersSubmittedHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private DraftOrdersNotificationRequestMapper notificationRequestMapper;

    @InjectMocks
    private ApproveDraftOrdersSubmittedHandler handler;

    @Test
    void canHandle() {
        assertCanHandle(handler, CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.APPROVE_ORDERS);
    }

    @ParameterizedTest
    @MethodSource("invokeNotificationServiceForRefusalOrdersData")
    void shouldInvokeNotificationServiceForRefusalOrders(DraftOrdersWrapper draftOrdersWrapper, int expectedInvocationCount) {
        // Arrange
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(
            FinremCaseData.builder()
                .draftOrdersWrapper(draftOrdersWrapper)
                .build()
        );

        NotificationRequest expectedNotificationRequest = new NotificationRequest();
        lenient().when(notificationRequestMapper.buildRefusedDraftOrderOrPsaNotificationRequest(any(), any()))
            .thenReturn(expectedNotificationRequest);

        // Act
        handler.handle(callbackRequest, AUTH_TOKEN);

        // Assert
        verify(notificationService, times(expectedInvocationCount)).sendDraftOrderOrPsaRefused(expectedNotificationRequest);
        verify(notificationRequestMapper, times(expectedInvocationCount)).buildRefusedDraftOrderOrPsaNotificationRequest(any(FinremCaseDetails.class),
            any(RefusedOrder.class));
    }

    private static Stream<Arguments> invokeNotificationServiceForRefusalOrdersData() {
        UUID randomUUID = UUID.randomUUID();
        UUID randomUUID2 = UUID.randomUUID();
        return Stream.of(
            Arguments.of(DraftOrdersWrapper.builder().build(), 0),
            Arguments.of(DraftOrdersWrapper.builder().refusedOrdersCollection(List.of()).build(), 0),
            Arguments.of(DraftOrdersWrapper.builder()
                .refusedOrdersCollection(List.of(
                    RefusedOrderCollection
                        .builder()
                        .id(randomUUID)
                        .value(RefusedOrder.builder().submittedByEmail("abc@abc.com").build())
                    .build()
                )).build(), 0), // without refusalOrderIdsToBeSent
            Arguments.of(DraftOrdersWrapper.builder()
                .refusalOrderIdsToBeSent(List.of(
                    UuidCollection.builder().value(randomUUID).build()
                ))
                .refusedOrdersCollection(List.of(
                    RefusedOrderCollection
                        .builder()
                        .id(randomUUID)
                        .value(RefusedOrder.builder().submittedByEmail("abc@abc.com").build())
                        .build()
                )).build(), 1), // happy path
            Arguments.of(DraftOrdersWrapper.builder()
                .refusalOrderIdsToBeSent(List.of(
                    UuidCollection.builder().value(randomUUID).build()
                ))
                .refusedOrdersCollection(List.of(
                    RefusedOrderCollection
                        .builder()
                        .id(randomUUID)
                        .value(RefusedOrder.builder().build())
                        .build()
                )).build(), 0), // missing submittedByEmail
            Arguments.of(DraftOrdersWrapper.builder()
                .refusalOrderIdsToBeSent(List.of(
                    UuidCollection.builder().value(randomUUID).build(),
                    UuidCollection.builder().value(randomUUID2).build()
                ))
                .refusedOrdersCollection(List.of(
                    RefusedOrderCollection
                        .builder()
                        .id(randomUUID)
                        .value(RefusedOrder.builder().submittedByEmail("abc@abc.com").build())
                        .build(),
                    RefusedOrderCollection
                        .builder()
                        .id(randomUUID2)
                        .value(RefusedOrder.builder().submittedByEmail("abc@abc.com").build())
                        .build()
                )).build(), 2), // two ids with two refusal orders
            Arguments.of(DraftOrdersWrapper.builder()
                .refusalOrderIdsToBeSent(List.of(
                    UuidCollection.builder().value(randomUUID).build()
                ))
                .refusedOrdersCollection(List.of(
                    RefusedOrderCollection
                        .builder()
                        .id(randomUUID2)
                        .value(RefusedOrder.builder().submittedByEmail("abc@abc.com").build())
                        .build()
                )).build(), 0), // with ID and a refusal order, but it does not match
            Arguments.of(DraftOrdersWrapper.builder()
                .refusalOrderIdsToBeSent(List.of(
                    UuidCollection.builder().value(randomUUID).build()
                ))
                .refusedOrdersCollection(List.of(
                    RefusedOrderCollection
                        .builder()
                        .value(RefusedOrder.builder().submittedByEmail("abc@abc.com").build())
                        .build()
                )).build(), 0) // refusal order without id
        );
    }

}
