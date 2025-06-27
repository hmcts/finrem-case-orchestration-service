package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.tabdata.managehearings.HearingTabDataMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Court;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingTypeDirection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs.HearingTabCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs.HearingTabItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.HearingRegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ListForHearingWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.MhMigrationWrapper;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManageHearingsMigrationServiceTest {

    @Mock
    private HearingTabDataMapper hearingTabDataMapper;

    @InjectMocks
    private ManageHearingsMigrationService underTest;

    @Test
    void givenNonMigratedData_thenMarkProvidedVersion() {
        // Arrange
        FinremCaseData caseData = FinremCaseData.builder().build();
        String expectedVersion = "1";

        // Act
        underTest.markCaseDataMigrated(caseData, expectedVersion);

        // Assert
        assertEquals(expectedVersion, caseData.getMhMigrationWrapper().getMhMigrationVersion());
    }

    @Test
    void givenExistingMigrationVersion_thenOverwriteProvidedVersion() {
        // Arrange
        FinremCaseData caseData = FinremCaseData.builder()
            .mhMigrationWrapper(MhMigrationWrapper.builder().mhMigrationVersion("0").build())
            .build();
        String expectedVersion = "1";

        // Act
        underTest.markCaseDataMigrated(caseData, expectedVersion);

        // Assert
        assertEquals(expectedVersion, caseData.getMhMigrationWrapper().getMhMigrationVersion());
    }

    @Test
    void givenMigratedCaseDataWithListForHearingDataShouldDoNothing() {
        // Arrange
        MhMigrationWrapper mhMigrationWrapper = MhMigrationWrapper.builder()
            .isListForHearingsMigrated(YesOrNo.YES)
            .mhMigrationVersion("1")
            .build();

        FinremCaseData caseData = FinremCaseData.builder()
            .mhMigrationWrapper(mhMigrationWrapper)
            .build();

        // Act
        underTest.populateListForHearingWrapper(caseData);

        // Assert
        assertEquals(YesOrNo.YES, caseData.getMhMigrationWrapper().getIsListForHearingsMigrated());
        assertThat(caseData.getManageHearingsWrapper().getHearingTabItems()).isNull();
    }

    @Test
    void givenNonMigratedCaseDataWithListForHearingDataShouldPopulateToHearingTabItem() {
        // Arrange
        LocalDate hearingDate = LocalDate.of(2025, 7, 1);
        String hearingTime = "10:00";
        String additionalInfo = "Some notes";

        String expectedCourtName = "London FR Court";
        String expectedDateTime = "01 Jul 2025 10:00";
        String expectedAdditionalInfo = "Formatted notes";

        HearingRegionWrapper hearingRegionWrapper = mock(HearingRegionWrapper.class);
        Court court = mock(Court.class);

        when(hearingRegionWrapper.toCourt()).thenReturn(court);
        when(hearingTabDataMapper.getCourtName(court)).thenReturn(expectedCourtName);
        when(hearingTabDataMapper.getFormattedDateTime(hearingDate, hearingTime)).thenReturn(expectedDateTime);
        when(hearingTabDataMapper.getAdditionalInformation(additionalInfo)).thenReturn(expectedAdditionalInfo);

        ListForHearingWrapper listForHearingWrapper = ListForHearingWrapper.builder()
            .hearingType(HearingTypeDirection.FH)
            .hearingDate(hearingDate)
            .hearingTime(hearingTime)
            .timeEstimate("45 minutes")
            .additionalInformationAboutHearing(additionalInfo)
            .hearingRegionWrapper(hearingRegionWrapper)
            .build();

        MhMigrationWrapper mhMigrationWrapper = MhMigrationWrapper.builder()
            .isListForHearingsMigrated(YesOrNo.NO)
            .build();

        FinremCaseData caseData = FinremCaseData.builder()
            .listForHearingWrapper(listForHearingWrapper)
            .mhMigrationWrapper(mhMigrationWrapper)
            .build();

        // Act
        underTest.populateListForHearingWrapper(caseData);

        // Assert
        assertEquals(YesOrNo.YES, caseData.getMhMigrationWrapper().getIsListForHearingsMigrated());
        assertThat(caseData.getManageHearingsWrapper().getHearingTabItems())
            .containsExactly(HearingTabCollectionItem.builder()
                .value(HearingTabItem.builder()
                    .tabHearingType("Final Hearing (FH)")
                    .tabCourtSelection(expectedCourtName)
                    .tabDateTime(expectedDateTime)
                    .tabTimeEstimate("45 minutes")
                    //.tabConfidentialParties(getConfidentialParties(hearing))
                    .tabAdditionalInformation(expectedAdditionalInfo)
                    //.tabHearingDocuments(mapHearingDocumentsToTabData(
                    // hearingDocumentsCollection, hearingCollectionItem.getId(), hearing))
                    .build())
                .build());
    }

}
