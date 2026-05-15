package uk.gov.hmcts.reform.finrem.caseorchestration.handler.creategeneralemail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

@Slf4j
@Service
public class GeneralEmailAboutToStartHandler extends FinremCallbackHandler {

    private final IdamService idamService;

    @Autowired
    public GeneralEmailAboutToStartHandler(FinremCaseDetailsMapper mapper, IdamService idamService) {
        super(mapper);
        this.idamService = idamService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && EventType.CREATE_GENERAL_EMAIL.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToStart(callbackRequest));

        FinremCaseData caseData = callbackRequest.getFinremCaseData();
        validateCaseData(callbackRequest);
        caseData.getGeneralEmailWrapper().setGeneralEmailRecipient(null);
        caseData.getGeneralEmailWrapper().setGeneralEmailCreatedBy(idamService.getIdamFullName(userAuthorisation));
        caseData.getGeneralEmailWrapper().setGeneralEmailBody(null);

        return response(caseData);
    }
}
