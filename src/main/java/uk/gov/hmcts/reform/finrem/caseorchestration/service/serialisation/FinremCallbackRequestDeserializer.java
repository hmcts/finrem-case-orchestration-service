package uk.gov.hmcts.reform.finrem.caseorchestration.service.serialisation;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class FinremCallbackRequestDeserializer implements Deserializer<CallbackRequest> {

    private final ObjectMapper mapper;

    @Override
    public CallbackRequest deserialize(String source) {
        mapper.registerModule(new JavaTimeModule());
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        try {
            return mapper.readValue(source, new TypeReference<>() {});
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not deserialize callback", e);
        }
    }
}
