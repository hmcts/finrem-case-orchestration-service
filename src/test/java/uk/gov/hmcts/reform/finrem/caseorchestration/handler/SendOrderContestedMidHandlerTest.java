package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Yes;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.AttachmentToShare;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.AttachmentToShareCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.OrderToShare;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.OrderToShareCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.OrdersToSend;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.SendOrderWrapper;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.MID_EVENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.SEND_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class SendOrderContestedMidHandlerTest {

    private static final String AT_LEAST_ONE_ORDER_MESSAGE = "You must select at least one order.";

    private static final String NONE_SUPPORTING_DOCUMENTS_SELECTED = "You chose to include a supporting document but none have been selected.";

    @InjectMocks
    private SendOrderContestedMidHandler underTest;

    @Test
    void testCanHandle() {
        assertCanHandle(underTest, MID_EVENT, CONTESTED, SEND_ORDER);
    }

    @ParameterizedTest
    @MethodSource("noOrderSelectedData")
    void givenCallbackRequest_whenNoOrderSelected_thenShowAnErrorMessage(FinremCaseData.FinremCaseDataBuilder builder) {
        FinremCallbackRequest finremCallbackRequest = FinremCallbackRequestFactory.from(builder.build());
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = underTest.handle(finremCallbackRequest, AUTH_TOKEN);

        assertThat(response)
            .extracting(GenericAboutToStartOrSubmitCallbackResponse::getErrors)
            .asInstanceOf(InstanceOfAssertFactories.collection(String.class))
            .contains(AT_LEAST_ONE_ORDER_MESSAGE);
    }

    private static Stream<Arguments> noOrderSelectedData() {
        return Stream.of(Arguments.of(
            FinremCaseData.builder(),
            FinremCaseData.builder().sendOrderWrapper(SendOrderWrapper.builder().build()),
            FinremCaseData.builder().sendOrderWrapper(SendOrderWrapper.builder().ordersToSend(OrdersToSend.builder().build()).build()),
            FinremCaseData.builder().sendOrderWrapper(SendOrderWrapper.builder()
                .ordersToSend(OrdersToSend.builder().value(List.of()).build())
                .build())
        ));
    }

    @ParameterizedTest
    @MethodSource("noSupportingDocumentsSelectedData")
    void givenCallbackRequest_whenNoSupportingDocumentsSelected_thenShowAnErrorMessage(OrdersToSend ordersToSend) {
        FinremCallbackRequest finremCallbackRequest = FinremCallbackRequestFactory.from(FinremCaseData.builder()
            .sendOrderWrapper(SendOrderWrapper.builder().ordersToSend(ordersToSend).build())
            .build());
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = underTest.handle(finremCallbackRequest, AUTH_TOKEN);

        assertThat(response)
            .extracting(GenericAboutToStartOrSubmitCallbackResponse::getErrors)
            .asInstanceOf(InstanceOfAssertFactories.collection(String.class))
            .contains(NONE_SUPPORTING_DOCUMENTS_SELECTED);
    }

    private static Stream<Arguments> noSupportingDocumentsSelectedData() {
        return Stream.of(Arguments.of(
            OrdersToSend.builder().value(List.of(
                OrderToShareCollection.builder()
                    .value(OrderToShare.builder()
                        .documentToShare(YesOrNo.YES)
                        .includeSupportingDocument(List.of(Yes.YES))
                        .attachmentsToShare(List.of(
                            AttachmentToShareCollection.builder().value(AttachmentToShare.builder().documentToShare(YesOrNo.NO).build()).build()
                        ))
                        .build())
                .build())
            ).build(),
            OrdersToSend.builder().value(List.of(
                OrderToShareCollection.builder()
                    .value(OrderToShare.builder()
                        .documentToShare(YesOrNo.YES)
                        .includeSupportingDocument(List.of(Yes.YES))
                        .attachmentsToShare(List.of(
                            AttachmentToShareCollection.builder().value(AttachmentToShare.builder().documentToShare(YesOrNo.NO).build()).build(),
                            AttachmentToShareCollection.builder().value(AttachmentToShare.builder().documentToShare(YesOrNo.NO).build()).build()
                        ))
                        .build())
                    .build()
            )).build(),
            OrdersToSend.builder().value(List.of(
                OrderToShareCollection.builder()
                .value(OrderToShare.builder()
                    .documentToShare(YesOrNo.YES)
                    .includeSupportingDocument(List.of(Yes.YES))
                    .build())
                .build()
            )).build(),
            OrdersToSend.builder().value(List.of(
                OrderToShareCollection.builder()
                    .value(OrderToShare.builder()
                        .documentToShare(YesOrNo.YES)
                        .includeSupportingDocument(List.of(Yes.YES))
                        .attachmentsToShare(List.of(
                            AttachmentToShareCollection.builder().value(AttachmentToShare.builder().documentToShare(YesOrNo.YES).build()).build()
                        ))
                        .build())
                    .build(),
                OrderToShareCollection.builder()
                    .value(OrderToShare.builder()
                        .documentToShare(YesOrNo.YES)
                        .includeSupportingDocument(List.of(Yes.YES))
                        .attachmentsToShare(List.of(
                            AttachmentToShareCollection.builder().value(AttachmentToShare.builder().documentToShare(YesOrNo.NO).build()).build()
                        ))
                        .build())
                .build()
            )).build()
        ));
    }
}
