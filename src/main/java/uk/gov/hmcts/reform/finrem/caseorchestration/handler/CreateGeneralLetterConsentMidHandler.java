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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class CreateGeneralLetterConsentMidHandler extends FinremCallbackHandler {

    private final GeneralLetterService generalLetterService;

    @Autowired
    public CreateGeneralLetterConsentMidHandler(FinremCaseDetailsMapper mapper,
                                                GeneralLetterService generalLetterService) {
        super(mapper);
        this.generalLetterService = generalLetterService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.MID_EVENT.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && EventType.CREATE_GENERAL_LETTER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.midEvent(callbackRequest));
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        validateCaseData(callbackRequest);
        FinremCaseData caseData = caseDetails.getData();
        resetInterveners(caseData);
        List<String> errors = new ArrayList<>(generalLetterService.getCaseDataErrorsForCreatingPreviewOrFinalLetter(caseDetails));
        if (errors.isEmpty()) {
            generalLetterService.previewGeneralLetter(userAuthorisation, caseDetails);
            Optional.ofNullable(caseData.getGeneralLetterWrapper().getGeneralLetterUploadedDocuments())
                .filter(list -> !list.isEmpty())
                .ifPresent(list -> generalLetterService.validateEncryptionOnUploadedDocuments(
                    list, userAuthorisation, caseDetails.getCaseIdAsString(), errors));

            return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().errors(errors).data(caseData).build();
        } else {
            log.info("Errors occurred while handling callback request: {} on case id {}", errors, caseDetails.getId());
            return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
                .errors(errors)
                .build();
        }
    }

    private void resetInterveners(FinremCaseData finremCaseData) {
        finremCaseData.setIntervenerOne(null);
        finremCaseData.setIntervenerTwo(null);
        finremCaseData.setIntervenerThree(null);
        finremCaseData.setIntervenerFour(null);
    }
}
