package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.managehearing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.managehearings.HearingCorrespondenceHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.ManageHearingsNotificationRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.PartyOnCase;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.PartyOnCaseCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.ManageHearingsDocumentService;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@RequiredArgsConstructor
@Service
@Slf4j
public class ManageHearingsCorresponder {

    private final HearingCorrespondenceHelper hearingCorrespondenceHelper;
    private final ManageHearingsNotificationRequestMapper notificationRequestMapper;
    private final NotificationService notificationService;
    private final ManageHearingsDocumentService manageHearingsDocumentService;
    private final DocumentHelper documentHelper;
    private final BulkPrintService bulkPrintService;

    /**
     * Begin sending hearing correspondence to parties, included based on the callback request data.
     * No notifications, notices or documents sent if the User has specified that.
     * No notifications, notices or documents sent if the party list is empty.
     * Loops through each selected party in the hearing and sends using
     * {@link #sendHearingCorrespondenceByParty}.</p>
     * @param callbackRequest the callback request containing case and hearing data
     * @param userAuthorisation the user authorisation token
     */
    public void sendHearingCorrespondence(FinremCallbackRequest callbackRequest, String userAuthorisation) {

        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();
        FinremCaseData finremCaseData = finremCaseDetails.getData();

        Hearing hearing = hearingCorrespondenceHelper.getHearingInContext(finremCaseData);

        if (hearingCorrespondenceHelper.shouldNotSendNotification(hearing)) {
            return;
        }

        List<PartyOnCaseCollection> partiesOnCaseCollection = hearing.getPartiesOnCase();
        if (partiesOnCaseCollection == null || partiesOnCaseCollection.isEmpty()) {
            return;
        }

        for (PartyOnCaseCollection partyCollection : partiesOnCaseCollection) {
            PartyOnCase party = partyCollection.getValue();
            if (party != null) {
                sendHearingCorrespondenceByParty(
                    party.getRole(),
                    finremCaseDetails,
                    hearing,
                    userAuthorisation
                );
            }
        }
    }

