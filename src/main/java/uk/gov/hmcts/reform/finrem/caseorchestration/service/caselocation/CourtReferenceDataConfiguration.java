package uk.gov.hmcts.reform.finrem.caseorchestration.service.caselocation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CourtRefData;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class CourtReferenceDataConfiguration {

    private static final String COURT_REF_DATA_FILE = "/json/fr-court-to-refdata-location-mappings.json";

    @Bean
    public Map<String, CourtRefData> courtReferenceDataByName(ObjectMapper objectMapper) {
        try (InputStream inputStream =
                 CourtReferenceDataConfiguration.class.getResourceAsStream(COURT_REF_DATA_FILE)) {
            if (inputStream == null) {
                throw new IllegalStateException("Classpath resource not found: " + COURT_REF_DATA_FILE);
            }

            Map<String, CourtRefData> result = objectMapper.readValue(
                inputStream,
                new TypeReference<Map<String, CourtRefData>>() {});

            return result.entrySet()
                .stream()
                .collect(Collectors.toUnmodifiableMap(
                    entry -> entry.getKey().toLowerCase(),
                    Map.Entry::getValue
                ));
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to parse court reference data: " + COURT_REF_DATA_FILE, exception);
        }
    }
}
