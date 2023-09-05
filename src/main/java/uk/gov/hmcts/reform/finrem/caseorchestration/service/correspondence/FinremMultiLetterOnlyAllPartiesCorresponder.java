package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.capitalize;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER3;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER4;

@Component
@Slf4j
@RequiredArgsConstructor
public abstract class FinremMultiLetterOnlyAllPartiesCorresponder extends MultiLetterOnlyAllPartiesCorresponder<FinremCaseDetails> {

    protected final BulkPrintService bulkPrintService;
    protected final NotificationService notificationService;

    protected void sendApplicantCorrespondence(String authorisationToken, FinremCaseDetails caseDetails) {
        if (caseDetails.getData().isApplicantCorrespondenceEnabled()) {
            if (!shouldSendApplicantSolicitorEmail(caseDetails)) {
                log.info("Sending letter correspondence to applicant for case: {}", caseDetails.getId());
                bulkPrintService.printApplicantDocuments(caseDetails,
                    authorisationToken,
                    getDocumentsToPrint(caseDetails, authorisationToken, DocumentHelper.PaperNotificationRecipient.APPLICANT));
            }
        }
    }

    protected void sendRespondentCorrespondence(String authorisationToken, FinremCaseDetails caseDetails) {
        if (caseDetails.getData().isRespondentCorrespondenceEnabled()) {
            if (!shouldSendRespondentSolicitorEmail(caseDetails)) {
                log.info("Sending letter correspondence to respondent for case: {}", caseDetails.getId());
                bulkPrintService.printRespondentDocuments(caseDetails,
                    authorisationToken,
                    getDocumentsToPrint(caseDetails, authorisationToken, DocumentHelper.PaperNotificationRecipient.RESPONDENT));
            }
        }
    }

    public void sendIntervenerCorrespondence(String authorisationToken, FinremCaseDetails caseDetails) {
        FinremCaseData caseData = caseDetails.getData();
        List<IntervenerWrapper> interveners = caseData.getInterveners();
        interveners.forEach(intervenerWrapper -> {
            log.info("Intervener type {}, communication enabled {}, caseId {}", intervenerWrapper.getIntervenerType(),
                intervenerWrapper.getIntervenerCorrespondenceEnabled(), caseDetails.getId());
            if (intervenerWrapper.getIntervenerCorrespondenceEnabled() != null
                && Boolean.TRUE.equals(intervenerWrapper.getIntervenerCorrespondenceEnabled())) {
                if (!shouldSendIntervenerSolicitorEmail(intervenerWrapper, caseDetails)
                    && intervenerWrapper.getIntervenerName() != null && !intervenerWrapper.getIntervenerName().isEmpty()) {
                    log.info("Sending letter correspondence to {} for case: {}",
                        intervenerWrapper.getIntervenerType().getTypeValue(),
                        caseDetails.getId());
                    bulkPrintService.printIntervenerDocuments(intervenerWrapper, caseDetails, authorisationToken,
                        getDocumentsToPrint(caseDetails, authorisationToken,
                            getIntervenerPaperNotificationRecipient(capitalize(intervenerWrapper.getIntervenerType().getTypeValue()))));
                }
            }
        });
    }

    protected DocumentHelper.PaperNotificationRecipient getIntervenerPaperNotificationRecipient(String recipient) {
        switch (recipient) {
            case INTERVENER1 -> {
                return DocumentHelper.PaperNotificationRecipient.INTERVENER_ONE;
            }
            case INTERVENER2 -> {
                return DocumentHelper.PaperNotificationRecipient.INTERVENER_TWO;
            }
            case INTERVENER3 -> {
                return DocumentHelper.PaperNotificationRecipient.INTERVENER_THREE;
            }
            case INTERVENER4 -> {
                return DocumentHelper.PaperNotificationRecipient.INTERVENER_FOUR;
            }
            default -> {
                return null;
            }
        }
    }

    protected boolean shouldSendApplicantSolicitorEmail(FinremCaseDetails caseDetails) {
        return notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails);
    }

    protected boolean shouldSendRespondentSolicitorEmail(FinremCaseDetails caseDetails) {
        return notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails);
    }

    protected boolean shouldSendIntervenerSolicitorEmail(IntervenerWrapper intervenerWrapper, FinremCaseDetails caseDetails) {
        return notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(intervenerWrapper, caseDetails);
    }

}