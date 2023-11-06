package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CustomRequestScopeAttr;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.GeneralApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReference;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceCsvLoader;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class GeneralApplicationRemoveTask implements Runnable {

    private final GeneralApplicationHelper generalApplicationHelper;
    private final CaseReferenceCsvLoader csvLoader;
    private final CcdService ccdService;
    private final SystemUserService systemUserService;
    private final FinremCaseDetailsMapper finremCaseDetailsMapper;
    @Value("${cron.batchsize:500}")
    private int bulkPrintBatchSize;
    @Value("${cron.wait-time-mins:10}")
    private int bulkPrintWaitTime;

    @Value("${cron.generalApplicationRemove.enabled:false}")
    private boolean isGeneralApplicationRemoveTaskEnabled;

    @Override
    public void run() {
        log.info("Scheduled task GeneralApplicationRemoveTask isEnabled {}", isGeneralApplicationRemoveTaskEnabled);
        if (isGeneralApplicationRemoveTaskEnabled) {
            log.info("Scheduled task GeneralApplicationRemoveTask started to run for selected cases");
            List<CaseReference> caseReferences = csvLoader.loadCaseReferenceList("generalApplicationRemoveCaseReferenceList.csv");
            int count = 0;
            int batchCount = 1;
            for (CaseReference caseReference : caseReferences) {
                count++;
                try {
                    RequestContextHolder.setRequestAttributes(new CustomRequestScopeAttr());
                    if (count == 1) {
                        log.info("Batch {} limit reached {}, pausing for {} seconds", batchCount, bulkPrintBatchSize, bulkPrintWaitTime);
                        TimeUnit.SECONDS.sleep(bulkPrintWaitTime);
                        count = 0;
                        batchCount++;
                    }

                    log.info("Process case reference {}, batch {}, count {}", caseReference.getCaseReference(), batchCount, count);
                    SearchResult searchResult =
                        ccdService.getCaseByCaseId(caseReference.getCaseReference(), CaseType.CONTESTED, systemUserService.getSysUserToken());
                    log.info("SearchResult count {}", searchResult.getTotal());
                    if (CollectionUtils.isNotEmpty(searchResult.getCases())) {
                        CaseDetails caseDetails = searchResult.getCases().get(0);
                        FinremCaseDetails finremCaseDetails = finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails);
                        log.info("Updating general applications for Case ID: {}", caseDetails.getId());
                        generalApplicationHelper.checkAndRemoveDuplicateGeneralApplications(finremCaseDetails.getData());
                        caseDetails = finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails);
                        ccdService.executeCcdEventOnCase(caseDetails, systemUserService.getSysUserToken(),
                            caseDetails.getId().toString(),
                            CaseType.CONTESTED.getCcdType(),
                            EventType.AMEND_CASE_CRON.getCcdType(),
                            "Remove duplicate General application DFR-2388",
                            "Remove duplicate General application DFR-2388");
                        log.info("Updated general applications for Case ID: {}", caseDetails.getId());
                    }

                } catch (InterruptedException | RuntimeException e) {
                    log.error("Error processing caseRef {} and error is {}", caseReference.getCaseReference(), e);
                } finally {
                    RequestContextHolder.resetRequestAttributes();
                }

            }
        }
    }
}
