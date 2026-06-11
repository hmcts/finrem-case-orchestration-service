package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTIFICATIONS_AUDITS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTIFICATIONS_TO_BE_SENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_HEARING_NOTIFICATION_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService.FINANCIAL_REMEDY_PACK_LETTER_TYPE;

@ExtendWith(MockitoExtension.class)
class NotificationAuditServiceTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private NotificationAuditService notificationAuditService;

    @Test
    void givenEmailNotification_whenCreateAuditsForCorrespondence_thenEmailAuditAndPendingIdAreCreated() {
        FinremCallbackRequest request = buildRequest();

        SendCorrespondenceEvent event = buildEvent(
            request,
            List.of(NotificationParty.APPLICANT),
            List.of(true),
            List.of()
        );

        notificationAuditService.createAuditsForCorrespondence(event, EventType.MANAGE_HEARINGS);

        verify(applicationEventPublisher).publishEvent(event);
        assertThat(event.isDryRun()).isTrue();

        NotificationAuditWrapper wrapper = request.getCaseDetails().getData().getNotificationAuditWrapper();
        NotificationAuditCollectionItem auditItem = wrapper.getNotificationsAudits().getFirst();
        NotificationAudit audit = auditItem.getValue();

        assertThat(wrapper.getNotificationsAudits()).hasSize(1);
        assertThat(wrapper.getNotificationsToBeSent()).hasSize(1);
        assertThat(wrapper.getNotificationsToBeSent().getFirst().getId()).isNotNull();
        assertThat(wrapper.getNotificationsToBeSent().getFirst().getValue()).isEqualTo(auditItem.getId());

        assertCommonAuditFields(audit, NotificationParty.APPLICANT, NotificationType.EMAIL);
        assertThat(audit.getEmailTemplate()).isEqualTo(FR_CONTESTED_HEARING_NOTIFICATION_SOLICITOR.name());
        assertThat(audit.getLetterTemplate()).isNull();
        assertThat(audit.getAttachedPostalDocs()).isNull();
    }

    @Test
    void givenPostalNotification_whenCreateAuditsForCorrespondence_thenPostalAuditAndPendingIdAreCreated() {
        FinremCallbackRequest request = buildRequest();

        CaseDocument hearingNotice = CaseDocument.builder()
            .documentFilename("hearingNotice.pdf")
            .build();

        CaseDocument miniFormA = CaseDocument.builder()
            .documentFilename("miniFormA.pdf")
            .build();

        SendCorrespondenceEvent event = buildEvent(
            request,
            List.of(NotificationParty.RESPONDENT),
            List.of(false),
            List.of(hearingNotice, miniFormA)
        );

        notificationAuditService.createAuditsForCorrespondence(event, EventType.MANAGE_HEARINGS);

        verify(applicationEventPublisher).publishEvent(event);
        assertThat(event.isDryRun()).isTrue();

        NotificationAuditWrapper wrapper = request.getCaseDetails().getData().getNotificationAuditWrapper();
        NotificationAuditCollectionItem auditItem = wrapper.getNotificationsAudits().getFirst();
        NotificationAudit audit = auditItem.getValue();

        assertThat(wrapper.getNotificationsAudits()).hasSize(1);
        assertThat(wrapper.getNotificationsToBeSent()).hasSize(1);
        assertThat(wrapper.getNotificationsToBeSent().getFirst().getId()).isNotNull();
        assertThat(wrapper.getNotificationsToBeSent().getFirst().getValue()).isEqualTo(auditItem.getId());

        assertCommonAuditFields(audit, NotificationParty.RESPONDENT, NotificationType.POSTAL);
        assertThat(audit.getEmailTemplate()).isNull();
        assertThat(audit.getLetterTemplate()).isEqualTo(FINANCIAL_REMEDY_PACK_LETTER_TYPE);
        assertThat(audit.getAttachedPostalDocs()).isEqualTo("hearingNotice.pdf, miniFormA.pdf");
    }

    @Test
    void givenExistingAuditsAndMultipleParties_whenCreateAuditsForCorrespondence_thenNewAuditsAreAppended() {
        UUID existingAuditId = UUID.randomUUID();

        NotificationAuditCollectionItem existingAudit = NotificationAuditCollectionItem.builder()
            .id(existingAuditId)
            .value(NotificationAudit.builder()
                .party("existingParty")
                .wasSent(YesOrNo.NO)
                .build())
            .build();

        NotificationToBeSentCollectionItem existingPendingNotification = pendingItem(existingAuditId);

        NotificationAuditWrapper wrapper = NotificationAuditWrapper.builder()
            .notificationsAudits(new ArrayList<>(List.of(existingAudit)))
            .notificationsToBeSent(new ArrayList<>(List.of(existingPendingNotification)))
            .build();

        FinremCallbackRequest request = buildRequest(wrapper);

        SendCorrespondenceEvent event = buildEvent(
            request,
            List.of(NotificationParty.APPLICANT, NotificationParty.RESPONDENT),
            List.of(true, false),
            List.of()
        );

        notificationAuditService.createAuditsForCorrespondence(event, EventType.MANAGE_HEARINGS);

        assertThat(wrapper.getNotificationsAudits()).hasSize(3);
        assertThat(wrapper.getNotificationsToBeSent()).hasSize(3);

        assertThat(wrapper.getNotificationsAudits().get(0)).isEqualTo(existingAudit);
        assertThat(wrapper.getNotificationsToBeSent().getFirst()).isEqualTo(existingPendingNotification);

        NotificationAuditCollectionItem applicantAudit = wrapper.getNotificationsAudits().get(1);
        NotificationAuditCollectionItem respondentAudit = wrapper.getNotificationsAudits().get(2);

        assertThat(applicantAudit.getValue().getParty()).isEqualTo(NotificationParty.APPLICANT.name());
        assertThat(applicantAudit.getValue().getType()).isEqualTo(NotificationType.EMAIL);

        assertThat(respondentAudit.getValue().getParty()).isEqualTo(NotificationParty.RESPONDENT.name());
        assertThat(respondentAudit.getValue().getType()).isEqualTo(NotificationType.POSTAL);

        assertThat(wrapper.getNotificationsToBeSent())
            .extracting(NotificationToBeSentCollectionItem::getValue)
            .containsExactly(
                existingAuditId,
                applicantAudit.getId(),
                respondentAudit.getId()
            );
    }

    @Test
    void givenNullPostalDocuments_whenCreateAuditsForCorrespondence_thenAttachedPostalDocsIsEmpty() {
        FinremCallbackRequest request = buildRequest();

        SendCorrespondenceEvent event = buildEvent(
            request,
            List.of(NotificationParty.RESPONDENT),
            List.of(false),
            null
        );

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

    @Test
    void givenNoPendingNotifications_whenMarkPendingNotificationsAsSent_thenEmptyMapIsReturned() {
        NotificationAuditCollectionItem audit = auditItem(UUID.randomUUID(), YesOrNo.NO);

        FinremCaseData caseData = FinremCaseData.builder()
            .notificationAuditWrapper(NotificationAuditWrapper.builder()
                .notificationsAudits(new ArrayList<>(List.of(audit)))
                .notificationsToBeSent(new ArrayList<>())
                .build())
            .build();

        SendCorrespondenceEvent sentEvent = SendCorrespondenceEvent.builder().build();
        Map<String, Object> result =
            notificationAuditService.markPendingNotificationsAsSent(caseData, sentEvent);


        assertThat(result).isEmpty();
        assertThat(audit.getValue().getWasSent()).isEqualTo(YesOrNo.NO);
        verifyNoInteractions(objectMapper);
    }

    @Test
    void givenPendingNotificationIds_whenMarkPendingNotificationsAsSent_thenMatchingAuditsAreMarkedAsSentAndPendingListIsCleared() {
        UUID pendingAuditId = UUID.randomUUID();
        UUID nonPendingAuditId = UUID.randomUUID();

        NotificationAuditCollectionItem pendingAudit = auditItem(pendingAuditId, YesOrNo.NO);
        NotificationAuditCollectionItem nonPendingAudit = auditItem(nonPendingAuditId, YesOrNo.NO);

        List<NotificationAuditCollectionItem> audits = new ArrayList<>(List.of(pendingAudit, nonPendingAudit));
        List<Map<String, Object>> convertedAudits = List.of(Map.of("converted", true));

        FinremCaseData caseData = FinremCaseData.builder()
            .notificationAuditWrapper(NotificationAuditWrapper.builder()
                .notificationsAudits(audits)
                .notificationsToBeSent(new ArrayList<>(List.of(pendingItem(pendingAuditId))))
                .build())
            .build();

        when(objectMapper.convertValue(any(Object.class), eq(List.class))).thenReturn(convertedAudits);

        SendCorrespondenceEvent sentEvent = SendCorrespondenceEvent.builder()
            .notificationParties(new ArrayList<>(List.of(NotificationParty.APPLICANT)))
            .build();

        sentEvent.recordEmailNotification(NotificationParty.APPLICANT);

        Map<String, Object> result =
            notificationAuditService.markPendingNotificationsAsSent(caseData, sentEvent);

        assertThat(pendingAudit.getValue().getWasSent()).isEqualTo(YesOrNo.YES);
        assertThat(nonPendingAudit.getValue().getWasSent()).isEqualTo(YesOrNo.NO);

        assertThat(result)
            .containsEntry(NOTIFICATIONS_AUDITS, convertedAudits)
            .containsEntry(NOTIFICATIONS_TO_BE_SENT, List.of());
    }

    @Test
    void givenOnlyApplicantWasSent_whenMarkPendingNotificationsAsSent_thenApplicantIsYesRespondentStaysNoAndQueueClears() {
        UUID applicantAuditId = UUID.randomUUID();
        UUID respondentAuditId = UUID.randomUUID();

        NotificationAudit applicantAudit = NotificationAudit.builder()
            .party(NotificationParty.APPLICANT.name())
            .wasSent(YesOrNo.NO)
            .build();

        NotificationAudit respondentAudit = NotificationAudit.builder()
            .party(NotificationParty.RESPONDENT.name())
            .wasSent(YesOrNo.NO)
            .build();

        List<NotificationAuditCollectionItem> audits = List.of(
            NotificationAuditCollectionItem.builder()
                .id(applicantAuditId)
                .value(applicantAudit)
                .build(),
            NotificationAuditCollectionItem.builder()
                .id(respondentAuditId)
                .value(respondentAudit)
                .build()
        );

        NotificationAuditWrapper wrapper = NotificationAuditWrapper.builder()
            .notificationsAudits(audits)
            .notificationsToBeSent(List.of(
                pendingItem(applicantAuditId),
                pendingItem(respondentAuditId)
            ))
            .build();

        FinremCaseData caseData = FinremCaseData.builder()
            .notificationAuditWrapper(wrapper)
            .build();

        SendCorrespondenceEvent sentEvent = SendCorrespondenceEvent.builder()
            .notificationParties(new ArrayList<>(List.of(
                NotificationParty.APPLICANT,
                NotificationParty.RESPONDENT
            )))
            .build();

        sentEvent.recordEmailNotification(NotificationParty.APPLICANT);

        List<Map<String, Object>> convertedAudits = List.of(Map.of("dummy", "value"));

        when(objectMapper.convertValue(any(Object.class), eq(List.class)))
            .thenReturn(convertedAudits);

        Map<String, Object> result =
            notificationAuditService.markPendingNotificationsAsSent(caseData, sentEvent);

        assertThat(applicantAudit.getWasSent()).isEqualTo(YesOrNo.YES);
        assertThat(respondentAudit.getWasSent()).isEqualTo(YesOrNo.NO);

        assertThat(result).containsEntry(NOTIFICATIONS_AUDITS, convertedAudits)
            .containsEntry(NOTIFICATIONS_TO_BE_SENT, List.of());
    }

    private void assertCommonAuditFields(NotificationAudit audit,
                                         NotificationParty party,
                                         NotificationType notificationType) {
        assertThat(audit.getCreatedAt()).isEqualTo(LocalDate.now());
        assertThat(audit.getWasSent()).isEqualTo(YesOrNo.NO);
        assertThat(audit.getEventId()).isEqualTo(EventType.MANAGE_HEARINGS.getCcdType());
        assertThat(audit.getParty()).isEqualTo(party.name());
        assertThat(audit.getType()).isEqualTo(notificationType);
    }

    private NotificationAuditCollectionItem auditItem(UUID id, YesOrNo wasSent) {
        return NotificationAuditCollectionItem.builder()
            .id(id)
            .value(NotificationAudit.builder()
                .party(NotificationParty.APPLICANT.name())
                .wasSent(wasSent)
                .build())
            .build();
    }

    private NotificationToBeSentCollectionItem pendingItem(UUID auditId) {
        return NotificationToBeSentCollectionItem.builder()
            .id(UUID.randomUUID())
            .value(auditId)
            .build();
    }

    private SendCorrespondenceEvent buildEvent(FinremCallbackRequest request,
                                               List<NotificationParty> parties,
                                               List<Boolean> emailOrLetters,
                                               List<CaseDocument> documentsToPost) {
        return SendCorrespondenceEvent.builder()
            .caseDetails(request.getCaseDetails())
            .notificationParties(new ArrayList<>(parties))
            .emailOrLetters(new ArrayList<>(emailOrLetters))
            .emailTemplate(FR_CONTESTED_HEARING_NOTIFICATION_SOLICITOR)
            .documentsToPost(documentsToPost)
            .build();
    }

    private FinremCallbackRequest buildRequest() {
        return buildRequest(NotificationAuditWrapper.builder().build());
    }

    private FinremCallbackRequest buildRequest(NotificationAuditWrapper notificationAuditWrapper) {
        FinremCaseData caseData = FinremCaseData.builder()
            .notificationAuditWrapper(notificationAuditWrapper)
            .build();

        return FinremCallbackRequestFactory.from(
            Long.parseLong(TestConstants.CASE_ID),
            CaseType.CONTESTED,
            caseData
        );
    }
}