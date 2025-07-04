package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.migration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs.HearingTabCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs.HearingTabItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.InterimWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.MhMigrationWrapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListForInterimHearingWrapperPopulatorTest {

    @Mock
    private HearingTabItemAppender hearingTabItemAppender;

    @InjectMocks
    private ListForInterimHearingWrapperPopulator underTest;

    @Test
    void testShouldPopulate() {
        assertThat(underTest.shouldPopulate(FinremCaseData.builder().build()))
            .isEqualTo(false);
        assertThat(underTest.shouldPopulate(FinremCaseData.builder().ccdCaseType(CaseType.CONSENTED).build()))
            .isEqualTo(false);
        assertThat(underTest.shouldPopulate(FinremCaseData.builder().ccdCaseType(CaseType.CONTESTED).build()))
            .isEqualTo(false);
        assertThat(underTest.shouldPopulate(FinremCaseData.builder().ccdCaseType(CaseType.CONTESTED)
            .mhMigrationWrapper(MhMigrationWrapper.builder().isListForInterimHearingsMigrated(YesOrNo.YES).build())
            .build()))
            .isEqualTo(false);
        assertThat(underTest.shouldPopulate(FinremCaseData.builder().ccdCaseType(CaseType.CONTESTED)
            .mhMigrationWrapper(MhMigrationWrapper.builder().isListForInterimHearingsMigrated(YesOrNo.NO).build())
            .build()))
            .isEqualTo(false);
        assertThat(underTest.shouldPopulate(FinremCaseData.builder().ccdCaseType(CaseType.CONTESTED)
            .mhMigrationWrapper(MhMigrationWrapper.builder().isListForInterimHearingsMigrated(YesOrNo.NO).build())
            .interimWrapper(InterimWrapper.builder().interimHearings(List.of(InterimHearingCollection.builder().build())).build())
            .build()))
            .isEqualTo(true);
    }

    @Test
    void shouldPopulateCaseDataCorrectly() {
        // Arrange
        InterimHearingItem interimHearingItem = mock(InterimHearingItem.class);
        FinremCaseData caseData = FinremCaseData.builder()
            .interimWrapper(InterimWrapper.builder()
                .interimHearings(List.of(
                    InterimHearingCollection.builder().value(interimHearingItem).build()
                ))
                .build())
            .build();

        HearingTabItem hearingTabItem = mock(HearingTabItem.class);
        Hearing hearing = mock(Hearing.class);

        when(hearingTabItemAppender.toHearingTabItem(interimHearingItem)).thenReturn(hearingTabItem);
        when(hearingTabItemAppender.toHearing(interimHearingItem)).thenReturn(hearing);

        // Act
        underTest.populate(caseData);

        // Assert
        verify(hearingTabItemAppender).appendToHearingTabItems(eq(caseData), eq(HearingTabCollectionItem.builder().value(hearingTabItem).build()));
        verify(hearingTabItemAppender).appendToHearings(eq(caseData), eq(ManageHearingsCollectionItem.builder().value(hearing).build()));
        assertEquals(YesOrNo.YES, caseData.getMhMigrationWrapper().getIsListForInterimHearingsMigrated());
    }
}
