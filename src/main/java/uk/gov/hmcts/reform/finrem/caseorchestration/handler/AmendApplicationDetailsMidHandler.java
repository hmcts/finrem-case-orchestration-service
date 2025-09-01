package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ContactDetailsValidator;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.express.ExpressCaseService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class AmendApplicationDetailsMidHandler extends FinremCallbackHandler {

    private final InternationalPostalService internationalPostalService;
    private final ExpressCaseService expressCaseService;
    private final FeatureToggleService featureToggleService;

    public AmendApplicationDetailsMidHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                             InternationalPostalService internationalPostalService,
                                             ExpressCaseService expressCaseService,
                                             FeatureToggleService featureToggleService) {
        super(finremCaseDetailsMapper);
        this.internationalPostalService = internationalPostalService;
        this.expressCaseService = expressCaseService;
        this.featureToggleService = featureToggleService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.MID_EVENT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && List.of(EventType.AMEND_CONTESTED_APP_DETAILS, EventType.AMEND_CONTESTED_PAPER_APP_DETAILS)
            .contains(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.midEvent(callbackRequest));

        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();
        FinremCaseData caseDataBefore = callbackRequest.getCaseDetailsBefore().getData();

        if (featureToggleService.isExpressPilotEnabled()) {
            expressCaseService.setExpressCaseEnrollmentStatus(caseData);
            expressCaseService.setWhichExpressCaseAmendmentLabelToShow(caseData, caseDataBefore);
        }

        List<String> errors = new ArrayList<>();
        errors.addAll(ContactDetailsValidator.validateCaseDataAddresses(caseDetails.getData()));
        errors.addAll(ContactDetailsValidator.validateCaseDataEmailAddresses(caseDetails.getData()));
        errors.addAll(internationalPostalService.validate(caseData));

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).errors(errors).build();
    }
}
