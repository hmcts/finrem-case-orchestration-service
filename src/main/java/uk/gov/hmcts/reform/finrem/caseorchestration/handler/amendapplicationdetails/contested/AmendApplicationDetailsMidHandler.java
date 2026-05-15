package uk.gov.hmcts.reform.finrem.caseorchestration.handler.amendapplicationdetails.contested;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ContactDetailsValidator;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidatePartiesService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.express.ExpressCaseService;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.MID_EVENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.AMEND_CONTESTED_APP_DETAILS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.AMEND_CONTESTED_PAPER_APP_DETAILS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@Slf4j
@Service
public class AmendApplicationDetailsMidHandler extends FinremCallbackHandler {

    private final InternationalPostalService internationalPostalService;
    private final ExpressCaseService expressCaseService;
    private final FeatureToggleService featureToggleService;
    private final ValidatePartiesService validatePartiesService;

    public AmendApplicationDetailsMidHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                             InternationalPostalService internationalPostalService,
                                             ExpressCaseService expressCaseService,
                                             FeatureToggleService featureToggleService,
                                             ValidatePartiesService validatePartiesService) {
        super(finremCaseDetailsMapper);
        this.internationalPostalService = internationalPostalService;
        this.expressCaseService = expressCaseService;
        this.featureToggleService = featureToggleService;
        this.validatePartiesService = validatePartiesService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return MID_EVENT.equals(callbackType) && CONTESTED.equals(caseType)
            && List.of(AMEND_CONTESTED_PAPER_APP_DETAILS, AMEND_CONTESTED_APP_DETAILS).contains(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.midEvent(callbackRequest));

        FinremCaseData caseData = callbackRequest.getFinremCaseData();
        FinremCaseData caseDataBefore = callbackRequest.getFinremCaseDataBefore();

        if (featureToggleService.isExpressPilotEnabled()) {
            expressCaseService.setExpressCaseEnrollmentStatus(caseData);
            expressCaseService.setWhichExpressCaseAmendmentLabelToShow(caseData, caseDataBefore);
        }

        List<String> errors = new ArrayList<>();
        errors.addAll(ContactDetailsValidator.validateCaseDataAddresses(caseData));
        errors.addAll(ContactDetailsValidator.validateCaseDataEmailAddresses(caseData, validatePartiesService));
        errors.addAll(internationalPostalService.validate(caseData));

        return response(caseData, null, errors);
    }
}
