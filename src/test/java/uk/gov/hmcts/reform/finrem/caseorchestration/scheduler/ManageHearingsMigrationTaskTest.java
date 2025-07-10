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
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCaseDetailsBuilderFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.ManageHearingsMigrationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReference;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceCsvLoader;

import java.util.List;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
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

    @Mock
    private CcdService ccdService;

    @Mock
    private SystemUserService systemUserService;

    @Mock
    private CaseReferenceCsvLoader caseReferenceCsvLoader;

    private ManageHearingsMigrationTask underTest;

    private ManageHearingsMigrationService manageHearingsMigrationService;

    private final FinremCaseDetailsMapper spyFinremCaesDetailsMapper = spy(new FinremCaseDetailsMapper(
        new ObjectMapper().registerModule(new JavaTimeModule())));

    @BeforeEach
    void setup() {
        manageHearingsMigrationService = mock(ManageHearingsMigrationService.class);

        underTest = new ManageHearingsMigrationTask(caseReferenceCsvLoader, ccdService, systemUserService,
            spyFinremCaesDetailsMapper,  manageHearingsMigrationService);
        ReflectionTestUtils.setField(underTest, "taskEnabled", true);
        ReflectionTestUtils.setField(underTest, "csvFile", ENCRYPTED_CSV_FILENAME);
        ReflectionTestUtils.setField(underTest, "secret", DUMMY_SECRET);
    }

    @Test
    void givenTaskNotEnabledWhenRunThenDoesNothing() {
        ReflectionTestUtils.setField(underTest, "taskEnabled", false);
        underTest.run();

        verifyNoInteractions(ccdService);
        verifyNoInteractions(caseReferenceCsvLoader);
        verifyNoInteractions(systemUserService);
        verifyNoInteractions(manageHearingsMigrationService);
    }

    @Test
    void givenTaskEnabled_whenSingleCaseIsRead_thenShouldPopulateCaseDetails() {
        CaseDetails caseDetailsOne = mock(CaseDetails.class);
        FinremCaseData caseDataOne = mock(FinremCaseData.class);
        FinremCaseDetails finremCaseDetailsOne = FinremCaseDetailsBuilderFactory
            .from(CASE_ID, CONTESTED, caseDataOne).build();

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(caseReferenceCsvLoader.loadCaseReferenceList(ENCRYPTED_CSV_FILENAME, DUMMY_SECRET))
            .thenReturn(List.of(
                // Single case
                CaseReference.builder().caseReference(CASE_ID).build()
            ));
        when(ccdService.getCaseByCaseId(CASE_ID, CaseType.CONTESTED, AUTH_TOKEN)).thenReturn(
            SearchResult.builder().cases(List.of(caseDetailsOne)).total(1).build()
        );
        when(ccdService.startEventForCaseWorker(AUTH_TOKEN, CASE_ID, CONTESTED.getCcdType(),
            AMEND_CASE_CRON.getCcdType())).thenReturn(StartEventResponse.builder()
            .caseDetails(caseDetailsOne)
            .build());
        doReturn(finremCaseDetailsOne).when(spyFinremCaesDetailsMapper).mapToFinremCaseDetails(caseDetailsOne);

        // Act
        underTest.run();

        // Assert
        verify(manageHearingsMigrationService).populateListForHearingWrapper(caseDataOne);
        verify(manageHearingsMigrationService).populateListForInterimHearingWrapper(caseDataOne);
        verify(manageHearingsMigrationService).populateGeneralApplicationWrapper(caseDataOne);
        verify(manageHearingsMigrationService).populateDirectionDetailsCollection(caseDataOne);
    }

    @Test
    void givenTaskEnabled_whenMultipleCasesAreRead_thenShouldPopulateCaseDetails() {
        CaseDetails caseDetailsOne = mock(CaseDetails.class);
        FinremCaseData caseDataOne = mock(FinremCaseData.class);
        FinremCaseDetails finremCaseDetailsOne = FinremCaseDetailsBuilderFactory
            .from(CASE_ID, CONTESTED, caseDataOne).build();

        CaseDetails caseDetailsTwo = mock(CaseDetails.class);
        FinremCaseData caseDataTwo = mock(FinremCaseData.class);
        FinremCaseDetails finremCaseDetailsTwo = FinremCaseDetailsBuilderFactory
            .from(CASE_ID_TWO, CONTESTED, caseDataTwo).build();

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(caseReferenceCsvLoader.loadCaseReferenceList(ENCRYPTED_CSV_FILENAME, DUMMY_SECRET))
            .thenReturn(List.of(
                // Multiple cases
                CaseReference.builder().caseReference(CASE_ID).build(),
                CaseReference.builder().caseReference(CASE_ID_TWO).build()
            ));
        when(ccdService.getCaseByCaseId(CASE_ID, CaseType.CONTESTED, AUTH_TOKEN)).thenReturn(
            SearchResult.builder().cases(List.of(caseDetailsOne)).total(1).build()
        );
        when(ccdService.getCaseByCaseId(CASE_ID_TWO, CaseType.CONTESTED, AUTH_TOKEN)).thenReturn(
            SearchResult.builder().cases(List.of(caseDetailsTwo)).total(1).build()
        );
        when(ccdService.startEventForCaseWorker(AUTH_TOKEN, CASE_ID, CONTESTED.getCcdType(),
            AMEND_CASE_CRON.getCcdType())).thenReturn(StartEventResponse.builder()
            .caseDetails(caseDetailsOne)
            .build());
        when(ccdService.startEventForCaseWorker(AUTH_TOKEN, CASE_ID_TWO, CONTESTED.getCcdType(),
            AMEND_CASE_CRON.getCcdType())).thenReturn(StartEventResponse.builder()
            .caseDetails(caseDetailsTwo)
            .build());
        doReturn(finremCaseDetailsOne).when(spyFinremCaesDetailsMapper).mapToFinremCaseDetails(caseDetailsOne);
        doReturn(finremCaseDetailsTwo).when(spyFinremCaesDetailsMapper).mapToFinremCaseDetails(caseDetailsTwo);

        // Act
        underTest.run();

        // Assert
        verify(manageHearingsMigrationService).populateListForHearingWrapper(caseDataOne);
        verify(manageHearingsMigrationService).populateListForHearingWrapper(caseDataTwo);
        verify(manageHearingsMigrationService).populateListForInterimHearingWrapper(caseDataOne);
        verify(manageHearingsMigrationService).populateListForInterimHearingWrapper(caseDataTwo);
        verify(manageHearingsMigrationService).populateGeneralApplicationWrapper(caseDataOne);
        verify(manageHearingsMigrationService).populateGeneralApplicationWrapper(caseDataTwo);
        verify(manageHearingsMigrationService).populateDirectionDetailsCollection(caseDataOne);
        verify(manageHearingsMigrationService).populateDirectionDetailsCollection(caseDataTwo);
    }
}
