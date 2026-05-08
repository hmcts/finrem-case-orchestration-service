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
 * Handler for Respondent CUI document upload events.
 *
 * <p>This handler processes {@link EventType#CUI_RESPONDENT_DOCUMENT_UPLOAD} events and is responsible for:
 * <ul>
 *     <li>Retrieving the respondent document collection</li>
 *     <li>Merging newly uploaded documents with existing ones</li>
 *     <li>Sorting documents by upload datetime (delegated to the abstract class)</li>
 *     <li>Updating the respondent document collection on the case data</li>
 * </ul>
 *
 * <p>All core merge and sort logic is implemented in
 * {@link CuiDocumentUploadAboutToSubmitHandler}.
 */
@Slf4j
@Service
public class CuiRespondentDocumentUploadAboutToSubmitHandler extends CuiDocumentUploadAboutToSubmitHandler {

    public CuiRespondentDocumentUploadAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper) {
        super(finremCaseDetailsMapper);
    }

    /**
     * Defines the event type handled by this class.
     *
     * @return {@link EventType#CUI_RESPONDENT_DOCUMENT_UPLOAD}
     */
    @Override
    protected EventType handledEventType() {
        return EventType.CUI_RESPONDENT_DOCUMENT_UPLOAD;
    }

    /**
     * Retrieves the respondent document collection from case data.
     *
     * @param caseData case data
     * @return list of respondent documents (may be null)
     */
    @Override
    protected List<CitizenDocumentCollection> getDocuments(FinremCaseData caseData) {
        return caseData.getCitizenDocumentWrapper().getCitizenRespondentDocument();
    }

    /**
     * Sets the updated respondent document collection onto case data.
     *
     * @param caseData  case data
     * @param documents merged and sorted respondent documents
     */
    @Override
    protected void setDocuments(FinremCaseData caseData, List<CitizenDocumentCollection> documents) {
        caseData.getCitizenDocumentWrapper().setCitizenRespondentDocument(documents);
    }

    @Override
    protected void handleLog(FinremCallbackRequest callbackRequest) {
        log.info(CallbackHandlerLogger.aboutToSubmit(callbackRequest));
    }
}
