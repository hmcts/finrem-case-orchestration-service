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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingDocumentsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.HearingLike;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.VacateOrAdjournedHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.VacatedOrAdjournedHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs.HearingTabCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs.HearingTabItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PaperNotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.express.ExpressCaseService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType.FDA;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType.FDR;

@ExtendWith(MockitoExtension.class)
class HearingCorrespondenceHelperTest {

    @Mock
    PaperNotificationService paperNotificationService;
    @Mock
    ExpressCaseService expressCaseService;

    @InjectMocks
    private HearingCorrespondenceHelper helper;

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

        Hearing hearing = new Hearing();
        ManageHearingsWrapper wrapper = new ManageHearingsWrapper();
        wrapper.setManageHearingsActionSelection(ManageHearingsAction.ADD_HEARING);
        wrapper.setHearings(List.of(new ManageHearingsCollectionItem(hearingId, hearing)));

        FinremCaseData caseData = new FinremCaseData();
        caseData.setManageHearingsWrapper(wrapper);

        UUID nonMatchingWorkingHearingId = UUID.randomUUID();
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            helper.getActiveHearingInContext(wrapper, nonMatchingWorkingHearingId));

        assertTrue(exception.getMessage().contains("Hearing not found for the given ID"));
    }

    @Test
    void shouldThrowExceptionWhenGetHearingInContextCalledForEmptyHearingCollection() {
        UUID hearingId = UUID.randomUUID();

        ManageHearingsWrapper wrapper = new ManageHearingsWrapper();
        wrapper.setWorkingHearingId(hearingId);
        wrapper.setManageHearingsActionSelection(ManageHearingsAction.ADD_HEARING);

        FinremCaseData caseData = new FinremCaseData();
        caseData.setManageHearingsWrapper(wrapper);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            helper.getActiveHearingInContext(wrapper, hearingId));

        assertTrue(exception.getMessage().contains(
            String.format("No hearings available to search for. Working hearing ID is: %s",
                hearingId)));
    }

    @Test
    void shouldReturnMiniFormAWhenFDAA() {
        FinremCaseData caseData = FinremCaseData.builder()
            .miniFormA(mock(CaseDocument.class))
            .build();

        Hearing hearing = Hearing.builder()
            .hearingType(FDA)
            .build();

        Optional<CaseDocument> result = helper.getMiniFormAIfRequired(caseData, hearing);

        assertTrue(result.isPresent());
        assertEquals(caseData.getMiniFormA(), result.get());
    }

    @Test
    void shouldReturnMiniFormAWhenExpressFDR() {
        FinremCaseData caseData = FinremCaseData.builder()
            .miniFormA(mock(CaseDocument.class))
            .build();

        Hearing hearing = Hearing.builder()
            .hearingType(FDR)
            .build();

        when(expressCaseService.isExpressCase(caseData)).thenReturn(true);

        Optional<CaseDocument> result = helper.getMiniFormAIfRequired(caseData, hearing);

        assertTrue(result.isPresent());
        assertEquals(caseData.getMiniFormA(), result.get());
    }

    @Test
    void shouldReturnEmptyWhenMiniFormAIsNotRequired() {
        FinremCaseData caseData = FinremCaseData.builder().build();

        Hearing hearing = Hearing.builder()
            .hearingType(null)
            .build();

        Optional<CaseDocument> result = helper.getMiniFormAIfRequired(caseData, hearing);

        assertTrue(result.isEmpty());
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

    @Test
    void when_isVacatedAndRelistedHearing_returns_true() {
        FinremCaseDetails finremCaseDetails = finremCaseDetails(ManageHearingsAction.VACATE_HEARING);
        finremCaseDetails.getData().getManageHearingsWrapper().setWasRelistSelected(YesOrNo.YES);
        assertTrue(helper.isVacatedAndRelistedHearing(finremCaseDetails));
    }

    @ParameterizedTest
    @MethodSource("provideFalseArgsForIsVacatedAndRelistedHearing")
    void when_isVacatedAndRelistedHearing_returns_false(ManageHearingsAction action, YesOrNo yesOrNo) {
        FinremCaseDetails finremCaseDetails = finremCaseDetails(action);
        finremCaseDetails.getData().getManageHearingsWrapper().setWasRelistSelected(yesOrNo);
        assertFalse(helper.isVacatedAndRelistedHearing(finremCaseDetails));
    }

    /**
     * Used by when_isVacatedAndRelistedHearing_returns_false.
     *
     * @return Stream of Arguments all of which should cause when_isVacatedAndRelistedHearing_returns_false to return false.
     */
    static Stream<Arguments> provideFalseArgsForIsVacatedAndRelistedHearing() {
        return Stream.of(
            arguments(
                ManageHearingsAction.VACATE_HEARING, YesOrNo.NO
            ),
            arguments(
                ManageHearingsAction.ADD_HEARING, YesOrNo.YES
            ),
            arguments(
                ManageHearingsAction.ADD_HEARING, YesOrNo.NO
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

    @Test
    void shouldReturnVacateOrAdjournedHearingInContextWhenIdMatches() {
        UUID vacatedHearingId = UUID.randomUUID();
        VacateOrAdjournedHearing vacatedHearing = VacateOrAdjournedHearing.builder().build();

        ManageHearingsWrapper wrapper = ManageHearingsWrapper.builder()
            .vacatedOrAdjournedHearings(List.of(
                VacatedOrAdjournedHearingsCollectionItem.builder()
                    .id(vacatedHearingId)
                    .value(vacatedHearing)
                    .build()))
            .build();

        VacateOrAdjournedHearing result = helper.getVacateOrAdjournedHearingInContext(wrapper, vacatedHearingId);

        assertEquals(vacatedHearing, result);
    }

    @Test
    void shouldThrowExceptionWhenVacateOrAdjournedHearingIdNotFound() {
        ManageHearingsWrapper wrapper = ManageHearingsWrapper.builder()
            .vacatedOrAdjournedHearings(List.of())
            .build();

        UUID nonExistentId = UUID.randomUUID();
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            helper.getVacateOrAdjournedHearingInContext(wrapper, nonExistentId));

        assertTrue(exception.getMessage().contains("Vacated hearing not found for the given ID: " + nonExistentId));
    }

    @Test
    void shouldThrowExceptionForEmptyVacateOrAdjournedHearingCollection() {
        UUID vacatedHearingId = UUID.randomUUID();
        ManageHearingsWrapper wrapper = ManageHearingsWrapper.builder()
            .vacatedOrAdjournedHearings(null)
            .build();

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            helper.getVacateOrAdjournedHearingInContext(wrapper, vacatedHearingId));

        assertTrue(exception.getMessage().contains(
            "No vacated or adjourned hearings available to search for. Working hearing ID is: " + vacatedHearingId));
    }

    @Test
    void shouldReturnHearingWhenIdMatches() {
        UUID hearingId = UUID.randomUUID();
        Hearing expectedHearing = new Hearing();
        ManageHearingsWrapper wrapper = new ManageHearingsWrapper();
        wrapper.setHearings(List.of(new ManageHearingsCollectionItem(hearingId, expectedHearing)));

        Hearing actualHearing = helper.getActiveHearingInContext(wrapper, hearingId);

        assertEquals(expectedHearing, actualHearing, "The hearing returned should match the expected hearing");
    }

    @Test
    void shouldThrowExceptionWhenNoHearingFoundForId() {
        UUID hearingId = UUID.randomUUID();
        UUID nonExistentId = UUID.randomUUID();
        ManageHearingsWrapper wrapper = new ManageHearingsWrapper();
        wrapper.setHearings(List.of(new ManageHearingsCollectionItem(hearingId, new Hearing())));

        Exception exception = assertThrows(IllegalStateException.class, () ->
            helper.getActiveHearingInContext(wrapper, nonExistentId));

        assertTrue(exception.getMessage().contains("Hearing not found for the given ID: " + nonExistentId));
    }

    @Test
    void shouldThrowExceptionWhenHearingCollectionIsNull() {
        UUID hearingId = UUID.randomUUID();
        ManageHearingsWrapper wrapper = new ManageHearingsWrapper();
        wrapper.setHearings(null);

        Exception exception = assertThrows(IllegalStateException.class, () ->
            helper.getActiveHearingInContext(wrapper, hearingId));

        assertTrue(exception.getMessage().contains("No hearings available to search for. Working hearing ID is: " + hearingId));
    }

    @Test
    void shouldReturnVacateHearingNoticeWhenPresent() {
        UUID hearingId = UUID.randomUUID();
        CaseDocument expectedDocument = CaseDocument.builder().documentFilename("vacate_notice.pdf").build();

        ManageHearingsWrapper wrapper = ManageHearingsWrapper.builder()
            .workingVacatedHearingId(hearingId)
            .hearingDocumentsCollection(List.of(
                ManageHearingDocumentsCollectionItem.builder()
                    .value(ManageHearingDocument.builder()
                        .hearingId(hearingId)
                        .hearingCaseDocumentType(CaseDocumentType.VACATE_HEARING_NOTICE)
                        .hearingDocument(expectedDocument)
                        .build())
                    .build()))
            .build();

        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder()
            .data(FinremCaseData.builder()
                .manageHearingsWrapper(wrapper)
                .build())
            .build();

        CaseDocument result = helper.getVacateHearingNotice(finremCaseDetails);

        assertEquals(expectedDocument, result);
    }

    @Test
    void shouldReturnNullWhenNoVacateHearingNoticeIsFound() {
        UUID hearingId = UUID.randomUUID();

        ManageHearingsWrapper wrapper = ManageHearingsWrapper.builder()
            .workingVacatedHearingId(hearingId)
            .hearingDocumentsCollection(List.of(
                ManageHearingDocumentsCollectionItem.builder()
                    .value(ManageHearingDocument.builder()
                        .hearingId(UUID.randomUUID())  // Different hearing ID
                        .hearingCaseDocumentType(CaseDocumentType.VACATE_HEARING_NOTICE)
                        .hearingDocument(CaseDocument.builder().build())
                        .build())
                    .build()))
            .build();

        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder()
            .data(FinremCaseData.builder()
                .manageHearingsWrapper(wrapper)
                .build())
            .build();

        CaseDocument result = helper.getVacateHearingNotice(finremCaseDetails);

        assertNull(result);
    }
}
