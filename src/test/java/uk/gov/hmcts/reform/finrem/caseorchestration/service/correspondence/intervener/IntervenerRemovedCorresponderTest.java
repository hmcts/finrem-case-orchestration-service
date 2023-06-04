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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFourWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOneWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThreeWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwoWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerChangeDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

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
        IntervenerOneWrapper wrapper = new IntervenerOneWrapper();
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
        IntervenerOneWrapper wrapper = new IntervenerOneWrapper();
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
        IntervenerOneWrapper wrapper = new IntervenerOneWrapper();
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
    public void shouldSendIntervenerOneLetterCorrespondenceIfWasNotRepresented() {
        finremCaseDetails = getFinremCaseDetails(IntervenerType.INTERVENER_ONE);
        IntervenerOneWrapper wrapper = new IntervenerOneWrapper();
        finremCaseDetails.getData().getCurrentIntervenerChangeDetails().setIntervenerDetails(wrapper);

        intervenerRemovedCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);

        verify(intervenerDocumentService, times(1))
            .generateIntervenerRemovedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
                DocumentHelper.PaperNotificationRecipient.INTERVENER_ONE);
    }

    @Test
    public void shouldSendIntervenerTwoLetterCorrespondenceIfWasNotRepresented() {
        finremCaseDetails = getFinremCaseDetails(IntervenerType.INTERVENER_TWO);
        IntervenerTwoWrapper wrapper = new IntervenerTwoWrapper();
        finremCaseDetails.getData().getCurrentIntervenerChangeDetails().setIntervenerDetails(wrapper);

        intervenerRemovedCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);

        verify(intervenerDocumentService, times(1))
            .generateIntervenerRemovedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
                DocumentHelper.PaperNotificationRecipient.INTERVENER_TWO);
    }

    @Test
    public void shouldSendIntervenerThreeLetterCorrespondenceIfWasNotRepresented() {
        finremCaseDetails = getFinremCaseDetails(IntervenerType.INTERVENER_THREE);
        IntervenerThreeWrapper wrapper = new IntervenerThreeWrapper();
        finremCaseDetails.getData().getCurrentIntervenerChangeDetails().setIntervenerDetails(wrapper);

        intervenerRemovedCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);

        verify(intervenerDocumentService, times(1))
            .generateIntervenerRemovedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
                DocumentHelper.PaperNotificationRecipient.INTERVENER_THREE);
    }

    @Test
    public void shouldSendIntervenerFourLetterCorrespondenceIfNotRepresented() {
        finremCaseDetails = getFinremCaseDetails(IntervenerType.INTERVENER_FOUR);
        IntervenerFourWrapper wrapper = new IntervenerFourWrapper();
        finremCaseDetails.getData().getCurrentIntervenerChangeDetails().setIntervenerDetails(wrapper);
        intervenerRemovedCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);

        verify(intervenerDocumentService, times(1))
            .generateIntervenerRemovedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
                DocumentHelper.PaperNotificationRecipient.INTERVENER_FOUR);
    }

    @Test
    public void shouldSendEmailIfIntervenerOneIsRepresented() {
        finremCaseDetails = getFinremCaseDetails(IntervenerType.INTERVENER_ONE);
        IntervenerChangeDetails intervenerChangeDetails = finremCaseDetails.getData().getCurrentIntervenerChangeDetails();
        IntervenerOneWrapper wrapper = new IntervenerOneWrapper();
        wrapper.setIntervenerRepresented(YesOrNo.YES);
        intervenerChangeDetails.setIntervenerDetails(wrapper);
        intervenerChangeDetails.getIntervenerDetails().setIntervenerRepresented(YesOrNo.YES);
        intervenerChangeDetails.getIntervenerDetails().setIntervenerSolEmail("digitalkevin@email.com");
        intervenerRemovedCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);
        verify(notificationService,
            times(1)).wasIntervenerSolicitorEmailPopulated(intervenerChangeDetails.getIntervenerDetails());
    }

    @Test
    public void shouldSendEmailIfIntervenerTwoIsRepresented() {
        finremCaseDetails = getFinremCaseDetails(IntervenerType.INTERVENER_TWO);
        IntervenerChangeDetails intervenerChangeDetails = finremCaseDetails.getData().getCurrentIntervenerChangeDetails();
        IntervenerTwoWrapper wrapper = new IntervenerTwoWrapper();
        wrapper.setIntervenerRepresented(YesOrNo.YES);
        intervenerChangeDetails.setIntervenerDetails(wrapper);
        intervenerChangeDetails.getIntervenerDetails().setIntervenerRepresented(YesOrNo.YES);
        intervenerChangeDetails.getIntervenerDetails().setIntervenerSolEmail("digitalkevin@email.com");
        intervenerRemovedCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);
        verify(notificationService,
            times(1)).wasIntervenerSolicitorEmailPopulated(intervenerChangeDetails.getIntervenerDetails());
    }

    @Test
    public void shouldSendEmailIfIntervenerThreeIsRepresented() {
        finremCaseDetails = getFinremCaseDetails(IntervenerType.INTERVENER_THREE);
        IntervenerChangeDetails intervenerChangeDetails = finremCaseDetails.getData().getCurrentIntervenerChangeDetails();
        IntervenerThreeWrapper wrapper = new IntervenerThreeWrapper();
        wrapper.setIntervenerRepresented(YesOrNo.YES);
        intervenerChangeDetails.setIntervenerDetails(wrapper);
        intervenerChangeDetails.getIntervenerDetails().setIntervenerRepresented(YesOrNo.YES);
        intervenerChangeDetails.getIntervenerDetails().setIntervenerSolEmail("digitalkevin@email.com");
        intervenerRemovedCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);
        verify(notificationService,
            times(1)).wasIntervenerSolicitorEmailPopulated(intervenerChangeDetails.getIntervenerDetails());
    }

    @Test
    public void shouldSendIntervenerSolicitorEmailIfIntervenerFourWasRepresented() {
        finremCaseDetails = getFinremCaseDetails(IntervenerType.INTERVENER_FOUR);
        IntervenerChangeDetails intervenerChangeDetails = finremCaseDetails.getData().getCurrentIntervenerChangeDetails();
        IntervenerFourWrapper wrapper = new IntervenerFourWrapper();
        wrapper.setIntervenerRepresented(YesOrNo.YES);
        intervenerChangeDetails.setIntervenerDetails(wrapper);
        intervenerChangeDetails.getIntervenerDetails().setIntervenerRepresented(YesOrNo.YES);
        intervenerChangeDetails.getIntervenerDetails().setIntervenerSolEmail("digitalkevin@email.com");
        intervenerRemovedCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);
        verify(notificationService,
            times(1)).wasIntervenerSolicitorEmailPopulated(intervenerChangeDetails.getIntervenerDetails());
    }

    private FinremCaseDetails getFinremCaseDetails(IntervenerType intervenerType) {
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerType(intervenerType);
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.REMOVED);
        finremCaseData.setCurrentIntervenerChangeDetails(intervenerChangeDetails);

        return FinremCaseDetails.builder().data(finremCaseData).build();
    }

}