package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseLocation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.State;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ListForHearingWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.globalsearch.GlobalSearchService;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.answerVoid;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.AMEND_CASE_CRON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;

@ExtendWith(MockitoExtension.class)
class GlobalSearchMigrationTaskTest {

    @InjectMocks
    private GlobalSearchMigrationTask globalSearchMigrationTask;

    @Mock
    private CcdService ccdService;
    @Mock
    private SystemUserService systemUserService;
    @Mock
    private GlobalSearchService globalSearchService;

    private final FinremCaseDetailsMapper finremCaseDetailsMapper = new FinremCaseDetailsMapper(
            new ObjectMapper().registerModule(new JavaTimeModule()));

    private static final String REFERENCE = "1234567890123456";

    @BeforeEach
    void setup() {

        ReflectionTestUtils.setField(globalSearchMigrationTask, "taskEnabled", true);
        ReflectionTestUtils.setField(globalSearchMigrationTask, "gsQuerySize", 10);
        ReflectionTestUtils.setField(globalSearchMigrationTask, "dryRun", false);
        ReflectionTestUtils.setField(globalSearchMigrationTask, "supplementaryDataRequired", true);
        ReflectionTestUtils.setField(globalSearchMigrationTask, "caseTypeId", CONSENTED.getCcdType());
        ReflectionTestUtils.setField(globalSearchMigrationTask, "finremCaseDetailsMapper", finremCaseDetailsMapper);
    }

    @Test
    void givenTaskNotEnabled_whenTaskRun_thenNoResend() {
        ReflectionTestUtils.setField(globalSearchMigrationTask, "taskEnabled", false);
        globalSearchMigrationTask.run();

        verifyNoInteractions(ccdService);
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
        verify(globalSearchService).setGlobalSearchData(any());
    }

