package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import uk.gov.hmcts.reform.finrem.ccd.domain.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.UploadCaseDocumentCollection;

import java.util.List;

public class OtherDocumentsHandler extends PartyDocumentHandler {

    public OtherDocumentsHandler(String party) {
        super(party);
    }

    @Override
    protected boolean isDocumentTypeValid(String caseDocumentType) {
        return caseDocumentType.equals("other")
            || caseDocumentType.equals("Form B")
            || caseDocumentType.equals("Form F")
            || caseDocumentType.equals("Care Plan")
            || caseDocumentType.equals("Pension Plan");
    }

    @Override
    protected boolean isDocumentTypeValid(CaseDocumentType caseDocumentType) {
        return CaseDocumentType.OTHER.equals(caseDocumentType)
            || CaseDocumentType.FORM_B.equals(caseDocumentType)
            || CaseDocumentType.FORM_F.equals(caseDocumentType)
            || CaseDocumentType.CARE_PLAN.equals(caseDocumentType)
            || CaseDocumentType.PENSION_PLAN.equals(caseDocumentType);
    }

    @Override
    protected List<UploadCaseDocumentCollection> getDocumentCollection(FinremCaseData caseData) {
        return null;
    }

    @Override
    protected void setDocumentCollection(FinremCaseData caseData, List<UploadCaseDocumentCollection> docs) {

    }
}
