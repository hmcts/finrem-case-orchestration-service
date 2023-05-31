package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.generalorder;

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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GeneralOrderRaisedCorresponderTest {

    GeneralOrderRaisedCorresponder generalOrderRaisedCorresponder;

    @Mock
    NotificationService notificationService;
    @Mock
    CaseDataService caseDataService;

    private CaseDetails caseDetails;

    @Before
    public void setUp() throws Exception {
        generalOrderRaisedCorresponder = new GeneralOrderRaisedCorresponder(notificationService, caseDataService);
        caseDetails = CaseDetails.builder().build();
    }

    @Test
    public void shouldEmailAllSolicitorsWhenConsentedAndBothDigital() {
        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(true);
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);

        generalOrderRaisedCorresponder.sendCorrespondence(caseDetails);

        verify(notificationService).sendConsentedGeneralOrderEmailToApplicantSolicitor(caseDetails);
        verify(notificationService).sendConsentedGeneralOrderEmailToRespondentSolicitor(caseDetails);
    }

    @Test
    public void shouldSendContestedConsentGeneralOrderEmailToRespondentInConsentedInContestedCase() {
        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(false);
        when(caseDataService.isConsentedInContestedCase(caseDetails)).thenReturn(true);
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);

        generalOrderRaisedCorresponder.sendCorrespondence(caseDetails);

        verify(notificationService).sendContestedConsentGeneralOrderEmailRespondentSolicitor(caseDetails);
        verify(notificationService, never()).sendContestedGeneralOrderEmailRespondent(caseDetails);
        verify(notificationService, never()).sendConsentedGeneralOrderEmailToRespondentSolicitor(caseDetails);
    }

    @Test
    public void shouldNotSendGeneralOrderEmail() {
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(any(CaseDetails.class), anyString(),
            any(CaseRole.class))).thenReturn(false);

        generalOrderRaisedCorresponder.sendCorrespondence(caseDetails);

        verify(notificationService, never()).sendConsentedGeneralOrderEmailToRespondentSolicitor(caseDetails);
        verify(notificationService, never()).sendContestedConsentGeneralOrderEmailRespondentSolicitor(caseDetails);
        verify(notificationService, never()).sendContestedGeneralOrderEmailRespondent(caseDetails);

        verify(notificationService, never()).sendContestedConsentGeneralOrderEmailIntervenerSolicitor(any(CaseDetails.class),
            any(SolicitorCaseDataKeysWrapper.class));
        verify(notificationService, never()).sendContestedGeneralOrderEmailIntervener(any(CaseDetails.class),
            any(SolicitorCaseDataKeysWrapper.class));
    }

    @Test
    public void shouldSendContestedConsentGeneralOrderEmail() {
        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(false);
        when(caseDataService.isConsentedInContestedCase(caseDetails)).thenReturn(true);
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);

        generalOrderRaisedCorresponder.sendCorrespondence(caseDetails);

        verify(notificationService).sendContestedConsentGeneralOrderEmailApplicantSolicitor(caseDetails);
    }

    @Test
    public void shouldSendContestedGeneralOrderEmails() {
        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(false);
        when(caseDataService.isConsentedInContestedCase(caseDetails)).thenReturn(false);
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);

        generalOrderRaisedCorresponder.sendCorrespondence(caseDetails);

        verify(notificationService).sendContestedGeneralOrderEmailApplicant(caseDetails);
        verify(notificationService).sendContestedGeneralOrderEmailRespondent(caseDetails);
    }

    @Test
    public void shouldNotSendContestedGeneralOrderEmailToRespondent_ThenTheEmailIsNotIssued() {
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);

        generalOrderRaisedCorresponder.sendCorrespondence(caseDetails);

        verify(notificationService, never()).sendContestedConsentGeneralOrderEmailRespondentSolicitor(caseDetails);
        verify(notificationService, never()).sendContestedGeneralOrderEmailRespondent(caseDetails);
    }

    @Test
    public void shouldNotSendContestedGeneralOrderEmailToIntervener_ThenTheEmailIsNotIssued() {
        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(any(CaseDetails.class), anyString(),
            any(CaseRole.class))).thenReturn(false);

        generalOrderRaisedCorresponder.sendCorrespondence(caseDetails);

        verify(notificationService, never()).sendContestedConsentGeneralOrderEmailIntervenerSolicitor(any(CaseDetails.class),
            any(SolicitorCaseDataKeysWrapper.class));
        verify(notificationService, never()).sendContestedGeneralOrderEmailIntervener(any(CaseDetails.class),
            any(SolicitorCaseDataKeysWrapper.class));
    }

}