package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails;

import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDataContested;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import java.util.Map;

public abstract class ContestedAbstractLetterDetailsMapperTest extends BaseServiceTest {

    protected FinremCaseDetails<FinremCaseDataContested> caseDetails;

    protected void setCaseDetails(String resource) {
        caseDetails = finremCaseDetailsMapper.mapToFinremCaseDetails(buildCaseDetailsFromJson(resource));
    }

    protected Map<String, Object> getCaseData(Map<String, Object> placeholdersMap) {
        Map<String, Object> actualCaseDetails = (Map<String, Object>) placeholdersMap.get(CASE_DETAILS);
        return (Map<String, Object>) actualCaseDetails.get(CASE_DATA);
    }


}
