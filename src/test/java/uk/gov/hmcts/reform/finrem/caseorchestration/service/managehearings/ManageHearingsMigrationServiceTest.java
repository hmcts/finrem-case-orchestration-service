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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingBulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingBulkPrintDocumentsData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimTypeOfHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs.HearingTabCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs.HearingTabItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.HearingRegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.InterimWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ListForHearingWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.MhMigrationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogs;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;

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
    void givenMigratedCaseDataWithListForHearingDataShouldDoNothing() {
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
    void givenNonMigratedCaseDataWithListForHearingDataShouldPopulateToHearingTabItem(boolean havingExistingHearingTabItem) {
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

            ManageHearingsCollectionItem existingWorkingHearings = havingExistingHearingTabItem ? mock(ManageHearingsCollectionItem.class) : null;
            HearingTabCollectionItem existingHearingTabCollectionItem = havingExistingHearingTabItem ? mock(HearingTabCollectionItem.class) : null;

            FinremCaseData caseData = FinremCaseData.builder()
                .listForHearingWrapper(listForHearingWrapper)
                .mhMigrationWrapper(mhMigrationWrapper)
                .manageHearingsWrapper(ManageHearingsWrapper.builder()
                    .hearings(toSingletonListOrNull(existingWorkingHearings))
                    .hearingTabItems(toSingletonListOrNull(existingHearingTabCollectionItem))
                    .build())
                .build();

            // Act
            underTest.populateListForHearingWrapper(caseData);

            // Assert
            assertEquals(YesOrNo.YES, caseData.getMhMigrationWrapper().getIsListForHearingsMigrated());
            assertThat(caseData.getManageHearingsWrapper().getHearingTabItems())
                .anySatisfy(item -> assertThat(item.getValue())
                    .usingRecursiveComparison()
                    .ignoringFields("tabHearingDocuments") // if this is still commented or not important in the test
                    .isEqualTo(HearingTabItem.builder()
                        .tabHearingMigratedDate(fixedDateTime)
                        .tabHearingType("Final Hearing (FH)")
                        .tabCourtSelection(expectedCourtName)
                        .tabDateTime(expectedDateTime)
                        .tabTimeEstimate("45 minutes")
                        .tabConfidentialParties("Unknown")
                        .tabAdditionalInformation(expectedAdditionalInfo)
                        .build()));

        }
    }

    @Test
    void givenMigratedCaseDataWithListForInterimHearingDataShouldDoNothing() {
        // Arrange
        MhMigrationWrapper mhMigrationWrapper = MhMigrationWrapper.builder()
            .isListForInterimHearingsMigrated(YesOrNo.YES)
            .mhMigrationVersion("1")
            .build();

        FinremCaseData caseData = FinremCaseData.builder()
            .ccdCaseId(CASE_ID)
            .mhMigrationWrapper(mhMigrationWrapper)
            .build();

        // Act
        underTest.populateListForInterimHearingWrapper(caseData);

        // Assert
        assertEquals(YesOrNo.YES, caseData.getMhMigrationWrapper().getIsListForInterimHearingsMigrated());
        assertThat(caseData.getManageHearingsWrapper().getHearingTabItems()).isNull();
        assertThat(logs.getWarns()).contains(CASE_ID + " - List for Interim Hearing migration skipped.");

        verifyNoInteractions(hearingTabDataMapper);
    }

    @Test
    void givenNonMigratedListForInterimHearingData_whenInterimHearingDocumentMissing_thenDoNothing() {
        // Arrange
        MhMigrationWrapper mhMigrationWrapper = MhMigrationWrapper.builder().build();

        FinremCaseData caseData = FinremCaseData.builder()
            .ccdCaseId(CASE_ID)
            .mhMigrationWrapper(mhMigrationWrapper)
            .interimWrapper(InterimWrapper.builder()
                .interimHearings(toInterimHearings(mock(InterimHearingItem.class)))
                .build())
            .build();

        // Act
        underTest.populateListForInterimHearingWrapper(caseData);

        // Assert
        assertEquals(YesOrNo.NO, caseData.getMhMigrationWrapper().getIsListForInterimHearingsMigrated());
        assertThat(caseData.getManageHearingsWrapper().getHearingTabItems()).isNull();
        assertThat(logs.getWarns()).contains(CASE_ID + " - List for Interim Hearing migration fails. Insufficient interim hearing documents.");

        verifyNoInteractions(hearingTabDataMapper);
    }

    @Test
    void givenNonMigratedSingleListForInterimHearingData_thenPopulateToHearingTabItems() {
        LocalDateTime fixedDateTime = LocalDateTime.of(2025, 6, 25, 10, 0);
        try (MockedStatic<LocalDateTime> mockedStatic = Mockito.mockStatic(LocalDateTime.class)) {
            mockedStatic.when(LocalDateTime::now).thenReturn(fixedDateTime);
            // Arrange
            MhMigrationWrapper mhMigrationWrapper = MhMigrationWrapper.builder().build();

            InterimHearingItem interimHearingItem1 = stubInterimHearingOne();

            FinremCaseData caseData = FinremCaseData.builder()
                .ccdCaseId(CASE_ID)
                .mhMigrationWrapper(mhMigrationWrapper)
                .interimWrapper(InterimWrapper.builder()
                    .interimHearings(toInterimHearings(interimHearingItem1))
                    .interimHearingDocuments(toInterimHearingDocuments(interimHearingItem1))
                    .build())
                .build();

            // Act
            underTest.populateListForInterimHearingWrapper(caseData);

            // Assert
            assertEquals(YesOrNo.YES, caseData.getMhMigrationWrapper().getIsListForInterimHearingsMigrated());
            List<HearingTabItem> tabItems = assertAndGetHearingTabItems(caseData);
            assertAllTabItemWithMigratedDate(tabItems, fixedDateTime);
            assertThat(tabItems)
                .extracting(HearingTabItem::getTabHearingType)
                .containsExactly("First Directions Appointment (FDA)");
            assertThat(tabItems)
                .extracting(HearingTabItem::getTabCourtSelection)
                .containsExactly("COURT ONE NAME");
            assertThat(tabItems)
                .extracting(HearingTabItem::getTabDateTime)
                .containsExactly("2025-06-04 10:00");
            assertThat(tabItems)
                .extracting(HearingTabItem::getTabTimeEstimate)
                .containsExactly("1.11 hour");
            assertThat(tabItems)
                .extracting(HearingTabItem::getTabConfidentialParties)
                .containsExactly("Unknown");
            assertThat(tabItems)
                .extracting(HearingTabItem::getTabAdditionalInformation)
                .containsExactly("<p>Some notes (1)</p>");
        }
    }

    @Test
    void givenNonMigratedMultipleListForInterimHearingData_thenPopulateToHearingTabItems() {
        LocalDateTime fixedDateTime = LocalDateTime.of(2025, 6, 25, 10, 0);
        try (MockedStatic<LocalDateTime> mockedStatic = Mockito.mockStatic(LocalDateTime.class)) {
            mockedStatic.when(LocalDateTime::now).thenReturn(fixedDateTime);
            // Arrange
            MhMigrationWrapper mhMigrationWrapper = MhMigrationWrapper.builder().build();

            InterimHearingItem interimHearingItem1 = stubInterimHearingOne();
            InterimHearingItem interimHearingItem2 = stubInterimHearingTwo();

            FinremCaseData caseData = FinremCaseData.builder()
                .ccdCaseId(CASE_ID)
                .mhMigrationWrapper(mhMigrationWrapper)
                .interimWrapper(InterimWrapper.builder()
                    .interimHearings(toInterimHearings(interimHearingItem1, interimHearingItem2))
                    .interimHearingDocuments(toInterimHearingDocuments(interimHearingItem1, interimHearingItem2))
                    .build())
                .build();

            // Act
            underTest.populateListForInterimHearingWrapper(caseData);

            // Assert
            assertEquals(YesOrNo.YES, caseData.getMhMigrationWrapper().getIsListForInterimHearingsMigrated());
            List<HearingTabItem> tabItems = assertAndGetHearingTabItems(caseData);
            assertAllTabItemWithMigratedDate(tabItems, fixedDateTime);
            assertThat(tabItems)
                .extracting(HearingTabItem::getTabHearingType)
                .containsExactly("First Directions Appointment (FDA)", "Directions (DIR)");
            assertThat(tabItems)
                .extracting(HearingTabItem::getTabCourtSelection)
                .containsExactly("COURT ONE NAME", "COURT TWO NAME");
            assertThat(tabItems)
                .extracting(HearingTabItem::getTabDateTime)
                .containsExactly("2025-06-04 10:00", "2025-06-09 11:00");
            assertThat(tabItems)
                .extracting(HearingTabItem::getTabTimeEstimate)
                .containsExactly("1.11 hour", "1.12 hour");
            assertThat(tabItems)
                .extracting(HearingTabItem::getTabConfidentialParties)
                .containsExactly("Unknown", "Unknown");
            assertThat(tabItems)
                .extracting(HearingTabItem::getTabAdditionalInformation)
                .containsExactly("<p>Some notes (1)</p>", "<p>Some notes (2)</p>");
        }
    }

    private InterimHearingItem stubInterimHearingOne() {
        LocalDate hearingDate = LocalDate.of(2025, 6, 4);
        String hearingTime = "10:00";
        String additionalInfo = "Some notes";

        InterimHearingItem interimHearingItem = spy(InterimHearingItem.class);
        interimHearingItem.setInterimHearingTime(hearingTime);
        interimHearingItem.setInterimHearingType(InterimTypeOfHearing.FDA);
        interimHearingItem.setInterimHearingDate(hearingDate);
        interimHearingItem.setInterimHearingTimeEstimate("1.11 hour");
        interimHearingItem.setInterimAdditionalInformationAboutHearing(additionalInfo);

        Court court = mock(Court.class);
        when(interimHearingItem.toCourt()).thenReturn(court);
        when(hearingTabDataMapper.getCourtName(court)).thenReturn("COURT ONE NAME");
        when(hearingTabDataMapper.getFormattedDateTime(hearingDate, hearingTime)).thenReturn("2025-06-04 10:00");
        when(hearingTabDataMapper.getAdditionalInformation(additionalInfo)).thenReturn("<p>Some notes (1)</p>");
        return interimHearingItem;
    }

    private InterimHearingItem stubInterimHearingTwo() {
        LocalDate hearingDate = LocalDate.of(2025, 6, 9);
        String hearingTime = "11:00";
        String additionalInfo = "Some notes 2";

        InterimHearingItem interimHearingItem = spy(InterimHearingItem.class);
        interimHearingItem.setInterimHearingTime(hearingTime);
        interimHearingItem.setInterimHearingType(InterimTypeOfHearing.DIR);
        interimHearingItem.setInterimHearingDate(hearingDate);
        interimHearingItem.setInterimHearingTimeEstimate("1.12 hour");
        interimHearingItem.setInterimAdditionalInformationAboutHearing(additionalInfo);

        Court court = mock(Court.class);
        when(interimHearingItem.toCourt()).thenReturn(court);
        when(hearingTabDataMapper.getCourtName(court)).thenReturn("COURT TWO NAME");
        when(hearingTabDataMapper.getFormattedDateTime(hearingDate, hearingTime)).thenReturn("2025-06-09 11:00");
        when(hearingTabDataMapper.getAdditionalInformation(additionalInfo)).thenReturn("<p>Some notes (2)</p>");
        return interimHearingItem;
    }

    private static List<InterimHearingBulkPrintDocumentsData> toInterimHearingDocuments(InterimHearingItem... interimHearingItems) {
        AtomicInteger index = new AtomicInteger(1);
        return Arrays.stream(interimHearingItems)
            .map(item -> InterimHearingBulkPrintDocumentsData.builder()
                .value(InterimHearingBulkPrintDocument.builder()
                    .caseDocument(caseDocument(String.valueOf(index.get()), index.getAndIncrement() + ".pdf"))
                    .build())
                .build())
            .toList();
    }

    private static List<InterimHearingCollection> toInterimHearings(InterimHearingItem... interimHearingItems) {
        return Arrays.stream(interimHearingItems)
            .map(item -> InterimHearingCollection.builder().value(item).build())
            .toList();
    }

    private static List<HearingTabItem> getTargetHearingTabItems(FinremCaseData caseData) {
        return caseData.getManageHearingsWrapper().getHearingTabItems().stream()
            .map(HearingTabCollectionItem::getValue)
            .toList();
    }

    private static List<HearingTabItem> assertAndGetHearingTabItems(FinremCaseData caseData) {
        assertThat(caseData.getManageHearingsWrapper()).isNotNull();
        assertThat(caseData.getManageHearingsWrapper().getHearingTabItems()).isNotNull();
        return getTargetHearingTabItems(caseData);
    }

    private static void assertAllTabItemWithMigratedDate(List<HearingTabItem> tabItems, LocalDateTime fixedDateTime) {
        assertThat(tabItems)
            .extracting(HearingTabItem::getTabHearingMigratedDate)
            .allMatch(date -> date.equals(fixedDateTime));
    }

    public static <T> List<T> toSingletonListOrNull(T item) {
        return item == null ? null : List.of(item);
    }
}
