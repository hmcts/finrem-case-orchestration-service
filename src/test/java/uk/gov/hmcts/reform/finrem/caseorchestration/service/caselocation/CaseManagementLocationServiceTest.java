package uk.gov.hmcts.reform.finrem.caseorchestration.service.caselocation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseLocation;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CaseManagementLocationServiceTest {

    private final CaseManagementLocationService service =
        new CaseManagementLocationService();

    @Test
    void shouldReturnCaseLocationWhenCourtListFieldExists() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("familyCourtList", "London");

        CaseLocation result = service.getCaseLocation(caseData);

        assertThat(result).isNotNull();
        assertThat(result.getBaseLocation()).isEqualTo("438850");
        assertThat(result.getRegion()).isEqualTo("438850");
    }

    @Test
    void shouldReturnNullWhenNoCourtListFieldExists() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("courtName", "London");

        CaseLocation result = service.getCaseLocation(caseData);

        assertThat(result).isNull();
    }

    @Test
    void shouldReturnCaseLocationWhenMultipleCourtListFieldsExist() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("hearingCourtList", "London");
        caseData.put("regionalCourtList", "Birmingham");

        CaseLocation result = service.getCaseLocation(caseData);

        assertThat(result).isNotNull();
        assertThat(result.getBaseLocation()).isEqualTo("438850");
        assertThat(result.getRegion()).isEqualTo("438850");
    }
}