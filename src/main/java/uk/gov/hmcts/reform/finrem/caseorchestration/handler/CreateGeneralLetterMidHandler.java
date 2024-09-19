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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralLetterService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class CreateGeneralLetterMidHandler extends FinremCallbackHandler {

    private final GeneralLetterService generalLetterService;

    public CreateGeneralLetterMidHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                         GeneralLetterService generalLetterService) {
        super(finremCaseDetailsMapper);
        this.generalLetterService = generalLetterService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return isMidEvent(callbackType) && isContestedCase(caseType) && isCreateGeneralLetterEvent(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {

        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Received request to preview general letter for Case ID: {}", caseDetails.getId());
        validateCaseData(callbackRequest);
        List<String> errors = getErrorsForCreatingPreviewOrFinalLetter(caseDetails);

        if (errors.isEmpty()) {
            previewGeneralLetter(userAuthorisation, caseDetails);
        }

        return buildCallbackResponse(caseDetails, errors);
    }

    private boolean isMidEvent(CallbackType callbackType) {
        return CallbackType.MID_EVENT.equals(callbackType);
    }

    private boolean isContestedCase(CaseType caseType) {
        return CaseType.CONTESTED.equals(caseType);
    }

    private boolean isCreateGeneralLetterEvent(EventType eventType) {
        return EventType.CREATE_GENERAL_LETTER.equals(eventType)
            || EventType.CREATE_GENERAL_LETTER_JUDGE.equals(eventType);
    }

    private List<String> getErrorsForCreatingPreviewOrFinalLetter(FinremCaseDetails caseDetails) {
        return new ArrayList<>(generalLetterService.getCaseDataErrorsForCreatingPreviewOrFinalLetter(caseDetails));
    }

    private void previewGeneralLetter(String userAuthorisation,
                                      FinremCaseDetails caseDetails) {
        generalLetterService.previewGeneralLetter(userAuthorisation, caseDetails);
    }

    private GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> buildCallbackResponse(FinremCaseDetails caseDetails,
                                                                                              List<String> errors) {
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .errors(errors)
            .data(caseDetails.getData())
            .build();
    }
}