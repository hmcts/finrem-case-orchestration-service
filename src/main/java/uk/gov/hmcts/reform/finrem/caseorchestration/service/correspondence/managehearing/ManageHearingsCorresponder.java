package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.managehearing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.managehearings.HearingNotificationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.ManageHearingsNotificationRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.ManageHearingsDocumentService;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class ManageHearingsCorresponder {

    private final HearingNotificationHelper hearingNotificationHelper;
    private final ManageHearingsNotificationRequestMapper notificationRequestMapper;
    private final NotificationService notificationService;
    private final ManageHearingsDocumentService manageHearingsDocumentService;
    private final DocumentHelper documentHelper;
    private final BulkPrintService bulkPrintService;

    /**
     * Begins sending hearing correspondence to relevant parties based on the callback request.
     * Loops through each selected party in the hearing and sends using
     *
     * todo - update this javadoc
     *
     * {@link #sendHearingCorrespondenceByParty}.</p>
     *
     * @param callbackRequest the callback request containing case and hearing data
     */
    public void sendHearingCorrespondence(FinremCallbackRequest callbackRequest, String userAuthorisation) {

        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();
        FinremCaseData finremCaseData = finremCaseDetails.getData();

        Hearing hearing = hearingNotificationHelper.getHearingInContext(finremCaseData);

        if (hearingNotificationHelper.shouldNotSendNotification(hearing)) {
            return;
        }

        DynamicMultiSelectList partyList = hearing.getPartiesOnCaseMultiSelectList();
        if (partyList == null || partyList.getValue() == null) {
            return;
        }

        for (DynamicMultiSelectListElement party : partyList.getValue()) {
            sendHearingCorrespondenceByParty(
                    party,
                    finremCaseDetails,
                    hearing,
                    userAuthorisation
            );
        }
    }

    /**
     *
     * Sends a hearing notification to the party specified in parameters.
     *
     * <p>Uses the {@link CaseRole} of the specified party to decide what to send.
     *
     * @param party the dynamic multi-select list element for the party
     * @param finremCaseDetails the case details with detail needed in generating the notification
     * @param hearing the hearing associated with the notification
     */
    private void sendHearingCorrespondenceByParty(DynamicMultiSelectListElement party,
                                                 FinremCaseDetails finremCaseDetails,
                                                 Hearing hearing,
                                                 String userAuthorisation) {
        CaseRole caseRole = CaseRole.forValue(party.getCode());
        switch (caseRole) {
            case CaseRole.APP_SOLICITOR ->
                    processCorrespondenceForApplicant(
                            finremCaseDetails,
                            hearing,
                            userAuthorisation);
            case CaseRole.RESP_SOLICITOR ->
                    log.info("Handling case: RESP_SOLICITOR, work to follow");
            case CaseRole.INTVR_SOLICITOR_1 ->
                    log.info("Handling case: INTVR_SOLICITOR_1, work to follow");
            case CaseRole.INTVR_SOLICITOR_2 ->
                    log.info("Handling case: INTVR_SOLICITOR_2, work to follow");
            case CaseRole.INTVR_SOLICITOR_3 ->
                    log.info("Handling case: INTVR_SOLICITOR_3, work to follow");
            case CaseRole.INTVR_SOLICITOR_4 ->
                    log.info("Handling case: INTVR_SOLICITOR_4, work to follow");
            default -> throw new IllegalStateException(
                    String.format(
                            "Unexpected value: %s for case reference %s",
                            caseRole,
                            finremCaseDetails.getId()
                    )
            );
        }
    }

    /**
     * Builds and sends a hearing notification to the applicant's solicitor.
     * If applicantSolicitorShouldGetEmailNotification is true.
     *
     * <p>This method uses the {@link ManageHearingsNotificationRequestMapper} to create a {@link NotificationRequest}
     * based on the case details and hearing provided. It then delegates the actual sending of the notification
     * to the {@link NotificationService}.</p>
     *
     * @param finremCaseDetails the case details containing relevant information about the hearing and case participants
     * @param hearing the hearing for which the notification is being sent
     *
     *                TODO - check name of methid
     */
    private void processCorrespondenceForApplicant(
            FinremCaseDetails finremCaseDetails,
            Hearing hearing,
            String userAuthorisation) {

        if (hearingNotificationHelper.emailingToApplicantSolicitor(finremCaseDetails)) {

            NotificationRequest notificationRequest = notificationRequestMapper
                    .buildHearingNotificationForApplicantSolicitor(finremCaseDetails, hearing);

            // Todo - test again. Check the contested court email contacts are used for Pres NW Court.

            notificationService.sendHearingNotificationToApplicant(notificationRequest);
            // If FDA or FDR, follow up by emailing certain docs.  DFR-3820 to follow.

            log.info("Notification for applicant solicitor. Request sent for case ID: {}",
                    finremCaseDetails.getId());
        }

        if (hearingNotificationHelper.postingToApplicant(finremCaseDetails)) {

            if (hearingNotificationHelper.shouldSendHearingNoticeOnly(finremCaseDetails, hearing)) {

                CaseDocument hearingNotice = manageHearingsDocumentService
                        .getHearingNotice(finremCaseDetails);

                if (hearingNotice != null) {
                    BulkPrintDocument hearingNoticeDocument =
                            documentHelper.getBulkPrintDocumentFromCaseDocument(hearingNotice);

                    List<BulkPrintDocument> bulkPrintDocuments = new ArrayList<>();
                    bulkPrintDocuments.add(hearingNoticeDocument);

                    bulkPrintService.printApplicantDocuments(
                            finremCaseDetails,
                            userAuthorisation,
                            bulkPrintDocuments
                    );

                    log.info("Posting notice to applicant solicitor. Request sent for case ID: {}",
                            finremCaseDetails.getId());
                } else {
                    log.warn("Hearing notice is null. No document sent for case ID: {}", finremCaseDetails.getId());
                }
            }
            // else send hearing docs too.  Logic to follow.
        }
    }
}
