package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.RefusalOrderDocumentService;

@Slf4j
@Service
public class RejectedConsentOrderInContestedAboutToStartHandler extends FinremCallbackHandler {

    private final RefusalOrderDocumentService service;

    @Autowired
    public RejectedConsentOrderInContestedAboutToStartHandler(FinremCaseDetailsMapper mapper,
                                                              RefusalOrderDocumentService service) {
        super(mapper);
        this.service = service;
    }


    @Override
    public boolean canHandle(final CallbackType callbackType, final CaseType caseType,
                             final EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.CONSENT_ORDER_NOT_APPROVED.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Received request for '{}' event '{}' for Case ID: {}",CallbackType.ABOUT_TO_START,
            EventType.CONSENT_ORDER_NOT_APPROVED, caseDetails.getId());

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(service.setDefaults(caseDetails.getData(), userAuthorisation))
            .build();
    }
}
