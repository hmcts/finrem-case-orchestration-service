package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerthree;

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
public class IntervenerThreeFdrHandler extends PartyDocumentsHandler {

    private final IntervenerThreeFdrDocumentCategoriser categoriser;

    public IntervenerThreeFdrHandler(FeatureToggleService featureToggleService, IntervenerThreeFdrDocumentCategoriser categoriser) {
        super(CaseDocumentCollectionType.INTERVENER_THREE_FDR_DOCS_COLLECTION, CaseDocumentParty.INTERVENER_THREE, featureToggleService);
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
