package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReference;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Scheduled task to find cases where the CaseRoleId is "CaseRoleId": {"value": null, "list_items": null}.
 * Then update so that CaseRoleId becomes "CaseRoleId": null"
 * To enable the task to execute set environment variables:
 * <ul>
 *     <li>CRON_NULL_CASEROLEIDS_WHERE_EMPTY_ENABLED=true</li>
 *     <li>TASK_NAME=NullCaseRoleIdsWhereEmptyTask</li>
 *     <li>CRON_NULL_CASEROLEIDS_WHERE_EMPTY_CASE_TYPE_ID=FinancialRemedyContested | FinancialRemedyMVP2</li>
 *     <li>CRON_NULL_CASEROLEIDS_WHERE_EMPTY_BATCH_SIZE=number of cases to search for</li>
 * </ul>
 */
@Component
@Slf4j
public class NullCaseRoleIdsWhereEmptyTask extends BaseTask {

    public static final String NOC_FIX_APPLIED_FLAG_FIELD = "isNocFixAppliedFlag";
    private static final String CASE_DATA_NOC_FIX_APPLIED_FLAG = String.format("data.%s", NOC_FIX_APPLIED_FLAG_FIELD);

    private static final String TASK_NAME = "NullCaseRoleIdsWhereEmptyTask";
    private static final String SUMMARY = "DFR-3351";

    @Value("${cron.nullCaseRoleIdsWhereEmpty.enabled:false}")
    private boolean taskEnabled;

    @Value("${cron.nullCaseRoleIdsWhereEmpty.caseTypeId:FinancialRemedyContested}")
    private String caseTypeId;
    @Value("${cron.nullCaseRoleIdsWhereEmpty.batchSize:500}")
    private int batchSize;

    protected NullCaseRoleIdsWhereEmptyTask(CcdService ccdService, SystemUserService systemUserService,
                                            FinremCaseDetailsMapper finremCaseDetailsMapper) {
        super(ccdService, systemUserService, finremCaseDetailsMapper);
    }

    @Override
    protected List<CaseReference> getCaseReferences() {
        List<CaseReference> references = new ArrayList<>();

        try (InputStream is = NullCaseRoleIdsWhereEmptyTask.class.getClassLoader().getResourceAsStream("NOC_Refs.csv")) {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            String line;
            boolean firstLine = true;  // To skip the header

            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;  // Skip the header line
                    continue;
                }

                String[] values = line.split(",");
                references.add(CaseReference.builder().caseReference(values[0]).build());
            }
        } catch (IOException e) {
            log.info("Error loading in Case Data CSV {}", e.getMessage());
        }
        return references;
    }

    @Override
    protected String getTaskName() {
        return TASK_NAME;
    }

    @Override
    protected boolean isTaskEnabled() {
        return taskEnabled;
    }

    @Override
    protected CaseType getCaseType() {
        return CaseType.forValue(caseTypeId);
    }

    @Override
    protected String getSummary() {
        return SUMMARY;
    }

    @Override
    protected void executeTask(FinremCaseDetails finremCaseDetails) {
        FinremCaseData caseData = finremCaseDetails.getData();
        ChangeOrganisationRequest noc = caseData.getChangeOrganisationRequestField();

        if (noc != null && noc.getCaseRoleId() != null && noc.getCaseRoleId().getValue() == null) {
            log.info("Case {} will have caseRoleId set to null", finremCaseDetails.getId());
        } else {
            log.info("Case {} not affected by caseRoleId NOC bug", finremCaseDetails.getId());
        }
    }

    void setTaskEnabled(boolean taskEnabled) {
        this.taskEnabled = taskEnabled;
    }

    void setCaseTypeContested() {
        this.caseTypeId = CaseType.CONTESTED.getCcdType();
    }
}
