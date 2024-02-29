package uk.gov.hmcts.reform.finrem.caseorchestration.handler.uploadgeneraldocuments;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadGeneralDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentchecker.DocumentCheckerService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class UploadGeneralDocumentsContestedMidEventHandler extends FinremCallbackHandler {

    private final DocumentCheckerService documentCheckerService;

    public UploadGeneralDocumentsContestedMidEventHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                          DocumentCheckerService documentCheckerService) {
        super(finremCaseDetailsMapper);
        this.documentCheckerService = documentCheckerService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.MID_EVENT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.UPLOAD_GENERAL_DOCUMENT.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(
        FinremCallbackRequest callbackRequestWithFinremCaseDetails, String userAuthorisation) {

        log.info("Mid-event Upload document callback for case {}",
            callbackRequestWithFinremCaseDetails.getCaseDetails().getId());

        FinremCaseData caseData = callbackRequestWithFinremCaseDetails.getCaseDetails().getData();

        List<String> warnings = new ArrayList<>();

        List<UploadGeneralDocumentCollection> newDocuments = getNewlyUploadedDocuments(
            callbackRequestWithFinremCaseDetails.getCaseDetails().getData().getUploadGeneralDocuments(),
            callbackRequestWithFinremCaseDetails.getCaseDetailsBefore().getData().getUploadGeneralDocuments());
        if (ObjectUtils.isNotEmpty(newDocuments)) {
            newDocuments.forEach(d -> {
                List<String> documentWarnings = documentCheckerService.getWarnings(d.getValue().getDocumentLink(),
                    callbackRequestWithFinremCaseDetails.getCaseDetails(), userAuthorisation);
                warnings.addAll(documentWarnings);
            });
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .warnings(warnings)
            .data(caseData)
            .build();
    }

    private List<UploadGeneralDocumentCollection> getNewlyUploadedDocuments(
        List<UploadGeneralDocumentCollection> uploadedDocuments,
        List<UploadGeneralDocumentCollection> previousDocuments) {

        if (ObjectUtils.isEmpty(uploadedDocuments)) {
            return Collections.emptyList();
        } else if (ObjectUtils.isEmpty(previousDocuments)) {
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
