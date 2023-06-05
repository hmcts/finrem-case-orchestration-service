package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFourWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOneWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThreeWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwoWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.SolicitorCaseDataKeysWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_EMAIL;

@RunWith(MockitoJUnitRunner.class)
public class ConsentOrderMadeCorresponderTest {

    ConsentOrderMadeCorresponder consentOrderMadeCorresponder;

    @Mock
    NotificationService notificationService;
    @Mock
    FinremCaseDetailsMapper finremCaseDetailsMapper;

    private CaseDetails caseDetails;

    @Before
    public void setUp() throws Exception {
        consentOrderMadeCorresponder = new ConsentOrderMadeCorresponder(notificationService, finremCaseDetailsMapper);
        caseDetails = CaseDetails.builder().data(new HashMap<>()).build();
    }

    @Test
    public void shouldEmailApplicantSolicitor() {
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        consentOrderMadeCorresponder.sendCorrespondence(caseDetails);
        verify(notificationService).sendConsentOrderMadeConfirmationEmailToApplicantSolicitor(caseDetails);
    }

    @Test
    public void shouldEmailRespondentSolicitor() {
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        consentOrderMadeCorresponder.sendCorrespondence(caseDetails);
        verify(notificationService).sendConsentOrderMadeConfirmationEmailToRespondentSolicitor(caseDetails);
    }

    @Test
    public void shouldEmailIntervenerOneSolicitor() {
        String intervenerEmailKey = "intervener1SolEmail";
        caseDetails.getData().put(intervenerEmailKey, TEST_SOLICITOR_EMAIL);
        SolicitorCaseDataKeysWrapper dataKeysWrapper = SolicitorCaseDataKeysWrapper.builder().build();
        when(notificationService.getCaseDataKeysForIntervenerSolicitor(any(IntervenerWrapper.class))).thenReturn(dataKeysWrapper);
        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(IntervenerOneWrapper.builder().build(),
            caseDetails)).thenReturn(true);
        consentOrderMadeCorresponder.sendCorrespondence(caseDetails);
        verify(notificationService).sendConsentOrderMadeConfirmationEmailToIntervenerSolicitor(caseDetails,
            dataKeysWrapper);
    }

    @Test
    public void shouldEmailIntervenerTwoSolicitor() {
        String intervenerEmailKey = "intervener2SolEmail";
        caseDetails.getData().put(intervenerEmailKey, TEST_SOLICITOR_EMAIL);
        SolicitorCaseDataKeysWrapper dataKeysWrapper = SolicitorCaseDataKeysWrapper.builder().build();
        when(notificationService.getCaseDataKeysForIntervenerSolicitor(any(IntervenerWrapper.class))).thenReturn(dataKeysWrapper);
        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(IntervenerTwoWrapper.builder().build(),
            caseDetails)).thenReturn(true);
        consentOrderMadeCorresponder.sendCorrespondence(caseDetails);
        verify(notificationService).sendConsentOrderMadeConfirmationEmailToIntervenerSolicitor(caseDetails,
            dataKeysWrapper);
    }

    @Test
    public void shouldEmailIntervenerThreeSolicitor() {
        String intervenerEmailKey = "intervener3SolEmail";
        caseDetails.getData().put(intervenerEmailKey, TEST_SOLICITOR_EMAIL);
        SolicitorCaseDataKeysWrapper dataKeysWrapper = SolicitorCaseDataKeysWrapper.builder().build();
        when(notificationService.getCaseDataKeysForIntervenerSolicitor(any(IntervenerWrapper.class))).thenReturn(dataKeysWrapper);
        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(IntervenerThreeWrapper.builder().build(),
            caseDetails)).thenReturn(true);
        consentOrderMadeCorresponder.sendCorrespondence(caseDetails);
        verify(notificationService).sendConsentOrderMadeConfirmationEmailToIntervenerSolicitor(caseDetails,
            dataKeysWrapper);
    }

    @Test
    public void shouldEmailIntervenerFourSolicitor() {
        String intervenerEmailKey = "intervener4SolEmail";
        caseDetails.getData().put(intervenerEmailKey, TEST_SOLICITOR_EMAIL);
        SolicitorCaseDataKeysWrapper dataKeysWrapper = SolicitorCaseDataKeysWrapper.builder().build();
        when(notificationService.getCaseDataKeysForIntervenerSolicitor(any(IntervenerWrapper.class))).thenReturn(dataKeysWrapper);
        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(IntervenerFourWrapper.builder().build(),
            caseDetails)).thenReturn(true);
        consentOrderMadeCorresponder.sendCorrespondence(caseDetails);
        verify(notificationService).sendConsentOrderMadeConfirmationEmailToIntervenerSolicitor(caseDetails,
            dataKeysWrapper);
    }

}