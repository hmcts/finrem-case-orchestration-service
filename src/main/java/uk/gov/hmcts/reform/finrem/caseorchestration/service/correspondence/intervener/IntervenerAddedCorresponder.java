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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.FinremSingleLetterOrEmailAllPartiesCorresponder;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_ONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_TWO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_THREE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_FOUR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;

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

    public void sendCorrespondence(FinremCaseDetails caseDetails, FinremCaseDetails caseDetailsBefore, String authToken) {
        sendApplicantCorrespondence(caseDetails, caseDetailsBefore, authToken);
        sendRespondentCorrespondence(caseDetails, caseDetailsBefore, authToken);
        if (hasIntervenerOneBeenAdded(caseDetails, caseDetailsBefore)) {
            sendIntervenerOneCorrespondence(caseDetails, authToken);
        } else if (hasIntervenerTwoBeenAdded(caseDetails, caseDetailsBefore)) {
            sendIntervenerTwoCorrespondence(caseDetails, authToken);
        } else if (hasIntervenerThreeBeenAdded(caseDetails, caseDetailsBefore)) {
            sendIntervenerThreeCorrespondence(caseDetails, authToken);
        } else if (hasIntervenerFourBeenAdded(caseDetails, caseDetailsBefore)) {
            sendIntervenerFourCorrespondence(caseDetails, authToken);
        }
    }

    protected void sendApplicantCorrespondence(FinremCaseDetails caseDetails, FinremCaseDetails caseDetailsBefore, String authorisationToken) {
        if (shouldSendApplicantSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to applicant for case: {}", caseDetails.getId());
            this.emailApplicantSolicitor(caseDetails);
        }
        if (hasAnyIntervenerSolicitorBeenAdded(caseDetails)) {
            bulkPrintService.sendDocumentForPrint(
                printIntervenerSolicitorAddedLetter(caseDetails, authorisationToken, DocumentHelper.PaperNotificationRecipient.APPLICANT),
                caseDetails, APPLICANT);
            log.info("Sending letter correspondence to applicant for case: {}", caseDetails.getId());
        }
        if (hasAnyIntervenerBeenAdded(caseDetails, caseDetailsBefore)) {
            bulkPrintService.sendDocumentForPrint(
                printIntervenerAddedLetter(caseDetails, authorisationToken, DocumentHelper.PaperNotificationRecipient.APPLICANT), caseDetails,
                APPLICANT);
            log.info("Sending letter correspondence to applicant for case: {}", caseDetails.getId());
        }
    }

    protected void sendRespondentCorrespondence(FinremCaseDetails caseDetails, FinremCaseDetails caseDetailsBefore, String authorisationToken) {
        if (shouldSendRespondentSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to respondent for case: {}", caseDetails.getId());
            this.emailRespondentSolicitor(caseDetails);
        }
        if (hasAnyIntervenerSolicitorBeenAdded(caseDetails)) {
            bulkPrintService.sendDocumentForPrint(
                printIntervenerSolicitorAddedLetter(caseDetails, authorisationToken, DocumentHelper.PaperNotificationRecipient.RESPONDENT),
                caseDetails, RESPONDENT);
            log.info("Sending letter correspondence to respondent for case: {}", caseDetails.getId());
        }
        if (hasAnyIntervenerBeenAdded(caseDetails, caseDetailsBefore)) {
            bulkPrintService.sendDocumentForPrint(
                printIntervenerAddedLetter(caseDetails, authorisationToken, DocumentHelper.PaperNotificationRecipient.RESPONDENT), caseDetails,
                RESPONDENT);
            log.info("Sending letter correspondence to respondent for case: {}", caseDetails.getId());
        }
    }

    protected void sendIntervenerOneCorrespondence(FinremCaseDetails caseDetails, String authorisationToken) {
        if (shouldSendIntervenerOneSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to Intervener One for case: {}", caseDetails.getId());
            //send email
        }
        log.info("Sending letter correspondence to Intervener One for case: {}", caseDetails.getId());
        caseDetails.getData().getCurrentIntervenerChangeDetails()
            .setIntervenerDetails(intervenerOneDetailsMapper.mapToIntervenerDetails(caseDetails.getData().getIntervenerOneWrapper()));
        bulkPrintService.sendDocumentForPrint(
            getDocumentToPrint(caseDetails, authorisationToken, DocumentHelper.PaperNotificationRecipient.INTERVENER_ONE), caseDetails,
            INTERVENER_ONE);
    }

    protected void sendIntervenerTwoCorrespondence(FinremCaseDetails caseDetails, String authorisationToken) {
        if (shouldSendIntervenerTwoSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to Intervener Two for case: {}", caseDetails.getId());
            //send email
        }
        log.info("Sending letter correspondence to Intervener Two for case: {}", caseDetails.getId());
        caseDetails.getData().getCurrentIntervenerChangeDetails()
            .setIntervenerDetails(intervenerTwoDetailsMapper.mapToIntervenerDetails(caseDetails.getData().getIntervenerTwoWrapper()));
        bulkPrintService.sendDocumentForPrint(
            getDocumentToPrint(caseDetails, authorisationToken, DocumentHelper.PaperNotificationRecipient.INTERVENER_TWO), caseDetails,
            INTERVENER_TWO);
    }

    protected void sendIntervenerThreeCorrespondence(FinremCaseDetails caseDetails, String authorisationToken) {
        if (shouldSendIntervenerThreeSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to Intervener Three for case: {}", caseDetails.getId());
            //send email
        }
        log.info("Sending letter correspondence to Intervener Three for case: {}", caseDetails.getId());
        caseDetails.getData().getCurrentIntervenerChangeDetails()
            .setIntervenerDetails(intervenerThreeDetailsMapper.mapToIntervenerDetails(caseDetails.getData().getIntervenerThreeWrapper()));
        bulkPrintService.sendDocumentForPrint(
            getDocumentToPrint(caseDetails, authorisationToken, DocumentHelper.PaperNotificationRecipient.INTERVENER_THREE), caseDetails,
            INTERVENER_THREE);
    }

    protected void sendIntervenerFourCorrespondence(FinremCaseDetails caseDetails, String authorisationToken) {
        if (shouldSendIntervenerFourSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to Intervener Four for case: {}", caseDetails.getId());
            //send email
        }
        log.info("Sending letter correspondence to Intervener Four for case: {}", caseDetails.getId());
        caseDetails.getData().getCurrentIntervenerChangeDetails()
            .setIntervenerDetails(intervenerFourDetailsMapper.mapToIntervenerDetails(caseDetails.getData().getIntervenerFourWrapper()));
        bulkPrintService.sendDocumentForPrint(
            getDocumentToPrint(caseDetails, authorisationToken, DocumentHelper.PaperNotificationRecipient.INTERVENER_FOUR), caseDetails,
            INTERVENER_FOUR);
    }

    public CaseDocument printIntervenerSolicitorAddedLetter(FinremCaseDetails caseDetails, String authorisationToken,
                                                            DocumentHelper.PaperNotificationRecipient recipient) {
        return intervenerDocumentService.generateIntervenerSolicitorAddedLetter(caseDetails, authorisationToken, recipient);
    }

    public CaseDocument printIntervenerAddedLetter(FinremCaseDetails caseDetails, String authorisationToken,
                                                   DocumentHelper.PaperNotificationRecipient recipient) {
        return intervenerDocumentService.generateIntervenerAddedNotificationLetter(caseDetails, authorisationToken, recipient);
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

    private boolean hasAnyIntervenerSolicitorBeenAdded(FinremCaseDetails caseDetails) {
        return caseDetails.getData().getCurrentIntervenerChangeDetails().getIntervenerDetails().getIntervenerRepresented() == YesOrNo.YES;
    }

    private boolean hasAnyIntervenerBeenAdded(FinremCaseDetails caseDetails, FinremCaseDetails caseDetailsBefore) {
        return hasIntervenerOneBeenAdded(caseDetails, caseDetailsBefore) || hasIntervenerTwoBeenAdded(caseDetails, caseDetailsBefore) ||
            hasIntervenerThreeBeenAdded(caseDetails, caseDetailsBefore) || hasIntervenerFourBeenAdded(caseDetails, caseDetailsBefore);
    }

    private boolean hasIntervenerOneBeenAdded(FinremCaseDetails caseDetails, FinremCaseDetails caseDetailsBefore) {
        return StringUtils.isNotEmpty(caseDetails.getData().getIntervenerOneWrapper().getIntervener1Name()) &&
            !StringUtils.isNotEmpty(caseDetailsBefore.getData().getIntervenerOneWrapper().getIntervener1Name());
    }

    private boolean hasIntervenerTwoBeenAdded(FinremCaseDetails caseDetails, FinremCaseDetails caseDetailsBefore) {
        return StringUtils.isNotEmpty(caseDetails.getData().getIntervenerTwoWrapper().getIntervener2Name()) &&
            !StringUtils.isNotEmpty(caseDetailsBefore.getData().getIntervenerTwoWrapper().getIntervener2Name());
    }

    private boolean hasIntervenerThreeBeenAdded(FinremCaseDetails caseDetails, FinremCaseDetails caseDetailsBefore) {
        return StringUtils.isNotEmpty(caseDetails.getData().getIntervenerThreeWrapper().getIntervener3Name()) &&
            !StringUtils.isNotEmpty(caseDetailsBefore.getData().getIntervenerThreeWrapper().getIntervener3Name());
    }

    private boolean hasIntervenerFourBeenAdded(FinremCaseDetails caseDetails, FinremCaseDetails caseDetailsBefore) {
        return StringUtils.isNotEmpty(caseDetails.getData().getIntervenerFourWrapper().getIntervener4Name()) &&
            !StringUtils.isNotEmpty(caseDetailsBefore.getData().getIntervenerFourWrapper().getIntervener4Name());
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
