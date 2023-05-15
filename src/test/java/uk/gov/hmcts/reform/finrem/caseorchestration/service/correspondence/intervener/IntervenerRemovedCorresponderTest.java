package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.intervener;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.intervener.IntervenerFourToIntervenerDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.intervener.IntervenerOneToIntervenerDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.intervener.IntervenerThreeToIntervenerDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.intervener.IntervenerTwoToIntervenerDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerChangeDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IntervenerService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@RunWith(MockitoJUnitRunner.class)
public class IntervenerRemovedCorresponderTest {

    IntervenerRemovedCorresponder intervenerRemovedCorresponder;
    @Mock
    private IntervenerDocumentService intervenerDocumentService;
    @Mock
    private IntervenerService intervenerService;
    @Mock
    private IntervenerOneToIntervenerDetailsMapper intervenerOneDetailsMapper;
    @Mock
    private IntervenerTwoToIntervenerDetailsMapper intervenerTwoDetailsMapper;
    @Mock
    private IntervenerThreeToIntervenerDetailsMapper intervenerThreeDetailsMapper;
    @Mock
    private IntervenerFourToIntervenerDetailsMapper intervenerFourDetailsMapper;
    @Mock
    private NotificationService notificationService;
    @Mock
    private BulkPrintService bulkPrintService;
    private static final String AUTHORISATION_TOKEN = "authToken";
    private FinremCaseDetails finremCaseDetails;
    private FinremCaseDetails finremCaseDetailsBefore;

    @Before
    public void setup() {
        intervenerRemovedCorresponder =
            new IntervenerRemovedCorresponder(notificationService, bulkPrintService, intervenerDocumentService, intervenerService,
                intervenerOneDetailsMapper, intervenerTwoDetailsMapper, intervenerThreeDetailsMapper, intervenerFourDetailsMapper);
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        finremCaseDetails = finremCallbackRequest.getCaseDetails();
        finremCaseDetailsBefore = finremCallbackRequest.getCaseDetailsBefore();
    }

