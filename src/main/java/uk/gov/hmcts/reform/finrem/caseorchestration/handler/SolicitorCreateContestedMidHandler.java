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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;

import java.util.List;

@Slf4j
@Service
public class SolicitorCreateContestedMidHandler extends FinremCallbackHandler {

    private final InternationalPostalService postalService;

    public SolicitorCreateContestedMidHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                              InternationalPostalService postalService) {
        super(finremCaseDetailsMapper);
        this.postalService = postalService;
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
        log.info("Invoking contested event {} mid event callback", EventType.SOLICITOR_CREATE);

        List<String> validate = postalService.validate(callbackRequest.getCaseDetails().getData());
        List<String> errors = ContactDetailsValidator.validateCaseDataAddresses(caseData);
        errors.addAll(validate);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData).errors(errors).build();
    }
}
