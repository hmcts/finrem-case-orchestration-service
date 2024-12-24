package uk.gov.hmcts.reform.finrem.caseorchestration.handler.processorders;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.DirectionUploadOrderAboutToSubmitHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AdditionalHearingDocumentService;

import java.util.List;

@Slf4j
@Service
public class ProcessOrdersAboutToSubmitHandler extends DirectionUploadOrderAboutToSubmitHandler {

    public ProcessOrdersAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                             AdditionalHearingDocumentService service) {
        super(finremCaseDetailsMapper, service);
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && (EventType.PROCESS_ORDER.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> resp = super.handle(callbackRequest, userAuthorisation);
        FinremCaseData caseData = resp.getData();

        // to clear the temp fields
        clearUnprocessedApprovedDocuments(caseData.getDraftOrdersWrapper());
        clearMetaDataFields(caseData);

        return resp;
    }

    private void clearUnprocessedApprovedDocuments(DraftOrdersWrapper draftOrdersWrapper) {
        draftOrdersWrapper.setUnprocessedApprovedDocuments(List.of());
    }

    private void clearMetaDataFields(FinremCaseData caseData) {
        caseData.getDraftOrdersWrapper().setIsLegacyApprovedOrderPresent(null);
        caseData.getDraftOrdersWrapper().setIsUnprocessedApprovedDocumentPresent(null);
    }
}
