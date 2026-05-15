package uk.gov.hmcts.reform.finrem.caseorchestration.handler.creategeneralemail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDownloadService;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;

@Slf4j
@Service
public class GeneralEmailMidHandler extends FinremCallbackHandler {

    private static final int MAX_FILE_SIZE = 2 * 1024 * 1024;

    private static final String UPLOADED_FILE_EXCEEDS_MAX_THRESHOLD_MESSAGE = "You attached a document which exceeds the size limit: 2MB";

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
        return CallbackType.MID_EVENT.equals(callbackType) && EventType.CREATE_GENERAL_EMAIL.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.midEvent(callbackRequest));

        FinremCaseData finremCaseData = callbackRequest.getFinremCaseData();

        List<String> errors = new ArrayList<>();
        CaseDocument uploadedDocument = finremCaseData.getGeneralEmailWrapper().getGeneralEmailUploadedDocument();
        if (nonNull(uploadedDocument)) {
            bulkPrintDocumentService.validateEncryptionOnUploadedDocument(uploadedDocument, finremCaseData.getCcdCaseId(),
                errors, userAuthorisation);
            if (isUploadedFileOverSizeLimit(finremCaseData, userAuthorisation)) {
                errors.add(UPLOADED_FILE_EXCEEDS_MAX_THRESHOLD_MESSAGE);
            }
        }

        return response(finremCaseData, null, errors);
    }

    private boolean isUploadedFileOverSizeLimit(FinremCaseData data, String userAuthorisation) {
        CaseDocument uploadedDocument = data.getGeneralEmailWrapper().getGeneralEmailUploadedDocument();
        return evidenceManagementDownloadService.getByteArray(uploadedDocument, userAuthorisation).length > MAX_FILE_SIZE;
    }
}
