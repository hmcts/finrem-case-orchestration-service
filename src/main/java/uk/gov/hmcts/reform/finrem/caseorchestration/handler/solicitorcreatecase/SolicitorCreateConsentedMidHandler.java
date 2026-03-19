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
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ContactDetailsValidator;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidatePartiesService;

import java.util.List;

@Slf4j
@Service
public class SolicitorCreateConsentedMidHandler extends FinremCallbackHandler {

    private final ConsentOrderService consentOrderService;
    private final InternationalPostalService internationalPostalService;
    private final ObjectMapper objectMapper;
    private final ConsentedApplicationHelper consentedApplicationHelper;
    private final ValidatePartiesService validatePartiesService;

    public SolicitorCreateConsentedMidHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                              ConsentOrderService consentOrderService,
                                              InternationalPostalService internationalPostalService,
                                              ObjectMapper objectMapper,
                                              ConsentedApplicationHelper consentedApplicationHelper,
                                              ValidatePartiesService validatePartiesService) {
        super(finremCaseDetailsMapper);
        this.consentOrderService = consentOrderService;
        this.internationalPostalService = internationalPostalService;
        this.objectMapper = objectMapper;
        this.consentedApplicationHelper = consentedApplicationHelper;
        this.validatePartiesService = validatePartiesService;
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
        // Having same logic with AmendApplicationConsentedMidHandler
        // But we are going to keep it in a separate handler.
        log.info(CallbackHandlerLogger.midEvent(callbackRequest));
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();

        List<String> errors = consentOrderService.performCheck(objectMapper.convertValue(callbackRequest, CallbackRequest.class), userAuthorisation);
        errors.addAll(internationalPostalService.validate(caseData));
        errors.addAll(ContactDetailsValidator.validateCaseDataAddresses(caseData));
        errors.addAll(ContactDetailsValidator.validateCaseDataEmailAddresses(caseData, validatePartiesService));
        errors.addAll(consentedApplicationHelper.validateRegionList(caseData));

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData).errors(errors).build();
    }
}
