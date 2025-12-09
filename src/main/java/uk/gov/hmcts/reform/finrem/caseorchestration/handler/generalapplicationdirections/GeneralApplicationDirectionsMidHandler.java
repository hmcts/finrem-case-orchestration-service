package uk.gov.hmcts.reform.finrem.caseorchestration.handler.generalapplicationdirections;

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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralApplicationDirectionsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class GeneralApplicationDirectionsMidHandler extends FinremCallbackHandler {

    private final ValidateHearingService validateHearingService;
    private final GeneralApplicationDirectionsService generalApplicationDirectionsService;

    public GeneralApplicationDirectionsMidHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                  ValidateHearingService validateHearingService,
                                                  GeneralApplicationDirectionsService generalApplicationDirectionsService) {
        super(finremCaseDetailsMapper);
        this.validateHearingService = validateHearingService;
        this.generalApplicationDirectionsService = generalApplicationDirectionsService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.MID_EVENT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.GENERAL_APPLICATION_DIRECTIONS_MH.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.midEvent(callbackRequest));
        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();

        FinremCaseData finremCaseData = finremCaseDetails.getData();

        List<String> warnings = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        if (generalApplicationDirectionsService.isHearingRequired(finremCaseDetails)) {
            if (validateHearingService.hasInvalidAdditionalHearingDocs(finremCaseData)) {
                errors.add("All additional hearing documents must be Word or PDF files.");
            }

            errors.addAll(validateHearingService.validateGeneralApplicationDirectionsMandatoryParties(finremCaseData));
            errors.addAll(validateHearingService.validateGeneralApplicationDirectionsNoticeSelection(finremCaseData));
            warnings.addAll(validateHearingService.validateGeneralApplicationDirectionsIntervenerParties(finremCaseData));
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(finremCaseData)
            .warnings(warnings)
            .errors(errors)
            .build();
    }
}
