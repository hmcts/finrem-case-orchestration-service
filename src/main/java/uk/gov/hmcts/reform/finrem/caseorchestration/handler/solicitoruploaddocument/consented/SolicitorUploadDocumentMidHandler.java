package uk.gov.hmcts.reform.finrem.caseorchestration.handler.solicitoruploaddocument.consented;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.ListUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionTypeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.SolUploadDocumentCollection;

import java.util.List;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;

@Slf4j
@Service
public class SolicitorUploadDocumentMidHandler extends FinremCallbackHandler {

    public SolicitorUploadDocumentMidHandler(FinremCaseDetailsMapper finremCaseDetailsMapper) {
        super(finremCaseDetailsMapper);
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.MID_EVENT.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && EventType.SOLICITOR_UPLOAD_DOCUMENT.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.midEvent(callbackRequest));

        FinremCaseData finremCaseData = callbackRequest.getFinremCaseData();

        List<PensionTypeCollection> inputPensionTypeCollection = emptyIfNull(finremCaseData.getPensionCollection());
        List<SolUploadDocumentCollection> inputSolUploadDocuments = emptyIfNull(finremCaseData.getSolUploadDocuments());

        FinremCaseData finremCaseDataBefore = callbackRequest.getFinremCaseDataBefore();
        List<PensionTypeCollection> existingPensionTypeCollection = emptyIfNull(finremCaseDataBefore.getPensionCollection());
        List<SolUploadDocumentCollection> existingSolUploadDocuments = emptyIfNull(finremCaseDataBefore.getSolUploadDocuments());
        if (isBothCollectionEmpty(existingPensionTypeCollection, existingSolUploadDocuments)
            && isBothCollectionEmpty(inputPensionTypeCollection, inputSolUploadDocuments)) {
            return response(finremCaseData, null, List.of(
                "No documents have been uploaded. Please upload at least one document to continue."
            ));
        } else if (isBothCollectionEquals(inputPensionTypeCollection, existingPensionTypeCollection,
            inputSolUploadDocuments, existingSolUploadDocuments)) {
            return response(finremCaseData, null, List.of(
                "You must upload at least one new document to continue."
            ));
        }

        return response(finremCaseData);
    }

    private boolean isBothCollectionEquals(List<PensionTypeCollection> inputPensionTypeCollection,
                                           List<PensionTypeCollection> existingPensionTypeCollection,
                                           List<SolUploadDocumentCollection> inputSolUploadDocuments,
                                           List<SolUploadDocumentCollection> existingSolUploadDocuments) {
        return ListUtils.isEqualList(inputPensionTypeCollection, existingPensionTypeCollection)
            && ListUtils.isEqualList(inputSolUploadDocuments, existingSolUploadDocuments);
    }

    private boolean isBothCollectionEmpty(List<PensionTypeCollection> pensionTypeCollections,
                                          List<SolUploadDocumentCollection> solUploadDocumentCollections) {
        return pensionTypeCollections.isEmpty() && solUploadDocumentCollections.isEmpty();
    }
}
