package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.MhMigrationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.migration.ListForHearingWrapperPopulator;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;

@ExtendWith(MockitoExtension.class)
class ManageHearingsMigrationServiceTest {

    @TestLogs
    private final TestLogger logs = new TestLogger(ManageHearingsMigrationService.class);

    @Mock
    private ListForHearingWrapperPopulator listForHearingWrapperPopulator;

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
    void shouldSkipPopulationWhenShouldPopulateReturnsFalse() {
        FinremCaseData caseData = spy(FinremCaseData.class);
        caseData.setCcdCaseId(CASE_ID);

        when(listForHearingWrapperPopulator.shouldPopulate(caseData)).thenReturn(false);

        // Act
        underTest.populateListForHearingWrapper(caseData);

        // Assert
        assertThat(logs.getWarns()).contains(CASE_ID + " - List for Hearing migration skipped.");
        verify(listForHearingWrapperPopulator, never()).populate(any(FinremCaseData.class));
    }

    @Test
    void shouldPopulateWhenShouldPopulateReturnsTrue() {
        FinremCaseData caseData = mock(FinremCaseData.class);

        when(listForHearingWrapperPopulator.shouldPopulate(caseData)).thenReturn(true);

        underTest.populateListForHearingWrapper(caseData);

        verify(listForHearingWrapperPopulator).populate(caseData);
    }
}
