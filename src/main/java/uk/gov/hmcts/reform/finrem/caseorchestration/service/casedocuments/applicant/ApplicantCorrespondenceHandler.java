package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CorrespondenceHandler;

import java.util.Objects;

@Service
public class ApplicantCorrespondenceHandler extends CorrespondenceHandler {

    @Autowired
    public ApplicantCorrespondenceHandler(FeatureToggleService featureToggleService) {
        super(CaseDocumentCollectionType.APPLICANT_CORRESPONDENCE_COLLECTION,
            CaseDocumentParty.APPLICANT, featureToggleService);
    }

    @Override
    protected DocumentCategory getDocumentCategoryFromDocumentType(CaseDocumentType caseDocumentType) {
        if (Objects.requireNonNull(caseDocumentType) == CaseDocumentType.OFFERS) {
            return DocumentCategory.APPLICANT_DOCUMENTS_OPEN_OFFERS;
        }
        return DocumentCategory.CORRESPONDENCE_APPLICANT;
    }
}
