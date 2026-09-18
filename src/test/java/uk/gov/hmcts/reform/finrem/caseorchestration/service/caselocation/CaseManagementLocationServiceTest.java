package uk.gov.hmcts.reform.finrem.caseorchestration.service.caselocation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    void shouldReturnNullWhenCourtListDoesNotExist() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("someOtherField", "value");

        assertThat(service.getCaseLocation(caseData)).isNull();
    }

    @Test
    void shouldReturnNullWhenCourtListValueIsUnknown() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("regionMiddleCourtList", "Unknown Court");

        assertThat(service.getCaseLocation(caseData)).isNull();
    }

    @Test
    void shouldReturnNullWhenCourtListValueIsBlank() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("regionMiddleCourtList", " ");

        assertThat(service.getCaseLocation(caseData)).isNull();
    }

    @Test
    void shouldReturnNullWhenCourtListValueIsNull() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("regionMiddleCourtList", null);

        assertThat(service.getCaseLocation(caseData)).isNull();
    }

    @Test
    void shouldReturnNullWhenCaseDataIsEmpty() {
        assertThat(service.getCaseLocation(new HashMap<>())).isNull();
    }
}
