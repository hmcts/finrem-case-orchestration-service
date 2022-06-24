package uk.gov.hmcts.reform.finrem.caseorchestration.service.serialisation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FinremCallbackRequestDeserializerTest {

    private final String CALLBACK_REQUEST_JSON_FIXTURE = "fixtures/refusal-order-contested.json";

    private FinremCallbackRequestDeserializer callbackRequestDeserializer;

    private ObjectMapper objectMapper;

    private String callback;

    @Before
    public void testSetUp() throws IOException {
        String path = Objects.requireNonNull(getClass().getClassLoader().getResource(CALLBACK_REQUEST_JSON_FIXTURE)).getFile();
        callback = FileUtils.readFileToString(new File(path), StandardCharsets.UTF_8.name());
        objectMapper = new ObjectMapper();
    }

    @Test
    public void givenValidCallbackRequest_whenDeserializeFromString_thenSuccessfullyDeserialize() {
        callbackRequestDeserializer = new FinremCallbackRequestDeserializer(objectMapper);

        CallbackRequest callbackRequest = callbackRequestDeserializer.deserialize(callback);

        assertNotNull(callbackRequest);
    }
}
