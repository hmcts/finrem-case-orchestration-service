package uk.gov.hmcts.reform.finrem.caseorchestration.handler.citizenui.documentupload;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.CitizenDocumentWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CorrespondenceEventAuditOrchestrationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.citizen.CitizenDocumentsUploadedCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.CUIDocumentsCategoriser;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.CitizenPartyType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentsorter.CitizenDocumentSorter;

/**
 * Handler for Applicant CUI document upload events.
 *
 * <p>This handler processes {@link EventType#CUI_APPLICANT_DOCUMENT_UPLOAD} events and is responsible for:
 * <ul>
 *     <li>Retrieving the applicant document collection</li>
 *     <li>Assigns each document it's CFV category</li>
 *     <li>Sorts the documents into their tab-specific collections</li>
 * </ul>
 *
 * <p>All core merge and sort logic is implemented in
 * {@link CUIDocumentUploadAboutToSubmitHandler}.
 */
@Slf4j
@Service
public class CUIApplicantDocumentUploadAboutToSubmitHandler extends CUIDocumentUploadAboutToSubmitHandler {

    public CUIApplicantDocumentUploadAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                          CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrchestrationService,
                                                          CitizenDocumentsUploadedCorresponder citizenDocumentsUploadedCorresponder,
                                                          CUIDocumentsCategoriser documentsCategoriser,
                                                          CitizenDocumentSorter citizenDocumentSorter) {
        super(finremCaseDetailsMapper,
            correspondenceEventAuditOrchestrationService,
            documentsCategoriser,
            citizenDocumentsUploadedCorresponder,
            citizenDocumentSorter);
    }

    /**
     * Defines the event type handled by this class.
     *
     * @return {@link EventType#CUI_APPLICANT_DOCUMENT_UPLOAD}
     */
    @Override
    protected EventType handledEventType() {
        return EventType.CUI_APPLICANT_DOCUMENT_UPLOAD;
    }

    @Override
    protected void handleLog(FinremCallbackRequest callbackRequest) {
        log.info(CallbackHandlerLogger.aboutToSubmit(callbackRequest));
    }

    @Override
    protected NotificationParty notificationParty() {
        return NotificationParty.CITIZEN_APPLICANT;
    }

    @Override
    protected void sortDocs(CitizenDocumentWrapper citizenDocumentWrapper) {
        citizenDocumentSorter.sortDocuments(citizenDocumentWrapper, CitizenPartyType.APPLICANT);
    }
}
