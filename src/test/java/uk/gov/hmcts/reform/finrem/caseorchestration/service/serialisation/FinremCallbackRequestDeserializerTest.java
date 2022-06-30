package uk.gov.hmcts.reform.finrem.caseorchestration.service.serialisation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest;
import uk.gov.hmcts.reform.finrem.ccd.domain.EventType;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FinremCallbackRequestDeserializerTest {

    private static final String REFUSAL_ORDER_CALLBACK_REQUEST = "fixtures/refusal-order-contested.json";
    private static final String CONTESTED_INTERIM_CALLBACK_REQUEST = "fixtures/contested-interim-hearing.json";
    private static final String SOL_CONTEST_CALLBACK_REQUEST = "fixtures/deserialisation/ccd-request-with-solicitor-contestApplicationIssued.json";

    private FinremCallbackRequestDeserializer callbackRequestDeserializer;

    private ObjectMapper objectMapper;

    private String callback;

    @Before
    public void testSetUp() {
        objectMapper = new ObjectMapper();
        callbackRequestDeserializer = new FinremCallbackRequestDeserializer(objectMapper);
    }

    @Test
    public void givenValidCallbackRequest_whenDeserializeFromString_thenSuccessfullyDeserialize() throws IOException {
        setCallbackString(REFUSAL_ORDER_CALLBACK_REQUEST);
        CallbackRequest callbackRequest = callbackRequestDeserializer.deserialize(callback);

        assertNotNull(callbackRequest);
        EventType eventType = callbackRequest.getEventType();
        assertEquals(eventType, EventType.GIVE_ALLOCATION_DIRECTIONS);
        FinremCaseData caseData = callbackRequest.getCaseDetails().getCaseData();
        assertEquals(caseData.getContactDetailsWrapper().getApplicantRepresented(), YesOrNo.YES);
        assertEquals(caseData.getRegionWrapper().getDefaultCourtList().getNottinghamCourtList().getId(), "FR_s_NottinghamList_1");
    }


    @Test
    public void givenGeneralOrderFixture_whenDeserializeFromString_thenSuccessfullyDeserialize() throws IOException {
        setCallbackString(CONTESTED_INTERIM_CALLBACK_REQUEST);
        CallbackRequest callbackRequest = callbackRequestDeserializer.deserialize(callback);

        assertNotNull(callbackRequest);
    }

    @Test
    public void givenCcdRequestAppIssued_whenDeserializeFromString_thenSuccessfullyDeserialize() throws IOException {
        setCallbackString(SOL_CONTEST_CALLBACK_REQUEST);
        CallbackRequest callbackRequest = callbackRequestDeserializer.deserialize(callback);

        assertNotNull(callbackRequest);
    }

    private void setCallbackString(String fileName) throws IOException {
        String path = Objects.requireNonNull(getClass().getClassLoader().getResource(fileName)).getFile();
        callback = FileUtils.readFileToString(new File(path), StandardCharsets.UTF_8.name());
    }
}
