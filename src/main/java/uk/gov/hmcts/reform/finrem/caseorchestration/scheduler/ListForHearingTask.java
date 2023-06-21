package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamAuthService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReference;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceCsvLoader;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class ListForHearingTask implements Runnable {

    private final HearingDocumentService hearingDocumentService;
    private final CaseReferenceCsvLoader csvLoader;
    private final CcdService ccdService;
    private final SystemUpdateUserConfiguration systemUpdateUserConfiguration;
    private final IdamAuthService idamAuthService;
    @Value("${cron.batchsize:500}")
    private int bulkPrintBatchSize;
    @Value("${cron.wait-time-mins:10}")
    private int bulkPrintWaitTime;

    @Value("${cron.listforhearing.enabled:false}")
    private boolean isListForHearingTaskEnabled;

    @Override
    public void run() {
        log.info("Scheduled task ListForHearingTask isEnabled {}", isListForHearingTaskEnabled);
        if (isListForHearingTaskEnabled) {
            log.info("Scheduled task ListForHearingTask started to run for selected cases");
            List<CaseReference> caseReferences = csvLoader.loadCaseReferenceList("listForHearingCaseReferenceList.csv");
            final String authToken = idamAuthService.getAccessToken(systemUpdateUserConfiguration.getUserName(),
                systemUpdateUserConfiguration.getPassword());
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
                    CaseDetails caseDetails =
                        ccdService.getCaseByCaseId(caseReference.getCaseReference(), CaseType.CONTESTED, authToken);
                    if (caseDetails != null) {
                        log.info("Sending Forms A, C, G to bulk print for Contested Case ID: {}", caseDetails.getId());
                        hearingDocumentService.sendInitialHearingCorrespondence(caseDetails, authToken);
                        log.info("sent Forms A, C, G to bulk print for Contested Case ID: {}", caseDetails.getId());

                    }

                } catch (InterruptedException | RuntimeException e) {
                    log.error("Error processing caseRef {} ", caseReference.getCaseReference());
                }
            }
        }
    }


}
