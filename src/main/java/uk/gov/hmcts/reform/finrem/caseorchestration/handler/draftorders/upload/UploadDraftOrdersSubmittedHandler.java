package uk.gov.hmcts.reform.finrem.caseorchestration.handler.draftorders.upload;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.DraftOrdersNotificationRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed.AgreedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed.AgreedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.suggested.SuggestedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.suggested.SuggestedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UploadDraftOrdersSubmittedHandler extends FinremCallbackHandler {

    private final NotificationService notificationService;

    private final DraftOrdersNotificationRequestMapper draftOrdersNotificationRequestMapper;

    private static final String CONFIRMATION_HEADER = "# Draft orders uploaded";

    private static final String AGREED_DRAFT_ORDER_CONFIRMATION_BODY_FORMAT =
        "<br>You have uploaded your documents. They will now be reviewed by the Judge."
            + "<br><br>You can find the draft orders that you have uploaded on the "
            + "['case documents' tab](/cases/case-details/%s#Case%%20documents).";

    private static final String SUGGESTED_DRAFT_ORDER_CONFIRMATION_BODY_FORMAT =
        "<br>You have uploaded your documents. They have now been saved to the case."
            + "<br><br>You can find the documents that you have uploaded on the "
            + "['case documents' tab](/cases/case-details/%s#Case%%20documents).";

    public UploadDraftOrdersSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper, NotificationService notificationService,
                                             DraftOrdersNotificationRequestMapper draftOrdersNotificationRequestMapper) {
        super(finremCaseDetailsMapper);
        this.notificationService = notificationService;
        this.draftOrdersNotificationRequestMapper = draftOrdersNotificationRequestMapper;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.DRAFT_ORDERS.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(
        FinremCallbackRequest finremCallbackRequest, String userAuthorisation) {
        log.info("Invoking contested {} submitted callback for Case ID: {}",
            finremCallbackRequest.getEventType(), finremCallbackRequest.getCaseDetails().getId());

        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        NotificationRequest judgeNotificationRequest = draftOrdersNotificationRequestMapper.buildJudgeNotificationRequest(caseDetails);
        notificationService.sendContestedReadyToReviewOrderToJudge(judgeNotificationRequest);

        caseDetails.getData().getDraftOrdersWrapper().setUploadSuggestedDraftOrder(null); // Clear the temporary field
        caseDetails.getData().getDraftOrdersWrapper().setUploadAgreedDraftOrder(null); // Clear the temporary field

        String confirmationBody = getConfirmationBody(caseDetails);
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .confirmationHeader(CONFIRMATION_HEADER)
            .confirmationBody(confirmationBody)
            .build();
    }

    private String getConfirmationBody(FinremCaseDetails caseDetails) {
        String caseReference = String.valueOf(caseDetails.getId());
        String confirmationBodyFormat = isLatestUploadAnAgreedDraftOrder(caseDetails.getData(), caseReference)
            ? AGREED_DRAFT_ORDER_CONFIRMATION_BODY_FORMAT : SUGGESTED_DRAFT_ORDER_CONFIRMATION_BODY_FORMAT;

        return String.format(confirmationBodyFormat, caseReference, caseReference);
    }

    private boolean isLatestUploadAnAgreedDraftOrder(FinremCaseData caseData, String caseReference) {
        DraftOrdersWrapper draftOrdersWrapper = caseData.getDraftOrdersWrapper();
        Optional<LocalDateTime> latestAgreedDraftOrderDate = getLatestAgreedDraftOrder(
            draftOrdersWrapper.getAgreedDraftOrderCollection());
        Optional<LocalDateTime> latestSuggestedDraftOrderDate = getLatestSuggestedDraftOrder(
            draftOrdersWrapper.getSuggestedDraftOrderCollection());

        if (latestAgreedDraftOrderDate.isEmpty() && latestSuggestedDraftOrderDate.isEmpty()) {
            String exceptionMessage = String.format("No uploaded draft order found for Case ID: %s", caseReference);
            throw new IllegalStateException(exceptionMessage);
        }

        return latestAgreedDraftOrderDate
            .map(dateTime -> latestSuggestedDraftOrderDate.map(dateTime::isAfter)
                .orElse(true))
            .orElse(false);
    }

    private Optional<LocalDateTime> getLatestAgreedDraftOrder(
        List<AgreedDraftOrderCollection> agreedDraftOrderCollection) {
        return Optional.ofNullable(agreedDraftOrderCollection)
            .orElse(Collections.emptyList())
            .stream()
            .map(AgreedDraftOrderCollection::getValue)
            .map(AgreedDraftOrder::getSubmittedDate)
            .max(LocalDateTime::compareTo);
    }

    private Optional<LocalDateTime> getLatestSuggestedDraftOrder(
        List<SuggestedDraftOrderCollection> suggestedDraftOrderCollection) {
        return Optional.ofNullable(suggestedDraftOrderCollection)
            .orElse(Collections.emptyList())
            .stream()
            .map(SuggestedDraftOrderCollection::getValue)
            .map(SuggestedDraftOrder::getSubmittedDate)
            .max(LocalDateTime::compareTo);
    }
}
