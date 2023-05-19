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
public class ContestedConsentOrderNotApprovedCorresponderTest {

    ContestedConsentOrderNotApprovedCorresponder contestedConsentOrderNotApprovedCorresponder;

    @Mock
    NotificationService notificationService;

    private CaseDetails caseDetails;

    @Before
    public void setUp() throws Exception {
        contestedConsentOrderNotApprovedCorresponder = new ContestedConsentOrderNotApprovedCorresponder(notificationService);
        caseDetails = CaseDetails.builder().data(new HashMap<>()).build();
    }

    @Test
    public void shouldEmailApplicantSolicitor() {
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        contestedConsentOrderNotApprovedCorresponder.sendCorrespondence(caseDetails);
        verify(notificationService).sendContestedConsentOrderNotApprovedEmailApplicantSolicitor(caseDetails);
    }

    @Test
    public void shouldEmailRespondentSolicitor() {
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        contestedConsentOrderNotApprovedCorresponder.sendCorrespondence(caseDetails);
        verify(notificationService).sendContestedConsentOrderNotApprovedEmailRespondentSolicitor(caseDetails);
    }

    @Test
    public void shouldEmailIntervenerSolicitorForContestedCase() {
        String intervenerEmailKey = "intervener1SolEmail";
        caseDetails.getData().put(intervenerEmailKey, TEST_SOLICITOR_EMAIL);
        SolicitorCaseDataKeysWrapper dataKeysWrapper = SolicitorCaseDataKeysWrapper.builder().build();
        when(notificationService.getCaseDataKeysForIntervenerOneSolicitor()).thenReturn(dataKeysWrapper);
        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(caseDetails, intervenerEmailKey,
            CaseRole.INTVR_SOLICITOR_1)).thenReturn(true);
        contestedConsentOrderNotApprovedCorresponder.sendCorrespondence(caseDetails);
        verify(notificationService).sendContestedConsentOrderNotApprovedEmailIntervenerSolicitor(caseDetails,
            dataKeysWrapper);
    }
}