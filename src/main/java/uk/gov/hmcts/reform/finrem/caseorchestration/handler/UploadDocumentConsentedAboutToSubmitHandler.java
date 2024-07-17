package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NewUploadedDocumentsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentchecker.DocumentCheckerService;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UploadDocumentConsentedAboutToSubmitHandler extends FinremCallbackHandler {

    private final DocumentCheckerService documentCheckerService;
    private final NewUploadedDocumentsService newUploadedDocumentsService;

    public UploadDocumentConsentedAboutToSubmitHandler(FinremCaseDetailsMapper mapper,
                                                       DocumentCheckerService documentCheckerService,
                                                       NewUploadedDocumentsService newUploadedDocumentsService) {
        super(mapper);
        this.documentCheckerService = documentCheckerService;
        this.newUploadedDocumentsService = newUploadedDocumentsService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && EventType.UPLOAD_DOCUMENT_CONSENTED.equals(eventType);
    }

    @Override
    @SuppressWarnings("squid:CallToDeprecatedMethod")
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails beforeFinremCaseDetails = callbackRequest.getCaseDetailsBefore();
        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = finremCaseDetails.getData();
        FinremCaseData caseDataBefore = callbackRequest.getCaseDetailsBefore().getData();

        final Set<String> warnings = newUploadedDocumentsService.getNewUploadDocuments(caseData, caseDataBefore, FinremCaseData::getUploadDocuments)
            .stream()
                .map(d -> documentCheckerService.getWarnings(d.getValue().getDocumentLink(), beforeFinremCaseDetails, finremCaseDetails,
                    userAuthorisation))
                .flatMap(List::stream)
                .filter(ObjectUtils::isNotEmpty)
                .collect(Collectors.toSet());
        if (!warnings.isEmpty()) {
            log.info("Number of warnings encountered when uploading document for a case {}: {}",
                    finremCaseDetails.getId(), warnings.size());
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .warnings((warnings.stream().sorted().toList()))
            .data(caseData).build();
    }

}
