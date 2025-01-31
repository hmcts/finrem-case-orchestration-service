package uk.gov.hmcts.reform.finrem.caseorchestration.handler.helper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadingDocumentAccessor;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NewUploadedDocumentsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentchecker.DocumentCheckerService;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentWarningsHelper {

    private final DocumentCheckerService documentCheckerService;

    private final NewUploadedDocumentsService newUploadedDocumentsService;

    public <T extends UploadingDocumentAccessor<?>> List<String> getDocumentWarnings(FinremCallbackRequest callbackRequest, Function<FinremCaseData,
        List<T>> accessor, String userAuthorisation) {

        FinremCaseDetails beforeFinremCaseDetails = callbackRequest.getCaseDetailsBefore();
        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();
        final Long caseId = finremCaseDetails.getId();

        FinremCaseData caseData = finremCaseDetails.getData();
        FinremCaseData caseDataBefore = callbackRequest.getCaseDetailsBefore().getData();

        List<CaseDocument> newDocuments = newUploadedDocumentsService.getNewUploadDocuments(caseData, caseDataBefore, accessor);

        Set<String> allWarnings = newDocuments.stream()
            .flatMap(documentLink -> {
                try {
                    return documentCheckerService.getWarnings(documentLink, beforeFinremCaseDetails, finremCaseDetails, userAuthorisation)
                        .stream();
                } catch (Exception e) {
                    log.error("{} - Exception was caught when checking the warnings", caseId, e);
                    return Stream.empty();
                }
            })
            .collect(Collectors.toSet());

        if (!allWarnings.isEmpty()) {
            log.info("{} - Number of warnings encountered when uploading document: {}", caseId, allWarnings.size());
        }

        return allWarnings.stream().sorted().toList();
    }

}
