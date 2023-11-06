package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

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
public class RespondentCorrespondenceHandler extends CorrespondenceHandler {

    @Autowired
    public RespondentCorrespondenceHandler(FeatureToggleService featureToggleService) {
        super(CaseDocumentCollectionType.RESP_CORRESPONDENCE_COLLECTION,
                CaseDocumentParty.RESPONDENT, featureToggleService);
    }

    @Override
    protected DocumentCategory getDocumentCategoryFromDocumentType(CaseDocumentType caseDocumentType) {
        if (Objects.requireNonNull(caseDocumentType) == CaseDocumentType.OFFERS) {
            return DocumentCategory.RESPONDENT_DOCUMENTS_OPEN_OFFERS;
        }
        return DocumentCategory.CORRESPONDENCE;
    }
}
