package uk.gov.hmcts.reform.finrem.caseorchestration.handler.helper.managehearings;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.managehearings.HearingCorrespondenceHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PaperNotificationService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HearingCorrespondenceHelperTest {

    @Mock
    PaperNotificationService paperNotificationService;

    @InjectMocks
    private HearingCorrespondenceHelper helper;

    @Test
    void shouldReturnCorrectValuesWhenShouldSendNotificationUsed() {
        Hearing hearing = mock(Hearing.class);

        when(hearing.getHearingNoticePrompt()).thenReturn(YesOrNo.NO);
        assertTrue(helper.shouldNotSendNotification(hearing));

        when(hearing.getHearingNoticePrompt()).thenReturn(YesOrNo.YES);
        assertFalse(helper.shouldNotSendNotification(hearing));

        when(hearing.getHearingNoticePrompt()).thenReturn(null);
        assertTrue(helper.shouldNotSendNotification(hearing));
    }

    @Test
    void shouldReturnHearingInContextWhenIdMatches() {
        UUID hearingId = UUID.randomUUID();
        Hearing hearing = new Hearing();
        ManageHearingsWrapper wrapper = new ManageHearingsWrapper();
        wrapper.setHearings(List.of(new ManageHearingsCollectionItem(hearingId, hearing)));
        wrapper.setWorkingHearingId(hearingId);

        FinremCaseData caseData = new FinremCaseData();
        caseData.setManageHearingsWrapper(wrapper);

        Hearing result = helper.getHearingInContext(caseData);

        assertEquals(hearing, result);
    }

    @Test
    void shouldThrowExceptionWhenHearingIdNotFound() {
        UUID hearingId = UUID.randomUUID();
        UUID nonMatchingWorkingHearingId = UUID.randomUUID();

        Hearing hearing = new Hearing();
        ManageHearingsWrapper wrapper = new ManageHearingsWrapper();
        wrapper.setHearings(List.of(new ManageHearingsCollectionItem(hearingId, hearing)));
        wrapper.setWorkingHearingId(nonMatchingWorkingHearingId);

        FinremCaseData caseData = new FinremCaseData();
        caseData.setManageHearingsWrapper(wrapper);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            helper.getHearingInContext(caseData));

        assertTrue(exception.getMessage().contains("Hearing not found for the given ID"));
    }

    @Test
    void shouldThrowExceptionWhenGetHearingInContextCalledForEmptyHearingCollection() {
        UUID hearingId = UUID.randomUUID();

        ManageHearingsWrapper wrapper = new ManageHearingsWrapper();
        wrapper.setWorkingHearingId(hearingId);

        FinremCaseData caseData = new FinremCaseData();
        caseData.setManageHearingsWrapper(wrapper);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            helper.getHearingInContext(caseData));

        assertTrue(exception.getMessage().contains(
            String.format("No hearings available to search for. Working hearing ID is: %s",
                hearingId)));
    }

    /**
     * shouldEmailToApplicantSolicitor is just a wrapper around the paperNotificationService.shouldPrintForApplicant.
     */
    @Test
    void shouldReturnCorrectValueForShouldEmailToApplicantSolicitor() {
        FinremCaseDetails finremCaseDetails = mock(FinremCaseDetails.class);

        when(paperNotificationService.shouldPrintForApplicant(finremCaseDetails)).thenReturn(false);
        assertTrue(helper.shouldEmailToApplicantSolicitor(finremCaseDetails));

        when(paperNotificationService.shouldPrintForApplicant(finremCaseDetails)).thenReturn(true);
        assertFalse(helper.shouldEmailToApplicantSolicitor(finremCaseDetails));
    }

    /**
     * shouldEmailToRespondentSolicitor is just a wrapper around the paperNotificationService.shouldPrintForRespondent.
     */
    @Test
    void shouldReturnCorrectValueForShouldEmailToRespondentSolicitor() {
        FinremCaseDetails finremCaseDetails = mock(FinremCaseDetails.class);

        when(paperNotificationService.shouldPrintForRespondent(finremCaseDetails)).thenReturn(false);
        assertTrue(helper.shouldEmailToRespondentSolicitor(finremCaseDetails));

        when(paperNotificationService.shouldPrintForRespondent(finremCaseDetails)).thenReturn(true);
        assertFalse(helper.shouldEmailToRespondentSolicitor(finremCaseDetails));
    }

    /**
     * shouldPostToApplicant is just a wrapper around the paperNotificationService.shouldPrintForApplicant.
     */
    @Test
    void shouldReturnCorrectValueForShouldPostToApplicant() {
        FinremCaseDetails finremCaseDetails = mock(FinremCaseDetails.class);

        when(paperNotificationService.shouldPrintForApplicant(finremCaseDetails)).thenReturn(true);
        assertTrue(helper.shouldPostToApplicant(finremCaseDetails));

        when(paperNotificationService.shouldPrintForApplicant(finremCaseDetails)).thenReturn(false);
        assertFalse(helper.shouldPostToApplicant(finremCaseDetails));
    }

    /**
     * shouldPostToRespondent is just a wrapper around the paperNotificationService.shouldPrintForRespondent.
     */
    @Test
    void shouldReturnCorrectValueForShouldPostToRespondent() {
        FinremCaseDetails finremCaseDetails = mock(FinremCaseDetails.class);

        when(paperNotificationService.shouldPrintForRespondent(finremCaseDetails)).thenReturn(true);
        assertTrue(helper.shouldPostToRespondent(finremCaseDetails));

        when(paperNotificationService.shouldPrintForRespondent(finremCaseDetails)).thenReturn(false);
        assertFalse(helper.shouldPostToRespondent(finremCaseDetails));
    }

    /**
     * Tests happy path for shouldSendHearingNoticeOnly::
     * - Action is ADD_HEARING
     * - Hearing type comes from a stream of arguments that should all be valid.
     */
    @ParameterizedTest
    @MethodSource("provideNoticeOnlyCases")
    void shouldSendHearingNoticeOnlyReturnsTrue(Hearing hearing) {
        // Arrange case
        FinremCaseDetails finremCaseDetails = finremCaseDetails(ManageHearingsAction.ADD_HEARING);
        // Act
        boolean result = helper.shouldSendHearingNoticeOnly(finremCaseDetails, hearing);
        // Assert
        assertTrue(result);
    }

    /**
     * Used by shouldSendHearingNoticeOnlyReturnsTrue.
     *
     * @return Stream of Arguments all of which should cause shouldSendHearingNoticeOnly to return True
     */
    static Stream<Arguments> provideNoticeOnlyCases() {
        return Stream.of(
            arguments(
                Hearing.builder().hearingType(HearingType.MPS).build()
            ),
            arguments(
                Hearing.builder().hearingType(HearingType.FH).build()
            ),
            arguments(
                Hearing.builder().hearingType(HearingType.DIR).build()
            ),
            arguments(
                Hearing.builder().hearingType(HearingType.MENTION).build()
            ),
            arguments(
                Hearing.builder().hearingType(HearingType.PERMISSION_TO_APPEAL).build()
            ),
            arguments(
                Hearing.builder().hearingType(HearingType.APPEAL_HEARING).build()
            ),
            arguments(
                Hearing.builder().hearingType(HearingType.RETRIAL_HEARING).build()
            ),
            arguments(
                Hearing.builder().hearingType(HearingType.PTR).build()
            )
        );
    }

    /**
     * Confirms that shouldSendHearingNoticeOnly returns false for the passes arguments.
     */
    @ParameterizedTest
    @MethodSource("provideInvalidNoticeOnlyCases")
    void shouldSendHearingNoticeOnlyReturnsFalse(FinremCaseDetails finremCaseDetails, Hearing hearing) {
        // Act
        boolean result = helper.shouldSendHearingNoticeOnly(finremCaseDetails, hearing);
        // Assert
        assertFalse(result);
    }

    /**
     * Used by shouldSendHearingNoticeOnlyReturnsFalse.
     *
     * @return Stream of Arguments all of which should cause shouldSendHearingNoticeOnly to return False
     */
    static Stream<Arguments> provideInvalidNoticeOnlyCases() {
        return Stream.of(
            // Case: null ManageHearingsAction, hearing type is in list
            arguments(
                finremCaseDetails(null),
                Hearing.builder().hearingType(HearingType.MPS).build()
            ),
            // Case: action is ADD_HEARING, which is valid. However, hearing type is wrong.
            arguments(
                finremCaseDetails(ManageHearingsAction.ADD_HEARING),
                Hearing.builder().hearingType(HearingType.FDR).build()
            ),
            arguments(
                finremCaseDetails(ManageHearingsAction.ADD_HEARING),
                Hearing.builder().hearingType(HearingType.FDA).build()
            ),
            // Case: valid action, but hearing is null
            arguments(
                finremCaseDetails(ManageHearingsAction.ADD_HEARING),
                null
            )
        );
    }

    private static FinremCaseDetails finremCaseDetails(ManageHearingsAction action) {
        return FinremCaseDetails.builder()
            .data(FinremCaseData.builder()
                .manageHearingsWrapper(ManageHearingsWrapper.builder()
                    .manageHearingsActionSelection(action)
                    .build())
                .build())
            .build();
    }
}
