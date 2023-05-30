package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.intervener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.intervener.IntervenerFourToIntervenerDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.intervener.IntervenerOneToIntervenerDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.intervener.IntervenerThreeToIntervenerDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.intervener.IntervenerTwoToIntervenerDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerChangeDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

@Slf4j
@Component
public class IntervenerAddedCorresponder extends IntervenerCorresponder {

    private final IntervenerOneToIntervenerDetailsMapper intervenerOneDetailsMapper;
    private final IntervenerTwoToIntervenerDetailsMapper intervenerTwoDetailsMapper;
    private final IntervenerThreeToIntervenerDetailsMapper intervenerThreeDetailsMapper;
    private final IntervenerFourToIntervenerDetailsMapper intervenerFourDetailsMapper;

    public IntervenerAddedCorresponder(NotificationService notificationService, BulkPrintService bulkPrintService,
                                       IntervenerDocumentService intervenerDocumentService,
                                       IntervenerOneToIntervenerDetailsMapper intervenerOneDetailsMapper,
                                       IntervenerTwoToIntervenerDetailsMapper intervenerTwoDetailsMapper,
                                       IntervenerThreeToIntervenerDetailsMapper intervenerThreeDetailsMapper,
                                       IntervenerFourToIntervenerDetailsMapper intervenerFourDetailsMapper) {
        super(notificationService, bulkPrintService, intervenerDocumentService);
        this.intervenerOneDetailsMapper = intervenerOneDetailsMapper;
        this.intervenerTwoDetailsMapper = intervenerTwoDetailsMapper;
        this.intervenerThreeDetailsMapper = intervenerThreeDetailsMapper;
        this.intervenerFourDetailsMapper = intervenerFourDetailsMapper;
    }

    @Override
    public void sendCorrespondence(FinremCaseDetails caseDetails, String authToken) {
        IntervenerChangeDetails intervenerChangeDetails = caseDetails.getData().getCurrentIntervenerChangeDetails();
        log.info("intervener type: {}", intervenerChangeDetails.getIntervenerType());
        sendApplicantCorrespondence(caseDetails, authToken);
        sendRespondentCorrespondence(caseDetails, authToken);
        if (intervenerChangeDetails.getIntervenerType() == IntervenerType.INTERVENER_ONE) {
            sendIntervenerOneCorrespondence(caseDetails, authToken);
        } else if (intervenerChangeDetails.getIntervenerType() == IntervenerType.INTERVENER_TWO) {
            sendIntervenerTwoCorrespondence(caseDetails, authToken);
        } else if (intervenerChangeDetails.getIntervenerType() == IntervenerType.INTERVENER_THREE) {
            sendIntervenerThreeCorrespondence(caseDetails, authToken);
        } else if (intervenerChangeDetails.getIntervenerType() == IntervenerType.INTERVENER_FOUR) {
            sendIntervenerFourCorrespondence(caseDetails, authToken);
        }
    }

    protected void sendIntervenerOneCorrespondence(FinremCaseDetails caseDetails, String authorisationToken) {
        if (shouldSendIntervenerOneSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to Intervener One for case: {}", caseDetails.getId());
            IntervenerDetails intervenerDetails =
                intervenerOneDetailsMapper.mapToIntervenerDetails(caseDetails.getData().getIntervenerOneWrapper());
            String recipientName = intervenerDetails.getIntervenerSolName();
            String recipientEmail = intervenerDetails.getIntervenerSolEmail();
            String referenceNumber = intervenerDetails.getIntervenerSolicitorReference();
            notificationService.sendIntervenerSolicitorAddedEmail(caseDetails, intervenerDetails,
                recipientName, recipientEmail, referenceNumber);
        } else {
            log.info("Sending letter correspondence to Intervener One for case: {}", caseDetails.getId());
            String recipient = DocumentHelper.PaperNotificationRecipient.INTERVENER_ONE.toString();
            bulkPrintService.sendDocumentForPrint(
                getDocumentToPrint(caseDetails, authorisationToken,
                    DocumentHelper.PaperNotificationRecipient.INTERVENER_ONE), caseDetails, recipient, authorisationToken);
        }
    }

    protected void sendIntervenerTwoCorrespondence(FinremCaseDetails caseDetails, String authorisationToken) {
        if (shouldSendIntervenerTwoSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to Intervener Two for case: {}", caseDetails.getId());
            IntervenerDetails intervenerDetails =
                intervenerTwoDetailsMapper.mapToIntervenerDetails(caseDetails.getData().getIntervenerTwoWrapper());
            String recipientName = intervenerDetails.getIntervenerSolName();
            String recipientEmail = intervenerDetails.getIntervenerSolEmail();
            String referenceNumber = intervenerDetails.getIntervenerSolicitorReference();
            notificationService.sendIntervenerSolicitorAddedEmail(caseDetails, intervenerDetails,
                recipientName, recipientEmail, referenceNumber);
        } else {
            log.info("Sending letter correspondence to Intervener Two for case: {}", caseDetails.getId());
            String recipient = DocumentHelper.PaperNotificationRecipient.INTERVENER_TWO.toString();
            bulkPrintService.sendDocumentForPrint(
                getDocumentToPrint(caseDetails, authorisationToken,
                    DocumentHelper.PaperNotificationRecipient.INTERVENER_TWO), caseDetails, recipient, authorisationToken);
        }
    }

