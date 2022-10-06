package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseLocation;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseManagementLocationService;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_MANAGEMENT_LOCATION;

@RunWith(MockitoJUnitRunner.class)
public class GiveAllocationDirectionsMidEventHandlerTest {

    public static final String BASE_LOCATION = "123458";
    public static final String REGION = "1";

    @Mock
    private CaseManagementLocationService caseManagementLocationService;

    @InjectMocks
    private GiveAllocationDirectionsMidEventHandler giveAllocationDirectionsMidEventHandler;

    @Test
    public void givenCallbackCanBeHandledCreateCase_whenCanHandleCalled_thenReturnTrue() {
        assertThat(giveAllocationDirectionsMidEventHandler
                .canHandle(CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.GIVE_ALLOCATION_DIRECTIONS),
            is(true));
    }

    @Test
    public void givenCallbackCannotBeHandledBadCallbackType_whenCanHandleCalled_thenReturnFalse() {
        assertThat(giveAllocationDirectionsMidEventHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.GIVE_ALLOCATION_DIRECTIONS),
            is(false));
    }

    @Test
    public void givenCallbackCannotBeHandledBadCaseType_whenCanHandleCalled_thenReturnFalse() {
        assertThat(giveAllocationDirectionsMidEventHandler
                .canHandle(CallbackType.MID_EVENT, CaseType.CONSENTED, EventType.GIVE_ALLOCATION_DIRECTIONS),
            is(false));
    }

    @Test
    public void givenCallbackCannotBeHandledBadEventType_whenCanHandleCalled_thenReturnFalse() {
        assertThat(giveAllocationDirectionsMidEventHandler
                .canHandle(CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.MANAGE_CASE_DOCUMENTS),
            is(false));
    }

    @Test
    public void givenValidRequest_whenHandle_thenReturnExpectedResponse() {
        Map<String, Object> caseData = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().data(caseData).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();

        when(caseManagementLocationService.setCaseManagementLocation(any()))
            .thenReturn(aboutToStartOrSubmitCallbackResponse());

        AboutToStartOrSubmitCallbackResponse response = giveAllocationDirectionsMidEventHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(caseManagementLocationService).setCaseManagementLocation(callbackRequest);

        assertThat(response.getData().get(CASE_MANAGEMENT_LOCATION), is(caseLocation()));
    }

    private AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse() {
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(Map.of(CASE_MANAGEMENT_LOCATION, caseLocation()))
            .build();
    }

    private CaseLocation caseLocation() {
        return CaseLocation.builder()
            .baseLocation(BASE_LOCATION)
            .region(REGION)
            .build();
    }

}