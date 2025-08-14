package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.MhMigrationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.migration.DirectionDetailsCollectionPopulator;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.migration.GeneralApplicationWrapperPopulator;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.migration.HearingDirectionDetailsCollectionPopulator;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.migration.ListForHearingWrapperPopulator;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.migration.ListForInterimHearingWrapperPopulator;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogs;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;

@ExtendWith(MockitoExtension.class)
class ManageHearingsMigrationServiceTest {

    @TestLogs
    private final TestLogger logs = new TestLogger(ManageHearingsMigrationService.class);

    @Mock
    private ListForHearingWrapperPopulator listForHearingWrapperPopulator;

    @Mock
    private ListForInterimHearingWrapperPopulator listForInterimHearingWrapperPopulator;

    @Mock
    private GeneralApplicationWrapperPopulator generalApplicationWrapperPopulator;

    @Mock
    private DirectionDetailsCollectionPopulator directionDetailsCollectionPopulator;

    @Mock
    private HearingDirectionDetailsCollectionPopulator hearingDetailsCollectionPopulator;

    @Mock
    private ManageHearingActionService manageHearingActionService;

    @Spy
    @InjectMocks
    private ManageHearingsMigrationService underTest;

    @Test
    void shouldMarkCaseDataAsMigratedWithGivenVersion() {
        // Arrange
        String mhMigrationVersion = "1";
        FinremCaseData caseData = FinremCaseData.builder().build();

        // Act
        underTest.markCaseDataMigrated(caseData, mhMigrationVersion);

        // Assert
        assertEquals(mhMigrationVersion, caseData.getMhMigrationWrapper().getMhMigrationVersion());
    }

    @Test
    void shouldOverwriteOldVersionWithGivenVersion() {
        // Arrange
        String mhMigrationVersion = "1";
        FinremCaseData caseData = FinremCaseData.builder()
            .mhMigrationWrapper(MhMigrationWrapper.builder().mhMigrationVersion("0").build())
            .build();

        // Act
        underTest.markCaseDataMigrated(caseData, mhMigrationVersion);

        // Assert
        assertEquals(mhMigrationVersion, caseData.getMhMigrationWrapper().getMhMigrationVersion());
    }

    @Test
    void shouldSkipListForHearingPopulationWhenShouldPopulateReturnsFalse() {
        FinremCaseData caseData = FinremCaseData.builder().ccdCaseId(CASE_ID).build();

        when(listForHearingWrapperPopulator.shouldPopulate(caseData)).thenReturn(false);

        // Act
        underTest.populateListForHearingWrapper(caseData);

        // Assert
        assertThat(logs.getWarns()).contains(CASE_ID + " - List for Hearing migration skipped.");
        verify(listForHearingWrapperPopulator, never()).populate(any(FinremCaseData.class));
    }

    @Test
    void shouldPopulateListForHearingWhenShouldPopulateReturnsTrue() {
        FinremCaseData caseData = mock(FinremCaseData.class);

        when(listForHearingWrapperPopulator.shouldPopulate(caseData)).thenReturn(true);

        underTest.populateListForHearingWrapper(caseData);

        verify(listForHearingWrapperPopulator).populate(caseData);
    }

    @Test
    void shouldSkipListForInterimHearingPopulationWhenShouldPopulateReturnsFalse() {
        FinremCaseData caseData = FinremCaseData.builder().ccdCaseId(CASE_ID).build();

        when(listForInterimHearingWrapperPopulator.shouldPopulate(caseData)).thenReturn(false);

        // Act
        underTest.populateListForInterimHearingWrapper(caseData);

        // Assert
        assertThat(logs.getWarns()).contains(CASE_ID + " - List for Interim Hearing migration skipped.");
        verify(listForInterimHearingWrapperPopulator, never()).populate(any(FinremCaseData.class));
    }

    @Test
    void shouldPopulateListForInterimHearingWhenShouldPopulateReturnsTrue() {
        FinremCaseData caseData = mock(FinremCaseData.class);

        when(listForInterimHearingWrapperPopulator.shouldPopulate(caseData)).thenReturn(true);

        underTest.populateListForInterimHearingWrapper(caseData);

        verify(listForInterimHearingWrapperPopulator).populate(caseData);
    }

    @Test
    void shouldSkipListForGeneralApplicationWrapperPopulationWhenShouldPopulateReturnsFalse() {
        FinremCaseData caseData = FinremCaseData.builder().ccdCaseId(CASE_ID).build();

        when(generalApplicationWrapperPopulator.shouldPopulate(caseData)).thenReturn(false);

        // Act
        underTest.populateGeneralApplicationWrapper(caseData);

        // Assert
        assertThat(logs.getWarns()).contains(CASE_ID + " - Existing hearings created with General Application Directions migration skipped.");
        verify(generalApplicationWrapperPopulator, never()).populate(any(FinremCaseData.class));
    }

    @Test
    void shouldPopulateGeneralApplicationWrapperWhenShouldPopulateReturnsTrue() {
        FinremCaseData caseData = mock(FinremCaseData.class);

        when(generalApplicationWrapperPopulator.shouldPopulate(caseData)).thenReturn(true);

        underTest.populateGeneralApplicationWrapper(caseData);

        verify(generalApplicationWrapperPopulator).populate(caseData);
    }

    @Test
    void shouldSkipDirectionDetailsCollectionPopulationWhenShouldPopulateReturnsFalse() {
        FinremCaseData caseData = FinremCaseData.builder().ccdCaseId(CASE_ID).build();

        when(directionDetailsCollectionPopulator.shouldPopulate(caseData)).thenReturn(false);

        // Act
        underTest.populateDirectionDetailsCollection(caseData);

        // Assert
        assertThat(logs.getWarns()).contains(CASE_ID + " - Existing hearings created with Process Order migration skipped.");
        verify(directionDetailsCollectionPopulator, never()).populate(any(FinremCaseData.class));
    }

