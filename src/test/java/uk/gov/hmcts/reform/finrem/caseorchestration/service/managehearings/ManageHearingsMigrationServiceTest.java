package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.tabdata.managehearings.HearingTabDataMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Court;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingTypeDirection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs.HearingTabCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs.HearingTabItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.HearingRegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ListForHearingWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.MhMigrationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogs;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.toSingletonListOrNull;

@ExtendWith(MockitoExtension.class)
class ManageHearingsMigrationServiceTest {

    @TestLogs
    private final TestLogger logs = new TestLogger(ManageHearingsMigrationService.class);

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
    void givenNonMigratedCaseData_whenPopulateListForHearing_thenDoNothing() {
        // Arrange
        MhMigrationWrapper mhMigrationWrapper = MhMigrationWrapper.builder()
            .isListForHearingsMigrated(YesOrNo.YES)
            .mhMigrationVersion("1")
            .build();

        FinremCaseData caseData = FinremCaseData.builder()
            .ccdCaseId(CASE_ID)
            .mhMigrationWrapper(mhMigrationWrapper)
            .build();

        // Act
        underTest.populateListForHearingWrapper(caseData);

        // Assert
        assertEquals(YesOrNo.YES, caseData.getMhMigrationWrapper().getIsListForHearingsMigrated());
        assertThat(caseData.getManageHearingsWrapper().getHearingTabItems()).isNull();
        assertThat(logs.getWarns()).contains(CASE_ID + " - List for Hearing migration skipped.");

        verifyNoInteractions(hearingTabDataMapper);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void givenNonMigratedCaseData_whenPopulateListForHearing_thenHearingsAndHearingTabItemsPopulated(boolean havingExistingHearings) {
        LocalDateTime fixedDateTime = LocalDateTime.of(2025, 6, 25, 10, 0);
        try (MockedStatic<LocalDateTime> mockedStatic = Mockito.mockStatic(LocalDateTime.class)) {
            mockedStatic.when(LocalDateTime::now).thenReturn(fixedDateTime);

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

            ManageHearingsCollectionItem existingHearings = havingExistingHearings ? mock(ManageHearingsCollectionItem.class) : null;
            HearingTabCollectionItem existingHearingTabCollectionItem = havingExistingHearings ? mock(HearingTabCollectionItem.class) : null;

            FinremCaseData caseData = FinremCaseData.builder()
                .listForHearingWrapper(listForHearingWrapper)
                .mhMigrationWrapper(mhMigrationWrapper)
                .manageHearingsWrapper(ManageHearingsWrapper.builder()
                    .hearings(toSingletonListOrNull(existingHearings))
                    .hearingTabItems(toSingletonListOrNull(existingHearingTabCollectionItem))
                    .build())
                .build();

            // Act
            underTest.populateListForHearingWrapper(caseData);

            // Assert
            assertEquals(YesOrNo.YES, caseData.getMhMigrationWrapper().getIsListForHearingsMigrated());

            // Assert Hearing Tab Items
            {
                List<HearingTabCollectionItem> actualItems = caseData.getManageHearingsWrapper().getHearingTabItems();
                assertThat(actualItems).hasSize(havingExistingHearings ? 2 : 1);

                if (havingExistingHearings) {
                    assertThat(actualItems).contains(existingHearingTabCollectionItem);
                }

                List<HearingTabCollectionItem> migratedTabItems = actualItems.stream()
                    .filter(item -> !item.equals(existingHearingTabCollectionItem))
                    .toList();

                HearingTabItem expectedTabItem = HearingTabItem.builder()
                    .tabHearingMigratedDate(fixedDateTime)
                    .tabHearingType("Final Hearing (FH)")
                    .tabCourtSelection(expectedCourtName)
                    .tabDateTime(expectedDateTime)
                    .tabTimeEstimate("45 minutes")
                    .tabConfidentialParties("Unknown")
                    .tabAdditionalInformation(expectedAdditionalInfo)
                    .build();

                assertThat(migratedTabItems)
                    .anySatisfy(tabItem -> assertThat(tabItem.getValue())
                        .usingRecursiveComparison()
                        .isEqualTo(expectedTabItem));
            }

            // Assert Hearings
            {
                List<ManageHearingsCollectionItem> actualHearings = caseData.getManageHearingsWrapper().getHearings();
                assertThat(actualHearings).hasSize(havingExistingHearings ? 2 : 1);

                if (havingExistingHearings) {
                    assertThat(actualHearings).contains(existingHearings);
                }

                List<ManageHearingsCollectionItem> migratedHearings = actualHearings.stream()
                    .filter(item -> !item.equals(existingHearings))
                    .toList();

                Hearing expectedHearing = Hearing.builder()
                    .hearingDate(hearingDate)
                    .hearingType(HearingType.FH)
                    .hearingTimeEstimate("45 minutes")
                    .hearingTime(hearingTime)
                    .hearingCourtSelection(court)
                    .additionalHearingInformation(additionalInfo)
                    .wasMigrated(YesOrNo.YES)
                    .build();

                assertThat(migratedHearings)
                    .anySatisfy(item -> assertThat(item.getValue())
                        .usingRecursiveComparison()
                        .isEqualTo(expectedHearing));
            }
        }
    }
}
