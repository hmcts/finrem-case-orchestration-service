package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCaseDetailsBuilderFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.ManageHearingActionService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.ManageHearingsMigrationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.migration.DirectionDetailsCollectionPopulator;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.migration.GeneralApplicationWrapperPopulator;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.migration.ListForHearingWrapperPopulator;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.migration.ListForInterimHearingWrapperPopulator;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReference;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceCsvLoader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID_TWO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.AMEND_CASE_CRON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@ExtendWith(MockitoExtension.class)
class ManageHearingsMigrationTaskTest {

    private static final String ENCRYPTED_CSV_FILENAME = "updateConsentOrderFRCName-encrypted.csv";

    private static final String DUMMY_SECRET = "dummySecret";

    private static final String MH_MIGRATION_VERSION = "1";

    @Mock
    private CcdService ccdService;

    @Mock
    private SystemUserService systemUserService;

    @Mock
    private CaseReferenceCsvLoader caseReferenceCsvLoader;

    private ManageHearingsMigrationTask underTest;

    private ManageHearingsMigrationService spyManageHearingsMigrationService;

    private final FinremCaseDetailsMapper spyFinremCaseDetailsMapper = spy(new FinremCaseDetailsMapper(
        new ObjectMapper().registerModule(new JavaTimeModule())));

    @Mock
    private ListForHearingWrapperPopulator listForHearingWrapperPopulator;

    @Mock
    private ListForInterimHearingWrapperPopulator listForInterimHearingWrapperPopulator;

    @Mock
    private GeneralApplicationWrapperPopulator generalApplicationWrapperPopulator;

    @Mock
    private DirectionDetailsCollectionPopulator directionDetailsCollectionPopulator;

    @Mock
    private ManageHearingActionService manageHearingActionService;

    @BeforeEach
    void setup() {
        spyManageHearingsMigrationService = spy(new ManageHearingsMigrationService(
            listForHearingWrapperPopulator, listForInterimHearingWrapperPopulator, generalApplicationWrapperPopulator,
            directionDetailsCollectionPopulator, manageHearingActionService
        ));

        underTest = new ManageHearingsMigrationTask(caseReferenceCsvLoader, ccdService, systemUserService,
            spyFinremCaseDetailsMapper, spyManageHearingsMigrationService);
        ReflectionTestUtils.setField(underTest, "taskEnabled", true);
        ReflectionTestUtils.setField(underTest, "rollback", false);
        ReflectionTestUtils.setField(underTest, "csvFile", ENCRYPTED_CSV_FILENAME);
        ReflectionTestUtils.setField(underTest, "secret", DUMMY_SECRET);
        ReflectionTestUtils.setField(underTest, "mhMigrationVersion", MH_MIGRATION_VERSION);
    }

    @Test
    void givenTaskNotEnabledWhenRunThenDoesNothing() {
        ReflectionTestUtils.setField(underTest, "taskEnabled", false);
        underTest.run();

        verifyNoInteractions(ccdService);
        verifyNoInteractions(caseReferenceCsvLoader);
        verifyNoInteractions(systemUserService);
        verifyNoInteractions(spyManageHearingsMigrationService);
    }

    @Test
    void givenTaskEnabled_whenSingleCaseIsRead_thenShouldPopulateCaseDetails() {
        CaseDetails loadedCaseDetailsOne = mock(CaseDetails.class);
        CaseDetails updatedCaseDetailsOne = mock(CaseDetails.class);
        FinremCaseData caseDataOne = FinremCaseData.builder().build();
        FinremCaseDetails finremCaseDetailsOne = FinremCaseDetailsBuilderFactory
            .from(CASE_ID, CONTESTED, caseDataOne).build();

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);

        when(caseReferenceCsvLoader.loadCaseReferenceList(ENCRYPTED_CSV_FILENAME, DUMMY_SECRET))
            .thenReturn(List.of(
                // Single case
                CaseReference.builder().caseReference(CASE_ID).build()
            ));
        stubCaseDetails(Arguments.of(CASE_ID, loadedCaseDetailsOne, finremCaseDetailsOne, updatedCaseDetailsOne));

        // Act
        underTest.run();

