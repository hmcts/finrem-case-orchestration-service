package uk.gov.hmcts.reform.finrem.caseorchestration.handler.creategeneralemail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralEmailService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class GeneralEmailMidHandler extends FinremCallbackHandler {

    private final GeneralEmailService generalEmailService;

    public GeneralEmailMidHandler(FinremCaseDetailsMapper mapper,
                                  GeneralEmailService generalEmailService) {
        super(mapper);
        this.generalEmailService = generalEmailService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.MID_EVENT.equals(callbackType) && EventType.CREATE_GENERAL_EMAIL.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.midEvent(callbackRequest));

        FinremCaseData finremCaseData = callbackRequest.getFinremCaseData();

        List<String> errors = new ArrayList<>();
        generalEmailService.validateUploadedDocuments(finremCaseData, userAuthorisation, errors);

        return response(finremCaseData, null, errors);
    }
}
