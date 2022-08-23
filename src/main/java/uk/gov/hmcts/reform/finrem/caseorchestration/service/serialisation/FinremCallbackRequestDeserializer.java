package uk.gov.hmcts.reform.finrem.caseorchestration.service.serialisation;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.InvalidCaseDataException;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;

import java.io.IOException;
import java.util.Optional;
import java.util.function.BiConsumer;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
@RequiredArgsConstructor
public class FinremCallbackRequestDeserializer implements Deserializer<CallbackRequest> {

    private final ObjectMapper mapper;

    private final BiConsumer<FinremCaseData, CallbackRequest> addExtraDataToCaseDataBefore = (caseDataBefore, callbackRequest) -> {
        caseDataBefore.setCcdCaseType(callbackRequest.getCaseDetails().getCaseType());
        caseDataBefore.setCcdCaseId(String.valueOf(callbackRequest.getCaseDetails().getId()));
    };

    @Override
    public CallbackRequest deserialize(String source) {
        mapper.registerModule(new JavaTimeModule());
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        try {
            CallbackRequest callbackRequest = mapper.readValue(source, new TypeReference<>() {});
            validateCaseData(callbackRequest);
            callbackRequest.getCaseDetails().getCaseData().setCcdCaseType(callbackRequest.getCaseDetails().getCaseType());
            callbackRequest.getCaseDetails().getCaseData().setCcdCaseId(String.valueOf(callbackRequest.getCaseDetails().getId()));

            Optional.ofNullable(callbackRequest.getCaseDetailsBefore())
                .flatMap(caseDetails -> Optional.ofNullable(caseDetails.getCaseData()))
                .ifPresent(caseData -> addExtraDataToCaseDataBefore.accept(caseData, callbackRequest));

            return callbackRequest;
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format("Could not deserialize callback %s", e.getMessage()), e);
        }
    }

    private void validateCaseData(CallbackRequest callbackRequest) {
        if (callbackRequest == null
            || callbackRequest.getCaseDetails() == null
            || callbackRequest.getCaseDetails().getCaseData() == null) {
            throw new InvalidCaseDataException(BAD_REQUEST.value(), "Missing data from callbackRequest.");
        }
    }
}
