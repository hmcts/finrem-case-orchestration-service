package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.managehearing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.managehearings.HearingCorrespondenceHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CfcCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Court;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Region;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionLondonFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingMode;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingDocumentsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.PartyOnCase;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.PartyOnCaseCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.VacateOrAdjournReason;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.VacateOrAdjournedHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.VacatedOrAdjournedHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DefaultCourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.EmailService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID_IN_LONG;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType.APPEAL_HEARING;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType.FDA;

@ExtendWith(MockitoExtension.class)
class ManageHearingsCorresponderTest {

    private static final UUID hearingId = UUID.fromString("c0a78b4c-5d85-4e50-9d62-219b1b8eb9bb");

    @Mock
    private HearingCorrespondenceHelper hearingCorrespondenceHelper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ManageHearingsCorresponder corresponder;

    @Test
    void givenHearingSelectedNoNotification_whenSendHearingCorrespondence_thenNoNotificationSent() {
        //Arrange
        FinremCallbackRequest callbackRequest =
            FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, CaseType.CONTESTED, new FinremCaseData());
        Hearing hearing = Hearing.builder().hearingNoticePrompt(YesOrNo.NO)
            .build();

        ManageHearingsWrapper manageHearingsWrapper = callbackRequest.getCaseDetails().getData().getManageHearingsWrapper();
        manageHearingsWrapper.setWorkingHearingId(hearingId);

        when(hearingCorrespondenceHelper.getActiveHearingInContext(manageHearingsWrapper, hearingId)).thenReturn(hearing);

        //Act
        corresponder.sendHearingCorrespondence(callbackRequest, AUTH_TOKEN);

        //Assert
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void givenHearingWithNullType_whenSendHearingCorrespondence_thenThrowsIllegalStateException() {
        //Arrange
        FinremCallbackRequest callbackRequest =
                FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, CaseType.CONTESTED, new FinremCaseData());
        Hearing hearing = Hearing.builder()
            .hearingNoticePrompt(YesOrNo.YES)
            .hearingType(null)
            .build();

        ManageHearingsWrapper manageHearingsWrapper = callbackRequest.getCaseDetails().getData().getManageHearingsWrapper();
        manageHearingsWrapper.setWorkingHearingId(hearingId);

        when(hearingCorrespondenceHelper.getActiveHearingInContext(manageHearingsWrapper, hearingId)).thenReturn(hearing);

        //Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            corresponder.sendHearingCorrespondence(callbackRequest, AUTH_TOKEN));

