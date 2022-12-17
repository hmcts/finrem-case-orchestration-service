package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.HearingBundleCollectionService;

@Service
public class ApplicantHearingBundleCollectionService extends HearingBundleCollectionService {

    @Autowired
    public ApplicantHearingBundleCollectionService() {
        super(ManageCaseDocumentsCollectionType.APP_HEARING_BUNDLES_COLLECTION,
            CaseDocumentParty.APPLICANT);
    }
}
