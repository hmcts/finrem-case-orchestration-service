package uk.gov.hmcts.reform.finrem.caseorchestration.handler.judgeapproval;

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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UuidCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Slf4j
@Service
public class ApproveDraftOrdersSubmittedHandler extends FinremCallbackHandler {

    private final NotificationService notificationService;

    private final DraftOrdersNotificationRequestMapper notificationRequestMapper;

    private static final String CONFIRMATION_HEADER = "# Draft orders reviewed";

    private static final String ORDERS_APPROVED_CONFIRMATION_BODY_FORMAT =
        "<br>The draft orders (%s) for the case have been approved. "
            + "They are now ready for caseworker review.";

    private static final String ORDERS_REQUIRE_REPRESENTATIVE_CHANGE_CONFIRMATION_BODY_FORMAT =
        "<br>You have said that the legal representative needs to make some changes to the draft order(s). "
            + "They have been sent an email with your reasons for rejecting the orders (%s)";

    private static final String ORDERS_AMENDED_CHANGE_CONFIRMATION_BODY_FORMAT =
        "<br>You have amended the draft orders (%s). They are now ready for caseworker review";

    private static final String ORDERS_REVIEW_LATER_CONFIRMATION_BODY_FORMAT =
        "<br>You have said you will review draft orders (%s) later. These will remain on the"
            + "['Draft Orders' tab](/cases/case-details/%s#Draft%%20orders).";

    public ApproveDraftOrdersSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper, NotificationService notificationService,
                                              DraftOrdersNotificationRequestMapper notificationRequestMapper) {
        super(finremCaseDetailsMapper);
        this.notificationService = notificationService;
        this.notificationRequestMapper = notificationRequestMapper;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.APPROVE_ORDERS.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        String caseId = String.valueOf(caseDetails.getId());
        log.info("Invoking contested {} submitted event callback for Case ID: {}", callbackRequest.getEventType(), caseId);

        FinremCaseData finremCaseData = caseDetails.getData();
        DraftOrdersWrapper draftOrdersWrapper = finremCaseData.getDraftOrdersWrapper();
        List<UUID> refusalOrderIdsToBeSent =
            ofNullable(draftOrdersWrapper.getRefusalOrderIdsToBeSent()).orElse(List.of()).stream().map(UuidCollection::getValue).toList();

        ofNullable(draftOrdersWrapper.getRefusedOrdersCollection()).orElse(List.of()).stream()
            .filter(d -> refusalOrderIdsToBeSent.contains(d.getId()))
            .forEach(a -> {
                if (!isEmpty(a.getValue().getSubmittedByEmail())) {
                    notificationService.sendRefusedDraftOrderOrPsa(notificationRequestMapper
                        .buildRefusedDraftOrderOrPsaNotificationRequest(caseDetails, a.getValue()));
                } else {
                    // TODO DFR-3497 send refusal order by post. Take a look on ContestedDraftOrderNotApprovedController.sendRefusalReason
                }
            });

        //Build confirmation body
        String confirmationBody = buildConfirmationBody(caseDetails, draftOrdersWrapper);
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(finremCaseData)
            .confirmationHeader(CONFIRMATION_HEADER)
            .confirmationBody(confirmationBody)
            .build();
    }

    private String buildConfirmationBody(FinremCaseDetails caseDetails, DraftOrdersWrapper draftOrdersWrapper) {
        List<String> body = new ArrayList<>();

        if (!draftOrdersWrapper.getOrdersApproved().isEmpty()) {
            body.add(String.format(ORDERS_APPROVED_CONFIRMATION_BODY_FORMAT,
                String.join(", ", draftOrdersWrapper.getOrdersApproved())));
        }
        if (!draftOrdersWrapper.getOrdersRepresentativeChanges().isEmpty()) {
            body.add(String.format(ORDERS_REQUIRE_REPRESENTATIVE_CHANGE_CONFIRMATION_BODY_FORMAT,
                String.join(", ", draftOrdersWrapper.getOrdersRepresentativeChanges())));
        }
        if (!draftOrdersWrapper.getOrdersChanged().isEmpty()) {
            body.add(String.format(ORDERS_AMENDED_CHANGE_CONFIRMATION_BODY_FORMAT,
                String.join(", ", draftOrdersWrapper.getOrdersChanged())));
        }
        if (!draftOrdersWrapper.getOrdersReviewLater().isEmpty()) {
            body.add(String.format(ORDERS_REVIEW_LATER_CONFIRMATION_BODY_FORMAT,
                String.join(", ", draftOrdersWrapper.getOrdersReviewLater()), caseDetails.getId()));
        }

        return String.join("\n", body);
    }
}
