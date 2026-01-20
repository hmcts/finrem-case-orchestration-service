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

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managecasedocuments.ManageCaseDocumentsAction.ADD_NEW;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managecasedocuments.ManageCaseDocumentsAction.AMEND;

@Slf4j
@Service
public class NewManageCaseDocumentsContestedMidHandler extends FinremCallbackHandler {

    public NewManageCaseDocumentsContestedMidHandler(FinremCaseDetailsMapper mapper) {
        super(mapper);
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
            // Get all manageable documents and create clean copies with only CCD-allowed fields
            List<UploadCaseDocumentCollection> amendableDocuments = caseData.getUploadCaseDocumentWrapper()
                .getAllManageableCollections()
                .stream()
                .map(doc -> UploadCaseDocumentCollection.builder()
                    .id(UUID.randomUUID().toString())
                    .uploadCaseDocument(UploadCaseDocument.builder()
                        .caseDocuments(doc.getUploadCaseDocument().getCaseDocuments())
                        .caseDocumentType(doc.getUploadCaseDocument().getCaseDocumentType())
                        .caseDocumentParty(doc.getUploadCaseDocument().getCaseDocumentParty())
                        .caseDocumentOther(doc.getUploadCaseDocument().getCaseDocumentOther())
                        .caseDocumentConfidentiality(doc.getUploadCaseDocument().getCaseDocumentConfidentiality())
                        .hearingDetails(doc.getUploadCaseDocument().getHearingDetails())
                        .caseDocumentFdr(doc.getUploadCaseDocument().getCaseDocumentFdr())
                        .build())
                    .build())
                .collect(Collectors.toList());

            caseData.getManageCaseDocumentsWrapper().setInputManageCaseDocumentCollection(amendableDocuments);
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).build();
    }
}