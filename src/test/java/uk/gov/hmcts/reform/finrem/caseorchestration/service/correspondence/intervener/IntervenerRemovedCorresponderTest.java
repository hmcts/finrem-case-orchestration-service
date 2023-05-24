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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerChangeDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
    private CaseDocument caseDocument;

    @Before
    public void setup() {
        intervenerRemovedCorresponder = new IntervenerRemovedCorresponder(notificationService, bulkPrintService,
            intervenerDocumentService);
        finremCaseData = FinremCaseData.builder().build();
        caseDocument = CaseDocument.builder().build();
    }

    @Test
    public void shouldAlwaysSendLetterCorrespondenceToAppAndRespIfNotRepresented() {
        finremCaseDetails = getFinremCaseDetailsWhenNotRepresented(IntervenerType.INTERVENER_ONE);
        intervenerRemovedCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);

        verify(intervenerDocumentService, times(1))
            .generateIntervenerRemovedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
                DocumentHelper.PaperNotificationRecipient.APPLICANT);
        verify(intervenerDocumentService, times(1))
            .generateIntervenerRemovedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
                DocumentHelper.PaperNotificationRecipient.RESPONDENT);
    }

    @Test
    public void shouldSendLetterCorrespondenceIfIntervenerIsRepresentedToAppAndRespIfNotRepresented() {
        finremCaseDetails = getFinremCaseDetailsWhenRepresented(IntervenerType.INTERVENER_ONE);
        intervenerRemovedCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);

        verify(intervenerDocumentService, times(1))
            .generateIntervenerSolicitorRemovedLetter(finremCaseDetails, AUTHORISATION_TOKEN,
                DocumentHelper.PaperNotificationRecipient.APPLICANT);
        verify(intervenerDocumentService, times(1))
            .generateIntervenerSolicitorRemovedLetter(finremCaseDetails, AUTHORISATION_TOKEN,
                DocumentHelper.PaperNotificationRecipient.RESPONDENT);
    }

    @Test
    public void shouldSendIntervenerOneLetterCorrespondenceIfNotRepresented() {
        finremCaseDetails = getFinremCaseDetailsWhenNotRepresented(IntervenerType.INTERVENER_ONE);
        intervenerRemovedCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);

        verify(intervenerDocumentService, times(1))
            .generateIntervenerRemovedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
                DocumentHelper.PaperNotificationRecipient.INTERVENER_ONE);
    }

    @Test
    public void shouldSendIntervenerTwoLetterCorrespondenceIfNotRepresented() {
        finremCaseDetails = getFinremCaseDetailsWhenNotRepresented(IntervenerType.INTERVENER_TWO);
        intervenerRemovedCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);

        verify(intervenerDocumentService, times(1))
            .generateIntervenerRemovedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
                DocumentHelper.PaperNotificationRecipient.INTERVENER_TWO);
    }

    @Test
    public void shouldSendIntervenerThreeLetterCorrespondenceIfNotRepresented() {
        finremCaseDetails = getFinremCaseDetailsWhenNotRepresented(IntervenerType.INTERVENER_THREE);
        intervenerRemovedCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);

        verify(intervenerDocumentService, times(1))
            .generateIntervenerRemovedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
                DocumentHelper.PaperNotificationRecipient.INTERVENER_THREE);
    }

    @Test
    public void shouldSendIntervenerFourLetterCorrespondenceIfNotRepresented() {
        finremCaseDetails = getFinremCaseDetailsWhenNotRepresented(IntervenerType.INTERVENER_FOUR);
        intervenerRemovedCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);

        verify(intervenerDocumentService, times(1))
            .generateIntervenerRemovedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
                DocumentHelper.PaperNotificationRecipient.INTERVENER_FOUR);
    }

    @Test
    public void shouldSendEmailIfIntervenerOneIsRepresented() {
        finremCaseDetails = getFinremCaseDetailsWhenRepresented(IntervenerType.INTERVENER_ONE);
        intervenerRemovedCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);

        verify(notificationService).wasIntervenerSolicitorDigitalAndEmailPopulated(finremCaseDetails);
    }

    @Test
    public void shouldSendEmailIfIntervenerTwoIsRepresented() {
        finremCaseDetails = getFinremCaseDetailsWhenRepresented(IntervenerType.INTERVENER_TWO);
        intervenerRemovedCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);

        verify(notificationService).wasIntervenerSolicitorDigitalAndEmailPopulated(finremCaseDetails);
    }

    @Test
    public void shouldSendEmailIfIntervenerThreeIsRepresented() {
        finremCaseDetails = getFinremCaseDetailsWhenRepresented(IntervenerType.INTERVENER_THREE);
        intervenerRemovedCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);

        verify(notificationService).wasIntervenerSolicitorDigitalAndEmailPopulated(finremCaseDetails);
    }

    @Test
    public void shouldSendEmailIfIntervenerFourIsRepresented() {
        finremCaseDetails = getFinremCaseDetailsWhenRepresented(IntervenerType.INTERVENER_FOUR);
        intervenerRemovedCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);

        verify(notificationService).wasIntervenerSolicitorDigitalAndEmailPopulated(finremCaseDetails);
    }

    private FinremCaseDetails getFinremCaseDetailsWhenRepresented(IntervenerType intervenerType) {
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerType(intervenerType);
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.REMOVED);
        IntervenerDetails intervenerDetails = IntervenerDetails.builder()
            .intervenerRepresented(YesOrNo.YES)
            .build();
        intervenerChangeDetails.setIntervenerDetails(intervenerDetails);
        finremCaseData.setCurrentIntervenerChangeDetails(intervenerChangeDetails);
        return FinremCaseDetails.builder().data(finremCaseData).build();
    }

    private FinremCaseDetails getFinremCaseDetailsWhenNotRepresented(IntervenerType intervenerType) {
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerType(intervenerType);
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.REMOVED);
        IntervenerDetails intervenerDetails = IntervenerDetails.builder()
            .intervenerRepresented(YesOrNo.NO)
            .build();
        intervenerChangeDetails.setIntervenerDetails(intervenerDetails);
        finremCaseData.setCurrentIntervenerChangeDetails(intervenerChangeDetails);
        return FinremCaseDetails.builder().data(finremCaseData).build();
    }


}
