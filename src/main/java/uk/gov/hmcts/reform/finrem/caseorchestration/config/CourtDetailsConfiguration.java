package uk.gov.hmcts.reform.finrem.caseorchestration.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Slf4j
@Component
@Getter
public class CourtDetailsConfiguration {
    private final Map<String, CourtDetails> courts;

    public CourtDetailsConfiguration(ObjectMapper objectMapper) throws IOException {
        InputStream inputStream = CourtDetailsConfiguration.class
                .getResourceAsStream("/json/court-details.json");
        try {
            courts = objectMapper.readValue(inputStream, new TypeReference<>() {
            });
        } finally {
            if (inputStream != null) {
                safeClose(inputStream);
            }
        }
    }

    public static void safeClose(InputStream inputStream) {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                log.error("Failed to close inputStream in CourtDetailsConfiguration.java: {}", e.getMessage());
            }
        }
    }
}
