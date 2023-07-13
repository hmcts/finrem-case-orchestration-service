package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public abstract class CaseDetailsMultiLetterOnlyAllPartiesCorresponder extends MultiLetterOnlyAllPartiesCorresponder<CaseDetails> {

    protected final BulkPrintService bulkPrintService;
    protected final NotificationService notificationService;
    protected final FinremCaseDetailsMapper finremCaseDetailsMapper;

    protected void sendApplicantCorrespondence(String authorisationToken, CaseDetails caseDetails) {
        if (!shouldSendApplicantSolicitorEmail(caseDetails)) {
            log.info("Sending letter correspondence to applicant for case: {}", caseDetails.getId());
            List<BulkPrintDocument> documentsToPrint = getDocumentsToPrint(caseDetails, authorisationToken);
            if (!documentsToPrint.isEmpty()) {
                bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken, documentsToPrint);
            }
        }
    }

    public void sendRespondentCorrespondence(String authorisationToken, CaseDetails caseDetails) {
        if (!shouldSendRespondentSolicitorEmail(caseDetails)) {
            log.info("Sending letter correspondence to respondent for case: {}", caseDetails.getId());
            List<BulkPrintDocument> documentsToPrint = getDocumentsToPrint(caseDetails, authorisationToken);
            if (!documentsToPrint.isEmpty()) {
                bulkPrintService.printRespondentDocuments(caseDetails, authorisationToken, documentsToPrint);
            }

        }
    }

    public void sendIntervenerCorrespondence(String authorisationToken, CaseDetails caseDetails) {
        if (notificationService.isContestedApplication(caseDetails)) {
            final FinremCaseDetails finremCaseDetails = finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails);
            final List<IntervenerWrapper> interveners = finremCaseDetails.getData().getInterveners();
            interveners.forEach(intervenerWrapper -> {
                if (!shouldSendIntervenerSolicitorEmail(intervenerWrapper, caseDetails)) {
                    if (intervenerWrapper.getIntervenerName() != null && !intervenerWrapper.getIntervenerName().isEmpty()) {
                        log.info("Sending letter correspondence to {} for case: {}",
                            intervenerWrapper.getIntervenerType().getTypeValue(),
                            caseDetails.getId());
                        List<BulkPrintDocument> documentsToPrint = getDocumentsToPrint(caseDetails, authorisationToken);
                        if (!documentsToPrint.isEmpty()) {
                            bulkPrintService.printIntervenerDocuments(intervenerWrapper, caseDetails, authorisationToken,
                                documentsToPrint);
                        }
                    }
                }
            });
        }
    }


}
