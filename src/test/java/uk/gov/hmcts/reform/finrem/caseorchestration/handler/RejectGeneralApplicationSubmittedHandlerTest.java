package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.GeneralApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationItems;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PaperNotificationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_COLLECTION;

@RunWith(MockitoJUnitRunner.class)
public class RejectGeneralApplicationSubmittedHandlerTest {

    public static final String APPLICANT = "applicant";
    public static final String RESPONDENT = "respondent";
    public static final String CASE = "case";
    public static final String TEST_ID = "12345";

    @InjectMocks
    private RejectGeneralApplicationSubmittedHandler submittedHandler;

    @Mock
    private NotificationService notificationService;

    @Mock
    private PaperNotificationService paperNotificationService;

    @Mock
    private GeneralApplicationHelper generalApplicationHelper;

    private CallbackRequest callbackRequest;
    private CaseDetails caseDetails;



    @Before
    public void setup() {
        callbackRequest = CallbackRequest.builder().build();
        caseDetails = CaseDetails.builder().data(new HashMap<>()).build();
        callbackRequest.setCaseDetails(caseDetails);

        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

        submittedHandler = new RejectGeneralApplicationSubmittedHandler(notificationService,
            paperNotificationService,
            objectMapper,
            generalApplicationHelper);
    }

    @Test
    public void givenValidCallBack_whenCanHandle_thenReturnTrue() {
        assertTrue(submittedHandler.canHandle(CallbackType.SUBMITTED,
            CaseType.CONTESTED,
            EventType.REJECT_GENERAL_APPLICATION));
    }

    @Test
    public void givenInvalidCallBack_whenCanHandle_thenReturnFalse() {
        assertFalse(submittedHandler.canHandle(CallbackType.ABOUT_TO_SUBMIT,
            CaseType.CONTESTED,
            EventType.REJECT_GENERAL_APPLICATION));
    }

    @Test
    public void givenApplicantSolicitorDigital_whenHandle_thenSendEmailToAppSolicitor() {
        callbackRequest.setCaseDetailsBefore(caseDetailsBefore(APPLICANT));
        when(generalApplicationHelper.objectToDynamicList(any())).thenReturn(generalApplicationDynamicList());
        when(notificationService.isApplicantSolicitorRegisteredAndEmailCommunicationEnabled(caseDetails))
            .thenReturn(true);
        submittedHandler.handle(callbackRequest, AUTH_TOKEN);
        verify(notificationService).sendGeneralApplicationRejectionEmailToAppSolicitor(caseDetails);
    }

    @Test
    public void givenRespondentSolicitorDigital_whenHandle_thenSendEmailToResSolicitor() {
        callbackRequest.setCaseDetailsBefore(caseDetailsBefore(RESPONDENT));
        when(generalApplicationHelper.objectToDynamicList(any())).thenReturn(generalApplicationDynamicList());
        when(notificationService.isRespondentSolicitorRegisteredAndEmailCommunicationEnabled(caseDetails))
            .thenReturn(true);
        submittedHandler.handle(callbackRequest, AUTH_TOKEN);
        verify(notificationService).sendGeneralApplicationRejectionEmailToResSolicitor(caseDetails);
    }

    @Test
    public void givenApplicantSolicitorNotDigital_whenHandle_thenSendLetterToAppSolicitor() {
        callbackRequest.setCaseDetailsBefore(caseDetailsBefore(APPLICANT));
        when(generalApplicationHelper.objectToDynamicList(any())).thenReturn(generalApplicationDynamicList());
        when(notificationService.isApplicantSolicitorRegisteredAndEmailCommunicationEnabled(caseDetails))
            .thenReturn(false);
        submittedHandler.handle(callbackRequest, AUTH_TOKEN);
        verify(paperNotificationService).printApplicantRejectionGeneralApplication(caseDetails, AUTH_TOKEN);
    }

    @Test
    public void givenRespondentSolicitorNotDigital_whenHandle_thenSendLetterToResSolicitor() {
        callbackRequest.setCaseDetailsBefore(caseDetailsBefore(RESPONDENT));
        when(generalApplicationHelper.objectToDynamicList(any())).thenReturn(generalApplicationDynamicList());
        when(notificationService.isRespondentSolicitorRegisteredAndEmailCommunicationEnabled(caseDetails))
            .thenReturn(false);
        submittedHandler.handle(callbackRequest, AUTH_TOKEN);
        verify(paperNotificationService).printRespondentRejectionGeneralApplication(caseDetails, AUTH_TOKEN);
    }

    @Test
    public void givenReceivedFromCase_whenHandle_thenDoNotSendNotifications() {
        callbackRequest.setCaseDetailsBefore(caseDetailsBefore(CASE));
        when(generalApplicationHelper.objectToDynamicList(any())).thenReturn(generalApplicationDynamicList());

        submittedHandler.handle(callbackRequest, AUTH_TOKEN);
        verify(paperNotificationService, never()).printApplicantRejectionGeneralApplication(caseDetails, AUTH_TOKEN);
        verify(paperNotificationService, never()).printRespondentRejectionGeneralApplication(caseDetails, AUTH_TOKEN);

    }

    private DynamicList generalApplicationDynamicList() {
        return DynamicList.builder()
            .value(DynamicListElement.builder().code(TEST_ID).build())
            .build();
    }

    private CaseDetails caseDetailsBefore(String receivedFrom) {
        Map<String, Object> caseData = new HashMap<>();
        List<GeneralApplicationCollectionData> rejectedGeneralApplicationData = List.of(
            GeneralApplicationCollectionData.builder()
                .id(TEST_ID)
                .generalApplicationItems(GeneralApplicationItems.builder()
                    .generalApplicationReceivedFrom(receivedFrom)
                    .build())
                .build()
        );
        caseData.put(GENERAL_APPLICATION_COLLECTION, rejectedGeneralApplicationData);
        return CaseDetails.builder().data(caseData).build();
    }
}
