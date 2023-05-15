package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.intervener;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.intervener.IntervenerFourToIntervenerDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.intervener.IntervenerOneToIntervenerDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.intervener.IntervenerThreeToIntervenerDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.intervener.IntervenerTwoToIntervenerDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IntervenerService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.FinremSingleLetterOrEmailAllPartiesCorresponder;

@Slf4j
@Component
public class IntervenerRemovedCorresponder extends FinremSingleLetterOrEmailAllPartiesCorresponder {

    private final IntervenerDocumentService intervenerDocumentService;
    private final IntervenerService intervenerService;
    private final IntervenerOneToIntervenerDetailsMapper intervenerOneDetailsMapper;
    private final IntervenerTwoToIntervenerDetailsMapper intervenerTwoDetailsMapper;
    private final IntervenerThreeToIntervenerDetailsMapper intervenerThreeDetailsMapper;
    private final IntervenerFourToIntervenerDetailsMapper intervenerFourDetailsMapper;

    public IntervenerRemovedCorresponder(NotificationService notificationService, BulkPrintService bulkPrintService,
                                         IntervenerDocumentService intervenerDocumentService, IntervenerService intervenerService,
                                         IntervenerOneToIntervenerDetailsMapper intervenerOneDetailsMapper,
                                         IntervenerTwoToIntervenerDetailsMapper intervenerTwoDetailsMapper,
                                         IntervenerThreeToIntervenerDetailsMapper intervenerThreeDetailsMapper,
                                         IntervenerFourToIntervenerDetailsMapper intervenerFourDetailsMapper) {
        super(notificationService, bulkPrintService);
        this.intervenerDocumentService = intervenerDocumentService;
        this.intervenerService = intervenerService;
        this.intervenerOneDetailsMapper = intervenerOneDetailsMapper;
        this.intervenerTwoDetailsMapper = intervenerTwoDetailsMapper;
        this.intervenerThreeDetailsMapper = intervenerThreeDetailsMapper;
        this.intervenerFourDetailsMapper = intervenerFourDetailsMapper;
    }

    public void sendCorrespondence(FinremCaseDetails caseDetails, FinremCaseDetails caseDetailsBefore, String authToken) {
        sendApplicantCorrespondence(caseDetails, caseDetailsBefore, authToken);
        sendRespondentCorrespondence(caseDetails, caseDetailsBefore, authToken);
        if (hasIntervenerOneBeenRemoved(caseDetails, caseDetailsBefore)) {
            sendIntervenerOneCorrespondence(caseDetails, caseDetailsBefore, authToken);
        } else if (hasIntervenerTwoBeenRemoved(caseDetails, caseDetailsBefore)) {
            sendIntervenerTwoCorrespondence(caseDetails, caseDetailsBefore, authToken);
        } else if (hasIntervenerThreeBeenRemoved(caseDetails, caseDetailsBefore)) {
            sendIntervenerThreeCorrespondence(caseDetails, caseDetailsBefore, authToken);
        } else if (hasIntervenerFourBeenRemoved(caseDetails, caseDetailsBefore)) {
            sendIntervenerFourCorrespondence(caseDetails, caseDetailsBefore, authToken);
        }
    }

    protected void sendApplicantCorrespondence(FinremCaseDetails caseDetails, FinremCaseDetails caseDetailsBefore, String authorisationToken) {
        if (shouldSendApplicantSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to applicant for case: {}", caseDetails.getId());
            this.emailApplicantSolicitor(caseDetails);
        }
        if (hasAnyIntervenerBeenRemoved(caseDetails, caseDetailsBefore)) {
            bulkPrintService.sendDocumentForPrint(
                printIntervenerRemovedLetter(caseDetails, authorisationToken, DocumentHelper.PaperNotificationRecipient.APPLICANT), caseDetails);
            log.info("Sending letter correspondence to applicant for case: {}", caseDetails.getId());
        }
        if (intervenerService.checkIfAnyIntervenerSolicitorRemoved(caseDetails.getData(), caseDetailsBefore.getData())) {
            bulkPrintService.sendDocumentForPrint(
                printIntervenerSolicitorRemovedLetter(caseDetails, authorisationToken, DocumentHelper.PaperNotificationRecipient.APPLICANT),
                caseDetails);
            log.info("Sending letter correspondence to applicant for case: {}", caseDetails.getId());
        }
    }

