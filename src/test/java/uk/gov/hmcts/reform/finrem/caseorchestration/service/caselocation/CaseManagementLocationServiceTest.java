package uk.gov.hmcts.reform.finrem.caseorchestration.service.caselocation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CourtRefData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseLocation;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CaseManagementLocationServiceTest {

    private CaseManagementLocationService service;
    private Map<String, CourtRefData> courtReferenceDataByName;

    @BeforeEach
    void setUp() {
        courtReferenceDataByName = new HashMap<>();

        courtReferenceDataByName.put(
            "FR_bristolList_3",
            CourtRefData.builder()
                .epimmsId("123456")
                .regionId("1")
                .build()
        );

        courtReferenceDataByName.put(
            "FR_londonList_1",
            CourtRefData.builder()
                .epimmsId("654321")
                .regionId("2")
                .build()
        );

        service = new CaseManagementLocationService(courtReferenceDataByName);
    }

    @Test
    void shouldReturnCaseLocationWhenCourtExists() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("regionMiddleCourtList", "FR_bristolList_3");

        CaseLocation location = service.getCaseLocation(caseData);

        assertThat(location).isNotNull();
        assertThat(location.getBaseLocation()).isEqualTo("123456");
        assertThat(location.getRegion()).isEqualTo("1");
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

    @Test
    void shouldUseFirstCourtListWhenMultipleCourtListFieldsFound() {
        Map<String, Object> caseData = new LinkedHashMap<>();

        caseData.put("bristolFRCourtList", "FR_bristolList_3");
        caseData.put("londonFRCourtList", "FR_londonList_1");

        CaseLocation location = service.getCaseLocation(caseData);

        assertThat(location).isNotNull();
        assertThat(location.getBaseLocation()).isEqualTo("123456");
        assertThat(location.getRegion()).isEqualTo("1");
    }
}
