package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.intervener;

import org.apache.http.auth.AUTH;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.intervener.IntervenerOneToIntervenerDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOneWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerChangeDetails;
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
            intervenerDocumentService, intervenerOneDetailsMapper);
        IntervenerOneWrapper intervenerOneWrapper = IntervenerOneWrapper.builder().build();
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails(
            IntervenerChangeDetails.IntervenerType.INTERVENER_ONE,
            IntervenerChangeDetails.IntervenerAction.ADDED);
        finremCaseData = FinremCaseData.builder()
            .intervenerOneWrapper(intervenerOneWrapper)
            .currentIntervenerChangeDetails(intervenerChangeDetails)
            .build();
        finremCaseDetails = FinremCaseDetails.builder().data(finremCaseData).build();
        caseDocument = CaseDocument.builder().build();
        when(intervenerDocumentService.generateIntervenerAddedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT)).thenReturn(caseDocument);
        when(intervenerDocumentService.generateIntervenerAddedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT)).thenReturn(caseDocument);
    }

    @Test
    public void shouldSendLetterCorrespondenceIfNotRepresented() {
        when(intervenerAddedCorresponder.shouldSendIntervenerOneSolicitorEmail(finremCaseDetails)).thenReturn(false);
        intervenerAddedCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);

        verify(intervenerDocumentService, times(1))
            .generateIntervenerAddedNotificationLetter(finremCaseDetails, AUTHORISATION_TOKEN,
                DocumentHelper.PaperNotificationRecipient.INTERVENER_ONE);
    }

    @Test
    public void shouldSendEmailIfIntervenerOneIsRepresented() {
        when(intervenerAddedCorresponder.shouldSendIntervenerOneSolicitorEmail(finremCaseDetails)).thenReturn(true);
        intervenerAddedCorresponder.sendCorrespondence(finremCaseDetails, AUTHORISATION_TOKEN);

        verify(notificationService).isIntervenerOneSolicitorDigitalAndEmailPopulated(finremCaseDetails);
    }

}
