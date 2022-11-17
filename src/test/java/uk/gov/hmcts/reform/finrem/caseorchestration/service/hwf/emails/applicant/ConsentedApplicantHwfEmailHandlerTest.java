package uk.gov.hmcts.reform.finrem.caseorchestration.service.hwf.emails.applicant;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.CheckApplicantSolicitorIsDigitalService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConsentedApplicantHwfEmailHandlerTest {

    ConsentedApplicantHwfEmailHandler consentedApplicantHwfEmailHandler;
    @Mock
    private CaseDataService caseDateService;
    @Mock
    private CheckApplicantSolicitorIsDigitalService checkApplicantSolicitorIsDigitalService;
    @Mock
    private NotificationService notificationService;

    @Before
    public void setUpTest() {
        consentedApplicantHwfEmailHandler =
            new ConsentedApplicantHwfEmailHandler(caseDateService, checkApplicantSolicitorIsDigitalService, notificationService);
    }

    @Test
    public void shouldSendNotification() {
        consentedApplicantHwfEmailHandler.sendNotification(CaseDetails.builder().build(), "authToken");
        verify(notificationService).sendConsentedHWFSuccessfulConfirmationEmail(CaseDetails.builder().build());
    }

    @Test
    public void shouldNotHandleWhenContestedApplication() {
        CaseDetails caseDetails = CaseDetails.builder().build();
        when(caseDateService.isConsentedApplication(caseDetails)).thenReturn(false);
        boolean result = consentedApplicantHwfEmailHandler.canHandle(caseDetails);
        assertThat(result, is(false));
    }
}