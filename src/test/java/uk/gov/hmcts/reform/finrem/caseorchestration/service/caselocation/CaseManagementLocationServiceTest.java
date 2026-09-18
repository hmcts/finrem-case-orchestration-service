package uk.gov.hmcts.reform.finrem.caseorchestration.service.caselocation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseLocation;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CaseManagementLocationServiceTest {

    private CaseManagementLocationService service;

    @BeforeEach
    void setUp() throws IOException {
        service = new CaseManagementLocationService(new ObjectMapper());
    }

    @Test
    void shouldReturnCaseLocationForKnownCourt() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("consentOrderFRCName", "ABERYSTWYTH JUSTICE CENTRE");

        CaseLocation result = service.getCaseLocation(caseData);

        assertThat(result).isNotNull();
        assertThat(result.getBaseLocation()).isEqualTo("827534");
        assertThat(result.getRegion()).isEqualTo("7");
    }

    @Test
    void shouldReturnCaseLocationWhenCourtNameMatchesIgnoringCase() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("consentOrderFRCName", "Aberystwyth justice centre");

        CaseLocation result = service.getCaseLocation(caseData);

        assertThat(result).isNotNull();
        assertThat(result.getBaseLocation()).isEqualTo("827534");
        assertThat(result.getRegion()).isEqualTo("7");
    }

    @Test
    void shouldReturnNullWhenCourtNameDoesNotExist() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("consentOrderFRCName", "Unknown Court");

        assertThat(service.getCaseLocation(caseData)).isNull();
    }

    @Test
    void shouldReturnNullWhenCourtNameIsBlank() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("consentOrderFRCName", " ");

        assertThat(service.getCaseLocation(caseData)).isNull();
    }

    @Test
    void shouldReturnNullWhenCourtNameIsMissing() {
        assertThat(service.getCaseLocation(new HashMap<>())).isNull();
    }
}
