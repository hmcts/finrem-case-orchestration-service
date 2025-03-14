package uk.gov.hmcts.reform.finrem.caseorchestration.handler.solicitorcreatecase;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ContactDetailsValidator;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SelectedCourtService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.express.ExpressCaseService;

import java.util.List;
@Slf4j
@Service
public class SolicitorCreateContestedMidHandler extends FinremCallbackHandler {

    private final InternationalPostalService postalService;
    private final SelectedCourtService selectedCourtService;
    private final ExpressCaseService expressCaseService;
    private final FeatureToggleService featureToggleService;

    public SolicitorCreateContestedMidHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                              InternationalPostalService postalService,
                                              SelectedCourtService selectedCourtService,
                                              ExpressCaseService expressCaseService,
                                              FeatureToggleService featureToggleService) {
        super(finremCaseDetailsMapper);
        this.postalService = postalService;
        this.selectedCourtService = selectedCourtService;
        this.expressCaseService = expressCaseService;
        this.featureToggleService = featureToggleService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.MID_EVENT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.SOLICITOR_CREATE.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();
        log.info("Invoking contested event {} mid event callback for Case ID: {}", EventType.SOLICITOR_CREATE, caseDetails.getId());

        List<String> errors = ContactDetailsValidator.validateCaseDataAddresses(caseData);

        selectedCourtService.setSelectedCourtDetailsIfPresent(caseData);

        if (featureToggleService.isExpressPilotEnabled()) {
            expressCaseService.setExpressCaseEnrollmentStatus(caseData);
        }

        if (selectedCourtService.royalCourtOrHighCourtChosen(caseData)) {
            errors.add("You cannot select High Court or Royal Court of Justice. Please select another court.");
        }
        errors.addAll(postalService.validate(caseData));

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData).errors(errors).build();
    }
}
