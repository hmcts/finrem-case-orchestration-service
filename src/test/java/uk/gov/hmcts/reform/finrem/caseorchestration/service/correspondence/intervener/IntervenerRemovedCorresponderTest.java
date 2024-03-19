package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.intervener;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFour;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOne;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThree;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerChangeDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IntervenerRemovedCorresponderTest {

    IntervenerRemovedCorresponder intervenerRemovedCorresponder;
    @Mock
    private IntervenerDocumentService intervenerDocumentService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private BulkPrintService bulkPrintService;
    private static final String AUTHORISATION_TOKEN = "authToken";
    private FinremCaseDetails finremCaseDetails;
    private FinremCaseData finremCaseData;

    @Before
    public void setup() {
        intervenerRemovedCorresponder = new IntervenerRemovedCorresponder(notificationService, bulkPrintService,
            intervenerDocumentService);
        finremCaseData = FinremCaseData.builder().build();
    }

    @Test
    public void shouldSendLetterCorrespondenceToAppAndRespIfNeitherAreRepresentedDigitallyAndIntervenerWasNotRepresented() {

        finremCaseDetails = getFinremCaseDetails(IntervenerType.INTERVENER_ONE);
        IntervenerOne wrapper = new IntervenerOne();
        finremCaseDetails.getData().getCurrentIntervenerChangeDetails().setIntervenerDetails(wrapper);
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(finremCaseDetails)).thenReturn(false);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(finremCaseDetails)).thenReturn(false);
        intervenerRemovedCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);

        verify(intervenerDocumentService, times(1))
            .generateIntervenerRemovedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
                DocumentHelper.PaperNotificationRecipient.APPLICANT);
        verify(intervenerDocumentService, times(1))
            .generateIntervenerRemovedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
                DocumentHelper.PaperNotificationRecipient.RESPONDENT);
    }

    @Test
    public void shouldSendLetterCorrespondenceToAppAndRespIfNeitherAreRepresentedDigitallyAndIntervenerWasRepresented() {

        finremCaseDetails = getFinremCaseDetails(IntervenerType.INTERVENER_ONE);
        IntervenerOne wrapper = new IntervenerOne();
        wrapper.setIntervenerRepresented(YesOrNo.YES);
        finremCaseDetails.getData().getCurrentIntervenerChangeDetails().setIntervenerDetails(wrapper);
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(finremCaseDetails)).thenReturn(false);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(finremCaseDetails)).thenReturn(false);

        intervenerRemovedCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);

        verify(intervenerDocumentService, times(1))
            .generateIntervenerSolicitorRemovedLetter(finremCaseDetails, AUTHORISATION_TOKEN,
                DocumentHelper.PaperNotificationRecipient.APPLICANT);
        verify(intervenerDocumentService, times(1))
            .generateIntervenerSolicitorRemovedLetter(finremCaseDetails, AUTHORISATION_TOKEN,
                DocumentHelper.PaperNotificationRecipient.RESPONDENT);
    }

    @Test
    public void shouldNotSendLetterCorrespondenceToAppAndRespIfBothAreRepresentedDigitally() {

        finremCaseDetails = getFinremCaseDetails(IntervenerType.INTERVENER_ONE);
        IntervenerOne wrapper = new IntervenerOne();
        finremCaseDetails.getData().getCurrentIntervenerChangeDetails().setIntervenerDetails(wrapper);
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(finremCaseDetails)).thenReturn(true);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(finremCaseDetails)).thenReturn(true);
        intervenerRemovedCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);

        verify(intervenerDocumentService, times(0))
            .generateIntervenerSolicitorRemovedLetter(finremCaseDetails, AUTHORISATION_TOKEN,
                DocumentHelper.PaperNotificationRecipient.APPLICANT);
        verify(intervenerDocumentService, times(0))
            .generateIntervenerSolicitorRemovedLetter(finremCaseDetails, AUTHORISATION_TOKEN,
                DocumentHelper.PaperNotificationRecipient.RESPONDENT);
        verify(intervenerDocumentService, times(0))
            .generateIntervenerRemovedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
                DocumentHelper.PaperNotificationRecipient.APPLICANT);
        verify(intervenerDocumentService, times(0))
            .generateIntervenerRemovedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
                DocumentHelper.PaperNotificationRecipient.RESPONDENT);
    }

    @Test
    public void shouldSendEmailIfApplicantIsRepresented() {
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerType(IntervenerType.INTERVENER_ONE);
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.ADDED);

        IntervenerOne intervenerDetails = IntervenerOne.builder()
            .intervenerName("intervener citizen")
            .intervenerRepresented(YesOrNo.NO)
            .build();
        intervenerChangeDetails.setIntervenerDetails(intervenerDetails);
        finremCaseData.setIntervenerOne(intervenerDetails);
        finremCaseData.setCurrentIntervenerChangeDetails(intervenerChangeDetails);

        ContactDetailsWrapper contactDetailsWrapper = ContactDetailsWrapper.builder()
            .solicitorReference("123456789").applicantSolicitorEmail("test@test.com").applicantSolicitorName("test name").build();
        finremCaseData.setContactDetailsWrapper(contactDetailsWrapper);
        finremCaseDetails = FinremCaseDetails.builder()
            .data(finremCaseData).build();
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(finremCaseDetails)).thenReturn(true);
        intervenerRemovedCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);

        verify(notificationService).sendIntervenerRemovedEmail(eq(finremCaseDetails), eq(intervenerDetails),
            anyString(), anyString(), anyString());
    }

    @Test
    public void shouldSendSolEmailIfApplicantIsRepresentedAndIntervenerIsRepresented() {
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerType(IntervenerType.INTERVENER_ONE);
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.ADDED);

        IntervenerOne intervenerDetails = IntervenerOne.builder()
            .intervenerSolName("intervener sol")
            .intervenerSolEmail("intervener@intervener.com")
            .intervenerSolicitorReference("123456789")
            .intervenerRepresented(YesOrNo.YES)
            .build();
        intervenerChangeDetails.setIntervenerDetails(intervenerDetails);
        finremCaseData.setIntervenerOne(intervenerDetails);
        finremCaseData.setCurrentIntervenerChangeDetails(intervenerChangeDetails);

        ContactDetailsWrapper contactDetailsWrapper = ContactDetailsWrapper.builder()
            .solicitorReference("123456789").applicantSolicitorEmail("test@test.com").applicantSolicitorName("test name").build();
        finremCaseData.setContactDetailsWrapper(contactDetailsWrapper);
        finremCaseDetails = FinremCaseDetails.builder()
            .data(finremCaseData).build();
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(finremCaseDetails)).thenReturn(true);
        intervenerRemovedCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);

        verify(notificationService).sendIntervenerSolicitorRemovedEmail(eq(finremCaseDetails), eq(intervenerDetails),
            anyString(), anyString(), anyString());
    }

    @Test
    public void shouldSendEmailIfRespondentIsRepresented() {
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerType(IntervenerType.INTERVENER_ONE);
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.ADDED);

        IntervenerOne intervenerDetails = IntervenerOne.builder()
            .intervenerName("intervener citizen")
            .intervenerRepresented(YesOrNo.NO)
            .build();
        intervenerChangeDetails.setIntervenerDetails(intervenerDetails);
        finremCaseData.setIntervenerOne(intervenerDetails);
        finremCaseData.setCurrentIntervenerChangeDetails(intervenerChangeDetails);

        ContactDetailsWrapper contactDetailsWrapper = ContactDetailsWrapper.builder()
            .respondentSolicitorReference("123456789").respondentSolicitorEmail("test@test.com").respondentSolicitorName("test name").build();
        finremCaseData.setContactDetailsWrapper(contactDetailsWrapper);
        finremCaseDetails = FinremCaseDetails.builder()
            .data(finremCaseData).build();
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(finremCaseDetails)).thenReturn(true);
        intervenerRemovedCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);

        verify(notificationService).sendIntervenerRemovedEmail(eq(finremCaseDetails), eq(intervenerDetails),
            anyString(), anyString(), anyString());
    }

    @Test
    public void shouldSendSolEmailIfRespondentIsRepresentedAndIntervenerIsRepresented() {
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerType(IntervenerType.INTERVENER_ONE);
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.ADDED);

        IntervenerOne intervenerDetails = IntervenerOne.builder()
            .intervenerSolName("intervener sol")
            .intervenerSolEmail("intervener@intervener.com")
            .intervenerSolicitorReference("123456789")
            .intervenerRepresented(YesOrNo.YES)
            .build();
        finremCaseData.setIntervenerOne(intervenerDetails);
        intervenerChangeDetails.setIntervenerDetails(intervenerDetails);
        finremCaseData.setCurrentIntervenerChangeDetails(intervenerChangeDetails);

        ContactDetailsWrapper contactDetailsWrapper = ContactDetailsWrapper.builder()
            .respondentSolicitorReference("123456789").respondentSolicitorEmail("test@test.com").respondentSolicitorName("test name").build();
        finremCaseData.setContactDetailsWrapper(contactDetailsWrapper);
        finremCaseDetails = FinremCaseDetails.builder()
            .data(finremCaseData).build();
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(finremCaseDetails)).thenReturn(true);
        intervenerRemovedCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);

        verify(notificationService).sendIntervenerSolicitorRemovedEmail(eq(finremCaseDetails), eq(intervenerDetails),
            anyString(), anyString(), anyString());
    }

    @Test
    public void shouldSendIntervenerOneLetterCorrespondenceIfWasNotRepresented() {
        finremCaseDetails = getFinremCaseDetails(IntervenerType.INTERVENER_ONE);
        IntervenerOne wrapper = new IntervenerOne();
        finremCaseDetails.getData().getCurrentIntervenerChangeDetails().setIntervenerDetails(wrapper);

        intervenerRemovedCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);

        verify(intervenerDocumentService, times(1))
            .generateIntervenerRemovedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
                DocumentHelper.PaperNotificationRecipient.INTERVENER_ONE);
    }

    @Test
    public void shouldSendIntervenerTwoLetterCorrespondenceIfWasNotRepresented() {
        finremCaseDetails = getFinremCaseDetails(IntervenerType.INTERVENER_TWO);
        IntervenerTwo wrapper = new IntervenerTwo();
        finremCaseDetails.getData().getCurrentIntervenerChangeDetails().setIntervenerDetails(wrapper);

        intervenerRemovedCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);

        verify(intervenerDocumentService, times(1))
            .generateIntervenerRemovedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
                DocumentHelper.PaperNotificationRecipient.INTERVENER_TWO);
    }

    @Test
    public void shouldSendIntervenerThreeLetterCorrespondenceIfWasNotRepresented() {
        finremCaseDetails = getFinremCaseDetails(IntervenerType.INTERVENER_THREE);
        IntervenerThree wrapper = new IntervenerThree();
        finremCaseDetails.getData().getCurrentIntervenerChangeDetails().setIntervenerDetails(wrapper);

        intervenerRemovedCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);

        verify(intervenerDocumentService, times(1))
            .generateIntervenerRemovedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
                DocumentHelper.PaperNotificationRecipient.INTERVENER_THREE);
    }

    @Test
    public void shouldSendIntervenerFourLetterCorrespondenceIfNotRepresented() {
        finremCaseDetails = getFinremCaseDetails(IntervenerType.INTERVENER_FOUR);
        IntervenerFour wrapper = new IntervenerFour();
        finremCaseDetails.getData().getCurrentIntervenerChangeDetails().setIntervenerDetails(wrapper);
        intervenerRemovedCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);

        verify(intervenerDocumentService, times(1))
            .generateIntervenerRemovedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
                DocumentHelper.PaperNotificationRecipient.INTERVENER_FOUR);
    }

    @Test
    public void shouldSendEmailIfIntervenerOneIsRepresented() {
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerType(IntervenerType.INTERVENER_ONE);
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.ADDED);
        IntervenerOne intervenerDetails = IntervenerOne.builder()
            .intervenerSolName("intervener sol")
            .intervenerSolEmail("intervener@intervener.com")
            .intervenerSolicitorReference("123456789")
            .intervenerRepresented(YesOrNo.YES)
            .build();
        intervenerChangeDetails.setIntervenerDetails(intervenerDetails);
        finremCaseData.setIntervenerOne(intervenerDetails);
        finremCaseData.setCurrentIntervenerChangeDetails(intervenerChangeDetails);
        finremCaseDetails = FinremCaseDetails.builder().data(finremCaseData).build();
        when(intervenerRemovedCorresponder.shouldSendIntervenerSolicitorEmail(intervenerDetails)).thenReturn(true);
        intervenerRemovedCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);

        verify(notificationService).sendIntervenerSolicitorRemovedEmail(eq(finremCaseDetails), eq(intervenerDetails),
            anyString(), anyString(), anyString());
    }

    @Test
    public void shouldSendEmailIfIntervenerTwoIsRepresented() {
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerType(IntervenerType.INTERVENER_TWO);
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.ADDED);

        IntervenerTwo intervenerDetails = IntervenerTwo.builder()
            .intervenerSolName("intervener sol")
            .intervenerSolEmail("intervener@intervener.com")
            .intervenerSolicitorReference("123456789")
            .intervenerRepresented(YesOrNo.YES)
            .build();

        intervenerChangeDetails.setIntervenerDetails(intervenerDetails);
        finremCaseData.setIntervenerTwo(intervenerDetails);
        finremCaseData.setCurrentIntervenerChangeDetails(intervenerChangeDetails);
        finremCaseDetails = FinremCaseDetails.builder()
            .data(finremCaseData).build();
        when(intervenerRemovedCorresponder.shouldSendIntervenerSolicitorEmail(intervenerDetails)).thenReturn(true);
        intervenerRemovedCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);

        verify(notificationService).sendIntervenerSolicitorRemovedEmail(eq(finremCaseDetails), eq(intervenerDetails),
            anyString(), anyString(), anyString());
    }

    @Test
    public void shouldSendEmailIfIntervenerThreeIsRepresented() {
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerType(IntervenerType.INTERVENER_THREE);
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.ADDED);
        IntervenerThree intervenerDetails = IntervenerThree.builder()
            .intervenerSolName("intervener sol")
            .intervenerSolEmail("intervener@intervener.com")
            .intervenerSolicitorReference("123456789")
            .intervenerRepresented(YesOrNo.YES)
            .build();
        intervenerChangeDetails.setIntervenerDetails(intervenerDetails);
        finremCaseData.setIntervenerThree(intervenerDetails);
        finremCaseData.setCurrentIntervenerChangeDetails(intervenerChangeDetails);
        finremCaseDetails = FinremCaseDetails.builder()
            .data(finremCaseData).build();
        when(intervenerRemovedCorresponder.shouldSendIntervenerSolicitorEmail(intervenerDetails)).thenReturn(true);
        intervenerRemovedCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);

        verify(notificationService).sendIntervenerSolicitorRemovedEmail(eq(finremCaseDetails), eq(intervenerDetails),
            anyString(), anyString(), anyString());
    }

    @Test
    public void shouldSendEmailIfIntervenerFourIsRepresented() {
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerType(IntervenerType.INTERVENER_FOUR);
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.ADDED);
        IntervenerFour intervenerDetails = IntervenerFour.builder()
            .intervenerSolName("intervener sol")
            .intervenerSolEmail("intervener@intervener.com")
            .intervenerSolicitorReference("123456789")
            .intervenerRepresented(YesOrNo.YES)
            .build();
        finremCaseData.setIntervenerFour(intervenerDetails);
        intervenerChangeDetails.setIntervenerDetails(intervenerDetails);
        finremCaseData.setCurrentIntervenerChangeDetails(intervenerChangeDetails);
        finremCaseDetails = FinremCaseDetails.builder()
            .data(finremCaseData).build();
        when(intervenerRemovedCorresponder.shouldSendIntervenerSolicitorEmail(intervenerDetails)).thenReturn(true);
        intervenerRemovedCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);

        verify(notificationService).sendIntervenerSolicitorRemovedEmail(eq(finremCaseDetails), eq(intervenerDetails),
            anyString(), anyString(), anyString());
    }

    private FinremCaseDetails getFinremCaseDetails(IntervenerType intervenerType) {
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerType(intervenerType);
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.REMOVED);
        finremCaseData.setCurrentIntervenerChangeDetails(intervenerChangeDetails);

        return FinremCaseDetails.builder().data(finremCaseData).build();
    }

}