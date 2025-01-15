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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.AgreedDraftOrderAdditionalDocumentsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.UploadAgreedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.SuggestedDraftOrderAdditionalDocumentsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.UploadSuggestedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.UploadedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.FileUtils;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.DraftOrdersConstants.AGREED_DRAFT_ORDER_OPTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.DraftOrdersConstants.SUGGESTED_DRAFT_ORDER_OPTION;

@Slf4j
@Service
public class UploadDraftOrdersMidEventHandler extends FinremCallbackHandler {

    private static final String HAVING_NON_WORD_DOCUMENT_IN_ORDER_OR_PSA_ERROR_MESSAGE =
        "You must upload Microsoft Word documents. Document names should clearly reflect the party name, "
            + "the type of hearing and the date of the hearing.";

    private static final String HAVING_WORD_DOCUMENT_IN_ADDITIONAL_ATTACHMENTS_ERROR_MESSAGE
        = "You must upload a PDF file in the additional attachments.";

    public UploadDraftOrdersMidEventHandler(FinremCaseDetailsMapper finremCaseDetailsMapper) {
        super(finremCaseDetailsMapper);
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.MID_EVENT.equals(callbackType) && CaseType.CONTESTED.equals(caseType) && EventType.DRAFT_ORDERS.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Invoking contested {} mid event callback for Case ID: {}", callbackRequest.getEventType(), caseDetails.getId());

        FinremCaseData finremCaseData = caseDetails.getData();

        List<String> errors = new ArrayList<>();
        DraftOrdersWrapper draftOrdersWrapper = finremCaseData.getDraftOrdersWrapper();

        String typeOfDraftOrder = draftOrdersWrapper.getTypeOfDraftOrder();
        if (SUGGESTED_DRAFT_ORDER_OPTION.equals(typeOfDraftOrder)) {
            if (isSuggestedDraftOrderHavingNonWordDocument(draftOrdersWrapper)) {
                errors.add(HAVING_NON_WORD_DOCUMENT_IN_ORDER_OR_PSA_ERROR_MESSAGE);
            }
            if (isSuggestedDraftOrderAdditionalAttachingHavingNonPdfDocument(draftOrdersWrapper)) {
                errors.add(HAVING_WORD_DOCUMENT_IN_ADDITIONAL_ATTACHMENTS_ERROR_MESSAGE);
            }
        } else if (AGREED_DRAFT_ORDER_OPTION.equals(typeOfDraftOrder)) {
            if (isAgreedDraftOrderHavingNonWordDocument(draftOrdersWrapper)) {
                errors.add(HAVING_NON_WORD_DOCUMENT_IN_ORDER_OR_PSA_ERROR_MESSAGE);
            }
            if (isAgreedDraftOrderAdditionalAttachingHavingNonPdfDocument(draftOrdersWrapper)) {
                errors.add(HAVING_WORD_DOCUMENT_IN_ADDITIONAL_ATTACHMENTS_ERROR_MESSAGE);
            }
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(finremCaseData).errors(errors).build();
    }

    private boolean isSuggestedDraftOrderHavingNonWordDocument(DraftOrdersWrapper draftOrdersWrapper) {
        return emptyIfNull(draftOrdersWrapper.getUploadSuggestedDraftOrder().getUploadSuggestedDraftOrderCollection())
            .stream()
            .map(UploadSuggestedDraftOrderCollection::getValue)
            .map(UploadedDraftOrder::getSuggestedDraftOrderDocument)
            .anyMatch(document -> document != null && !FileUtils.isWordDocument(document));
    }

    private boolean isAgreedDraftOrderHavingNonWordDocument(DraftOrdersWrapper draftOrdersWrapper) {
        return emptyIfNull(draftOrdersWrapper.getUploadAgreedDraftOrder().getUploadAgreedDraftOrderCollection())
            .stream()
            .map(UploadAgreedDraftOrderCollection::getValue)
            .map(uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.UploadedDraftOrder::getAgreedDraftOrderDocument)
            .anyMatch(document -> document != null && !FileUtils.isWordDocument(document));
    }

    private boolean isSuggestedDraftOrderAdditionalAttachingHavingNonPdfDocument(DraftOrdersWrapper draftOrdersWrapper) {
        return emptyIfNull(draftOrdersWrapper.getUploadSuggestedDraftOrder().getUploadSuggestedDraftOrderCollection())
            .stream()
            .map(UploadSuggestedDraftOrderCollection::getValue)
            .flatMap(order -> emptyIfNull(order.getSuggestedDraftOrderAdditionalDocumentsCollection()).stream())
            .map(SuggestedDraftOrderAdditionalDocumentsCollection::getValue)
            .anyMatch(document -> document != null && !FileUtils.isPdf(document));
    }

    private boolean isAgreedDraftOrderAdditionalAttachingHavingNonPdfDocument(DraftOrdersWrapper draftOrdersWrapper) {
        return emptyIfNull(draftOrdersWrapper.getUploadAgreedDraftOrder().getUploadAgreedDraftOrderCollection())
            .stream()
            .map(UploadAgreedDraftOrderCollection::getValue)
            .flatMap(order -> emptyIfNull(order.getAgreedDraftOrderAdditionalDocumentsCollection()).stream())
            .map(AgreedDraftOrderAdditionalDocumentsCollection::getValue)
            .anyMatch(document -> document != null && !FileUtils.isPdf(document));
    }
}
