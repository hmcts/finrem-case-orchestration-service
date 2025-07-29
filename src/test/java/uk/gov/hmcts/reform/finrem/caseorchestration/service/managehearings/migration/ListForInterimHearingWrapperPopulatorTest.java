package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.migration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.PartyOnCase;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.PartyOnCaseCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.InterimWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.MhMigrationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PartyService;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogs;

import java.util.List;

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
class ListForInterimHearingWrapperPopulatorTest {

    @TestLogs
    private final TestLogger logger = new TestLogger(ListForInterimHearingWrapperPopulator.class);

    @Mock
    private HearingsAppender hearingsAppender;

    @Mock
    private PartyService partyService;

    @InjectMocks
    private ListForInterimHearingWrapperPopulator underTest;

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
            .mhMigrationWrapper(MhMigrationWrapper.builder().isListForInterimHearingsMigrated(YesOrNo.YES).build())
            .build()))
            .isEqualTo(false);
        assertThat(logger.getInfos()).containsExactly("1234567890 - Skip populate because migration had been done.");

        logger.reset();
        assertThat(underTest.shouldPopulate(FinremCaseData.builder().ccdCaseId(CASE_ID).ccdCaseType(CaseType.CONTESTED)
            .mhMigrationWrapper(MhMigrationWrapper.builder().isListForInterimHearingsMigrated(YesOrNo.NO).build())
            .build()))
            .isEqualTo(false);
        assertThat(logger.getInfos()).containsExactly("1234567890 - Skip populate because collection \"interimHearings\" is empty.");

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
        InterimHearingItem interimHearingItemTwo = mock(InterimHearingItem.class);
        FinremCaseData caseData = FinremCaseData.builder()
            .interimWrapper(InterimWrapper.builder()
                .interimHearings(List.of(
                    InterimHearingCollection.builder().value(interimHearingItem).build(),
                    InterimHearingCollection.builder().value(interimHearingItemTwo).build()
                ))
                .build())
            .build();

        Hearing hearing = hearing("10:00");
        Hearing hearingTwo = hearing("11:00");
        DynamicMultiSelectList allActivePartyList = DynamicMultiSelectList.builder()
            .value(List.of(
                DynamicMultiSelectListElement.builder()
                    .code("[APPSOLICITOR]")
                    .label("Applicant Solicitor - Hamzah")
                    .build()
            ))
            .build();
        when(partyService.getAllActivePartyList(caseData)).thenReturn(allActivePartyList);
        when(hearingsAppender.toHearing(interimHearingItem)).thenReturn(hearing);
        when(hearingsAppender.toHearing(interimHearingItemTwo)).thenReturn(hearingTwo);
        doCallRealMethod().when(hearingsAppender).appendToHearings(eq(caseData), anySupplier());

        // Act
        underTest.populate(caseData);

        // Assert
        List<PartyOnCaseCollectionItem> expectedParties = List.of(
            PartyOnCaseCollectionItem.builder()
                .value(PartyOnCase.builder()
                    .role("[APPSOLICITOR]")
                    .label("Applicant Solicitor - Hamzah")
                    .build())
                .build()
        );
        verify(partyService).getAllActivePartyList(caseData);

        assertEquals(YesOrNo.YES, caseData.getMhMigrationWrapper().getIsListForInterimHearingsMigrated());
        assertThat(caseData.getManageHearingsWrapper().getHearings())
            .hasSize(2)
            .extracting(ManageHearingsCollectionItem::getValue)
            .extracting(Hearing::getWasMigrated, Hearing::getPartiesOnCase, Hearing::getHearingTime)
            .containsOnly(tuple(YesOrNo.YES, expectedParties, "10:00"),
                tuple(YesOrNo.YES, expectedParties, "11:00"));
    }
}
