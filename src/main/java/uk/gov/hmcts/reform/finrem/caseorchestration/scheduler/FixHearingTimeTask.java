package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs.HearingTabCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceCsvLoader;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

@Component
@Slf4j
public class FixHearingTimeTask extends EncryptedCsvFileProcessingTask {
    private static final String TASK_NAME = "FixHearingTimeTask";
    private static final String SUMMARY = "DFR-5936 Fix hearingTime containing additional information";

    private static final String CORRECT_TIME = "10:00 AM";
    private static final int MAX_TIME_LENGTH = 100;
    private static final DateTimeFormatter TAB_DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.UK);

    @Value("${cron.fixHearingTime.task.enabled:false}")
    private boolean taskEnabled;
    @Value("${cron.fixHearingTime.caseTypeId:FinancialRemedyContested}")
    private String caseTypeId;
    @Value("${cron.fixHearingTime.caseListFileName:fixHearingTime-encrypted.csv}")
    private String csvFile;

    public FixHearingTimeTask(CaseReferenceCsvLoader csvLoader, CcdService ccdService,
                              SystemUserService systemUserService,
                              FinremCaseDetailsMapper finremCaseDetailsMapper) {
        super(csvLoader, ccdService, systemUserService, finremCaseDetailsMapper);
    }

    @Override
    protected void executeTask(FinremCaseDetails finremCaseDetails) {
        FinremCaseData caseData = finremCaseDetails.getData();
        ManageHearingsWrapper wrapper = caseData.getManageHearingsWrapper();

        ManageHearingsCollectionItem hearingItem = wrapper.getHearings().stream()
            .filter(this::hasInvalidTime)
            .findFirst()
            .orElse(null);

        if (hearingItem == null) {
            log.info("Case ID: {} - No hearing with invalid time, skipping.", finremCaseDetails.getId());
            return;
        }

        UUID hearingId = hearingItem.getId();
        Hearing hearing = hearingItem.getValue();

        hearing.setHearingTime(CORRECT_TIME);

        String tabDateTime = hearing.getHearingDate().format(TAB_DATE_FORMAT) + " " + CORRECT_TIME;

        Stream.of(wrapper.getHearingTabItems(),
                wrapper.getApplicantHearingTabItems(),
                wrapper.getRespondentHearingTabItems())
            .filter(Objects::nonNull)
            .flatMap(List::stream)
            .filter(tab -> hearingId.equals(tab.getId()))
            .map(HearingTabCollectionItem::getValue)
            .forEach(tab -> tab.setTabDateTime(tabDateTime));

        log.info("Case ID: {} - Hearing {} time set to {}.", finremCaseDetails.getId(), hearingId, CORRECT_TIME);
    }

    private boolean hasInvalidTime(ManageHearingsCollectionItem item) {
        String time = item.getValue().getHearingTime();
        return time != null && time.length() > MAX_TIME_LENGTH;
    }

    @Override
    protected String getDescription(FinremCaseDetails finremCaseDetails) {
        return String.format("Fix hearingTime longer than %d characters, set to %s", MAX_TIME_LENGTH, CORRECT_TIME);
    }

    @Override
    protected String getCaseListFileName() {
        return csvFile;
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
}
