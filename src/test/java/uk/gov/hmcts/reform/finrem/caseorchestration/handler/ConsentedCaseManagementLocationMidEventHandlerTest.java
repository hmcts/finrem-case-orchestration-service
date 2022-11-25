package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseLocation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.State;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseManagementLocationService;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.GIVE_ALLOCATION_DIRECTIONS;

@RunWith(MockitoJUnitRunner.class)
public class ConsentedCaseManagementLocationMidEventHandlerTest {

    public static final String BASE_LOCATION = "123458";
    public static final String REGION = "1";

    @Mock
    private CaseManagementLocationService caseManagementLocationService;

    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @InjectMocks
    private ConsentedCaseManagementLocationMidEventHandler consentedCaseManagementLocationMidEventHandler;

    private CallbackRequest callbackRequest;

    @Test
    public void givenCallbackCanBeHandledUpdateFrcInfo_whenCanHandleCalled_thenReturnTrue() {
        assertThat(consentedCaseManagementLocationMidEventHandler
                .canHandle(CallbackType.MID_EVENT, CaseType.CONSENTED, EventType.CONSENTED_UPDATE_COURT_INFO),
            is(true));
    }

    @Test
    public void givenCallbackCannotBeHandledBadCallbackType_whenCanHandleCalled_thenReturnFalse() {
        assertThat(consentedCaseManagementLocationMidEventHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.AMEND_APP_DETAILS),
            is(false));
    }

    @Test
    public void givenCallbackCannotBeHandledBadCaseType_whenCanHandleCalled_thenReturnFalse() {
        assertThat(consentedCaseManagementLocationMidEventHandler
                .canHandle(CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.AMEND_APP_DETAILS),
            is(false));
    }

    @Test
    public void givenCallbackCannotBeHandledBadEventType_whenCanHandleCalled_thenReturnFalse() {
        assertThat(consentedCaseManagementLocationMidEventHandler
                .canHandle(CallbackType.MID_EVENT, CaseType.CONSENTED, EventType.MANAGE_CASE_DOCUMENTS),
            is(false));
    }

    @Test
    public void givenValidRequest_whenHandle_thenReturnExpectedResponse() {
        Map<String, Object> caseData = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().data(caseData).build();
        callbackRequest = CallbackRequest.builder().eventId(GIVE_ALLOCATION_DIRECTIONS.getCcdType())
            .caseDetails(caseDetails).build();
        FinremCaseDetails finremCaseDetails = buildFinremCaseDetails();

        when(finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails)).thenReturn(finremCaseDetails);
        when(caseManagementLocationService.setCaseManagementLocation(any(FinremCallbackRequest.class)))
            .thenReturn(aboutToStartOrSubmitCallbackResponse());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            consentedCaseManagementLocationMidEventHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(caseManagementLocationService).setCaseManagementLocation(finremCallbackRequest(finremCaseDetails));

        assertThat(response.getData().getWorkAllocationWrapper().getCaseManagementLocation(), is(caseLocation()));
    }

    private FinremCaseDetails buildFinremCaseDetails() {
        return FinremCaseDetails.builder().id(1234567890L)
            .caseType(uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED)
            .state(State.CONSENT_ORDER_MADE)
            .data(new FinremCaseData()).build();
    }

    private GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> aboutToStartOrSubmitCallbackResponse() {
        FinremCaseData caseData = new FinremCaseData();
        caseData.getWorkAllocationWrapper().setCaseManagementLocation(caseLocation());
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData)
            .build();
    }

    private CaseLocation caseLocation() {
        return CaseLocation.builder()
            .baseLocation(BASE_LOCATION)
            .region(REGION)
            .build();
    }

    private FinremCallbackRequest finremCallbackRequest(FinremCaseDetails caseDetails) {
        return FinremCallbackRequest
            .builder()
            .caseDetails(caseDetails)
            .eventType(GIVE_ALLOCATION_DIRECTIONS)
            .build();
    }
}