package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.updatefrc;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PaperNotificationService;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class UpdateFrcCorrespondenceServiceTest {

    protected static final String AUTH_TOKEN = "authToken";
    UpdateFrcCorrespondenceService updateFrcCorrespondenceService;
    @Mock
    UpdateFrcEmailAllLitigantsCorresponder updateFrcEmailAllLitigantsCorresponder;
    @Mock
    NotificationService notificationService;
    @Mock
    PaperNotificationService paperNotificationService;

    private CaseDetails caseDetails;

    @Before
    public void setUp() throws Exception {
        updateFrcCorrespondenceService =
            new UpdateFrcCorrespondenceService(updateFrcEmailAllLitigantsCorresponder, notificationService, paperNotificationService);
        caseDetails = CaseDetails.builder().build();
    }

    @Test
    public void shouldSendCorrespondence() throws JsonProcessingException {

        updateFrcCorrespondenceService.sendCorrespondence(caseDetails, AUTH_TOKEN);

        verify(updateFrcEmailAllLitigantsCorresponder).sendEmails(caseDetails);
        verify(notificationService).sendUpdateFrcInformationEmailToCourt(caseDetails);
        verify(paperNotificationService).printUpdateFrcInformationNotification(caseDetails, AUTH_TOKEN);
    }
}