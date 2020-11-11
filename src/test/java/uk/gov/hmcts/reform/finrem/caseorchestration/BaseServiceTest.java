package uk.gov.hmcts.reform.finrem.caseorchestration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CASE_TYPE_ID_CONTESTED;

@TestPropertySource(locations = "/application.properties")
@DirtiesContext
public abstract class BaseServiceTest extends BaseTest {

    @Autowired
    protected ObjectMapper mapper;

    protected CaseDetails buildCaseDetails() {
        Map<String, Object> caseData = new HashMap<>();
        return CaseDetails.builder().id(Long.valueOf(123)).caseTypeId(CASE_TYPE_ID_CONTESTED).data(caseData).build();
    }
}
