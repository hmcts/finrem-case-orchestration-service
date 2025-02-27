package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDownloadService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class GeneralEmailMidHandler extends FinremCallbackHandler {

    private static final String UPLOADED_FILE_EXCEEDS_2MB_MESSAGE = "You attached a document which exceeds the size limit: 2MB";

    private final EvidenceManagementDownloadService evidenceManagementDownloadService;

    private final BulkPrintDocumentService bulkPrintDocumentService;

    @Autowired
    public GeneralEmailMidHandler(FinremCaseDetailsMapper mapper, EvidenceManagementDownloadService evidenceManagementDownloadService,
                                  BulkPrintDocumentService bulkPrintDocumentService) {
        super(mapper);
        this.evidenceManagementDownloadService = evidenceManagementDownloadService;
        this.bulkPrintDocumentService = bulkPrintDocumentService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.MID_EVENT.equals(callbackType)
            && (CaseType.CONSENTED.equals(caseType) || CaseType.CONTESTED.equals(caseType))
            && EventType.CREATE_GENERAL_EMAIL.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        String caseId = String.valueOf(caseDetails.getId());
        log.info("Invoking {} event {} mid callback for Case ID: {}", caseDetails.getCaseType(), callbackRequest.getEventType(), caseId);

        FinremCaseData caseData = caseDetails.getData();
        CaseDocument uploadedDocument = caseData.getGeneralEmailWrapper().getGeneralEmailUploadedDocument();

        List<String> errors = new ArrayList<>();
        bulkPrintDocumentService.validateEncryptionOnUploadedDocument(uploadedDocument, caseId, errors, userAuthorisation);
        if (doesUploadedFileExceeds2MB(caseData, userAuthorisation)) {
            errors.add(UPLOADED_FILE_EXCEEDS_2MB_MESSAGE);
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().errors(errors).data(caseData).build();
    }

    private boolean doesUploadedFileExceeds2MB(FinremCaseData data, String userAuthorisation) {
        CaseDocument uploadedDocument = data.getGeneralEmailWrapper().getGeneralEmailUploadedDocument();
        return evidenceManagementDownloadService.getByteArray(uploadedDocument, userAuthorisation).length > 2 * 1024 * 1024;
    }
}
