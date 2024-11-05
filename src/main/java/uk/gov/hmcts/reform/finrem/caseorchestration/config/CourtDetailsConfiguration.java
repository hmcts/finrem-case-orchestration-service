package uk.gov.hmcts.reform.finrem.caseorchestration.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@Getter
public class CourtDetailsConfiguration {
    private final Map<String, CourtDetails> courts;

    public CourtDetailsConfiguration(ResourceLoader resourceLoader, ObjectMapper objectMapper) throws IOException {
        Resource resource = resourceLoader.getResource("classpath:json/court-details.json");
        courts = objectMapper.readValue(resource.getFile(), new TypeReference<>() {
        });
    }
}
