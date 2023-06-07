package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
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
public class ConsentOrderAvailableCorresponderTest {

    ConsentOrderAvailableCorresponder consentOrderAvailableCorresponder;

    @Mock
    NotificationService notificationService;
    @Mock
    FinremCaseDetailsMapper finremCaseDetailsMapper;

    private CaseDetails caseDetails;

    @Before
    public void setUp() throws Exception {
        consentOrderAvailableCorresponder = new ConsentOrderAvailableCorresponder(notificationService,
            finremCaseDetailsMapper);
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
        when(notificationService.isContestedApplication(caseDetails)).thenReturn(true);
        when(finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails)).thenReturn(FinremCaseDetails.builder()
            .data(FinremCaseData.builder()
                .intervenerOneWrapper(IntervenerOneWrapper.builder()
                    .intervenerSolEmail(TEST_SOLICITOR_EMAIL)
                    .build())
                .build())
            .build());
        when(notificationService.getCaseDataKeysForIntervenerSolicitor(any(IntervenerWrapper.class))).thenReturn(dataKeysWrapper);
        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(IntervenerOneWrapper.builder()
                .intervenerSolEmail(TEST_SOLICITOR_EMAIL).build(), caseDetails)).thenReturn(true);
        consentOrderAvailableCorresponder.sendCorrespondence(caseDetails);
        verify(notificationService).sendConsentOrderAvailableEmailToIntervenerSolicitor(caseDetails,
            dataKeysWrapper);
    }

    @Test
    public void shouldEmailIntervenerTwoSolicitor() {
        String intervenerEmailKey = "intervener2SolEmail";
        caseDetails.getData().put(intervenerEmailKey, TEST_SOLICITOR_EMAIL);
        SolicitorCaseDataKeysWrapper dataKeysWrapper = SolicitorCaseDataKeysWrapper.builder().build();
        when(notificationService.isContestedApplication(caseDetails)).thenReturn(true);
        when(finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails)).thenReturn(FinremCaseDetails.builder()
            .data(FinremCaseData.builder()
                .intervenerTwoWrapper(IntervenerTwoWrapper.builder()
                    .intervenerSolEmail(TEST_SOLICITOR_EMAIL)
                    .build())
                .build())
            .build());
        when(notificationService.getCaseDataKeysForIntervenerSolicitor(any(IntervenerWrapper.class))).thenReturn(dataKeysWrapper);
        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(IntervenerTwoWrapper.builder()
                .intervenerSolEmail(TEST_SOLICITOR_EMAIL).build(),caseDetails)).thenReturn(true);
        consentOrderAvailableCorresponder.sendCorrespondence(caseDetails);
        verify(notificationService).sendConsentOrderAvailableEmailToIntervenerSolicitor(caseDetails,
            dataKeysWrapper);
    }

    @Test
    public void shouldEmailIntervenerThreeSolicitor() {
        String intervenerEmailKey = "intervener3SolEmail";
        caseDetails.getData().put(intervenerEmailKey, TEST_SOLICITOR_EMAIL);
        SolicitorCaseDataKeysWrapper dataKeysWrapper = SolicitorCaseDataKeysWrapper.builder().build();
        when(notificationService.isContestedApplication(caseDetails)).thenReturn(true);
        when(finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails)).thenReturn(FinremCaseDetails.builder()
            .data(FinremCaseData.builder()
                .intervenerThreeWrapper(IntervenerThreeWrapper.builder()
                    .intervenerSolEmail(TEST_SOLICITOR_EMAIL)
                    .build())
                .build())
            .build());
        when(notificationService.getCaseDataKeysForIntervenerSolicitor(any(IntervenerWrapper.class))).thenReturn(dataKeysWrapper);
        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(IntervenerThreeWrapper.builder()
                .intervenerSolEmail(TEST_SOLICITOR_EMAIL).build(),caseDetails)).thenReturn(true);
        consentOrderAvailableCorresponder.sendCorrespondence(caseDetails);
        verify(notificationService).sendConsentOrderAvailableEmailToIntervenerSolicitor(caseDetails,
            dataKeysWrapper);
    }

    @Test
    public void shouldEmailIntervenerFourSolicitor() {
        String intervenerEmailKey = "intervener4SolEmail";
        caseDetails.getData().put(intervenerEmailKey, TEST_SOLICITOR_EMAIL);
        SolicitorCaseDataKeysWrapper dataKeysWrapper = SolicitorCaseDataKeysWrapper.builder().build();
        when(notificationService.isContestedApplication(caseDetails)).thenReturn(true);
        when(finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails)).thenReturn(FinremCaseDetails.builder()
            .data(FinremCaseData.builder()
                .intervenerFourWrapper(IntervenerFourWrapper.builder()
                    .intervenerSolEmail(TEST_SOLICITOR_EMAIL)
                    .build())
                .build())
            .build());
        when(notificationService.getCaseDataKeysForIntervenerSolicitor(any(IntervenerWrapper.class))).thenReturn(dataKeysWrapper);
        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(IntervenerFourWrapper.builder()
                .intervenerSolEmail(TEST_SOLICITOR_EMAIL).build(),caseDetails)).thenReturn(true);
        consentOrderAvailableCorresponder.sendCorrespondence(caseDetails);
        verify(notificationService).sendConsentOrderAvailableEmailToIntervenerSolicitor(caseDetails,
            dataKeysWrapper);
    }

}