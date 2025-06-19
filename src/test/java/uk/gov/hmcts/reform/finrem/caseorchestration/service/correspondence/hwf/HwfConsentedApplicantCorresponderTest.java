package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hwf;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HelpWithFeesDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;

@RunWith(MockitoJUnitRunner.class)
public class HwfConsentedApplicantCorresponderTest {

    @Mock
    private BulkPrintService bulkPrintService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private HelpWithFeesDocumentService helpWithFessDocumentService;

    HwfConsentedApplicantCorresponder hwfConsentedApplicantCorresponder;

    private FinremCaseDetails caseDetails;
    private CaseDocument caseDocument;

    @Before
    public void setUp() throws Exception {
        hwfConsentedApplicantCorresponder = new HwfConsentedApplicantCorresponder(bulkPrintService, notificationService, helpWithFessDocumentService);
        caseDetails = FinremCaseDetails.builder().build();
        caseDocument = CaseDocument.builder().build();
        when(helpWithFessDocumentService.generateHwfSuccessfulNotificationLetter(caseDetails, AUTH_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT)).thenReturn(
            caseDocument);
    }

    @Test
    public void shouldGetDocumentToPrint() {
        CaseDocument result = hwfConsentedApplicantCorresponder.getDocumentToPrint(caseDetails, AUTH_TOKEN);
        assertEquals(caseDocument, result);
        verify(helpWithFessDocumentService).generateHwfSuccessfulNotificationLetter(caseDetails, AUTH_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT);
    }

    @Test
    public void shouldSendLetterToApplicant() {
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        hwfConsentedApplicantCorresponder.sendCorrespondence(caseDetails, AUTH_TOKEN);
        verify(bulkPrintService).sendDocumentForPrint(caseDocument, caseDetails, APPLICANT, AUTH_TOKEN);
    }

    @Test
    public void shouldSendEmailToApplicant() {
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        hwfConsentedApplicantCorresponder.sendCorrespondence(caseDetails, AUTH_TOKEN);
        verify(notificationService).sendConsentedHWFSuccessfulConfirmationEmail(caseDetails);
    }
}
