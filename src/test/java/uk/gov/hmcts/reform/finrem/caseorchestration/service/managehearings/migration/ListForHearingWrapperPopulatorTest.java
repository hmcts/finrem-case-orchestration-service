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
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;

@ExtendWith(MockitoExtension.class)
class ListForHearingWrapperPopulatorTest {

    @TestLogs
    private final TestLogger logger = new TestLogger(ListForHearingWrapperPopulator.class);

    @Mock
    private HearingsAppender hearingsAppender;

    @Mock
    private HearingTabItemsAppender hearingTabItemsAppender;

    @InjectMocks
    private ListForHearingWrapperPopulator underTest;

    @Test
    void testShouldPopulate() {
        assertThat(underTest.shouldPopulate(FinremCaseData.builder().build()))
            .isEqualTo(false);
        assertThat(logger.getInfos()).containsExactly("null - Skip populate because it's not a contested application.");

        logger.reset();
        assertThat(underTest.shouldPopulate(FinremCaseData.builder().ccdCaseType(CaseType.CONSENTED).build()))
            .isEqualTo(false);
        assertThat(logger.getInfos()).containsExactly("null - Skip populate because it's not a contested application.");

        logger.reset();
        assertThat(underTest.shouldPopulate(FinremCaseData.builder().ccdCaseId(CASE_ID).ccdCaseType(CaseType.CONSENTED).build()))
            .isEqualTo(false);
        assertThat(logger.getInfos()).containsExactly("1234567890 - Skip populate because it's not a contested application.");

        assertThat(underTest.shouldPopulate(FinremCaseData.builder().ccdCaseType(CaseType.CONTESTED).build()))
            .isEqualTo(false);

        logger.reset();
        assertThat(underTest.shouldPopulate(FinremCaseData.builder().ccdCaseId(CASE_ID).ccdCaseType(CaseType.CONTESTED)
            .mhMigrationWrapper(MhMigrationWrapper.builder().isListForHearingsMigrated(YesOrNo.YES).build())
            .build()))
            .isEqualTo(false);
        assertThat(logger.getInfos()).containsExactly("1234567890 - Skip populate because migration had been done.");

        logger.reset();
        assertThat(underTest.shouldPopulate(FinremCaseData.builder().ccdCaseId(CASE_ID).ccdCaseType(CaseType.CONTESTED)
            .mhMigrationWrapper(MhMigrationWrapper.builder().isListForHearingsMigrated(YesOrNo.NO).build())
            .build()))
            .isEqualTo(false);
        assertThat(logger.getInfos()).containsExactly("1234567890 - Skip populate because hearing type is null.");

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
        when(hearingsAppender.toHearing(listForHearingWrapper)).thenReturn(hearing);

        // Act
        underTest.populate(caseData);

        // Assert
        verify(hearingTabItemsAppender).appendToHearingTabItems(eq(caseData), eq(HearingTabCollectionItem.builder().value(hearingTabItem).build()));
        verify(hearingsAppender).appendToHearings(eq(caseData), eq(ManageHearingsCollectionItem.builder().value(hearing).build()));
        assertEquals(YesOrNo.YES, caseData.getMhMigrationWrapper().getIsListForHearingsMigrated());
    }
}
