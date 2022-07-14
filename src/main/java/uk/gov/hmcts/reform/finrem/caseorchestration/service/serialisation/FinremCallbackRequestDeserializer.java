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
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FinremCallbackRequestDeserializer implements Deserializer<CallbackRequest> {

    private final ObjectMapper mapper;

    @Override
    public CallbackRequest deserialize(String source) {
        mapper.registerModule(new JavaTimeModule());
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        try {
            CallbackRequest callbackRequest = mapper.readValue(source, new TypeReference<>() {});
            callbackRequest.getCaseDetails().getCaseData().setCcdCaseType(callbackRequest.getCaseDetails().getCaseType());

            Optional.ofNullable(callbackRequest.getCaseDetailsBefore())
                .flatMap(caseDetails -> Optional.ofNullable(caseDetails.getCaseData()))
                .ifPresent(caseData -> caseData.setCcdCaseType(callbackRequest.getCaseDetails().getCaseType()));

            return callbackRequest;
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format("Could not deserialize callback %s", e.getMessage()), e);
        }
    }
}
