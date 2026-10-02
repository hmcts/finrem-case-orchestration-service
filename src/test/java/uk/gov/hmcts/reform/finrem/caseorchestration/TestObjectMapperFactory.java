package uk.gov.hmcts.reform.finrem.caseorchestration;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

public final class TestObjectMapperFactory {

    private TestObjectMapperFactory() {
    }

    public static ObjectMapper createObjectMapper() {
        return JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .addModule(new ParameterNamesModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT)
            .build();
    }
}
