package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.migration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimTypeOfHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs.HearingTabCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs.HearingTabItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.HearingRegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ListForHearingWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HearingTabItemsAppenderTest {

    @Mock
    private HearingTabDataMapper hearingTabDataMapper;

    @InjectMocks
    private HearingTabItemsAppender underTest;

    @Test
    void shouldAppendToHearingTabItems() {
        // Arrange
        HearingTabCollectionItem item = mock(HearingTabCollectionItem.class);
        FinremCaseData caseData = FinremCaseData.builder().build();

        // Act
        underTest.appendToHearingTabItems(caseData, item);

        // Assert
        assertThat(caseData.getManageHearingsWrapper().getHearingTabItems()).containsExactly(item);
    }

    @Test
    void shouldAppendToHearingTabItemsWhenHearingTabItemExists() {
        // Arrange
        HearingTabCollectionItem existing = mock(HearingTabCollectionItem.class);
        HearingTabCollectionItem item = mock(HearingTabCollectionItem.class);
        FinremCaseData caseData = FinremCaseData.builder()
            .manageHearingsWrapper(ManageHearingsWrapper.builder()
                .hearingTabItems(List.of(existing))
                .build())
            .build();

        // Act
        underTest.appendToHearingTabItems(caseData, item);

        // Assert
        assertThat(caseData.getManageHearingsWrapper().getHearingTabItems()).containsExactly(existing, item);
    }

    @Test
    void shouldConvertListForHearingWrapperToHearingTabItem() {
        LocalDateTime fixedDateTime = LocalDateTime.of(2025, 6, 25, 10, 0);
        try (MockedStatic<LocalDateTime> mockedStatic = Mockito.mockStatic(LocalDateTime.class)) {
            mockedStatic.when(LocalDateTime::now).thenReturn(fixedDateTime);
            // Arrange
            LocalDate hearingDate = LocalDate.of(2025, 7, 3);
            String hearingTime = "10:00 AM";
            String timeEstimate = "1 hour";
            String additionalInfo = "Details for tab";
            HearingTypeDirection hearingTypeDirection = HearingTypeDirection.DIR;
            Court court = mock(Court.class);

            String expectedCourtName = "Birmingham FRC";
            String expectedDateTime = "03 Jul 2025 10:00 AM";
            String expectedAdditionalInfo = "Processed details";

            CaseDocument additionalDoc = mock(CaseDocument.class);

            HearingRegionWrapper hearingRegionWrapper = mock(HearingRegionWrapper.class);
            when(hearingRegionWrapper.toCourt()).thenReturn(court);

            ListForHearingWrapper listForHearingWrapper = ListForHearingWrapper.builder()
                .hearingDate(hearingDate)
                .hearingTime(hearingTime)
                .timeEstimate(timeEstimate)
                .additionalInformationAboutHearing(additionalInfo)
                .hearingType(hearingTypeDirection)
                .hearingRegionWrapper(hearingRegionWrapper)
                .additionalListOfHearingDocuments(additionalDoc)
                .build();

            when(hearingTabDataMapper.getCourtName(court)).thenReturn(expectedCourtName);
            when(hearingTabDataMapper.getFormattedDateTime(hearingDate, hearingTime)).thenReturn(expectedDateTime);
            when(hearingTabDataMapper.getAdditionalInformation(additionalInfo)).thenReturn(expectedAdditionalInfo);

            // Act
            HearingTabItem result = underTest.toHearingTabItem(listForHearingWrapper);

            // Assert
            assertThat(result)
                .extracting(
                    HearingTabItem::getTabHearingType,
                    HearingTabItem::getTabCourtSelection,
                    HearingTabItem::getTabDateTime,
                    HearingTabItem::getTabTimeEstimate,
                    HearingTabItem::getTabConfidentialParties,
                    HearingTabItem::getTabAdditionalInformation
                )
                .containsExactly(
                    hearingTypeDirection.getId(),
                    expectedCourtName,
                    expectedDateTime,
                    timeEstimate,
                    "Unknown",
                    expectedAdditionalInfo
                );
            assertThat(result).extracting(HearingTabItem::getTabHearingMigratedDate).isNotNull();
            assertThat(result.getTabHearingDocuments())
                .isNotNull()
                .hasSize(1)
                .extracting(DocumentCollectionItem::getValue)
                .containsExactly(additionalDoc);
            assertThat(result.getTabHearingMigratedDate()).isEqualTo(fixedDateTime);
        }
    }

    @Test
    void shouldConvertInterimHearingItemToHearingTabItem() {
        LocalDateTime fixedDateTime = LocalDateTime.of(2025, 6, 25, 10, 0);
        try (MockedStatic<LocalDateTime> mockedStatic = Mockito.mockStatic(LocalDateTime.class)) {
            mockedStatic.when(LocalDateTime::now).thenReturn(fixedDateTime);
            // Arrange
            LocalDate hearingDate = LocalDate.of(2025, 7, 3);
            String hearingTime = "10:00 AM";
            String timeEstimate = "1 hour";
            String additionalInfo = "Details for tab";
            InterimTypeOfHearing typeOfHearing = InterimTypeOfHearing.FH;
            Court court = mock(Court.class);

            String expectedCourtName = "Birmingham FRC";
            String expectedDateTime = "03 Jul 2025 10:00 AM";
            String expectedAdditionalInfo = "Processed details";

            CaseDocument additionalDoc = mock(CaseDocument.class);

            InterimHearingItem interimHearingItem = spy(InterimHearingItem.builder()
                .interimHearingDate(hearingDate)
                .interimHearingTime(hearingTime)
                .interimHearingTimeEstimate(timeEstimate)
                .interimAdditionalInformationAboutHearing(additionalInfo)
                .interimHearingType(typeOfHearing)
                .interimUploadAdditionalDocument(additionalDoc)
                .build());

            when(interimHearingItem.toCourt()).thenReturn(court);
            when(hearingTabDataMapper.getCourtName(court)).thenReturn(expectedCourtName);
            when(hearingTabDataMapper.getFormattedDateTime(hearingDate, hearingTime)).thenReturn(expectedDateTime);
            when(hearingTabDataMapper.getAdditionalInformation(additionalInfo)).thenReturn(expectedAdditionalInfo);

            // Act
            HearingTabItem result = underTest.toHearingTabItem(interimHearingItem);

            // Assert
            assertThat(result)
                .extracting(
                    HearingTabItem::getTabHearingType,
                    HearingTabItem::getTabCourtSelection,
                    HearingTabItem::getTabDateTime,
                    HearingTabItem::getTabTimeEstimate,
                    HearingTabItem::getTabConfidentialParties,
                    HearingTabItem::getTabAdditionalInformation
                )
                .containsExactly(
                    typeOfHearing.getId(),
                    expectedCourtName,
                    expectedDateTime,
                    timeEstimate,
                    "Unknown",
                    expectedAdditionalInfo
                );
            assertThat(result.getTabHearingDocuments())
                .isNotNull()
                .hasSize(1)
                .extracting(DocumentCollectionItem::getValue)
                .containsExactly(additionalDoc);
            assertThat(result.getTabHearingMigratedDate()).isEqualTo(fixedDateTime);
        }
    }
}
