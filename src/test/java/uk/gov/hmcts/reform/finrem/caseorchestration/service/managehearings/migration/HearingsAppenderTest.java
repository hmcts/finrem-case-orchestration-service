package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.migration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Court;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetail;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingDirectionDetail;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingTypeDirection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimTypeOfHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationRegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationWrapper;
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
class HearingsAppenderTest {

    @InjectMocks
    private HearingsAppender underTest;

    @Test
    void shouldAppendToHearings() {
        // Arrange
        ManageHearingsCollectionItem item = mock(ManageHearingsCollectionItem.class);
        FinremCaseData caseData = FinremCaseData.builder().build();

        // Act
        underTest.appendToHearings(caseData, () -> item);

        // Assert
        assertThat(caseData.getManageHearingsWrapper().getHearings()).containsExactly(item);
        assertThat(caseData.getManageHearingsWrapper().getHearings()).extracting(ManageHearingsCollectionItem::getId)
            .isNotNull();
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
        underTest.appendToHearings(caseData, () -> item);

        // Assert
        assertThat(caseData.getManageHearingsWrapper().getHearings()).containsExactly(existing, item);
    }

    @ValueSource(booleans = {true, false})
    @ParameterizedTest
    void shouldConvertListForHearingWrapperToHearing(boolean withAdditionDoc) {
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

        CaseDocument additionalDoc = withAdditionDoc ? spy(CaseDocument.class) : null;

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
        if (withAdditionDoc) {
            assertThat(result.getAdditionalHearingDocs())
                .extracting(DocumentCollectionItem::getValue)
                .containsExactly(additionalDoc);
        } else {
            assertThat(result.getAdditionalHearingDocs()).isNull();
        }
    }

    @ValueSource(booleans = {true, false})
    @ParameterizedTest
    void shouldConvertInterimHearingItemToHearing(boolean withAdditionDoc) {
        // Arrange
        LocalDate hearingDate = LocalDate.of(2025, 7, 3);
        String hearingTime = "10:30 AM";
        String timeEstimate = "1 hour";
        String additionalInfo = "Judge prefers early hearing";
        InterimTypeOfHearing typeOfHearing = InterimTypeOfHearing.FH;

        HearingType expectedHearingType = HearingType.FH;
        Court expectedCourt = mock(Court.class);

        CaseDocument additionalDoc = withAdditionDoc ? spy(CaseDocument.class) : null;

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
        if (withAdditionDoc) {
            assertThat(result.getAdditionalHearingDocs())
                .extracting(DocumentCollectionItem::getValue)
                .containsExactly(additionalDoc);
        } else {
            assertThat(result.getAdditionalHearingDocs()).isNull();
        }
    }

    @Test
    void shouldConvertGeneralApplicationWrapperToHearing() {
        // Arrange
        LocalDate hearingDate = LocalDate.of(2025, 7, 3);
        String hearingTime = "10:45 AM";
        String timeEstimate = "1 hour";
        String additionalInfo = "Judge prefers early hearing";

        HearingType expectedHearingType = HearingType.APPLICATION_HEARING;
        Court expectedCourt = mock(Court.class);

        GeneralApplicationWrapper generalApplicationWrapper = spy(GeneralApplicationWrapper.builder()
            .generalApplicationDirectionsHearingDate(hearingDate)
            .generalApplicationDirectionsHearingTime(hearingTime)
            .generalApplicationDirectionsHearingTimeEstimate(timeEstimate)
            .generalApplicationDirectionsAdditionalInformation(additionalInfo)
            .build());
        GeneralApplicationRegionWrapper generalApplicationRegionWrapper = spy(GeneralApplicationRegionWrapper.builder()
            .build());
        when(generalApplicationRegionWrapper.toCourt()).thenReturn(expectedCourt);

        // Act
        Hearing result = underTest.toHearing(generalApplicationWrapper, generalApplicationRegionWrapper);

        // Assert
        assertEquals(hearingDate, result.getHearingDate());
        assertEquals(expectedHearingType, result.getHearingType());
        assertEquals(hearingTime, result.getHearingTime());
        assertEquals(timeEstimate, result.getHearingTimeEstimate());
        assertEquals(additionalInfo, result.getAdditionalHearingInformation());
        assertEquals(expectedCourt, result.getHearingCourtSelection());
        assertEquals(YesOrNo.YES, result.getWasMigrated());
        assertThat(result.getAdditionalHearingDocs()).isNull();
    }

    @Test
    void shouldConvertDirectionDetailToHearing() {
        // Arrange
        LocalDate hearingDate = LocalDate.of(2025, 7, 3);
        String hearingTime = "10:45 AM";
        String timeEstimate = "1 hour";

        HearingType expectedHearingType = HearingType.FH;
        Court expectedCourt = mock(Court.class);

        DirectionDetail directionDetail = DirectionDetail.builder()
            .typeOfHearing(HearingTypeDirection.FH)
            .dateOfHearing(hearingDate)
            .hearingTime(hearingTime)
            .timeEstimate(timeEstimate)
            .localCourt(expectedCourt)
            .build();

        // Act
        Hearing result = underTest.toHearing(directionDetail);

        // Assert
        assertEquals(hearingDate, result.getHearingDate());
        assertEquals(expectedHearingType, result.getHearingType());
        assertEquals(hearingTime, result.getHearingTime());
        assertEquals(timeEstimate, result.getHearingTimeEstimate());
        assertThat(result.getAdditionalHearingInformation()).isNull();
        assertEquals(expectedCourt, result.getHearingCourtSelection());
        assertEquals(YesOrNo.YES, result.getWasMigrated());
        assertThat(result.getAdditionalHearingDocs()).isNull();
    }

    @Test
    void shouldConvertHearingDirectionDetailToHearing() {
        // Arrange
        LocalDate hearingDate = LocalDate.of(2025, 7, 3);
        String hearingTime = "10:45 AM";
        String timeEstimate = "1 hour";

        HearingType expectedHearingType = HearingType.FH;
        Court expectedCourt = mock(Court.class);

        HearingDirectionDetail hearingDirectionDetail = HearingDirectionDetail.builder()
            .typeOfHearing(HearingTypeDirection.FH)
            .dateOfHearing(hearingDate)
            .hearingTime(hearingTime)
            .timeEstimate(timeEstimate)
            .localCourt(expectedCourt)
            .build();

        // Act
        Hearing result = underTest.toHearing(hearingDirectionDetail);

        // Assert
        assertEquals(hearingDate, result.getHearingDate());
        assertEquals(expectedHearingType, result.getHearingType());
        assertEquals(hearingTime, result.getHearingTime());
        assertEquals(timeEstimate, result.getHearingTimeEstimate());
        assertThat(result.getAdditionalHearingInformation()).isNull();
        assertEquals(expectedCourt, result.getHearingCourtSelection());
        assertEquals(YesOrNo.YES, result.getWasMigrated());
        assertThat(result.getAdditionalHearingDocs()).isNull();
    }
}
