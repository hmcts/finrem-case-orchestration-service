package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.migration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetail;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetailCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.PartyOnCase;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.PartyOnCaseCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.MhMigrationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PartyService;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogs;

import java.time.LocalDate;
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
class DirectionDetailsCollectionPopulatorTest {

    @TestLogs
    private final TestLogger logger = new TestLogger(DirectionDetailsCollectionPopulator.class);

    @Mock
    private HearingsAppender hearingsAppender;

    @Mock
    private PartyService partyService;

    @InjectMocks
    private DirectionDetailsCollectionPopulator underTest;

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

        // event directionDetailsCollection was not set or empty
        assertThat(underTest.shouldPopulate(FinremCaseData.builder().ccdCaseType(CaseType.CONTESTED).build()))
            .isEqualTo(true);
        assertThat(underTest.shouldPopulate(FinremCaseData.builder().ccdCaseType(CaseType.CONTESTED)
            .directionDetailsCollection(Collections.emptyList()).build()))
            .isEqualTo(true);

        logger.reset();
        assertThat(underTest.shouldPopulate(FinremCaseData.builder().ccdCaseId(CASE_ID).ccdCaseType(CaseType.CONTESTED)
            .mhMigrationWrapper(MhMigrationWrapper.builder().isDirectionDetailsCollectionMigrated(YesOrNo.YES).build())
            .build()))
            .isEqualTo(false);
        assertThat(logger.getInfos()).containsExactly("1234567890 - Skip populate because migration had been done.");

        assertThat(underTest.shouldPopulate(FinremCaseData.builder().ccdCaseType(CaseType.CONTESTED)
            .mhMigrationWrapper(MhMigrationWrapper.builder().isGeneralApplicationMigrated(YesOrNo.NO).build())
            .generalApplicationWrapper(GeneralApplicationWrapper.builder().generalApplicationDirectionsHearingDate(LocalDate.now()).build())
            .build()))
            .isEqualTo(true);
    }

    @Test
    void shouldPopulateCaseDataCorrectly() {
        // Arrange
        DirectionDetail hearingReq1 = DirectionDetail.builder().isAnotherHearingYN(YesOrNo.YES).build();
        DirectionDetail hearingReq2 = DirectionDetail.builder().isAnotherHearingYN(YesOrNo.NO).build();
        DirectionDetail hearingReq3 = DirectionDetail.builder().isAnotherHearingYN(null).build();

        Hearing newHearing1 = hearing("11:00");
        Hearing newHearing2 = hearing("12:00");
        Hearing newHearing3 = hearing("13:00");

        FinremCaseData caseData = FinremCaseData.builder()
            .directionDetailsCollection(List.of(
                DirectionDetailCollection.builder().value(hearingReq1).build(),
                DirectionDetailCollection.builder().value(hearingReq2).build(),
                DirectionDetailCollection.builder().value(hearingReq3).build()
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
        List<PartyOnCaseCollection> expectedParties = List.of(
            PartyOnCaseCollection.builder()
                .value(PartyOnCase.builder()
                    .role("[APPSOLICITOR]")
                    .label("Applicant Solicitor - Hamzah")
                    .build())
                .build()
        );
        verify(partyService).getAllActivePartyList(caseData);

        assertEquals(YesOrNo.YES, caseData.getMhMigrationWrapper().getIsDirectionDetailsCollectionMigrated());
        assertThat(caseData.getManageHearingsWrapper().getHearings())
            .hasSize(1)
            .extracting(ManageHearingsCollectionItem::getValue)
            .extracting(Hearing::getWasMigrated, Hearing::getPartiesOnCase, Hearing::getHearingTime)
            .containsOnly(tuple(YesOrNo.YES, expectedParties, "11:00"));
    }
}
