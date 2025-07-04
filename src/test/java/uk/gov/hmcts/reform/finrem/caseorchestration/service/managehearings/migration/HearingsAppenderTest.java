package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.migration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Court;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingTypeDirection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimTypeOfHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.HearingRegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ListForHearingWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HearingsAppenderTest {

    @InjectMocks
    private HearingsAppender underTest;

    @Test
    void shouldAppendToHearings() {
        // Arrange
        ManageHearingsCollectionItem item = mock(ManageHearingsCollectionItem.class);
        FinremCaseData caseData = FinremCaseData.builder().build();

        // Act
        underTest.appendToHearings(caseData, item);

        // Assert
        assertThat(caseData.getManageHearingsWrapper().getHearings()).containsExactly(item);
    }

    @Test
    void shouldAppendToHearingsWithExistingHearingExists() {
        // Arrange
        ManageHearingsCollectionItem existing = mock(ManageHearingsCollectionItem.class);
        ManageHearingsCollectionItem item = mock(ManageHearingsCollectionItem.class);
        FinremCaseData caseData = FinremCaseData.builder()
            .manageHearingsWrapper(ManageHearingsWrapper.builder()
                .hearings(List.of(existing))
                .build())
            .build();

        // Act
        underTest.appendToHearings(caseData, item);

        // Assert
        assertThat(caseData.getManageHearingsWrapper().getHearings()).containsExactly(existing, item);
    }

    @Test
    void shouldConvertListForHearingWrapperToHearing() {
        // Arrange
        LocalDate hearingDate = LocalDate.of(2025, 7, 3);
        String hearingTime = "10:30 AM";
        String timeEstimate = "1 hour";
        String additionalInfo = "Judge prefers early hearing";
        HearingTypeDirection hearingTypeDirection = HearingTypeDirection.FDA;

        HearingType expectedHearingType = HearingType.FDA;
        Court expectedCourt = mock(Court.class);

        HearingRegionWrapper hearingRegionWrapper = mock(HearingRegionWrapper.class);
        when(hearingRegionWrapper.toCourt()).thenReturn(expectedCourt);

        CaseDocument additionalDoc = mock(CaseDocument.class);

        ListForHearingWrapper listForHearingWrapper = ListForHearingWrapper.builder()
            .hearingDate(hearingDate)
            .hearingTime(hearingTime)
            .timeEstimate(timeEstimate)
            .additionalInformationAboutHearing(additionalInfo)
            .hearingType(hearingTypeDirection)
            .hearingRegionWrapper(hearingRegionWrapper)
            .additionalListOfHearingDocuments(additionalDoc)
            .build();

        // Act
        Hearing result = underTest.toHearing(listForHearingWrapper);

        // Assert
        assertEquals(hearingDate, result.getHearingDate());
        assertEquals(expectedHearingType, result.getHearingType());
        assertEquals(hearingTime, result.getHearingTime());
        assertEquals(timeEstimate, result.getHearingTimeEstimate());
        assertEquals(additionalInfo, result.getAdditionalHearingInformation());
        assertEquals(expectedCourt, result.getHearingCourtSelection());
        assertEquals(YesOrNo.YES, result.getWasMigrated());
        assertThat(result.getAdditionalHearingDocs())
            .extracting(DocumentCollectionItem::getValue)
            .containsExactly(additionalDoc);
    }

    @Test
    void shouldConvertInterimHearingItemToHearing() {
        // Arrange
        LocalDate hearingDate = LocalDate.of(2025, 7, 3);
        String hearingTime = "10:30 AM";
        String timeEstimate = "1 hour";
        String additionalInfo = "Judge prefers early hearing";
        InterimTypeOfHearing typeOfHearing = InterimTypeOfHearing.FH;

        HearingType expectedHearingType = HearingType.FH;
        Court expectedCourt = mock(Court.class);

        CaseDocument additionalDoc = mock(CaseDocument.class);

        InterimHearingItem interimHearingItem = spy(InterimHearingItem.builder()
            .interimHearingDate(hearingDate)
            .interimHearingTime(hearingTime)
            .interimHearingTimeEstimate(timeEstimate)
            .interimAdditionalInformationAboutHearing(additionalInfo)
            .interimHearingType(typeOfHearing)
            .interimUploadAdditionalDocument(additionalDoc)
            .build());
        when(interimHearingItem.toCourt()).thenReturn(expectedCourt);

        // Act
        Hearing result = underTest.toHearing(interimHearingItem);

        // Assert
        assertEquals(hearingDate, result.getHearingDate());
        assertEquals(expectedHearingType, result.getHearingType());
        assertEquals(hearingTime, result.getHearingTime());
        assertEquals(timeEstimate, result.getHearingTimeEstimate());
        assertEquals(additionalInfo, result.getAdditionalHearingInformation());
        assertEquals(expectedCourt, result.getHearingCourtSelection());
        assertEquals(YesOrNo.YES, result.getWasMigrated());
        assertThat(result.getAdditionalHearingDocs())
            .extracting(DocumentCollectionItem::getValue)
            .containsExactly(additionalDoc);
    }
}
