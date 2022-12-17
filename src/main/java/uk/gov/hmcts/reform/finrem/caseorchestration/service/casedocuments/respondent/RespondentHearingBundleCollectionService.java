package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.HearingBundleCollectionService;

@Service
public class RespondentHearingBundleCollectionService extends HearingBundleCollectionService {

    @Autowired
    public RespondentHearingBundleCollectionService() {
        super(ManageCaseDocumentsCollectionType.RESP_HEARING_BUNDLES_COLLECTION,
                CaseDocumentParty.RESPONDENT);
    }

}
