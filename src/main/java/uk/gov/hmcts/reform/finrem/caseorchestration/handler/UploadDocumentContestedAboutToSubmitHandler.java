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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadGeneralDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadGeneralDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NewUploadedDocumentsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.UploadGeneralDocumentsCategoriser;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentchecker.DocumentCheckerService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsLast;
import static java.util.Optional.ofNullable;

@Slf4j
@Service
public class UploadDocumentContestedAboutToSubmitHandler extends FinremCallbackHandler {

    private final DocumentCheckerService documentCheckerService;
    private final NewUploadedDocumentsService newUploadedDocumentsService;
    private final UploadGeneralDocumentsCategoriser uploadGeneralDocumentsCategoriser;

    public UploadDocumentContestedAboutToSubmitHandler(FinremCaseDetailsMapper mapper,
                                                       DocumentCheckerService documentCheckerService,
                                                       NewUploadedDocumentsService newUploadedDocumentsService,
                                                       UploadGeneralDocumentsCategoriser uploadGeneralDocumentsCategoriser) {
        super(mapper);
        this.documentCheckerService = documentCheckerService;
        this.newUploadedDocumentsService = newUploadedDocumentsService;
        this.uploadGeneralDocumentsCategoriser = uploadGeneralDocumentsCategoriser;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.UPLOAD_DOCUMENT_CONTESTED.equals(eventType);
    }

    @Override
    @SuppressWarnings("squid:CallToDeprecatedMethod")
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails beforeFinremCaseDetails = callbackRequest.getCaseDetailsBefore();
        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = finremCaseDetails.getData();
        FinremCaseData caseDataBefore = callbackRequest.getCaseDetailsBefore().getData();

        final Set<String> warnings = newUploadedDocumentsService.getNewUploadDocuments(caseData, caseDataBefore,
            FinremCaseData::getUploadGeneralDocuments).stream()
                .map(d -> documentCheckerService.getWarnings(d.getValue().getDocumentLink(), beforeFinremCaseDetails, finremCaseDetails,
                    userAuthorisation))
                .flatMap(List::stream)
                .filter(ObjectUtils::isNotEmpty)
                .collect(Collectors.toSet());
        if (!warnings.isEmpty()) {
            log.info("Number of warnings encountered when uploading general document for a case {}: {}",
                    finremCaseDetails.getId(), warnings.size());
        }

        // Do sorting
        List<UploadGeneralDocumentCollection> uploadedDocuments = new ArrayList<>(ofNullable(caseData.getUploadGeneralDocuments())
            .orElse(List.of()));
        uploadedDocuments.sort(comparing(UploadGeneralDocumentCollection::getValue,
            comparing(UploadGeneralDocument::getGeneralDocumentUploadDateTime, nullsLast(Comparator.reverseOrder()))));
        caseData.setUploadGeneralDocuments(uploadedDocuments);

        // Execute the categoriser for CFV
        uploadGeneralDocumentsCategoriser.categorise(caseData);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .warnings(warnings.stream().sorted().toList())
            .data(caseData).build();
    }

}