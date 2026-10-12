package uk.gov.hmcts.reform.finrem.caseorchestration.handler.citizenui.documentupload;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremAboutToSubmitCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenDocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.CitizenDocumentWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CorrespondenceEventAuditOrchestrationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.citizen.CitizenDocumentsUploadedCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentsorter.CitizenDocumentSorter;

import java.util.List;

/**
 * Abstract handler for CUI document upload events (Applicant/Respondent).
 *
 * <p>This handler is responsible for:
 * <ul>
 *     <li>Merging newly uploaded documents from {@code currentCaseData}</li>
 *     <li>With existing documents from {@code dataBefore}</li>
 *     <li>Sorting the combined collection in descending order of upload datetime</li>
 *     <li>Setting the updated collection back onto {@code currentCaseData}</li>
 * </ul>
 *
 * <p><b>Important:</b>
 * In CCD callbacks, the entire collection must be returned. Returning only the newly
 * uploaded document would overwrite existing documents. Therefore, this handler always
 * merges {@code dataBefore + currentCaseData}.
 *
 * <p>The specific document collection (Applicant or Respondent) is determined
 * by subclasses via {@link #getDocuments(FinremCaseData)} and
 * {@link #setDocuments(FinremCaseData, List)}.
 */
@Slf4j
public abstract class CUIDocumentUploadAboutToSubmitHandler extends FinremAboutToSubmitCallbackHandler {

    private final CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrchestrationService;
    private final CitizenDocumentsUploadedCorresponder citizenDocumentsUploadedCorresponder;
    protected final CitizenDocumentSorter citizenDocumentSorter;

    protected CUIDocumentUploadAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                    CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrchestrationService,
                                                    CitizenDocumentsUploadedCorresponder citizenDocumentsUploadedCorresponder,
                                                    CitizenDocumentSorter citizenDocumentSorter) {
        super(finremCaseDetailsMapper);
        this.correspondenceEventAuditOrchestrationService = correspondenceEventAuditOrchestrationService;
        this.citizenDocumentsUploadedCorresponder = citizenDocumentsUploadedCorresponder;
        this.citizenDocumentSorter = citizenDocumentSorter;
    }

    /**
     * Handles the ABOUT_TO_SUBMIT callback by merging and sorting document collections.
     * TODO: Update with new sorted temp list pattern notes
     * <p>Flow:
     * <ol>
     *     <li>Read existing documents from {@code dataBefore}</li>
     *     <li>Read newly uploaded documents from {@code currentCaseData}</li>
     *     <li>Merge both collections</li>
     *     <li>Sort by {@code generalDocumentUploadDateTime} (descending, nulls last)</li>
     *     <li>Set the merged list back onto {@code currentCaseData}</li>
     * </ol>
     *
     * @param callbackRequest     the callback request containing case data
     * @param userAuthorisation   authorisation token
     * @return response containing updated {@link FinremCaseData}
     */
    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(
        FinremCallbackRequest callbackRequest,
        String userAuthorisation
    ) {
        handleLog(callbackRequest);

        FinremCaseData currentCaseData = callbackRequest.getFinremCaseData();
        categoriseDocuments(currentCaseData);
        sortDocs(currentCaseData.getCitizenDocumentWrapper());
        createNotificationAuditRows(callbackRequest, userAuthorisation);

        return response(currentCaseData);
    }

    private void createNotificationAuditRows(FinremCallbackRequest callbackRequest,
                                             String userAuthorisation) {
        correspondenceEventAuditOrchestrationService.createPendingAudits(
            buildSendCorrespondenceEvent(callbackRequest, userAuthorisation),
            handledEventType()
        );
    }

    private SendCorrespondenceEvent buildSendCorrespondenceEvent(
        FinremCallbackRequest callbackRequest,
        String userAuthorisation) {
        return citizenDocumentsUploadedCorresponder.buildCorrespondenceEvent(
            callbackRequest.getCaseDetails(),
            userAuthorisation,
            notificationParty()
        );
    }

    /**
     * Determines whether this handler can process the callback.
     *
     * <p>Only handles:
     * <ul>
     *     <li>{@link CallbackType#ABOUT_TO_SUBMIT}</li>
     *     <li>{@link CaseType#CONTESTED}</li>
     *     <li>Specific event type defined by subclass</li>
     * </ul>
     *
     * @param callbackType type of callback
     * @param caseType     type of case
     * @param eventType    event being processed
     * @return true if this handler supports the given combination
     */
    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && handledEventType().equals(eventType);
    }

    /**
     * Defines the event type handled by the concrete implementation.
     *
     * @return event type (e.g. Applicant or Respondent upload)
     */
    protected abstract EventType handledEventType();

    /**
     * Retrieves the relevant document collection from case data.
     *
     * <p>Implemented by subclasses to return either:
     * <ul>
     *     <li>Applicant document collection</li>
     *     <li>Respondent document collection</li>
     * </ul>
     *
     * @param caseData case data
     * @return list of documents (may be null)
     */
    protected abstract List<CitizenDocumentCollectionItem> getDocuments(FinremCaseData caseData);

    /**
     * Sets the updated document collection onto case data.
     *
     * <p>Implemented by subclasses to update the correct field
     * (Applicant or Respondent collection).
     *
     * @param caseData  case data
     * @param documents merged and sorted documents
     */
    protected abstract void setDocuments(
        FinremCaseData caseData,
        List<CitizenDocumentCollectionItem> documents
    );

    protected abstract NotificationParty notificationParty();

    protected abstract void handleLog(FinremCallbackRequest callbackRequest);

    protected abstract void categoriseDocuments(FinremCaseData caseData);

    protected abstract void sortDocs(CitizenDocumentWrapper citizenDocumentWrapper);
}
