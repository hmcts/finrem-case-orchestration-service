package uk.gov.hmcts.reform.finrem.caseorchestration.handler.solicitoruploaddocument.consented;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremAboutToSubmitCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionTypeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.SolUploadDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.SolUploadDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.State;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.Bin;

import java.util.List;
import java.util.Objects;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;

@Slf4j
@Service
public class SolicitorUploadDocumentAboutToSubmitHandler extends FinremAboutToSubmitCallbackHandler {

    public SolicitorUploadDocumentAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper) {
        super(finremCaseDetailsMapper);
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && EventType.SOLICITOR_UPLOAD_DOCUMENT.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToSubmit(callbackRequest));

        FinremCaseData finremCaseData = callbackRequest.getFinremCaseData();
        FinremCaseData finremCaseDataBefore = callbackRequest.getFinremCaseDataBefore();

        boolean isReadyToSubmit = finremCaseData.getGenericInputFields().isReadyToSubmit();

        binDeletedCaseDocuments(finremCaseDataBefore, finremCaseData);

        return response(finremCaseData, calculateWarning(isReadyToSubmit), null,
            calculatePostState(isReadyToSubmit));
    }

    private String calculatePostState(boolean isReadyToSubmit) {
        return isReadyToSubmit ? State.INFO_RECEIVED.getStateId() : null;
    }

    private List<String> calculateWarning(boolean isReadyToSubmit) {
        return List.of(
            isReadyToSubmit ? "Please note your documents will be submitted and you won't be able to upload any additional documents."
                : "Please note your documents will not be submitted, to allow you upload additional documents."
        );
    }

    private void binDeletedCaseDocuments(FinremCaseData before, FinremCaseData current) {
        Bin bin = current.getBin();
        bin.binDeletedCaseDocument(
            emptyIfNull(before.getSolUploadDocuments()).stream()
                .map(SolUploadDocumentCollection::getValue)
                .filter(Objects::nonNull)
                .map(SolUploadDocument::getDocumentLink),

            emptyIfNull(current.getSolUploadDocuments()).stream()
                .map(SolUploadDocumentCollection::getValue)
                .filter(Objects::nonNull)
                .map(SolUploadDocument::getDocumentLink)
        );
        bin.binDeletedCaseDocument(
            emptyIfNull(before.getPensionCollection()).stream()
                .map(PensionTypeCollection::getTypedCaseDocument)
                .filter(Objects::nonNull)
                .map(PensionType::getPensionDocument),

            emptyIfNull(current.getPensionCollection()).stream()
                .map(PensionTypeCollection::getTypedCaseDocument)
                .filter(Objects::nonNull)
                .map(PensionType::getPensionDocument)
        );
    }
}
