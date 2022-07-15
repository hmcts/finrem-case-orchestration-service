package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.serialisation.FinremCallbackRequestDeserializer;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest;

import java.io.File;

public class BaseHandlerTest {

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
                new File(getClass()
                    .getResource(jsonPath)
                    .toURI()));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
