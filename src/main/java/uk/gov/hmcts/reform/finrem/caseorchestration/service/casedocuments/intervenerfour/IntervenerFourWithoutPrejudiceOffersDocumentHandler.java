package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerfour;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.WithoutPrejudiceOffersDocumentsHandler;

@Component
public class IntervenerFourWithoutPrejudiceOffersDocumentHandler extends WithoutPrejudiceOffersDocumentsHandler {

    @Autowired
    protected IntervenerFourWithoutPrejudiceOffersDocumentHandler(FeatureToggleService featureToggleService) {
        super(CaseDocumentParty.INTERVENER_FOUR, featureToggleService);
    }

    @Override
    protected DocumentCategory getDocumentCategoryFromDocumentType(CaseDocumentType caseDocumentType) {
        return DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_4_WITHOUT_PREJUDICE_OFFERS;
    }
}