    protected void sendRespondentCorrespondence(FinremCaseDetails caseDetails, FinremCaseDetails caseDetailsBefore, String authorisationToken) {
        if (shouldSendRespondentSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to respondent for case: {}", caseDetails.getId());
            this.emailRespondentSolicitor(caseDetails);
        }
        if (hasAnyIntervenerBeenRemoved(caseDetails, caseDetailsBefore)) {
            bulkPrintService.sendDocumentForPrint(
                printIntervenerRemovedLetter(caseDetails, authorisationToken, DocumentHelper.PaperNotificationRecipient.RESPONDENT), caseDetails);
            log.info("Sending letter correspondence to respondent for case: {}", caseDetails.getId());
        }
        if (intervenerService.checkIfAnyIntervenerSolicitorRemoved(caseDetails.getData(), caseDetailsBefore.getData())) {
            bulkPrintService.sendDocumentForPrint(
                printIntervenerSolicitorRemovedLetter(caseDetails, authorisationToken, DocumentHelper.PaperNotificationRecipient.RESPONDENT),
                caseDetails);
            log.info("Sending letter correspondence to respondent for case: {}", caseDetails.getId());
        }
    }

    protected void sendIntervenerOneCorrespondence(FinremCaseDetails caseDetails, FinremCaseDetails caseDetailsBefore, String authorisationToken) {
        if (shouldSendIntervenerOneSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to Intervener One for case: {}", caseDetails.getId());
            //send email
        }
        log.info("Sending letter correspondence to Intervener One for case: {}", caseDetails.getId());
        caseDetails.getData().getCurrentIntervenerChangeDetails()
            .setIntervenerDetails(intervenerOneDetailsMapper.mapToIntervenerDetails(caseDetailsBefore.getData().getIntervenerOneWrapper()));
        bulkPrintService.sendDocumentForPrint(
            getDocumentToPrint(caseDetails, authorisationToken, DocumentHelper.PaperNotificationRecipient.INTERVENER_ONE), caseDetails);
    }

    protected void sendIntervenerTwoCorrespondence(FinremCaseDetails caseDetails, FinremCaseDetails caseDetailsBefore, String authorisationToken) {
        if (shouldSendIntervenerTwoSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to Intervener Two for case: {}", caseDetails.getId());
            //send email
        }
        log.info("Sending letter correspondence to Intervener Two for case: {}", caseDetails.getId());
        caseDetails.getData().getCurrentIntervenerChangeDetails()
            .setIntervenerDetails(intervenerTwoDetailsMapper.mapToIntervenerDetails(caseDetailsBefore.getData().getIntervenerTwoWrapper()));
        bulkPrintService.sendDocumentForPrint(
            getDocumentToPrint(caseDetails, authorisationToken, DocumentHelper.PaperNotificationRecipient.INTERVENER_TWO), caseDetails);
    }

    protected void sendIntervenerThreeCorrespondence(FinremCaseDetails caseDetails, FinremCaseDetails caseDetailsBefore, String authorisationToken) {
        if (shouldSendIntervenerThreeSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to Intervener Three for case: {}", caseDetails.getId());
            //send email
        }
        log.info("Sending letter correspondence to Intervener Three for case: {}", caseDetails.getId());
        caseDetails.getData().getCurrentIntervenerChangeDetails()
            .setIntervenerDetails(intervenerThreeDetailsMapper.mapToIntervenerDetails(caseDetailsBefore.getData().getIntervenerThreeWrapper()));
        bulkPrintService.sendDocumentForPrint(
            getDocumentToPrint(caseDetails, authorisationToken, DocumentHelper.PaperNotificationRecipient.INTERVENER_THREE), caseDetails);
    }

