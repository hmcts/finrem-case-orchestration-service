package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.notifications.NotificationAudit;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.notifications.NotificationAuditCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.notifications.NotificationToBeSentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.notifications.NotificationType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.NotificationAuditWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_HEARING_NOTIFICATION_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService.FINANCIAL_REMEDY_PACK_LETTER_TYPE;

@ExtendWith(MockitoExtension.class)
class NotificationAuditServiceTest {

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private NotificationAuditService notificationAuditService;

    @Test
    void givenEmailNotification_whenCreateAuditsForCorrespondence_thenEmailAuditAndPendingNotificationCreated() {
        FinremCallbackRequest request = buildRequest();

        SendCorrespondenceEvent event = SendCorrespondenceEvent.builder()
            .caseDetails(request.getCaseDetails())
            .notificationParties(new ArrayList<>(List.of(NotificationParty.APPLICANT)))
            .emailOrLetters(new ArrayList<>(List.of(true)))
            .emailTemplate(FR_CONTESTED_HEARING_NOTIFICATION_SOLICITOR)
            .documentsToPost(List.of())
            .build();

        notificationAuditService.createAuditsForCorrespondence(event, EventType.MANAGE_HEARINGS);

        verify(applicationEventPublisher).publishEvent(event);
        assertThat(event.isDryRun()).isTrue();

        NotificationAuditWrapper wrapper = request.getCaseDetails().getData().getNotificationAuditWrapper();

        assertThat(wrapper.getNotificationsAudits()).hasSize(1);
        assertThat(wrapper.getNotificationsToBeSent()).hasSize(1);

        NotificationAuditCollectionItem auditItem = wrapper.getNotificationsAudits().getFirst();
        NotificationAudit audit = auditItem.getValue();

        assertThat(auditItem.getId()).isNotNull();
        assertThat(audit.getCreatedAt()).isEqualTo(LocalDate.now());
        assertThat(audit.getWasSent()).isEqualTo(YesOrNo.NO);
        assertThat(audit.getEventId()).isEqualTo(EventType.MANAGE_HEARINGS.getCcdType());
        assertThat(audit.getParty()).isEqualTo(NotificationParty.APPLICANT.name());
        assertThat(audit.getType()).isEqualTo(NotificationType.EMAIL);
        assertThat(audit.getEmailTemplate()).isEqualTo(FR_CONTESTED_HEARING_NOTIFICATION_SOLICITOR.name());
        assertThat(audit.getLetterTemplate()).isNull();
        assertThat(audit.getAttachedPostalDocs()).isNull();

        assertThat(wrapper.getNotificationsToBeSent().getFirst().getId()).isNotNull();
        assertThat(wrapper.getNotificationsToBeSent().getFirst().getValue()).isEqualTo(auditItem.getId());
    }

    @Test
    void givenPostalNotification_whenCreateAuditsForCorrespondence_thenPostalAuditAndPendingNotificationCreated() {
        CaseDocument hearingNotice = CaseDocument.builder()
            .documentFilename("hearingNotice.pdf")
            .build();

        CaseDocument miniFormA = CaseDocument.builder()
            .documentFilename("miniFormA.pdf")
            .build();

        FinremCallbackRequest request = buildRequest();

        SendCorrespondenceEvent event = SendCorrespondenceEvent.builder()
            .caseDetails(request.getCaseDetails())
            .notificationParties(new ArrayList<>(List.of(NotificationParty.RESPONDENT)))
            .emailOrLetters(new ArrayList<>(List.of(false)))
            .emailTemplate(FR_CONTESTED_HEARING_NOTIFICATION_SOLICITOR)
            .documentsToPost(List.of(hearingNotice, miniFormA))
            .build();

        notificationAuditService.createAuditsForCorrespondence(event, EventType.MANAGE_HEARINGS);

        verify(applicationEventPublisher).publishEvent(event);
        assertThat(event.isDryRun()).isTrue();

        NotificationAuditWrapper wrapper = request.getCaseDetails().getData().getNotificationAuditWrapper();

        assertThat(wrapper.getNotificationsAudits()).hasSize(1);
        assertThat(wrapper.getNotificationsToBeSent()).hasSize(1);

        NotificationAuditCollectionItem auditItem = wrapper.getNotificationsAudits().getFirst();
        NotificationAudit audit = auditItem.getValue();

        assertThat(auditItem.getId()).isNotNull();
        assertThat(audit.getCreatedAt()).isEqualTo(LocalDate.now());
        assertThat(audit.getWasSent()).isEqualTo(YesOrNo.NO);
        assertThat(audit.getEventId()).isEqualTo(EventType.MANAGE_HEARINGS.getCcdType());
        assertThat(audit.getParty()).isEqualTo(NotificationParty.RESPONDENT.name());
        assertThat(audit.getType()).isEqualTo(NotificationType.POSTAL);
        assertThat(audit.getLetterTemplate()).isEqualTo(FINANCIAL_REMEDY_PACK_LETTER_TYPE);
        assertThat(audit.getAttachedPostalDocs()).isEqualTo("hearingNotice.pdf, miniFormA.pdf");
        assertThat(audit.getEmailTemplate()).isNull();

        assertThat(wrapper.getNotificationsToBeSent().getFirst().getValue()).isEqualTo(auditItem.getId());
    }

