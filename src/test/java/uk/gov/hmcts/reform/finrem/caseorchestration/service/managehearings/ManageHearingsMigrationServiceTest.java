package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.tabdata.managehearings.HearingTabDataMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Court;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingTypeDirection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingBulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingBulkPrintDocumentsData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimTypeOfHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType;
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
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.utils.ListUtils.safeListWithoutNulls;
import static uk.gov.hmcts.reform.finrem.caseorchestration.utils.ListUtils.toListOrNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.utils.ListUtils.toSingletonListOrNull;

@ExtendWith(MockitoExtension.class)
class ManageHearingsMigrationServiceTest {

    @TestLogs
    private final TestLogger logs = new TestLogger(ManageHearingsMigrationService.class);

    @Mock
    private HearingTabDataMapper hearingTabDataMapper;

    @InjectMocks
    private ManageHearingsMigrationService underTest;

    private final LocalDate interimHearingDateOne = LocalDate.of(2025, 6, 4);
    private final HearingType interimHearingTypeeOne = HearingType.FDA;
    private final String interimHearingTimeOne = "10:00";
    private final String interimHearingTimeEstimateOne = "1.11 hour";
    private final String interimHearingAdditionalInfoOne = "Some notes";
    private final Court interimHearingCourtOne = mock(Court.class);

    private final LocalDate interimHearingDateTwo = LocalDate.of(2025, 6, 9);
    private final HearingType interimHearingTypeeTwo = HearingType.DIR;
    private final String interimHearingTimeTwo = "11:00";
    private final String interimHearingTimeEstimateTwo = "1.12 hour";
    private final String interimHearingAdditionalInfoTwo = "Some notes 2";
    private final Court interimHearingCourtTwo = mock(Court.class);

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

    private static Stream<Arguments> listForHearingTestConfigurations() {
        return Stream.of(
            Arguments.of(true, true),
            Arguments.of(true, false),
            Arguments.of(false, true),
            Arguments.of(false, false)
        );
    }

