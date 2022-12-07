package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.FormEExhibitsCollectionService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;

@Service
public class RespondentFormEExhibitsCollectionService extends FormEExhibitsCollectionService {

    @Autowired
    public RespondentFormEExhibitsCollectionService(EvidenceManagementDeleteService evidenceManagementDeleteService) {
        super(ManageCaseDocumentsCollectionType.RESP_FORM_E_EXHIBITS_COLLECTION, evidenceManagementDeleteService,
            CaseDocumentParty.RESPONDENT);
    }

}
