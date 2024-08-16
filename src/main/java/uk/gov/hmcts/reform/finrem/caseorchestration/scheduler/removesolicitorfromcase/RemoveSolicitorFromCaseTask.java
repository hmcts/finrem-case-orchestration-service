package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler.removesolicitorfromcase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdDataStoreService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;

import java.io.IOException;
import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.AMEND_CASE_CRON;

/**
 * Scheduled task to remove solicitor access from cases.
 * To enable the task to execute set environment variables:
 * <ul>
 *     <li>CRON_REMOVE_SOLICITOR_FROM_CASE_ENABLED=true</li>
 *     <li>TASK_NAME=RemoveSolicitorFromCaseTask</li>
 * </ul>
 * and add data to the <code>removeSolicitorFromCase.csv</code> file.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RemoveSolicitorFromCaseTask implements Runnable {

    @Value("${cron.removeSolicitorFromCase.enabled:false}")
    private boolean taskEnabled;

    private static final String TASK_NAME = "RemoveSolicitorFromCaseTask";

    private static final String FILENAME = "removeSolicitorFromCase.csv";

    private final RemoveSolicitorFromCaseFileReader removeSolicitorFromCaseFileReader;
    private final SystemUserService systemUserService;
    private final CcdService ccdService;
    private final CcdDataStoreService ccdDataStoreService;

    @Override
    public void run() {
        log.info("Scheduled task {} enabled: {}", TASK_NAME, taskEnabled);
        if (!taskEnabled) {
            return;
        }

        List<RemoveSolicitorFromCaseRequest> requests;
        try {
            requests = removeSolicitorFromCaseFileReader.read(FILENAME);
        } catch (IOException e) {
            throw new RemoveSolicitorFromCaseException("Unable to read requests", e);
        }
        if (requests.isEmpty()) {
            log.info("No requests found");
            return;
        }

        String systemUserToken = getSystemUserToken();
        requests.forEach(r -> handleRequest(r, systemUserToken));
    }

    private void handleRequest(RemoveSolicitorFromCaseRequest request, String systemUserToken) {
        log.info("Removing {} access to case {} for solicitor {}", request.role(), request.caseReference(),
            request.userId());

        CaseType caseType = CaseType.forValue(request.caseType());
        SearchResult searchResult = ccdService.getCaseByCaseId(request.caseReference(), caseType, systemUserToken);

        if (searchResult.getTotal() != 1) {
            throw new RemoveSolicitorFromCaseException(
                String.format("Found %d search results for case %s", searchResult.getTotal(), request.caseReference()));
        }

        CaseDetails caseDetails = searchResult.getCases().get(0);
        removeSolicitorCaseAccess(request, caseDetails, systemUserToken);

        log.info("Removed {} access to case {} for solicitor {}", request.role(), request.caseReference(),
            request.userId());
    }

    private String getSystemUserToken() {
        return systemUserService.getSysUserToken();
    }

    private void removeSolicitorCaseAccess(RemoveSolicitorFromCaseRequest request, CaseDetails caseDetails,
                                           String systemUserToken) {
        String caseType = CaseType.forValue(request.caseType()).getCcdType();
        String ccdEventType = AMEND_CASE_CRON.getCcdType();

        StartEventResponse startEventResponse = ccdService.startEventForCaseWorker(systemUserToken,
            request.caseReference(), caseType, ccdEventType);

        ccdDataStoreService.removeUserRole(caseDetails, systemUserToken, request.userId(), request.role());

        String summary = request.updateReference();
        String description = String.format("Remove user %s case role %s", request.userId(), request.role());
        ccdService.submitEventForCaseWorker(startEventResponse, systemUserToken, request.caseReference(), caseType,
            ccdEventType, summary, description);
    }

    void setEnabled(boolean enabled) {
        this.taskEnabled = enabled;
    }
}
