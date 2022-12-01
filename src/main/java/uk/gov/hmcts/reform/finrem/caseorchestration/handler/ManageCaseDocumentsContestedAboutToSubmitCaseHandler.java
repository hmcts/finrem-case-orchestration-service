package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.UploadedDocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ManageCaseDocumentsService;

import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_MANAGE_CASE_DOCUMENT_COLLECTION;

@Slf4j
@Service
public class ManageCaseDocumentsContestedAboutToSubmitCaseHandler extends FinremCallbackHandler {

    private final ManageCaseDocumentsService manageCaseDocumentsService;
    private final UploadedDocumentHelper uploadedDocumentHelper;

    @Autowired
    public ManageCaseDocumentsContestedAboutToSubmitCaseHandler(FinremCaseDetailsMapper mapper,
                                                               ManageCaseDocumentsService manageCaseDocumentsService,
                                                                UploadedDocumentHelper uploadedDocumentHelper) {
        super(mapper);
        this.manageCaseDocumentsService = manageCaseDocumentsService;
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

        uploadedDocumentHelper.addUploadDateToNewDocuments(callbackRequest.getCaseDetails().getData(),
            callbackRequest.getCaseDetailsBefore().getData());

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data().build();
    }

//    @Override
//    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(CallbackRequest callbackRequest, String userAuthorisation) {
//        Map<String, Object> caseDataBefore = manageCaseDocumentsService.setCaseDataBeforeManageCaseDocumentCollection(
//            callbackRequest.getCaseDetails().getData(), callbackRequest.getCaseDetailsBefore().getData());
//
//        Map<String, Object> caseData = uploadedDocumentHelper.addUploadDateToNewDocuments(
//            callbackRequest.getCaseDetails().getData(),
//            caseDataBefore, CONTESTED_MANAGE_CASE_DOCUMENT_COLLECTION);
//
//        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(
//            manageCaseDocumentsService.manageCaseDocuments(
//                caseData)).build();
//    }
}