package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDataContested;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerHearingNotice;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerHearingNoticeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public abstract class FinremMultiLetterOrEmailAllPartiesCorresponder extends MultiLetterOrEmailAllPartiesCorresponder<FinremCaseDetails> {

    protected final BulkPrintService bulkPrintService;
    protected final NotificationService notificationService;
    protected final DocumentHelper documentHelper;

    protected void sendApplicantCorrespondence(String authorisationToken, FinremCaseDetails caseDetails) {
        if (caseDetails.getData().isApplicantCorrespondenceEnabled()) {
            if (shouldSendApplicantSolicitorEmail(caseDetails)) {
                log.info("Sending email correspondence to applicant for case: {}", caseDetails.getId());
                this.emailApplicantSolicitor(caseDetails);
            } else {

                log.info("Sending letter correspondence to applicant for case: {}", caseDetails.getId());
                bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken,
                    documentHelper.getCaseDocumentsAsBulkPrintDocuments(getCaseDocuments(caseDetails)));
            }
        }
    }

    public void sendRespondentCorrespondence(String authorisationToken, FinremCaseDetails caseDetails) {
        if (caseDetails.getData().isRespondentCorrespondenceEnabled()) {
            if (shouldSendRespondentSolicitorEmail(caseDetails)) {
                log.info("Sending email correspondence to respondent for case: {}", caseDetails.getId());
                this.emailRespondentSolicitor(caseDetails);
            } else {
                log.info("Sending letter correspondence to respondent for case: {}", caseDetails.getId());
                bulkPrintService.printRespondentDocuments(caseDetails, authorisationToken,
                    documentHelper.getCaseDocumentsAsBulkPrintDocuments(getCaseDocuments(caseDetails)));
            }
        }
    }

    public void sendIntervenerCorrespondence(String authorisationToken,
                                             FinremCaseDetails caseDetails) {
        if (caseDetails.getData().isContestedApplication()) {
            FinremCaseDataContested caseData = (FinremCaseDataContested) caseDetails.getData();
            List<IntervenerWrapper> interveners = caseData.getInterveners();
            interveners.forEach(intervenerWrapper -> {
                log.info("Intervener type {}, communication enabled {}, caseId {}", intervenerWrapper.getIntervenerType(),
                    intervenerWrapper.getIntervenerCorrespondenceEnabled(), caseDetails.getId());
                if (intervenerWrapper.getIntervenerCorrespondenceEnabled() == null
                    || Boolean.TRUE.equals(intervenerWrapper.getIntervenerCorrespondenceEnabled())) {
                    List<CaseDocument> caseDocuments = returnAndAddCaseDocumentsToIntervenerHearingNotices(caseDetails, intervenerWrapper);
                    if (shouldSendIntervenerSolicitorEmail(intervenerWrapper, caseDetails)) {
                        log.info("Sending email correspondence to {} for case: {}",
                            intervenerWrapper.getIntervenerType().getTypeValue(),
                            caseDetails.getId());
                        this.emailIntervenerSolicitor(intervenerWrapper, caseDetails);
                    } else if (intervenerWrapper.getIntervenerName() != null && !intervenerWrapper.getIntervenerName().isEmpty()) {
                        log.info("Sending letter correspondence to {} for case: {}",
                            intervenerWrapper.getIntervenerType().getTypeValue(),
                            caseDetails.getId());
                        List<BulkPrintDocument> documentsToPrint = documentHelper.getCaseDocumentsAsBulkPrintDocuments(caseDocuments);
                        bulkPrintService.printIntervenerDocuments(intervenerWrapper, caseDetails, authorisationToken, documentsToPrint);
                    }

                }
            });
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

    private List<CaseDocument> returnAndAddCaseDocumentsToIntervenerHearingNotices(FinremCaseDetails<FinremCaseDataContested> caseDetails,
                                                                                   IntervenerWrapper intervenerWrapper) {
        List<CaseDocument> caseDocuments = getCaseDocuments(caseDetails);
        List<IntervenerHearingNoticeCollection> intervenerHearingNoticesCollection =
            intervenerWrapper.getIntervenerHearingNoticesCollection(caseDetails.getData());
        caseDocuments.forEach(cd -> intervenerHearingNoticesCollection.add(getHearingNoticesDocumentCollection(cd)));
        return caseDocuments;
    }


    private IntervenerHearingNoticeCollection getHearingNoticesDocumentCollection(CaseDocument hearingNotice) {
        return IntervenerHearingNoticeCollection.builder()
            .value(IntervenerHearingNotice.builder().caseDocument(hearingNotice)
                .noticeReceivedAt(LocalDateTime.now()).build()).build();
    }

}