    protected void sendIntervenerThreeCorrespondence(FinremCaseDetails caseDetails, String authorisationToken) {
        if (shouldSendIntervenerThreeSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to Intervener Three for case: {}", caseDetails.getId());
            IntervenerDetails intervenerDetails =
                intervenerThreeDetailsMapper.mapToIntervenerDetails(caseDetails.getData().getIntervenerThreeWrapper());
            String recipientName = intervenerDetails.getIntervenerSolName();
            String recipientEmail = intervenerDetails.getIntervenerSolEmail();
            String referenceNumber = intervenerDetails.getIntervenerSolicitorReference();
            notificationService.sendIntervenerSolicitorAddedEmail(caseDetails, intervenerDetails,
                recipientName, recipientEmail, referenceNumber);
        } else {
            log.info("Sending letter correspondence to Intervener Three for case: {}", caseDetails.getId());
            String recipient = DocumentHelper.PaperNotificationRecipient.INTERVENER_THREE.toString();
            bulkPrintService.sendDocumentForPrint(
                getDocumentToPrint(caseDetails, authorisationToken,
                    DocumentHelper.PaperNotificationRecipient.INTERVENER_THREE), caseDetails, recipient, authorisationToken);
        }
    }

    protected void sendIntervenerFourCorrespondence(FinremCaseDetails caseDetails, String authorisationToken) {
        if (shouldSendIntervenerFourSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to Intervener Four for case: {}", caseDetails.getId());
            IntervenerDetails intervenerDetails =
                intervenerFourDetailsMapper.mapToIntervenerDetails(caseDetails.getData().getIntervenerFourWrapper());
            String recipientName = intervenerDetails.getIntervenerSolName();
            String recipientEmail = intervenerDetails.getIntervenerSolEmail();
            String referenceNumber = intervenerDetails.getIntervenerSolicitorReference();
            notificationService.sendIntervenerSolicitorAddedEmail(caseDetails, intervenerDetails,
                recipientName, recipientEmail, referenceNumber);
        } else {
            log.info("Sending letter correspondence to Intervener Four for case: {}", caseDetails.getId());
            String recipient = DocumentHelper.PaperNotificationRecipient.INTERVENER_FOUR.toString();
            bulkPrintService.sendDocumentForPrint(
                getDocumentToPrint(caseDetails, authorisationToken,
                    DocumentHelper.PaperNotificationRecipient.INTERVENER_FOUR), caseDetails, recipient, authorisationToken);
        }
    }

    @Override
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

    protected boolean shouldSendIntervenerOneSolicitorEmail(FinremCaseDetails caseDetails) {
        return notificationService.isIntervenerOneSolicitorDigitalAndEmailPopulated(caseDetails);
    }

    protected boolean shouldSendIntervenerTwoSolicitorEmail(FinremCaseDetails caseDetails) {
        return notificationService.isIntervenerTwoSolicitorDigitalAndEmailPopulated(caseDetails);
    }

    protected boolean shouldSendIntervenerThreeSolicitorEmail(FinremCaseDetails caseDetails) {
        return notificationService.isIntervenerThreeSolicitorDigitalAndEmailPopulated(caseDetails);
    }

    protected boolean shouldSendIntervenerFourSolicitorEmail(FinremCaseDetails caseDetails) {
        return notificationService.isIntervenerFourSolicitorDigitalAndEmailPopulated(caseDetails);
    }

    @Override
    protected void emailApplicantSolicitor(FinremCaseDetails caseDetails) {
        IntervenerDetails intervenerDetails = caseDetails.getData()
            .getCurrentIntervenerChangeDetails().getIntervenerDetails();
        String recipientName = caseDetails.getData().getAppSolicitorName();
        String recipientEmail = caseDetails.getData().getAppSolicitorEmail();
        String referenceNumber = caseDetails.getData().getContactDetailsWrapper().getSolicitorReference();
        if (caseDetails.getData().getCurrentIntervenerChangeDetails().getIntervenerDetails().getIntervenerRepresented() == YesOrNo.YES) {
            notificationService.sendIntervenerSolicitorAddedEmail(caseDetails, intervenerDetails, recipientName, recipientEmail, referenceNumber);
        } else {
            notificationService.sendIntervenerAddedEmail(caseDetails, intervenerDetails, recipientName, recipientEmail, referenceNumber);
        }
    }

    @Override
    protected void emailRespondentSolicitor(FinremCaseDetails caseDetails) {
        IntervenerDetails intervenerDetails = caseDetails.getData()
            .getCurrentIntervenerChangeDetails().getIntervenerDetails();
        String recipientName = caseDetails.getData().getRespondentSolicitorName();
        String recipientEmail = caseDetails.getData().getContactDetailsWrapper().getRespondentSolicitorEmail();
        String referenceNumber = caseDetails.getData().getContactDetailsWrapper().getRespondentSolicitorReference();
        if (caseDetails.getData().getCurrentIntervenerChangeDetails().getIntervenerDetails().getIntervenerRepresented() == YesOrNo.YES) {
            notificationService.sendIntervenerSolicitorAddedEmail(caseDetails, intervenerDetails, recipientName, recipientEmail, referenceNumber);
        } else {
            notificationService.sendIntervenerAddedEmail(caseDetails, intervenerDetails, recipientName, recipientEmail, referenceNumber);
        }
    }
}
