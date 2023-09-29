package uk.gov.hmcts.reform.finrem.caseorchestration.model.document;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum DocumentCategory {
    APPLICATIONS("applications"),
    HEARING_NOTICES("hearingNotices"),
    ORDERS("orders"),
    LIP_SCANNED_DOCUMENTS("lipOrScannedDocuments"),
    APPLICANT_DOCUMENTS("applicantDocuments"),
    RESPONDENT_DOCUMENTS("respondentDocuments"),
    INTERVENER_DOCUMENTS("intervenerDocuments"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE("fdrDocumentsAndFDRBundle"),
    HEARING_DOCUMENTS("hearingDocuments"),
    HEARING_BUNDLE("hearingBundle"),
    REPORTS("reports"),
    CORRESPONDENCE("correspondence"),
    CONFIDENTIAL_DOCUMENTS("confidentialDocuments"),
    DIVORCE_DOCUMENTS("divorceDocuments"),
    JUDGMENT_TRANSCRIPT("judgmentOrTranscript"),
    UNCATEGORISED(null);

    private final String documentCategoryId;

    public String getDocumentCategoryId() {
        return documentCategoryId;
    }
}
