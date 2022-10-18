package uk.gov.hmcts.reform.finrem.caseorchestration.handler.managebarrister;

import org.junit.Before;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.BarristerValidationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.ManageBarristerService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdServiceTest.AUTH_TOKEN;

@RunWith(MockitoJUnitRunner.class)
public class ManageBarristerMidEventHandlerTest {

    public static final String CASE_ID = "1234567890";

    @Mock
    private ManageBarristerService manageBarristerService;
    @Mock
    private BarristerValidationService barristerValidationService;
    @InjectMocks
    private ManageBarristerMidEventHandler manageBarristerMidEventHandler;

    private CallbackRequest callbackRequest;

    @Before
    public void setUp() {
        Map<String, Object> caseData = new HashMap<>();
        callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder().id(Long.parseLong(CASE_ID)).data(caseData).build())
            .build();
    }

    @Test
    public void givenHandlerCanHandleCallback_whenCanHandle_thenReturnTrue() {
        assertThat(manageBarristerMidEventHandler.canHandle(
            CallbackType.MID_EVENT,
            CaseType.CONTESTED,
            EventType.MANAGE_BARRISTER
        ), is(true));
    }

    @Test
    public void givenInvalidCallbackType_whenCanHandle_thenReturnFalse() {
        assertThat(manageBarristerMidEventHandler.canHandle(
            CallbackType.ABOUT_TO_SUBMIT,
            CaseType.CONTESTED,
            EventType.MANAGE_BARRISTER
        ), is(false));
    }

    @Test
    public void givenInvalidCaseType_whenCanHandle_thenReturnFalse() {
        assertThat(manageBarristerMidEventHandler.canHandle(
            CallbackType.MID_EVENT,
            CaseType.CONSENTED,
            EventType.MANAGE_BARRISTER
        ), is(false));
    }

    @Test
    public void givenInvalidEventType_whenCanHandle_thenReturnFalse() {
        assertThat(manageBarristerMidEventHandler.canHandle(
            CallbackType.MID_EVENT,
            CaseType.CONTESTED,
            EventType.SEND_ORDER
        ), is(false));
    }

    @Test
    public void givenBarristerEmailsAreValid_whenHandle_thenReturnResponseWithNoErrors() {
        when(manageBarristerService.getAuthTokenToUse(callbackRequest.getCaseDetails(), AUTH_TOKEN)).thenReturn(AUTH_TOKEN);
        when(manageBarristerService.getBarristersForParty(callbackRequest.getCaseDetails(), AUTH_TOKEN))
            .thenReturn(getBarristerData());
        when(manageBarristerService.getCaseRole(callbackRequest.getCaseDetails(), AUTH_TOKEN)).thenReturn(APP_SOLICITOR_POLICY);
        when(barristerValidationService.validateBarristerEmails(getBarristerData(), AUTH_TOKEN, CASE_ID, APP_SOLICITOR_POLICY))
            .thenReturn(new ArrayList<>());

        AboutToStartOrSubmitCallbackResponse response = manageBarristerMidEventHandler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response.getErrors(), is(nullValue()));
    }

    @Test
    public void givenBarristerEmailsAreInvalid_whenHandle_thenReturnResponseWithErrors() {
        when(manageBarristerService.getAuthTokenToUse(callbackRequest.getCaseDetails(), AUTH_TOKEN)).thenReturn(AUTH_TOKEN);
        when(manageBarristerService.getBarristersForParty(callbackRequest.getCaseDetails(), AUTH_TOKEN))
            .thenReturn(getBarristerData());
        when(manageBarristerService.getCaseRole(callbackRequest.getCaseDetails(), AUTH_TOKEN)).thenReturn(APP_SOLICITOR_POLICY);
        when(barristerValidationService.validateBarristerEmails(getBarristerData(), AUTH_TOKEN, CASE_ID, APP_SOLICITOR_POLICY))
            .thenReturn(List.of("Validation Error"));

        AboutToStartOrSubmitCallbackResponse response = manageBarristerMidEventHandler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response.getErrors(), hasSize(1));
    }

    private List<BarristerData> getBarristerData() {
        return List.of(
            BarristerData.builder()
                .barrister(Barrister.builder()
                    .name("Barrister One")
                    .email("barristerone@gmail.com")
                    .organisation(Organisation.builder().organisationID("A1765").build())
                    .build())
                .build()
        );
    }
}