package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamDomesticViolence;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamOtherGrounds;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamPreviousAttendance;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.MiamWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceCsvLoader;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamDomesticViolence.FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_10;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamDomesticViolence.FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_11;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamDomesticViolence.FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_12;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamDomesticViolence.FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_13;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamDomesticViolence.FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_14;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamDomesticViolence.FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_7;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamDomesticViolence.FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_8;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamDomesticViolence.FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_9;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamOtherGrounds.FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_12;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamOtherGrounds.FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_13;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamOtherGrounds.FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_14;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamOtherGrounds.FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_15;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamOtherGrounds.FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_16;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamOtherGrounds.FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_5;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamOtherGrounds.FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_9;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamPreviousAttendance.FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_4;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamPreviousAttendance.FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_6;

/**
 * To run as a cron job this task requires environment variables to be set.
 * <ul>
 *     <li>TASK_NAME=MiamExemptionsUpdateTask</li>
 *     <li>CRON_MIAM_EXEMPTIONS_UPDATE_ENABLED=true</li>
 * </ul>
 */
@Setter
@Component
@Slf4j
public class MiamExemptionsUpdateTask extends CsvFileProcessingTask {

    @Value("${cron.miamExemptionsUpdate.enabled:false}")
    private boolean taskEnabled;

    protected MiamExemptionsUpdateTask(CaseReferenceCsvLoader csvLoader,
                                       CcdService ccdService,
                                       SystemUserService systemUserService,
                                       FinremCaseDetailsMapper finremCaseDetailsMapper) {
        super(csvLoader, ccdService, systemUserService, finremCaseDetailsMapper);
    }

    @Override
    protected String getTaskName() {
        return "MiamExemptionsUpdateTask";
    }

    @Override
    protected boolean isTaskEnabled() {
        return taskEnabled;
    }

    @Override
    protected CaseType getCaseType() {
        return CaseType.CONTESTED;
    }

    @Override
    protected String getSummary() {
        return "DFR-3003";
    }

    @Override
    protected void executeTask(FinremCaseDetails finremCaseDetails) {
        long id = finremCaseDetails.getId();
        MiamWrapper miamWrapper = finremCaseDetails.getData().getMiamWrapper();
        updateDomestic(id, miamWrapper);
        updatePrevious(id, miamWrapper);
        updateOther(id, miamWrapper);
    }

    @Override
    protected String getCaseListFileName() {
        return "miamExemptionsUpdateCaseReferenceList.csv";
    }

    private void updateDomestic(long caseReference, MiamWrapper miamWrapper) {
        List<MiamDomesticViolence> current = miamWrapper.getMiamDomesticViolenceChecklist();
        log.info("{} current domestic {}", caseReference, current);
        if (CollectionUtils.isEmpty(current)) {
            return;
        }

        List<MiamDomesticViolence> updated = new ArrayList<>();
        for (MiamDomesticViolence currentValue : current) {
            MiamDomesticViolence updatedValue = switch (currentValue) {
                case FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_7 -> FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_14;
                case FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_8 -> FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_7;
                case FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_9 -> FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_8;
                case FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_10 -> FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_9;
                case FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_11 -> FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_10;
                case FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_12 -> FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_11;
                case FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_13 -> FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_12;
                case FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_14 -> FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_13;
                default -> currentValue;
            };
            log.info("{}: {} domestic mapped to {}", caseReference, currentValue, updatedValue);
            updated.add(updatedValue);
        }
        miamWrapper.setMiamDomesticViolenceChecklist(updated);
    }

    private void updatePrevious(long caseReference, MiamWrapper miamWrapper) {
        MiamPreviousAttendance currentValue = miamWrapper.getMiamPreviousAttendanceChecklist();
        log.info("{} current previous {}", caseReference, currentValue);
        if (currentValue == null) {
            return;
        }

        MiamPreviousAttendance updatedValue = switch (currentValue) {
            case FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_2 -> FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_4;
            case FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_3 -> FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_6;
            default -> currentValue;
        };

        log.info("{}: {} previous mapped to {}", caseReference, currentValue, updatedValue);
        miamWrapper.setMiamPreviousAttendanceChecklist(updatedValue);
    }

    private void updateOther(long caseReference, MiamWrapper miamWrapper) {
        MiamOtherGrounds currentValue = miamWrapper.getMiamOtherGroundsChecklist();
        log.info("{}: current other {}", caseReference, currentValue);
        if (currentValue == null) {
            return;
        }

        MiamOtherGrounds updatedValue = switch (currentValue) {
            case FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_1 -> FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_5;
            case FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_2 -> FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_9;
            case FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_3 -> FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_12;
            case FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_4 -> FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_13;
            case FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_5 -> FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_14;
            case FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_6 -> FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_15;
            case FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_7 -> FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_16;
            default -> currentValue;
        };

        log.info("{}: {} other mapped to {}", caseReference, currentValue, updatedValue);
        miamWrapper.setMiamOtherGroundsChecklist(updatedValue);
    }
}