    @Test
    public void shouldSendLetterCorrespondenceToIntvrAppAndRespIfIntvr1Removed() {
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerType(IntervenerType.INTERVENER_ONE);
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.REMOVED);
        IntervenerDetails intervenerDetails = IntervenerDetails.builder().build();
        intervenerChangeDetails.setIntervenerDetails(intervenerDetails);
        finremCaseDetailsBefore.getData().getIntervenerOneWrapper().setIntervener1Name("John Smith");
        finremCaseDetailsBefore.getData().getIntervenerOneWrapper().setIntervener1Represented(YesOrNo.NO);
        finremCaseDetails.getData().getIntervenerOneWrapper().setIntervener1Name(null);
        finremCaseDetails.getData().setCurrentIntervenerChangeDetails(intervenerChangeDetails);
        when(intervenerRemovedCorresponder.shouldSendIntervenerOneSolicitorEmail(finremCaseDetails)).thenReturn(false);
        intervenerRemovedCorresponder.sendCorrespondence(finremCaseDetails, finremCaseDetailsBefore, AUTHORISATION_TOKEN);
        verify(intervenerDocumentService, times(1)).generateIntervenerRemovedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT);
        verify(intervenerDocumentService, times(1)).generateIntervenerRemovedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT);
        verify(intervenerDocumentService, times(1)).generateIntervenerRemovedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.INTERVENER_ONE);
        verify(intervenerDocumentService, times(0)).generateIntervenerSolicitorRemovedLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT);
        verify(intervenerDocumentService, times(0)).generateIntervenerSolicitorRemovedLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT);
    }

    @Test
    public void shouldSendLetterCorrespondenceToAppAndRespIfIntvr1SolicitorRemoved() {
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerType(IntervenerType.INTERVENER_ONE);
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.REMOVED);
        IntervenerDetails intervenerDetails = IntervenerDetails.builder().intervenerRepresented(YesOrNo.NO).build();
        intervenerChangeDetails.setIntervenerDetails(intervenerDetails);
        finremCaseDetails.getData().getIntervenerOneWrapper().setIntervener1Represented(YesOrNo.NO);
        finremCaseDetailsBefore.getData().getIntervenerOneWrapper().setIntervener1Represented(YesOrNo.YES);
        finremCaseDetails.getData().setCurrentIntervenerChangeDetails(intervenerChangeDetails);
        when(intervenerRemovedCorresponder.shouldSendIntervenerOneSolicitorEmail(finremCaseDetails)).thenReturn(false);
        when(intervenerService.checkIfAnyIntervenerSolicitorRemoved(any(), any())).thenReturn(true);
        intervenerRemovedCorresponder.sendCorrespondence(finremCaseDetails, finremCaseDetailsBefore, AUTHORISATION_TOKEN);
        verify(intervenerDocumentService, times(0)).generateIntervenerRemovedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT);
        verify(intervenerDocumentService, times(0)).generateIntervenerRemovedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT);
        verify(intervenerDocumentService, times(0)).generateIntervenerRemovedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.INTERVENER_ONE);
        verify(intervenerDocumentService, times(1)).generateIntervenerSolicitorRemovedLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT);
        verify(intervenerDocumentService, times(1)).generateIntervenerSolicitorRemovedLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT);
    }

    @Test
    public void shouldSendLetterCorrespondenceToIntvrAppAndRespIfIntvr2Removed() {
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerType(IntervenerType.INTERVENER_TWO);
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.REMOVED);
        IntervenerDetails intervenerDetails = IntervenerDetails.builder().build();
        intervenerChangeDetails.setIntervenerDetails(intervenerDetails);
        finremCaseDetailsBefore.getData().getIntervenerTwoWrapper().setIntervener2Name("John Smith");
        finremCaseDetailsBefore.getData().getIntervenerTwoWrapper().setIntervener2Represented(YesOrNo.NO);
        finremCaseDetails.getData().getIntervenerTwoWrapper().setIntervener2Name(null);
        finremCaseDetails.getData().setCurrentIntervenerChangeDetails(intervenerChangeDetails);
        when(intervenerRemovedCorresponder.shouldSendIntervenerTwoSolicitorEmail(finremCaseDetails)).thenReturn(false);
        intervenerRemovedCorresponder.sendCorrespondence(finremCaseDetails, finremCaseDetailsBefore, AUTHORISATION_TOKEN);
        verify(intervenerDocumentService, times(1)).generateIntervenerRemovedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT);
        verify(intervenerDocumentService, times(1)).generateIntervenerRemovedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT);
        verify(intervenerDocumentService, times(1)).generateIntervenerRemovedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.INTERVENER_TWO);
        verify(intervenerDocumentService, times(0)).generateIntervenerSolicitorRemovedLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT);
        verify(intervenerDocumentService, times(0)).generateIntervenerSolicitorRemovedLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT);
    }

    @Test
    public void shouldSendLetterCorrespondenceToAppAndRespIfIntvr2SolicitorRemoved() {
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerType(IntervenerType.INTERVENER_TWO);
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.REMOVED);
        IntervenerDetails intervenerDetails = IntervenerDetails.builder().intervenerRepresented(YesOrNo.NO).build();
        intervenerChangeDetails.setIntervenerDetails(intervenerDetails);
        finremCaseDetails.getData().getIntervenerTwoWrapper().setIntervener2Represented(YesOrNo.NO);
        finremCaseDetailsBefore.getData().getIntervenerTwoWrapper().setIntervener2Represented(YesOrNo.YES);
        finremCaseDetails.getData().setCurrentIntervenerChangeDetails(intervenerChangeDetails);
        when(intervenerRemovedCorresponder.shouldSendIntervenerTwoSolicitorEmail(finremCaseDetails)).thenReturn(false);
        when(intervenerService.checkIfAnyIntervenerSolicitorRemoved(any(), any())).thenReturn(true);
        intervenerRemovedCorresponder.sendCorrespondence(finremCaseDetails, finremCaseDetailsBefore, AUTHORISATION_TOKEN);
        verify(intervenerDocumentService, times(0)).generateIntervenerRemovedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT);
        verify(intervenerDocumentService, times(0)).generateIntervenerRemovedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT);
        verify(intervenerDocumentService, times(0)).generateIntervenerRemovedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.INTERVENER_FOUR);
        verify(intervenerDocumentService, times(1)).generateIntervenerSolicitorRemovedLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT);
        verify(intervenerDocumentService, times(1)).generateIntervenerSolicitorRemovedLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT);
    }

    @Test
    public void shouldSendLetterCorrespondenceToIntvrAppAndRespIfIntvr3Removed() {
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerType(IntervenerType.INTERVENER_THREE);
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.REMOVED);
        IntervenerDetails intervenerDetails = IntervenerDetails.builder().build();
        intervenerChangeDetails.setIntervenerDetails(intervenerDetails);
        finremCaseDetailsBefore.getData().getIntervenerThreeWrapper().setIntervener3Name("John Smith");
        finremCaseDetailsBefore.getData().getIntervenerThreeWrapper().setIntervener3Represented(YesOrNo.NO);
        finremCaseDetails.getData().getIntervenerThreeWrapper().setIntervener3Name(null);
        finremCaseDetails.getData().setCurrentIntervenerChangeDetails(intervenerChangeDetails);
        when(intervenerRemovedCorresponder.shouldSendIntervenerThreeSolicitorEmail(finremCaseDetails)).thenReturn(false);
        intervenerRemovedCorresponder.sendCorrespondence(finremCaseDetails, finremCaseDetailsBefore, AUTHORISATION_TOKEN);
        verify(intervenerDocumentService, times(1)).generateIntervenerRemovedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT);
        verify(intervenerDocumentService, times(1)).generateIntervenerRemovedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT);
        verify(intervenerDocumentService, times(1)).generateIntervenerRemovedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.INTERVENER_THREE);
        verify(intervenerDocumentService, times(0)).generateIntervenerSolicitorRemovedLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT);
        verify(intervenerDocumentService, times(0)).generateIntervenerSolicitorRemovedLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT);
    }

    @Test
    public void shouldSendLetterCorrespondenceToAppAndRespIfIntvr3SolicitorRemoved() {
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerType(IntervenerType.INTERVENER_THREE);
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.REMOVED);
        IntervenerDetails intervenerDetails = IntervenerDetails.builder().intervenerRepresented(YesOrNo.NO).build();
        intervenerChangeDetails.setIntervenerDetails(intervenerDetails);
        finremCaseDetails.getData().getIntervenerThreeWrapper().setIntervener3Represented(YesOrNo.NO);
        finremCaseDetailsBefore.getData().getIntervenerThreeWrapper().setIntervener3Represented(YesOrNo.YES);
        finremCaseDetails.getData().setCurrentIntervenerChangeDetails(intervenerChangeDetails);
        when(intervenerRemovedCorresponder.shouldSendIntervenerFourSolicitorEmail(finremCaseDetails)).thenReturn(false);
        when(intervenerService.checkIfAnyIntervenerSolicitorRemoved(any(), any())).thenReturn(true);
        intervenerRemovedCorresponder.sendCorrespondence(finremCaseDetails, finremCaseDetailsBefore, AUTHORISATION_TOKEN);
        verify(intervenerDocumentService, times(0)).generateIntervenerRemovedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT);
        verify(intervenerDocumentService, times(0)).generateIntervenerRemovedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT);
        verify(intervenerDocumentService, times(0)).generateIntervenerRemovedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.INTERVENER_FOUR);
        verify(intervenerDocumentService, times(1)).generateIntervenerSolicitorRemovedLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT);
        verify(intervenerDocumentService, times(1)).generateIntervenerSolicitorRemovedLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT);
    }

    @Test
    public void shouldSendLetterCorrespondenceToIntvrAppAndRespIfIntvr4Removed() {
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerType(IntervenerType.INTERVENER_FOUR);
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.REMOVED);
        IntervenerDetails intervenerDetails = IntervenerDetails.builder().build();
        intervenerChangeDetails.setIntervenerDetails(intervenerDetails);
        finremCaseDetailsBefore.getData().getIntervenerFourWrapper().setIntervener4Name("John Smith");
        finremCaseDetailsBefore.getData().getIntervenerFourWrapper().setIntervener4Represented(YesOrNo.NO);
        finremCaseDetails.getData().getIntervenerFourWrapper().setIntervener4Name(null);
        finremCaseDetails.getData().setCurrentIntervenerChangeDetails(intervenerChangeDetails);
        when(intervenerRemovedCorresponder.shouldSendIntervenerFourSolicitorEmail(finremCaseDetails)).thenReturn(false);
        intervenerRemovedCorresponder.sendCorrespondence(finremCaseDetails, finremCaseDetailsBefore, AUTHORISATION_TOKEN);
        verify(intervenerDocumentService, times(1)).generateIntervenerRemovedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT);
        verify(intervenerDocumentService, times(1)).generateIntervenerRemovedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT);
        verify(intervenerDocumentService, times(1)).generateIntervenerRemovedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.INTERVENER_FOUR);
        verify(intervenerDocumentService, times(0)).generateIntervenerSolicitorRemovedLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT);
        verify(intervenerDocumentService, times(0)).generateIntervenerSolicitorRemovedLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT);
    }

    @Test
    public void shouldSendLetterCorrespondenceToAppAndRespIfIntvr4SolicitorRemoved() {
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerType(IntervenerType.INTERVENER_FOUR);
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.REMOVED);
        IntervenerDetails intervenerDetails = IntervenerDetails.builder().intervenerRepresented(YesOrNo.NO).build();
        intervenerChangeDetails.setIntervenerDetails(intervenerDetails);
        finremCaseDetails.getData().getIntervenerFourWrapper().setIntervener4Represented(YesOrNo.NO);
        finremCaseDetailsBefore.getData().getIntervenerFourWrapper().setIntervener4Represented(YesOrNo.YES);
        finremCaseDetails.getData().setCurrentIntervenerChangeDetails(intervenerChangeDetails);
        when(intervenerRemovedCorresponder.shouldSendIntervenerFourSolicitorEmail(finremCaseDetails)).thenReturn(false);
        when(intervenerService.checkIfAnyIntervenerSolicitorRemoved(any(), any())).thenReturn(true);
        intervenerRemovedCorresponder.sendCorrespondence(finremCaseDetails, finremCaseDetailsBefore, AUTHORISATION_TOKEN);
        verify(intervenerDocumentService, times(0)).generateIntervenerRemovedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT);
        verify(intervenerDocumentService, times(0)).generateIntervenerRemovedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT);
        verify(intervenerDocumentService, times(0)).generateIntervenerRemovedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.INTERVENER_FOUR);
        verify(intervenerDocumentService, times(1)).generateIntervenerSolicitorRemovedLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT);
        verify(intervenerDocumentService, times(1)).generateIntervenerSolicitorRemovedLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT);
    }

    private FinremCallbackRequest buildCallbackRequest() {
        return FinremCallbackRequest
            .builder()
            .eventType(EventType.MANAGE_INTERVENERS)
            .caseDetailsBefore(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(new FinremCaseData()).build())
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(new FinremCaseData()).build())
            .build();
    }
}
