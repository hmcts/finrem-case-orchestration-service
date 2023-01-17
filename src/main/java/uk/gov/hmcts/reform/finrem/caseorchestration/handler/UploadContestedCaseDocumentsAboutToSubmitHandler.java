package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UploadedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.DocumentCollectionService;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
public class UploadContestedCaseDocumentsAboutToSubmitHandler extends FinremCallbackHandler {

    private final List<DocumentCollectionService> documentCollectionServices;
    private final UploadedDocumentService uploadedDocumentHelper;

    public UploadContestedCaseDocumentsAboutToSubmitHandler(FinremCaseDetailsMapper mapper,
                                                            List<DocumentCollectionService> documentCollectionServices,
                                                            UploadedDocumentService uploadedDocumentHelper) {
        super(mapper);
        this.documentCollectionServices = documentCollectionServices;
        this.uploadedDocumentHelper = uploadedDocumentHelper;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.UPLOAD_CASE_FILES.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();
        FinremCaseData caseDataBefore = callbackRequest.getCaseDetailsBefore().getData();
        List<UploadCaseDocumentCollection> screenCollections = caseData.getManageCaseDocumentCollection();

        documentCollectionServices.forEach(documentCollectionService ->
            documentCollectionService.addManagedDocumentToCollection(callbackRequest, screenCollections));

        screenCollections.sort(Comparator.comparing(
            UploadCaseDocumentCollection::getUploadCaseDocument, Comparator.comparing(
                UploadCaseDocument::getCaseDocumentUploadDateTime, Comparator.nullsLast(
                    Comparator.reverseOrder()))));

        uploadedDocumentHelper.addUploadDateToNewDocuments(caseData, caseDataBefore);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).build();
    }


}
