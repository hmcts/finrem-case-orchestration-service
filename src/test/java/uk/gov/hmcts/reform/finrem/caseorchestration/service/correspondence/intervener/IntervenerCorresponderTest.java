package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.intervener;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDataContested;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFourWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOneWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThreeWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwoWrapper;
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
public class IntervenerCorresponderTest {
    @Mock
    private IntervenerCorresponder intervenerCorresponder;
    @Mock
    private IntervenerDocumentService intervenerDocumentService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private BulkPrintService bulkPrintService;
    private static final String AUTHORISATION_TOKEN = "authToken";
    private FinremCaseDetails<FinremCaseDataContested> finremCaseDetails;
    private FinremCaseDataContested finremCaseData;

    @Before
    public void setup() {
        intervenerCorresponder = new IntervenerCorresponder(notificationService, bulkPrintService,
            intervenerDocumentService);
        IntervenerOneWrapper intervenerOneWrapper = IntervenerOneWrapper.builder().build();
        IntervenerTwoWrapper intervenerTwoWrapper = IntervenerTwoWrapper.builder().build();
        IntervenerThreeWrapper intervenerThreeWrapper = IntervenerThreeWrapper.builder().build();
        IntervenerFourWrapper intervenerFourWrapper = IntervenerFourWrapper.builder().build();
        finremCaseData = FinremCaseDataContested.builder()
            .intervenerOneWrapper(intervenerOneWrapper)
            .intervenerTwoWrapper(intervenerTwoWrapper)
            .intervenerThreeWrapper(intervenerThreeWrapper)
            .intervenerFourWrapper(intervenerFourWrapper)
            .build();
    }

