package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralLetterService;

@Slf4j
@Service
public class CreateGeneralLetterConsentedAboutToSubmitHandler extends FinremCallbackHandler {
    private final GeneralLetterService generalLetterService;

    @Autowired
    public CreateGeneralLetterConsentedAboutToSubmitHandler(FinremCaseDetailsMapper mapper,
                                                            GeneralLetterService generalLetterService) {
        super(mapper);
        this.generalLetterService = generalLetterService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && (EventType.CREATE_GENERAL_LETTER.equals(eventType)
            || EventType.CREATE_GENERAL_LETTER_JUDGE.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Received request to generate general letter with Case ID: {}", caseDetails.getId());
        validateCaseData(callbackRequest);

        if (generalLetterService.getCaseDataErrorsForCreatingPreviewOrFinalLetter(caseDetails).isEmpty()) {
            generalLetterService.createGeneralLetter(userAuthorisation, caseDetails);
            return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseDetails.getData()).build();
        } else {
            return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
                .errors(generalLetterService.getCaseDataErrorsForCreatingPreviewOrFinalLetter(caseDetails))
                .build();
        }
    }
}
