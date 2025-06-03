package uk.gov.hmcts.reform.finrem.caseorchestration.handler.managehearings;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CfcCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Court;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Region;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionLondonFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingMode;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DefaultCourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class HearingsSubmittedHandlerTest {

    @InjectMocks
    private ManageHearingsSubmittedHandler manageHearingsSubmittedHandler;

    @Test
    void testCanHandle() {
        Assertions.assertCanHandle(manageHearingsSubmittedHandler, CallbackType.SUBMITTED, CaseType.CONTESTED,
            EventType.MANAGE_HEARINGS);
    }

    @Test
    void shouldHandleSubmittedCallback() {
        // Arrange
        UUID hearingID = UUID.randomUUID();
        FinremCallbackRequest callbackRequest = buildCallbackRequest(hearingID, hearingID);

        // Act
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            manageHearingsSubmittedHandler.handle(callbackRequest, TestConstants.AUTH_TOKEN);

        // Assert
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getManageHearingsWrapper().getWorkingHearingId()).isEqualTo(hearingID);
        assertThat(response.getErrors()).isNullOrEmpty();
    }

    @Test
    void shouldThrowIllegalStateIfHearingNotFound() {
        // Arrange
        UUID hearingID = UUID.randomUUID();
        UUID mismatchedHearingID = UUID.randomUUID();
        FinremCallbackRequest callbackRequest = buildCallbackRequest(hearingID, mismatchedHearingID);

        // Act & Assert
        assertThrows(IllegalStateException.class, () ->
            manageHearingsSubmittedHandler.handle(callbackRequest, TestConstants.AUTH_TOKEN)
        );
    }

    private FinremCallbackRequest buildCallbackRequest(UUID hearingID, UUID hearingItemId) {
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .manageHearingsWrapper(ManageHearingsWrapper
                .builder()
                .workingHearingId(hearingID)
                .hearings(List.of(
                    ManageHearingsCollectionItem
                        .builder()
                        .id(hearingItemId)
                        .value(Hearing
                            .builder()
                            .hearingType(HearingType.APPEAL_HEARING)
                            .hearingDate(LocalDate.now())
                            .hearingTime("10:00 AM")
                            .hearingTimeEstimate("2 hours")
                            .hearingMode(HearingMode.IN_PERSON)
                            .additionalHearingInformation("Additional Info")
                            .hearingCourtSelection(Court.builder()
                                .region(Region.LONDON)
                                .londonList(RegionLondonFrc.LONDON)
                                .courtListWrapper(DefaultCourtListWrapper.builder()
                                    .cfcCourtList(CfcCourt.BROMLEY_COUNTY_COURT_AND_FAMILY_COURT)
                                    .build())
                                .build())
                            .build())
                        .build()
                ))
                .build())
            .build();

        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .data(finremCaseData)
            .build();

        return FinremCallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
    }

}
