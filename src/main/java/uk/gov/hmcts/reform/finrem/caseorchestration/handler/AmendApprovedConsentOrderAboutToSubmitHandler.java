package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.ApprovedConsentOrderDocumentCategoriser;

@Slf4j
@Service
public class AmendApprovedConsentOrderAboutToSubmitHandler extends FinremCallbackHandler {
    private final ApprovedConsentOrderDocumentCategoriser approvedConsentOrderCategoriser;

    public AmendApprovedConsentOrderAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                         ApprovedConsentOrderDocumentCategoriser approvedConsentOrderCategoriser) {
        super(finremCaseDetailsMapper);
        this.approvedConsentOrderCategoriser = approvedConsentOrderCategoriser;
    }

    @Override
    public boolean canHandle(final CallbackType callbackType, final CaseType caseType,
                             final EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.AMEND_CONTESTED_APPROVED_CONSENT_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        approvedConsentOrderCategoriser.categorise(caseDetails.getData());
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseDetails.getData()).build();
    }
}
