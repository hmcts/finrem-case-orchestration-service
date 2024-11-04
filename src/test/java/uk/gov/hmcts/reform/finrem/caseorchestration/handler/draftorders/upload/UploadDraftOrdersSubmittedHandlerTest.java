package uk.gov.hmcts.reform.finrem.caseorchestration.handler.draftorders.upload;

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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed.AgreedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed.AgreedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.suggested.SuggestedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.suggested.SuggestedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.UploadAgreedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.UploadSuggestedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
class UploadDraftOrdersSubmittedHandlerTest {

    @InjectMocks
    private UploadDraftOrdersSubmittedHandler uploadDraftOrdersSubmittedHandler;

    @Mock
    private NotificationService notificationService;

    @Mock
    private DraftOrdersNotificationRequestMapper draftOrdersNotificationRequestMapper;


    @Test
    void testCanHandle() {
        Assertions.assertCanHandle(uploadDraftOrdersSubmittedHandler, CallbackType.SUBMITTED, CaseType.CONTESTED,
            EventType.DRAFT_ORDERS);
    }

    @Test
    void givenCaseWithNoUploadedDraftOrdersWhenHandleThenThrowsException() {
        String caseReference = "1727874196328932";
        FinremCaseData caseData = FinremCaseData.builder()
            .build();
        FinremCallbackRequest request = FinremCallbackRequestFactory.from(Long.parseLong(caseReference),
            CaseType.CONTESTED, caseData);

        assertThatThrownBy(() -> {
            uploadDraftOrdersSubmittedHandler.handle(request, AUTH_TOKEN);
        }).isInstanceOf(IllegalStateException.class)
            .hasMessage("No uploaded draft order found for Case ID: " + caseReference);
    }

    @ParameterizedTest
    @MethodSource
    void testHandle(List<AgreedDraftOrderCollection> agreedDraftOrderCollection,
                    List<SuggestedDraftOrderCollection> suggestedDraftOrderCollection,
                    boolean isAgreedDraftOrderUpload) {
        String caseReference = "1727874196328932";
        FinremCaseData caseData = FinremCaseData.builder()
            .build();
        DraftOrdersWrapper draftOrdersWrapper = caseData.getDraftOrdersWrapper();
        draftOrdersWrapper.setAgreedDraftOrderCollection(agreedDraftOrderCollection);
        draftOrdersWrapper.setSuggestedDraftOrderCollection(suggestedDraftOrderCollection);

        FinremCallbackRequest request = FinremCallbackRequestFactory.from(Long.parseLong(caseReference),
            CaseType.CONTESTED, caseData);

        var response = uploadDraftOrdersSubmittedHandler.handle(request, AUTH_TOKEN);

        String expectedConfirmationBody = isAgreedDraftOrderUpload
            ? getExpectedAgreedConfirmationBody((caseReference))
            : getExpectedSuggestedConfirmationBody(caseReference);

        verify(draftOrdersNotificationRequestMapper).buildJudgeNotificationRequest(any(FinremCaseDetails.class));
        verify(notificationService).sendContestedReadyToReviewOrderToJudge(any());
        assertThat(response.getConfirmationHeader()).isEqualTo("# Draft orders uploaded");
        assertThat(response.getConfirmationBody()).isEqualTo(expectedConfirmationBody);
        assertThat(response.getData()).isNull();
        assertThat(response.getWarnings()).isEmpty();
        assertThat(response.getErrors()).isEmpty();
    }

