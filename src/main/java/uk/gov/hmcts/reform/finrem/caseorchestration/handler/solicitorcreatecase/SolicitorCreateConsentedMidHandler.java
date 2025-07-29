package uk.gov.hmcts.reform.finrem.caseorchestration.handler.solicitorcreatecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.solicitorcreatecase.mandatorydatavalidation.RespondentSolicitorDetailsValidator;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ContactDetailsValidator;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SolicitorCreateConsentedMidHandler implements CallbackHandler<Map<String, Object>> {

    private final FinremCaseDetailsMapper finremCaseDetailsMapper;
    private final ConsentOrderService consentOrderService;
    private final InternationalPostalService postalService;
    private final RespondentSolicitorDetailsValidator respondentSolicitorDetailsValidator;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.MID_EVENT.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && EventType.SOLICITOR_CREATE.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> handle(CallbackRequest callbackRequest,
                                                                                   String userAuthorisation) {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Invoking consented event {} mid event callback for Case ID: {}", EventType.SOLICITOR_CREATE, caseDetails.getId());
        List<String> errors = consentOrderService.performCheck(callbackRequest, userAuthorisation);
        errors.addAll(postalService.validate(caseDetails.getData()));

        FinremCaseDetails finremCaseDetails = finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails);
        errors.addAll(ContactDetailsValidator.validateCaseDataAddresses(finremCaseDetails.getData()));
        errors.addAll(respondentSolicitorDetailsValidator.validate(finremCaseDetails.getData()));

        return GenericAboutToStartOrSubmitCallbackResponse.<Map<String, Object>>builder()
            .data(callbackRequest.getCaseDetails().getData()).errors(errors).build();
    }
}
