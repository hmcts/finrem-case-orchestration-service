package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.migration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingTypeDirection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs.HearingTabCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs.HearingTabItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ListForHearingWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.MhMigrationWrapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListForHearingWrapperPopulatorTest {

    @Mock
    private HearingTabItemsAppender hearingTabItemsAppender;

    @InjectMocks
    private ListForHearingWrapperPopulator underTest;

    @Test
    void testShouldPopulate() {
        assertThat(underTest.shouldPopulate(FinremCaseData.builder().build()))
            .isEqualTo(false);
        assertThat(underTest.shouldPopulate(FinremCaseData.builder().ccdCaseType(CaseType.CONSENTED).build()))
            .isEqualTo(false);
        assertThat(underTest.shouldPopulate(FinremCaseData.builder().ccdCaseType(CaseType.CONTESTED).build()))
            .isEqualTo(false);
        assertThat(underTest.shouldPopulate(FinremCaseData.builder().ccdCaseType(CaseType.CONTESTED)
            .mhMigrationWrapper(MhMigrationWrapper.builder().isListForHearingsMigrated(YesOrNo.YES).build())
            .build()))
            .isEqualTo(false);
        assertThat(underTest.shouldPopulate(FinremCaseData.builder().ccdCaseType(CaseType.CONTESTED)
            .mhMigrationWrapper(MhMigrationWrapper.builder().isListForHearingsMigrated(YesOrNo.NO).build())
            .build()))
            .isEqualTo(false);
        HearingTypeDirection mockedHearingTypeDirection = mock(HearingTypeDirection.class);
        assertThat(underTest.shouldPopulate(FinremCaseData.builder().ccdCaseType(CaseType.CONTESTED)
            .mhMigrationWrapper(MhMigrationWrapper.builder().isListForHearingsMigrated(YesOrNo.NO).build())
            .listForHearingWrapper(ListForHearingWrapper.builder().hearingType(mockedHearingTypeDirection).build())
            .build()))
            .isEqualTo(true);
    }

    @Test
    void shouldPopulateCaseDataCorrectly() {
        // Arrange
        ListForHearingWrapper listForHearingWrapper = mock(ListForHearingWrapper.class);
        FinremCaseData caseData = FinremCaseData.builder()
            .listForHearingWrapper(listForHearingWrapper)
            .build();

        HearingTabItem hearingTabItem = mock(HearingTabItem.class);
        Hearing hearing = mock(Hearing.class);

        when(hearingTabItemsAppender.toHearingTabItem(listForHearingWrapper)).thenReturn(hearingTabItem);
        when(hearingTabItemsAppender.toHearing(listForHearingWrapper)).thenReturn(hearing);

        // Act
        underTest.populate(caseData);

        // Assert
        verify(hearingTabItemsAppender).appendToHearingTabItems(eq(caseData), eq(HearingTabCollectionItem.builder().value(hearingTabItem).build()));
        verify(hearingTabItemsAppender).appendToHearings(eq(caseData), eq(ManageHearingsCollectionItem.builder().value(hearing).build()));
        assertEquals(YesOrNo.YES, caseData.getMhMigrationWrapper().getIsListForHearingsMigrated());
    }
}
