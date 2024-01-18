package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

public abstract class WithoutPrejudiceOffersDocumentsHandler extends PartyDocumentsHandler {
    protected WithoutPrejudiceOffersDocumentsHandler(
        CaseDocumentParty party,
        FeatureToggleService featureToggleService) {
        super(CaseDocumentCollectionType.CONTESTED_FDR_CASE_DOCUMENT_COLLECTION, party, featureToggleService);
    }

    @Override
    protected boolean canHandleDocument(UploadCaseDocument uploadCaseDocument) {
        CaseDocumentType caseDocumentType = uploadCaseDocument.getCaseDocumentType();
        return caseDocumentType.equals(CaseDocumentType.WITHOUT_PREJUDICE_OFFERS);
    }

}
