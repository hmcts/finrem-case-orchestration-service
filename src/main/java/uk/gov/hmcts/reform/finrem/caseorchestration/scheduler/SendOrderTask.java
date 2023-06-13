package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReference;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceCsvLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_APPROVED_COVER_LETTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LATEST_DRAFT_HEARING_ORDER;

@Component
@Slf4j
@RequiredArgsConstructor
public class SendOrderTask implements Runnable {

    private final NotificationService notificationService;
    private final BulkPrintService bulkPrintService;
    private final DocumentHelper documentHelper;
    private final CaseReferenceCsvLoader csvLoader;
    private final CcdService ccdService;
    private final SystemUserService systemUserService;
    @Value("${cron.batchsize:500}")
    private int bulkPrintBatchSize;
    @Value("${cron.wait-time-mins:10}")
    private int bulkPrintWaitTime;

    @Value("${cron.sendorder.enabled:false}")
    private boolean isSendOrderTaskEnabled;

    @Override
    public void run() {
        log.info("Scheduled task SendOrderTask isEnabled {}", isSendOrderTaskEnabled);
        if (isSendOrderTaskEnabled) {
            log.info("Scheduled task SendOrderTask started to run for selected cases");
            List<CaseReference> caseReferences = csvLoader.loadCaseReferenceList("sendOrderCaseReferenceList.csv");
            int count = 0;
            int batchCount = 1;
            for (CaseReference caseReference : caseReferences) {
                count++;
                try {
                    if (count == bulkPrintBatchSize) {
                        log.info("Batch {} limit reached {}, pausing for {} minutes", batchCount, bulkPrintBatchSize, bulkPrintWaitTime);
                        TimeUnit.MINUTES.sleep(bulkPrintWaitTime);
                        count = 0;
                        batchCount++;
                    }

                    log.info("Process case reference {}, batch {}, count {}", caseReference.getCaseReference(), batchCount, count);
                    SearchResult searchResult =
                        ccdService.getCaseByCaseId(caseReference.getCaseReference(), CaseType.CONTESTED, systemUserService.getSysUserToken());
                    if (CollectionUtils.isNotEmpty(searchResult.getCases())) {
                        CaseDetails caseDetails = searchResult.getCases().get(0);
                        printAndMailHearingDocuments(caseDetails, systemUserService.getSysUserToken());
                    }

                } catch (InterruptedException | RuntimeException e) {
                    log.error("Error processing caseRef {} ", caseReference.getCaseReference());
                }
            }
        }
    }

    private void printAndMailHearingDocuments(CaseDetails caseDetails, String authorisationToken) {

        String caseId = String.valueOf(caseDetails.getId());
        log.info("In request to send hearing pack for case {}:", caseId);
        Map<String, Object> caseData = caseDetails.getData();

        List<BulkPrintDocument> hearingDocumentPack = createHearingDocumentPack(caseData, authorisationToken, caseId);
        if (!hearingDocumentPack.isEmpty()) {
            if (!notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)) {
                log.info("Received request to send hearing pack for applicant for case {}:", caseId);
                bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken, hearingDocumentPack);
            }

            if (!notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)) {
                log.info("Received request to send hearing pack for respondent for case {}:", caseId);
                bulkPrintService.printRespondentDocuments(caseDetails, authorisationToken, hearingDocumentPack);
            }
        }
    }

    private List<BulkPrintDocument> createHearingDocumentPack(Map<String, Object> caseData, String authorisationToken, String caseId) {
        List<BulkPrintDocument> hearingDocumentPack = new ArrayList<>();

        documentHelper.getDocumentLinkAsBulkPrintDocument(caseData, CONTESTED_ORDER_APPROVED_COVER_LETTER).ifPresent(hearingDocumentPack::add);
        documentHelper.getDocumentLinkAsBulkPrintDocument(caseData, LATEST_DRAFT_HEARING_ORDER).ifPresent(hearingDocumentPack::add);

        if (documentHelper.hasAnotherHearing(caseData)) {
            Optional<CaseDocument> latestAdditionalHearingDocument = documentHelper.getLatestAdditionalHearingDocument(caseData);
            latestAdditionalHearingDocument.ifPresent(
                caseDocument -> hearingDocumentPack.add(documentHelper.getCaseDocumentAsBulkPrintDocument(caseDocument)));
        }

        List<BulkPrintDocument> otherHearingDocuments = documentHelper.getHearingDocumentsAsBulkPrintDocuments(
            caseData, authorisationToken, caseId);

        if (otherHearingDocuments != null) {
            hearingDocumentPack.addAll(otherHearingDocuments);
        }

        return hearingDocumentPack;
    }

}
