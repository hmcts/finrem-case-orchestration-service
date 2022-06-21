package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;

import java.io.File;
import java.util.Map;
import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_COLLECTION;

@RunWith(MockitoJUnitRunner.class)
public class InterimHearingContestedAboutToStartHandlerTest {

    private InterimHearingContestedAboutToStartHandler interimHearingContestedAboutToStartHandler;
    private final ObjectMapper objectMapper = new ObjectMapper();
    public static final String AUTH_TOKEN = "tokien:)";

    private static final String CONTESTED_INTERIM_HEARING_JSON = "/fixtures/contested/interim-hearing.json";

    @Before
    public void setup() {
        interimHearingContestedAboutToStartHandler  = new InterimHearingContestedAboutToStartHandler(objectMapper);
    }

    @Test
    public void canHandle() {
        assertThat(interimHearingContestedAboutToStartHandler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.INTERIM_HEARING),
            is(true));
    }

    @Test
    public void canNotHandle() {
        assertThat(interimHearingContestedAboutToStartHandler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.INTERIM_HEARING),
            is(false));
    }

    @Test
    public void handle() throws JsonProcessingException {
        CallbackRequest callbackRequest = buildCallbackRequest();
        AboutToStartOrSubmitCallbackResponse handle = interimHearingContestedAboutToStartHandler.handle(callbackRequest, AUTH_TOKEN);
        Assert.assertNotNull(handle.getData().get(INTERIM_HEARING_COLLECTION));
    }

    private CallbackRequest buildCallbackRequest() throws JsonProcessingException {
        JsonNode requestContent = readJsonNodeFromFile();
        Map<String,Object> caseData = objectMapper.readValue(requestContent.toString(), Map.class);
        CaseDetails caseDetails = CaseDetails.builder().id(123L).data(caseData).build();
        return CallbackRequest.builder().eventId("SomeEventId").caseDetails(caseDetails).build();
    }

    private JsonNode readJsonNodeFromFile() {
        try {
            return objectMapper.readTree(
                new File(Objects.requireNonNull(getClass()
                        .getResource(CONTESTED_INTERIM_HEARING_JSON))
                    .toURI()));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}