    @Test
    public void shouldSendIntervenerOneLetterCorrespondenceIfNotRepresented() {
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerType(IntervenerType.INTERVENER_ONE);
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.REMOVED);
        IntervenerOneWrapper intervenerDetails = IntervenerOneWrapper.builder()
            .intervenerRepresented(YesOrNo.NO)
            .build();
        intervenerChangeDetails.setIntervenerDetails(intervenerDetails);
        finremCaseData.setIntervenerOneWrapper(intervenerDetails);
        finremCaseData.setCurrentIntervenerChangeDetails(intervenerChangeDetails);
        finremCaseDetails = FinremCaseDetails.<FinremCaseDataContested>builder().data(finremCaseData).build();
        when(intervenerCorresponder.shouldSendIntervenerSolicitorEmail(intervenerDetails)).thenReturn(false);
        intervenerCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);

        verify(intervenerDocumentService, times(1))
            .generateIntervenerRemovedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
                DocumentHelper.PaperNotificationRecipient.INTERVENER_ONE);
    }

    @Test
    public void shouldSendIntervenerTwoLetterCorrespondenceIfNotRepresented() {
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerType(IntervenerType.INTERVENER_TWO);
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.REMOVED);
        IntervenerTwoWrapper intervenerDetails = IntervenerTwoWrapper.builder()
            .intervenerRepresented(YesOrNo.NO)
            .build();
        finremCaseData.setIntervenerTwoWrapper(intervenerDetails);
        intervenerChangeDetails.setIntervenerDetails(intervenerDetails);
        finremCaseData.setCurrentIntervenerChangeDetails(intervenerChangeDetails);
        finremCaseDetails = FinremCaseDetails.<FinremCaseDataContested>builder().data(finremCaseData).build();
        when(intervenerCorresponder.shouldSendIntervenerSolicitorEmail(intervenerDetails)).thenReturn(false);
        intervenerCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);

        verify(intervenerDocumentService, times(1))
            .generateIntervenerRemovedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
                DocumentHelper.PaperNotificationRecipient.INTERVENER_TWO);
    }

    @Test
    public void shouldSendIntervenerThreeLetterCorrespondenceIfNotRepresented() {
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerType(IntervenerType.INTERVENER_THREE);
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.REMOVED);
        IntervenerThreeWrapper intervenerDetails = IntervenerThreeWrapper.builder()
            .intervenerRepresented(YesOrNo.NO)
            .build();
        intervenerChangeDetails.setIntervenerDetails(intervenerDetails);
        finremCaseData.setIntervenerThreeWrapper(intervenerDetails);
        finremCaseData.setCurrentIntervenerChangeDetails(intervenerChangeDetails);
        finremCaseDetails = FinremCaseDetails.<FinremCaseDataContested>builder().data(finremCaseData).build();
        when(intervenerCorresponder.shouldSendIntervenerSolicitorEmail(intervenerDetails)).thenReturn(false);
        intervenerCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);

        verify(intervenerDocumentService, times(1))
            .generateIntervenerRemovedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
                DocumentHelper.PaperNotificationRecipient.INTERVENER_THREE);
    }

    @Test
    public void shouldSendIntervenerFourLetterCorrespondenceIfNotRepresented() {
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerType(IntervenerType.INTERVENER_FOUR);
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.REMOVED);
        IntervenerFourWrapper intervenerDetails = IntervenerFourWrapper.builder()
            .intervenerRepresented(YesOrNo.NO)
            .build();
        intervenerChangeDetails.setIntervenerDetails(intervenerDetails);
        finremCaseData.setIntervenerFourWrapper(intervenerDetails);
        finremCaseData.setCurrentIntervenerChangeDetails(intervenerChangeDetails);
        finremCaseDetails = FinremCaseDetails.<FinremCaseDataContested>builder().data(finremCaseData).build();
        when(intervenerCorresponder.shouldSendIntervenerSolicitorEmail(intervenerDetails)).thenReturn(false);
        intervenerCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);

        verify(intervenerDocumentService, times(1))
            .generateIntervenerRemovedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
                DocumentHelper.PaperNotificationRecipient.INTERVENER_FOUR);
    }

    @Test
    public void shouldSendEmailIfIntervenerOneIsRepresented() {
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerType(IntervenerType.INTERVENER_ONE);
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.ADDED);
        IntervenerOneWrapper intervenerDetails = IntervenerOneWrapper.builder()
            .intervenerSolName("intervener sol")
            .intervenerSolEmail("intervener@intervener.com")
            .intervenerSolicitorReference("123456789")
            .intervenerRepresented(YesOrNo.YES)
            .build();
        intervenerChangeDetails.setIntervenerDetails(intervenerDetails);
        finremCaseData.setIntervenerOneWrapper(intervenerDetails);
        finremCaseData.setCurrentIntervenerChangeDetails(intervenerChangeDetails);
        finremCaseDetails = FinremCaseDetails.<FinremCaseDataContested>builder().data(finremCaseData).build();
        when(intervenerCorresponder.shouldSendIntervenerSolicitorEmail(intervenerDetails)).thenReturn(true);
        intervenerCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);

        verify(notificationService).sendIntervenerSolicitorAddedEmail(eq(finremCaseDetails), eq(intervenerDetails),
            anyString(), anyString(), anyString());
    }

    @Test
    public void shouldSendEmailIfIntervenerTwoIsRepresented() {
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerType(IntervenerType.INTERVENER_TWO);
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.ADDED);

        IntervenerTwoWrapper intervenerDetails = IntervenerTwoWrapper.builder()
            .intervenerSolName("intervener sol")
            .intervenerSolEmail("intervener@intervener.com")
            .intervenerSolicitorReference("123456789")
            .intervenerRepresented(YesOrNo.YES)
            .build();

        intervenerChangeDetails.setIntervenerDetails(intervenerDetails);
        finremCaseData.setIntervenerTwoWrapper(intervenerDetails);
        finremCaseData.setCurrentIntervenerChangeDetails(intervenerChangeDetails);
        finremCaseDetails = FinremCaseDetails.<FinremCaseDataContested>builder()
            .data(finremCaseData).build();
        when(intervenerCorresponder.shouldSendIntervenerSolicitorEmail(intervenerDetails)).thenReturn(true);
        intervenerCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);

        verify(notificationService).sendIntervenerSolicitorAddedEmail(eq(finremCaseDetails), eq(intervenerDetails),
            anyString(), anyString(), anyString());
    }

    @Test
    public void shouldSendEmailIfIntervenerThreeIsRepresented() {
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerType(IntervenerType.INTERVENER_THREE);
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.ADDED);
        IntervenerThreeWrapper intervenerDetails = IntervenerThreeWrapper.builder()
            .intervenerSolName("intervener sol")
            .intervenerSolEmail("intervener@intervener.com")
            .intervenerSolicitorReference("123456789")
            .intervenerRepresented(YesOrNo.YES)
            .build();
        intervenerChangeDetails.setIntervenerDetails(intervenerDetails);
        finremCaseData.setIntervenerThreeWrapper(intervenerDetails);
        finremCaseData.setCurrentIntervenerChangeDetails(intervenerChangeDetails);
        finremCaseDetails = FinremCaseDetails.<FinremCaseDataContested>builder()
            .data(finremCaseData).build();
        when(intervenerCorresponder.shouldSendIntervenerSolicitorEmail(intervenerDetails)).thenReturn(true);
        intervenerCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);

        verify(notificationService).sendIntervenerSolicitorAddedEmail(eq(finremCaseDetails), eq(intervenerDetails),
            anyString(), anyString(), anyString());
    }

    @Test
    public void shouldSendEmailIfIntervenerFourIsRepresented() {
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerType(IntervenerType.INTERVENER_FOUR);
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.ADDED);
        IntervenerFourWrapper intervenerDetails = IntervenerFourWrapper.builder()
            .intervenerSolName("intervener sol")
            .intervenerSolEmail("intervener@intervener.com")
            .intervenerSolicitorReference("123456789")
            .intervenerRepresented(YesOrNo.YES)
            .build();
        finremCaseData.setIntervenerFourWrapper(intervenerDetails);
        intervenerChangeDetails.setIntervenerDetails(intervenerDetails);
        finremCaseData.setCurrentIntervenerChangeDetails(intervenerChangeDetails);
        finremCaseDetails = FinremCaseDetails.<FinremCaseDataContested>builder()
            .data(finremCaseData).build();
        when(intervenerCorresponder.shouldSendIntervenerSolicitorEmail(intervenerDetails)).thenReturn(true);
        intervenerCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);

        verify(notificationService).sendIntervenerSolicitorAddedEmail(eq(finremCaseDetails), eq(intervenerDetails),
            anyString(), anyString(), anyString());
    }

}
