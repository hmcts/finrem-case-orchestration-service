package uk.gov.hmcts.reform.finrem.caseorchestration.service.serialisation;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest;

import java.io.IOException;

@Component
public class FinremCallbackRequestDeserializer implements Deserializer<CallbackRequest> {

    private final ObjectMapper mapper;

    public FinremCallbackRequestDeserializer(ObjectMapper mapper) {
        this.mapper = mapper;
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    }

    @Override
    public CallbackRequest deserialize(String source) {
        try {
            return mapper.readValue(source, new TypeReference<>() {});
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not deserialize callback", e);
        }
    }
}
