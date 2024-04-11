package uk.gov.hmcts.reform.finrem.caseorchestration.handler.intervener;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IntervenerService;

import java.util.ArrayList;
import java.util.Arrays;
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
    private final IntervenerService service;
    private static final List<String> ADD_OPERATION_CODES = Arrays.asList(ADD_INTERVENER_ONE_CODE, ADD_INTERVENER_TWO_CODE, ADD_INTERVENER_THREE_CODE, ADD_INTERVENER_FOUR_CODE);
    private static final List<String> DELETE_OPERATION_CODES = Arrays.asList(DEL_INTERVENER_ONE_CODE, DEL_INTERVENER_TWO_CODE, DEL_INTERVENER_THREE_CODE, DEL_INTERVENER_FOUR_CODE);

    public IntervenersAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                           IntervenerService service) {
        super(finremCaseDetailsMapper);
        this.service = service;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && (EventType.MANAGE_INTERVENERS.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        Long caseId = callbackRequest.getCaseDetails().getId();
        log.info("Invoking contested event {}, callback {} callback for Case ID: {}",
            callbackRequest.getEventType(), CallbackType.ABOUT_TO_SUBMIT, caseId);
        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();

        String selectedOperationCode = caseData.getIntervenerOptionList().getValueCode();
        log.info("selected operation choice {} for intervener {} for Case ID: {}",
            selectedOperationCode, caseData.getIntervenersList().getValueCode(), caseId);
        List<String> errors = new ArrayList<>();

        if (ADD_OPERATION_CODES.contains(selectedOperationCode)) {
            IntervenerWrapper intervener = getIntervenerWrapper(caseData, selectedOperationCode);
            if (isIntervenerPostCodeMissing(intervener)) {
                errors.add("Postcode field is required for the intervener.");
            } else {
                service.updateIntervenerDetails(intervener, errors, callbackRequest);
            }
        } else if (DELETE_OPERATION_CODES.contains(selectedOperationCode)) {
            IntervenerWrapper intervener = getIntervenerWrapper(caseData, selectedOperationCode);
            service.removeIntervenerDetails(intervener, errors, caseData, caseId);
        } else {
            throw new IllegalArgumentException("Invalid operation code: " + selectedOperationCode);
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData).errors(errors).build();
    }

    private boolean isIntervenerPostCodeMissing(IntervenerWrapper intervener) {
        String postCode = intervener.getIntervenerAddress().getPostCode();
        return ObjectUtils.isEmpty(postCode);
    }

    private IntervenerWrapper getIntervenerWrapper(FinremCaseData caseData, String selectedOperationCode) {
        return switch (selectedOperationCode) {
            case ADD_INTERVENER_ONE_CODE -> caseData.getIntervenerOne();
            case ADD_INTERVENER_TWO_CODE -> caseData.getIntervenerTwo();
            case ADD_INTERVENER_THREE_CODE -> caseData.getIntervenerThree();
            case ADD_INTERVENER_FOUR_CODE -> caseData.getIntervenerFour();
            case DEL_INTERVENER_ONE_CODE -> caseData.getIntervenerOne();
            case DEL_INTERVENER_TWO_CODE -> caseData.getIntervenerTwo();
            case DEL_INTERVENER_THREE_CODE -> caseData.getIntervenerThree();
            case DEL_INTERVENER_FOUR_CODE -> caseData.getIntervenerFour();
            default -> throw new IllegalArgumentException("Invalid operation code: " + selectedOperationCode);
        };
    }


}
