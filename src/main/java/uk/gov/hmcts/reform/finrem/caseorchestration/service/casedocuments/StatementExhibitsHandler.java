package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import uk.gov.hmcts.reform.finrem.ccd.domain.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.UploadCaseDocumentCollection;

import java.util.List;

public class StatementExhibitsHandler extends PartyDocumentHandler {

    public StatementExhibitsHandler(String party) {
        super(party);
    }

    @Override
    protected boolean isDocumentTypeValid(String caseDocumentType) {
        return caseDocumentType.equals("Statement/Affidavit")
            || caseDocumentType.equals("Witness Statement/Affidavit");
    }

    @Override
    protected boolean isDocumentTypeValid(CaseDocumentType caseDocumentType) {
        return caseDocumentType.equals(CaseDocumentType.STATEMENT_AFFIDAVIT)
            || caseDocumentType.equals(CaseDocumentType.WITNESS_STATEMENT_AFFIDAVIT);
    }

    @Override
    protected List<UploadCaseDocumentCollection> getDocumentCollection(FinremCaseData caseData) {
        return null;
    }

    @Override
    protected void setDocumentCollection(FinremCaseData caseData, List<UploadCaseDocumentCollection> docs) {

    }
}
