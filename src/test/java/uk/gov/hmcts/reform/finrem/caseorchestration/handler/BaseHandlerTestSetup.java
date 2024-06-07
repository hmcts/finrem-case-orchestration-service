package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.serialisation.FinremCallbackRequestDeserializer;

import java.io.File;
import java.io.InputStream;
import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

public class BaseHandlerTestSetup {

    protected JsonNode requestContent;

    protected ObjectMapper objectMapper = JsonMapper
        .builder()
        .addModule(new JavaTimeModule())
        .addModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES))
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .build();
    protected FinremCallbackRequestDeserializer deserializer = new FinremCallbackRequestDeserializer(objectMapper);

    protected CallbackRequest getCallbackRequestFromResource(String path) {
        return deserializer.deserialize(resourceContentAsString(path));
    }

    protected String resourceContentAsString(String resourcePath) {
        return readJsonNodeFromFile(resourcePath).toString();
    }

    private JsonNode readJsonNodeFromFile(String jsonPath) {
        try {
            return objectMapper.readTree(
                new File(Objects.requireNonNull(getClass()
                        .getResource(jsonPath))
                    .toURI()));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    protected CallbackRequest buildCallbackRequest(String testJson) {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(testJson)) {
            CaseDetails caseDetails =
                objectMapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
            return CallbackRequest.builder().caseDetails(caseDetails).build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected FinremCallbackRequest buildCallbackRequest(EventType eventType) {
        return FinremCallbackRequest
            .builder()
            .eventType(eventType)
            .caseDetailsBefore(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(new FinremCaseData()).build())
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(new FinremCaseData()).build())
            .build();
    }

    protected FinremCallbackRequest buildConsentCallbackRequest(EventType eventType) {
        return FinremCallbackRequest
            .builder()
            .eventType(eventType)
            .caseDetailsBefore(FinremCaseDetails.builder().id(123L).caseType(CONSENTED)
                .data(new FinremCaseData()).build())
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONSENTED)
                .data(new FinremCaseData()).build())
            .build();
    }

    protected FinremCallbackRequest buildFinremCallbackRequest(String testJson) {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(testJson)) {
            FinremCaseDetails caseDetails =
                objectMapper.readValue(resourceAsStream, FinremCallbackRequest.class).getCaseDetails();
            return FinremCallbackRequest.builder().caseDetails(caseDetails).build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void assertCanHandle(CallbackHandler handler, CallbackType expectedCallbackType, CaseType expectedCaseType,
                                   EventType expectedEventType) {
        for (CallbackType callbackType : CallbackType.values()) {
            for (CaseType caseType : CaseType.values()) {
                for (EventType eventType : EventType.values()) {
                    boolean expectedOutcome = callbackType == expectedCallbackType
                            && caseType == expectedCaseType
                            && eventType == expectedEventType; // This condition will always be true
                    assertThat(handler.canHandle(callbackType, caseType, eventType), equalTo(expectedOutcome));
                }
            }
        }
    }
}
