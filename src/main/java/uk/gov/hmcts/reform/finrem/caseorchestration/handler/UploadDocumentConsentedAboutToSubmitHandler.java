package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.helper.DocumentWarningsHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadDocumentCollection;

import java.util.List;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.UPLOAD_DOCUMENT_CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;

@Slf4j
@Service
public class UploadDocumentConsentedAboutToSubmitHandler extends FinremCallbackHandler {

    private final DocumentWarningsHelper documentWarningsHelper;

    public UploadDocumentConsentedAboutToSubmitHandler(FinremCaseDetailsMapper mapper, DocumentWarningsHelper documentWarningsHelper) {
        super(mapper);
        this.documentWarningsHelper = documentWarningsHelper;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return ABOUT_TO_SUBMIT.equals(callbackType) && CONSENTED.equals(caseType) && UPLOAD_DOCUMENT_CONSENTED.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToSubmit(callbackRequest));

        FinremCaseData caseData = callbackRequest.getFinremCaseData();

        List<String> warnings = documentWarningsHelper.getDocumentWarnings(callbackRequest,
            data -> emptyIfNull(data.getUploadDocuments()).stream()
                .map(UploadDocumentCollection::getValue).toList(), userAuthorisation);

        return response(caseData, warnings, null);
    }
}
