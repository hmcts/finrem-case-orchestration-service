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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingDirectionDetail;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingDirectionDetailsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.PartyOnCase;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.PartyOnCaseCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.MhMigrationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PartyService;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogs;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.anySupplier;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.hearing;

@ExtendWith(MockitoExtension.class)
class HearingDirectionDetailsCollectionPopulatorTest {

    @TestLogs
    private final TestLogger logger = new TestLogger(HearingDirectionDetailsCollectionPopulator.class);

    @Mock
    private HearingsAppender hearingsAppender;

    @Mock
    private PartyService partyService;

    @InjectMocks
    private HearingDirectionDetailsCollectionPopulator underTest;

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

        // event hearingDirectionDetailsCollection was not set or empty
        assertThat(underTest.shouldPopulate(FinremCaseData.builder().ccdCaseType(CaseType.CONTESTED).build()))
            .isTrue();
        assertThat(underTest.shouldPopulate(FinremCaseData.builder().ccdCaseType(CaseType.CONTESTED)
            .hearingDirectionDetailsCollection(Collections.emptyList())
            .build()))
            .isTrue();

        logger.reset();
        assertThat(underTest.shouldPopulate(FinremCaseData.builder().ccdCaseId(CASE_ID).ccdCaseType(CaseType.CONTESTED)
            .mhMigrationWrapper(MhMigrationWrapper.builder().isHearingDirectionDetailsCollectionMigrated(YesOrNo.YES).build())
            .build()))
            .isFalse();
        assertThat(logger.getInfos()).containsExactly("1234567890 - Skip populate because migration had been done.");

        assertThat(underTest.shouldPopulate(FinremCaseData.builder().ccdCaseType(CaseType.CONTESTED)
            .mhMigrationWrapper(MhMigrationWrapper.builder().isGeneralApplicationMigrated(YesOrNo.NO).build())
            .build()))
            .isTrue();
    }

    @Test
    void shouldPopulateCaseDataCorrectly() {
        // Arrange
        HearingDirectionDetail hearingReq1 = HearingDirectionDetail.builder().isAnotherHearingYN(YesOrNo.YES).build();
        HearingDirectionDetail hearingReq2 = HearingDirectionDetail.builder().isAnotherHearingYN(YesOrNo.NO).build();
        HearingDirectionDetail hearingReq3 = HearingDirectionDetail.builder().isAnotherHearingYN(null).build();

        Hearing newHearing1 = hearing("11:00");
        Hearing newHearing2 = hearing("12:00");
        Hearing newHearing3 = hearing("13:00");

        FinremCaseData caseData = FinremCaseData.builder()
            .hearingDirectionDetailsCollection(List.of(
                HearingDirectionDetailsCollection.builder().value(hearingReq1).build(),
                HearingDirectionDetailsCollection.builder().value(hearingReq2).build(),
                HearingDirectionDetailsCollection.builder().value(hearingReq3).build()
            ))
            .build();

        DynamicMultiSelectList allActivePartyList = DynamicMultiSelectList.builder()
            .value(List.of(
                DynamicMultiSelectListElement.builder()
                    .code("[APPSOLICITOR]")
                    .label("Applicant Solicitor - Hamzah")
                    .build()
            ))
            .build();
        when(partyService.getAllActivePartyList(caseData)).thenReturn(allActivePartyList);
        when(hearingsAppender.toHearing(hearingReq1)).thenReturn(newHearing1);
        lenient().when(hearingsAppender.toHearing(hearingReq2)).thenReturn(newHearing2);
        lenient().when(hearingsAppender.toHearing(hearingReq3)).thenReturn(newHearing3);
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

        assertEquals(YesOrNo.YES, caseData.getMhMigrationWrapper().getIsHearingDirectionDetailsCollectionMigrated());
        assertThat(caseData.getManageHearingsWrapper().getHearings())
            .hasSize(1)
            .extracting(ManageHearingsCollectionItem::getValue)
            .extracting(Hearing::getWasMigrated, Hearing::getPartiesOnCase, Hearing::getHearingTime)
            .containsOnly(tuple(YesOrNo.YES, expectedParties, "11:00"));
    }
}
