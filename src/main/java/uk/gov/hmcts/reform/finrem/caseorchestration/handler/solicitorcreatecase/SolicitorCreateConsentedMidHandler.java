package uk.gov.hmcts.reform.finrem.caseorchestration.handler.solicitorcreatecase;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.solicitorcreatecase.mandatorydatavalidation.RespondentSolicitorDetailsValidator;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ContactDetailsValidator;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;

import java.util.List;

@Slf4j
@Service
public class SolicitorCreateConsentedMidHandler extends FinremCallbackHandler {

    private final ConsentOrderService consentOrderService;
    private final InternationalPostalService postalService;
    private final RespondentSolicitorDetailsValidator respondentSolicitorDetailsValidator;

    public SolicitorCreateConsentedMidHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                              InternationalPostalService postalService,
                                              ConsentOrderService consentOrderService,
                                              RespondentSolicitorDetailsValidator respondentSolicitorDetailsValidator) {
        super(finremCaseDetailsMapper);
        this.postalService = postalService;
        this.consentOrderService = consentOrderService;
        this.respondentSolicitorDetailsValidator = respondentSolicitorDetailsValidator;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.MID_EVENT.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && EventType.SOLICITOR_CREATE.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {

        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = finremCaseDetails.getData();
        log.info(CallbackHandlerLogger.midEvent(callbackRequest));
        List<String> errors = consentOrderService.performCheck(callbackRequest, userAuthorisation);
        errors.addAll(postalService.validate(caseData));
        errors.addAll(ContactDetailsValidator.validateCaseDataAddresses(caseData));
        errors.addAll(respondentSolicitorDetailsValidator.validate(caseData));

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData).errors(errors).build();
    }
}
