package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.AttachmentToShare;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.AttachmentToShareCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.OrderToShare;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.OrderToShareCollection;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.MID_EVENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.SEND_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@Slf4j
@Service
public class SendOrderContestedMidHandler extends FinremCallbackHandler {

    public SendOrderContestedMidHandler(FinremCaseDetailsMapper finremCaseDetailsMapper) {
        super(finremCaseDetailsMapper);
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return MID_EVENT.equals(callbackType) && CONTESTED.equals(caseType) && SEND_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest, String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Invoking contested {} mid callback for Case ID: {}", callbackRequest.getEventType(), caseDetails.getId());
        FinremCaseData caseData = caseDetails.getData();

        List<String> errors = new ArrayList<>();

        List<OrderToShareCollection> inputs = emptyIfNull(caseData.getSendOrderWrapper().getOrdersToSend().getValue());
        if (nonSelectedOrder(inputs)) {
            errors.add("You must select at least one order.");
        } else {
            getOrdersMarkedToIncludeSupportingDocuments(inputs).stream()
                .filter(this::containsSupportingDocumentNotSelected)
                .forEach(order -> errors.add("You chose to include a supporting document but none have been selected."));
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().errors(errors).data(caseData).build();
    }

    private boolean containsSupportingDocumentNotSelected(OrderToShare orderToShare) {
        return emptyIfNull(orderToShare.getAttachmentsToShare()).stream().map(AttachmentToShareCollection::getValue)
            .map(AttachmentToShare::getDocumentToShare).noneMatch(attachmentYesOrNo -> YesOrNo.isYes(attachmentYesOrNo));
    }

    private List<OrderToShare> getOrdersMarkedToIncludeSupportingDocuments(List<OrderToShareCollection> inputs) {
        return inputs.stream().map(OrderToShareCollection::getValue)
            .filter(orderToShare -> orderToShare.getDocumentToShare().isYes() && isIncludeSupportingDocumentsChecked(orderToShare))
            .toList();
    }

    private boolean nonSelectedOrder(List<OrderToShareCollection> inputs) {
        return inputs.stream().map(OrderToShareCollection::getValue).map(OrderToShare::getDocumentToShare)
            .noneMatch(sendYesOrNo -> sendYesOrNo.isYes());
    }

    private boolean isIncludeSupportingDocumentsChecked(OrderToShare orderToShare) {
        return !orderToShare.getIncludeSupportingDocument().isEmpty();
    }
}
