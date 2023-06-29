package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.issueapplication;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IssueApplicationContestedEmailCorresponderTest {

    @InjectMocks
    private IssueApplicationContestedEmailCorresponder corresponder;

    @Mock
    NotificationService notificationService;

    private FinremCaseDetails caseDetails;

    @BeforeEach
    public void setUp() throws Exception {
        caseDetails = FinremCaseDetails.builder().build();
    }

    @Test
    void shouldEmailApplicant() {
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        corresponder.sendCorrespondence(caseDetails);
        verify(notificationService).isApplicantSolicitorDigitalAndEmailPopulated(caseDetails);
        verify(notificationService).sendContestedApplicationIssuedEmailToApplicantSolicitor(caseDetails);
    }

    @Test
    void shouldNotEmailApplicant() {
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        corresponder.sendCorrespondence(caseDetails);
        verify(notificationService).isApplicantSolicitorDigitalAndEmailPopulated(caseDetails);
        verifyNoMoreInteractions(notificationService);
    }
}