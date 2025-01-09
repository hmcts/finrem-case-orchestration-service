package uk.gov.hmcts.reform.finrem.caseorchestration.handler.judgeapproval;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UuidCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.RefusedOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.RefusedOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.draftorders.RefusedOrderCorrespondenceRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.draftorders.RefusedOrderCorresponder;

import java.util.List;
import java.util.UUID;

import static java.util.Optional.ofNullable;

@Slf4j
@Service
public class ApproveDraftOrdersSubmittedHandler extends FinremCallbackHandler {
    private final RefusedOrderCorresponder refusedOrderCorresponder;

    private static final String CONFIRMATION_HEADER = "# Draft orders reviewed";

    public ApproveDraftOrdersSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                              RefusedOrderCorresponder refusedOrderCorresponder) {
        super(finremCaseDetailsMapper);
        this.refusedOrderCorresponder = refusedOrderCorresponder;
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

        sendRefusalOrderToParties(caseDetails, userAuthorisation);

        String confirmationBody = caseDetails.getData().getDraftOrdersWrapper().getApproveOrdersConfirmationBody();
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseDetails.getData())
            .confirmationHeader(CONFIRMATION_HEADER)
            .confirmationBody(confirmationBody)
            .build();
    }

    private void sendRefusalOrderToParties(FinremCaseDetails finremCaseDetails, String userAuthorisation) {
        FinremCaseData finremCaseData = finremCaseDetails.getData();
        DraftOrdersWrapper draftOrdersWrapper = finremCaseData.getDraftOrdersWrapper();
        List<UUID> refusalOrderIdsToBeSent = readLatestRefusalOrderIds(draftOrdersWrapper);

        // Process the refused orders collection from the draftOrdersWrapper.
        // Filter the orders whose IDs are present in the refusalOrderIdsToBeSent list.
        List<RefusedOrder> refusedOrders =
            ofNullable(draftOrdersWrapper.getRefusedOrdersCollection()).orElse(List.of()).stream()
            .filter(d -> refusalOrderIdsToBeSent.contains(d.getId())).map(RefusedOrderCollection::getValue)
            .toList();

        if (!refusedOrders.isEmpty()) {
            RefusedOrderCorrespondenceRequest request = new RefusedOrderCorrespondenceRequest(finremCaseDetails,
                userAuthorisation, refusedOrders);
            refusedOrderCorresponder.sendRefusedOrder(request);
        }
    }

    private List<UUID> readLatestRefusalOrderIds(DraftOrdersWrapper draftOrdersWrapper) {
        return ofNullable(draftOrdersWrapper.getRefusalOrderIdsToBeSent()).orElse(List.of()).stream()
            .map(UuidCollection::getValue)
            .toList();
    }
}
