package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.StatementExhibitsHandler;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.UploadCaseDocumentCollection;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class RespondentStatementsExhibitsHandler extends StatementExhibitsHandler {

    @Autowired
    public RespondentStatementsExhibitsHandler() {
        super(RESPONDENT);
    }

    @Override
    protected List<UploadCaseDocumentCollection> getDocumentCollection(FinremCaseData caseData) {
        return Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getRespStatementsExhibitsCollection())
            .orElse(new ArrayList<>());
    }

    @Override
    protected void setDocumentCollection(FinremCaseData caseData, List<UploadCaseDocumentCollection> docs) {
        caseData.getUploadCaseDocumentWrapper().setRespStatementsExhibitsCollection(docs);
    }
}
