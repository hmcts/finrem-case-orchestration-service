package uk.gov.hmcts.reform.finrem.caseorchestration.model.document;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum DocumentCategory {
    APPLICATIONS("applications"),
    APPLICANT_DOCUMENTS("applicantDocuments"),
    APPLICATIONS_FORM_A("formA"),
    APPLICANT_FORM_G("appFormG"),
    APPLICANT_STATEMENT_OF_ISSUES("appStatementOfIssues"),
    APPLICANT_WITNESS_STATEMENT("appWitnessStatement"),
    CASE_DOCUMENTS("caseDocuments"),
    CONFIDENTIAL_DOCUMENTS("confidentialDocuments"),
    CORRESPONDENCE("correspondence"),
    DIVORCE_DOCUMENTS("divorceDocuments"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE("fdrDocumentsAndFDRBundle"),
    HEARING_BUNDLE("hearingBundle"),
    HEARING_DOCUMENTS("hearingDocuments"),
    HEARING_NOTICES("hearingNotices"),
    INTERVENER_DOCUMENTS("intervenerDocuments"),
    JUDGMENT_TRANSCRIPT("judgmentOrTranscript"),
    LIP_SCANNED_DOCUMENTS("lipOrScannedDocuments"),
    ORDERS("orders"),
    REPORTS("reports"),
    RESPONDENT_DOCUMENTS("respondentDocuments"),
    SHARED("shared"),
    UNCATEGORISED(null);

    private final String documentCategoryId;

    public String getDocumentCategoryId() {
        return documentCategoryId;
    }
}