    protected void sendIntervenerFourCorrespondence(FinremCaseDetails caseDetails, FinremCaseDetails caseDetailsBefore, String authorisationToken) {
        if (shouldSendIntervenerFourSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to Intervener Four for case: {}", caseDetails.getId());
            //send email
        }
        log.info("Sending letter correspondence to Intervener Four for case: {}", caseDetails.getId());
        caseDetails.getData().getCurrentIntervenerChangeDetails()
            .setIntervenerDetails(intervenerFourDetailsMapper.mapToIntervenerDetails(caseDetailsBefore.getData().getIntervenerFourWrapper()));
        bulkPrintService.sendDocumentForPrint(
            getDocumentToPrint(caseDetails, authorisationToken, DocumentHelper.PaperNotificationRecipient.INTERVENER_FOUR), caseDetails);
    }

    public CaseDocument printIntervenerSolicitorRemovedLetter(FinremCaseDetails caseDetails, String authorisationToken,
                                                              DocumentHelper.PaperNotificationRecipient recipient) {
        return intervenerDocumentService.generateIntervenerSolicitorRemovedLetter(caseDetails, authorisationToken, recipient);
    }

    public CaseDocument printIntervenerRemovedLetter(FinremCaseDetails caseDetails, String authorisationToken,
                                                     DocumentHelper.PaperNotificationRecipient recipient) {
        return intervenerDocumentService.generateIntervenerRemovedNotificationLetter(caseDetails, authorisationToken, recipient);
    }

    @Override
    public CaseDocument getDocumentToPrint(FinremCaseDetails caseDetails, String authorisationToken,
                                           DocumentHelper.PaperNotificationRecipient recipient) {
        return intervenerDocumentService.generateIntervenerRemovedNotificationLetter(caseDetails, authorisationToken, recipient);
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

    private boolean hasIntervenerOneBeenRemoved(FinremCaseDetails caseDetails, FinremCaseDetails caseDetailsBefore) {
        return !StringUtils.isNotEmpty(caseDetails.getData().getIntervenerOneWrapper().getIntervener1Name()) &&
            StringUtils.isNotEmpty(caseDetailsBefore.getData().getIntervenerOneWrapper().getIntervener1Name());
    }

    private boolean hasIntervenerTwoBeenRemoved(FinremCaseDetails caseDetails, FinremCaseDetails caseDetailsBefore) {
        return !StringUtils.isNotEmpty(caseDetails.getData().getIntervenerTwoWrapper().getIntervener2Name()) &&
            StringUtils.isNotEmpty(caseDetailsBefore.getData().getIntervenerTwoWrapper().getIntervener2Name());
    }

    private boolean hasIntervenerThreeBeenRemoved(FinremCaseDetails caseDetails, FinremCaseDetails caseDetailsBefore) {
        return !StringUtils.isNotEmpty(caseDetails.getData().getIntervenerThreeWrapper().getIntervener3Name()) &&
            StringUtils.isNotEmpty(caseDetailsBefore.getData().getIntervenerThreeWrapper().getIntervener3Name());
    }

    private boolean hasIntervenerFourBeenRemoved(FinremCaseDetails caseDetails, FinremCaseDetails caseDetailsBefore) {
        return !StringUtils.isNotEmpty(caseDetails.getData().getIntervenerFourWrapper().getIntervener4Name()) &&
            StringUtils.isNotEmpty(caseDetailsBefore.getData().getIntervenerFourWrapper().getIntervener4Name());
    }

    private boolean hasAnyIntervenerBeenRemoved(FinremCaseDetails caseDetails, FinremCaseDetails caseDetailsBefore) {
        return hasIntervenerOneBeenRemoved(caseDetails, caseDetailsBefore) || hasIntervenerTwoBeenRemoved(caseDetails, caseDetailsBefore) ||
            hasIntervenerThreeBeenRemoved(caseDetails, caseDetailsBefore) || hasIntervenerFourBeenRemoved(caseDetails, caseDetailsBefore);
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
