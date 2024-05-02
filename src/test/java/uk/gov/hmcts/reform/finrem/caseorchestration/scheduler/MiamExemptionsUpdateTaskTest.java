package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamDomesticViolence;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamOtherGrounds;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamPreviousAttendance;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamUrgencyReason;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.State;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.MiamWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReference;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceCsvLoader;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.AMEND_CASE_CRON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamDomesticViolence.FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamDomesticViolence.FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_10;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamDomesticViolence.FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_11;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamDomesticViolence.FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_12;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamDomesticViolence.FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_13;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamDomesticViolence.FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_14;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamDomesticViolence.FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_15;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamDomesticViolence.FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_16;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamDomesticViolence.FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_17;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamDomesticViolence.FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_18;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamDomesticViolence.FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_19;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamDomesticViolence.FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamDomesticViolence.FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_20;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamDomesticViolence.FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_21;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamDomesticViolence.FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_22;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamDomesticViolence.FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_23;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamDomesticViolence.FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_3;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamDomesticViolence.FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_4;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamDomesticViolence.FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_5;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamDomesticViolence.FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_6;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamDomesticViolence.FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_7;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamDomesticViolence.FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_8;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamDomesticViolence.FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_9;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamOtherGrounds.FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_1;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamOtherGrounds.FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_9;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamPreviousAttendance.FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamPreviousAttendance.FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_3;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamPreviousAttendance.FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_4;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamPreviousAttendance.FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_6;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamUrgencyReason.FR_MS_MIAM_URGENCY_REASON_CHECKLIST_VALUE_1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamUrgencyReason.FR_MS_MIAM_URGENCY_REASON_CHECKLIST_VALUE_2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamUrgencyReason.FR_MS_MIAM_URGENCY_REASON_CHECKLIST_VALUE_3;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamUrgencyReason.FR_MS_MIAM_URGENCY_REASON_CHECKLIST_VALUE_4;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamUrgencyReason.FR_MS_MIAM_URGENCY_REASON_CHECKLIST_VALUE_5;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamUrgencyReason.FR_MS_MIAM_URGENCY_REASON_CHECKLIST_VALUE_6;

class MiamExemptionsUpdateTaskTest {

    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    private CcdService ccdService;

    private MiamExemptionsUpdateTask task;

    @BeforeEach
    void setup() {
        finremCaseDetailsMapper = new FinremCaseDetailsMapper(new ObjectMapper());

        CaseReferenceCsvLoader csvLoader = mock(CaseReferenceCsvLoader.class);
        List<CaseReference> caseReferences = List.of(
            new CaseReference("1")
        );
        when(csvLoader.loadCaseReferenceList(anyString())).thenReturn(caseReferences);

        ccdService = mock(CcdService.class);

        SystemUserService systemUserService = mock(SystemUserService.class);
        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);

