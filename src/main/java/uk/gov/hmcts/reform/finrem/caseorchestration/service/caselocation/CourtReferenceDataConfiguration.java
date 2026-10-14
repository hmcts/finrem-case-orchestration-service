package uk.gov.hmcts.reform.finrem.caseorchestration.service.caselocation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CourtRefData;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class CourtReferenceDataConfiguration {

    private static final String COURT_REF_DATA_FILE = "/json/fr-court-to-refdata-location-mappings.json";

    @Bean
    public Map<String, CourtRefData> courtReferenceDataByName(ObjectMapper objectMapper) throws IOException {

        try (InputStream inputStream =
                 CourtReferenceDataConfiguration.class.getResourceAsStream(COURT_REF_DATA_FILE)) {
            if (inputStream == null) {
                throw new IOException(
                    "Unable to load court reference data from " + COURT_REF_DATA_FILE);
            }
            return objectMapper.readValue(
                    inputStream,
                    new TypeReference<Map<String, CourtRefData>>() {})
                .entrySet()
                .stream()
                .collect(Collectors.toUnmodifiableMap(
                    entry -> entry.getKey().toLowerCase(),
                    Map.Entry::getValue
                ));
        }
    }
}
