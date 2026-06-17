package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.POINTS_OF_CLAIM_DEFENCE;

@Configuration
public class CitizenDocumentsCategoriser extends DocumentCategoriser {

    private static final List<CitizenUploadDocumentType> POINTS_OF_CLAIM = List.of(POINTS_OF_CLAIM_DEFENCE);


    public CitizenDocumentsCategoriser(FeatureToggleService featureToggleService) {
        super(featureToggleService);
    }

    @Override
    protected void categoriseDocuments(FinremCaseData finremCaseData) {
        if (CollectionUtils.isNotEmpty(finremCaseData.getCitizenDocumentWrapper().getCitizenApplicantDocument())) {
            finremCaseData.getCitizenDocumentWrapper().getCitizenApplicantDocument().forEach(doc -> checkTypeAndSetCategory(doc.getValue()));
        }
    }

    private void checkTypeAndSetCategory(CitizenUploadDocument document) {
        if (!isDocumentDataValid(document)) {
            return;
        }

        if (POINTS_OF_CLAIM.contains(document.getDocumentType())) {
            CaseDocument documentCopy = new CaseDocument(document.getDocumentLink());
            setCategoryToAllOrdersDocs(documentCopy, DocumentCategory.APPLICANT_DOCUMENTS_POINTS_OF_CLAIM_OR_DEFENCE.getDocumentCategoryId());
            document.setDocumentLink(documentCopy);
        } else {
            CaseDocument documentCopy = new CaseDocument(document.getDocumentLink());
            setCategoryToAllOrdersDocs(documentCopy, null);
            document.setDocumentLink(documentCopy);
        }
    }

    private boolean isDocumentDataValid(CitizenUploadDocument document) {
        return document != null && document.getDocumentLink() != null;
    }

    private void setCategoryToAllOrdersDocs(CaseDocument document, String categoryToApply) {
        if (document != null) {
            document.setCategoryId(categoryToApply);
        }
    }
}
