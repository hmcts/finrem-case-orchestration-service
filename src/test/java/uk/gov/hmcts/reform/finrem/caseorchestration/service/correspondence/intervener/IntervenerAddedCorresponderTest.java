package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.intervener;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.intervener.IntervenerFourToIntervenerDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.intervener.IntervenerOneToIntervenerDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.intervener.IntervenerThreeToIntervenerDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.intervener.IntervenerTwoToIntervenerDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFourWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOneWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThreeWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwoWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerChangeDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IntervenerAddedCorresponderTest {

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
    private FinremCaseData finremCaseData;
    private CaseDocument caseDocument;

    @Before
    public void setup() {
        intervenerAddedCorresponder = new IntervenerAddedCorresponder(notificationService, bulkPrintService,
            intervenerDocumentService, intervenerOneDetailsMapper, intervenerTwoDetailsMapper,
            intervenerThreeDetailsMapper, intervenerFourDetailsMapper);
        IntervenerOneWrapper intervenerOneWrapper = IntervenerOneWrapper.builder().build();
        IntervenerTwoWrapper intervenerTwoWrapper = IntervenerTwoWrapper.builder().build();
        IntervenerThreeWrapper intervenerThreeWrapper = IntervenerThreeWrapper.builder().build();
        IntervenerFourWrapper intervenerFourWrapper = IntervenerFourWrapper.builder().build();
        finremCaseData = FinremCaseData.builder()
            .intervenerOneWrapper(intervenerOneWrapper)
            .intervenerTwoWrapper(intervenerTwoWrapper)
            .intervenerThreeWrapper(intervenerThreeWrapper)
            .intervenerFourWrapper(intervenerFourWrapper)
            .build();
        caseDocument = CaseDocument.builder().build();
    }

    @Test
    public void shouldAlwaysSendLetterCorrespondenceToAppAndRespIfNotRepresented() {
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerType(IntervenerType.INTERVENER_ONE);
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.ADDED);
        IntervenerDetails intervenerDetails = IntervenerDetails.builder()
            .intervenerRepresented(YesOrNo.NO)
            .build();
        intervenerChangeDetails.setIntervenerDetails(intervenerDetails);
        finremCaseData.setCurrentIntervenerChangeDetails(intervenerChangeDetails);
        finremCaseDetails = FinremCaseDetails.builder().data(finremCaseData).build();
        when(intervenerAddedCorresponder.shouldSendIntervenerOneSolicitorEmail(finremCaseDetails)).thenReturn(false);
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
        IntervenerDetails intervenerDetails = IntervenerDetails.builder()
            .intervenerRepresented(YesOrNo.YES)
            .build();
        intervenerChangeDetails.setIntervenerDetails(intervenerDetails);

        finremCaseData.setCurrentIntervenerChangeDetails(intervenerChangeDetails);
        finremCaseDetails = FinremCaseDetails.builder().data(finremCaseData).build();
        when(intervenerAddedCorresponder.shouldSendIntervenerOneSolicitorEmail(finremCaseDetails)).thenReturn(false);
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
        IntervenerDetails intervenerDetails = IntervenerDetails.builder()
            .intervenerRepresented(YesOrNo.NO)
            .build();
        intervenerChangeDetails.setIntervenerDetails(intervenerDetails);
        finremCaseData.setCurrentIntervenerChangeDetails(intervenerChangeDetails);
        finremCaseDetails = FinremCaseDetails.builder().data(finremCaseData).build();
        when(intervenerAddedCorresponder.shouldSendIntervenerOneSolicitorEmail(finremCaseDetails)).thenReturn(false);
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
        IntervenerDetails intervenerDetails = IntervenerDetails.builder()
            .intervenerRepresented(YesOrNo.NO)
            .build();
        intervenerChangeDetails.setIntervenerDetails(intervenerDetails);
        finremCaseData.setCurrentIntervenerChangeDetails(intervenerChangeDetails);
        finremCaseDetails = FinremCaseDetails.builder().data(finremCaseData).build();
        when(intervenerAddedCorresponder.shouldSendIntervenerTwoSolicitorEmail(finremCaseDetails)).thenReturn(false);
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
        IntervenerDetails intervenerDetails = IntervenerDetails.builder()
            .intervenerRepresented(YesOrNo.NO)
            .build();
        intervenerChangeDetails.setIntervenerDetails(intervenerDetails);
        finremCaseData.setCurrentIntervenerChangeDetails(intervenerChangeDetails);
        finremCaseDetails = FinremCaseDetails.builder().data(finremCaseData).build();
        when(intervenerAddedCorresponder.shouldSendIntervenerThreeSolicitorEmail(finremCaseDetails)).thenReturn(false);
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
        IntervenerDetails intervenerDetails = IntervenerDetails.builder()
            .intervenerRepresented(YesOrNo.NO)
            .build();
        intervenerChangeDetails.setIntervenerDetails(intervenerDetails);
        finremCaseData.setCurrentIntervenerChangeDetails(intervenerChangeDetails);
        finremCaseDetails = FinremCaseDetails.builder().data(finremCaseData).build();
        when(intervenerAddedCorresponder.shouldSendIntervenerFourSolicitorEmail(finremCaseDetails)).thenReturn(false);
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
        IntervenerDetails intervenerDetails = IntervenerDetails.builder()
            .intervenerRepresented(YesOrNo.YES)
            .build();
        intervenerChangeDetails.setIntervenerDetails(intervenerDetails);
        finremCaseData.setCurrentIntervenerChangeDetails(intervenerChangeDetails);
        finremCaseDetails = FinremCaseDetails.builder().data(finremCaseData).build();
        when(intervenerAddedCorresponder.shouldSendIntervenerOneSolicitorEmail(finremCaseDetails)).thenReturn(true);
        intervenerAddedCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);

        verify(notificationService).isIntervenerOneSolicitorDigitalAndEmailPopulated(finremCaseDetails);
    }

    @Test
    public void shouldSendEmailIfIntervenerTwoIsRepresented() {
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerType(IntervenerType.INTERVENER_TWO);
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.ADDED);
        IntervenerDetails intervenerDetails = IntervenerDetails.builder()
            .intervenerRepresented(YesOrNo.YES)
            .build();
        intervenerChangeDetails.setIntervenerDetails(intervenerDetails);
        finremCaseData.setCurrentIntervenerChangeDetails(intervenerChangeDetails);
        finremCaseDetails = FinremCaseDetails.builder()
            .data(finremCaseData).build();
        when(intervenerAddedCorresponder.shouldSendIntervenerTwoSolicitorEmail(finremCaseDetails)).thenReturn(true);
        intervenerAddedCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);

        verify(notificationService).isIntervenerTwoSolicitorDigitalAndEmailPopulated(finremCaseDetails);
    }

    @Test
    public void shouldSendEmailIfIntervenerThreeIsRepresented() {
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerType(IntervenerType.INTERVENER_THREE);
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.ADDED);
        IntervenerDetails intervenerDetails = IntervenerDetails.builder()
            .intervenerRepresented(YesOrNo.YES)
            .build();
        intervenerChangeDetails.setIntervenerDetails(intervenerDetails);
        finremCaseData.setCurrentIntervenerChangeDetails(intervenerChangeDetails);
        finremCaseDetails = FinremCaseDetails.builder()
            .data(finremCaseData).build();
        when(intervenerAddedCorresponder.shouldSendIntervenerThreeSolicitorEmail(finremCaseDetails)).thenReturn(true);
        intervenerAddedCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);

        verify(notificationService).isIntervenerThreeSolicitorDigitalAndEmailPopulated(finremCaseDetails);
    }

    @Test
    public void shouldSendEmailIfIntervenerFourIsRepresented() {
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerType(IntervenerType.INTERVENER_FOUR);
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.ADDED);
        IntervenerDetails intervenerDetails = IntervenerDetails.builder()
            .intervenerRepresented(YesOrNo.YES)
            .build();
        intervenerChangeDetails.setIntervenerDetails(intervenerDetails);
        finremCaseData.setCurrentIntervenerChangeDetails(intervenerChangeDetails);
        finremCaseDetails = FinremCaseDetails.builder()
            .data(finremCaseData).build();
        when(intervenerAddedCorresponder.shouldSendIntervenerFourSolicitorEmail(finremCaseDetails)).thenReturn(true);
        intervenerAddedCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);

        verify(notificationService).isIntervenerFourSolicitorDigitalAndEmailPopulated(finremCaseDetails);
    }
}
