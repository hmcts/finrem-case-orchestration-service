package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.draftordernotapproved;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DraftOrderNotApprovedCorresponderTest {

    DraftOrderNotApprovedCorresponder draftOrderNotApprovedCorresponder;

    @Mock
    NotificationService notificationService;
    @Mock
    CaseDataService caseDataService;


    private CaseDetails caseDetails;

    protected static final String AUTHORISATION_TOKEN = "authorisationToken";

    @Before
    public void setUp() throws Exception {
        draftOrderNotApprovedCorresponder = new DraftOrderNotApprovedCorresponder(notificationService, caseDataService);
        caseDetails = CaseDetails.builder().build();
    }

    @Test
    public void shouldEmailApplicantSolicitorForConsentedCase() {
        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(true);
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        draftOrderNotApprovedCorresponder.sendCorrespondence(caseDetails, AUTHORISATION_TOKEN);
        verify(notificationService).sendConsentOrderNotApprovedEmailToApplicantSolicitor(caseDetails);
    }

    @Test
    public void shouldEmailApplicantSolicitorForContestedCase() {
        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(false);
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        draftOrderNotApprovedCorresponder.sendCorrespondence(caseDetails, AUTHORISATION_TOKEN);
        verify(notificationService).sendContestOrderNotApprovedEmailApplicant(caseDetails);
    }

    @Test
    public void shouldEmailRespondentSolicitorForConsentedCase() {
        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(true);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        draftOrderNotApprovedCorresponder.sendCorrespondence(caseDetails, AUTHORISATION_TOKEN);
        verify(notificationService).sendConsentOrderNotApprovedEmailToRespondentSolicitor(caseDetails);
    }


    @Test
    public void shouldEmailRespondentSolicitorForContestedCase() {
        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(false);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        draftOrderNotApprovedCorresponder.sendCorrespondence(caseDetails, AUTHORISATION_TOKEN);
        verify(notificationService).sendContestOrderNotApprovedEmailRespondent(caseDetails);
    }

}