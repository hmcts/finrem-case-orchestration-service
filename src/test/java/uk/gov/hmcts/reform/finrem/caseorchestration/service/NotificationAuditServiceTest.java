package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.PartyOnCase;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.PartyOnCaseCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.notifications.NotificationAudit;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.notifications.NotificationAuditCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.notifications.NotificationToBeSentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.notifications.NotificationType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.NotificationAuditWrapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.APP_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.RESP_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_HEARING_NOTIFICATION_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService.FINANCIAL_REMEDY_PACK_LETTER_TYPE;

@ExtendWith(MockitoExtension.class)
class NotificationAuditServiceTest {

    private static final String APPLICANT_ROLE = APP_SOLICITOR.getCcdCode();
    private static final String RESPONDENT_ROLE = RESP_SOLICITOR.getCcdCode();

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationAuditService notificationAuditService;

    @Test
    void givenDigitalApplicant_whenCreateAuditsForHearingCorrespondence_thenEmailAuditAndPendingNotificationCreated() {

        Hearing hearing = Hearing.builder()
            .partiesOnCase(List.of(partyOnCase(APPLICANT_ROLE)))
            .build();

        FinremCallbackRequest request = buildRequest();

        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(request.getCaseDetails()))
            .thenReturn(true);

        notificationAuditService.createAuditsForHearingCorrespondence(
            request.getCaseDetails(),
            hearing,
            EventType.MANAGE_HEARINGS,
            FR_CONTESTED_HEARING_NOTIFICATION_SOLICITOR,
            List.of());

        NotificationAuditWrapper wrapper = request.getCaseDetails().getData().getNotificationAuditWrapper();
        assertThat(wrapper.getNotificationsAudits()).hasSize(1);
        assertThat(wrapper.getNotificationsToBeSent()).hasSize(1);

        NotificationAuditCollectionItem auditItem = wrapper.getNotificationsAudits().getFirst();
        NotificationAudit audit = auditItem.getValue();

        assertThat(auditItem.getId()).isNotNull();
        assertThat(audit.getCreatedAt()).isEqualTo(LocalDate.now());
        assertThat(audit.getWasSent()).isEqualTo(YesOrNo.NO);
        assertThat(audit.getEventId()).isEqualTo(EventType.MANAGE_HEARINGS.getCcdType());
        assertThat(audit.getParty()).isEqualTo(APPLICANT_ROLE);
        assertThat(audit.getType()).isEqualTo(NotificationType.EMAIL);
        assertThat(audit.getEmailTemplate()).isEqualTo(FR_CONTESTED_HEARING_NOTIFICATION_SOLICITOR.name());
        assertThat(audit.getLetterTemplate()).isNull();
        assertThat(audit.getAttachedPostalDocs()).isNull();

