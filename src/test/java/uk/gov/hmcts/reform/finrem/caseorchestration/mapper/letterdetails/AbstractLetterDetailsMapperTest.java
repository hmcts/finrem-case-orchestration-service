package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails;

import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import java.util.Map;

public abstract class AbstractLetterDetailsMapperTest extends BaseServiceTest {

    protected FinremCaseDetails caseDetails;

    protected void setCaseDetails(String resource) {
        caseDetails = finremCaseDetailsMapper.mapToFinremCaseDetails(buildCaseDetailsFromJson(resource));
    }

    /**
     * Retrieves the case data from the placeholders map.
     *
     * <p><b>Deprecated:</b> This method is no longer recommended for use.
     * Developers should use {@link uk.gov.hmcts.reform.finrem.caseorchestration.utils.TestUtils#getCaseData(Map)} instead.
     *
     * @param placeholdersMap the map containing placeholders, including case details
     * @return the extracted case data as a map
     * @deprecated Use {@link uk.gov.hmcts.reform.finrem.caseorchestration.utils.TestUtils#getCaseData(Map)} for improved maintainability.
     */
    @Deprecated
    protected Map<String, Object> getCaseData(Map<String, Object> placeholdersMap) {
        Map<String, Object> actualCaseDetails = (Map<String, Object>) placeholdersMap.get(CASE_DETAILS);
        return (Map<String, Object>) actualCaseDetails.get(CASE_DATA);
    }

}
