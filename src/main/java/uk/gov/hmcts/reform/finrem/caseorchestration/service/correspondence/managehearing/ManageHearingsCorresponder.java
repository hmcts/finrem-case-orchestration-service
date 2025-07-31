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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.PartyOnCase;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.PartyOnCaseCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.ManageHearingsDocumentService;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

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

        List<PartyOnCaseCollectionItem> partiesOnCaseCollection = hearing.getPartiesOnCase();
        if (partiesOnCaseCollection == null || partiesOnCaseCollection.isEmpty()) {
            return;
        }

        for (PartyOnCaseCollectionItem partyCollection : partiesOnCaseCollection) {
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
                processCorrespondenceForApplicant(finremCaseDetails, hearing, userAuthorisation);
            case CaseRole.RESP_SOLICITOR ->
                processCorrespondenceForRespondent(finremCaseDetails, hearing, userAuthorisation);
            case CaseRole.INTVR_SOLICITOR_1 ->
                processCorrespondenceForIntervenerOne(finremCaseDetails, hearing, userAuthorisation, caseRole);
            case CaseRole.INTVR_SOLICITOR_2 ->
                processCorrespondenceForIntervenerTwo(finremCaseDetails, hearing, userAuthorisation, caseRole);
            case CaseRole.INTVR_SOLICITOR_3 ->
                processCorrespondenceForIntervenerThree(finremCaseDetails, hearing, userAuthorisation, caseRole);
            case CaseRole.INTVR_SOLICITOR_4 ->
                processCorrespondenceForIntervenerFour(finremCaseDetails, hearing, userAuthorisation, caseRole);
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
     * Used by processCorrespondenceForIntervener.  Forwards Intervener One data to processCorrespondenceForIntervener.
     */
    private void processCorrespondenceForIntervenerOne(FinremCaseDetails finremCaseDetails, Hearing hearing,
                                                       String auth, CaseRole role) {
        IntervenerWrapper intervenerOne = finremCaseDetails.getData().getIntervenerOneWrapperIfPopulated();

        if (isIntervenerDataIncomplete(intervenerOne)) {
            log.warn("Intervener One has no addresses for case ID: {}. Hearing correspondence not processed.", finremCaseDetails.getId());
            return;
        }

        processCorrespondenceForIntervener(finremCaseDetails, hearing, auth, role, intervenerOne);
    }

    /**
     * Used by processCorrespondenceForIntervener. Forwards Intervener Two data to processCorrespondenceForIntervener.
     */
    private void processCorrespondenceForIntervenerTwo(FinremCaseDetails finremCaseDetails, Hearing hearing,
                                                       String auth, CaseRole role) {
        IntervenerWrapper intervenerTwo = finremCaseDetails.getData().getIntervenerTwoWrapperIfPopulated();

        if (isIntervenerDataIncomplete(intervenerTwo)) {
            log.warn("Intervener Two has no addresses for case ID: {}. Hearing correspondence not processed.", finremCaseDetails.getId());
            return;
        }

        processCorrespondenceForIntervener(finremCaseDetails, hearing, auth, role, intervenerTwo);
    }

    /**
     * Used by processCorrespondenceForIntervener. Forwards Intervener Three data to processCorrespondenceForIntervener.
     */
    private void processCorrespondenceForIntervenerThree(FinremCaseDetails finremCaseDetails, Hearing hearing,
                                                         String auth, CaseRole role) {
        IntervenerWrapper intervenerThree = finremCaseDetails.getData().getIntervenerThreeWrapperIfPopulated();

        if (isIntervenerDataIncomplete(intervenerThree)) {
            log.warn("Intervener Three has no addresses for case ID: {}. Hearing correspondence not processed.", finremCaseDetails.getId());
            return;
        }

        processCorrespondenceForIntervener(finremCaseDetails, hearing, auth, role, intervenerThree);
    }

    /**
     * Used by processCorrespondenceForIntervener. Forwards Intervener Four data to processCorrespondenceForIntervener.
     */
    private void processCorrespondenceForIntervenerFour(FinremCaseDetails finremCaseDetails, Hearing hearing,
                                                        String auth, CaseRole role) {
        IntervenerWrapper intervenerFour = finremCaseDetails.getData().getIntervenerFourWrapperIfPopulated();

        if (isIntervenerDataIncomplete(intervenerFour)) {
            log.warn("Intervener Four has no addresses for case ID: {}. Hearing correspondence not processed.", finremCaseDetails.getId());
            return;
        }

        processCorrespondenceForIntervener(finremCaseDetails, hearing, auth, role, intervenerFour);
    }

    /**
     * Returns true if we can't post to, or email the intervener.
     * @param intervener the intervener wrapper containing the data for the intervener
     * @return true if the intervener data is insufficient, false is an email address or postal address is available.
     */
    private boolean isIntervenerDataIncomplete(IntervenerWrapper intervener) {
        return intervener == null
            || !intervener.isIntervenerSolicitorPopulated()
            && intervener.getIntervenerAddress() == null;
    }

    /**
     * Used by the methods processCorrespondenceForIntervenerOne to processCorrespondenceForIntervenerFour
     * encapsulates the common logic for sending hearing correspondence for each intervener.
     */
    private void processCorrespondenceForIntervener(FinremCaseDetails finremCaseDetails,
                                                    Hearing hearing,
                                                    String userAuthorisation,
                                                    CaseRole caseRole,
                                                    IntervenerWrapper intervener) {

        BooleanSupplier shouldEmailSupplier = () -> YesOrNo.YES.equals(intervener.getIntervenerRepresented());
        BooleanSupplier shouldPostSupplier = () -> !shouldEmailSupplier.getAsBoolean();

        processCorrespondenceForParty(
            finremCaseDetails,
            hearing,
            caseRole,
            userAuthorisation,
            shouldEmailSupplier,
            shouldPostSupplier,
            () -> notificationRequestMapper.buildHearingNotificationForIntervenerSolicitor(
                finremCaseDetails,
                hearing,
                intervener)
        );
    }

    /**
     * Common logic to send hearing correspondence to parties.
     * Party-specific logic is determined by the calling method.
     * Determines whether to send email notifications post documents.
     * @param finremCaseDetails the case details containing relevant information about the hearing and case participants
     * @param hearing the hearing for which the notification is being sent
     */
    private void processCorrespondenceForParty(
        FinremCaseDetails finremCaseDetails,
        Hearing hearing,
        CaseRole caseRole,
        String userAuthorisation,
        BooleanSupplier shouldEmailPartySolicitor,
        BooleanSupplier shouldPostToParty,
        Supplier<NotificationRequest> notificationRequestSupplier) {

        if (shouldEmailPartySolicitor.getAsBoolean()) {
            notificationService.sendHearingNotificationToSolicitor(
                notificationRequestSupplier.get(),
                caseRole.toString()
            );
        }

        if (shouldPostToParty.getAsBoolean()) {
            if (hearingCorrespondenceHelper.shouldPostHearingNoticeOnly(finremCaseDetails, hearing)) {
                postHearingNoticeOnly(finremCaseDetails, caseRole, userAuthorisation);
            }
            if (hearingCorrespondenceHelper.shouldPostAllHearingDocuments(finremCaseDetails, hearing)) {
                postAllHearingDocuments(finremCaseDetails, caseRole, userAuthorisation);
            }
        }
    }

    /**
     * Gets the hearing notice then sends it to the Bulk Print service.
     * @param finremCaseDetails the case details.
     * @param caseRole the case role of the party to whom the notice is being sent.
     * @param userAuthorisation the user authorisation token.
     */
    private void postHearingNoticeOnly(FinremCaseDetails finremCaseDetails, CaseRole caseRole, String userAuthorisation) {
        CaseDocument hearingNotice = manageHearingsDocumentService
            .getHearingNotice(finremCaseDetails);

        if (hearingNotice == null) {
            log.warn("Hearing notice is null. No document sent to {} for case ID: {}", caseRole, finremCaseDetails.getId());
            return;
        }

        List<CaseDocument> hearingDocuments = manageHearingsDocumentService
            .getHearingDocumentsToPost(finremCaseDetails);

        hearingDocuments.add(hearingNotice);

        List<BulkPrintDocument> bulkPrintDocuments =
            documentHelper.getCaseDocumentsAsBulkPrintDocuments(hearingDocuments);

        printDocuments(
            finremCaseDetails,
            userAuthorisation,
            bulkPrintDocuments,
            caseRole
        );

        log.info("Request sent to Bulk Print to post notice to the {} party. Request sent for case ID: {}",
            caseRole, finremCaseDetails.getId());
    }

    /**
     * Gets the correct hearing documents then sends them to the Bulk Print service.
     * @param finremCaseDetails the case details.
     * @param caseRole the case role of the party to whom the documents are being sent.
     * @param userAuthorisation the user authorisation token.
     */
    private void postAllHearingDocuments(FinremCaseDetails finremCaseDetails, CaseRole caseRole, String userAuthorisation) {
        List<CaseDocument> hearingDocuments = manageHearingsDocumentService
            .getHearingDocumentsToPost(finremCaseDetails);

        hearingDocuments.addAll(manageHearingsDocumentService.getAdditonalHearingDocument(finremCaseDetails));

        if (isEmpty(hearingDocuments)) {
            log.warn("No hearing documents found. No documents sent for case ID: {}", finremCaseDetails.getId());
            return;
        }

        List<BulkPrintDocument> bulkPrintDocuments =
            documentHelper.getCaseDocumentsAsBulkPrintDocuments(hearingDocuments);

        printDocuments(
            finremCaseDetails,
            userAuthorisation,
            bulkPrintDocuments,
            caseRole
        );

        log.info("Request sent to Bulk Print to post hearing documents to the {} party. Request sent for case ID: {}",
            caseRole, finremCaseDetails.getId());
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
                bulkPrintService.printApplicantDocuments(finremCaseDetails, userAuthorisation, bulkPrintDocuments);
            case CaseRole.RESP_SOLICITOR ->
                bulkPrintService.printRespondentDocuments(finremCaseDetails, userAuthorisation, bulkPrintDocuments);
            case CaseRole.INTVR_SOLICITOR_1 -> {
                IntervenerWrapper intervenerOne =
                    finremCaseDetails.getData().getIntervenerOneWrapperIfPopulated();
                bulkPrintService.printIntervenerDocuments(intervenerOne, finremCaseDetails, userAuthorisation, bulkPrintDocuments);
            }
            case CaseRole.INTVR_SOLICITOR_2 -> {
                IntervenerWrapper intervenerTwo =
                    finremCaseDetails.getData().getIntervenerTwoWrapperIfPopulated();
                bulkPrintService.printIntervenerDocuments(intervenerTwo, finremCaseDetails, userAuthorisation, bulkPrintDocuments);
            }
            case CaseRole.INTVR_SOLICITOR_3 -> {
                IntervenerWrapper intervenerThree =
                    finremCaseDetails.getData().getIntervenerThreeWrapperIfPopulated();
                bulkPrintService.printIntervenerDocuments(intervenerThree, finremCaseDetails, userAuthorisation, bulkPrintDocuments);
            }
            case CaseRole.INTVR_SOLICITOR_4 -> {
                IntervenerWrapper intervenerFour =
                    finremCaseDetails.getData().getIntervenerFourWrapperIfPopulated();
                bulkPrintService.printIntervenerDocuments(intervenerFour, finremCaseDetails, userAuthorisation, bulkPrintDocuments);
            }
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
