package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOne;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.SolicitorCaseDataKeysWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsentOrderNotApprovedCorresponderTest {

    @InjectMocks
    private ConsentOrderNotApprovedCorresponder consentOrderNotApprovedCorresponder;

    @Mock
    private NotificationService notificationService;

    @Mock
    private FinremCaseDetails caseDetails;

    @Test
    void givenConsentedCaseWithDigitalApplicantSolicitor_whenSendCorrespondence_thenEmailApplicantSolicitor() {
        when(caseDetails.isConsentedApplication()).thenReturn(true);
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);

        consentOrderNotApprovedCorresponder.sendCorrespondence(caseDetails);

        verify(notificationService).sendConsentOrderNotApprovedEmailToApplicantSolicitor(caseDetails);
    }

    @Test
    void givenContestedCaseWithDigitalApplicantSolicitor_whenSendCorrespondence_thenEmailApplicantSolicitor() {
        when(caseDetails.isConsentedApplication()).thenReturn(false);
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);

        consentOrderNotApprovedCorresponder.sendCorrespondence(caseDetails);

        verify(notificationService).sendContestOrderNotApprovedEmailApplicant(caseDetails);
    }

    @Test
    void givenConsentedCaseWithDigitalRespondentSolicitor_whenSendCorrespondence_thenEmailRespondentSolicitor() {
        when(caseDetails.isConsentedApplication()).thenReturn(true);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);

        consentOrderNotApprovedCorresponder.sendCorrespondence(caseDetails);

        verify(notificationService).sendConsentOrderNotApprovedEmailToRespondentSolicitor(caseDetails);
    }

    @Test
    void givenContestedCaseWithDigitalRespondentSolicitor_whenSendCorrespondence_thenEmailRespondentSolicitor() {
        when(caseDetails.isConsentedApplication()).thenReturn(false);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);

        consentOrderNotApprovedCorresponder.sendCorrespondence(caseDetails);

        verify(notificationService).sendContestOrderNotApprovedEmailRespondent(caseDetails);
    }

    @Test
    void shouldEmailIntervenerSolicitorForContestedCase() {
        IntervenerOne intervenerOne = IntervenerOne.builder().build();

        when(caseDetails.getData()).thenReturn(FinremCaseData.builder().intervenerOne(intervenerOne).build());
        SolicitorCaseDataKeysWrapper dataKeysWrapper = mock(SolicitorCaseDataKeysWrapper.class);
        when(caseDetails.isContestedApplication()).thenReturn(true);

        when(notificationService.getCaseDataKeysForIntervenerSolicitor(any(IntervenerWrapper.class))).thenReturn(dataKeysWrapper);
        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(same(intervenerOne), eq(caseDetails))).thenReturn(true);
        consentOrderNotApprovedCorresponder.sendCorrespondence(caseDetails);

        verify(notificationService).sendContestOrderNotApprovedEmailIntervener(caseDetails,
            dataKeysWrapper);
    }
}
