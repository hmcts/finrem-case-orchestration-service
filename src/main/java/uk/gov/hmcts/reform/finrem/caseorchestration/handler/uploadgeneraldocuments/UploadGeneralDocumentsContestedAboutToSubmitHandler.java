package uk.gov.hmcts.reform.finrem.caseorchestration.handler.uploadgeneraldocuments;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.UploadedGeneralDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadGeneralDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadGeneralDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.UploadGeneralDocumentsCategoriser;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentchecker.DocumentCheckerService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsLast;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Slf4j
@Service
public class UploadGeneralDocumentsContestedAboutToSubmitHandler extends FinremCallbackHandler {

    private final DocumentCheckerService documentCheckerService;
    private final UploadedGeneralDocumentService uploadedGeneralDocumentHelper;
    private final UploadGeneralDocumentsCategoriser uploadGeneralDocumentsCategoriser;

    public UploadGeneralDocumentsContestedAboutToSubmitHandler(FinremCaseDetailsMapper mapper,
                                                               DocumentCheckerService documentCheckerService,
                                                               UploadedGeneralDocumentService uploadedGeneralDocumentHelper,
                                                               UploadGeneralDocumentsCategoriser uploadGeneralDocumentsCategoriser) {
        super(mapper);
        this.documentCheckerService = documentCheckerService;
        this.uploadedGeneralDocumentHelper = uploadedGeneralDocumentHelper;
        this.uploadGeneralDocumentsCategoriser = uploadGeneralDocumentsCategoriser;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.UPLOAD_GENERAL_DOCUMENT.equals(eventType);
    }

    @Override
    @SuppressWarnings("squid:CallToDeprecatedMethod")
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = finremCaseDetails.getData();
        FinremCaseData caseDataBefore = callbackRequest.getCaseDetailsBefore().getData();

        uploadedGeneralDocumentHelper.addUploadDateToNewDocuments(
            caseData,
            callbackRequest.getCaseDetailsBefore().getData());

        final List<String> warnings = getNewlyUploadedDocuments(caseData, caseDataBefore).stream()
                .map(d -> documentCheckerService.getWarnings(d.getValue().getDocumentLink(), finremCaseDetails, userAuthorisation))
                .flatMap(List::stream)
                .toList();
        if (!warnings.isEmpty()) {
            log.info("Number of warnings encountered when uploading general document for a case {}: {}",
                    finremCaseDetails.getId(), warnings.size());
        }

        List<UploadGeneralDocumentCollection> uploadedDocuments = caseData.getUploadGeneralDocuments();

        uploadedDocuments.sort(comparing(UploadGeneralDocumentCollection::getValue,
            comparing(UploadGeneralDocument::getGeneralDocumentUploadDateTime, nullsLast(Comparator.reverseOrder()))));

        caseData.setUploadGeneralDocuments(uploadedDocuments);

        uploadGeneralDocumentsCategoriser.categorise(caseData);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
                .warnings(warnings)
                .data(caseData).build();
    }

    private List<UploadGeneralDocumentCollection> getNewlyUploadedDocuments(FinremCaseData caseData, FinremCaseData caseDataBefore) {
        List<UploadGeneralDocumentCollection> uploadedDocuments = caseData.getUploadGeneralDocuments();
        List<UploadGeneralDocumentCollection> previousDocuments = caseDataBefore.getUploadGeneralDocuments();

        if (isEmpty(uploadedDocuments)) {
            return Collections.emptyList();
        } else if (isEmpty(previousDocuments)) {
            return uploadedDocuments;
        }

        List<UploadGeneralDocumentCollection> newlyUploadedDocuments = new ArrayList<>();
        uploadedDocuments.forEach(d -> {
            boolean exists = previousDocuments.stream()
                    .anyMatch(pd -> pd.getValue().getDocumentLink().getDocumentUrl().equals(d.getValue().getDocumentLink().getDocumentUrl()));
            if (!exists) {
                newlyUploadedDocuments.add(d);
            }
        });

        return newlyUploadedDocuments;
    }

}