        // Assert
        verify(spyManageHearingsMigrationService).runManageHearingMigration(caseDataOne, MH_MIGRATION_VERSION);
        verify(spyManageHearingsMigrationService, never()).revertManageHearingMigration(caseDataOne);
    }

    @Test
    void givenTaskEnabled_whenMultipleCasesAreRead_thenShouldPopulateCaseDetails() {
        CaseDetails loadedCaseDetailsOne = mock(CaseDetails.class);
        CaseDetails updatedCaseDetailsOne = mock(CaseDetails.class);
        FinremCaseData caseDataOne = spy(FinremCaseData.builder().build());
        FinremCaseDetails finremCaseDetailsOne = FinremCaseDetailsBuilderFactory
            .from(CASE_ID, CONTESTED, caseDataOne).build();

        CaseDetails loadedCaseDetailsTwo = mock(CaseDetails.class);
        FinremCaseData caseDataTwo = spy(FinremCaseData.builder().build()); // spy is used to differentiate it.
        FinremCaseDetails finremCaseDetailsTwo = FinremCaseDetailsBuilderFactory
            .from(CASE_ID_TWO, CONTESTED, caseDataTwo).build();

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(caseReferenceCsvLoader.loadCaseReferenceList(ENCRYPTED_CSV_FILENAME, DUMMY_SECRET))
            .thenReturn(List.of(
                // Multiple cases
                CaseReference.builder().caseReference(CASE_ID).build(),
                CaseReference.builder().caseReference(CASE_ID_TWO).build()
            ));
        stubCaseDetails(
            Arguments.of(CASE_ID, loadedCaseDetailsOne, finremCaseDetailsOne, updatedCaseDetailsOne),
            Arguments.of(CASE_ID_TWO, loadedCaseDetailsTwo, finremCaseDetailsTwo, updatedCaseDetailsOne)
        );

        // Act
        underTest.run();

        // Assert
        verify(spyManageHearingsMigrationService).runManageHearingMigration(caseDataOne, MH_MIGRATION_VERSION);
        verify(spyManageHearingsMigrationService).runManageHearingMigration(caseDataTwo, MH_MIGRATION_VERSION);
        verify(spyManageHearingsMigrationService, never()).revertManageHearingMigration(caseDataOne);
        verify(spyManageHearingsMigrationService, never()).revertManageHearingMigration(caseDataTwo);
    }

    @Test
    void givenTaskEnabled_whenMultipleCasesAreRead_thenShouldPopulateNonMigratedCaseDetails() {
        CaseDetails loadedCaseDetailsOne = mock(CaseDetails.class);
        CaseDetails updatedCaseDetailsOne = mock(CaseDetails.class);
        FinremCaseData caseDataOne = spy(FinremCaseData.builder().build());
        FinremCaseDetails finremCaseDetailsOne = FinremCaseDetailsBuilderFactory
            .from(CASE_ID, CONTESTED, caseDataOne).build();

        CaseDetails loadedCaseDetailsTwo = mock(CaseDetails.class);
        CaseDetails updatedCaseDetailsTwo = mock(CaseDetails.class);
        FinremCaseData caseDataTwo = spy(FinremCaseData.builder().build()); // spy is used to differentiate it.
        FinremCaseDetails finremCaseDetailsTwo = FinremCaseDetailsBuilderFactory
            .from(CASE_ID_TWO, CONTESTED, caseDataTwo).build();

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(caseReferenceCsvLoader.loadCaseReferenceList(ENCRYPTED_CSV_FILENAME, DUMMY_SECRET))
            .thenReturn(List.of(
                CaseReference.builder().caseReference(CASE_ID).build(),
                CaseReference.builder().caseReference(CASE_ID_TWO).build()
            ));
        stubCaseDetails(
            Arguments.of(CASE_ID, loadedCaseDetailsOne, finremCaseDetailsOne, updatedCaseDetailsOne, false),
            Arguments.of(CASE_ID_TWO, loadedCaseDetailsTwo, finremCaseDetailsTwo, updatedCaseDetailsTwo, true) // 2nd case data should be skipped
        );

        // Act
        underTest.run();

        // Assert
        verify(spyManageHearingsMigrationService).runManageHearingMigration(caseDataOne, MH_MIGRATION_VERSION);
        verify(spyManageHearingsMigrationService, never()).runManageHearingMigration(caseDataTwo, MH_MIGRATION_VERSION);
        verify(spyManageHearingsMigrationService, never()).revertManageHearingMigration(caseDataOne);
        verify(spyManageHearingsMigrationService, never()).revertManageHearingMigration(caseDataTwo);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void givenTaskEnabledAndRollbackIsSetToTrue_whenMultipleCasesAreRead_thenShouldRevertMigratedCaseDetails(
        boolean updatedMapHavingNull
    ) {
        ReflectionTestUtils.setField(underTest, "rollback", true);
        CaseDetails loadedCaseDetailsOne = mock(CaseDetails.class);
        CaseDetails updatedCaseDetailsOne = mock(CaseDetails.class);
        FinremCaseData caseDataOne = spy(FinremCaseData.builder().build());
        FinremCaseDetails finremCaseDetailsOne = FinremCaseDetailsBuilderFactory
            .from(CASE_ID, CONTESTED, caseDataOne).build();

        CaseDetails loadedCaseDetailsTwo = CaseDetails.builder().data(new HashMap<>()).build();
        CaseDetails updatedCaseDetailsTwo = CaseDetails.builder().data(new HashMap<>()).build();

        // simulate data has been deleted
        loadedCaseDetailsTwo.getData().put("valueRetained", "1");
        loadedCaseDetailsTwo.getData().put("valueModified", "X");
        loadedCaseDetailsTwo.getData().put("hearingTabItems", List.of(1L));
        loadedCaseDetailsTwo.getData().put("address", Map.of("street", "High St", "city", "London"));
        loadedCaseDetailsTwo.getData().put("mhMigrationVersion", "1");
        updatedCaseDetailsTwo.getData().put("valueRetained", "1");
        updatedCaseDetailsTwo.getData().put("valueModified", "Z");
        updatedCaseDetailsTwo.getData().put("hearingTabItems", List.of(2L));
        updatedCaseDetailsTwo.getData().put("address", Map.of("city", "Manchester"));
        updatedCaseDetailsTwo.getData().put("valueInserted", "NEW FIELD");
        if (updatedMapHavingNull) {
            updatedCaseDetailsTwo.getData().put("mhMigrationVersion", null);
        }

        FinremCaseData caseDataTwo = spy(FinremCaseData.builder().build()); // spy is used to differentiate it.
        FinremCaseDetails finremCaseDetailsTwo = FinremCaseDetailsBuilderFactory
            .from(CASE_ID_TWO, CONTESTED, caseDataTwo).build();

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(caseReferenceCsvLoader.loadCaseReferenceList(ENCRYPTED_CSV_FILENAME, DUMMY_SECRET))
            .thenReturn(List.of(
                CaseReference.builder().caseReference(CASE_ID).build(),
                CaseReference.builder().caseReference(CASE_ID_TWO).build()
            ));
        stubCaseDetails(
            Arguments.of(CASE_ID, loadedCaseDetailsOne, finremCaseDetailsOne, updatedCaseDetailsOne, false),
            Arguments.of(CASE_ID_TWO, loadedCaseDetailsTwo, finremCaseDetailsTwo, updatedCaseDetailsTwo, true)
        );

        // Act
        underTest.run();

        // Assert
        verify(spyManageHearingsMigrationService).revertManageHearingMigration(caseDataTwo);
        verify(spyManageHearingsMigrationService, never()).revertManageHearingMigration(caseDataOne);
        verify(spyManageHearingsMigrationService, never()).runManageHearingMigration(caseDataOne, MH_MIGRATION_VERSION);
        verify(spyManageHearingsMigrationService, never()).runManageHearingMigration(caseDataTwo, MH_MIGRATION_VERSION);

        ArgumentCaptor<StartEventResponse> startEventCaptor = ArgumentCaptor.forClass(StartEventResponse.class);
        verify(ccdService)
            .submitEventForCaseWorker(startEventCaptor.capture(),
                eq(AUTH_TOKEN), eq(CASE_ID), eq(CONTESTED.getCcdType()), eq(AMEND_CASE_CRON.getCcdType()),
                eq("Manage Hearings migration"),
                eq("Manage Hearings migration"));
        ArgumentCaptor<StartEventResponse> startEventCaptorTwo = ArgumentCaptor.forClass(StartEventResponse.class);
        verify(ccdService)
            .submitEventForCaseWorker(startEventCaptorTwo.capture(),
                eq(AUTH_TOKEN), eq(CASE_ID_TWO), eq(CONTESTED.getCcdType()), eq(AMEND_CASE_CRON.getCcdType()),
                eq("Manage Hearings migration"),
                eq("Manage Hearings migration"));

        // verify mhMigrationVersion should be set to null
        Map<String, Object> actualData = startEventCaptorTwo.getValue().getCaseDetails().getData();

        assertThat(actualData.get("mhMigrationVersion")).isNull();
        assertThat(actualData.get("valueRetained")).isEqualTo("1");
        assertThat(actualData.get("valueModified")).isEqualTo("Z");
        assertThat(actualData.get("hearingTabItems")).isEqualTo(List.of(2L));
        assertThat(actualData.get("valueInserted")).isEqualTo("NEW FIELD");

        assertThat(actualData.get("address")).isInstanceOf(Map.class);
        Map<String, Object> address = (Map<String, Object>) actualData.get("address");
        assertThat(address.get("city")).isEqualTo("Manchester");
        assertThat(address.get("street")).isNull();
    }

    private void stubCaseDetails(Arguments... caseDetailsPairs) {
        for (Arguments args : caseDetailsPairs) {
            Object[] values = args.get();
            String caseId = (String) values[0];
            CaseDetails loadedCaseDetails = (CaseDetails) values[1];
            FinremCaseDetails finremCaseDetails = (FinremCaseDetails) values[2];
            CaseDetails updatedCaseDetails = (CaseDetails) values[3];
            boolean wasMigrated = values.length > 4 && Boolean.TRUE.equals(values[4]);

            when(ccdService.getCaseByCaseId(caseId, CONTESTED, AUTH_TOKEN))
                .thenReturn(SearchResult.builder()
                    .cases(List.of(loadedCaseDetails))
                    .total(1)
                    .build());

            when(ccdService.startEventForCaseWorker(AUTH_TOKEN, caseId, CONTESTED.getCcdType(), AMEND_CASE_CRON.getCcdType()))
                .thenReturn(StartEventResponse.builder()
                    .caseDetails(loadedCaseDetails)
                    .build());

            doReturn(finremCaseDetails).when(spyFinremCaseDetailsMapper).mapToFinremCaseDetails(loadedCaseDetails);
            doReturn(updatedCaseDetails).when(spyFinremCaseDetailsMapper).mapToCaseDetails(finremCaseDetails);
            doReturn(wasMigrated).when(spyManageHearingsMigrationService).wasMigrated(finremCaseDetails.getData());
        }
    }
}
