package uk.gov.hmcts.reform.finrem.caseorchestration.handler.uploadapprovedorder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDataConsented;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderApprovedDocumentService;

@Slf4j
@Service
public class UploadApprovedOrderConsentedAboutToSubmitHandler extends FinremCallbackHandler<FinremCaseDataConsented> {

    private final ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;


    public UploadApprovedOrderConsentedAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                            ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService) {
        super(finremCaseDetailsMapper);
        this.consentOrderApprovedDocumentService = consentOrderApprovedDocumentService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && EventType.UPLOAD_APPROVED_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseDataConsented> handle(FinremCallbackRequest<FinremCaseDataConsented> callbackRequest,
                                                                                       String userAuthorisation) {
        log.info("Handling Upload Approved Order Consented application about to submit callback for case id: {}",
            callbackRequest.getCaseDetails().getId());
        FinremCaseDetails<FinremCaseDataConsented> finremCaseDetails = callbackRequest.getCaseDetails();
        log.info("Received request to set nature of application for consented case with Case ID: {}",
            finremCaseDetails.getId());
        FinremCaseDataConsented caseData = finremCaseDetails.getData();

        caseData.setLatestConsentOrder(caseData.getUploadApprovedConsentOrder());
        consentOrderApprovedDocumentService
            .addGeneratedApprovedConsentOrderDocumentsToCase(userAuthorisation, finremCaseDetails);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseDataConsented>builder().data(caseData).build();
    }
}
