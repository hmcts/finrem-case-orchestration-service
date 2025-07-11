package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.migration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetail;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetailCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.MhMigrationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogs;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;

@ExtendWith(MockitoExtension.class)
class DirectionDetailsCollectionPopulatorTest {

    @TestLogs
    private final TestLogger logger = new TestLogger(DirectionDetailsCollectionPopulator.class);

    @Mock
    private HearingsAppender hearingsAppender;

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
        DirectionDetail hearing1 = DirectionDetail.builder().isAnotherHearingYN(YesOrNo.YES).build();
        DirectionDetail hearing2 = DirectionDetail.builder().isAnotherHearingYN(YesOrNo.NO).build();
        DirectionDetail hearing3 = DirectionDetail.builder().isAnotherHearingYN(null).build();

        Hearing newHearing1 = mock(Hearing.class);
        Hearing newHearing2 = mock(Hearing.class);
        Hearing newHearing3 = mock(Hearing.class);

        when(hearingsAppender.toHearing(hearing1)).thenReturn(newHearing1);
        lenient().when(hearingsAppender.toHearing(hearing2)).thenReturn(newHearing2);
        lenient().when(hearingsAppender.toHearing(hearing3)).thenReturn(newHearing3);

        FinremCaseData caseData = FinremCaseData.builder()
            .directionDetailsCollection(List.of(
                DirectionDetailCollection.builder().value(hearing1).build(),
                DirectionDetailCollection.builder().value(hearing2).build(),
                DirectionDetailCollection.builder().value(hearing3).build()
            ))
            .build();

        // Act
        underTest.populate(caseData);

        // Assert
        verify(hearingsAppender).appendToHearings(eq(caseData), eq(ManageHearingsCollectionItem.builder().value(newHearing1).build()));
        verify(hearingsAppender, never()).appendToHearings(eq(caseData), eq(ManageHearingsCollectionItem.builder().value(newHearing2).build()));
        verify(hearingsAppender, never()).appendToHearings(eq(caseData), eq(ManageHearingsCollectionItem.builder().value(newHearing3).build()));

        assertEquals(YesOrNo.YES, caseData.getMhMigrationWrapper().getIsDirectionDetailsCollectionMigrated());
    }
}