    @Test
    void shouldPopulateDirectionDetailsCollectionWhenShouldPopulateReturnsTrue() {
        FinremCaseData caseData = mock(FinremCaseData.class);

        when(directionDetailsCollectionPopulator.shouldPopulate(caseData)).thenReturn(true);

        underTest.populateDirectionDetailsCollection(caseData);

        verify(directionDetailsCollectionPopulator).populate(caseData);
    }

    @Test
    void shouldSkipHearingDetailsCollectionPopulationWhenShouldPopulateReturnsFalse() {
        FinremCaseData caseData = FinremCaseData.builder().ccdCaseId(CASE_ID).build();

        when(hearingDetailsCollectionPopulator.shouldPopulate(caseData)).thenReturn(false);

        // Act
        underTest.populateHearingDirectionDetailsCollection(caseData);

        // Assert
        assertThat(logs.getWarns()).contains(CASE_ID + " - Existing hearings created with Upload Approved Order migration skipped.");
        verify(hearingDetailsCollectionPopulator, never()).populate(any(FinremCaseData.class));
    }

    @Test
    void shouldPopulateHearingDetailsCollectionWhenShouldPopulateReturnsTrue() {
        FinremCaseData caseData = mock(FinremCaseData.class);

        when(hearingDetailsCollectionPopulator.shouldPopulate(caseData)).thenReturn(true);

        underTest.populateHearingDirectionDetailsCollection(caseData);

        verify(hearingDetailsCollectionPopulator).populate(caseData);
    }


    @Test
    void shouldRunManageHearingMigrationSuccessfully() {
        // Given
        FinremCaseData caseData = mock(FinremCaseData.class);
        String migrationVersion = "1";

        // Spy only the method under test so we can verify internal calls
        doNothing().when(underTest).populateListForHearingWrapper(caseData);
        doNothing().when(underTest).populateListForInterimHearingWrapper(caseData);
        doNothing().when(underTest).populateGeneralApplicationWrapper(caseData);
        doNothing().when(underTest).populateDirectionDetailsCollection(caseData);
        doNothing().when(underTest).populateHearingDirectionDetailsCollection(caseData);
        doNothing().when(underTest).markCaseDataMigrated(caseData, migrationVersion);

        // When
        underTest.runManageHearingMigration(caseData, migrationVersion);

        // Then
        verify(underTest).populateListForHearingWrapper(caseData);
        verify(underTest).populateListForInterimHearingWrapper(caseData);
        verify(underTest).populateGeneralApplicationWrapper(caseData);
        verify(underTest).populateDirectionDetailsCollection(caseData);
        verify(underTest).populateHearingDirectionDetailsCollection(caseData);
        verify(underTest).markCaseDataMigrated(caseData, migrationVersion);
        verify(manageHearingActionService).updateTabData(caseData);
    }

    @Test
    void shouldRevertManageHearingMigrationMigrationAndRemoveOnlyMigratedHearings() {
        // Given
        ManageHearingsCollectionItem hearingWithYes = ManageHearingsCollectionItem.builder()
            .value(Hearing.builder().wasMigrated(YesOrNo.YES).build())
            .build();

        ManageHearingsCollectionItem hearingWithNo = ManageHearingsCollectionItem.builder()
            .value(Hearing.builder().wasMigrated(YesOrNo.NO).build())
            .build();

        ManageHearingsCollectionItem hearingWithNull = ManageHearingsCollectionItem.builder()
            .value(Hearing.builder().wasMigrated(null).build())
            .build();

        ManageHearingsWrapper wrapper = ManageHearingsWrapper.builder()
            .hearings(List.of(hearingWithYes, hearingWithNo, hearingWithNull))
            .build();

        MhMigrationWrapper mhMigrationWrapper = mock(MhMigrationWrapper.class);

        FinremCaseData caseData = FinremCaseData.builder()
            .mhMigrationWrapper(mhMigrationWrapper)
            .manageHearingsWrapper(wrapper)
            .build();

        // When
        underTest.revertManageHearingMigration(caseData);

        // Then
        verify(mhMigrationWrapper).clearAll();
        assertThat(caseData.getManageHearingsWrapper().getHearings()).containsExactly(hearingWithNo, hearingWithNull);
        verify(manageHearingActionService).updateTabData(caseData);
    }

    @Test
    void shouldRevertManageHearingMigrationMigrationAndRemoveAllHearings() {
        // Given
        ManageHearingsCollectionItem hearingWithYes1 = ManageHearingsCollectionItem.builder()
            .value(Hearing.builder().wasMigrated(YesOrNo.YES).build())
            .build();

        ManageHearingsCollectionItem hearingWithYes2 = ManageHearingsCollectionItem.builder()
            .value(Hearing.builder().wasMigrated(YesOrNo.YES).build())
            .build();

        ManageHearingsWrapper wrapper = ManageHearingsWrapper.builder()
            .hearings(List.of(hearingWithYes1, hearingWithYes2))
            .build();

        MhMigrationWrapper mhMigrationWrapper = mock(MhMigrationWrapper.class);

        FinremCaseData caseData = FinremCaseData.builder()
            .mhMigrationWrapper(mhMigrationWrapper)
            .manageHearingsWrapper(wrapper)
            .build();

        // When
        underTest.revertManageHearingMigration(caseData);

        // Then
        verify(mhMigrationWrapper).clearAll();
        assertThat(caseData.getManageHearingsWrapper().getHearings()).isNull();
        verify(manageHearingActionService).updateTabData(caseData);
    }
}
