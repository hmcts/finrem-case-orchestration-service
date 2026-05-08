package uk.gov.hmcts.reform.finrem.caseorchestration.handler.citizendocumentupload;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;

import java.util.List;

/**
 * Handler for Applicant CUI document upload events.
 *
 * <p>This handler processes {@link EventType#CUI_APPLICANT_DOCUMENT_UPLOAD} events and is responsible for:
 * <ul>
 *     <li>Retrieving the applicant document collection</li>
 *     <li>Merging newly uploaded documents with existing ones</li>
 *     <li>Sorting documents by upload datetime (handled in the abstract class)</li>
 *     <li>Updating the applicant document collection on the case data</li>
 * </ul>
 *
 * <p>All core merge and sort logic is implemented in
 * {@link CuiDocumentUploadAboutToSubmitHandler}.
 */
@Slf4j
@Service
public class CuiApplicantDocumentUploadAboutToSubmitHandler extends CuiDocumentUploadAboutToSubmitHandler {

    public CuiApplicantDocumentUploadAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper) {
        super(finremCaseDetailsMapper);
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

    /**
     * Retrieves the applicant document collection from case data.
     *
     * @param caseData case data
     * @return list of applicant documents (may be null)
     */
    @Override
    protected List<CitizenDocumentCollection> getDocuments(FinremCaseData caseData) {
        return caseData.getCitizenDocumentWrapper().getCitizenApplicantDocument();
    }

    /**
     * Sets the updated applicant document collection onto case data.
     *
     * @param caseData  case data
     * @param documents merged and sorted applicant documents
     */
    @Override
    protected void setDocuments(FinremCaseData caseData, List<CitizenDocumentCollection> documents) {
        caseData.getCitizenDocumentWrapper().setCitizenApplicantDocument(documents);
    }

    @Override
    protected void handleLog(FinremCallbackRequest callbackRequest) {
        log.info(CallbackHandlerLogger.aboutToSubmit(callbackRequest));
    }
}
