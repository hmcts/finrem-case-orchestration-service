package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.finrem.ccd.domain.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.UploadCaseDocumentCollection;

import java.util.List;

public class QuestionnairesAnswersHandler extends PartyDocumentHandler {

    public QuestionnairesAnswersHandler(String party) {
        super(party);
    }

    @Override
    protected boolean isDocumentTypeValid(String caseDocumentType) {
        return caseDocumentType.equals("Questionnaire")
            || caseDocumentType.equals("Reply to Questionnaire");
    }

    @Override
    protected boolean isDocumentTypeValid(CaseDocumentType caseDocumentType) {
        return caseDocumentType.equals(CaseDocumentType.QUESTIONNAIRE)
            || caseDocumentType.equals(CaseDocumentType.REPLY_TO_QUESTIONNAIRE);
    }

    @Override
    protected List<UploadCaseDocumentCollection> getDocumentCollection(FinremCaseData caseData) {
        return null;
    }

    @Override
    protected void setDocumentCollection(FinremCaseData caseData, List<UploadCaseDocumentCollection> docs) {

    }
}
