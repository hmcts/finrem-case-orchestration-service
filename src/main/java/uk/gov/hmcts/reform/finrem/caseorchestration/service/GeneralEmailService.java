package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralEmailCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralEmailHolder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralEmailWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDownloadService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static java.util.Objects.isNull;
import static org.apache.commons.collections4.ListUtils.emptyIfNull;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeneralEmailService {

    private static final int MAX_ATTACHMENTS = 10;
    private static final int MAX_FILE_SIZE = 2 * 1024 * 1024;

    private static final String TOO_MANY_ATTACHMENTS_ERROR = "A maximum of 10 email attachments is supported";
    private static final String FILE_TOO_LARGE_ERROR = "You attached a document which exceeds the size limit: 2MB";
    private static final String INVALID_FILE_TYPE_ERROR = "Only Word and PDF documents are permitted";

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".pdf", ".doc", ".docx");

    private final EvidenceManagementDownloadService evidenceManagementDownloadService;
    private final BulkPrintDocumentService bulkPrintDocumentService;

    public void storeGeneralEmail(FinremCaseData finremCaseData) {
        GeneralEmailWrapper generalEmailWrapper = finremCaseData.getGeneralEmailWrapper();
        List<GeneralEmailCollection> generalEmailCollection = generalEmailWrapper
            .getGeneralEmailCollection();

        if (isNull(generalEmailCollection)) {
            generalEmailCollection = new ArrayList<>();
            generalEmailWrapper.setGeneralEmailCollection(generalEmailCollection);
        }

        GeneralEmailCollection collection = GeneralEmailCollection.builder().value(GeneralEmailHolder.builder()
            .generalEmailBody(generalEmailWrapper.getGeneralEmailBody())
            .generalEmailCreatedBy(generalEmailWrapper.getGeneralEmailCreatedBy())
            .generalEmailRecipient(generalEmailWrapper.getGeneralEmailRecipient())
            .generalEmailUploadedDocuments(generalEmailWrapper.getGeneralEmailUploadedDocuments())
            .generalEmailDateSent(LocalDateTime.now())
            .build()).build();

        generalEmailCollection.add(collection);
    }
    /**
     * Validates the uploaded general email documents and adds any validation errors to the provided list.
     *
     * @param finremCaseData the case data containing the uploaded documents
     * @param userAuthorisation the user authorisation token
     * @param errors the list of validation errors
     */
    public void validateUploadedDocuments(FinremCaseData finremCaseData, String userAuthorisation, List<String> errors) {
        List<CaseDocument> uploadedDocuments = getUploadedDocuments(finremCaseData);

        if (uploadedDocuments.size() > MAX_ATTACHMENTS) {
            addError(errors, TOO_MANY_ATTACHMENTS_ERROR);
        }

        uploadedDocuments.forEach(document ->
            validateUploadedDocument(document, finremCaseData.getCcdCaseId(), userAuthorisation, errors)
        );
    }
    /**
     * Returns the uploaded general email documents from the case data.
     *
     * @param finremCaseData the case data
     * @return the list of uploaded documents, or an empty list if none exist
     */
    public List<CaseDocument> getUploadedDocuments(FinremCaseData finremCaseData) {
        return emptyIfNull(finremCaseData.getGeneralEmailWrapper().getGeneralEmailUploadedDocuments())
            .stream()
            .filter(Objects::nonNull)
            .map(DocumentCollectionItem::getValue)
            .filter(Objects::nonNull)
            .toList();
    }

    private void validateUploadedDocument(CaseDocument document,
                                          String caseId,
                                          String userAuthorisation,
                                          List<String> errors) {
        if (!isAllowedFileType(document)) {
            addError(errors, INVALID_FILE_TYPE_ERROR);
            return;
        }

        if (isFileOverSizeLimit(document, userAuthorisation)) {
            addError(errors, FILE_TOO_LARGE_ERROR);
        }

        if (!errors.contains(FILE_TOO_LARGE_ERROR)) {
            bulkPrintDocumentService.validateEncryptionOnUploadedDocument(
                document,
                caseId,
                errors,
                userAuthorisation
            );
        }
    }

    private boolean isFileOverSizeLimit(CaseDocument document, String userAuthorisation) {
        return evidenceManagementDownloadService.getByteArray(document, userAuthorisation).length > MAX_FILE_SIZE;
    }

    private boolean isAllowedFileType(CaseDocument document) {
        String filename = getFilename(document).toLowerCase();

        return ALLOWED_EXTENSIONS.stream().anyMatch(filename::endsWith);
    }

    private String getFilename(CaseDocument document) {
        return Objects.toString(document.getDocumentFilename(), "");
    }

    private void addError(List<String> errors, String error) {
        if (!errors.contains(error)) {
            errors.add(error);
        }
    }
}
