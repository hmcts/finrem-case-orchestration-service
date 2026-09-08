package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.sendorder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.notificationrequest.FinremNotificationRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerHearingNotice;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerHearingNoticeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrderSentToPartiesCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTEST_ORDER_APPROVED_APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTEST_ORDER_APPROVED_INTERVENER1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTEST_ORDER_APPROVED_INTERVENER2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTEST_ORDER_APPROVED_INTERVENER3;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTEST_ORDER_APPROVED_INTERVENER4;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTEST_ORDER_APPROVED_RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.RESPONDENT;

@RequiredArgsConstructor
@Service
@Slf4j
public class SendOrderCorresponder {

    private final FinremNotificationRequestMapper finremNotificationRequestMapper;

    private final NotificationService notificationService;

    private final Clock clock;

    public List<SendCorrespondenceEvent> buildCorrespondenceEventIfNeeded(
        FinremCallbackRequest callbackRequest,
        String userAuthorisation) {

        List<SendCorrespondenceEvent> ret = new ArrayList<>();

        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();

        // Applicant
        ret.add(baseEventBuilder(
            List.of(APPLICANT),
            finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(finremCaseDetails),
            finremCaseDetails,
            userAuthorisation,
            FR_CONTEST_ORDER_APPROVED_APPLICANT,
            getDocumentsToPostForApplicantAndRespondent(finremCaseDetails)
        ).build());

        // Respondent
        ret.add(baseEventBuilder(
            List.of(RESPONDENT),
            finremNotificationRequestMapper.getNotificationRequestForRespondentSolicitor(finremCaseDetails),
            finremCaseDetails,
            userAuthorisation,
            FR_CONTEST_ORDER_APPROVED_RESPONDENT,
            getDocumentsToPostForApplicantAndRespondent(finremCaseDetails)
        ).build());

        // Interveners
        finremCaseDetails.getData().getInterveners().stream()
            .filter(IntervenerWrapper::isPresent)
            .forEach(intervener ->
            ret.add(
                baseEventBuilder(
                    List.of(NotificationParty.getIntervener(intervener.getIntervenerType())),
                    finremNotificationRequestMapper.getNotificationRequestForIntervenerSolicitor(finremCaseDetails,
                        notificationService.getCaseDataKeysForIntervenerSolicitor(intervener)),
                    finremCaseDetails,
                    userAuthorisation,
                    getIntervenerEmailTemplate(intervener.getIntervenerType()),
                    getDocumentsToPostForIntervener(finremCaseDetails, intervener)
                ).build()
            )
        );

        return ret;
    }

    private SendCorrespondenceEvent.SendCorrespondenceEventBuilder baseEventBuilder(
        List<NotificationParty> notificationParties,
        NotificationRequest notificationRequest,
        FinremCaseDetails caseDetails,
        String userAuthorisation,
        EmailTemplateNames templateName,
        List<CaseDocument> documentsToPost) {
        return SendCorrespondenceEvent.builder()
            .notificationParties(notificationParties)
            .checkNotificationPartySelection(true) // Depends on question "Who should receive this order?"
            .emailNotificationRequest(notificationRequest)
            .emailTemplate(templateName)
            .documentsToPost(documentsToPost)
            .caseDetails(caseDetails)
            .authToken(userAuthorisation);
    }

    private EmailTemplateNames getIntervenerEmailTemplate(IntervenerType intervener) {
        switch (intervener) {
            case INTERVENER_ONE -> {
                return FR_CONTEST_ORDER_APPROVED_INTERVENER1;
            }
            case INTERVENER_TWO -> {
                return FR_CONTEST_ORDER_APPROVED_INTERVENER2;
            }
            case INTERVENER_THREE -> {
                return FR_CONTEST_ORDER_APPROVED_INTERVENER3;
            }
            case INTERVENER_FOUR -> {
                return FR_CONTEST_ORDER_APPROVED_INTERVENER4;
            }
            default -> {
                return FR_CONTEST_ORDER_APPROVED_INTERVENER1;
            }
        }
    }

    private List<CaseDocument> getDocumentsToPostForApplicantAndRespondent(FinremCaseDetails caseDetails) {
        // Copied from FinremContestedSendOrderCorresponder.getCaseDocuments
        FinremCaseData finremCaseData = caseDetails.getData();
        List<OrderSentToPartiesCollection> sentToPartiesCollection = finremCaseData.getOrdersSentToPartiesCollection();
        List<CaseDocument> caseDocuments = new ArrayList<>();
        sentToPartiesCollection.forEach(sendOrderObj -> caseDocuments.add(sendOrderObj.getValue().getCaseDocument()));
        return caseDocuments;
    }

    private List<CaseDocument> getDocumentsToPostForIntervener(FinremCaseDetails caseDetails,
                                                               IntervenerWrapper intervenerWrapper) {
        // Copied from FinremMultiLetterOrEmailAllPartiesCorresponder.returnAndAddCaseDocumentsToIntervenerHearingNotices

        List<CaseDocument> caseDocuments = getDocumentsToPostForApplicantAndRespondent(caseDetails);
        List<IntervenerHearingNoticeCollection> intervenerHearingNoticesCollection =
            intervenerWrapper.getIntervenerHearingNoticesCollection(caseDetails.getData());
        caseDocuments.forEach(cd -> intervenerHearingNoticesCollection.add(getHearingNoticesDocumentCollection(cd)));
        return caseDocuments;
    }

    private IntervenerHearingNoticeCollection getHearingNoticesDocumentCollection(CaseDocument hearingNotice) {
        return IntervenerHearingNoticeCollection.builder()
            .value(IntervenerHearingNotice.builder().caseDocument(hearingNotice)
                .noticeReceivedAt(LocalDateTime.now(clock)).build()).build();
    }
}
