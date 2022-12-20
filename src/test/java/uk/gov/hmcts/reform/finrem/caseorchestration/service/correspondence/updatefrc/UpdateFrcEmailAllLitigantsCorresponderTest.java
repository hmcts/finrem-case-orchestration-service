package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.updatefrc;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.updatefrc.service.UpdateFrcInfoRespondentDocumentService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UpdateFrcEmailAllLitigantsCorresponderTest {

    UpdateFrcLetterOrEmailAllSolicitorsCorresponder updateFrcEmailAllLitigantsCorresponder;

    @Mock
    NotificationService notificationService;
    @Mock
    BulkPrintService bulkPrintService;
    @Mock
    UpdateFrcInfoRespondentDocumentService updateFrcInfoRespondentDocumentService;

    UpdateFrcApplicantCorresponder updateFrcApplicantCorresponder;
    UpdateFrcRespondentCorresponder updateFrcRespondentCorresponder;

    private CaseDetails caseDetails;

    protected static final String AUTHORISATION_TOKEN = "authorisationToken";

    @Before
    public void setUp() throws Exception {
        updateFrcApplicantCorresponder =
            new UpdateFrcApplicantCorresponder(bulkPrintService, notificationService, updateFrcInfoRespondentDocumentService);
        updateFrcRespondentCorresponder =
            new UpdateFrcRespondentCorresponder(bulkPrintService, notificationService, updateFrcInfoRespondentDocumentService);
        updateFrcEmailAllLitigantsCorresponder =
            new UpdateFrcLetterOrEmailAllSolicitorsCorresponder(updateFrcApplicantCorresponder, updateFrcRespondentCorresponder);
        caseDetails = CaseDetails.builder().build();
    }

    @Test
    public void givenApplicantSolicitorIsDigitalshouldSendEmailApplicantSolicitor() {

        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        updateFrcEmailAllLitigantsCorresponder.sendCorrespondence(caseDetails, AUTHORISATION_TOKEN);
        verify(notificationService).isApplicantSolicitorDigitalAndEmailPopulated(caseDetails);
        verify(notificationService).isRespondentSolicitorDigitalAndEmailPopulated(caseDetails);
        verify(notificationService).sendUpdateFrcInformationEmailToAppSolicitor(caseDetails);
    }

    @Test
    public void givenRespondentSolicitorIsDigitalshouldSendEmailRespondentSolicitor() {

        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        updateFrcEmailAllLitigantsCorresponder.sendCorrespondence(caseDetails, AUTHORISATION_TOKEN);
        verify(notificationService).isRespondentSolicitorDigitalAndEmailPopulated(caseDetails);
        verify(notificationService).isApplicantSolicitorDigitalAndEmailPopulated(caseDetails);
        verify(notificationService).sendUpdateFrcInformationEmailToRespondentSolicitor(caseDetails);
    }
}