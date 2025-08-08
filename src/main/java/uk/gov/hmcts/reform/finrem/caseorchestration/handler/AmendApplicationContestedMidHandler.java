package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.solicitorcreatecase.mandatorydatavalidation.RespondentSolicitorDetailsValidator;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.express.ExpressCaseService;

import java.util.List;

@Slf4j
@Service
public class AmendApplicationContestedMidHandler extends FinremCallbackHandler {

    private final InternationalPostalService postalService;
    private final ExpressCaseService expressCaseService;
    private final FeatureToggleService featureToggleService;
    private final RespondentSolicitorDetailsValidator respondentSolicitorDetailsValidator;

    public AmendApplicationContestedMidHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                               InternationalPostalService postalService,
                                               ExpressCaseService expressCaseService,
                                               FeatureToggleService featureToggleService,
                                               RespondentSolicitorDetailsValidator respondentSolicitorDetailsValidator) {
        super(finremCaseDetailsMapper);
        this.postalService = postalService;
        this.expressCaseService = expressCaseService;
        this.featureToggleService = featureToggleService;
        this.respondentSolicitorDetailsValidator = respondentSolicitorDetailsValidator;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.MID_EVENT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.AMEND_CONTESTED_APP_DETAILS.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();
        FinremCaseData caseDataBefore = callbackRequest.getCaseDetailsBefore().getData();

        log.info("Invoking contested event {} mid event callback for Case ID: {}",
            EventType.AMEND_CONTESTED_APP_DETAILS, caseDetails.getId());

        if (featureToggleService.isExpressPilotEnabled()) {
            expressCaseService.setExpressCaseEnrollmentStatus(caseData);
            expressCaseService.setWhichExpressCaseAmendmentLabelToShow(caseData, caseDataBefore);
        }
        List<String> errors = postalService.validate(caseData);
        errors.addAll(respondentSolicitorDetailsValidator.validate(caseData));

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData).errors(errors).build();
    }
}
