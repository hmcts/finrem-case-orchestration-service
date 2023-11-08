package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenertwo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CorrespondenceHandler;

import java.util.Objects;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_TWO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_TWO_CORRESPONDENCE_COLLECTION;

@Component
public class IntervenerTwoCorrespondenceHandler extends CorrespondenceHandler {

    @Autowired
    public IntervenerTwoCorrespondenceHandler(FeatureToggleService featureToggleService) {
        super(INTERVENER_TWO_CORRESPONDENCE_COLLECTION, INTERVENER_TWO, featureToggleService);
    }

    @Override
    protected DocumentCategory getDocumentCategoryFromDocumentType(CaseDocumentType caseDocumentType) {
        if (Objects.requireNonNull(caseDocumentType) == CaseDocumentType.OFFERS) {
            return DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_2_OPEN_OFFERS;
        }
        return DocumentCategory.CORRESPONDENCE_INTERVENER_2;
    }
}
