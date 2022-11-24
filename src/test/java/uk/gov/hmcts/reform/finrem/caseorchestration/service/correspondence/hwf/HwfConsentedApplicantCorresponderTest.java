package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hwf;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HelpWithFeesDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HwfConsentedApplicantCorresponderTest {

    @Mock
    private BulkPrintService bulkPrintService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private HelpWithFeesDocumentService helpWithFessDocumentService;

    HwfConsentedApplicantCorresponder hwfConsentedApplicantCorresponder;

    private CaseDetails caseDetails;
    private CaseDocument caseDocument;

    protected static final String AUTHORISATION_TOKEN = "authorisationToken";

    @Before
    public void setUp() throws Exception {
        hwfConsentedApplicantCorresponder = new HwfConsentedApplicantCorresponder(bulkPrintService, notificationService, helpWithFessDocumentService);
        caseDetails = CaseDetails.builder().build();
        caseDocument = CaseDocument.builder().build();
        when(helpWithFessDocumentService.generateHwfSuccessfulNotificationLetter(caseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT)).thenReturn(
            caseDocument);
    }

    @Test
    public void shouldGetDocumentToPrint() {

        CaseDocument result = hwfConsentedApplicantCorresponder.getDocumentToPrint(caseDetails, AUTHORISATION_TOKEN);
        assertEquals(caseDocument, result);
        verify(helpWithFessDocumentService).generateHwfSuccessfulNotificationLetter(caseDetails, AUTHORISATION_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT);
    }

    @Test
    public void shouldSendLetterToApplicant() {

        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        hwfConsentedApplicantCorresponder.sendApplicantCorrespondence(AUTHORISATION_TOKEN, caseDetails);
        verify(bulkPrintService).sendDocumentForPrint(caseDocument, caseDetails);
    }

    @Test
    public void shouldSendEmailToApplicant() {

        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        hwfConsentedApplicantCorresponder.sendApplicantCorrespondence(AUTHORISATION_TOKEN, caseDetails);
        verify(notificationService).sendConsentedHWFSuccessfulConfirmationEmail(caseDetails);
    }
}