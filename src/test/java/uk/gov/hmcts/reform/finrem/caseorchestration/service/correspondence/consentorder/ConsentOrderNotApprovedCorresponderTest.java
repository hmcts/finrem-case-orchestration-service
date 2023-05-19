package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.SolicitorCaseDataKeysWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.HashMap;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_EMAIL;

@RunWith(MockitoJUnitRunner.class)
public class ConsentOrderNotApprovedCorresponderTest {

    ConsentOrderNotApprovedCorresponder consentOrderNotApprovedCorresponder;

    @Mock
    NotificationService notificationService;
    @Mock
    CaseDataService caseDataService;

    private CaseDetails caseDetails;

    @Before
    public void setUp() throws Exception {
        consentOrderNotApprovedCorresponder = new ConsentOrderNotApprovedCorresponder(notificationService, caseDataService);
        caseDetails = CaseDetails.builder().data(new HashMap<>()).build();
    }

    @Test
    public void shouldEmailApplicantSolicitorForConsentedCase() {
        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(true);
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        consentOrderNotApprovedCorresponder.sendCorrespondence(caseDetails);
        verify(notificationService).sendConsentOrderNotApprovedEmailToApplicantSolicitor(caseDetails);
    }

    @Test
    public void shouldEmailApplicantSolicitorForContestedCase() {
        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(false);
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        consentOrderNotApprovedCorresponder.sendCorrespondence(caseDetails);
        verify(notificationService).sendContestOrderNotApprovedEmailApplicant(caseDetails);
    }

    @Test
    public void shouldEmailRespondentSolicitorForConsentedCase() {
        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(true);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        consentOrderNotApprovedCorresponder.sendCorrespondence(caseDetails);
        verify(notificationService).sendConsentOrderNotApprovedEmailToRespondentSolicitor(caseDetails);
    }


    @Test
    public void shouldEmailRespondentSolicitorForContestedCase() {
        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(false);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        consentOrderNotApprovedCorresponder.sendCorrespondence(caseDetails);
        verify(notificationService).sendContestOrderNotApprovedEmailRespondent(caseDetails);
    }

    @Test
    public void shouldEmailIntervenerSolicitorForContestedCase() {
        String intervenerEmailKey = "intervener1SolEmail";
        caseDetails.getData().put(intervenerEmailKey, TEST_SOLICITOR_EMAIL);
        SolicitorCaseDataKeysWrapper dataKeysWrapper = SolicitorCaseDataKeysWrapper.builder().build();
        when(caseDataService.isContestedApplication(caseDetails)).thenReturn(true);
        when(notificationService.getCaseDataKeysForIntervenerOneSolicitor()).thenReturn(dataKeysWrapper);
        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(caseDetails, intervenerEmailKey,
            CaseRole.INTVR_SOLICITOR_1)).thenReturn(true);
        consentOrderNotApprovedCorresponder.sendCorrespondence(caseDetails);
        verify(notificationService).sendContestOrderNotApprovedEmailIntervener(caseDetails,
            dataKeysWrapper);
    }

}