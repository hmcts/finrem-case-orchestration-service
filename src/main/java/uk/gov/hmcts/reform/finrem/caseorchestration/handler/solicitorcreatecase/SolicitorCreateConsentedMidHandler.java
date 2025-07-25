package uk.gov.hmcts.reform.finrem.caseorchestration.handler.solicitorcreatecase;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;

import java.util.List;

@Slf4j
@Service
public class SolicitorCreateConsentedMidHandler extends FinremCallbackHandler {
    // Looks like AmendApplicationConsentedMidHandler is the same. Consider merge them later.

    private final ConsentOrderService consentOrderService;
    private final InternationalPostalService internationalPostalService;
    private final ObjectMapper objectMapper;

    public SolicitorCreateConsentedMidHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                              ConsentOrderService consentOrderService,
                                              InternationalPostalService internationalPostalService,
                                              ObjectMapper objectMapper) {
        super(finremCaseDetailsMapper);
        this.consentOrderService = consentOrderService;
        this.internationalPostalService = internationalPostalService;
        this.objectMapper = objectMapper;
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
        log.info(CallbackHandlerLogger.midEvent(callbackRequest));
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();

        List<String> errors = consentOrderService.performCheck(objectMapper.convertValue(callbackRequest, CallbackRequest.class), userAuthorisation);
        errors.addAll(internationalPostalService.validate(caseData));
        errors.addAll(ContactDetailsValidator.validateCaseDataAddresses(caseData));
        errors.addAll(ContactDetailsValidator.validateCaseDataEmailAddresses(caseData));

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData).errors(errors).build();
    }
}
