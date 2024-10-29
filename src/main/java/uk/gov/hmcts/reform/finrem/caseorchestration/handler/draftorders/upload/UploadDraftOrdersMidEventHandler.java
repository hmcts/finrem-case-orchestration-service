package uk.gov.hmcts.reform.finrem.caseorchestration.handler.draftorders.upload;

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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.UploadAgreedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.UploadSuggestedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.UploadedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.FileUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.DraftOrdersConstants.AGREED_DRAFT_ORDER_OPTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.DraftOrdersConstants.SUGGESTED_DRAFT_ORDER_OPTION;

@Slf4j
@Service
public class UploadDraftOrdersMidEventHandler extends FinremCallbackHandler {

    public UploadDraftOrdersMidEventHandler(FinremCaseDetailsMapper finremCaseDetailsMapper) {
        super(finremCaseDetailsMapper);
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.MID_EVENT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.DRAFT_ORDERS.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        String caseId = String.valueOf(caseDetails.getId());
        log.info("Invoking contested {} mid event callback for Case ID: {}", callbackRequest.getEventType(),
            caseId);

        FinremCaseData finremCaseData = caseDetails.getData();

        List<String> errors = new ArrayList<>();
        DraftOrdersWrapper draftOrdersWrapper = finremCaseData.getDraftOrdersWrapper();
        boolean hasNonWordDocument;
        String error = "You must upload Microsoft Word documents. Document names should clearly reflect the party name, "
            + "the type of hearing and the date of the hearing.";

        String typeOfDraftOrder = draftOrdersWrapper.getTypeOfDraftOrder();
        if (SUGGESTED_DRAFT_ORDER_OPTION.equals(typeOfDraftOrder)) {
            hasNonWordDocument = ofNullable(draftOrdersWrapper.getUploadSuggestedDraftOrder().getUploadSuggestedDraftOrderCollection())
                .orElse(List.of())
                .stream()
                .map(UploadSuggestedDraftOrderCollection::getValue)
                .filter(Objects::nonNull)
                .map(UploadedDraftOrder::getSuggestedDraftOrderDocument)
                .anyMatch(document -> document != null && !FileUtils.isWordDocument(document));
            if (hasNonWordDocument) {
                errors.add(error);
            }
        } else if (AGREED_DRAFT_ORDER_OPTION.equals(typeOfDraftOrder)) {
            hasNonWordDocument = ofNullable(draftOrdersWrapper.getUploadAgreedDraftOrder().getUploadAgreedDraftOrderCollection())
                .orElse(List.of())
                .stream()
                .map(UploadAgreedDraftOrderCollection::getValue)
                .filter(Objects::nonNull)
                .map(uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.UploadedDraftOrder::getAgreedDraftOrderDocument)
                .anyMatch(document -> document != null && !FileUtils.isWordDocument(document));
            if (hasNonWordDocument) {
                errors.add(error);
            }
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(finremCaseData).errors(errors).build();
    }

}
