package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NewUploadedDocumentsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentchecker.DocumentCheckerService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.lang.String.format;

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
        final Long caseId = finremCaseDetails.getId();

        List<UploadDocumentCollection> newDocuments = newUploadedDocumentsService.getNewUploadDocuments(caseData, caseDataBefore,
            FinremCaseData::getUploadDocuments);

        Set<String> allWarnings = new HashSet<>();
        for (UploadDocumentCollection doc : newDocuments) {
            if (doc.getValue() != null && doc.getValue().getDocumentLink() != null) {
                try {
                    List<String> warnings = documentCheckerService.getWarnings(doc.getValue().getDocumentLink(), beforeFinremCaseDetails,
                        finremCaseDetails, userAuthorisation);
                    if (!ObjectUtils.isEmpty(warnings)) {
                        allWarnings.addAll(warnings);
                    }
                } catch (Exception t) {
                    log.error(format("%s - Exception was caught when checking the warnings", caseId), t);
                }
            }
        }

        if (!allWarnings.isEmpty()) {
            log.info(format("%s - Number of warnings encountered when uploading document: %s",
                caseId, allWarnings.size()));
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .warnings((allWarnings.stream().sorted().toList()))
            .data(caseData).build();
    }

}
