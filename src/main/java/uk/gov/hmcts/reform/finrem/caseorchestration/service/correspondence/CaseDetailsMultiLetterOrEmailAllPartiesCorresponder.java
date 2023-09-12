package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public abstract class CaseDetailsMultiLetterOrEmailAllPartiesCorresponder extends MultiLetterOrEmailAllPartiesCorresponder<CaseDetails> {

    protected final BulkPrintService bulkPrintService;
    protected final NotificationService notificationService;
    protected final FinremCaseDetailsMapper finremCaseDetailsMapper;
    protected final DocumentHelper documentHelper;

    @SuppressWarnings("java:S1874")
    protected void sendApplicantCorrespondence(String authorisationToken, CaseDetails caseDetails) {
        if (shouldSendApplicantSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to applicant for case: {}", caseDetails.getId());
            this.emailApplicantSolicitor(caseDetails);
        } else {
            log.info("Sending letter correspondence to applicant for case: {}", caseDetails.getId());
            bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken,
                documentHelper.getCaseDocumentsAsBulkPrintDocuments(getCaseDocuments(caseDetails)));
        }
    }

    @SuppressWarnings("java:S1874")
    public void sendRespondentCorrespondence(String authorisationToken, CaseDetails caseDetails) {
        if (shouldSendRespondentSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to respondent for case: {}", caseDetails.getId());
            this.emailRespondentSolicitor(caseDetails);
        } else {
            log.info("Sending letter correspondence to respondent for case: {}", caseDetails.getId());
            bulkPrintService.printRespondentDocuments(caseDetails, authorisationToken,
                documentHelper.getCaseDocumentsAsBulkPrintDocuments(getCaseDocuments(caseDetails)));
        }
    }


    @SuppressWarnings("java:S1874")
    public void sendIntervenerCorrespondence(String authorisationToken, CaseDetails caseDetails) {
        if (notificationService.isContestedApplication(caseDetails)) {
            final FinremCaseDetails finremCaseDetails = finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails);
            final List<IntervenerWrapper> interveners = finremCaseDetails.getData().getInterveners();
            interveners.forEach(intervenerWrapper -> {
                if (intervenerWrapper.getIntervenerCorrespondenceEnabled() == null
                    || Boolean.TRUE.equals(intervenerWrapper.getIntervenerCorrespondenceEnabled())) {
                    List<CaseDocument> caseDocuments = returnAndAddCaseDocumentsToIntervenerHearingNotices(caseDetails, intervenerWrapper);
                    if (shouldSendIntervenerSolicitorEmail(intervenerWrapper, finremCaseDetails)) {
                        log.info("Sending email correspondence to {} for case: {}",
                            intervenerWrapper.getIntervenerType().getTypeValue(),
                            caseDetails.getId());
                        this.emailIntervenerSolicitor(intervenerWrapper, caseDetails);
                    } else if (intervenerWrapper.getIntervenerName() != null && !intervenerWrapper.getIntervenerName().isEmpty()) {
                        log.info("Sending letter correspondence to {} for case: {}",
                            intervenerWrapper.getIntervenerType().getTypeValue(),
                            caseDetails.getId());
                        bulkPrintService.printIntervenerDocuments(intervenerWrapper, caseDetails, authorisationToken,
                            documentHelper.getCaseDocumentsAsBulkPrintDocuments(caseDocuments));
                    }
                }
            });

        }
    }

    private List<CaseDocument> returnAndAddCaseDocumentsToIntervenerHearingNotices(CaseDetails caseDetails, IntervenerWrapper intervenerWrapper) {
        List<CaseDocument> caseDocuments = getCaseDocuments(caseDetails);
        if (intervenerWrapper.getHearingNoticesDocumentCollection() == null) {
            intervenerWrapper.setHearingNoticesDocumentCollection(new ArrayList<>());
        }
        caseDocuments.forEach(cd -> {
            intervenerWrapper.getHearingNoticesDocumentCollection().add(DocumentCollection.builder().value(cd).build());
        });
        return caseDocuments;
    }

    protected boolean shouldSendApplicantSolicitorEmail(CaseDetails caseDetails) {
        return notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails);
    }

    protected boolean shouldSendRespondentSolicitorEmail(CaseDetails caseDetails) {
        return notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails);
    }

    protected boolean shouldSendIntervenerSolicitorEmail(IntervenerWrapper intervenerWrapper, FinremCaseDetails caseDetails) {
        return notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(intervenerWrapper, caseDetails);
    }

}