    @Test
    void givenMultipleParties_whenCreateAuditsForCorrespondence_thenAuditCreatedForEachParty() {
        FinremCallbackRequest request = buildRequest();

        SendCorrespondenceEvent event = SendCorrespondenceEvent.builder()
            .caseDetails(request.getCaseDetails())
            .notificationParties(new ArrayList<>(List.of(
                NotificationParty.APPLICANT,
                NotificationParty.RESPONDENT
            )))
            .emailOrLetters(new ArrayList<>(List.of(true, false)))
            .emailTemplate(FR_CONTESTED_HEARING_NOTIFICATION_SOLICITOR)
            .documentsToPost(List.of())
            .build();

        notificationAuditService.createAuditsForCorrespondence(event, EventType.MANAGE_HEARINGS);

        NotificationAuditWrapper wrapper = request.getCaseDetails().getData().getNotificationAuditWrapper();

        assertThat(wrapper.getNotificationsAudits()).hasSize(2);
        assertThat(wrapper.getNotificationsToBeSent()).hasSize(2);

        assertThat(wrapper.getNotificationsAudits())
            .extracting(item -> item.getValue().getParty())
            .containsExactly(
                NotificationParty.APPLICANT.name(),
                NotificationParty.RESPONDENT.name()
            );

        assertThat(wrapper.getNotificationsAudits())
            .extracting(item -> item.getValue().getType())
            .containsExactly(NotificationType.EMAIL, NotificationType.POSTAL);

        assertThat(wrapper.getNotificationsToBeSent())
            .extracting(NotificationToBeSentCollectionItem::getValue)
            .containsExactly(
                wrapper.getNotificationsAudits().get(0).getId(),
                wrapper.getNotificationsAudits().get(1).getId()
            );
    }

    @Test
    void givenExistingAudits_whenCreateAuditsForCorrespondence_thenNewAuditsAreAppended() {
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
            caseData
        );

        SendCorrespondenceEvent event = SendCorrespondenceEvent.builder()
            .caseDetails(request.getCaseDetails())
            .notificationParties(new ArrayList<>(List.of(NotificationParty.APPLICANT)))
            .emailOrLetters(new ArrayList<>(List.of(true)))
            .emailTemplate(FR_CONTESTED_HEARING_NOTIFICATION_SOLICITOR)
            .documentsToPost(List.of())
            .build();

        notificationAuditService.createAuditsForCorrespondence(event, EventType.MANAGE_HEARINGS);

        assertThat(wrapper.getNotificationsAudits()).hasSize(2);
        assertThat(wrapper.getNotificationsToBeSent()).hasSize(2);
        assertThat(wrapper.getNotificationsAudits().getFirst()).isEqualTo(existingAudit);
        assertThat(wrapper.getNotificationsToBeSent().getFirst()).isEqualTo(existingPendingNotification);

        NotificationAuditCollectionItem newAudit = wrapper.getNotificationsAudits().get(1);
        NotificationToBeSentCollectionItem newPendingNotification = wrapper.getNotificationsToBeSent().get(1);

        assertThat(newAudit.getValue().getParty()).isEqualTo(NotificationParty.APPLICANT.name());
        assertThat(newPendingNotification.getValue()).isEqualTo(newAudit.getId());
    }

    @Test
    void givenNoNotificationParties_whenCreateAuditsForCorrespondence_thenNoAuditsAreCreated() {
        FinremCallbackRequest request = buildRequest();

        SendCorrespondenceEvent event = SendCorrespondenceEvent.builder()
            .caseDetails(request.getCaseDetails())
            .notificationParties(new ArrayList<>())
            .emailOrLetters(new ArrayList<>())
            .emailTemplate(FR_CONTESTED_HEARING_NOTIFICATION_SOLICITOR)
            .documentsToPost(List.of())
            .build();

        notificationAuditService.createAuditsForCorrespondence(event, EventType.MANAGE_HEARINGS);

        NotificationAuditWrapper wrapper = request.getCaseDetails().getData().getNotificationAuditWrapper();

        assertThat(wrapper.getNotificationsAudits()).isEmpty();
        assertThat(wrapper.getNotificationsToBeSent()).isEmpty();
    }

    @Test
    void givenNullPostalDocuments_whenCreateAuditsForCorrespondence_thenPostalAuditHasEmptyAttachedDocs() {
        FinremCallbackRequest request = buildRequest();

        SendCorrespondenceEvent event = SendCorrespondenceEvent.builder()
            .caseDetails(request.getCaseDetails())
            .notificationParties(new ArrayList<>(List.of(NotificationParty.RESPONDENT)))
            .emailOrLetters(new ArrayList<>(List.of(false)))
            .emailTemplate(FR_CONTESTED_HEARING_NOTIFICATION_SOLICITOR)
            .documentsToPost(null)
            .build();

        notificationAuditService.createAuditsForCorrespondence(event, EventType.MANAGE_HEARINGS);

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
            caseData
        );
    }
}