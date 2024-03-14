package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.intervener;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
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
public class IntervenerAddedCorresponderTest {

    IntervenerAddedCorresponder intervenerAddedCorresponder;

    @Mock
    private IntervenerDocumentService intervenerDocumentService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private BulkPrintService bulkPrintService;

    private static final String AUTHORISATION_TOKEN = "authToken";
    private FinremCaseDetails finremCaseDetails;
    private FinremCaseData finremCaseData;
    private CaseDocument caseDocument;

    @Before
    public void setup() {
        intervenerAddedCorresponder = new IntervenerAddedCorresponder(notificationService, bulkPrintService,
            intervenerDocumentService);
        IntervenerOne intervenerOne = IntervenerOne.builder().build();
        IntervenerTwo intervenerTwo = IntervenerTwo.builder().build();
        IntervenerThree intervenerThree = IntervenerThree.builder().build();
        IntervenerFour intervenerFour = IntervenerFour.builder().build();
        finremCaseData = FinremCaseData.builder()
            .intervenerOne(intervenerOne)
            .intervenerTwo(intervenerTwo)
            .intervenerThree(intervenerThree)
            .intervenerFour(intervenerFour)
            .build();
        caseDocument = CaseDocument.builder().build();
    }

    @Test
    public void shouldAlwaysSendLetterCorrespondenceToAppAndRespIfNotRepresented() {
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerType(IntervenerType.INTERVENER_ONE);
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.ADDED);
        IntervenerOne intervenerDetails = IntervenerOne.builder()
            .intervenerRepresented(YesOrNo.NO)
            .build();
        intervenerChangeDetails.setIntervenerDetails(intervenerDetails);
        finremCaseData.setIntervenerOne(intervenerDetails);
        finremCaseData.setCurrentIntervenerChangeDetails(intervenerChangeDetails);
        finremCaseDetails = FinremCaseDetails.builder().data(finremCaseData).build();
        when(intervenerAddedCorresponder.shouldSendIntervenerSolicitorEmail(intervenerDetails)).thenReturn(false);
        intervenerAddedCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);

        verify(intervenerDocumentService, times(1))
            .generateIntervenerAddedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
                DocumentHelper.PaperNotificationRecipient.APPLICANT);
        verify(intervenerDocumentService, times(1))
            .generateIntervenerAddedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
                DocumentHelper.PaperNotificationRecipient.RESPONDENT);
    }

    @Test
    public void shouldSendLetterCorrespondenceIfIntervenerIsRepresentedToAppAndRespIfNotRepresented() {
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerType(IntervenerType.INTERVENER_ONE);
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.ADDED);
        IntervenerOne intervenerDetails = IntervenerOne.builder()
            .intervenerRepresented(YesOrNo.YES)
            .build();
        intervenerChangeDetails.setIntervenerDetails(intervenerDetails);
        finremCaseData.setIntervenerOne(intervenerDetails);
        finremCaseData.setCurrentIntervenerChangeDetails(intervenerChangeDetails);
        finremCaseDetails = FinremCaseDetails.builder().data(finremCaseData).build();
        intervenerAddedCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);

        verify(intervenerDocumentService, times(1))
            .generateIntervenerSolicitorAddedLetter(finremCaseDetails, AUTHORISATION_TOKEN,
                DocumentHelper.PaperNotificationRecipient.APPLICANT);
        verify(intervenerDocumentService, times(1))
            .generateIntervenerSolicitorAddedLetter(finremCaseDetails, AUTHORISATION_TOKEN,
                DocumentHelper.PaperNotificationRecipient.RESPONDENT);
    }

    @Test
    public void shouldSendIntervenerOneLetterCorrespondenceIfNotRepresented() {
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerType(IntervenerType.INTERVENER_ONE);
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.ADDED);
        IntervenerOne intervenerDetails = IntervenerOne.builder()
            .intervenerRepresented(YesOrNo.NO)
            .build();
        intervenerChangeDetails.setIntervenerDetails(intervenerDetails);
        finremCaseData.setIntervenerOne(intervenerDetails);
        finremCaseData.setCurrentIntervenerChangeDetails(intervenerChangeDetails);
        finremCaseDetails = FinremCaseDetails.builder().data(finremCaseData).build();
        when(intervenerAddedCorresponder.shouldSendIntervenerSolicitorEmail(intervenerDetails)).thenReturn(false);
        intervenerAddedCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);

        verify(intervenerDocumentService, times(1))
            .generateIntervenerAddedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
                DocumentHelper.PaperNotificationRecipient.INTERVENER_ONE);
    }

    @Test
    public void shouldSendIntervenerTwoLetterCorrespondenceIfNotRepresented() {
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerType(IntervenerType.INTERVENER_TWO);
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.ADDED);
        IntervenerTwo intervenerDetails = IntervenerTwo.builder()
            .intervenerRepresented(YesOrNo.NO)
            .build();
        finremCaseData.setIntervenerTwo(intervenerDetails);
        intervenerChangeDetails.setIntervenerDetails(intervenerDetails);
        finremCaseData.setCurrentIntervenerChangeDetails(intervenerChangeDetails);
        finremCaseDetails = FinremCaseDetails.builder().data(finremCaseData).build();
        when(intervenerAddedCorresponder.shouldSendIntervenerSolicitorEmail(intervenerDetails)).thenReturn(false);
        intervenerAddedCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);

        verify(intervenerDocumentService, times(1))
            .generateIntervenerAddedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
                DocumentHelper.PaperNotificationRecipient.INTERVENER_TWO);
    }

    @Test
    public void shouldSendIntervenerThreeLetterCorrespondenceIfNotRepresented() {
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerType(IntervenerType.INTERVENER_THREE);
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.ADDED);
        IntervenerThree intervenerDetails = IntervenerThree.builder()
            .intervenerRepresented(YesOrNo.NO)
            .build();
        intervenerChangeDetails.setIntervenerDetails(intervenerDetails);
        finremCaseData.setIntervenerThree(intervenerDetails);
        finremCaseData.setCurrentIntervenerChangeDetails(intervenerChangeDetails);
        finremCaseDetails = FinremCaseDetails.builder().data(finremCaseData).build();
        when(intervenerAddedCorresponder.shouldSendIntervenerSolicitorEmail(intervenerDetails)).thenReturn(false);
        intervenerAddedCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);

        verify(intervenerDocumentService, times(1))
            .generateIntervenerAddedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
                DocumentHelper.PaperNotificationRecipient.INTERVENER_THREE);
    }

    @Test
    public void shouldSendIntervenerFourLetterCorrespondenceIfNotRepresented() {
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerType(IntervenerType.INTERVENER_FOUR);
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.ADDED);
        IntervenerFour intervenerDetails = IntervenerFour.builder()
            .intervenerRepresented(YesOrNo.NO)
            .build();
        intervenerChangeDetails.setIntervenerDetails(intervenerDetails);
        finremCaseData.setIntervenerFour(intervenerDetails);
        finremCaseData.setCurrentIntervenerChangeDetails(intervenerChangeDetails);
        finremCaseDetails = FinremCaseDetails.builder().data(finremCaseData).build();
        when(intervenerAddedCorresponder.shouldSendIntervenerSolicitorEmail(intervenerDetails)).thenReturn(false);
        intervenerAddedCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);

        verify(intervenerDocumentService, times(1))
            .generateIntervenerAddedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
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
        when(intervenerAddedCorresponder.shouldSendIntervenerSolicitorEmail(intervenerDetails)).thenReturn(true);
        intervenerAddedCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);

        verify(notificationService).sendIntervenerSolicitorAddedEmail(eq(finremCaseDetails), eq(intervenerDetails),
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
        when(intervenerAddedCorresponder.shouldSendIntervenerSolicitorEmail(intervenerDetails)).thenReturn(true);
        intervenerAddedCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);

        verify(notificationService).sendIntervenerSolicitorAddedEmail(eq(finremCaseDetails), eq(intervenerDetails),
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
        when(intervenerAddedCorresponder.shouldSendIntervenerSolicitorEmail(intervenerDetails)).thenReturn(true);
        intervenerAddedCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);

        verify(notificationService).sendIntervenerSolicitorAddedEmail(eq(finremCaseDetails), eq(intervenerDetails),
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
        when(intervenerAddedCorresponder.shouldSendIntervenerSolicitorEmail(intervenerDetails)).thenReturn(true);
        intervenerAddedCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);

        verify(notificationService).sendIntervenerSolicitorAddedEmail(eq(finremCaseDetails), eq(intervenerDetails),
            anyString(), anyString(), anyString());
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
        intervenerAddedCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);

        verify(notificationService).sendIntervenerAddedEmail(eq(finremCaseDetails), eq(intervenerDetails),
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
        intervenerAddedCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);

        verify(notificationService).sendIntervenerSolicitorAddedEmail(eq(finremCaseDetails), eq(intervenerDetails),
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
        intervenerAddedCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);

        verify(notificationService).sendIntervenerAddedEmail(eq(finremCaseDetails), eq(intervenerDetails),
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
        intervenerAddedCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);

        verify(notificationService).sendIntervenerSolicitorAddedEmail(eq(finremCaseDetails), eq(intervenerDetails),
            anyString(), anyString(), anyString());
    }
}
