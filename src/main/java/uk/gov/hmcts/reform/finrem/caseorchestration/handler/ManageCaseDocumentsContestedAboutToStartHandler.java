package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.LegacyConfidentialDocumentsService;

import java.util.ArrayList;

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
        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();

        caseData.setManageCaseDocumentCollection(caseData.getUploadCaseDocumentWrapper().getAllCollections());

        migrateLegacyConfidentialCaseDocumentFormat(caseData);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).build();
    }

    private void migrateLegacyConfidentialCaseDocumentFormat(FinremCaseData caseData) {
        caseData.getManageCaseDocumentCollection().addAll(
            legacyConfidentialDocumentsService.getConfidentialCaseDocumentCollection(
                caseData.getConfidentialDocumentsUploaded()));
        caseData.setConfidentialDocumentsUploaded(new ArrayList<>());
    }
}