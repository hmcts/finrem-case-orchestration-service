package uk.gov.hmcts.reform.finrem.caseorchestration.handler.helper.managehearings;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.managehearings.HearingCorrespondenceHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.HearingLike;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs.HearingTabCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs.HearingTabItem;
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
        Hearing hearing = Hearing.builder().build();

        // hearingNoticePrompt not initialised
        assertTrue(helper.shouldNotSendNotification(hearing));

        hearing.setHearingNoticePrompt(YesOrNo.NO);
        assertTrue(helper.shouldNotSendNotification(hearing));

        hearing.setHearingNoticePrompt(YesOrNo.YES);
        assertFalse(helper.shouldNotSendNotification(hearing));
    }

    @Test
    void shouldReturnHearingInContextWhenIdMatches() {
        UUID hearingId = UUID.randomUUID();
        Hearing hearing = new Hearing();
        ManageHearingsWrapper wrapper = new ManageHearingsWrapper();
        wrapper.setManageHearingsActionSelection(ManageHearingsAction.ADD_HEARING);
        wrapper.setHearings(List.of(new ManageHearingsCollectionItem(hearingId, hearing)));
        wrapper.setWorkingHearingId(hearingId);

        FinremCaseData caseData = new FinremCaseData();
        caseData.setManageHearingsWrapper(wrapper);

        HearingLike result = helper.getActiveHearingInContext(wrapper, hearingId);

        assertEquals(hearing, result);
    }

    @Test
    void shouldThrowExceptionWhenHearingIdNotFound() {
        UUID hearingId = UUID.randomUUID();
        UUID nonMatchingWorkingHearingId = UUID.randomUUID();

        Hearing hearing = new Hearing();
        ManageHearingsWrapper wrapper = new ManageHearingsWrapper();
        wrapper.setManageHearingsActionSelection(ManageHearingsAction.ADD_HEARING);
        wrapper.setHearings(List.of(new ManageHearingsCollectionItem(hearingId, hearing)));
        wrapper.setWorkingHearingId(nonMatchingWorkingHearingId);

        FinremCaseData caseData = new FinremCaseData();
        caseData.setManageHearingsWrapper(wrapper);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            helper.getHearingInContext(caseData));

        assertTrue(exception.getMessage().contains("Hearing not found for the given ID"));
    }

    // PT todo - more tests needed to cover vacate action and missing action
    @Test
    void shouldThrowExceptionWhenGetHearingInContextCalledForEmptyHearingCollection() {
        UUID hearingId = UUID.randomUUID();

        ManageHearingsWrapper wrapper = new ManageHearingsWrapper();
        wrapper.setWorkingHearingId(hearingId);
        wrapper.setManageHearingsActionSelection(ManageHearingsAction.ADD_HEARING);

        FinremCaseData caseData = new FinremCaseData();
        caseData.setManageHearingsWrapper(wrapper);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            helper.getHearingInContext(caseData));

        assertTrue(exception.getMessage().contains(
            String.format("No hearings available to search for. Working hearing ID is: %s",
                hearingId)));
    }

    @Test
    void givenRequestForHearingTabItem_whenInvoked_shouldReturnHearingInContextWhenIdMatches() {
        UUID hearingId = UUID.randomUUID();
        HearingTabItem hearingTabItem = HearingTabItem.builder().build();

        ManageHearingsWrapper wrapper = ManageHearingsWrapper.builder()
            .hearingTabItems(List.of(HearingTabCollectionItem.builder()
                .id(hearingId)
                .value(hearingTabItem)
                .build()))
            .workingHearingId(hearingId)
            .build();

        FinremCaseData caseData = FinremCaseData.builder()
            .manageHearingsWrapper(wrapper)
            .build();

        HearingTabItem result = helper.getHearingInContextFromTab(caseData);

        assertEquals(hearingTabItem, result);
    }

    /**
     * shouldEmailToApplicantSolicitor is just a wrapper around the paperNotificationService.shouldPrintForApplicant.
     */
    @Test
    void shouldReturnCorrectValueForShouldEmailToApplicantSolicitor() {
        FinremCaseDetails finremCaseDetails = mock(FinremCaseDetails.class);

        when(paperNotificationService.shouldPrintForApplicantDisregardApplicationType(finremCaseDetails)).thenReturn(false);
        assertTrue(helper.shouldEmailToApplicantSolicitor(finremCaseDetails));

        when(paperNotificationService.shouldPrintForApplicantDisregardApplicationType(finremCaseDetails)).thenReturn(true);
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

        when(paperNotificationService.shouldPrintForApplicantDisregardApplicationType(finremCaseDetails)).thenReturn(true);
        assertTrue(helper.shouldPostToApplicant(finremCaseDetails));

        when(paperNotificationService.shouldPrintForApplicantDisregardApplicationType(finremCaseDetails)).thenReturn(false);
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
            arguments(
                Hearing.builder().hearingType(HearingType.APPLICATION_HEARING).build()
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
    void shouldPostHearingNoticeOnlyReturnsFalse(Hearing hearing) {
        // Arrange case
        FinremCaseDetails finremCaseDetails = finremCaseDetails();
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
            // Case: FDA Hearing type is wrong.
            arguments(
                Hearing.builder().hearingType(HearingType.FDA).build()
            ),
            // but FDR Hearing type is wrong (for a standard, non-express, case like this).
            arguments(
                Hearing.builder().hearingType(HearingType.FDR).build()
            )
        );
    }

    // PT todo - finish when other tests done
    /**
     * Test fails if both methods return false.  This would mean no correspondence is sent for a hearing.
     * Either shouldPostAllHearingDocuments or shouldPostHearingNoticeOnly must return true.
     * If the test fails, it's likely that a new HearingType has been added and the logic in one of the methods
     * needs updating.
     */
    @ParameterizedTest
    @EnumSource(HearingType.class)
    void shouldAlwaysPostSomeHearingCorrespondence(HearingType hearingType) {
        // Arrange. The hearing uses the parameterised hearing type.
        FinremCaseDetails finremCaseDetails = finremCaseDetails(ManageHearingsAction.ADD_HEARING);
        Hearing hearing = Hearing.builder().hearingType(hearingType).build();

        // Assert that at least one of the methods returns true.
        // This test changes.  If you keep, changes so that if the 'should' checks fail, then everything sent.
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

    private static FinremCaseDetails finremCaseDetails() {
        return FinremCaseDetails.builder()
            .data(FinremCaseData.builder()
                .manageHearingsWrapper(ManageHearingsWrapper.builder()
                    .build())
                .build())
            .build();
    }
}
