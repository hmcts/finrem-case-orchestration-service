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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderNotApprovedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;


import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.SEND_ORDER;

@Slf4j
@Service
public class SendOrderConsentForNotApprovedOrderAboutToSubmitHandler extends FinremCallbackHandler {

    private final NotificationService notificationService;
    private final ConsentOrderNotApprovedDocumentService consentOrderNotApprovedDocumentService;

    public SendOrderConsentForNotApprovedOrderAboutToSubmitHandler(FinremCaseDetailsMapper mapper,
                                                                   NotificationService notificationService,
                                                                   ConsentOrderNotApprovedDocumentService consentOrderNotApprovedDocumentService) {
        super(mapper);
        this.notificationService = notificationService;
        this.consentOrderNotApprovedDocumentService = consentOrderNotApprovedDocumentService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && SEND_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToSubmit(callbackRequest));

        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();

        if (!notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)) {
            log.info("Generating not approved cover letter for Case ID: {}", caseDetails.getId());
            caseData.getConsentSendOrderWrapper().setConsentOrderNotApprovedCoverLetterApp(consentOrderNotApprovedDocumentService.getNotApprovedCoverLetter(
                caseDetails, userAuthorisation, DocumentHelper.PaperNotificationRecipient.APPLICANT));
        }else {
            caseData.getConsentSendOrderWrapper().setConsentOrderNotApprovedCoverLetterApp(null);
        }
        if (!notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)) {
            log.info("Generating not approved cover letter for Case ID: {}", caseDetails.getId());
            caseData.getConsentSendOrderWrapper().setConsentOrderNotApprovedCoverLetterResp(consentOrderNotApprovedDocumentService.getNotApprovedCoverLetter(
                caseDetails, userAuthorisation, DocumentHelper.PaperNotificationRecipient.RESPONDENT));
        }else {
            caseData.getConsentSendOrderWrapper().setConsentOrderNotApprovedCoverLetterResp(null);
        }
        return response(caseData);
    }
}
