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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.State;

import java.util.List;

import static java.util.Optional.ofNullable;

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

        boolean isReadyToSubmit = finremCaseData.getGenericInputFields().isReadyToSubmit();
        int numberOfDocuments =
            ofNullable(finremCaseData.getSolUploadDocuments()).orElse(List.of()).size()
            + ofNullable(finremCaseData.getPensionCollection()).orElse(List.of()).size();
        return response(finremCaseData, calculateWarning(isReadyToSubmit, numberOfDocuments), null,
            calculatePostState(isReadyToSubmit));
    }

    private String calculatePostState(boolean isReadyToSubmit) {
        return isReadyToSubmit ? State.INFO_RECEIVED.getStateId() : null;
    }

    private List<String> calculateWarning(boolean isReadyToSubmit, int numberOfDocuments) {
       return List.of(
           isReadyToSubmit ? readyToSubmitWarning(numberOfDocuments) : notReadyToSubmitWarning(numberOfDocuments)
       );
    }

    private String readyToSubmitWarning(int numberOfDocuments) {
        return "The document%s being submitted to the court".formatted(numberOfDocuments > 1 ? "s are" : " is");
    }

    private String notReadyToSubmitWarning(int numberOfDocuments) {
        return "The document%s not been submitted to the court.".formatted(numberOfDocuments > 1 ? "s have" : " has");
    }
}
