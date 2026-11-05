package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderApprovedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.CONSENT_SEND_ORDER_FOR_APPROVED_ORDER;

@Slf4j
@Service
public class SendOrderConsentForApprovedOrderAboutToSubmitHandler extends FinremCallbackHandler {

    private final ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;
    private final NotificationService notificationService;

    public SendOrderConsentForApprovedOrderAboutToSubmitHandler(FinremCaseDetailsMapper mapper,
                                                                ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService,
                                                                NotificationService notificationService) {
        super(mapper);
        this.consentOrderApprovedDocumentService = consentOrderApprovedDocumentService;
        this.notificationService = notificationService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && CONSENT_SEND_ORDER_FOR_APPROVED_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToSubmit(callbackRequest));
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();

        if (!notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)) {
            caseData.getConsentSendOrderWrapper().setConsentOrderApprovedCoverLetterApp(consentOrderApprovedDocumentService.getApprovedConsentOrderCoverLetter(
                caseDetails, userAuthorisation, DocumentHelper.PaperNotificationRecipient.APPLICANT));
        } else {
            caseData.getConsentSendOrderWrapper().setConsentOrderApprovedCoverLetterApp(null);
        }

        if (!notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)) {
            caseData.getConsentSendOrderWrapper().setConsentOrderApprovedCoverLetterResp(consentOrderApprovedDocumentService.getApprovedConsentOrderCoverLetter(
                caseDetails, userAuthorisation, DocumentHelper.PaperNotificationRecipient.RESPONDENT));
        } else {
            caseData.getConsentSendOrderWrapper().setConsentOrderApprovedCoverLetterResp(null);
        }
        return response(caseData);
    }
}
