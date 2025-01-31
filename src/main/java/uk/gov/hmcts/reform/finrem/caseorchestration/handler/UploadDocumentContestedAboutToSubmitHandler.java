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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadGeneralDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadGeneralDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.UploadGeneralDocumentsCategoriser;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsLast;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.UPLOAD_DOCUMENT_CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@Slf4j
@Service
public class UploadDocumentContestedAboutToSubmitHandler extends FinremCallbackHandler {

    private final DocumentWarningsHelper documentWarningsHelper;
    private final UploadGeneralDocumentsCategoriser uploadGeneralDocumentsCategoriser;

    public UploadDocumentContestedAboutToSubmitHandler(FinremCaseDetailsMapper mapper, DocumentWarningsHelper documentWarningsHelper,
                                                       UploadGeneralDocumentsCategoriser uploadGeneralDocumentsCategoriser) {
        super(mapper);
        this.documentWarningsHelper = documentWarningsHelper;
        this.uploadGeneralDocumentsCategoriser = uploadGeneralDocumentsCategoriser;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return ABOUT_TO_SUBMIT.equals(callbackType) && CONTESTED.equals(caseType) && UPLOAD_DOCUMENT_CONTESTED.equals(eventType);
    }

    @Override
    @SuppressWarnings("squid:CallToDeprecatedMethod")
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = finremCaseDetails.getData();

        // Do sorting
        List<UploadGeneralDocumentCollection> uploadedDocuments = new ArrayList<>(ofNullable(caseData.getUploadGeneralDocuments())
            .orElse(List.of()));
        uploadedDocuments.sort(comparing(UploadGeneralDocumentCollection::getValue,
            comparing(UploadGeneralDocument::getGeneralDocumentUploadDateTime, nullsLast(Comparator.reverseOrder()))));
        caseData.setUploadGeneralDocuments(uploadedDocuments);

        // Execute the categoriser for CFV
        uploadGeneralDocumentsCategoriser.categorise(caseData);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .warnings(documentWarningsHelper.getDocumentWarnings(callbackRequest, FinremCaseData::getUploadGeneralDocuments, userAuthorisation))
            .data(caseData).build();
    }

}
