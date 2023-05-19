package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.SolicitorCaseDataKeysWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.HashMap;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_EMAIL;

@RunWith(MockitoJUnitRunner.class)
public class ConsentOrderAvailableCorresponderTest {

    ConsentOrderAvailableCorresponder consentOrderAvailableCorresponder;

    @Mock
    NotificationService notificationService;

    private CaseDetails caseDetails;

    @Before
    public void setUp() throws Exception {
        consentOrderAvailableCorresponder = new ConsentOrderAvailableCorresponder(notificationService);
        caseDetails = CaseDetails.builder().data(new HashMap<>()).build();
    }

    @Test
    public void shouldEmailApplicantSolicitor() {
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        consentOrderAvailableCorresponder.sendCorrespondence(caseDetails);
        verify(notificationService).sendConsentOrderAvailableEmailToApplicantSolicitor(caseDetails);
    }

    @Test
    public void shouldEmailRespondentSolicitor() {
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        consentOrderAvailableCorresponder.sendCorrespondence(caseDetails);
        verify(notificationService).sendConsentOrderAvailableEmailToRespondentSolicitor(caseDetails);
    }

    @Test
    public void shouldEmailIntervenerOneSolicitor() {
        String intervenerEmailKey = "intervener1SolEmail";
        caseDetails.getData().put(intervenerEmailKey, TEST_SOLICITOR_EMAIL);
        SolicitorCaseDataKeysWrapper dataKeysWrapper = SolicitorCaseDataKeysWrapper.builder().build();
        when(notificationService.getCaseDataKeysForIntervenerOneSolicitor()).thenReturn(dataKeysWrapper);
        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(caseDetails, intervenerEmailKey,
            CaseRole.INTVR_SOLICITOR_1)).thenReturn(true);
        consentOrderAvailableCorresponder.sendCorrespondence(caseDetails);
        verify(notificationService).sendConsentOrderAvailableEmailToIntervenerSolicitor(caseDetails,
            dataKeysWrapper);
    }

    @Test
    public void shouldEmailIntervenerTwoSolicitor() {
        String intervenerEmailKey = "intervener2SolEmail";
        caseDetails.getData().put(intervenerEmailKey, TEST_SOLICITOR_EMAIL);
        SolicitorCaseDataKeysWrapper dataKeysWrapper = SolicitorCaseDataKeysWrapper.builder().build();
        when(notificationService.getCaseDataKeysForIntervenerTwoSolicitor()).thenReturn(dataKeysWrapper);
        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(caseDetails, intervenerEmailKey,
            CaseRole.INTVR_SOLICITOR_2)).thenReturn(true);
        consentOrderAvailableCorresponder.sendCorrespondence(caseDetails);
        verify(notificationService).sendConsentOrderAvailableEmailToIntervenerSolicitor(caseDetails,
            dataKeysWrapper);
    }

    @Test
    public void shouldEmailIntervenerThreeSolicitor() {
        String intervenerEmailKey = "intervener3SolEmail";
        caseDetails.getData().put(intervenerEmailKey, TEST_SOLICITOR_EMAIL);
        SolicitorCaseDataKeysWrapper dataKeysWrapper = SolicitorCaseDataKeysWrapper.builder().build();
        when(notificationService.getCaseDataKeysForIntervenerThreeSolicitor()).thenReturn(dataKeysWrapper);
        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(caseDetails, intervenerEmailKey,
            CaseRole.INTVR_SOLICITOR_3)).thenReturn(true);
        consentOrderAvailableCorresponder.sendCorrespondence(caseDetails);
        verify(notificationService).sendConsentOrderAvailableEmailToIntervenerSolicitor(caseDetails,
            dataKeysWrapper);
    }

    @Test
    public void shouldEmailIntervenerFourSolicitor() {
        String intervenerEmailKey = "intervener4SolEmail";
        caseDetails.getData().put(intervenerEmailKey, TEST_SOLICITOR_EMAIL);
        SolicitorCaseDataKeysWrapper dataKeysWrapper = SolicitorCaseDataKeysWrapper.builder().build();
        when(notificationService.getCaseDataKeysForIntervenerFourSolicitor()).thenReturn(dataKeysWrapper);
        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(caseDetails, intervenerEmailKey,
            CaseRole.INTVR_SOLICITOR_4)).thenReturn(true);
        consentOrderAvailableCorresponder.sendCorrespondence(caseDetails);
        verify(notificationService).sendConsentOrderAvailableEmailToIntervenerSolicitor(caseDetails,
            dataKeysWrapper);
    }

}