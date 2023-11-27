package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.validation.ManageDocumentsHandlerValidator;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UploadedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.DocumentHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ManageCaseDocumentsContestedAboutToSubmitHandler extends FinremCallbackHandler {
    private final List<DocumentHandler> documentHandlers;
    private final UploadedDocumentService uploadedDocumentHelper;

    private final EvidenceManagementDeleteService evidenceManagementDeleteService;

    private final ManageDocumentsHandlerValidator manageDocumentsHandlerValidator;
    private final FeatureToggleService featureToggleService;


    @Autowired
    public ManageCaseDocumentsContestedAboutToSubmitHandler(FinremCaseDetailsMapper mapper,
                                                            List<DocumentHandler> documentHandlers,
                                                            UploadedDocumentService uploadedDocumentHelper,
                                                            EvidenceManagementDeleteService evidenceManagementDeleteService,
                                                            ManageDocumentsHandlerValidator manageDocumentsHandlerValidator,
                                                            FeatureToggleService featureToggleService) {
        super(mapper);
        this.documentHandlers = documentHandlers;
        this.uploadedDocumentHelper = uploadedDocumentHelper;
        this.evidenceManagementDeleteService = evidenceManagementDeleteService;
        this.manageDocumentsHandlerValidator = manageDocumentsHandlerValidator;
        this.featureToggleService = featureToggleService;
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
        List<String> warnings = new ArrayList<>();

        List<UploadCaseDocumentCollection> managedCollections = caseData.getManageCaseDocumentCollection();
        manageDocumentsHandlerValidator.validateSelectedIntervenerParties(caseData, managedCollections, warnings);

        FinremCaseData caseDataBefore = callbackRequest.getCaseDetailsBefore().getData();

        documentHandlers.forEach(documentCollectionService ->
            documentCollectionService.replaceManagedDocumentsInCollectionType(callbackRequest, managedCollections));
        uploadedDocumentHelper.addUploadDateToNewDocuments(caseData, caseDataBefore);

        Optional.ofNullable(caseData.getConfidentialDocumentsUploaded()).ifPresent(List::clear);

        if (featureToggleService.isSecureDocEnabled()) {
            deleteRemovedDocuments(caseData, caseDataBefore, userAuthorisation);
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).warnings(warnings).build();
    }


    private void deleteRemovedDocuments(FinremCaseData caseData,
                                        FinremCaseData caseDataBefore,
                                        String userAuthorisation) {
        List<UploadCaseDocumentCollection> allCollectionsBefore =
            caseDataBefore.getUploadCaseDocumentWrapper().getAllManageableCollections();
        allCollectionsBefore.removeAll(caseData.getUploadCaseDocumentWrapper().getAllManageableCollections());

        allCollectionsBefore.stream().map(this::getDocumentUrl)
            .forEach(docUrl -> evidenceManagementDeleteService.delete(docUrl, userAuthorisation));
    }

    private String getDocumentUrl(UploadCaseDocumentCollection documentCollection) {
        return documentCollection.getUploadCaseDocument().getCaseDocuments().getDocumentUrl();
    }
}
