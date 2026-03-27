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
        if (ADD_NEW.equals(caseData.getManageCaseDocumentsWrapper().getManageCaseDocumentsActionSelection())) {
            caseData.getManageCaseDocumentsWrapper().setInputManageCaseDocumentCollection(
                // save a click
                List.of(UploadCaseDocumentCollection.builder()
                    .uploadCaseDocument(UploadCaseDocument.builder().build())
                    .build())
            );
        } else if (AMEND.equals(caseData.getManageCaseDocumentsWrapper().getManageCaseDocumentsActionSelection())) {
            caseData.getManageCaseDocumentsWrapper()
                .setInputManageCaseDocumentCollection(
                    caseData.getUploadCaseDocumentWrapper().getAllManageableCollections()
                );

            migrateLegacyConfidentialCaseDocumentFormat(caseData);
            populateMissingConfidentialFlag(caseData);
        }

        return response(caseData);
    }

    // The following methods are migrated from ManageCaseDocumentsContestedAboutToStartHandler
    private void migrateLegacyConfidentialCaseDocumentFormat(FinremCaseData data) {
        if (data.getConfidentialDocumentsUploaded() != null) {
            // Looks like uploadCaseDocumentWrapper.confidentialDocumentCollection is the new one
            data.getManageCaseDocumentsWrapper().getInputManageCaseDocumentCollection()
                .addAll(getConfidentialCaseDocumentCollectionFromLegacyConfidentialDocs(data));
        }
    }

    private void populateMissingConfidentialFlag(FinremCaseData caseData) {
        caseData.getManageCaseDocumentsWrapper().getInputManageCaseDocumentCollection().stream()
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
