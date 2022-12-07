package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.HearingBundleCollectionService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;

@Service
public class ApplicantHearingBundleCollectionService extends HearingBundleCollectionService {

    @Autowired
    public ApplicantHearingBundleCollectionService(EvidenceManagementDeleteService evidenceManagementDeleteService) {
        super(ManageCaseDocumentsCollectionType.APP_HEARING_BUNDLES_COLLECTION, evidenceManagementDeleteService,
            CaseDocumentParty.APPLICANT);
    }
}
