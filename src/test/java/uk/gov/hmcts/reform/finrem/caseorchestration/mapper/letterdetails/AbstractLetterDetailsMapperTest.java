package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;

import java.io.IOException;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.finremCaseDetailsFromResource;

public class AbstractLetterDetailsMapperTest extends BaseServiceTest {

    protected FinremCaseDetails caseDetails;
    protected final ObjectMapper mapper = new ObjectMapper();

    protected void setCaseDetails(String resource) {
        try {
            caseDetails = finremCaseDetailsFromResource(getResource(resource), mapper);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    protected Map<String, Object> getCaseData(Map<String, Object> placeholdersMap) {
        Map<String, Object> actualCaseDetails = (Map<String, Object>) placeholdersMap.get(CASE_DETAILS);
        return (Map<String, Object>) actualCaseDetails.get(CASE_DATA);
    }
}
