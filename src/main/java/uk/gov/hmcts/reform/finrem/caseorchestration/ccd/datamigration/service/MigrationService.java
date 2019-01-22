package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.datamigration.service;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.datamigration.model.prod.ProdCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.datamigration.model.v1.CaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.datamigration.model.v1.CaseDetails;

@Service
public class MigrationService {
    public CaseDetails migrateTov1(ProdCaseDetails caseDetails) {
        CaseDetails migratedCaseDetails = new CaseDetails();
        BeanUtils.copyProperties(caseDetails, migratedCaseDetails);
        migratedCaseDetails.setCaseData(new CaseData());
        BeanUtils.copyProperties(caseDetails.getCaseData(), migratedCaseDetails.getCaseData());
        return migratedCaseDetails;
    }
}
