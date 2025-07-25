package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.migration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingTypeDirection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ListForHearingWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.MhMigrationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PartyService;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.anySupplier;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.hearing;

@ExtendWith(MockitoExtension.class)
class ListForHearingWrapperPopulatorTest {

    @TestLogs
    private final TestLogger logger = new TestLogger(ListForHearingWrapperPopulator.class);

    @Mock
    private HearingsAppender hearingsAppender;

    @Mock
    private PartyService partyService;

    @InjectMocks
    private ListForHearingWrapperPopulator underTest;

    @Test
    void testShouldPopulate() {
        assertThat(underTest.shouldPopulate(FinremCaseData.builder().build()))
            .isFalse();
        assertThat(logger.getInfos()).containsExactly("null - Skip populate because it's not a contested application.");

        logger.reset();
        assertThat(underTest.shouldPopulate(FinremCaseData.builder().ccdCaseType(CaseType.CONSENTED).build()))
            .isFalse();
        assertThat(logger.getInfos()).containsExactly("null - Skip populate because it's not a contested application.");

        logger.reset();
        assertThat(underTest.shouldPopulate(FinremCaseData.builder().ccdCaseId(CASE_ID).ccdCaseType(CaseType.CONSENTED).build()))
            .isFalse();
        assertThat(logger.getInfos()).containsExactly("1234567890 - Skip populate because it's not a contested application.");

        assertThat(underTest.shouldPopulate(FinremCaseData.builder().ccdCaseType(CaseType.CONTESTED).build()))
            .isFalse();

        logger.reset();
        assertThat(underTest.shouldPopulate(FinremCaseData.builder().ccdCaseId(CASE_ID).ccdCaseType(CaseType.CONTESTED)
            .mhMigrationWrapper(MhMigrationWrapper.builder().isListForHearingsMigrated(YesOrNo.YES).build())
            .build()))
            .isFalse();
        assertThat(logger.getInfos()).containsExactly("1234567890 - Skip populate because migration had been done.");

        logger.reset();
        assertThat(underTest.shouldPopulate(FinremCaseData.builder().ccdCaseId(CASE_ID).ccdCaseType(CaseType.CONTESTED)
            .mhMigrationWrapper(MhMigrationWrapper.builder().isListForHearingsMigrated(YesOrNo.NO).build())
            .build()))
            .isFalse();
        assertThat(logger.getInfos()).containsExactly("1234567890 - Skip populate because hearing type is null.");

        HearingTypeDirection mockedHearingTypeDirection = mock(HearingTypeDirection.class);
        assertThat(underTest.shouldPopulate(FinremCaseData.builder().ccdCaseType(CaseType.CONTESTED)
            .mhMigrationWrapper(MhMigrationWrapper.builder().isListForHearingsMigrated(YesOrNo.NO).build())
            .listForHearingWrapper(ListForHearingWrapper.builder().hearingType(mockedHearingTypeDirection).build())
            .build()))
            .isTrue();
    }

    @Test
    void shouldPopulateCaseDataCorrectly() {
        // Arrange
        ListForHearingWrapper listForHearingWrapper = mock(ListForHearingWrapper.class);
        FinremCaseData caseData = FinremCaseData.builder()
            .listForHearingWrapper(listForHearingWrapper)
            .build();

        Hearing hearing = hearing("10:00");
        DynamicMultiSelectList allActivePartyList = mock(DynamicMultiSelectList.class);
        when(partyService.getAllActivePartyList(caseData)).thenReturn(allActivePartyList);
        when(hearingsAppender.toHearing(listForHearingWrapper)).thenReturn(hearing);
        doCallRealMethod().when(hearingsAppender).appendToHearings(eq(caseData), anySupplier());

        // Act
        underTest.populate(caseData);

        // Assert
        verify(partyService).getAllActivePartyList(caseData);

        assertEquals(YesOrNo.YES, caseData.getMhMigrationWrapper().getIsListForHearingsMigrated());
        assertThat(caseData.getManageHearingsWrapper().getHearings())
            .hasSize(1)
            .extracting(ManageHearingsCollectionItem::getValue)
            .extracting(Hearing::getWasMigrated, Hearing::getPartiesOnCaseMultiSelectList, Hearing::getHearingTime)
            .containsOnly(tuple(YesOrNo.YES, allActivePartyList, "10:00"));
    }
}
