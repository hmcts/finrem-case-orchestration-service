package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.managehearing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.ManageHearingsNotificationRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.FinremCorresponder;

import java.util.UUID;

@Component
@Slf4j
public class ManageHearingsCorresponder extends FinremCorresponder {
    // todo - if you don't use anything from abstract class FinremCorresponder, change this from a
    // component to a service.  Can't be a service while extending an abstract class.

    private final ManageHearingsNotificationRequestMapper notificationRequestMapper;

    public ManageHearingsCorresponder(NotificationService notificationService,
                                      ManageHearingsNotificationRequestMapper notificationRequestMapper) {
        super(notificationService);
        this.notificationRequestMapper = notificationRequestMapper;
    }

    // todo
    public void sendHearingNotifications(FinremCallbackRequest callbackRequest) {

        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();
        FinremCaseData finremCaseData = finremCaseDetails.getData();

        // consider breaking following lines into a separate method - "get hearing in context"
        ManageHearingsWrapper manageHearingsWrapper = finremCaseData.getManageHearingsWrapper();
        UUID hearingId = manageHearingsWrapper.getWorkingHearingId();

        Hearing hearing = manageHearingsWrapper.getHearings().stream()
                .filter(h -> h.getId().equals(hearingId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Hearing not found for the given ID: " + hearingId))
                .getValue();
        // end of breaking lines into a separate method

        if (hearingNotificationsShouldBeSent(hearing)) {
            // consider breaking following lines into a separate method - "sendNotificationsToPartiesForHearing"
            hearing.getPartiesOnCaseMultiSelectList()
                    .getValue()
                    .forEach(party -> sendHearingNotificationsByParty(
                            party,
                            finremCaseDetails,
                            hearing));
        }
    }

    // todo
    public boolean hearingNotificationsShouldBeSent(Hearing hearing) {
        return YesOrNo.YES.equals(hearing.getHearingNoticePrompt());
    }

    // todo
    public void sendHearingNotificationsByParty(DynamicMultiSelectListElement party,
                                                FinremCaseDetails finremCaseDetails,
                                                Hearing hearing)
    {
        CaseRole caseRole = CaseRole.forValue(party.getCode());
        switch (caseRole) {
            case CaseRole.APP_SOLICITOR -> {
                getHearingNotificationForApplicantSolicitor(
                        party,
                        finremCaseDetails,
                        hearing);
            }
            case CaseRole.RESP_SOLICITOR -> {
                log.info("Handling case: RESP_SOLICITOR");
            }
            case CaseRole.INTVR_SOLICITOR_1 -> {
                log.info("Handling case: INTVR_SOLICITOR_1");
            }
            case CaseRole.INTVR_SOLICITOR_2 -> {
                log.info("Handling case: INTVR_SOLICITOR_2");
            }
            case CaseRole.INTVR_SOLICITOR_3 -> {
                log.info("Handling case: INTVR_SOLICITOR_3");
            }
            case CaseRole.INTVR_SOLICITOR_4 -> {
                log.info("Handling case: INTVR_SOLICITOR_4");
            }
        }
    }

    // todo
    public void getHearingNotificationForApplicantSolicitor(
            DynamicMultiSelectListElement party,
            FinremCaseDetails finremCaseDetails,
            Hearing hearing)
    {

        NotificationRequest notificationRequest = notificationRequestMapper
                .buildHearingNotificationForApplicantSolicitor(party, finremCaseDetails, hearing);

        notificationService.sendHearingNotificationToApplicant(notificationRequest);

        log.info(notificationRequest.toString());
    }
}
