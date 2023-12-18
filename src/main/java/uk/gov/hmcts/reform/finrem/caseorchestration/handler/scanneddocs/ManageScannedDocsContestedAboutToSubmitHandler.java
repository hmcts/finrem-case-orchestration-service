package uk.gov.hmcts.reform.finrem.caseorchestration.handler.scanneddocs;

import com.google.common.io.Files;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.utils.StringUtils;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.DocumentHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.validation.ManageDocumentsHandlerValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ManageScannedDocsContestedAboutToSubmitHandler extends FinremCallbackHandler {

    private final List<DocumentHandler> documentHandlers;
    private final ManageDocumentsHandlerValidator manageDocumentsHandlerValidator;

    @Autowired
    public ManageScannedDocsContestedAboutToSubmitHandler(
        FinremCaseDetailsMapper finremCaseDetailsMapper, List<DocumentHandler> documentHandlers,
        ManageDocumentsHandlerValidator manageDocumentsHandlerValidator) {
        super(finremCaseDetailsMapper);
        this.documentHandlers = documentHandlers;
        this.manageDocumentsHandlerValidator = manageDocumentsHandlerValidator;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && (EventType.MANAGE_SCANNED_DOCS.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest, String userAuthorisation) {

        log.info("Received request to manage scanned documents for Case ID : {}", callbackRequest.getCaseDetails().getId());
        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();
        List<String> warnings = new ArrayList<>();

        List<UploadCaseDocumentCollection> manageScannedDocumentCollection = caseData.getManageScannedDocumentCollection();

        manageDocumentsHandlerValidator.validateSelectedIntervenerParties(caseData, manageScannedDocumentCollection, warnings);

        updateFileNames(manageScannedDocumentCollection);

        documentHandlers.forEach(documentCollectionService ->
            documentCollectionService.replaceManagedDocumentsInCollectionType(callbackRequest, manageScannedDocumentCollection));

        caseData.setEvidenceHandled(YesOrNo.YES);
        if (caseData.getEvidenceHandled() != null && caseData.getEvidenceHandled().isYes()) {
            Optional.ofNullable(caseData.getScannedDocuments()).ifPresent(List::clear);
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).warnings(warnings).build();

    }

    private void updateFileNames(List<UploadCaseDocumentCollection> manageScannedDocumentCollection) {
        manageScannedDocumentCollection.forEach(uploadCaseDocumentCollection -> {
            UploadCaseDocument uploadCaseDocument = uploadCaseDocumentCollection.getUploadCaseDocument();
            CaseDocument caseDocument = uploadCaseDocument.getCaseDocuments();
            uploadCaseDocument.setScannedFileName(caseDocument.getDocumentFilename());
            if (StringUtils.isEmpty(uploadCaseDocument.getFileName())) {
                caseDocument.setDocumentFilename(caseDocument.getDocumentFilename());
            } else if (StringUtils.isEmpty(Files.getFileExtension(uploadCaseDocument.getFileName()))) {
                caseDocument.setDocumentFilename(uploadCaseDocument.getFileName()
                    + Files.getFileExtension(caseDocument.getDocumentFilename()));
            } else {
                caseDocument.setDocumentFilename(uploadCaseDocument.getFileName());
            }
        });
    }

}