    @Test
    void whenTaskRun_AndCaseDataUpdatesFails_thenMigrationAbortsForTheCase() {
        mockSystemUserToken();
        CaseDetails caseDetails = createCaseData();
        mockSearchCases(caseDetails);
        mockStartEvent(caseDetails);
        SearchResult searchResult = createSearchResult(List.of(caseDetails));
        when(ccdService.esSearchCases(any(CaseType.class), anyString(), anyString())).thenReturn(searchResult);
        doThrow(new RuntimeException("")).when(ccdService).submitEventForCaseWorker(any(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString());
        globalSearchMigrationTask.run();

        verify(ccdService, times(1)).esSearchCases(any(CaseType.class), anyString(), anyString());
        verifyCcdEvent();
        verify(ccdService, never()).submitSupplementaryDataToCcd(AUTH_TOKEN, REFERENCE);
        assertThat(globalSearchMigrationTask.taskFailures).containsEntry("1234567890123456", "Unexpected error");
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
        assertThat(globalSearchMigrationTask.taskFailures).containsEntry("1234567890123456", "Unexpected error");
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

    @Test
    void shouldBuildBoolQueryWithMustAndShouldClauses() {
        String query = globalSearchMigrationTask.getSearchQuery(null);

        JSONObject json = new JSONObject(query);

        JSONObject bool = json.getJSONObject("query")
                        .getJSONObject("bool");

        JSONArray must = bool.getJSONArray("must");
        assertThat(must).hasSize(1);

        JSONArray should = bool.getJSONArray("should");
        assertThat(should).hasSize(3);

        assertThat(bool.getInt("minimum_should_match"))
                .isEqualTo(1);
    }

    @Test
    void shouldContainExpectedStateMustClause() {
        String query = globalSearchMigrationTask.getSearchQuery(null);

        JSONObject json = new JSONObject(query);

        JSONObject mustClause = json.getJSONObject("query")
                .getJSONObject("bool")
                .getJSONArray("must")
                .getJSONObject(0);

        assertThat(mustClause.toString())
                .contains("state.keyword")
                .contains("close")
                .contains("consentOrderMade");
    }

    @Test
    void shouldBuildQueryWithoutSearchAfter() {
        String query = globalSearchMigrationTask.getSearchQuery(null);

        JSONObject json = new JSONObject(query);

        assertThat(json.getInt("size")).isEqualTo(10); // or expected gsQuerySize

        JSONArray source = json.getJSONArray("_source");
        assertThat(source.length()).isEqualTo(1);
        assertThat(source.getString(0)).isEqualTo("reference");

        assertThat(query).contains("state.keyword");
        assertThat(query).contains("supplementary_data.HMCTSServiceId");
        assertThat(query).contains("data.SearchCriteria");
        assertThat(query).contains("data.caseManagementLocation");
        assertThat(query).contains("reference.keyword");
        assertThat(query).doesNotContain("search_after");
    }

    @Test
    void shouldBuildQueryWithSearchAfter() {
        String searchAfter = "1695723578123";

        String query = globalSearchMigrationTask.getSearchQuery(searchAfter);

        JSONObject json = new JSONObject(query);

        JSONArray searchAfterArray = json.getJSONArray("search_after");

        assertThat(searchAfterArray.length()).isEqualTo(1);
        assertThat(searchAfterArray.getString(0)).isEqualTo(searchAfter);

        JSONArray source = json.getJSONArray("_source");
        assertThat(source.getString(0)).isEqualTo("reference");
    }

    @Test
    void shouldIncludeReferenceAsOnlySourceField() {
        String query = globalSearchMigrationTask.getSearchQuery(null);

        JSONObject json = new JSONObject(query);

        JSONArray source = json.getJSONArray("_source");

        assertThat(source.length()).isEqualTo(1);
        assertThat(source.getString(0)).isEqualTo("reference");
    }

    @Test
    void shouldContainRequiredMustNotClauses() {
        String query = globalSearchMigrationTask.getSearchQuery(null);

        assertThat(query)
                .contains("\"state.keyword\"")
                .contains("\"close\"")
                .contains("\"consentOrderMade\"")
                .contains("\"supplementary_data.HMCTSServiceId\"")
                .contains("\"data.SearchCriteria\"")
                .contains("\"data.caseManagementLocation\"");
    }

    @Test
    void givenGlobalSearchServicePopulatesMap_whenExecuteTask_thenFieldsCopiedOntoCaseData() {
        DynamicListElement element = DynamicListElement.builder()
            .code("Financial Remedy").label("Financial Remedy").build();
        DynamicList category = DynamicList.builder().value(element).listItems(List.of(element)).build();
        String caseName = "Smith vs Jones";
        CaseLocation location = CaseLocation.builder().region("1").baseLocation("698118").build();

        doAnswer(answerVoid((FinremCaseData caseData) -> {
            assertThat(caseData.getApplicantLastName()).isEqualTo("Smith");
            caseData.setCaseManagementCategory(category);
            caseData.setCaseNameHmctsInternal(caseName);
            caseData.setCaseManagementLocation(location);
        })).when(globalSearchService).setGlobalSearchData(
            any(FinremCaseData.class));
        FinremCaseDetails finremCaseDetails = createFinremCaseDetails(FinremCaseData.builder()
            .contactDetailsWrapper(ContactDetailsWrapper.builder().applicantLname("Smith").build())
            .build());

        globalSearchMigrationTask.executeTask(finremCaseDetails);

        FinremCaseData caseData = finremCaseDetails.getData();
        assertThat(caseData.getCaseManagementCategory()).isEqualTo(category);
        assertThat(caseData.getCaseNameHmctsInternal()).isEqualTo(caseName);
        assertThat(caseData.getCaseManagementLocation()).isEqualTo(location);
    }

    @Test
    void givenGlobalSearchServiceDoesNotPopulateMap_whenExecuteTask_thenFieldsRemainUnsetAndOtherDataUntouched() {
        FinremCaseDetails finremCaseDetails = createFinremCaseDetails(FinremCaseData.builder()
            .contactDetailsWrapper(ContactDetailsWrapper.builder().applicantLname("Smith").build())
            .build());

        globalSearchMigrationTask.executeTask(finremCaseDetails);

        FinremCaseData caseData = finremCaseDetails.getData();
        assertThat(caseData.getCaseManagementCategory()).isNull();
        assertThat(caseData.getCaseNameHmctsInternal()).isNull();
        assertThat(caseData.getCaseManagementLocation()).isNull();
        assertThat(caseData.getContactDetailsWrapper().getApplicantLname()).isEqualTo("Smith");
    }

    @Test
    @Disabled("Fails due to ClassCastException")
    void givenExistingGlobalSearchFieldsAndServiceDoesNotPopulateMap_whenExecuteTask_thenExistingValuesRetained() {
        DynamicListElement element =
            DynamicListElement.builder().code("Financial Remedy").label("Financial Remedy").build();
        DynamicList category = DynamicList.builder().value(element).listItems(List.of(element)).build();
        String caseName = "Smith vs Jones";
        CaseLocation location = CaseLocation.builder().region("1").baseLocation("698118").build();

        FinremCaseDetails finremCaseDetails = createFinremCaseDetails(FinremCaseData.builder()
            .caseManagementCategory(category)
            .caseNameHmctsInternal(caseName)
            .caseManagementLocation(location)
            .build());

        /*TODO
            Fails because calling finremCaseDetailsMapper.finremCaseDataToMap(caseData) converts everything to a
            LinkedHashMap but we want a DynamicList or CaseLocation.
            Solution: Do not convert to map and use the FinremCaseDetails which has the correct types already.
            Enable test after update.
         */
        globalSearchMigrationTask.executeTask(finremCaseDetails);

        FinremCaseData caseData = finremCaseDetails.getData();
        assertThat(caseData.getCaseManagementCategory()).isEqualTo(category);
        assertThat(caseData.getCaseNameHmctsInternal()).isEqualTo(caseName);
        assertThat(caseData.getCaseManagementLocation()).isEqualTo(location);
    }

    private FinremCaseDetails createFinremCaseDetails(FinremCaseData caseData) {
        return FinremCaseDetails.builder()
            .id(Long.parseLong(REFERENCE))
            .caseType(CONSENTED)
            .data(caseData)
            .build();
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
        when(ccdService.getCaseByCaseId(REFERENCE, CaseType.CONSENTED, AUTH_TOKEN)).thenReturn(searchResult);
    }

    private void mockStartEvent(CaseDetails caseDetails) {
        StartEventResponse startEventResponse = StartEventResponse.builder()
                .caseDetails(caseDetails)
                .build();

        when(ccdService.startEventForCaseWorker(AUTH_TOKEN, REFERENCE, CONSENTED.getCcdType(),
                AMEND_CASE_CRON.getCcdType())).thenReturn(startEventResponse);
    }

    private void verifyCcdEvent() {
        verify(ccdService, times(1)).startEventForCaseWorker(AUTH_TOKEN, REFERENCE, CONSENTED.getCcdType(),
                AMEND_CASE_CRON.getCcdType());
        verify(ccdService).submitEventForCaseWorker(any(StartEventResponse.class), eq(AUTH_TOKEN), eq(REFERENCE),
                eq(CONSENTED.getCcdType()), eq(AMEND_CASE_CRON.getCcdType()),
                eq("DFR-4961"),
                eq("DFR-4961"));
    }

    private void verifySupplementaryDataUpdate() {
        verify(ccdService, times(1)).submitSupplementaryDataToCcd(AUTH_TOKEN, REFERENCE);
    }

    private CaseDetails createCaseData() {
        FinremCaseData caseData = FinremCaseData.builder()
                .ccdCaseType(CaseType.CONSENTED)
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
                .caseType(CONSENTED)
                .state(State.READY_FOR_HEARING)
                .data(caseData)
                .build();

        return finremCaseDetailsMapper.mapToCaseDetails(caseDetails);
    }
}
