package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.updatefrc;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class UpdateFrcCorrespondenceHandlerTest {

    protected static final String AUTH_TOKEN = "authToken";
    UpdateFrcCorrespondenceService updateFrcCorrespondenceService;
    @Mock
    UpdateFrcLetterOrEmailAllSolicitorsCorresponder updateFrcEmailAllLitigantsCorresponder;
    @Mock
    NotificationService notificationService;

    private CaseDetails caseDetails;

    @Before
    public void setUp() throws Exception {
        updateFrcCorrespondenceService =
            new UpdateFrcCorrespondenceService(updateFrcEmailAllLitigantsCorresponder, notificationService);
        caseDetails = CaseDetails.builder().build();
    }

    @Test
    public void shouldSendCorrespondence() throws JsonProcessingException {

        updateFrcCorrespondenceService.sendCorrespondence(caseDetails, AUTH_TOKEN);
        verify(updateFrcEmailAllLitigantsCorresponder).sendCorrespondence(caseDetails, AUTH_TOKEN);
        verify(notificationService).sendUpdateFrcInformationEmailToCourt(caseDetails);

    }
}