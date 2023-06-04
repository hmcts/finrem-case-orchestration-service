package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.intervener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerChangeDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

@Slf4j
@Component
public class IntervenerAddedCorresponder extends IntervenerCorresponder {

    public IntervenerAddedCorresponder(NotificationService notificationService, BulkPrintService bulkPrintService,
                                       IntervenerDocumentService intervenerDocumentService) {
        super(notificationService, bulkPrintService, intervenerDocumentService);

    }

    @Override
    public void sendCorrespondence(FinremCaseDetails caseDetails, String authToken) {
        IntervenerChangeDetails intervenerChangeDetails = caseDetails.getData().getCurrentIntervenerChangeDetails();
        log.info("intervener type: {}", intervenerChangeDetails.getIntervenerType());
        sendApplicantCorrespondence(caseDetails, authToken);
        sendRespondentCorrespondence(caseDetails, authToken);
        if (intervenerChangeDetails.getIntervenerType() == IntervenerType.INTERVENER_ONE) {
            sendIntervenerCorrespondence(caseDetails.getData().getIntervenerOneWrapper(), caseDetails, authToken);
        } else if (intervenerChangeDetails.getIntervenerType() == IntervenerType.INTERVENER_TWO) {
            sendIntervenerCorrespondence(caseDetails.getData().getIntervenerTwoWrapper(), caseDetails, authToken);
        } else if (intervenerChangeDetails.getIntervenerType() == IntervenerType.INTERVENER_THREE) {
            sendIntervenerCorrespondence(caseDetails.getData().getIntervenerThreeWrapper(), caseDetails, authToken);
        } else if (intervenerChangeDetails.getIntervenerType() == IntervenerType.INTERVENER_FOUR) {
            sendIntervenerCorrespondence(caseDetails.getData().getIntervenerFourWrapper(), caseDetails, authToken);
        }
    }

    public CaseDocument getAppRepDocumentToPrint(FinremCaseDetails caseDetails, String authorisationToken,
                                                 DocumentHelper.PaperNotificationRecipient recipient) {
        if (caseDetails.getData().getCurrentIntervenerChangeDetails().getIntervenerDetails().getIntervenerRepresented() == YesOrNo.YES) {
            return intervenerDocumentService.generateIntervenerSolicitorAddedLetter(caseDetails, authorisationToken, recipient);
        } else {
            return intervenerDocumentService.generateIntervenerAddedNotificationLetter(caseDetails, authorisationToken, recipient);
        }
    }

    @Override
    public CaseDocument getDocumentToPrint(FinremCaseDetails caseDetails, String authorisationToken,
                                           DocumentHelper.PaperNotificationRecipient recipient) {
        return intervenerDocumentService.generateIntervenerAddedNotificationLetter(caseDetails, authorisationToken, recipient);
    }

    @Override
    protected boolean shouldSendIntervenerSolicitorEmail(IntervenerWrapper intervenerWrapper) {
        return notificationService.isIntervenerSolicitorEmailPopulated(intervenerWrapper);
    }
}
