package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

public abstract class OtherDocumentsHandler extends PartyDocumentsHandler {

    public OtherDocumentsHandler(CaseDocumentCollectionType caseDocumentCollectionType,
                                 CaseDocumentParty party, FeatureToggleService featureToggleService) {
        super(caseDocumentCollectionType, party, featureToggleService);
    }

    @Override
    protected boolean canHandleDocument(UploadCaseDocument uploadCaseDocument) {

        CaseDocumentType caseDocumentType = uploadCaseDocument.getCaseDocumentType();
        return uploadCaseDocument.getCaseDocumentFdr().equals(YesOrNo.NO)
            && (caseDocumentType.equals(CaseDocumentType.OTHER)
            || caseDocumentType.equals(CaseDocumentType.FORM_B)
            || caseDocumentType.equals(CaseDocumentType.FORM_F)
            || caseDocumentType.equals(CaseDocumentType.CARE_PLAN)
            || caseDocumentType.equals(CaseDocumentType.PENSION_PLAN));
    }
}
