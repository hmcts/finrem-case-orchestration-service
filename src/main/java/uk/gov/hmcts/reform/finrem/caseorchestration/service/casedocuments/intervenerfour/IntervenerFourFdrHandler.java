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

    public IntervenerFourFdrHandler(FeatureToggleService featureToggleService) {
        super(CaseDocumentCollectionType.INTERVENER_FOUR_FDR_DOCS_COLLECTION, CaseDocumentParty.INTERVENER_FOUR, featureToggleService);
    }

    @Override
    protected boolean canHandleDocument(UploadCaseDocument uploadCaseDocument) {
        return uploadCaseDocument.getCaseDocumentFdr().equals(YesOrNo.YES);
    }

    @Override
    public DocumentCategory getDocumentCategoryFromDocumentType(CaseDocumentType caseDocumentType, CaseDocumentParty caseDocumentParty) {
        if (caseDocumentType.equals(CaseDocumentType.WITHOUT_PREJUDICE_OFFERS)) {
            return DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_4_WITHOUT_PREJUDICE_OFFERS;
        }
        return DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_4;
    }
}
