package uk.gov.hmcts.reform.finrem.caseorchestration.handler.scanneddocs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ManageScannedDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ManageScannedDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ScannedDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ManageScannedDocsContestedAboutToStartHandler extends FinremCallbackHandler {

    private static final String SCANNED_DOCUMENT_MISSING_FILE_MESSAGE =
        "A scanned document record exists that is missing a file. Please amend this in Attach scanned docs";

    @Autowired
    public ManageScannedDocsContestedAboutToStartHandler(
        FinremCaseDetailsMapper finremCaseDetailsMapper) {
        super(finremCaseDetailsMapper);
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && (EventType.MANAGE_SCANNED_DOCS.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest, String userAuthorisation) {

        log.info("Received request to manage scanned documents for Case ID : {}", callbackRequest.getCaseDetails().getId());
        FinremCaseData finremCaseData = callbackRequest.getCaseDetails().getData();
        finremCaseData.setManageScannedDocumentCollection(new ArrayList<>());

        List<ScannedDocumentCollection> scannedDocumentCollections =
            Optional.ofNullable(finremCaseData.getScannedDocuments()).orElse(new ArrayList<>());

        if (isScannedDocumentExistsWithoutFile(scannedDocumentCollections)) {
            return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
                .errors(List.of(SCANNED_DOCUMENT_MISSING_FILE_MESSAGE))
                .data(finremCaseData)
                .build();
        }

        scannedDocumentCollections.forEach(doc -> {

            ManageScannedDocumentCollection item = ManageScannedDocumentCollection.builder()
                .id(doc.getId())
                .manageScannedDocument(ManageScannedDocument.builder()
                    .selectForUpdate(YesOrNo.NO)
                    .uploadCaseDocument(UploadCaseDocument.from(doc))
                    .build())
                .build();

            finremCaseData.getManageScannedDocumentCollection().add(item);
        });

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(finremCaseData).build();
    }

    private boolean isScannedDocumentExistsWithoutFile(List<ScannedDocumentCollection> scannedDocumentCollections) {
        return scannedDocumentCollections.stream()
            .anyMatch(sdc -> sdc.getValue().getUrl() == null);
    }
}
