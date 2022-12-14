package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.UploadedDocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.DocumentCollectionService;

import java.util.List;

@Slf4j
@Service
public class  ManageCaseDocumentsContestedAboutToSubmitCaseHandler extends FinremCallbackHandler {

    private final List<DocumentCollectionService> documentCollectionServices;
    private final UploadedDocumentHelper uploadedDocumentHelper;

    @Autowired
    public ManageCaseDocumentsContestedAboutToSubmitCaseHandler(FinremCaseDetailsMapper mapper,
                                                                List<DocumentCollectionService> documentCollectionServices,
                                                                UploadedDocumentHelper uploadedDocumentHelper) {
        super(mapper);
        this.documentCollectionServices = documentCollectionServices;
        this.uploadedDocumentHelper = uploadedDocumentHelper;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.MANAGE_CASE_DOCUMENTS.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {

        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();
        FinremCaseData caseDataBefore = callbackRequest.getCaseDetailsBefore().getData();
        uploadedDocumentHelper.addUploadDateToNewDocuments(caseData, caseDataBefore);
        List<UploadCaseDocumentCollection> screenCollections = caseData.getManageCaseDocumentCollection();
        documentCollectionServices.forEach(documentCollectionService -> {
            documentCollectionService.deleteRemovedDocumentFromAllPlaces(callbackRequest, userAuthorisation);
            documentCollectionService.addManagedDocumentToCollection(callbackRequest, screenCollections);
        });

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).build();
    }
}