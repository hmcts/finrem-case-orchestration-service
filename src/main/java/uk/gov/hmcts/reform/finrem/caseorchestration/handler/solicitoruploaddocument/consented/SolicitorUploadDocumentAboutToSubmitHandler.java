package uk.gov.hmcts.reform.finrem.caseorchestration.handler.solicitoruploaddocument.consented;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremAboutToSubmitCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionTypeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.SolUploadDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.SolUploadDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.State;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static org.apache.commons.collections4.ListUtils.emptyIfNull;

@Slf4j
@Service
public class SolicitorUploadDocumentAboutToSubmitHandler extends FinremAboutToSubmitCallbackHandler {

    public SolicitorUploadDocumentAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper) {
        super(finremCaseDetailsMapper);
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && EventType.SOLICITOR_UPLOAD_DOCUMENT.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToSubmit(callbackRequest));

        FinremCaseData finremCaseData = callbackRequest.getFinremCaseData();
        FinremCaseData finremCaseDataBefore = callbackRequest.getFinremCaseDataBefore();

        boolean isReadyToSubmit = finremCaseData.getGenericInputFields().isReadyToSubmit();
        int numberOfDocuments =
            ofNullable(finremCaseData.getSolUploadDocuments()).orElse(List.of()).size()
            + ofNullable(finremCaseData.getPensionCollection()).orElse(List.of()).size();

        binDeletedCaseDocuments(finremCaseDataBefore, finremCaseData);

        return response(finremCaseData, calculateWarning(isReadyToSubmit, numberOfDocuments), null,
            calculatePostState(isReadyToSubmit));
    }

    private String calculatePostState(boolean isReadyToSubmit) {
        return isReadyToSubmit ? State.INFO_RECEIVED.getStateId() : null;
    }

    private List<String> calculateWarning(boolean isReadyToSubmit, int numberOfDocuments) {
        return List.of(
            isReadyToSubmit ? readyToSubmitWarning(numberOfDocuments) : notReadyToSubmitWarning(numberOfDocuments)
        );
    }

    private String readyToSubmitWarning(int numberOfDocuments) {
        return "The document%s being submitted to the court".formatted(numberOfDocuments > 1 ? "s are" : " is");
    }

    private String notReadyToSubmitWarning(int numberOfDocuments) {
        return "The document%s not been submitted to the court.".formatted(numberOfDocuments > 1 ? "s have" : " has");
    }

    private void binDeletedCaseDocuments(FinremCaseData before, FinremCaseData current) {
        extractDeletedCaseDocuments(before, current).forEach(del ->
            current.getBin().binCaseDocument(del));
    }

    private List<CaseDocument> extractDeletedCaseDocuments(FinremCaseData before, FinremCaseData current) {
        List<CaseDocument> deletedSolDocuments = extractDeletedDocuments(
            emptyIfNull(before.getSolUploadDocuments()).stream()
                .map(SolUploadDocumentCollection::getValue)
                .filter(Objects::nonNull)
                .map(SolUploadDocument::getDocumentLink),

            emptyIfNull(current.getSolUploadDocuments()).stream()
                .map(SolUploadDocumentCollection::getValue)
                .filter(Objects::nonNull)
                .map(SolUploadDocument::getDocumentLink)
        );

        List<CaseDocument> deletedPensionDocuments = extractDeletedDocuments(
            emptyIfNull(before.getPensionCollection()).stream()
                .map(PensionTypeCollection::getTypedCaseDocument)
                .filter(Objects::nonNull)
                .map(PensionType::getPensionDocument),

            emptyIfNull(current.getPensionCollection()).stream()
                .map(PensionTypeCollection::getTypedCaseDocument)
                .filter(Objects::nonNull)
                .map(PensionType::getPensionDocument)
        );

        return Stream.concat(
                deletedSolDocuments.stream(),
                deletedPensionDocuments.stream()
            )
            .toList();
    }

    private List<CaseDocument> extractDeletedDocuments(
        Stream<CaseDocument> previousDocuments,
        Stream<CaseDocument> currentDocuments
    ) {
        Set<String> currentDocumentUrls = currentDocuments
            .filter(Objects::nonNull)
            .map(CaseDocument::getDocumentUrl)
            .collect(Collectors.toSet());

        return previousDocuments
            .filter(Objects::nonNull)
            .filter(previousDocument ->
                !currentDocumentUrls.contains(previousDocument.getDocumentUrl()))
            .toList();
    }
}
