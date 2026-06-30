package uk.gov.hmcts.reform.finrem.caseorchestration.service.globalsearch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GlobalSearchService {

    public void setGlobalSearchData(FinremCaseData caseData) {
        log.info("setGlobalSearchData::Received request to set global search fields for case with CCD ID: {}", caseData.getCcdCaseId());
        caseData.setCaseNameHmctsInternal(caseData.getFullApplicantName());
        caseData.setCaseManagementLocation(caseData.getSelectedAllocatedCourt());
    }

    public void setGlobalSearchDataByMap(Map<String, Object> caseData) {
        log.info("setGlobalSearchDataByMap::Received request to set global search fields for case with CCD ID: {}", caseData.get("ccdCaseId"));
        caseData.put("caseNameHmctsInternal", caseData.get("fullApplicantName"));
        caseData.put("caseManagementLocation", caseData.get("selectedAllocatedCourt"));
    }

}