        assertTrue(exception.getMessage().contains("Hearing type must not be null"));
    }

    @Test
    void givenHearingWithNullDate_whenSendHearingCorrespondence_thenThrowsIllegalStateException() {
        //Arrange
        FinremCallbackRequest callbackRequest =
                FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, CaseType.CONTESTED, new FinremCaseData());
        Hearing hearing = Hearing.builder()
            .hearingNoticePrompt(YesOrNo.YES)
            .hearingType(FDA)
            .hearingDate(null)
            .build();

        ManageHearingsWrapper manageHearingsWrapper = callbackRequest.getCaseDetails().getData().getManageHearingsWrapper();
        manageHearingsWrapper.setWorkingHearingId(hearingId);

        when(hearingCorrespondenceHelper.getActiveHearingInContext(manageHearingsWrapper, hearingId)).thenReturn(hearing);

        //Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            corresponder.sendHearingCorrespondence(callbackRequest, AUTH_TOKEN));

        assertTrue(exception.getMessage().contains("Hearing date must not be null"));
    }

    @Test
    void givenHearingWithNullTime_whenSendVacateOrAdjournCorrespondence_thenThrowsIllegalStateException() {
        //Arrange
        FinremCallbackRequest callbackRequest =
            FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, CaseType.CONTESTED, new FinremCaseData());
        VacateOrAdjournedHearing hearing = VacateOrAdjournedHearing.builder()
            .hearingNoticePrompt(YesOrNo.YES)
            .hearingType(FDA)
            .wasVacOrAdjNoticeSent(YesOrNo.YES)
            .hearingDate(LocalDate.now())
            .hearingTime(null)
            .build();

        ManageHearingsWrapper manageHearingsWrapper = callbackRequest.getCaseDetails().getData().getManageHearingsWrapper();
        manageHearingsWrapper.setWorkingVacatedHearingId(hearingId);

        FinremCaseData finremCaseData = callbackRequest.getCaseDetails().getData();

        when(hearingCorrespondenceHelper.isVacatedAndRelistedHearing(finremCaseData)).thenReturn(false);
        when(hearingCorrespondenceHelper.getVacateOrAdjournedHearingInContext(manageHearingsWrapper, hearingId)).thenReturn(hearing);
        when(hearingCorrespondenceHelper.getVacateHearingNotice(finremCaseData)).thenReturn(
            CaseDocument.builder()
                .documentFilename("test Vacate.pdf")
                .build());

        //Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            corresponder.sendAdjournedOrVacatedHearingCorrespondence(callbackRequest, AUTH_TOKEN));

        assertTrue(exception.getMessage().contains("Hearing time must not be null"));
    }

    @Test
    void givenHearingSelectedToNotification_whenSendHearingCorrespondence_thenNotificationSent() {

        //Arrange
        Set<CaseRole> caseRoles = Set.of(
            CaseRole.APP_SOLICITOR,
            CaseRole.RESP_SOLICITOR,
            CaseRole.INTVR_SOLICITOR_1,
            CaseRole.INTVR_SOLICITOR_2,
            CaseRole.INTVR_SOLICITOR_3,
            CaseRole.INTVR_SOLICITOR_4
        );
        List<PartyOnCaseCollectionItem> partyList = buildPartiesList(caseRoles);

        Hearing hearing = Hearing
            .builder()
            .hearingDate(LocalDate.of(2026, 1, 1))
            .hearingTime("12:30")
            .hearingTimeEstimate("120 minutes")
            .hearingType(APPEAL_HEARING)
            .hearingNoticePrompt(YesOrNo.YES)
            .hearingMode(HearingMode.IN_PERSON)
            .hearingCourtSelection(Court.builder()
                .region(Region.LONDON)
                .londonList(RegionLondonFrc.LONDON)
                .courtListWrapper(DefaultCourtListWrapper.builder()
                    .cfcCourtList(CfcCourt.BROMLEY_COUNTY_COURT_AND_FAMILY_COURT)
                    .build())
                .build())
            .additionalHearingDocs(List.of(DocumentCollectionItem
                .builder()
                .value(CaseDocument
                    .builder()
                    .documentFilename("AdditionalDoc.pdf")
                    .build())
                .build()))
            .partiesOnCase(partyList)
            .build();

        FinremCaseData caseData = FinremCaseData
            .builder()
            .ccdCaseId(CASE_ID)
            .ccdCaseType(CaseType.CONTESTED)
            .contactDetailsWrapper(ContactDetailsWrapper
                .builder()
                .applicantLname("Frodo")
                .respondentLname("Gollum")
                .build())
            .manageHearingsWrapper(ManageHearingsWrapper
                .builder()
                .hearings(List.of(ManageHearingsCollectionItem
                    .builder()
                    .value(hearing)
                    .build()))
                .workingHearingId(hearingId)
                .hearingDocumentsCollection(List.of(ManageHearingDocumentsCollectionItem
                    .builder()
                    .value(ManageHearingDocument.builder()
                        .hearingId(hearingId)
                        .hearingDocument(CaseDocument
                            .builder()
                            .documentFilename("HearingNotice.pdf")
                            .build()).build())
                    .build()))
                .build())
            .build();

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, CaseType.CONTESTED, caseData);

        ManageHearingsWrapper manageHearingsWrapper = callbackRequest.getCaseDetails().getData().getManageHearingsWrapper();

        when(hearingCorrespondenceHelper.getActiveHearingInContext(manageHearingsWrapper, hearingId)).thenReturn(hearing);

        //Act
        corresponder.sendHearingCorrespondence(callbackRequest, AUTH_TOKEN);

        // Assert
        ArgumentCaptor<SendCorrespondenceEvent> captor = ArgumentCaptor.forClass(SendCorrespondenceEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        SendCorrespondenceEvent actualEvent = captor.getValue();
        SendCorrespondenceEvent expectedEvent = getExpectedHearingEvent(callbackRequest.getCaseDetails());

        assertThat(actualEvent.getEmailNotificationRequest())
            .usingRecursiveComparison()
            .isEqualTo(expectedEvent.getEmailNotificationRequest());

        assertThat(actualEvent.getEmailTemplate())
            .isEqualTo(expectedEvent.getEmailTemplate());

        assertThat(actualEvent.getNotificationParties())
            .containsExactlyInAnyOrderElementsOf(expectedEvent.getNotificationParties());

        assertThat(actualEvent.getDocumentsToPost())
            .containsExactlyInAnyOrderElementsOf(expectedEvent.getDocumentsToPost());

        assertThat(actualEvent.getCaseDetails())
            .usingRecursiveComparison()
            .isEqualTo(expectedEvent.getCaseDetails());

        assertThat(actualEvent.getAuthToken())
            .isEqualTo(expectedEvent.getAuthToken());
    }

    @Test
    void givenVacatedOrAdjournedHearingSelectedNoNotification_whenSendVacatedOrAdjournedHearingCorrespondence_thenNoNotificationSent() {
        //Arrange
        FinremCallbackRequest callbackRequest =
            FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, CaseType.CONTESTED, new FinremCaseData());
        VacateOrAdjournedHearing vacatedOrAdjournedHearing = VacateOrAdjournedHearing
            .builder().hearingNoticePrompt(YesOrNo.NO)
            .build();

        ManageHearingsWrapper manageHearingsWrapper =
            callbackRequest.getCaseDetails().getData().getManageHearingsWrapper();
        manageHearingsWrapper.setWorkingVacatedHearingId(hearingId);

        when(hearingCorrespondenceHelper
            .getVacateOrAdjournedHearingInContext(manageHearingsWrapper, hearingId))
            .thenReturn(vacatedOrAdjournedHearing);

        //Act
        corresponder.sendAdjournedOrVacatedHearingCorrespondence(callbackRequest, AUTH_TOKEN);

        //Assert
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void givenVacatedOrAdjournedHearingSelectedNoNotification_whenSendVacatedOrAdjournedHearingCorrespondence_thenNotificationSent() {

        //Arrange
        Set<CaseRole> caseRoles = Set.of(
            CaseRole.APP_SOLICITOR,
            CaseRole.RESP_SOLICITOR,
            CaseRole.INTVR_SOLICITOR_1,
            CaseRole.INTVR_SOLICITOR_2,
            CaseRole.INTVR_SOLICITOR_3,
            CaseRole.INTVR_SOLICITOR_4
        );
        List<PartyOnCaseCollectionItem> partyList = buildPartiesList(caseRoles);

        VacateOrAdjournedHearing hearing = VacateOrAdjournedHearing
            .builder()
            .hearingDate(LocalDate.of(2026, 1, 10))
            .hearingTime("12:30")
            .hearingTimeEstimate("120 minutes")
            .hearingType(APPEAL_HEARING)
            .hearingNoticePrompt(YesOrNo.YES)
            .hearingMode(HearingMode.IN_PERSON)
            .hearingCourtSelection(Court.builder()
                .region(Region.LONDON)
                .londonList(RegionLondonFrc.LONDON)
                .courtListWrapper(DefaultCourtListWrapper.builder()
                    .cfcCourtList(CfcCourt.BROMLEY_COUNTY_COURT_AND_FAMILY_COURT)
                    .build())
                .build())
            .additionalHearingDocs(List.of(DocumentCollectionItem
                .builder()
                .value(CaseDocument.builder().documentFilename("AdditionalDoc.pdf").build())
                .build()))
            .partiesOnCase(partyList)
            .vacateOrAdjournReason(VacateOrAdjournReason.OTHER)
            .vacatedOrAdjournedDate(LocalDate.of(2026, 1, 1))
            .wasVacOrAdjNoticeSent(YesOrNo.YES)
            .build();

        FinremCaseData caseData = FinremCaseData
            .builder()
            .ccdCaseId(CASE_ID)
            .ccdCaseType(CaseType.CONTESTED)
            .contactDetailsWrapper(ContactDetailsWrapper
                .builder()
                .applicantLname("Frodo")
                .respondentLname("Gollum")
                .build())
            .manageHearingsWrapper(ManageHearingsWrapper
                .builder()
                .vacatedOrAdjournedHearings(List.of(VacatedOrAdjournedHearingsCollectionItem
                    .builder()
                    .value(hearing)
                    .build()))
                .workingVacatedHearingId(hearingId)
                .hearingDocumentsCollection(List.of(ManageHearingDocumentsCollectionItem
                    .builder()
                    .value(ManageHearingDocument.builder()
                        .hearingId(hearingId)
                        .hearingDocument(CaseDocument
                            .builder()
                            .documentFilename("VacateHearingNotice.pdf")
                            .build()).build())
                    .build()))
                .build())
            .build();

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, CaseType.CONTESTED, caseData);

        ManageHearingsWrapper manageHearingsWrapper = callbackRequest.getCaseDetails().getData().getManageHearingsWrapper();

        when(hearingCorrespondenceHelper.getVacateOrAdjournedHearingInContext(manageHearingsWrapper, hearingId)).thenReturn(hearing);

        when(hearingCorrespondenceHelper.getVacateHearingNotice(callbackRequest.getCaseDetails().getData())).thenReturn(
            CaseDocument
            .builder()
            .documentFilename("VacateHearingNotice.pdf")
            .build()
        );

        //Act
        corresponder.sendAdjournedOrVacatedHearingCorrespondence(callbackRequest, AUTH_TOKEN);

        // Assert
        ArgumentCaptor<SendCorrespondenceEvent> captor = ArgumentCaptor.forClass(SendCorrespondenceEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        SendCorrespondenceEvent actualEvent = captor.getValue();
        SendCorrespondenceEvent expectedEvent = getExpectedVacatedOrAdjournedHearingEvent(callbackRequest.getCaseDetails());

        assertThat(actualEvent.getEmailNotificationRequest())
            .usingRecursiveComparison()
            .isEqualTo(expectedEvent.getEmailNotificationRequest());

        assertThat(actualEvent.getEmailTemplate())
            .isEqualTo(expectedEvent.getEmailTemplate());

        assertThat(actualEvent.getNotificationParties())
            .containsExactlyInAnyOrderElementsOf(expectedEvent.getNotificationParties());

        assertThat(actualEvent.getDocumentsToPost())
            .containsExactlyInAnyOrderElementsOf(expectedEvent.getDocumentsToPost());

        assertThat(actualEvent.getCaseDetails())
            .usingRecursiveComparison()
            .isEqualTo(expectedEvent.getCaseDetails());

        assertThat(actualEvent.getAuthToken())
            .isEqualTo(expectedEvent.getAuthToken());
    }

    private SendCorrespondenceEvent getExpectedHearingEvent(FinremCaseDetails caseDetails) {
        return SendCorrespondenceEvent.builder()
            .caseDetails(caseDetails)
            .notificationParties(List.of(
                NotificationParty.APPLICANT,
                NotificationParty.RESPONDENT,
                NotificationParty.INTERVENER_ONE,
                NotificationParty.INTERVENER_TWO,
                NotificationParty.INTERVENER_THREE,
                NotificationParty.INTERVENER_FOUR))
            .emailTemplate(EmailTemplateNames.FR_CONTESTED_HEARING_NOTIFICATION_SOLICITOR)
            .emailNotificationRequest(NotificationRequest
                .builder()
                .caseReferenceNumber(CASE_ID)
                .hearingType(APPEAL_HEARING.getId())
                .hearingDate("01 January 2026")
                    .caseType(EmailService.CONTESTED)
                .applicantName("Frodo")
                .respondentName("Gollum")
                .selectedCourt("cfc")
                .vacatedHearingDateTime("")
                .vacatedHearingType("")
                .build())
            .documentsToPost(List.of(
                CaseDocument.builder()
                    .documentFilename("AdditionalDoc.pdf")
                    .build(),
                CaseDocument.builder()
                    .documentFilename("HearingNotice.pdf")
                    .build())
                )
            .authToken(AUTH_TOKEN)
            .build();
    }

    private SendCorrespondenceEvent getExpectedVacatedOrAdjournedHearingEvent(FinremCaseDetails caseDetails) {
        return SendCorrespondenceEvent.builder()
            .caseDetails(caseDetails)
            .notificationParties(List.of(
                NotificationParty.APPLICANT,
                NotificationParty.RESPONDENT,
                NotificationParty.INTERVENER_ONE,
                NotificationParty.INTERVENER_TWO,
                NotificationParty.INTERVENER_THREE,
                NotificationParty.INTERVENER_FOUR))
            .emailTemplate(EmailTemplateNames.FR_CONTESTED_VACATE_NOTIFICATION_SOLICITOR)
            .emailNotificationRequest(NotificationRequest
                .builder()
                .caseReferenceNumber(CASE_ID)
                .hearingType(APPEAL_HEARING.getId())
                .hearingDate("10 January 2026")
                .caseType(EmailService.CONTESTED)
                .applicantName("Frodo")
                .respondentName("Gollum")
                .selectedCourt("cfc")
                .vacatedHearingDateTime("10 January 2026 at 12:30")
                .vacatedHearingType(APPEAL_HEARING.getId())
                .build())
            .documentsToPost(List.of(
                CaseDocument
                    .builder()
                    .documentFilename("VacateHearingNotice.pdf")
                    .build()))
            .authToken(AUTH_TOKEN)
            .build();
    }

    /**
     * Reusable test setup method to build a list of PartyOnCaseCollection from a set of CaseRoles.
     */
    private List<PartyOnCaseCollectionItem> buildPartiesList(Set<CaseRole> caseRoles) {

        // build the list elements from the passed set of case roles
        List<PartyOnCaseCollectionItem> parties = new ArrayList<>();
        for (CaseRole role : caseRoles) {
            PartyOnCase party = PartyOnCase.builder()
                .role(role.getCcdCode())
                .label(role.name())
                .build();
            parties.add(PartyOnCaseCollectionItem.builder()
                .value(party)
                .build());
        }
        return parties;
    }
}