    private static Stream<Arguments> testHandle() {
        return Stream.of(
            Arguments.of(
                agreedDraftOrdersCollection(List.of(LocalDateTime.of(2024, 10, 10, 1, 0, 0))),
                Collections.emptyList(),
                true),
            Arguments.of(
                Collections.emptyList(),
                suggestedOrdersCollection(List.of(LocalDateTime.of(2024, 10, 10, 1, 0, 0))),
                false),
            Arguments.of(
                agreedDraftOrdersCollection(List.of(LocalDateTime.of(2024, 10, 10, 1, 0, 1))),
                suggestedOrdersCollection(List.of(LocalDateTime.of(2024, 10, 10, 1, 0, 0))),
                true),
            Arguments.of(
                agreedDraftOrdersCollection(List.of(LocalDateTime.of(2024, 10, 10, 1, 0, 1))),
                suggestedOrdersCollection(List.of(LocalDateTime.of(2024, 10, 10, 1, 0, 2))),
                false),
            Arguments.of(
                agreedDraftOrdersCollection(List.of(LocalDateTime.of(2024, 9, 10, 1, 0, 1),
                    LocalDateTime.of(2024, 10, 10, 1, 0, 1))),
                suggestedOrdersCollection(List.of(LocalDateTime.of(2024, 10, 8, 1, 0, 0),
                    LocalDateTime.of(2024, 10, 7, 1, 0, 1))),
                true),
            Arguments.of(
                agreedDraftOrdersCollection(List.of(LocalDateTime.of(2023, 10, 10, 1, 0, 1),
                    LocalDateTime.of(2024, 10, 10, 1, 0, 4))),
                suggestedOrdersCollection(List.of(LocalDateTime.of(2024, 3, 10, 1, 0, 2),
                    LocalDateTime.of(2024, 10, 10, 1, 5, 4))),
                false)
        );
    }

    private static List<AgreedDraftOrderCollection> agreedDraftOrdersCollection(
        List<LocalDateTime> agreedDraftOrderSubmittedDates) {

        return agreedDraftOrderSubmittedDates.stream()
            .map(dateTime -> AgreedDraftOrder.builder().submittedDate(dateTime).build())
            .map(value -> AgreedDraftOrderCollection.builder().value(value).build())
            .toList();
    }

    private static List<SuggestedDraftOrderCollection> suggestedOrdersCollection(
        List<LocalDateTime> suggestedDraftOrderSubmittedDates) {

        return suggestedDraftOrderSubmittedDates.stream()
            .map(dateTime -> SuggestedDraftOrder.builder().submittedDate(dateTime).build())
            .map(value -> SuggestedDraftOrderCollection.builder().value(value).build())
            .toList();
    }

    private String getExpectedAgreedConfirmationBody(String caseReference) {
        return "<br>You have uploaded your documents. They will now be reviewed by the Judge."
            + "<br><br>You can find the draft orders that you have uploaded on the "
            + "['case documents' tab](/cases/case-details/"
            + caseReference
            + "#Case%20documents).";
    }

    private String getExpectedSuggestedConfirmationBody(String caseReference) {
        return "<br>You have uploaded your documents. They have now been saved to the case."
            + "<br><br>You can find the documents that you have uploaded on the "
            + "['case documents' tab](/cases/case-details/"
            + caseReference
            + "#Case%20documents).";
    }

    @Test
    void testHandleClearsUploadedDraftOrders() {
        String caseReference = "1727874196328932";
        FinremCaseData caseData = FinremCaseData.builder()
            .draftOrdersWrapper(DraftOrdersWrapper.builder()
                .uploadSuggestedDraftOrder(UploadSuggestedDraftOrder.builder().build())
                .uploadAgreedDraftOrder(UploadAgreedDraftOrder.builder().build())
                .agreedDraftOrderCollection(agreedDraftOrdersCollection(List.of(LocalDateTime.now())))
                .build())
            .build();
        FinremCallbackRequest request = FinremCallbackRequestFactory.from(Long.parseLong(caseReference),
            CaseType.CONTESTED, caseData);

        uploadDraftOrdersSubmittedHandler.handle(request, AUTH_TOKEN);

        assertThat(caseData.getDraftOrdersWrapper().getUploadSuggestedDraftOrder()).isNull();
        assertThat(caseData.getDraftOrdersWrapper().getUploadAgreedDraftOrder()).isNull();
    }
}
