package uk.gov.hmcts.reform.finrem.caseorchestration.handler.judgeapproval;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.DraftOrdersNotificationRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UuidCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PaperNotificationService;

import java.util.List;
import java.util.UUID;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Slf4j
@Service
public class ApproveDraftOrdersSubmittedHandler extends FinremCallbackHandler {

    private final NotificationService notificationService;

    private final DraftOrdersNotificationRequestMapper notificationRequestMapper;

    private final BulkPrintService bulkPrintService;

    private final PaperNotificationService paperNotificationService;

    private final DocumentHelper documentHelper;

    public ApproveDraftOrdersSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper, NotificationService notificationService,
                                              DraftOrdersNotificationRequestMapper notificationRequestMapper,
                                              BulkPrintService bulkPrintService,
                                              PaperNotificationService paperNotificationService,
                                              DocumentHelper documentHelper) {
        super(finremCaseDetailsMapper);
        this.notificationService = notificationService;
        this.notificationRequestMapper = notificationRequestMapper;
        this.bulkPrintService = bulkPrintService;
        this.paperNotificationService = paperNotificationService;
        this.documentHelper = documentHelper;
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

        draftOrdersWrapper.getRefusedOrdersCollection().stream()
            .filter(d -> refusalOrderIdsToBeSent.contains(d.getId()))
            .forEach(a -> {
                if (!isEmpty(a.getValue().getSubmittedByEmail())) {
                    notificationService.sendDraftOrderOrPsaRefused(notificationRequestMapper
                        .buildRefusedDraftOrderOrPsaNotificationRequest(caseDetails, a.getValue()));
                } else {
                    // TODO send refusal order by post.
                    // cloned the logic from ContestedDraftOrderNotApprovedController.sendRefusalReason
                }
            });

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(finremCaseData).build();
    }

}
