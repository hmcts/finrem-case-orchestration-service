package uk.gov.hmcts.reform.finrem.caseorchestration.service.globalsearch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;

@Slf4j
@Service
@RequiredArgsConstructor
public class GlobalSearchService {


    public void setGlobalSearchData(FinremCaseData caseData) {
        log.info("Received request to set global search fields for case with CCD ID: {}", caseData.getCcdCaseId());
        caseData.setCaseNameHmctsInternal(caseData.getFullApplicantName());
        caseData.setCaseManagementLocation(caseData.getSelectedAllocatedCourt());
    }

}
