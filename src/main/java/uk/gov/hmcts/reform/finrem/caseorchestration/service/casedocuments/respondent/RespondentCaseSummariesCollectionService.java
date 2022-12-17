package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseSummariesCollectionService;

@Service
public class RespondentCaseSummariesCollectionService extends CaseSummariesCollectionService {

    @Autowired
    public RespondentCaseSummariesCollectionService() {
        super(ManageCaseDocumentsCollectionType.RESP_CASE_SUMMARIES_COLLECTION,
                CaseDocumentParty.RESPONDENT);
    }
}
