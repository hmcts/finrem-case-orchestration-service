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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.express.ExpressCaseService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HearingCorrespondenceHelperTest {

    @Mock
    PaperNotificationService paperNotificationService;

    @Mock
    ExpressCaseService expressCaseService;

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
     * Tests true path for shouldSendHearingNoticeOnly::
     * - Action is ADD_HEARING
     * - Hearing type comes from a stream of arguments that should all be valid.
     * When a case needs documents posted, shouldPostHearingNoticeOnly decides if only the notice needs posting.
     */
    @ParameterizedTest
    @MethodSource("provideNoticeOnlyCases")
    void shouldPostHearingNoticeOnlyReturnsTrue(Hearing hearing) {
        // Arrange case
        FinremCaseDetails finremCaseDetails = finremCaseDetails(ManageHearingsAction.ADD_HEARING);
        lenient().when(expressCaseService.isExpressCase(finremCaseDetails.getData())).thenReturn(false);
        // Act
        boolean result = helper.shouldPostHearingNoticeOnly(finremCaseDetails, hearing);
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
            ),
            // Test should mock that this FDR is not an express case to pass.
            arguments(
                Hearing.builder().hearingType(HearingType.FDR).build()
            )
        );
    }

    /**
     * Tests false path for shouldSendHearingNoticeOnly::
     * - Action is ADD_HEARING
     * - Hearing type comes from a stream of arguments that should all be invalid.
     * When a case needs documents posted, shouldPostHearingNoticeOnly decides if only the notice needs posting.
     */
    @ParameterizedTest
    @MethodSource("provideInvalidNoticeOnlyCases")
    void shouldPostHearingNoticeOnlyReturnsFalse(FinremCaseDetails finremCaseDetails, Hearing hearing) {
        // Arrange case
        lenient().when(expressCaseService.isExpressCase(finremCaseDetails.getData())).thenReturn(true);
        // Act
        boolean result = helper.shouldPostHearingNoticeOnly(finremCaseDetails, hearing);
        // Assert
        assertFalse(result);
    }

    /**
     * Used by shouldSendHearingNoticeOnlyReturnsTrue.
     *
     * @return Stream of Arguments all of which should cause shouldSendHearingNoticeOnly to return True
     */
    static Stream<Arguments> provideInvalidNoticeOnlyCases() {
        return Stream.of(
            // Case: invalid ManageHearingsAction, hearing type is OK
            arguments(
                finremCaseDetails(null), Hearing.builder().hearingType(HearingType.MPS).build()
            ),
            // Case: valid action, but hearing is null
            arguments(
                finremCaseDetails(ManageHearingsAction.ADD_HEARING), null
            ),
            // Case: Hearing type is wrong.
            arguments(
                finremCaseDetails(ManageHearingsAction.ADD_HEARING), Hearing.builder().hearingType(HearingType.FDA).build()
            ),
            // Case: valid action, but FDR invalid for non-express case
            arguments(
                finremCaseDetails(ManageHearingsAction.ADD_HEARING), Hearing.builder().hearingType(HearingType.FDR).build()
            )
        );
    }

    /**
     * Tests happy path for shouldPostAllHearingDocuments::
     * - Action is ADD_HEARING
     * - Hearing type comes from a stream of arguments that should all be valid.
     * When a case needs documents posted, shouldPostAllHearingDocuments decides if all hearing docs need posting.
     */
    @Test
    void shouldPostAllHearingDocumentsReturnsTrue() {
        // Arrange case
        FinremCaseDetails finremCaseDetails = finremCaseDetails(ManageHearingsAction.ADD_HEARING);
        Hearing fdaHearing = Hearing.builder().hearingType(HearingType.FDA).build();
        // This mock is just for the FDR case.  FDR cases that are express have all docs posted.
        lenient().when(expressCaseService.isExpressCase(finremCaseDetails.getData())).thenReturn(true);
        Hearing fdrHearing = Hearing.builder().hearingType(HearingType.FDR).build();

        // Act
        boolean fdaResult = helper.shouldPostAllHearingDocuments(finremCaseDetails, fdaHearing);
        boolean fdrResult = helper.shouldPostAllHearingDocuments(finremCaseDetails, fdrHearing);

        // Assert
        assertTrue(fdaResult);
        assertTrue(fdrResult);
    }

    /**
     * Confirms that shouldPostAllHearingDocuments returns false for the passed arguments.
     * When a case needs documents posted, shouldPostAllHearingDocuments decides if all hearing docs need posting.
     */
    @ParameterizedTest
    @MethodSource("provideCasesSoShouldPostAllHearingDocumentsReturnsFalse")
    void shouldPostAllHearingDocumentsReturnsFalse(FinremCaseDetails finremCaseDetails, Hearing hearing) {
        // Arrange.  This mock is just for the FDR case.  FDR cases that are not express don't have all docs posted.
        lenient().when(expressCaseService.isExpressCase(finremCaseDetails.getData())).thenReturn(false);
        // Act
        boolean result = helper.shouldPostAllHearingDocuments(finremCaseDetails, hearing);
        // Assert
        assertFalse(result);
    }

    /**
     * Used by shouldPostAllHearingDocumentsReturnsFalse.
     *
     * @return Stream of Arguments all of which should cause shouldPostAllHearingDocuments to return False
     */
    static Stream<Arguments> provideCasesSoShouldPostAllHearingDocumentsReturnsFalse() {
        return Stream.of(
            // Case: valid action, but hearing is null
            arguments(
                finremCaseDetails(ManageHearingsAction.ADD_HEARING), null
            ),
            // ManageHearingsAction is null, so a failing condition.
            arguments(
                finremCaseDetails(null), Hearing.builder().hearingType(HearingType.FDA).build()
            ),
            // Action is ADD_HEARING, which is valid. However, hearing types are wrong.
            arguments(
                finremCaseDetails(ManageHearingsAction.ADD_HEARING), Hearing.builder().hearingType(HearingType.MPS).build()
            ),
            // Calling test must mock the express case service to return true for this Case.  Then FDR will mean false returned.
            arguments(
                finremCaseDetails(ManageHearingsAction.ADD_HEARING), Hearing.builder().hearingType(HearingType.FDR).build()
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
