package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.serialisation.FinremCallbackRequestDeserializer;

import java.io.File;
import java.io.InputStream;
import java.util.Objects;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

public class BaseHandlerTestSetup {

    protected JsonNode requestContent;

    protected ObjectMapper objectMapper = new ObjectMapper();
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
}
