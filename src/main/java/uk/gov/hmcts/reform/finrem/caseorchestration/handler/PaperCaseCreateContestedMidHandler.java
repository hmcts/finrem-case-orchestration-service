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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.express.ExpressCaseService;

import java.util.List;

@Slf4j
@Service
public class PaperCaseCreateContestedMidHandler extends FinremCallbackHandler {

    private final ExpressCaseService expressCaseService;
    private final InternationalPostalService internationalPostalService;

    public PaperCaseCreateContestedMidHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                              ExpressCaseService expressCaseService,
                                              InternationalPostalService internationalPostalService) {
        super(finremCaseDetailsMapper);
        this.expressCaseService = expressCaseService;
        this.internationalPostalService = internationalPostalService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.MID_EVENT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.NEW_PAPER_CASE.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.midEvent(callbackRequest));
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();

        List<String> errors = ContactDetailsValidator.validateCaseDataAddresses(caseData);
        errors.addAll(ContactDetailsValidator.validateCaseDataEmailAddresses(caseData));
        errors.addAll(internationalPostalService.validate(caseData));

        expressCaseService.setExpressCaseEnrollmentStatus(caseDetails.getData());

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).errors(errors).build();
    }
}