    /**
     * Sends a hearing notification to the party specified in parameters.
     *
     * <p>Uses the {@link CaseRole} of the specified party to decide what to send.</p>
     * @param role the role of the party on the case, representing their case role
     * @param finremCaseDetails the case details with detail needed in generating the notification
     * @param hearing the hearing associated with the notification
     * @param userAuthorisation the user authorisation token
     */
    private void sendHearingCorrespondenceByParty(String role,
                                                  FinremCaseDetails finremCaseDetails,
                                                  Hearing hearing,
                                                  String userAuthorisation) {
        CaseRole caseRole = CaseRole.forValue(role);
        switch (caseRole) {
            case CaseRole.APP_SOLICITOR ->
                processCorrespondenceForApplicant(
                    finremCaseDetails,
                    hearing,
                    userAuthorisation);
            case CaseRole.RESP_SOLICITOR ->
                processCorrespondenceForRespondent(
                    finremCaseDetails,
                    hearing,
                    userAuthorisation);
            case CaseRole.INTVR_SOLICITOR_1, CaseRole.INTVR_SOLICITOR_2, CaseRole.INTVR_SOLICITOR_3, CaseRole.INTVR_SOLICITOR_4 ->
                processCorrespondenceForIntervener(
                    finremCaseDetails,
                    hearing,
                    userAuthorisation,
                    caseRole);
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
     * Processes correspondence for the applicant's solicitor.
     * Calls ProcessCorrespondenceForParty with the appropriate parameters.
     * Lambdas are passed to ProcessCorrespondenceForParty, so the information is lazy-loaded if needed.
     *
     * @param finremCaseDetails the case details containing relevant information about the hearing and case participants
     * @param hearing the hearing for which the notification is being sent
     * @param userAuthorisation the user authorisation token for sending notifications
     */
    private void processCorrespondenceForApplicant(FinremCaseDetails finremCaseDetails, Hearing hearing, String userAuthorisation) {
        processCorrespondenceForParty(
            finremCaseDetails,
            hearing,
            CaseRole.APP_SOLICITOR,
            userAuthorisation,
            () -> hearingCorrespondenceHelper.shouldEmailToApplicantSolicitor(finremCaseDetails),
            () -> hearingCorrespondenceHelper.shouldPostToApplicant(finremCaseDetails),
            () -> notificationRequestMapper.buildHearingNotificationForApplicantSolicitor(finremCaseDetails, hearing)
        );
    }

    /**
     * Processes correspondence for the respondent's solicitor.
     * Calls ProcessCorrespondenceForParty with the appropriate parameters.
     * Lambdas are passed to ProcessCorrespondenceForParty, so the information is lazy-loaded if needed.
     *
     * @param finremCaseDetails the case details containing relevant information about the hearing and case participants
     * @param hearing the hearing for which the notification is being sent
     * @param userAuthorisation the user authorisation token for sending notifications
     */
    private void processCorrespondenceForRespondent(FinremCaseDetails finremCaseDetails, Hearing hearing, String userAuthorisation) {
        processCorrespondenceForParty(
            finremCaseDetails,
            hearing,
            CaseRole.RESP_SOLICITOR,
            userAuthorisation,
            () -> hearingCorrespondenceHelper.shouldEmailToRespondentSolicitor(finremCaseDetails),
            () -> hearingCorrespondenceHelper.shouldPostToRespondent(finremCaseDetails),
            () -> notificationRequestMapper.buildHearingNotificationForRespondentSolicitor(finremCaseDetails, hearing)
        );
    }

    /**
     * Processes correspondence for Interveners. Uses CaseRole to determine which Intervener.
     * Calls ProcessCorrespondenceForParty with the appropriate parameters.
     * Lambdas are passed to ProcessCorrespondenceForParty, so the information is lazy-loaded if needed.
     *
     * @param finremCaseDetails the case details containing relevant information about the hearing and case participants
     * @param hearing the hearing for which the notification is being sent
     * @param userAuthorisation the user authorisation token for sending notifications
     */
    private void processCorrespondenceForIntervener(FinremCaseDetails finremCaseDetails, Hearing hearing,
                                                     String userAuthorisation, CaseRole caseRole) {
        processCorrespondenceForParty(
            finremCaseDetails,
            hearing,
            caseRole,
            userAuthorisation,
            () -> hearingCorrespondenceHelper.shouldEmailToIntervener(finremCaseDetails, caseRole),
            () -> hearingCorrespondenceHelper.shouldPostToIntervener(finremCaseDetails, caseRole),
            () -> notificationRequestMapper.buildHearingNotificationForIntervenerSolicitor(
                finremCaseDetails,
                hearing,
                caseRole)
        );
    }

    /**
     * Common logic to send hearing correspondence to parties.
     * Party-specific logic is determined by the calling method.
     * Determines whether to send email notifications post documents.
     * @param finremCaseDetails the case details containing relevant information about the hearing and case participants
     * @param hearing the hearing for which the notification is being sent
     *
     *                change javadoc
     */
    private void processCorrespondenceForParty(
        FinremCaseDetails finremCaseDetails,
        // If FDA or FDR, follow up by emailing certain docs.  Future planned work will address this.
        Hearing hearing,
        CaseRole caseRole,
        String userAuthorisation,
        Supplier<Boolean> shouldEmailPartySolicitor,
        Supplier<Boolean> shouldPostToParty,
        Supplier<NotificationRequest> notificationRequestSupplier) {

        if (shouldEmailPartySolicitor.get()) {
            notificationService.sendHearingNotificationToSolicitor(
                notificationRequestSupplier.get(),
                caseRole.toString()
            );
        }

        if (shouldPostToParty.get()) {

            if (hearingCorrespondenceHelper.shouldSendHearingNoticeOnly(finremCaseDetails, hearing)) {

                CaseDocument hearingNotice = manageHearingsDocumentService
                    .getHearingNotice(finremCaseDetails);

                if (hearingNotice != null) {
                    BulkPrintDocument hearingNoticeDocument =
                        documentHelper.getBulkPrintDocumentFromCaseDocument(hearingNotice);

                    List<BulkPrintDocument> bulkPrintDocuments = new ArrayList<>();
                    bulkPrintDocuments.add(hearingNoticeDocument);

                    printDocuments(
                        finremCaseDetails,
                        userAuthorisation,
                        bulkPrintDocuments,
                        caseRole
                    );

                    log.info("Request sent to Bulk Print to post notice to the {} party. Request sent for case ID: {}",
                        caseRole, finremCaseDetails.getId());
                } else {
                    log.warn("Hearing notice is null. No document sent for case ID: {}", finremCaseDetails.getId());
                }
            }
            // Else send hearing docs too.  Future planned work will address this.
        }
    }

    /**
     * Prints documents for the specified case role.
     * Uses the {@link BulkPrintService} to send the documents for printing.
     *
     * @param finremCaseDetails the case details containing relevant information about the hearing and case participants
     * @param userAuthorisation the user authorisation token for sending notifications
     * @param bulkPrintDocuments the list of documents to be printed
     * @param caseRole the case role for which the documents are being printed
     */
    private void printDocuments(FinremCaseDetails finremCaseDetails, String userAuthorisation,
                                List<BulkPrintDocument> bulkPrintDocuments, CaseRole caseRole) {
        switch (caseRole) {
            case CaseRole.APP_SOLICITOR ->
                bulkPrintService.printApplicantDocuments(
                    finremCaseDetails,
                    userAuthorisation,
                    bulkPrintDocuments);
            case CaseRole.RESP_SOLICITOR ->
                bulkPrintService.printRespondentDocuments(
                    finremCaseDetails,
                    userAuthorisation,
                    bulkPrintDocuments);
            case CaseRole.INTVR_SOLICITOR_1 ->
                // Likely to need the wrapper to be populated correctly first.
                bulkPrintService.printIntervenerDocuments(
                    finremCaseDetails.getData().getIntervenerOneWrapperIfPopulated(),
                    finremCaseDetails,
                    userAuthorisation,
                    bulkPrintDocuments);
            case CaseRole.INTVR_SOLICITOR_2 -> log.info("print for: INTVR_SOLICITOR_2, work to follow");
            case CaseRole.INTVR_SOLICITOR_3 -> log.info("print for: INTVR_SOLICITOR_3, work to follow");
            case CaseRole.INTVR_SOLICITOR_4 -> log.info("print for: INTVR_SOLICITOR_4, work to follow");
            default -> throw new IllegalStateException(
                String.format(
                    "Unexpected value: %s for case reference %s",
                    caseRole,
                    finremCaseDetails.getId()
                )
            );
        }
    }
}
