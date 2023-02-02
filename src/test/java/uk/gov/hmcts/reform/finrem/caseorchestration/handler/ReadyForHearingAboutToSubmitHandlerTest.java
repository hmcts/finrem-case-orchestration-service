package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedHearingHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentedHearingDataWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService.REQUIRED_FIELD_EMPTY_ERROR;

@RunWith(MockitoJUnitRunner.class)
public class ReadyForHearingAboutToSubmitHandlerTest extends BaseHandlerTest {

    @InjectMocks
    private ReadyForHearingAboutToSubmitHandler handler;

    @Mock
    private ValidateHearingService service;

    private ConsentedHearingHelper helper;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String AUTH_TOKEN = "tokien:)";
    private static final String TEST_JSON_WITH_HEARING = "/fixtures/consented.listOfHearing/list-for-hearing.json";

    @Before
    public void setup() {
        helper = new ConsentedHearingHelper(objectMapper);
    }

    @Test
    public void canHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.READY_FOR_HEARING),
            is(true));
    }

    @Test
    public void canNotHandleWrongCaseType() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.READY_FOR_HEARING),
            is(false));
    }

    @Test
    public void canNotHandleWrongEvent() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.CLOSE),
            is(false));
    }

    @Test
    public void canNotHandleWrongCallbackType() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.READY_FOR_HEARING),
            is(false));
    }


    @Test
    public void givenConsentedCase_WhenHearingListed_ThenShouldBeReadyForHearing() {
        CallbackRequest callbackRequest = buildCallbackRequestWithHearingListed();
        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> response =
            handler.handle(callbackRequest, AUTH_TOKEN);

        Map<String, Object> caseData = response.getData();
        List<ConsentedHearingDataWrapper> hearings = helper.getHearings(caseData);
        assertEquals("2012-05-19", hearings.get(0).getValue().hearingDate);
        verify(service).validateHearingErrors(any());
        assertNull(response.getErrors());
    }


    @Test
    public void givenConsentedCase_WhenHearingNotListed_ThenShouldNotBeReadyForHearing() {

        when(service.validateHearingErrors(any())).thenReturn(ImmutableList.of(REQUIRED_FIELD_EMPTY_ERROR));
        CallbackRequest callbackRequest = buildCallbackRequestEmptyCaseData();
        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> response = handler.handle(callbackRequest, AUTH_TOKEN);
        verify(service).validateHearingErrors(any());
        assertEquals(List.of("There is no hearing on the case."), response.getErrors());
    }


    private CallbackRequest buildCallbackRequestWithHearingListed() {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(TEST_JSON_WITH_HEARING)) {
            return objectMapper.readValue(resourceAsStream, CallbackRequest.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private CallbackRequest buildCallbackRequestEmptyCaseData() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(DIVORCE_CASE_NUMBER, TEST_DIVORCE_CASE_NUMBER);

        return CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .caseTypeId(uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED.getCcdType())
                .id(12345L)
                .data(caseData)
                .build())
            .build();
    }
}
