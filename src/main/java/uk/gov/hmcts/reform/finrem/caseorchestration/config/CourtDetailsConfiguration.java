package uk.gov.hmcts.reform.finrem.caseorchestration.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Component
@Getter
public class CourtDetailsConfiguration {
    private final Map<String, CourtDetails> courts;

    public CourtDetailsConfiguration(ObjectMapper objectMapper) throws IOException {
        try (InputStream inputStream = CourtDetailsConfiguration.class
            .getResourceAsStream("/json/court-details.json")) {
            courts = objectMapper.readValue(inputStream, new TypeReference<>() {
            });
        }
    }
}
