package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerfour;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CorrespondenceHandler;

import java.util.Objects;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_FOUR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_FOUR_CORRESPONDENCE_COLLECTION;

@Component
public class IntervenerFourCorrespondenceHandler extends CorrespondenceHandler {

    @Autowired
    public IntervenerFourCorrespondenceHandler(FeatureToggleService featureToggleService) {
        super(INTERVENER_FOUR_CORRESPONDENCE_COLLECTION, INTERVENER_FOUR, featureToggleService);
    }

    @Override
    protected DocumentCategory getDocumentCategoryFromDocumentType(CaseDocumentType caseDocumentType) {
        if (Objects.requireNonNull(caseDocumentType) == CaseDocumentType.OFFERS) {
            return DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_4_OPEN_OFFERS;
        }
        return DocumentCategory.CORRESPONDENCE;
    }
}
