package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ContactDetailsHelper;
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
public class AmendApplicationConsentedMidHandler extends FinremCallbackHandler {
    private final ConsentOrderService consentOrderService;
    private final InternationalPostalService postalService;
    private final ObjectMapper objectMapper;

    public AmendApplicationConsentedMidHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                               ConsentOrderService consentOrderService,
                                               InternationalPostalService postalService,
                                               ObjectMapper objectMapper) {
        super(finremCaseDetailsMapper);
        this.consentOrderService = consentOrderService;
        this.postalService = postalService;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.MID_EVENT.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && EventType.AMEND_APP_DETAILS.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                                   String userAuthorisation) {
        log.info("Invoking amend application mid event for caseId {}", callbackRequest.getCaseDetails().getId());
        List<String> errors = consentOrderService.performCheck(objectMapper.convertValue(callbackRequest, CallbackRequest.class), userAuthorisation);
        List<String> validate = postalService.validate(callbackRequest.getCaseDetails().getData());
        errors.addAll(validate);

        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = finremCaseDetails.getData();
        errors.addAll(ContactDetailsHelper.validateCaseData(caseData));

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData).errors(errors).build();
    }


}