        assertThat(wrapper.getNotificationsToBeSent().getFirst().getId()).isNotNull();
        assertThat(wrapper.getNotificationsToBeSent().getFirst().getValue()).isEqualTo(auditItem.getId());
    }

    @Test
    void givenNonDigitalRespondent_whenCreateAuditsForHearingCorrespondence_thenPostalAuditAndPendingNotificationCreated() {

        CaseDocument hearingNotice = CaseDocument.builder()
            .documentFilename("hearingNotice.pdf")
            .build();
        CaseDocument miniFormA = CaseDocument.builder()
            .documentFilename("miniFormA.pdf")
            .build();

        Hearing hearing = Hearing.builder()
            .partiesOnCase(List.of(partyOnCase(RESPONDENT_ROLE)))
            .build();

        FinremCallbackRequest request = buildRequest();

        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(request.getCaseDetails()))
            .thenReturn(false);

        notificationAuditService.createAuditsForHearingCorrespondence(
            request.getCaseDetails(),
            hearing,
            EventType.MANAGE_HEARINGS,
            FR_CONTESTED_HEARING_NOTIFICATION_SOLICITOR,
            List.of(hearingNotice, miniFormA));

        NotificationAuditWrapper wrapper = request.getCaseDetails().getData().getNotificationAuditWrapper();

        assertThat(wrapper.getNotificationsAudits()).hasSize(1);
        assertThat(wrapper.getNotificationsToBeSent()).hasSize(1);

        NotificationAuditCollectionItem auditItem = wrapper.getNotificationsAudits().getFirst();
        NotificationAudit audit = auditItem.getValue();

        assertThat(auditItem.getId()).isNotNull();
        assertThat(audit.getCreatedAt()).isEqualTo(LocalDate.now());
        assertThat(audit.getWasSent()).isEqualTo(YesOrNo.NO);
        assertThat(audit.getEventId()).isEqualTo(EventType.MANAGE_HEARINGS.getCcdType());
        assertThat(audit.getParty()).isEqualTo(RESPONDENT_ROLE);
        assertThat(audit.getType()).isEqualTo(NotificationType.POSTAL);
        assertThat(audit.getLetterTemplate()).isEqualTo(FINANCIAL_REMEDY_PACK_LETTER_TYPE);
        assertThat(audit.getAttachedPostalDocs()).isEqualTo("hearingNotice.pdf, miniFormA.pdf");
        assertThat(audit.getEmailTemplate()).isNull();
        assertThat(wrapper.getNotificationsToBeSent().getFirst().getValue()).isEqualTo(auditItem.getId());
    }

    @Test
    void givenMultipleParties_whenCreateAuditsForHearingCorrespondence_thenAuditCreatedForEachParty() {

        Hearing hearing = Hearing.builder()
            .partiesOnCase(List.of(
                partyOnCase(APPLICANT_ROLE),
                partyOnCase(RESPONDENT_ROLE)
            ))
            .build();

        FinremCallbackRequest request = buildRequest();

        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(request.getCaseDetails()))
            .thenReturn(true);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(request.getCaseDetails()))
            .thenReturn(false);

        notificationAuditService.createAuditsForHearingCorrespondence(
            request.getCaseDetails(),
            hearing,
            EventType.MANAGE_HEARINGS,
            FR_CONTESTED_HEARING_NOTIFICATION_SOLICITOR,
            List.of());

        NotificationAuditWrapper wrapper = request.getCaseDetails().getData().getNotificationAuditWrapper();

        assertThat(wrapper.getNotificationsAudits()).hasSize(2);
        assertThat(wrapper.getNotificationsToBeSent()).hasSize(2);

        assertThat(wrapper.getNotificationsAudits())
            .extracting(item -> item.getValue().getParty())
            .containsExactly(APPLICANT_ROLE, RESPONDENT_ROLE);

        assertThat(wrapper.getNotificationsAudits())
            .extracting(item -> item.getValue().getType())
            .containsExactly(NotificationType.EMAIL, NotificationType.POSTAL);

        assertThat(wrapper.getNotificationsToBeSent())
            .extracting(NotificationToBeSentCollectionItem::getValue)
            .containsExactly(
                wrapper.getNotificationsAudits().get(0).getId(),
                wrapper.getNotificationsAudits().get(1).getId());
    }

    @Test
    void givenExistingAudits_whenCreateAuditsForHearingCorrespondence_thenNewAuditsAreAppended() {

        UUID existingAuditId = UUID.randomUUID();
        NotificationAuditCollectionItem existingAudit = NotificationAuditCollectionItem.builder()
            .id(existingAuditId)
            .value(NotificationAudit.builder()
                .party("existingParty")
                .wasSent(YesOrNo.NO)
                .build())
            .build();
        NotificationToBeSentCollectionItem existingPendingNotification = NotificationToBeSentCollectionItem.builder()
            .id(UUID.randomUUID())
            .value(existingAuditId)
            .build();
        NotificationAuditWrapper wrapper = NotificationAuditWrapper.builder()
            .notificationsAudits(new ArrayList<>(List.of(existingAudit)))
            .notificationsToBeSent(new ArrayList<>(List.of(existingPendingNotification)))
            .build();
        FinremCaseData caseData = FinremCaseData.builder()
            .notificationAuditWrapper(wrapper)
            .build();

        FinremCallbackRequest request = FinremCallbackRequestFactory.from(
            Long.parseLong(TestConstants.CASE_ID),
            CaseType.CONTESTED,
            caseData);

        Hearing hearing = Hearing.builder()
            .partiesOnCase(List.of(partyOnCase(APPLICANT_ROLE)))
            .build();

        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(request.getCaseDetails()))
            .thenReturn(true);

        notificationAuditService.createAuditsForHearingCorrespondence(
            request.getCaseDetails(),
            hearing,
            EventType.MANAGE_HEARINGS,
            FR_CONTESTED_HEARING_NOTIFICATION_SOLICITOR,
            List.of());

        assertThat(wrapper.getNotificationsAudits()).hasSize(2);
        assertThat(wrapper.getNotificationsToBeSent()).hasSize(2);
        assertThat(wrapper.getNotificationsAudits().getFirst()).isEqualTo(existingAudit);
        assertThat(wrapper.getNotificationsToBeSent().getFirst()).isEqualTo(existingPendingNotification);

        NotificationAuditCollectionItem newAudit = wrapper.getNotificationsAudits().get(1);
        NotificationToBeSentCollectionItem newPendingNotification = wrapper.getNotificationsToBeSent().get(1);

        assertThat(newAudit.getValue().getParty()).isEqualTo(APPLICANT_ROLE);
        assertThat(newPendingNotification.getValue()).isEqualTo(newAudit.getId());
    }

    @Test
    void givenNoPartiesOnHearing_whenCreateAuditsForHearingCorrespondence_thenNoAuditsAreCreated() {

        Hearing hearing = Hearing.builder()
            .partiesOnCase(null)
            .build();

        FinremCallbackRequest request = buildRequest();

        notificationAuditService.createAuditsForHearingCorrespondence(
            request.getCaseDetails(),
            hearing,
            EventType.MANAGE_HEARINGS,
            FR_CONTESTED_HEARING_NOTIFICATION_SOLICITOR,
            List.of());

        NotificationAuditWrapper wrapper = request.getCaseDetails().getData().getNotificationAuditWrapper();

        assertThat(wrapper.getNotificationsAudits()).isEmpty();
        assertThat(wrapper.getNotificationsToBeSent()).isEmpty();
    }

    @Test
    void givenNullPostalDocuments_whenCreateAuditsForHearingCorrespondence_thenPostalAuditHasEmptyAttachedDocs() {

        Hearing hearing = Hearing.builder()
            .partiesOnCase(List.of(partyOnCase(RESPONDENT_ROLE)))
            .build();

        FinremCallbackRequest request = buildRequest();

        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(request.getCaseDetails()))
            .thenReturn(false);

        notificationAuditService.createAuditsForHearingCorrespondence(
            request.getCaseDetails(),
            hearing,
            EventType.MANAGE_HEARINGS,
            FR_CONTESTED_HEARING_NOTIFICATION_SOLICITOR,
            null);

        NotificationAudit audit = request.getCaseDetails()
            .getData()
            .getNotificationAuditWrapper()
            .getNotificationsAudits()
            .getFirst()
            .getValue();

        assertThat(audit.getType()).isEqualTo(NotificationType.POSTAL);
        assertThat(audit.getLetterTemplate()).isEqualTo(FINANCIAL_REMEDY_PACK_LETTER_TYPE);
        assertThat(audit.getAttachedPostalDocs()).isEmpty();
    }

    private FinremCallbackRequest buildRequest() {
        FinremCaseData caseData = FinremCaseData.builder()
            .notificationAuditWrapper(NotificationAuditWrapper.builder().build())
            .build();

        return FinremCallbackRequestFactory.from(
            Long.parseLong(TestConstants.CASE_ID),
            CaseType.CONTESTED,
            caseData);
    }

    private PartyOnCaseCollectionItem partyOnCase(String role) {
        return PartyOnCaseCollectionItem.builder()
            .value(PartyOnCase.builder()
                .role(role)
                .label(role)
                .build())
            .build();
    }
}
