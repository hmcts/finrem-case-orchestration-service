package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.LegacyConfidentialDocumentsService;

import java.util.List;

/**
 * Event "FR_manageCaseDocuments" will be replaced by "FR_newManageCaseDocuments soon.
 * Similar logics have been moved to ManageCaseDocumentsContestedMidHandler
 *
 * @deprecated Event "FR_manageCaseDocuments" will be replaced by "FR_newManageCaseDocuments soon.
 */
@Deprecated
@Slf4j
@Service
public class ManageCaseDocumentsContestedAboutToStartHandler extends FinremCallbackHandler {

    private final LegacyConfidentialDocumentsService legacyConfidentialDocumentsService;

    public ManageCaseDocumentsContestedAboutToStartHandler(FinremCaseDetailsMapper mapper,
                                                           LegacyConfidentialDocumentsService legacyConfidentialDocumentsService) {
        super(mapper);
        this.legacyConfidentialDocumentsService = legacyConfidentialDocumentsService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.MANAGE_CASE_DOCUMENTS.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToStart(callbackRequest));
        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();

        caseData.getManageCaseDocumentsWrapper()
            .setManageCaseDocumentCollection(caseData.getUploadCaseDocumentWrapper().getAllManageableCollections());

        migrateLegacyConfidentialCaseDocumentFormat(caseData);
        populateMissingConfidentialFlag(caseData);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).build();
    }

    private void migrateLegacyConfidentialCaseDocumentFormat(FinremCaseData data) {
        if (data.getConfidentialDocumentsUploaded() != null) {
            data.getManageCaseDocumentsWrapper().getManageCaseDocumentCollection()
                .addAll(getConfidentialCaseDocumentCollectionFromLegacyConfidentialDocs(data));
            data.getConfidentialDocumentsUploaded().clear();
        }
    }

    private void populateMissingConfidentialFlag(FinremCaseData caseData) {
        caseData.getManageCaseDocumentsWrapper().getManageCaseDocumentCollection().stream()
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
