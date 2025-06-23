package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.State;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ConsentOrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SelectedCourtService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReference;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceCsvLoader;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.AMEND_CASE_CRON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@SpringBootTest
@TestPropertySource("classpath:application.properties")
class SetConsentOrderFRCNameTaskTest {
    @Mock
    private CcdService ccdService;

    @Mock
    private SystemUserService systemUserService;

    @Mock
    private CaseReferenceCsvLoader caseReferenceCsvLoader;

    @Mock
    private SelectedCourtService selectedCourtService;
    private final FinremCaseDetailsMapper finremCaseDetailsMapper = new FinremCaseDetailsMapper(
        new ObjectMapper().registerModule(new JavaTimeModule()));

    private SetConsentOrderFRCNameTask setConsentOrderFRCNameTask;
    private static final String REFERENCE = "1234567890123456";

    @BeforeEach
    void setup() {
        setConsentOrderFRCNameTask = new SetConsentOrderFRCNameTask(caseReferenceCsvLoader, ccdService, systemUserService,
            finremCaseDetailsMapper, selectedCourtService);
        ReflectionTestUtils.setField(setConsentOrderFRCNameTask, "taskEnabled", true);
        ReflectionTestUtils.setField(setConsentOrderFRCNameTask, "csvFile", "test.csv");
        ReflectionTestUtils.setField(setConsentOrderFRCNameTask, "secret", "DUMMY_SECRET");
        ReflectionTestUtils.setField(setConsentOrderFRCNameTask, "caseTypeId", CaseType.CONTESTED.getCcdType());
    }

    @Test
    void givenTaskNotEnabled_whenTaskRun_thenNoAssign() {
        ReflectionTestUtils.setField(setConsentOrderFRCNameTask, "taskEnabled", false);
        setConsentOrderFRCNameTask.run();

        verifyNoInteractions(ccdService);
        verifyNoInteractions(caseReferenceCsvLoader);
        verifyNoInteractions(systemUserService);
        verifyNoInteractions(selectedCourtService);
    }

    @Test
    void givenConsentOrderFRCNameIsNull_whenTaskRun_thenAssignWithCorrectValue() {
        FinremCaseDetails nullDetails = FinremCaseDetails.builder()
            .id(Long.parseLong(REFERENCE))
            .caseType(CaseType.CONTESTED)
            .state(State.APPLICATION_ISSUED)
            .data(FinremCaseData.builder()
                .ccdCaseType(CaseType.CONTESTED)
                .build()
            )
            .build();
        CaseDetails caseDetails = finremCaseDetailsMapper.mapToCaseDetails(nullDetails);

        mockLoadCaseReferenceList();
        mockSystemUserToken();
        mockSearchCases(caseDetails);
        mockStartEvent(caseDetails);

        setConsentOrderFRCNameTask.run();

        verifySelectedCourtService();
    }

    @Test
    void givenConsentOrderFRCNameIsNotNull_whenTaskRun_thenAssignWithCorrectValue() {
        CaseDetails caseDetails = createCaseData();
        mockLoadCaseReferenceList();
        mockSystemUserToken();
        mockSearchCases(caseDetails);
        mockStartEvent(caseDetails);

        setConsentOrderFRCNameTask.run();

        verifySelectedCourtService();
        verifyCcdEvent();
    }

    private void verifySelectedCourtService() {
        verify(selectedCourtService)
            .setSelectedCourtDetailsIfPresent(any(FinremCaseData.class));
    }

    private void verifyCcdEvent() {
        verify(ccdService, times(1)).startEventForCaseWorker(AUTH_TOKEN, REFERENCE, CONTESTED.getCcdType(),
            AMEND_CASE_CRON.getCcdType());
        verify(ccdService).submitEventForCaseWorker(any(StartEventResponse.class), eq(AUTH_TOKEN), eq(REFERENCE),
            eq(CONTESTED.getCcdType()), eq(AMEND_CASE_CRON.getCcdType()),
            eq("DFR-3693 CT Fix ConsentOrderFRCName"),
            eq("ConsentOrderFRCName is: Croydon County Court And Family Court"));
    }

    private CaseDetails createCaseData() {
        FinremCaseData caseData = FinremCaseData.builder()
            .ccdCaseType(CaseType.CONTESTED)
            .consentOrderWrapper(ConsentOrderWrapper.builder()
                .consentOrderFrcName("Croydon County Court And Family Court")
                .build())
            .build();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .id(Long.parseLong(REFERENCE))
            .caseType(CaseType.CONTESTED)
            .state(State.APPLICATION_ISSUED)
            .data(caseData)
            .build();

        return finremCaseDetailsMapper.mapToCaseDetails(caseDetails);
    }

    private void mockStartEvent(CaseDetails caseDetails) {
        StartEventResponse startEventResponse = StartEventResponse.builder()
            .caseDetails(caseDetails)
            .build();

        when(ccdService.startEventForCaseWorker(AUTH_TOKEN, REFERENCE, CONTESTED.getCcdType(),
            AMEND_CASE_CRON.getCcdType())).thenReturn(startEventResponse);
    }

    private void mockLoadCaseReferenceList() {
        CaseReference caseReference = new CaseReference();
        caseReference.setCaseReference(REFERENCE);
        when(caseReferenceCsvLoader.loadCaseReferenceList("test.csv", "DUMMY_SECRET"))
            .thenReturn(List.of(caseReference));
    }

    private void mockSystemUserToken() {
        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
    }

    private void mockSearchCases(CaseDetails caseDetails) {
        SearchResult searchResult = SearchResult.builder()
            .cases(List.of(caseDetails))
            .total(1)
            .build();
        when(ccdService.getCaseByCaseId(REFERENCE, CaseType.CONTESTED, AUTH_TOKEN)).thenReturn(searchResult);
    }
}