        task = new MiamExemptionsUpdateTask(csvLoader, ccdService, systemUserService, finremCaseDetailsMapper);
        task.setTaskEnabled(true);
    }

    @Test
    void testDomesticExemptions() {
        FinremCaseData caseData = createCaseData(null, null);
        SearchResult searchResult = createSearchResult(caseData);
        when(ccdService.getCaseByCaseId("1", CaseType.CONTESTED, AUTH_TOKEN)).thenReturn(searchResult);

        StartEventResponse startEventResponse = createStartEventResponse(caseData);
        when(ccdService.startEventForCaseWorker(AUTH_TOKEN, "1", CaseType.CONTESTED.getCcdType(),
            AMEND_CASE_CRON.getCcdType())).thenReturn(startEventResponse);

        task.run();

        FinremCaseData updatedCaseData = finremCaseDetailsMapper.mapToFinremCaseData(
            startEventResponse.getCaseDetails().getData(), CaseType.CONTESTED.getCcdType());
        MiamWrapper miamWrapper = updatedCaseData.getMiamWrapper();
        assertThat(miamWrapper.getMiamDomesticViolenceChecklist()).containsExactly(
            FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_1,
            FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_2,
            FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_3,
            FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_4,
            FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_5,
            FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_6,
            FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_14,
            FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_7,
            FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_8,
            FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_9,
            FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_10,
            FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_11,
            FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_12,
            FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_13,
            FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_15,
            FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_16,
            FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_17,
            FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_18,
            FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_19,
            FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_20,
            FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_21,
            FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_22,
            FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_23
        );
    }

    @Test
    void testUrgencyExemptions() {
        FinremCaseData caseData = createCaseData(null, null);
        SearchResult searchResult = createSearchResult(caseData);
        when(ccdService.getCaseByCaseId("1", CaseType.CONTESTED, AUTH_TOKEN)).thenReturn(searchResult);

        StartEventResponse startEventResponse = createStartEventResponse(caseData);
        when(ccdService.startEventForCaseWorker(AUTH_TOKEN, "1", CaseType.CONTESTED.getCcdType(),
            AMEND_CASE_CRON.getCcdType())).thenReturn(startEventResponse);

        task.run();

        FinremCaseData updatedCaseData = finremCaseDetailsMapper.mapToFinremCaseData(
            startEventResponse.getCaseDetails().getData(), CaseType.CONTESTED.getCcdType());
        MiamWrapper miamWrapper = updatedCaseData.getMiamWrapper();
        assertThat(miamWrapper.getMiamUrgencyReasonChecklist()).containsExactly(
            FR_MS_MIAM_URGENCY_REASON_CHECKLIST_VALUE_1, FR_MS_MIAM_URGENCY_REASON_CHECKLIST_VALUE_2,
            FR_MS_MIAM_URGENCY_REASON_CHECKLIST_VALUE_3, FR_MS_MIAM_URGENCY_REASON_CHECKLIST_VALUE_4,
            FR_MS_MIAM_URGENCY_REASON_CHECKLIST_VALUE_5, FR_MS_MIAM_URGENCY_REASON_CHECKLIST_VALUE_6
        );
    }

    @ParameterizedTest
    @MethodSource
    void testPreviousExemptions(MiamPreviousAttendance currentValue, MiamPreviousAttendance expectedUpdatedValue) {
        FinremCaseData caseData = createCaseData(currentValue, null);
        SearchResult searchResult = createSearchResult(caseData);
        when(ccdService.getCaseByCaseId("1", CaseType.CONTESTED, AUTH_TOKEN)).thenReturn(searchResult);

        StartEventResponse startEventResponse = createStartEventResponse(caseData);
        when(ccdService.startEventForCaseWorker(AUTH_TOKEN, "1", CaseType.CONTESTED.getCcdType(),
            AMEND_CASE_CRON.getCcdType())).thenReturn(startEventResponse);

        task.run();

        FinremCaseData updatedCaseData = finremCaseDetailsMapper.mapToFinremCaseData(
            startEventResponse.getCaseDetails().getData(), CaseType.CONTESTED.getCcdType());
        MiamWrapper miamWrapper = updatedCaseData.getMiamWrapper();
        assertThat(miamWrapper.getMiamPreviousAttendanceChecklist()).isEqualTo(expectedUpdatedValue);
    }

    private static Stream<Arguments> testPreviousExemptions() {
        return Stream.of(
            Arguments.of(FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_2,
                FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_4),
            Arguments.of(FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_3,
                FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_6)
        );
    }

    @ParameterizedTest
    @MethodSource
    void testOtherExemptions(MiamOtherGrounds currentValue, MiamOtherGrounds expectedUpdatedValue) {
        FinremCaseData caseData = createCaseData(null, currentValue);
        SearchResult searchResult = createSearchResult(caseData);
        when(ccdService.getCaseByCaseId("1", CaseType.CONTESTED, AUTH_TOKEN)).thenReturn(searchResult);

        StartEventResponse startEventResponse = createStartEventResponse(caseData);
        when(ccdService.startEventForCaseWorker(AUTH_TOKEN, "1", CaseType.CONTESTED.getCcdType(),
            AMEND_CASE_CRON.getCcdType())).thenReturn(startEventResponse);

        task.run();

        FinremCaseData updatedCaseData = finremCaseDetailsMapper.mapToFinremCaseData(
            startEventResponse.getCaseDetails().getData(), CaseType.CONTESTED.getCcdType());
        MiamWrapper miamWrapper = updatedCaseData.getMiamWrapper();
        assertThat(miamWrapper.getMiamOtherGroundsChecklist()).isEqualTo(expectedUpdatedValue);
    }

    private static Stream<Arguments> testOtherExemptions() {
        return Stream.of(
            Arguments.of(FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_1, FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_5),
            Arguments.of(FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_2, FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_9),
            Arguments.of(FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_3, FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_12),
            Arguments.of(FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_4, FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_13),
            Arguments.of(FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_5, FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_14),
            Arguments.of(FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_6, FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_15),
            Arguments.of(FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_7, FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_16)
        );
    }

    private FinremCaseData createCaseData(MiamPreviousAttendance miamPreviousAttendance,
                                          MiamOtherGrounds miamOtherGrounds) {
        MiamWrapper miamWrapper = MiamWrapper.builder()
            .miamDomesticViolenceChecklist(Arrays.asList(MiamDomesticViolence.values()))
            .miamUrgencyReasonChecklist(Arrays.asList(MiamUrgencyReason.values()))
            .miamPreviousAttendanceChecklist(miamPreviousAttendance)
            .miamOtherGroundsChecklist(miamOtherGrounds)
            .build();

        return FinremCaseData.builder()
            .miamWrapper(miamWrapper)
            .build();
    }

    private StartEventResponse createStartEventResponse(FinremCaseData caseData) {
        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .id(1L)
            .caseType(CaseType.CONTESTED)
            .state(State.APPLICATION_ISSUED)
            .data(caseData)
            .build();

        return StartEventResponse.builder()
            .caseDetails(finremCaseDetailsMapper.mapToCaseDetails(caseDetails))
            .build();
    }

    private SearchResult createSearchResult(FinremCaseData caseData) {
        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .id(1L)
            .caseType(CaseType.CONTESTED)
            .state(State.APPLICATION_ISSUED)
            .data(caseData)
            .build();

        return SearchResult.builder()
            .cases(List.of(finremCaseDetailsMapper.mapToCaseDetails(caseDetails)))
            .total(1)
            .build();
    }
}
