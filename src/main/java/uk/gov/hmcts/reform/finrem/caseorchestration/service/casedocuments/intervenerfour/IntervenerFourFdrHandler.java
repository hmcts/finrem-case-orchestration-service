package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerfour;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.PartyDocumentsHandler;

@Component
public class IntervenerFourFdrHandler extends PartyDocumentsHandler {

    private final IntervenerFourFdrDocumentCategoriser categoriser;

    public IntervenerFourFdrHandler(FeatureToggleService featureToggleService, IntervenerFourFdrDocumentCategoriser categoriser) {
        super(CaseDocumentCollectionType.INTERVENER_FOUR_FDR_DOCS_COLLECTION, CaseDocumentParty.INTERVENER_FOUR, featureToggleService);
        this.categoriser = categoriser;
    }

    @Override
    protected boolean canHandleDocument(UploadCaseDocument uploadCaseDocument) {
        return uploadCaseDocument.getCaseDocumentFdr().equals(YesOrNo.YES);
    }

    @Override
    public DocumentCategory getDocumentCategoryFromDocumentType(CaseDocumentType caseDocumentType, CaseDocumentParty caseDocumentParty) {
        return categoriser.getDocumentCategory(caseDocumentType);
    }
}
