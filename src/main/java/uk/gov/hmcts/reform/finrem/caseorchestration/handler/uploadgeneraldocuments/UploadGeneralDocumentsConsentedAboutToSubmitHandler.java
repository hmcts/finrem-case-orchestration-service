package uk.gov.hmcts.reform.finrem.caseorchestration.handler.uploadgeneraldocuments;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentchecker.DocumentCheckerService;

@Slf4j
@Service
public class UploadGeneralDocumentsConsentedAboutToSubmitHandler extends FinremCallbackHandler {

    private final DocumentCheckerService documentCheckerService;
//    private final UploadGeneralDocumentService uploadGeneralDocumentService;

    public UploadGeneralDocumentsConsentedAboutToSubmitHandler(FinremCaseDetailsMapper mapper,
                                                               DocumentCheckerService documentCheckerService
//        ,
//                                                               UploadGeneralDocumentService uploadGeneralDocumentService
    ) {
        super(mapper);
        this.documentCheckerService = documentCheckerService;
//        this.uploadGeneralDocumentService = uploadGeneralDocumentService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && EventType.UPLOAD_GENERAL_DOCUMENT_CONSENTED.equals(eventType);
    }

    @Override
    @SuppressWarnings("squid:CallToDeprecatedMethod")
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = finremCaseDetails.getData();
        FinremCaseData caseDataBefore = callbackRequest.getCaseDetailsBefore().getData();

//        final List<String> warnings = uploadGeneralDocumentService.getNewlyUploadedDocuments(caseData, caseDataBefore).stream()
//                .map(d -> documentCheckerService.getWarnings(d.getValue().getDocumentLink(), finremCaseDetails, userAuthorisation))
//                .flatMap(List::stream)
//                .filter(ObjectUtils::isNotEmpty)
//                .toList();
//        if (!warnings.isEmpty()) {
//            log.info("Number of warnings encountered when uploading general document for a case {}: {}",
//                    finremCaseDetails.getId(), warnings.size());
//        }
//
//        List<UploadGeneralDocumentCollection> uploadedDocuments = caseData.getUploadGeneralDocuments();

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
//                .warnings(warnings)
                .data(caseData).build();
    }

}
