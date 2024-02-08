package uk.gov.hmcts.reform.finrem.caseorchestration.handler.scanneddocs;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ScannedDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;

import java.util.List;

@Service
@Slf4j
public class ManagedScannedDocsContestedMidEventHandler extends FinremCallbackHandler {

    public ManagedScannedDocsContestedMidEventHandler(FinremCaseDetailsMapper finremCaseDetailsMapper) {
        super(finremCaseDetailsMapper);
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.MID_EVENT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && (EventType.MANAGE_SCANNED_DOCS.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(
        FinremCallbackRequest callbackRequestWithFinremCaseDetails, String userAuthorisation) {
        log.info("Received mid-event request to manage scanned documents for Case ID : {}",
            callbackRequestWithFinremCaseDetails.getCaseDetails().getId());
        FinremCaseData caseData = callbackRequestWithFinremCaseDetails.getCaseDetails().getData();

        if (ObjectUtils.isEmpty(caseData.getScannedDocsToUpdate().getValue())) {
            return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
                .data(caseData)
                .errors(List.of("No scanned document has been selected"))
                .build();
        }

        List<ScannedDocumentCollection> selectedScannedDocuments = getSelectedScannedDocuments(caseData);
        List<UploadCaseDocumentCollection> manageScannedDocuments = convert(selectedScannedDocuments);
        caseData.setManageScannedDocumentCollection(manageScannedDocuments);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData)
            .build();
    }

    private List<ScannedDocumentCollection> getSelectedScannedDocuments(FinremCaseData caseData) {
        List<String> selectedScannedDocuments = caseData.getScannedDocsToUpdate().getValue().stream()
            .map(DynamicMultiSelectListElement::getCode)
            .toList();

        return caseData.getScannedDocuments().stream()
            .filter(sdc -> selectedScannedDocuments.contains(sdc.getId()))
            .toList();
    }

    private List<UploadCaseDocumentCollection> convert(List<ScannedDocumentCollection> scannedDocumentCollections) {
        return scannedDocumentCollections.stream()
            .map(this::map)
            .toList();
    }

    private UploadCaseDocumentCollection map(ScannedDocumentCollection scannedDocument) {
        return UploadCaseDocumentCollection.builder()
            .uploadCaseDocument(UploadCaseDocument.from(scannedDocument))
            .build();
    }
}
