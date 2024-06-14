package uk.gov.hmcts.reform.finrem.caseorchestration.service.miam;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamOtherGrounds;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamOtherGroundsV2;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamPreviousAttendance;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamPreviousAttendanceV2;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.MiamWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIAM_OTHER_GROUNDS_CHECKLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIAM_PREVIOUS_ATTENDANCE_CHECKLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamOtherGrounds.FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamOtherGrounds.FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_10;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamOtherGrounds.FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_11;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamOtherGrounds.FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_12;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamOtherGrounds.FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_13;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamOtherGrounds.FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_14;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamOtherGrounds.FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_15;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamOtherGrounds.FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_16;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamOtherGrounds.FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamOtherGrounds.FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_3;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamOtherGrounds.FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_4;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamOtherGrounds.FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_5;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamOtherGrounds.FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_6;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamOtherGrounds.FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_7;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamOtherGrounds.FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_8;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamOtherGrounds.FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_9;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamOtherGroundsV2.FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_V2_VALUE_12;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamOtherGroundsV2.FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_V2_VALUE_13;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamOtherGroundsV2.FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_V2_VALUE_14;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamOtherGroundsV2.FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_V2_VALUE_15;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamOtherGroundsV2.FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_V2_VALUE_16;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamOtherGroundsV2.FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_V2_VALUE_5;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamOtherGroundsV2.FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_V2_VALUE_9;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamPreviousAttendance.FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamPreviousAttendance.FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamPreviousAttendance.FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_3;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamPreviousAttendance.FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_4;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamPreviousAttendance.FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_5;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamPreviousAttendance.FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_6;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamPreviousAttendanceV2.FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_V2_VALUE_1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamPreviousAttendanceV2.FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_V2_VALUE_4;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamPreviousAttendanceV2.FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_V2_VALUE_6;

@Service
public class MiamLegacyExemptionsService {

    /**
     * Previous Attendance options that were present in the v1 MIAM options list but are no longer present in v2.
     * They are therefore invalid if present on draft cases.
     */
    private static final List<MiamPreviousAttendance> PREVIOUS_ATTENDANCE_INVALID_OPTIONS = List.of(
        FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_2,
        FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_3,
        FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_5
    );

    /**
     * Other Grounds options that were present in the v1 MIAM options list but are no longer present in v2.
     * They are therefore invalid if present on draft cases.
     */
    private static final List<MiamOtherGrounds> OTHER_GROUNDS_INVALID_OPTIONS = List.of(
        FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_1,
        FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_2,
        FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_3,
        FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_4,
        FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_6,
        FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_7,
        FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_8,
        FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_10,
        FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_11
    );

    /** Map from v1 to v2 MIAM options. */
    private static final Map<MiamPreviousAttendance, MiamPreviousAttendanceV2> PREVIOUS_ATTENDANCE_MAP = Map.of(
        FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_1, FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_V2_VALUE_1,
        FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_4, FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_V2_VALUE_4,
        FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_6, FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_V2_VALUE_6
    );

    /** Map from v1 to v2 MIAM options. */
    private static final Map<MiamOtherGrounds, MiamOtherGroundsV2> OTHER_GROUNDS_MAP = Map.of(
        FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_5, FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_V2_VALUE_5,
        FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_9, FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_V2_VALUE_9,
        FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_12, FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_V2_VALUE_12,
        FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_13, FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_V2_VALUE_13,
        FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_14, FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_V2_VALUE_14,
        FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_15, FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_V2_VALUE_15,
        FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_16, FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_V2_VALUE_16
    );

    /**
     * Check if the MIAM data contains an exemption that is no longer valid for MIAM v2 exemptions.
     * @param miamWrapper MIAM case data
     * @return true if there is an invalid MIAM exemption false otherwise
     */
    public boolean isLegacyExemptionsInvalid(MiamWrapper miamWrapper) {
        return isPreviousAttendanceInvalid(miamWrapper.getMiamPreviousAttendanceChecklist())
            || isOtherGroundsInvalid(miamWrapper.getMiamOtherGroundsChecklist());
    }

