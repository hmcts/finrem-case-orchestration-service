package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCaseDetailsBuilderFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
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

import java.util.List;

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
        CaseDetails caseDetailsOne = mock(CaseDetails.class);
        FinremCaseData caseDataOne = FinremCaseData.builder().build();
        FinremCaseDetails finremCaseDetailsOne = FinremCaseDetailsBuilderFactory
            .from(CASE_ID, CONTESTED, caseDataOne).build();

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);

        when(caseReferenceCsvLoader.loadCaseReferenceList(ENCRYPTED_CSV_FILENAME, DUMMY_SECRET))
            .thenReturn(List.of(
                // Single case
                CaseReference.builder().caseReference(CASE_ID).build()
            ));
        stubCaseDetails(Arguments.of(CASE_ID, caseDetailsOne, finremCaseDetailsOne));

        // Act
        underTest.run();

        // Assert
        verify(spyManageHearingsMigrationService).runManageHearingMigration(caseDataOne, MH_MIGRATION_VERSION);
    }

    @Test
    void givenTaskEnabled_whenMultipleCasesAreRead_thenShouldPopulateCaseDetails() {
        CaseDetails caseDetailsOne = mock(CaseDetails.class);
        FinremCaseData caseDataOne = spy(FinremCaseData.builder().build());
        FinremCaseDetails finremCaseDetailsOne = FinremCaseDetailsBuilderFactory
            .from(CASE_ID, CONTESTED, caseDataOne).build();

        CaseDetails caseDetailsTwo = mock(CaseDetails.class);
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
            Arguments.of(CASE_ID, caseDetailsOne, finremCaseDetailsOne),
            Arguments.of(CASE_ID_TWO, caseDetailsTwo, finremCaseDetailsTwo)
        );

        // Act
        underTest.run();

        // Assert
        verify(spyManageHearingsMigrationService).runManageHearingMigration(caseDataOne, MH_MIGRATION_VERSION);
        verify(spyManageHearingsMigrationService).runManageHearingMigration(caseDataTwo, MH_MIGRATION_VERSION);
    }

    @Test
    void givenTaskEnabled_whenMultipleCasesAreRead_thenShouldPopulateNonMigratedCaseDetails() {
        CaseDetails caseDetailsOne = mock(CaseDetails.class);
        FinremCaseData caseDataOne = spy(FinremCaseData.builder().build());
        FinremCaseDetails finremCaseDetailsOne = FinremCaseDetailsBuilderFactory
            .from(CASE_ID, CONTESTED, caseDataOne).build();

        CaseDetails caseDetailsTwo = mock(CaseDetails.class);
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
            Arguments.of(CASE_ID, caseDetailsOne, finremCaseDetailsOne, false),
            Arguments.of(CASE_ID_TWO, caseDetailsTwo, finremCaseDetailsTwo, true) // 2nd case data should be skipped
        );

        // Act
        underTest.run();

        // Assert
        verify(spyManageHearingsMigrationService).runManageHearingMigration(caseDataOne, MH_MIGRATION_VERSION);
        verify(spyManageHearingsMigrationService, never()).runManageHearingMigration(caseDataTwo, MH_MIGRATION_VERSION);
    }

    private void stubCaseDetails(Arguments... caseDetailsPairs) {
        for (Arguments args : caseDetailsPairs) {
            Object[] values = args.get();
            String caseId = (String) values[0];
            CaseDetails caseDetails = (CaseDetails) values[1];
            FinremCaseDetails finremCaseDetails = (FinremCaseDetails) values[2];
            boolean wasMigrated = values.length > 3 && Boolean.TRUE.equals(values[3]);

            when(ccdService.getCaseByCaseId(caseId, CaseType.CONTESTED, AUTH_TOKEN))
                .thenReturn(SearchResult.builder()
                    .cases(List.of(caseDetails))
                    .total(1)
                    .build());

            when(ccdService.startEventForCaseWorker(
                AUTH_TOKEN, caseId, CONTESTED.getCcdType(), AMEND_CASE_CRON.getCcdType()))
                .thenReturn(StartEventResponse.builder()
                    .caseDetails(caseDetails)
                    .build());

            doReturn(finremCaseDetails).when(spyFinremCaseDetailsMapper).mapToFinremCaseDetails(caseDetails);
            doReturn(wasMigrated).when(spyManageHearingsMigrationService).wasMigrated(finremCaseDetails.getData());
        }
    }
}
