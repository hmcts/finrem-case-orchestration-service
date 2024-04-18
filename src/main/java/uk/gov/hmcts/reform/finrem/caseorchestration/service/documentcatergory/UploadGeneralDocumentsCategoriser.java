package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadGeneralDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadGeneralDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadGeneralDocumentType.LETTER_EMAIL_FROM_APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadGeneralDocumentType.LETTER_EMAIL_FROM_APPLICANT_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadGeneralDocumentType.LETTER_EMAIL_FROM_RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadGeneralDocumentType.LETTER_EMAIL_FROM_RESPONDENT_CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadGeneralDocumentType.LETTER_EMAIL_FROM_RESPONDENT_SOLICITOR;

@Configuration
public class UploadGeneralDocumentsCategoriser extends DocumentCategoriser {

    private static final List<UploadGeneralDocumentType> APPLICANT_DOC_TYPES = List.of(LETTER_EMAIL_FROM_APPLICANT,
        LETTER_EMAIL_FROM_APPLICANT_SOLICITOR);

    private static final List<UploadGeneralDocumentType> RESPONDENT_DOC_TYPES = List.of(LETTER_EMAIL_FROM_RESPONDENT,
        LETTER_EMAIL_FROM_RESPONDENT_SOLICITOR, LETTER_EMAIL_FROM_RESPONDENT_CONTESTED);

    public UploadGeneralDocumentsCategoriser(FeatureToggleService featureToggleService) {
        super(featureToggleService);
    }

    @Override
    protected void categoriseDocuments(FinremCaseData finremCaseData) {
        if (CollectionUtils.isNotEmpty(finremCaseData.getUploadGeneralDocuments())) {
            finremCaseData.getUploadGeneralDocuments().forEach(doc -> checkTypeAndSetCategory(doc.getValue()));
        }
    }

    private void checkTypeAndSetCategory(UploadGeneralDocument document) {
        if (!isDocumentDataValid(document)) {
            return;
        }

        if (document.getDocumentType() == null) {
            CaseDocument documentCopy = new CaseDocument(document.getDocumentLink());
            setCategoryToAllOrdersDocs(documentCopy, DocumentCategory.CASE_DOCUMENTS.getDocumentCategoryId());
            document.setDocumentLink(documentCopy);
        } else if (APPLICANT_DOC_TYPES.contains(document.getDocumentType())) {
            CaseDocument documentCopy = new CaseDocument(document.getDocumentLink());
            setCategoryToAllOrdersDocs(documentCopy, DocumentCategory.COURT_CORRESPONDENCE_APPLICANT.getDocumentCategoryId());
            document.setDocumentLink(documentCopy);
        } else if (RESPONDENT_DOC_TYPES.contains(document.getDocumentType())) {
            CaseDocument documentCopy = new CaseDocument(document.getDocumentLink());
            setCategoryToAllOrdersDocs(documentCopy, DocumentCategory.COURT_CORRESPONDENCE_RESPONDENT.getDocumentCategoryId());
            document.setDocumentLink(documentCopy);
        } else {
            CaseDocument documentCopy = new CaseDocument(document.getDocumentLink());
            setCategoryToAllOrdersDocs(documentCopy, null);
            document.setDocumentLink(documentCopy);
        }
    }

    private boolean isDocumentDataValid(UploadGeneralDocument document) {
        return document != null && document.getDocumentLink() != null;
    }

    private void setCategoryToAllOrdersDocs(CaseDocument document, String categoryToApply) {
        if (document != null) {
            document.setCategoryId(categoryToApply);
        }
    }
}