    @ParameterizedTest
    @MethodSource("listForHearingTestConfigurations")
    void givenNonMigratedCaseData_whenPopulateListForHearing_thenHearingsAndHearingTabItemsPopulated(boolean havingExistingHearing,
                                                                                                     boolean withAdditionalDocument) {
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
            CaseDocument additionalCaseDocument = mock(CaseDocument.class);

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
                .additionalListOfHearingDocuments(withAdditionalDocument ? additionalCaseDocument : null)
                .build();

            MhMigrationWrapper mhMigrationWrapper = MhMigrationWrapper.builder()
                .isListForHearingsMigrated(YesOrNo.NO)
                .build();

            ManageHearingsCollectionItem existingHearing = havingExistingHearing ? mock(ManageHearingsCollectionItem.class) : null;
            HearingTabCollectionItem existingHearingTabCollectionItem = havingExistingHearing ? mock(HearingTabCollectionItem.class) : null;

            FinremCaseData caseData = FinremCaseData.builder()
                .listForHearingWrapper(listForHearingWrapper)
                .mhMigrationWrapper(mhMigrationWrapper)
                .manageHearingsWrapper(ManageHearingsWrapper.builder()
                    .hearings(toSingletonListOrNull(existingHearing))
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
                assertThat(actualItems).hasSize(havingExistingHearing ? 2 : 1);

                if (havingExistingHearing) {
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
                    .anySatisfy(tabItem -> {
                        assertThat(tabItem.getValue())
                            .usingRecursiveComparison()
                            .ignoringFields("tabHearingDocuments")
                            .isEqualTo(expectedTabItem);
                        if (withAdditionalDocument) {
                            assertThat(tabItem.getValue())
                                .extracting(HearingTabItem::getTabHearingDocuments)
                                .satisfies(documents -> {
                                    assertThat(documents).isNotNull().hasSize(1)
                                        .extracting(DocumentCollectionItem::getValue)
                                            .containsExactly(additionalCaseDocument);
                                });
                        }
                    });
            }

            // Assert Hearings
            {
                List<ManageHearingsCollectionItem> actualHearings = caseData.getManageHearingsWrapper().getHearings();
                assertThat(actualHearings).hasSize(havingExistingHearing ? 2 : 1);

                if (havingExistingHearing) {
                    assertThat(actualHearings).contains(existingHearing);
                }

                List<ManageHearingsCollectionItem> migratedHearings = actualHearings.stream()
                    .filter(item -> !item.equals(existingHearing))
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
                        .ignoringFields("additionalHearingDocs")
                        .isEqualTo(expectedHearing));
            }
        }
    }

    @Test
    void givenMigratedCaseData_whenPopulateListForInterimHearing_thenSkipProcess() {
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
        assertThat(caseData.getManageHearingsWrapper().getHearings()).isNull();
        assertThat(logs.getWarns()).contains(CASE_ID + " - List for Interim Hearing migration skipped.");

        verifyNoInteractions(hearingTabDataMapper);
    }

    @Test
    void givenMigratedCaseDataWithoutInterimHearings_whenPopulateListForInterimHearing_thenMarkMigrated() {
        // Arrange
        MhMigrationWrapper mhMigrationWrapper = MhMigrationWrapper.builder()
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
        assertThat(caseData.getManageHearingsWrapper().getHearings()).isNull();
        assertThat(logs.getWarns()).doesNotContain(CASE_ID + " - List for Interim Hearing migration skipped.");

        verifyNoInteractions(hearingTabDataMapper);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void givenNonMigratedWithSingleInterimHearings_whenPopulateListForInterimHearing_thenHearingsAndHearingTabItemsPopulated(
            boolean havingExistingHearing) {
        LocalDateTime fixedDateTime = LocalDateTime.of(2025, 6, 25, 10, 0);
        try (MockedStatic<LocalDateTime> mockedStatic = Mockito.mockStatic(LocalDateTime.class)) {
            mockedStatic.when(LocalDateTime::now).thenReturn(fixedDateTime);
            // Arrange
            MhMigrationWrapper mhMigrationWrapper = MhMigrationWrapper.builder().build();

            InterimHearingItem interimHearingItem1 = stubInterimHearingOne();

            ManageHearingsCollectionItem existingHearing = havingExistingHearing ? mock(ManageHearingsCollectionItem.class) : null;
            HearingTabCollectionItem existingHearingTabCollectionItem = havingExistingHearing ? mock(HearingTabCollectionItem.class) : null;

            FinremCaseData caseData = FinremCaseData.builder()
                .ccdCaseId(CASE_ID)
                .mhMigrationWrapper(mhMigrationWrapper)
                .interimWrapper(InterimWrapper.builder()
                    .interimHearings(toInterimHearings(interimHearingItem1))
                    .interimHearingDocuments(toInterimHearingDocuments(interimHearingItem1))
                    .build())
                .manageHearingsWrapper(ManageHearingsWrapper.builder()
                    .hearings(toSingletonListOrNull(existingHearing))
                    .hearingTabItems(toSingletonListOrNull(existingHearingTabCollectionItem))
                    .build())
                .build();

            // Act
            underTest.populateListForInterimHearingWrapper(caseData);

            // Assert
            assertEquals(YesOrNo.YES, caseData.getMhMigrationWrapper().getIsListForInterimHearingsMigrated());

            // Assert Hearing Tab Item
            {
                List<HearingTabCollectionItem> actualTabItems = caseData.getManageHearingsWrapper().getHearingTabItems();
                assertThat(actualTabItems).hasSize(havingExistingHearing ? 2 : 1);
                assertExistingHearingTabItemRetained(actualTabItems, existingHearingTabCollectionItem);

                List<HearingTabItem> migratedTabItems = assertAndGetHearingTabItems(caseData, existingHearingTabCollectionItem);
                assertAllTabItemWithMigratedDate(migratedTabItems, fixedDateTime);
                assertThat(migratedTabItems)
                    .extracting(HearingTabItem::getTabHearingType)
                    .containsExactly("First Directions Appointment (FDA)");
                assertThat(migratedTabItems)
                    .extracting(HearingTabItem::getTabCourtSelection)
                    .containsExactly("COURT ONE NAME");
                assertThat(migratedTabItems)
                    .extracting(HearingTabItem::getTabDateTime)
                    .containsExactly("2025-06-04 10:00");
                assertThat(migratedTabItems)
                    .extracting(HearingTabItem::getTabTimeEstimate)
                    .containsExactly("1.11 hour");
                assertThat(migratedTabItems)
                    .extracting(HearingTabItem::getTabConfidentialParties)
                    .containsExactly("Unknown");
                assertThat(migratedTabItems)
                    .extracting(HearingTabItem::getTabAdditionalInformation)
                    .containsExactly("<p>Some notes (1)</p>");
            }
            // Assert Hearings
            {
                List<ManageHearingsCollectionItem> actualHearings = caseData.getManageHearingsWrapper().getHearings();
                assertThat(actualHearings).hasSize(havingExistingHearing ? 2 : 1);
                if (havingExistingHearing) {
                    assertThat(actualHearings).contains(existingHearing);
                }

                List<ManageHearingsCollectionItem> migratedHearings = actualHearings.stream()
                    .filter(item -> !item.equals(existingHearing))
                    .toList();

                Hearing expectedHearing = Hearing.builder()
                    .hearingDate(interimHearingDateOne)
                    .hearingType(interimHearingTypeeOne)
                    .hearingTimeEstimate(interimHearingTimeEstimateOne)
                    .hearingTime(interimHearingTimeOne)
                    .hearingCourtSelection(interimHearingCourtOne)
                    .additionalHearingInformation(interimHearingAdditionalInfoOne)
                    .wasMigrated(YesOrNo.YES)
                    .build();

                assertThat(migratedHearings)
                    .anySatisfy(item -> assertThat(item.getValue())
                        .usingRecursiveComparison()
                        .isEqualTo(expectedHearing));
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void givenNonMigratedWithMultipleInterimHearings_whenPopulateListForInterimHearing_thenHearingsAndHearingTabItemsPopulated(
            boolean havingExistingHearing) {
        LocalDateTime fixedDateTime = LocalDateTime.of(2025, 6, 25, 10, 0);
        try (MockedStatic<LocalDateTime> mockedStatic = Mockito.mockStatic(LocalDateTime.class)) {
            mockedStatic.when(LocalDateTime::now).thenReturn(fixedDateTime);
            // Arrange
            MhMigrationWrapper mhMigrationWrapper = MhMigrationWrapper.builder().build();

            InterimHearingItem interimHearingItem1 = stubInterimHearingOne();
            InterimHearingItem interimHearingItem2 = stubInterimHearingTwo();

            ManageHearingsCollectionItem existingHearing = havingExistingHearing ? mock(ManageHearingsCollectionItem.class) : null;
            ManageHearingsCollectionItem existingHearing2 = havingExistingHearing ? mock(ManageHearingsCollectionItem.class) : null;
            HearingTabCollectionItem existingHearingTabCollectionItem = havingExistingHearing ? mock(HearingTabCollectionItem.class) : null;
            HearingTabCollectionItem existingHearingTabCollectionItem2 = havingExistingHearing ? mock(HearingTabCollectionItem.class) : null;
            HearingTabCollectionItem[] existingHearingTabCollectionItems = {existingHearingTabCollectionItem, existingHearingTabCollectionItem2};

            FinremCaseData caseData = FinremCaseData.builder()
                .ccdCaseId(CASE_ID)
                .mhMigrationWrapper(mhMigrationWrapper)
                .interimWrapper(InterimWrapper.builder()
                    .interimHearings(toInterimHearings(interimHearingItem1, interimHearingItem2))
                    .interimHearingDocuments(toInterimHearingDocuments(interimHearingItem1, interimHearingItem2))
                    .build())
                .manageHearingsWrapper(ManageHearingsWrapper.builder()
                    .hearings(toListOrNull(existingHearing, existingHearing2))
                    .hearingTabItems(toListOrNull(existingHearingTabCollectionItems))
                    .build())
                .build();

            // Act
            underTest.populateListForInterimHearingWrapper(caseData);

            // Assert
            assertEquals(YesOrNo.YES, caseData.getMhMigrationWrapper().getIsListForInterimHearingsMigrated());

            // Assert Hearing Tab Item
            {
                List<HearingTabCollectionItem> actualItems = caseData.getManageHearingsWrapper().getHearingTabItems();
                assertThat(actualItems).hasSize(havingExistingHearing ? 4 : 2);
                assertExistingHearingTabItemRetained(actualItems, existingHearingTabCollectionItems);

                List<HearingTabItem> migratedTabItems = assertAndGetHearingTabItems(caseData, existingHearingTabCollectionItems);
                assertAllTabItemWithMigratedDate(migratedTabItems, fixedDateTime);
                assertThat(migratedTabItems)
                    .extracting(HearingTabItem::getTabHearingType)
                    .containsExactly("First Directions Appointment (FDA)", "Directions (DIR)");
                assertThat(migratedTabItems)
                    .extracting(HearingTabItem::getTabCourtSelection)
                    .containsExactly("COURT ONE NAME", "COURT TWO NAME");
                assertThat(migratedTabItems)
                    .extracting(HearingTabItem::getTabDateTime)
                    .containsExactly("2025-06-04 10:00", "2025-06-09 11:00");
                assertThat(migratedTabItems)
                    .extracting(HearingTabItem::getTabTimeEstimate)
                    .containsExactly("1.11 hour", "1.12 hour");
                assertThat(migratedTabItems)
                    .extracting(HearingTabItem::getTabConfidentialParties)
                    .containsExactly("Unknown", "Unknown");
                assertThat(migratedTabItems)
                    .extracting(HearingTabItem::getTabAdditionalInformation)
                    .containsExactly("<p>Some notes (1)</p>", "<p>Some notes (2)</p>");
            }
            // Assert Hearings
            {
                List<ManageHearingsCollectionItem> actualHearings = caseData.getManageHearingsWrapper().getHearings();
                assertThat(actualHearings).hasSize(havingExistingHearing ? 4 : 2);
                if (havingExistingHearing) {
                    assertThat(actualHearings).contains(existingHearing);
                }

                List<ManageHearingsCollectionItem> migratedHearings = actualHearings.stream()
                    .filter(item -> !safeListWithoutNulls(existingHearing, existingHearing2).contains(item))
                    .toList();

                Hearing expectedHearingOne = Hearing.builder()
                    .hearingDate(interimHearingDateOne)
                    .hearingType(interimHearingTypeeOne)
                    .hearingTimeEstimate(interimHearingTimeEstimateOne)
                    .hearingTime(interimHearingTimeOne)
                    .hearingCourtSelection(interimHearingCourtOne)
                    .additionalHearingInformation(interimHearingAdditionalInfoOne)
                    .wasMigrated(YesOrNo.YES)
                    .build();

                Hearing expectedHearingTwo = Hearing.builder()
                    .hearingDate(interimHearingDateTwo)
                    .hearingType(interimHearingTypeeTwo)
                    .hearingTimeEstimate(interimHearingTimeEstimateTwo)
                    .hearingTime(interimHearingTimeTwo)
                    .hearingCourtSelection(interimHearingCourtTwo)
                    .additionalHearingInformation(interimHearingAdditionalInfoTwo)
                    .wasMigrated(YesOrNo.YES)
                    .build();

                // Assert both expected hearings are in the migrated list
                assertThat(migratedHearings)
                    .extracting(ManageHearingsCollectionItem::getValue)
                    .usingRecursiveFieldByFieldElementComparator()
                    .containsExactlyInAnyOrder(expectedHearingOne, expectedHearingTwo);
            }
        }
    }

    private InterimHearingItem stubInterimHearingOne() {
        InterimHearingItem interimHearingItem = spy(InterimHearingItem.class);
        interimHearingItem.setInterimHearingTime(interimHearingTimeOne);
        interimHearingItem.setInterimHearingType(InterimTypeOfHearing.FDA);
        interimHearingItem.setInterimHearingDate(interimHearingDateOne);
        interimHearingItem.setInterimHearingTimeEstimate(interimHearingTimeEstimateOne);
        interimHearingItem.setInterimAdditionalInformationAboutHearing(interimHearingAdditionalInfoOne);

        when(interimHearingItem.toCourt()).thenReturn(interimHearingCourtOne);
        when(hearingTabDataMapper.getCourtName(interimHearingCourtOne)).thenReturn("COURT ONE NAME");
        when(hearingTabDataMapper.getFormattedDateTime(interimHearingDateOne, interimHearingTimeOne)).thenReturn("2025-06-04 10:00");
        when(hearingTabDataMapper.getAdditionalInformation(interimHearingAdditionalInfoOne)).thenReturn("<p>Some notes (1)</p>");
        return interimHearingItem;
    }

    private InterimHearingItem stubInterimHearingTwo() {
        InterimHearingItem interimHearingItem = spy(InterimHearingItem.class);
        interimHearingItem.setInterimHearingTime(interimHearingTimeTwo);
        interimHearingItem.setInterimHearingType(InterimTypeOfHearing.DIR);
        interimHearingItem.setInterimHearingDate(interimHearingDateTwo);
        interimHearingItem.setInterimHearingTimeEstimate(interimHearingTimeEstimateTwo);
        interimHearingItem.setInterimAdditionalInformationAboutHearing(interimHearingAdditionalInfoTwo);

        when(interimHearingItem.toCourt()).thenReturn(interimHearingCourtTwo);
        when(hearingTabDataMapper.getCourtName(interimHearingCourtTwo)).thenReturn("COURT TWO NAME");
        when(hearingTabDataMapper.getFormattedDateTime(interimHearingDateTwo, interimHearingTimeTwo)).thenReturn("2025-06-09 11:00");
        when(hearingTabDataMapper.getAdditionalInformation(interimHearingAdditionalInfoTwo)).thenReturn("<p>Some notes (2)</p>");
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

    private static List<HearingTabItem> getTargetHearingTabItems(FinremCaseData caseData, HearingTabCollectionItem... existingHearingTabItems) {
        List<HearingTabCollectionItem> existing = safeListWithoutNulls(existingHearingTabItems);
        return caseData.getManageHearingsWrapper().getHearingTabItems().stream()
            .filter(item -> !existing.contains(item))
            .map(HearingTabCollectionItem::getValue)
            .toList();
    }

    private static List<HearingTabItem> assertAndGetHearingTabItems(FinremCaseData caseData, HearingTabCollectionItem... existingHearingTabItems) {
        assertThat(caseData.getManageHearingsWrapper()).isNotNull();
        assertThat(caseData.getManageHearingsWrapper().getHearingTabItems()).isNotNull();
        return getTargetHearingTabItems(caseData, existingHearingTabItems);
    }

    private static void assertAllTabItemWithMigratedDate(List<HearingTabItem> tabItems, LocalDateTime fixedDateTime) {
        assertThat(tabItems)
            .extracting(HearingTabItem::getTabHearingMigratedDate)
            .allMatch(date -> date.equals(fixedDateTime));
    }

    private static void assertExistingHearingTabItemRetained(List<HearingTabCollectionItem> actualItems,
                                                             HearingTabCollectionItem... existingHearingTabCollectionItems) {
        List<HearingTabCollectionItem> a = safeListWithoutNulls(existingHearingTabCollectionItems);
        if (!a.isEmpty()) {
            assertThat(actualItems).containsAll(a);
        }
    }
}
