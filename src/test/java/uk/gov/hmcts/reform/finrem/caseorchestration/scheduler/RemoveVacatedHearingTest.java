package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.State;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.VacatedOrAdjournedHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs.VacatedOrAdjournedHearingTabCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogs;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReference;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceCsvLoader;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.AMEND_CASE_CRON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@ExtendWith(MockitoExtension.class)
class RemoveVacatedHearingTest {

    private static final String AUTH_TOKEN = "testAuthToken";
    private static final String REFERENCE = "1234567890123456";

    @TestLogs
    private final TestLogger logs = new TestLogger(RemoveVacatedHearing.class);

    @Mock private CaseReferenceCsvLoader caseReferenceCsvLoader;
    @Mock private CcdService ccdService;
    @Mock private SystemUserService systemUserService;

    private final FinremCaseDetailsMapper finremCaseDetailsMapper = new FinremCaseDetailsMapper(
        new ObjectMapper().registerModule(new JavaTimeModule()));

    private RemoveVacatedHearing task;

    @BeforeEach
    void setUp() {
        task = new RemoveVacatedHearing(caseReferenceCsvLoader, ccdService, systemUserService, finremCaseDetailsMapper);
        ReflectionTestUtils.setField(task, "isTaskEnabled", true);
        ReflectionTestUtils.setField(task, "csvFile", "caserefs-encrypted.csv");
        ReflectionTestUtils.setField(task, "secret", "DUMMY_SECRET");
        ReflectionTestUtils.setField(task, "caseTypeId", CaseType.CONTESTED.getCcdType());
    }

    @Test
    void givenTaskNotEnabled_whenTaskRun_thenNoInteractions() {
        ReflectionTestUtils.setField(task, "isTaskEnabled", false);
        task.run();
        verifyNoInteractions(ccdService, caseReferenceCsvLoader, systemUserService);
    }

    @Test
    void givenExactlyOneVacatedHearing_whenTaskRun_thenCleared() {
        orchestrateCronMocks(buildCaseDetailsWithHearingCount(1));
        task.run();
        assertThat(logs.getInfos()).contains("Cleared vacatedOrAdjournedHearings for case id " + REFERENCE);
    }

    @Test
    void givenMoreThanOneVacatedHearing_whenTaskRun_thenNotCleared() {
        orchestrateCronMocks(buildCaseDetailsWithHearingCount(2));
        task.run();
        assertThat(logs.getInfos()).doesNotContain("Cleared vacatedOrAdjournedHearings for case id " + REFERENCE);
    }

    private CaseDetails buildCaseDetailsWithHearingCount(int count) {
        List<VacatedOrAdjournedHearingsCollectionItem> vacatedHearings = new ArrayList<>();
        List<VacatedOrAdjournedHearingTabCollectionItem> tabItems = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            vacatedHearings.add(VacatedOrAdjournedHearingsCollectionItem.builder().build());
            tabItems.add(VacatedOrAdjournedHearingTabCollectionItem.builder().build());
        }

        FinremCaseData caseData = FinremCaseData.builder()
            .ccdCaseType(CONTESTED)
            .manageHearingsWrapper(ManageHearingsWrapper.builder()
                .vacatedOrAdjournedHearings(vacatedHearings)
                .vacatedOrAdjournedHearingTabItems(tabItems)
                .build())
            .build();

        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder()
            .data(caseData).id(Long.parseLong(REFERENCE))
            .caseType(CONTESTED).state(State.APPLICATION_ISSUED)
            .build();

        return finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails);
    }

    private void orchestrateCronMocks(CaseDetails caseDetails) {
        CaseReference ref = new CaseReference();
        ref.setCaseReference(REFERENCE);
        when(caseReferenceCsvLoader.loadCaseReferenceList("caserefs-encrypted.csv", "DUMMY_SECRET")).thenReturn(List.of(ref));
        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(ccdService.getCaseByCaseId(REFERENCE, CONTESTED, AUTH_TOKEN))
            .thenReturn(SearchResult.builder().cases(List.of(caseDetails)).total(1).build());
        lenient().when(ccdService.startEventForCaseWorker(AUTH_TOKEN, REFERENCE, CONTESTED.getCcdType(), AMEND_CASE_CRON.getCcdType()))
            .thenReturn(StartEventResponse.builder().caseDetails(caseDetails).build());
    }
}
