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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ScannedDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ManageScannedDocsContestedAboutToStartHandler extends FinremCallbackHandler {


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
        scannedDocumentCollections.forEach(doc -> {
            UploadCaseDocumentCollection scannedCaseDocumentCollectionItem = UploadCaseDocumentCollection.builder()
                .uploadCaseDocument(UploadCaseDocument.builder()
                    .fileName(doc.getValue().getFileName())
                    .scannedDate(doc.getValue().getScannedDate())
                    .caseDocuments(CaseDocument.builder()
                        .documentUrl(doc.getValue().getUrl().getDocumentUrl())
                        .documentBinaryUrl(doc.getValue().getUrl().getDocumentBinaryUrl())
                        .documentFilename(doc.getValue().getUrl().getDocumentFilename())
                        .build())
                    .exceptionRecordReference(doc.getValue().getExceptionRecordReference())
                    .build())
                .build();
            finremCaseData.getManageScannedDocumentCollection().add(scannedCaseDocumentCollectionItem);
        });


        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(finremCaseData).build();
    }
}
