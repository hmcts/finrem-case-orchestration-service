package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerone;

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
public class IntervenerOneFdrHandler extends PartyDocumentsHandler {

    private final IntervenerOneFdrDocumentCategoriser categoriser;

    public IntervenerOneFdrHandler(FeatureToggleService featureToggleService, IntervenerOneFdrDocumentCategoriser categoriser) {
        super(CaseDocumentCollectionType.INTERVENER_ONE_FDR_DOCS_COLLECTION, CaseDocumentParty.INTERVENER_ONE, featureToggleService);
        this.categoriser = categoriser;
    }

    @Override
    protected boolean canHandleDocument(UploadCaseDocument uploadCaseDocument) {
        YesOrNo caseDocumentFdr = uploadCaseDocument.getCaseDocumentFdr();
        return caseDocumentFdr != null && caseDocumentFdr.equals(YesOrNo.YES);
    }


    @Override
    public DocumentCategory getDocumentCategoryFromDocumentType(CaseDocumentType caseDocumentType, CaseDocumentParty caseDocumentParty) {
        return categoriser.getDocumentCategory(caseDocumentType);
    }
}
