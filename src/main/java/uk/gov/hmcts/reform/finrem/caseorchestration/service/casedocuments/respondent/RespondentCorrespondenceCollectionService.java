package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CorrespondenceService;

@Service
public class RespondentCorrespondenceCollectionService extends CorrespondenceService {

    @Autowired
    public RespondentCorrespondenceCollectionService() {
        super(ManageCaseDocumentsCollectionType.RESP_CORRESPONDENCE_COLLECTION,
                CaseDocumentParty.RESPONDENT);
    }
}
