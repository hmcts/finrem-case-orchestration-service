package uk.gov.hmcts.reform.finrem.caseorchestration.service.miam;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamOtherGrounds;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamOtherGroundsV2;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamPreviousAttendance;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamPreviousAttendanceV2;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.MiamWrapper;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
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

class MiamLegacyExemptionsServiceTest {

    @ParameterizedTest
    @MethodSource("legacyExemptions")
    void testIsLegacyExemptionsInvalidMiamWrapper(MiamPreviousAttendance miamPreviousAttendance,
                                                  MiamOtherGrounds miamOtherGrounds, boolean expected) {
        MiamWrapper miamWrapper = miamWrapper(miamPreviousAttendance, miamOtherGrounds);
        MiamLegacyExemptionsService miamLegacyExemptionsService = new MiamLegacyExemptionsService();
        assertThat(miamLegacyExemptionsService.isLegacyExemptionsInvalid(miamWrapper)).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("legacyExemptions")
    void testIsLegacyExemptionsInvalidMap(MiamPreviousAttendance miamPreviousAttendance,
                                          MiamOtherGrounds miamOtherGrounds, boolean expected) {
        Map<String, Object> caseData = createCaseData(miamPreviousAttendance, miamOtherGrounds);
        MiamLegacyExemptionsService miamLegacyExemptionsService = new MiamLegacyExemptionsService();
        assertThat(miamLegacyExemptionsService.isLegacyExemptionsInvalid(caseData)).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("legacyConversions")
    void testConvertLegacyExemptions(MiamWrapper miamWrapper, MiamPreviousAttendanceV2 expectedPreviousAttendance,
                                     MiamOtherGroundsV2 expectedOtherAttendance) {
        MiamLegacyExemptionsService miamLegacyExemptionsService = new MiamLegacyExemptionsService();
        miamLegacyExemptionsService.convertLegacyExemptions(miamWrapper);

        assertThat(miamWrapper.getMiamPreviousAttendanceChecklistV2()).isEqualTo(expectedPreviousAttendance);
        assertThat(miamWrapper.getMiamOtherGroundsChecklistV2()).isEqualTo(expectedOtherAttendance);
        assertThat(miamWrapper.getMiamPreviousAttendanceChecklist()).isNull();
        assertThat(miamWrapper.getMiamOtherGroundsChecklist()).isNull();
    }

    @ParameterizedTest
    @MethodSource("legacyExemptions")
    void testGetInvalidLegacyExemptionsWrapper(MiamPreviousAttendance miamPreviousAttendance,
                                               MiamOtherGrounds miamOtherGrounds, boolean expected) {
        MiamWrapper miamWrapper = miamWrapper(miamPreviousAttendance, miamOtherGrounds);
        MiamLegacyExemptionsService miamLegacyExemptionsService = new MiamLegacyExemptionsService();

        int expectedInvalidExemptions = getExpectedInvalidExemptions(expected, miamPreviousAttendance,
            miamOtherGrounds);

        assertThat(miamLegacyExemptionsService.getInvalidLegacyExemptions(miamWrapper))
            .hasSize(expectedInvalidExemptions);
    }

    @ParameterizedTest
    @MethodSource("legacyExemptions")
    void testGetInvalidLegacyExemptionsMap(MiamPreviousAttendance miamPreviousAttendance,
                                               MiamOtherGrounds miamOtherGrounds, boolean expected) {
        Map<String, Object> caseData = createCaseData(miamPreviousAttendance, miamOtherGrounds);
        MiamLegacyExemptionsService miamLegacyExemptionsService = new MiamLegacyExemptionsService();

        int expectedInvalidExemptions = getExpectedInvalidExemptions(expected, miamPreviousAttendance,
            miamOtherGrounds);

        assertThat(miamLegacyExemptionsService.getInvalidLegacyExemptions(caseData))
            .hasSize(expectedInvalidExemptions);
    }

    @Test
    void testRemoveLegacyExemptions() {
        Map<String, Object> caseData = createCaseData(FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_1,
            FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_1);
        MiamLegacyExemptionsService miamLegacyExemptionsService = new MiamLegacyExemptionsService();
        miamLegacyExemptionsService.removeLegacyExemptions(caseData);
        assertThat(caseData.containsKey(MIAM_PREVIOUS_ATTENDANCE_CHECKLIST)).isFalse();
        assertThat(caseData.containsKey(MIAM_OTHER_GROUNDS_CHECKLIST)).isFalse();

        // Repeat test to ensure it works on case data with no legacy exemptions
        miamLegacyExemptionsService.removeLegacyExemptions(caseData);
        assertThat(caseData.containsKey(MIAM_PREVIOUS_ATTENDANCE_CHECKLIST)).isFalse();
        assertThat(caseData.containsKey(MIAM_OTHER_GROUNDS_CHECKLIST)).isFalse();
    }

    private static Stream<Arguments> legacyExemptions() {
        return Stream.of(
            Arguments.of(FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_1, null, false),
            Arguments.of(FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_2, null, true),
            Arguments.of(FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_3, null, true),
            Arguments.of(FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_4, null, false),
            Arguments.of(FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_5, null, true),
            Arguments.of(FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_6, null, false),

            Arguments.of(null, FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_1, true),
            Arguments.of(null, FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_2, true),
            Arguments.of(null, FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_3, true),
            Arguments.of(null, FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_4, true),
            Arguments.of(null, FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_5, false),
            Arguments.of(null, FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_6, true),
            Arguments.of(null, FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_7, true),
            Arguments.of(null, FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_8, true),
            Arguments.of(null, FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_9, false),
            Arguments.of(null, FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_10, true),
            Arguments.of(null, FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_11, true),
            Arguments.of(null, FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_12, false),
            Arguments.of(null, FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_13, false),
            Arguments.of(null, FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_14, false),
            Arguments.of(null, FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_15, false),
            Arguments.of(null, FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_16, false)
        );
    }

    private static Stream<Arguments> legacyConversions() {
        return Stream.of(
            Arguments.of(miamWrapper(FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_1),
                FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_V2_VALUE_1, null),
            Arguments.of(miamWrapper(FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_2), null, null),
            Arguments.of(miamWrapper(FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_3), null, null),
            Arguments.of(miamWrapper(FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_4),
                FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_V2_VALUE_4, null),
            Arguments.of(miamWrapper(FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_5), null, null),
            Arguments.of(miamWrapper(FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_6),
                FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_V2_VALUE_6, null),

            Arguments.of(miamWrapper(FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_1), null, null),
            Arguments.of(miamWrapper(FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_2), null, null),
            Arguments.of(miamWrapper(FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_3), null, null),
            Arguments.of(miamWrapper(FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_4), null, null),
            Arguments.of(miamWrapper(FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_5), null,
                FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_V2_VALUE_5),
            Arguments.of(miamWrapper(FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_6), null, null),
            Arguments.of(miamWrapper(FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_7), null, null),
            Arguments.of(miamWrapper(FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_8), null, null),
            Arguments.of(miamWrapper(FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_9), null,
                FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_V2_VALUE_9),
            Arguments.of(miamWrapper(FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_10), null, null),
            Arguments.of(miamWrapper(FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_11), null, null),
            Arguments.of(miamWrapper(FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_12), null,
                FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_V2_VALUE_12),
            Arguments.of(miamWrapper(FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_13), null,
                FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_V2_VALUE_13),
            Arguments.of(miamWrapper(FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_14), null,
                FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_V2_VALUE_14),
            Arguments.of(miamWrapper(FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_15), null,
                FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_V2_VALUE_15),
            Arguments.of(miamWrapper(FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_16), null,
                FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_V2_VALUE_16)
        );
    }

    private static MiamWrapper miamWrapper(MiamPreviousAttendance miamPreviousAttendance) {
        return miamWrapper(miamPreviousAttendance, null);
    }

    private static MiamWrapper miamWrapper(MiamOtherGrounds miamOtherGrounds) {
        return miamWrapper(null, miamOtherGrounds);
    }

    private static MiamWrapper miamWrapper(MiamPreviousAttendance miamPreviousAttendance,
                                           MiamOtherGrounds miamOtherGrounds) {
        return MiamWrapper.builder()
            .miamPreviousAttendanceChecklist(miamPreviousAttendance)
            .miamOtherGroundsChecklist(miamOtherGrounds)
            .build();
    }

    private Map<String, Object> createCaseData(MiamPreviousAttendance miamPreviousAttendance,
                                               MiamOtherGrounds miamOtherGrounds) {
        Map<String, Object> caseData = new HashMap<>();
        if (miamPreviousAttendance != null) {
            caseData.put(MIAM_PREVIOUS_ATTENDANCE_CHECKLIST, miamPreviousAttendance.getValue());
        }
        if (miamOtherGrounds != null) {
            caseData.put(MIAM_OTHER_GROUNDS_CHECKLIST, miamOtherGrounds.getValue());
        }

        return caseData;
    }

    private int getExpectedInvalidExemptions(boolean expected, MiamPreviousAttendance miamPreviousAttendance,
                                             MiamOtherGrounds miamOtherGrounds) {
        if (expected) {
            return (miamPreviousAttendance != null ? 1 : 0) + (miamOtherGrounds != null ? 1 : 0);
        } else {
            return 0;
        }
    }
}
