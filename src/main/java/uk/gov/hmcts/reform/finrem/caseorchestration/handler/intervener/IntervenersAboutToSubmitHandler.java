package uk.gov.hmcts.reform.finrem.caseorchestration.handler.intervener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerChangeDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.intervener.IntervenerCoversheetService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.intervener.IntervenerService;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.ADD_INTERVENER_FOUR_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.ADD_INTERVENER_ONE_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.ADD_INTERVENER_THREE_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.ADD_INTERVENER_TWO_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.DEL_INTERVENER_FOUR_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.DEL_INTERVENER_ONE_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.DEL_INTERVENER_THREE_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.DEL_INTERVENER_TWO_CODE;

@Slf4j
@Service
public class IntervenersAboutToSubmitHandler extends FinremCallbackHandler {

    private final IntervenerService intervenerService;
    private final IntervenerCoversheetService intervenerCoversheetService;

    private static final List<String> ADD_OPERATION_CODES = List.of(ADD_INTERVENER_ONE_CODE, ADD_INTERVENER_TWO_CODE,
        ADD_INTERVENER_THREE_CODE, ADD_INTERVENER_FOUR_CODE);
    private static final List<String> DELETE_OPERATION_CODES = List.of(DEL_INTERVENER_ONE_CODE, DEL_INTERVENER_TWO_CODE,
        DEL_INTERVENER_THREE_CODE, DEL_INTERVENER_FOUR_CODE);

    public IntervenersAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                           IntervenerService intervenerService, IntervenerCoversheetService intervenerCoversheetService) {
        super(finremCaseDetailsMapper);
        this.intervenerService = intervenerService;
        this.intervenerCoversheetService = intervenerCoversheetService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.MANAGE_INTERVENERS.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToSubmit(callbackRequest));
        Long caseId = callbackRequest.getCaseDetails().getId();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();

        String selectedOperationCode = caseData.getIntervenerOptionList().getValueCode();

        List<String> errors = new ArrayList<>();
        IntervenerWrapper intervener = getIntervenerWrapper(caseData, selectedOperationCode);

        IntervenerChangeDetails intervenerChangeDetails;

        if (!ADD_OPERATION_CODES.contains(selectedOperationCode) && !DELETE_OPERATION_CODES.contains(selectedOperationCode)) {
            throw new IllegalArgumentException("Invalid operation code: " + selectedOperationCode);
        }

        if (ADD_OPERATION_CODES.contains(selectedOperationCode)) {
            intervenerService.validateIntervenerInformation(intervener, errors);

            if (errors.isEmpty()) {
                intervenerChangeDetails = intervenerService.updateIntervenerDetails(intervener, errors, callbackRequest);
                intervenerCoversheetService.updateIntervenerCoversheet(caseDetails, intervenerChangeDetails, userAuthorisation);
            }
        } else {
            intervenerChangeDetails = intervenerService.removeIntervenerDetails(intervener, errors, caseData, caseId);
            intervenerCoversheetService.updateIntervenerCoversheet(caseDetails, intervenerChangeDetails, userAuthorisation);
        }

        return response(caseData, null, errors);
    }

    private IntervenerWrapper getIntervenerWrapper(FinremCaseData caseData, String selectedOperationCode) {
        return switch (selectedOperationCode) {
            case ADD_INTERVENER_ONE_CODE, DEL_INTERVENER_ONE_CODE -> caseData.getIntervenerOne();
            case ADD_INTERVENER_TWO_CODE, DEL_INTERVENER_TWO_CODE -> caseData.getIntervenerTwo();
            case ADD_INTERVENER_THREE_CODE, DEL_INTERVENER_THREE_CODE -> caseData.getIntervenerThree();
            case ADD_INTERVENER_FOUR_CODE, DEL_INTERVENER_FOUR_CODE -> caseData.getIntervenerFour();
            default -> throw new IllegalArgumentException("Invalid operation code: " + selectedOperationCode);
        };
    }
}