    /**
     * Check if the MIAM data contains an exemption that is no longer valid for MIAM v2 exemptions.
     * @param caseData map of all case data
     * @return true if there is an invalid MIAM exemption false otherwise
     */
    public boolean isLegacyExemptionsInvalid(Map<String, Object> caseData) {
        MiamPreviousAttendance miamPreviousAttendance = getMiamPreviousAttendance(caseData);
        MiamOtherGrounds miamOtherGrounds = getMiamOtherGrounds(caseData);

        return isPreviousAttendanceInvalid(miamPreviousAttendance) || isOtherGroundsInvalid(miamOtherGrounds);
    }

    /**
     * Convert MIAM exemption data from v1 to v2.
     * @param miamWrapper MIAM case data
     */
    public void convertLegacyExemptions(MiamWrapper miamWrapper) {
        if (miamWrapper.getMiamPreviousAttendanceChecklist() != null) {
            miamWrapper.setMiamPreviousAttendanceChecklistV2(
                PREVIOUS_ATTENDANCE_MAP.get(miamWrapper.getMiamPreviousAttendanceChecklist()));
            miamWrapper.setMiamPreviousAttendanceChecklist(null);
        }
        if (miamWrapper.getMiamOtherGroundsChecklist() != null) {
            miamWrapper.setMiamOtherGroundsChecklistV2(
                OTHER_GROUNDS_MAP.get(miamWrapper.getMiamOtherGroundsChecklist()));
            miamWrapper.setMiamOtherGroundsChecklist(null);
        }
    }

    public List<String> getInvalidLegacyExemptions(MiamWrapper miamWrapper) {
        return getInvalidLegacyExemptions(miamWrapper.getMiamPreviousAttendanceChecklist(),
            miamWrapper.getMiamOtherGroundsChecklist());
    }

    public List<String> getInvalidLegacyExemptions(Map<String, Object> caseData) {
        MiamPreviousAttendance miamPreviousAttendance = getMiamPreviousAttendance(caseData);
        MiamOtherGrounds miamOtherGrounds = getMiamOtherGrounds(caseData);

        return getInvalidLegacyExemptions(miamPreviousAttendance, miamOtherGrounds);
    }

    private List<String> getInvalidLegacyExemptions(MiamPreviousAttendance miamPreviousAttendance,
                                                   MiamOtherGrounds miamOtherGrounds) {
        List<String> invalidLegacyExemptions = new ArrayList<>();

        if (isPreviousAttendanceInvalid(miamPreviousAttendance)) {
            invalidLegacyExemptions.add(miamPreviousAttendance.getText());
        }
        if (isOtherGroundsInvalid(miamOtherGrounds)) {
            invalidLegacyExemptions.add(miamOtherGrounds.getText());
        }

        return invalidLegacyExemptions;
    }

    private boolean isPreviousAttendanceInvalid(MiamPreviousAttendance miamPreviousAttendance) {
        return miamPreviousAttendance != null && PREVIOUS_ATTENDANCE_INVALID_OPTIONS.contains(miamPreviousAttendance);
    }

    private boolean isOtherGroundsInvalid(MiamOtherGrounds miamOtherGrounds) {
        return miamOtherGrounds != null && OTHER_GROUNDS_INVALID_OPTIONS.contains(miamOtherGrounds);
    }

    private MiamPreviousAttendance getMiamPreviousAttendance(Map<String, Object> caseData) {
        if (caseData.get(MIAM_PREVIOUS_ATTENDANCE_CHECKLIST) instanceof String previous) {
            return MiamPreviousAttendance.forValue(previous);
        } else {
            return null;
        }
    }

    private MiamOtherGrounds getMiamOtherGrounds(Map<String, Object> caseData) {
        if (caseData.get(MIAM_OTHER_GROUNDS_CHECKLIST) instanceof String other) {
            return MiamOtherGrounds.forValue(other);
        } else {
            return null;
        }
    }
}
