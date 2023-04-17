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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.FinremSingleLetterOrEmailAllPartiesCorresponder;

@Slf4j
@Component
public class IntervenerAddedCorresponder extends FinremSingleLetterOrEmailAllPartiesCorresponder {

    private final IntervenerDocumentService intervenerDocumentService;
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
        super(notificationService, bulkPrintService);
        this.intervenerDocumentService = intervenerDocumentService;
        this.intervenerOneDetailsMapper = intervenerOneDetailsMapper;
        this.intervenerTwoDetailsMapper = intervenerTwoDetailsMapper;
        this.intervenerThreeDetailsMapper = intervenerThreeDetailsMapper;
        this.intervenerFourDetailsMapper = intervenerFourDetailsMapper;
    }

    @Override
    public void sendCorrespondence(FinremCaseDetails caseDetails, String authToken) {
        sendApplicantCorrespondence(caseDetails, authToken);
        sendRespondentCorrespondence(caseDetails, authToken);
        IntervenerChangeDetails intervenerChangeDetails = caseDetails.getData().getCurrentIntervenerChangeDetails();
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

    @Override
    protected void sendApplicantCorrespondence(FinremCaseDetails caseDetails, String authorisationToken) {
        if (shouldSendApplicantSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to applicant for case: {}", caseDetails.getId());
            this.emailApplicantSolicitor(caseDetails);
        } else {
            log.info("Sending letter correspondence to applicant for case: {}", caseDetails.getId());
            bulkPrintService.sendDocumentForPrint(
                getAppRepDocumentToPrint(caseDetails, authorisationToken, DocumentHelper.PaperNotificationRecipient.APPLICANT), caseDetails);
        }
    }

    @Override
    protected void sendRespondentCorrespondence(FinremCaseDetails caseDetails, String authorisationToken) {
        if (shouldSendRespondentSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to respondent for case: {}", caseDetails.getId());
            this.emailRespondentSolicitor(caseDetails);
        } else {
            log.info("Sending letter correspondence to respondent for case: {}", caseDetails.getId());
            bulkPrintService.sendDocumentForPrint(
                getAppRepDocumentToPrint(caseDetails, authorisationToken, DocumentHelper.PaperNotificationRecipient.RESPONDENT), caseDetails);
        }
    }

    protected void sendIntervenerOneCorrespondence(FinremCaseDetails caseDetails, String authorisationToken) {
        if (shouldSendIntervenerOneSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to Intervener One for case: {}", caseDetails.getId());
            //send email
        } else {
            log.info("Sending letter correspondence to Intervener One for case: {}", caseDetails.getId());
            caseDetails.getData().getCurrentIntervenerChangeDetails().setIntervenerDetails(
                intervenerOneDetailsMapper.mapToIntervenerDetails(caseDetails.getData().getIntervenerOneWrapper()));

            bulkPrintService.sendDocumentForPrint(
                getDocumentToPrint(caseDetails, authorisationToken,
                    DocumentHelper.PaperNotificationRecipient.INTERVENER_ONE), caseDetails);
        }
    }

    protected void sendIntervenerTwoCorrespondence(FinremCaseDetails caseDetails, String authorisationToken) {
        if (shouldSendIntervenerTwoSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to Intervener Two for case: {}", caseDetails.getId());
            //send email
        } else {
            log.info("Sending letter correspondence to Intervener Two for case: {}", caseDetails.getId());
            caseDetails.getData().getCurrentIntervenerChangeDetails().setIntervenerDetails(
                intervenerTwoDetailsMapper.mapToIntervenerDetails(caseDetails.getData().getIntervenerTwoWrapper()));

            bulkPrintService.sendDocumentForPrint(
                getDocumentToPrint(caseDetails, authorisationToken,
                    DocumentHelper.PaperNotificationRecipient.INTERVENER_TWO), caseDetails);
        }
    }

    protected void sendIntervenerThreeCorrespondence(FinremCaseDetails caseDetails, String authorisationToken) {
        if (shouldSendIntervenerThreeSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to Intervener Three for case: {}", caseDetails.getId());
            //send email
        } else {
            log.info("Sending letter correspondence to Intervener Three for case: {}", caseDetails.getId());
            caseDetails.getData().getCurrentIntervenerChangeDetails().setIntervenerDetails(
                intervenerThreeDetailsMapper.mapToIntervenerDetails(caseDetails.getData().getIntervenerThreeWrapper()));

            bulkPrintService.sendDocumentForPrint(
                getDocumentToPrint(caseDetails, authorisationToken,
                    DocumentHelper.PaperNotificationRecipient.INTERVENER_THREE), caseDetails);
        }
    }

    protected void sendIntervenerFourCorrespondence(FinremCaseDetails caseDetails, String authorisationToken) {
        if (shouldSendIntervenerFourSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to Intervener Four for case: {}", caseDetails.getId());
            //send email
        } else {
            log.info("Sending letter correspondence to Intervener Four for case: {}", caseDetails.getId());
            caseDetails.getData().getCurrentIntervenerChangeDetails().setIntervenerDetails(
                intervenerFourDetailsMapper.mapToIntervenerDetails(caseDetails.getData().getIntervenerFourWrapper()));

            bulkPrintService.sendDocumentForPrint(
                getDocumentToPrint(caseDetails, authorisationToken,
                    DocumentHelper.PaperNotificationRecipient.INTERVENER_FOUR), caseDetails);
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
        // TODO document why this method is empty
    }

    @Override
    protected void emailRespondentSolicitor(FinremCaseDetails caseDetails) {
        // TODO document why this method is empty
    }

}
