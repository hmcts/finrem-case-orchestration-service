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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralLetterService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class CreateGeneralLetterConsentMidHandler extends FinremCallbackHandler {

    private final GeneralLetterService generalLetterService;
    private final BulkPrintDocumentService service;

    @Autowired
    public CreateGeneralLetterConsentMidHandler(FinremCaseDetailsMapper mapper,
                                                GeneralLetterService generalLetterService,
                                                BulkPrintDocumentService service) {
        super(mapper);
        this.generalLetterService = generalLetterService;
        this.service = service;
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

        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Received request to consent general letter for Case ID: {}", caseDetails.getId());
        validateCaseData(callbackRequest);
        FinremCaseData caseData = caseDetails.getData();
        resetInterveners(caseData);
        List<String> errors = new ArrayList<>(generalLetterService.getCaseDataErrorsForCreatingPreviewOrFinalLetter(caseDetails));
        if (errors.isEmpty()) {
            generalLetterService.previewGeneralLetter(userAuthorisation, caseDetails);
            return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().errors(errors).data(caseData).build();
        } else {
            log.error("Errors occurred while handling callback request: {} on case id {}", errors, caseDetails.getId());
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
