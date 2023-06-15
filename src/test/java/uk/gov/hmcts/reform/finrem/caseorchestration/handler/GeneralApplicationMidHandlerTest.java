package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.GeneralApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationItems;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralApplicationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@RunWith(MockitoJUnitRunner.class)
public class GeneralApplicationMidHandlerTest extends BaseHandlerTest {

    private GeneralApplicationMidHandler handler;
    @Mock
    private GenericDocumentService service;
    @Mock
    private GeneralApplicationService gaService;

    public static final String AUTH_TOKEN = "tokien:)";


    @Before
    public void setup() {
        FinremCaseDetailsMapper finremCaseDetailsMapper = new FinremCaseDetailsMapper(new ObjectMapper().registerModule(new JavaTimeModule()));
        ObjectMapper objectMapper = new ObjectMapper();
        GeneralApplicationHelper helper = new GeneralApplicationHelper(objectMapper, service);
        handler = new GeneralApplicationMidHandler(finremCaseDetailsMapper, helper, gaService);
    }

    @Test
    public void canHandle() {
        assertThat(handler
                .canHandle(CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.GENERAL_APPLICATION),
            is(true));
    }

    @Test
    public void canNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.MID_EVENT, CaseType.CONSENTED, EventType.GENERAL_APPLICATION),
            is(false));
    }

    @Test
    public void canNotHandleWrongEventType() {
        assertThat(handler
                .canHandle(CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.CLOSE),
            is(false));
    }

    @Test
    public void canNotHandleWrongCallbackType() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.GENERAL_APPLICATION),
            is(false));
    }

    @Test
    public void givenContestedCase_whenGeneralApplicationEventStartButNotAddedDetails_thenThrowErrorMessage() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        when(gaService.getActiveUser(finremCallbackRequest.getCaseDetails().getId().toString(), AUTH_TOKEN)).thenReturn("Case");
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(finremCallbackRequest, AUTH_TOKEN);
        assertTrue(handle.getErrors().get(0)
            .contains("Please complete the General Application. No information has been entered for this application."));
    }

    @Test
    public void givenContestedCase_whenGeneralApplicationEventStartAndThereIsExistingApplicationButNotAddedNewApplication_thenThrowErrorMessage() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequestWithCaseDetailsBefore();
        when(gaService.getActiveUser(finremCallbackRequest.getCaseDetails().getId().toString(), AUTH_TOKEN)).thenReturn("Case");
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertTrue(response.getErrors().get(0)
            .contains("Any changes to an existing General Applications will not be saved."
                + " Please add a new General Application in order to progress."));
    }


    private FinremCallbackRequest buildCallbackRequest() {
        return FinremCallbackRequest
            .builder()
            .eventType(EventType.GENERAL_APPLICATION)
            .caseDetailsBefore(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(new FinremCaseData()).build())
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(new FinremCaseData()).build())
            .build();
    }


    private FinremCallbackRequest buildCallbackRequestWithCaseDetailsBefore() {
        GeneralApplicationsCollection record1 = GeneralApplicationsCollection.builder().id(UUID.randomUUID())
            .value(GeneralApplicationItems.builder().generalApplicationCreatedBy("Test1").build()).build();
        GeneralApplicationsCollection record2 = GeneralApplicationsCollection.builder().id(UUID.randomUUID())
            .value(GeneralApplicationItems.builder().generalApplicationCreatedBy("Test2").build()).build();

        GeneralApplicationWrapper wrapper1 = GeneralApplicationWrapper.builder().generalApplications(List.of(record1, record2)).build();

        return FinremCallbackRequest
            .builder()
            .eventType(EventType.GENERAL_APPLICATION)
            .caseDetailsBefore(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(FinremCaseData.builder().generalApplicationWrapper(wrapper1).build()).build())
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(FinremCaseData.builder().generalApplicationWrapper(wrapper1).build()).build())
            .build();
    }
}