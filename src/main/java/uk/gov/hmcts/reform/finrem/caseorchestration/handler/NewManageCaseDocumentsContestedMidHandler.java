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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageCaseDocumentsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.LegacyConfidentialDocumentsService;

import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managecasedocuments.ManageCaseDocumentsAction.ADD_NEW;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managecasedocuments.ManageCaseDocumentsAction.AMEND;

@Slf4j
@Service
public class NewManageCaseDocumentsContestedMidHandler extends FinremCallbackHandler {

    private final LegacyConfidentialDocumentsService legacyConfidentialDocumentsService;

    public NewManageCaseDocumentsContestedMidHandler(FinremCaseDetailsMapper mapper,
                                                     LegacyConfidentialDocumentsService legacyConfidentialDocumentsService) {
        super(mapper);
        this.legacyConfidentialDocumentsService = legacyConfidentialDocumentsService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.MID_EVENT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.NEW_MANAGE_CASE_DOCUMENTS.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.midEvent(callbackRequest));

        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();
        ManageCaseDocumentsWrapper wrapper = caseData.getManageCaseDocumentsWrapper();

        if (ADD_NEW.equals(wrapper.getManageCaseDocumentsActionSelection())) {
            // to save a click
            wrapper.setInputManageCaseDocumentCollection(
                getEmptyUploadCaseDocumentCollection()
            );
        } else if (AMEND.equals(wrapper.getManageCaseDocumentsActionSelection())) {
            wrapper.setInputManageCaseDocumentCollection(
                caseData.getUploadCaseDocumentWrapper().getAllManageableCollections()
            );
            migrateLegacyConfidentialCaseDocumentFormat(caseData);
            populateMissingConfidentialFlag(wrapper);
        }

        return response(caseData);
    }

    private List<UploadCaseDocumentCollection> getEmptyUploadCaseDocumentCollection() {
        return List.of(UploadCaseDocumentCollection.builder()
            .uploadCaseDocument(UploadCaseDocument.builder().build())
            .build());
    }

    // The following methods are migrated from ManageCaseDocumentsContestedAboutToStartHandler
    private void migrateLegacyConfidentialCaseDocumentFormat(FinremCaseData data) {
        if (data.getConfidentialDocumentsUploaded() != null) {
            data.getManageCaseDocumentsWrapper().getInputManageCaseDocumentCollection()
                .addAll(getConfidentialCaseDocumentCollectionFromLegacyConfidentialDocs(data));
        }
    }

    private void populateMissingConfidentialFlag(ManageCaseDocumentsWrapper wrapper ) {
        wrapper.getInputManageCaseDocumentCollection().stream()
            .filter(this::isConfidentialFlagMissing).forEach(documentCollection ->
                documentCollection.getUploadCaseDocument().setCaseDocumentConfidentiality(YesOrNo.NO));
    }

    private boolean isConfidentialFlagMissing(UploadCaseDocumentCollection documentCollection) {
        return documentCollection.getUploadCaseDocument() != null
            && documentCollection.getUploadCaseDocument().getCaseDocumentConfidentiality() == null;
    }

    private List<UploadCaseDocumentCollection> getConfidentialCaseDocumentCollectionFromLegacyConfidentialDocs(
        FinremCaseData caseData) {
        return legacyConfidentialDocumentsService.mapLegacyConfidentialDocumentToConfidentialDocumentCollection(
            caseData.getConfidentialDocumentsUploaded());
    }
}
