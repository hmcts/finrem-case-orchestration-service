package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.State;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ListForHearingWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceCsvLoader;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.AMEND_CASE_CRON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@ExtendWith(MockitoExtension.class)
class GlobalSearchMigrationTaskTest {

    @InjectMocks
    private GlobalSearchMigrationTask globalSearchMigrationTask;

    @Mock
    private CcdService ccdService;
    @Mock
    private CaseReferenceCsvLoader caseReferenceCsvLoader;
    @Mock
    private SystemUserService systemUserService;

    private final FinremCaseDetailsMapper finremCaseDetailsMapper = new FinremCaseDetailsMapper(
        new ObjectMapper().registerModule(new JavaTimeModule()));

    @Captor
    private ArgumentCaptor<FinremCaseDetails> finremCaseDetailsArgumentCaptor;
    private static final String REFERENCE = "1234567890123456";

    @BeforeEach
    void setup() {

        ReflectionTestUtils.setField(globalSearchMigrationTask, "taskEnabled", true);
        ReflectionTestUtils.setField(globalSearchMigrationTask, "dryRun", false);
        ReflectionTestUtils.setField(globalSearchMigrationTask, "supplementaryDataRequired", true);
        ReflectionTestUtils.setField(globalSearchMigrationTask, "caseTypeId", CaseType.CONTESTED.getCcdType());
        ReflectionTestUtils.setField(globalSearchMigrationTask, "finremCaseDetailsMapper", finremCaseDetailsMapper);
    }

    @Test
    void givenTaskNotEnabled_whenTaskRun_thenNoResend() {
        ReflectionTestUtils.setField(globalSearchMigrationTask, "taskEnabled", false);
        globalSearchMigrationTask.run();

        verifyNoInteractions(ccdService);
        verifyNoInteractions(caseReferenceCsvLoader);
        verifyNoInteractions(systemUserService);
    }

    @Test
    void whenTaskRun_thenMigrationForGlobalSearchCompletes() {
        mockSystemUserToken();
        CaseDetails caseDetails = createCaseData();
        mockSearchCases(caseDetails);
        mockStartEvent(caseDetails);
        SearchResult searchResult = createSearchResult(List.of(caseDetails));
        when(ccdService.esSearchCases(any(CaseType.class), anyString(), anyString())).thenReturn(searchResult);

        globalSearchMigrationTask.run();

        verify(ccdService, times(1)).esSearchCases(any(CaseType.class), anyString(), anyString());
        verifyCcdEvent();
        verifySupplementaryDataUpdate();
    }

    @Test
    void whenTaskRun_AndSupplementaryUpdatesFails_thenMigrationAbortsForTheCase() {
        mockSystemUserToken();
        CaseDetails caseDetails = createCaseData();
        mockSearchCases(caseDetails);
        mockStartEvent(caseDetails);
        SearchResult searchResult = createSearchResult(List.of(caseDetails));
        when(ccdService.esSearchCases(any(CaseType.class), anyString(), anyString())).thenReturn(searchResult);
        doThrow(new RuntimeException("")).when(ccdService).submitSupplementaryDataToCcd(AUTH_TOKEN, REFERENCE);
        globalSearchMigrationTask.run();

        verify(ccdService, times(1)).esSearchCases(any(CaseType.class), anyString(), anyString());
        verifyCcdEvent();
        verifySupplementaryDataUpdate();
    }

    @Test
    void givenNoCasesNeedUpdatingWhenRunThenNoUpdatesExecuted() {
        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);

        SearchResult searchResult = createSearchResult(Collections.emptyList());
        when(ccdService.esSearchCases(any(CaseType.class), anyString(), anyString())).thenReturn(searchResult);

        globalSearchMigrationTask.run();

        verify(ccdService, times(1)).esSearchCases(any(CaseType.class), anyString(), anyString());
        verifyNoMoreInteractions(ccdService);
    }

    private void mockSystemUserToken() {
        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
    }

    private SearchResult createSearchResult(List<CaseDetails> cases) {
        return SearchResult.builder()
            .cases(cases)
            .total(cases.size())
            .build();
    }

    private void mockSearchCases(CaseDetails caseDetails) {
        SearchResult searchResult = SearchResult.builder()
            .cases(List.of(caseDetails))
            .total(1)
            .build();
        when(ccdService.getCaseByCaseId(REFERENCE, CaseType.CONTESTED, AUTH_TOKEN)).thenReturn(searchResult);
    }

    private void mockStartEvent(CaseDetails caseDetails) {
        StartEventResponse startEventResponse = StartEventResponse.builder()
            .caseDetails(caseDetails)
            .build();

        when(ccdService.startEventForCaseWorker(AUTH_TOKEN, REFERENCE, CONTESTED.getCcdType(),
            AMEND_CASE_CRON.getCcdType())).thenReturn(startEventResponse);
    }

    private void verifyCcdEvent() {
        verify(ccdService, times(1)).startEventForCaseWorker(AUTH_TOKEN, REFERENCE, CONTESTED.getCcdType(),
            AMEND_CASE_CRON.getCcdType());
        verify(ccdService).submitEventForCaseWorker(any(StartEventResponse.class), eq(AUTH_TOKEN), eq(REFERENCE),
            eq(CONTESTED.getCcdType()), eq(AMEND_CASE_CRON.getCcdType()),
            eq("DFR-4961"),
            eq("DFR-4961"));
    }

    private void verifySupplementaryDataUpdate() {
        verify(ccdService, times(1)).submitSupplementaryDataToCcd(AUTH_TOKEN, REFERENCE);
    }

    private CaseDetails createCaseData() {
        FinremCaseData caseData = FinremCaseData.builder()
            .ccdCaseType(CaseType.CONTESTED)
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .applicantRepresented(YesOrNo.NO)
                .contestedRespondentRepresented(YesOrNo.NO)
                .build())
            .listForHearingWrapper(ListForHearingWrapper.builder()
                .formC(new CaseDocument())
                .build())
            .build();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .id(Long.parseLong(REFERENCE))
            .caseType(CaseType.CONTESTED)
            .state(State.READY_FOR_HEARING)
            .data(caseData)
            .build();

        return finremCaseDetailsMapper.mapToCaseDetails(caseDetails);
    }
}