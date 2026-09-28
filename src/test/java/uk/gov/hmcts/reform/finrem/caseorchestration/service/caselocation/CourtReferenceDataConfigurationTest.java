package uk.gov.hmcts.reform.finrem.caseorchestration.service.caselocation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CourtRefData;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CourtReferenceDataConfigurationTest {

    private final CourtReferenceDataConfiguration configuration =
        new CourtReferenceDataConfiguration();

    @Test
    void shouldLoadCourtReferenceDataAndConvertKeysToLowerCase() throws Exception {
        ObjectMapper objectMapper = mock(ObjectMapper.class);

        CourtRefData courtRefData = CourtRefData.builder()
            .epimmsId("123")
            .regionId("1")
            .courtName("Test Court")
            .build();

        Map<String, CourtRefData> sourceData = Map.of(
            "FR_Swansea_HC_List_2", courtRefData
        );

        when(objectMapper.readValue(any(InputStream.class), any(TypeReference.class))).thenReturn(sourceData);

        Map<String, CourtRefData> result = configuration.courtReferenceDataByName(objectMapper);

        assertThat(result)
            .containsKey("fr_swansea_hc_list_2")
            .doesNotContainKey("FR_Swansea_HC_List_2");

        assertThat(result.get("fr_swansea_hc_list_2"))
            .isEqualTo(courtRefData);
    }

    @Test
    void shouldReturnUnmodifiableMap() throws Exception {
        ObjectMapper objectMapper = mock(ObjectMapper.class);

        CourtRefData courtRefData = CourtRefData.builder().build();

        when(objectMapper.readValue(any(InputStream.class), any(TypeReference.class)))
            .thenReturn(Map.of("TEST", courtRefData));

        Map<String, CourtRefData> result = configuration.courtReferenceDataByName(objectMapper);

        assertThrows(UnsupportedOperationException.class, () -> result.put("another", courtRefData));
    }

    @Test
    void shouldThrowIoExceptionWhenResourceCannotBeLoaded() {
        ObjectMapper objectMapper = mock(ObjectMapper.class);

        CourtReferenceDataConfiguration configurationWithoutResource =
            new CourtReferenceDataConfiguration() {
                @Override
                public Map<String, CourtRefData> courtReferenceDataByName(
                    ObjectMapper objectMapper
                ) throws IOException {
                    throw new IOException(
                        "Unable to load court reference data from "
                            + "/json/fr-court-to-refdata-location-mappings.json"
                    );
                }
            };

        IOException exception = assertThrows(
            IOException.class,
            () -> configurationWithoutResource.courtReferenceDataByName(objectMapper)
        );

        assertThat(exception.getMessage())
            .contains("Unable to load court reference data");
    }
}