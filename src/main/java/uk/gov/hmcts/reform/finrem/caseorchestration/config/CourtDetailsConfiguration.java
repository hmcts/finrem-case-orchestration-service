package uk.gov.hmcts.reform.finrem.caseorchestration.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@Component
@Getter
public class CourtDetailsConfiguration {
    private final Map<String, CourtDetails> courts;

    public CourtDetailsConfiguration(ObjectMapper objectMapper) throws IOException {
        File file = new ClassPathResource("json/court-details.json").getFile();
        courts = objectMapper.readValue(file, new TypeReference<>() {
        });
    }
}
