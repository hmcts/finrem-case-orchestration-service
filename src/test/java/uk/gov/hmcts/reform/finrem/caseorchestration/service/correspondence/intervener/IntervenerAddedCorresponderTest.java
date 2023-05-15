package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.intervener;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@RunWith(MockitoJUnitRunner.class)
public class IntervenerAddedCorresponderTest {
    @Autowired
    IntervenerAddedCorresponder intervenerAddedCorresponder;
    @Mock
    private IntervenerDocumentService intervenerDocumentService;
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
        intervenerAddedCorresponder =
            new IntervenerAddedCorresponder(notificationService, bulkPrintService, intervenerDocumentService, intervenerOneDetailsMapper,
                intervenerTwoDetailsMapper, intervenerThreeDetailsMapper, intervenerFourDetailsMapper);
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        finremCaseDetails = finremCallbackRequest.getCaseDetails();
        finremCaseDetailsBefore = finremCallbackRequest.getCaseDetailsBefore();
    }

    @Test
    public void shouldSendLetterCorrespondenceToIntvrAppAndRespIfIntvr1Added() {
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerType(IntervenerType.INTERVENER_ONE);
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.ADDED);
        IntervenerDetails intervenerDetails = IntervenerDetails.builder().intervenerRepresented(YesOrNo.NO).build();
        intervenerChangeDetails.setIntervenerDetails(intervenerDetails);
        finremCaseDetails.getData().getIntervenerOneWrapper().setIntervener1Name("John Smith");
        finremCaseDetailsBefore.getData().getIntervenerOneWrapper().setIntervener1Name(null);
        finremCaseDetails.getData().setCurrentIntervenerChangeDetails(intervenerChangeDetails);
        when(intervenerAddedCorresponder.shouldSendIntervenerOneSolicitorEmail(finremCaseDetails)).thenReturn(false);
        intervenerAddedCorresponder.sendCorrespondence(finremCaseDetails, finremCaseDetailsBefore, AUTHORISATION_TOKEN);
        verify(intervenerDocumentService, times(1)).generateIntervenerAddedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT);
        verify(intervenerDocumentService, times(1)).generateIntervenerAddedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT);
        verify(intervenerDocumentService, times(1)).generateIntervenerAddedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.INTERVENER_ONE);
        verify(intervenerDocumentService, times(0)).generateIntervenerSolicitorAddedLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT);
        verify(intervenerDocumentService, times(0)).generateIntervenerSolicitorAddedLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT);
    }

    @Test
    public void shouldSendLetterCorrespondenceToAppAndRespIfIntvr1SolicitorAdded() {
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerType(IntervenerType.INTERVENER_ONE);
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.ADDED);
        IntervenerDetails intervenerDetails = IntervenerDetails.builder().intervenerRepresented(YesOrNo.YES).build();
        intervenerChangeDetails.setIntervenerDetails(intervenerDetails);
        finremCaseDetails.getData().getIntervenerOneWrapper().setIntervener1Represented(YesOrNo.YES);
        finremCaseDetailsBefore.getData().getIntervenerOneWrapper().setIntervener1Represented(YesOrNo.NO);
        finremCaseDetails.getData().setCurrentIntervenerChangeDetails(intervenerChangeDetails);
        when(intervenerAddedCorresponder.shouldSendIntervenerOneSolicitorEmail(finremCaseDetails)).thenReturn(false);
        intervenerAddedCorresponder.sendCorrespondence(finremCaseDetails, finremCaseDetailsBefore, AUTHORISATION_TOKEN);
        verify(intervenerDocumentService, times(0)).generateIntervenerAddedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT);
        verify(intervenerDocumentService, times(0)).generateIntervenerAddedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT);
        verify(intervenerDocumentService, times(0)).generateIntervenerAddedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.INTERVENER_ONE);
        verify(intervenerDocumentService, times(1)).generateIntervenerSolicitorAddedLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT);
        verify(intervenerDocumentService, times(1)).generateIntervenerSolicitorAddedLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT);
    }

    @Test
    public void shouldSendLetterCorrespondenceToIntvrAppAndRespIfIntvr2Added() {
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerType(IntervenerType.INTERVENER_TWO);
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.ADDED);
        IntervenerDetails intervenerDetails = IntervenerDetails.builder().intervenerRepresented(YesOrNo.NO).build();
        intervenerChangeDetails.setIntervenerDetails(intervenerDetails);
        finremCaseDetails.getData().getIntervenerTwoWrapper().setIntervener2Name("John Smith");
        finremCaseDetailsBefore.getData().getIntervenerTwoWrapper().setIntervener2Name(null);
        finremCaseDetails.getData().setCurrentIntervenerChangeDetails(intervenerChangeDetails);
        when(intervenerAddedCorresponder.shouldSendIntervenerTwoSolicitorEmail(finremCaseDetails)).thenReturn(false);
        intervenerAddedCorresponder.sendCorrespondence(finremCaseDetails, finremCaseDetailsBefore, AUTHORISATION_TOKEN);
        verify(intervenerDocumentService, times(1)).generateIntervenerAddedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT);
        verify(intervenerDocumentService, times(1)).generateIntervenerAddedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT);
        verify(intervenerDocumentService, times(1)).generateIntervenerAddedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.INTERVENER_TWO);
        verify(intervenerDocumentService, times(0)).generateIntervenerSolicitorAddedLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT);
        verify(intervenerDocumentService, times(0)).generateIntervenerSolicitorAddedLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT);
    }

    @Test
    public void shouldSendLetterCorrespondenceToAppAndRespIfIntvr2SolicitorAdded() {
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerType(IntervenerType.INTERVENER_TWO);
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.ADDED);
        IntervenerDetails intervenerDetails = IntervenerDetails.builder().intervenerRepresented(YesOrNo.YES).build();
        intervenerChangeDetails.setIntervenerDetails(intervenerDetails);
        finremCaseDetails.getData().getIntervenerTwoWrapper().setIntervener2Represented(YesOrNo.YES);
        finremCaseDetailsBefore.getData().getIntervenerTwoWrapper().setIntervener2Represented(YesOrNo.NO);
        finremCaseDetails.getData().setCurrentIntervenerChangeDetails(intervenerChangeDetails);
        when(intervenerAddedCorresponder.shouldSendIntervenerTwoSolicitorEmail(finremCaseDetails)).thenReturn(false);
        intervenerAddedCorresponder.sendCorrespondence(finremCaseDetails, finremCaseDetailsBefore, AUTHORISATION_TOKEN);
        verify(intervenerDocumentService, times(0)).generateIntervenerAddedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT);
        verify(intervenerDocumentService, times(0)).generateIntervenerAddedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT);
        verify(intervenerDocumentService, times(0)).generateIntervenerAddedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.INTERVENER_TWO);
        verify(intervenerDocumentService, times(1)).generateIntervenerSolicitorAddedLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT);
        verify(intervenerDocumentService, times(1)).generateIntervenerSolicitorAddedLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT);
    }

    @Test
    public void shouldSendLetterCorrespondenceToIntvrAppAndRespIfIntvr3Added() {
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerType(IntervenerType.INTERVENER_THREE);
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.ADDED);
        IntervenerDetails intervenerDetails = IntervenerDetails.builder().intervenerRepresented(YesOrNo.NO).build();
        intervenerChangeDetails.setIntervenerDetails(intervenerDetails);
        finremCaseDetails.getData().getIntervenerThreeWrapper().setIntervener3Name("John Smith");
        finremCaseDetailsBefore.getData().getIntervenerThreeWrapper().setIntervener3Name(null);
        finremCaseDetails.getData().setCurrentIntervenerChangeDetails(intervenerChangeDetails);
        when(intervenerAddedCorresponder.shouldSendIntervenerThreeSolicitorEmail(finremCaseDetails)).thenReturn(false);
        intervenerAddedCorresponder.sendCorrespondence(finremCaseDetails, finremCaseDetailsBefore, AUTHORISATION_TOKEN);
        verify(intervenerDocumentService, times(1)).generateIntervenerAddedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT);
        verify(intervenerDocumentService, times(1)).generateIntervenerAddedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT);
        verify(intervenerDocumentService, times(1)).generateIntervenerAddedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.INTERVENER_THREE);
        verify(intervenerDocumentService, times(0)).generateIntervenerSolicitorAddedLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT);
        verify(intervenerDocumentService, times(0)).generateIntervenerSolicitorAddedLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT);
    }

    @Test
    public void shouldSendLetterCorrespondenceToAppAndRespIfIntvr3SolicitorAdded() {
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerType(IntervenerType.INTERVENER_THREE);
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.ADDED);
        IntervenerDetails intervenerDetails = IntervenerDetails.builder().intervenerRepresented(YesOrNo.YES).build();
        intervenerChangeDetails.setIntervenerDetails(intervenerDetails);
        finremCaseDetails.getData().getIntervenerThreeWrapper().setIntervener3Represented(YesOrNo.YES);
        finremCaseDetailsBefore.getData().getIntervenerThreeWrapper().setIntervener3Represented(YesOrNo.NO);
        finremCaseDetails.getData().setCurrentIntervenerChangeDetails(intervenerChangeDetails);
        when(intervenerAddedCorresponder.shouldSendIntervenerThreeSolicitorEmail(finremCaseDetails)).thenReturn(false);
        intervenerAddedCorresponder.sendCorrespondence(finremCaseDetails, finremCaseDetailsBefore, AUTHORISATION_TOKEN);
        verify(intervenerDocumentService, times(0)).generateIntervenerAddedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT);
        verify(intervenerDocumentService, times(0)).generateIntervenerAddedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT);
        verify(intervenerDocumentService, times(0)).generateIntervenerAddedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.INTERVENER_THREE);
        verify(intervenerDocumentService, times(1)).generateIntervenerSolicitorAddedLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT);
        verify(intervenerDocumentService, times(1)).generateIntervenerSolicitorAddedLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT);
    }

    @Test
    public void shouldSendLetterCorrespondenceToIntvrAppAndRespIfIntvr4Added() {
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerType(IntervenerType.INTERVENER_FOUR);
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.ADDED);
        IntervenerDetails intervenerDetails = IntervenerDetails.builder().intervenerRepresented(YesOrNo.NO).build();
        intervenerChangeDetails.setIntervenerDetails(intervenerDetails);
        finremCaseDetails.getData().getIntervenerFourWrapper().setIntervener4Name("John Smith");
        finremCaseDetailsBefore.getData().getIntervenerFourWrapper().setIntervener4Name(null);
        finremCaseDetails.getData().setCurrentIntervenerChangeDetails(intervenerChangeDetails);
        when(intervenerAddedCorresponder.shouldSendIntervenerFourSolicitorEmail(finremCaseDetails)).thenReturn(false);
        intervenerAddedCorresponder.sendCorrespondence(finremCaseDetails, finremCaseDetailsBefore, AUTHORISATION_TOKEN);
        verify(intervenerDocumentService, times(1)).generateIntervenerAddedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT);
        verify(intervenerDocumentService, times(1)).generateIntervenerAddedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT);
        verify(intervenerDocumentService, times(1)).generateIntervenerAddedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.INTERVENER_FOUR);
        verify(intervenerDocumentService, times(0)).generateIntervenerSolicitorAddedLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT);
        verify(intervenerDocumentService, times(0)).generateIntervenerSolicitorAddedLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT);
    }

    @Test
    public void shouldSendLetterCorrespondenceToAppAndRespIfIntvr4SolicitorAdded() {
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerType(IntervenerType.INTERVENER_FOUR);
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.ADDED);
        IntervenerDetails intervenerDetails = IntervenerDetails.builder().intervenerRepresented(YesOrNo.YES).build();
        intervenerChangeDetails.setIntervenerDetails(intervenerDetails);
        finremCaseDetails.getData().getIntervenerFourWrapper().setIntervener4Represented(YesOrNo.YES);
        finremCaseDetailsBefore.getData().getIntervenerFourWrapper().setIntervener4Represented(YesOrNo.NO);
        finremCaseDetails.getData().setCurrentIntervenerChangeDetails(intervenerChangeDetails);
        when(intervenerAddedCorresponder.shouldSendIntervenerFourSolicitorEmail(finremCaseDetails)).thenReturn(false);
        intervenerAddedCorresponder.sendCorrespondence(finremCaseDetails, finremCaseDetailsBefore, AUTHORISATION_TOKEN);
        verify(intervenerDocumentService, times(0)).generateIntervenerAddedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT);
        verify(intervenerDocumentService, times(0)).generateIntervenerAddedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT);
        verify(intervenerDocumentService, times(0)).generateIntervenerAddedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.INTERVENER_FOUR);
        verify(intervenerDocumentService, times(1)).generateIntervenerSolicitorAddedLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT);
        verify(intervenerDocumentService, times(1)).generateIntervenerSolicitorAddedLetter(finremCaseDetails, AUTHORISATION_TOKEN,
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
