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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.RefusedConsentOrderDocumentCategoriser;

@Slf4j
@Service
public class RejectedConsentOrderInContestedAboutToSubmitHandler extends FinremCallbackHandler {

    private final RefusalOrderDocumentService service;
    private final RefusedConsentOrderDocumentCategoriser categoriser;

    @Autowired
    public RejectedConsentOrderInContestedAboutToSubmitHandler(FinremCaseDetailsMapper mapper,
                                          RefusalOrderDocumentService service,
                                                               RefusedConsentOrderDocumentCategoriser categoriser) {
        super(mapper);
        this.service = service;
        this.categoriser = categoriser;
    }

    @Override
    public boolean canHandle(final CallbackType callbackType, final CaseType caseType,
                             final EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.CONSENT_ORDER_NOT_APPROVED.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Received request for '{}' event '{}' for Case ID: {}",CallbackType.ABOUT_TO_SUBMIT,
            EventType.CONSENT_ORDER_NOT_APPROVED, caseDetails.getId());

        FinremCaseData caseData = service.processConsentOrderNotApproved(caseDetails, userAuthorisation);
        categoriser.categorise(caseData);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData)
            .build();
    }
}
