package uk.gov.hmcts.reform.finrem.caseorchestration.handler.managebarrister;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.ManageBarristerService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdServiceTest.AUTH_TOKEN;


@RunWith(MockitoJUnitRunner.class)
public class ManageBarristerSubmittedHandlerTest {

    public static final String CASE_ID = "1234567890";

    @Mock
    private ManageBarristerService manageBarristerService;

    @InjectMocks
    private ManageBarristerSubmittedHandler manageBarristerSubmittedHandler;

    private CallbackRequest callbackRequest;

    @Before
    public void setUp() {
        Map<String, Object> caseData = new HashMap<>();
        Map<String, Object> caseDataBefore = new HashMap<>();
        callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder().id(Long.parseLong(CASE_ID)).data(caseData).build())
            .caseDetailsBefore(CaseDetails.builder().id(Long.parseLong(CASE_ID)).data(caseDataBefore).build())
            .build();
    }

    @Test
    public void givenHandlerCanHandleCallback_whenCanHandle_thenReturnTrue() {
        assertThat(manageBarristerSubmittedHandler.canHandle(
            CallbackType.SUBMITTED,
            CaseType.CONTESTED,
            EventType.MANAGE_BARRISTER),
            is(true));
    }

    @Test
    public void givenInvalidCallbackType_whenCanHandle_thenReturnFalse() {
        assertThat(manageBarristerSubmittedHandler.canHandle(
                CallbackType.ABOUT_TO_SUBMIT,
                CaseType.CONTESTED,
                EventType.MANAGE_BARRISTER),
            is(false));
    }

    @Test
    public void givenInvalidCaseType_whenCanHandle_thenReturnFalse() {
        assertThat(manageBarristerSubmittedHandler.canHandle(
                CallbackType.SUBMITTED,
                CaseType.CONSENTED,
                EventType.MANAGE_BARRISTER),
            is(false));
    }

    @Test
    public void givenInvalidEventType_whenCanHandle_thenReturnFalse() {
        assertThat(manageBarristerSubmittedHandler.canHandle(
                CallbackType.SUBMITTED,
                CaseType.CONTESTED,
                EventType.UPLOAD_APPROVED_ORDER),
            is(false));
    }

    @Test
    public void givenValidData_whenHandle_thenReturnResponseAndCallNotify() {
        List<BarristerData> barristerCollection = getBarristers();
        when(manageBarristerService.getBarristersForParty(callbackRequest.getCaseDetails(), AUTH_TOKEN))
            .thenReturn(barristerCollection);
        when(manageBarristerService.getBarristersForParty(callbackRequest.getCaseDetailsBefore(), AUTH_TOKEN))
            .thenReturn(barristerCollection);
        List<Barrister> barristers = getBarristers().stream().map(BarristerData::getBarrister).collect(Collectors.toList());

        manageBarristerSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(manageBarristerService).notifyBarristerAccess(callbackRequest.getCaseDetails(), barristers, barristers, AUTH_TOKEN);
    }


    private List<BarristerData> getBarristers() {
        return List.of(
            BarristerData.builder()
                .barrister(Barrister.builder()
                    .name("Barrister Name")
                    .email("barrister@gmail.com")
                    .organisation(Organisation.builder().build())
                    .build())
                .build()
        );
    }
}