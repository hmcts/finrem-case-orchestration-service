package uk.gov.hmcts.reform.finrem.caseorchestration.handler.uploadpensiondocument.consented;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;

@Slf4j
@Service
public class UploadPensionDocumentMidHandler extends FinremCallbackHandler {

    private final ConsentedApplicationHelper consentedApplicationHelper;

    public UploadPensionDocumentMidHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                           ConsentedApplicationHelper consentedApplicationHelper) {
        super(finremCaseDetailsMapper);
        this.consentedApplicationHelper = consentedApplicationHelper;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.MID_EVENT.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && EventType.UPLOAD_PENSION_DOCUMENTS.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.midEvent(callbackRequest));
        this.validateCaseData(callbackRequest);

        FinremCaseData finremCaseData = callbackRequest.getFinremCaseData();

        consentedApplicationHelper.setConsentVariationOrderLabelField(finremCaseData);

        return response(finremCaseData);
    }
}
