package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

@Service
public class IntervenerTwoFdrHandler extends PartyDocumentsHandler {

    private final FeatureToggleService featureToggleService;

    public IntervenerTwoFdrHandler(FeatureToggleService featureToggleService) {
        super(CaseDocumentCollectionType.INTERVENER_TWO_FDR_DOCS_COLLECTION, CaseDocumentParty.INTERVENER_TWO, featureToggleService);
        this.featureToggleService = featureToggleService;
    }

    @Override
    protected boolean canHandleDocument(UploadCaseDocument uploadCaseDocument) {
        return uploadCaseDocument.getCaseDocumentFdr().equals(YesOrNo.YES);
    }


    @Override
    public DocumentCategory getDocumentCategoryFromDocumentType(CaseDocumentType caseDocumentType) {
        // TODO This had to be changed to public - discuss with Ruban
        return DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE;
        //TODO Check that this is correct category
    }
}
