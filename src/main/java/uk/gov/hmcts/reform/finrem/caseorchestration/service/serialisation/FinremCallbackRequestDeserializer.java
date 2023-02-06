package uk.gov.hmcts.reform.finrem.caseorchestration.service.serialisation;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.InvalidCaseDataException;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;

import java.io.IOException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
@RequiredArgsConstructor
public class FinremCallbackRequestDeserializer implements Deserializer<CallbackRequest> {

    private final ObjectMapper mapper;

    @Override
    public CallbackRequest deserialize(String source) {
        mapper.registerModule(new JavaTimeModule());
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        try {
            CallbackRequest callbackRequest = mapper.readValue(source, new TypeReference<>() {
            });
            validateCaseData(callbackRequest);

            return callbackRequest;
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format("Could not deserialize callback %s", e.getMessage()), e);
        }
    }

    public FinremCallbackRequest deserializeFinremCallbackRequest(String source) {
        mapper.registerModule(new JavaTimeModule());
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        try {
            FinremCallbackRequest callbackRequest = mapper.readValue(source, new TypeReference<>() {
            });
            validateCaseData(callbackRequest);

            return callbackRequest;
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format("Could not deserialize callback %s", e.getMessage()), e);
        }
    }

    private void validateCaseData(CallbackRequest callbackRequest) {
        if (callbackRequest == null
            || callbackRequest.getCaseDetails() == null
            || callbackRequest.getCaseDetails().getData() == null) {
            throw new InvalidCaseDataException(BAD_REQUEST.value(), "Missing data from callbackRequest.");
        }
    }


    private void validateCaseData(FinremCallbackRequest callbackRequest) {
        if (callbackRequest == null
            || callbackRequest.getCaseDetails() == null
            || callbackRequest.getCaseDetails().getData() == null) {
            throw new InvalidCaseDataException(BAD_REQUEST.value(), "Missing data from callbackRequest.");
        }
    }

}
