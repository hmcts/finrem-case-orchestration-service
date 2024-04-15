package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.generalorder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFour;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOne;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThree;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.SolicitorCaseDataKeysWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinremGeneralOrderRaisedConsentInContestedCorresponderTest {

    @InjectMocks
    private FinremGeneralOrderRaisedConsentInContestedCorresponder corresponder;

    @Mock
    private NotificationService notificationService;

    private FinremCaseData caseData;
    private FinremCaseDetails caseDetails;
    private SolicitorCaseDataKeysWrapper solicitorCaseDataKeysWrapper;

    @BeforeEach
    void setup() {
        IntervenerOne intervenerOne = IntervenerOne.builder().build();
        IntervenerTwo intervenerTwo = IntervenerTwo.builder().build();
        IntervenerThree intervenerThree = IntervenerThree.builder().build();
        IntervenerFour intervenerFour = IntervenerFour.builder().build();
        caseData = FinremCaseData.builder().intervenerOne(intervenerOne)
            .intervenerTwo(intervenerTwo).intervenerThree(intervenerThree)
            .intervenerFour(intervenerFour).build();
        caseDetails = FinremCaseDetails.builder().data(caseData).build();
        solicitorCaseDataKeysWrapper = SolicitorCaseDataKeysWrapper.builder().build();
    }

    @Test
    void shouldNotSendGeneralOrderEmail() {
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);

        corresponder.sendCorrespondence(caseDetails);

        verify(notificationService, never()).sendContestedConsentGeneralOrderEmailRespondentSolicitor(caseDetails);
    }

    @Test
    void shouldSendContestedConsentGeneralOrderEmailToRespondentInConsentedInContestedCase() {
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);

        corresponder.sendCorrespondence(caseDetails);

        verify(notificationService).sendContestedConsentGeneralOrderEmailRespondentSolicitor(caseDetails);
        verify(notificationService, never()).sendContestedGeneralOrderEmailRespondent(caseDetails);
        verify(notificationService, never()).sendConsentedGeneralOrderEmailToRespondentSolicitor(caseDetails);
    }

    @Test
    void shouldSendContestedConsentGeneralOrderEmail() {
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        corresponder.sendCorrespondence(caseDetails);

        verify(notificationService).sendContestedConsentGeneralOrderEmailApplicantSolicitor(caseDetails);
    }

    @Test
    void shouldNotSendContestedGeneralOrderEmailToIntervener_ThenTheEmailIsNotIssued() {
        corresponder.sendCorrespondence(caseDetails);

        verify(notificationService, never()).sendContestedConsentGeneralOrderEmailIntervenerSolicitor(any(FinremCaseDetails.class),
            any(SolicitorCaseDataKeysWrapper.class));
    }

    @Test
    void whenGeneralOrderIsRaised_thenShouldSendContestedGeneralOrderEmailToInterveners() {
        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(any(IntervenerWrapper.class),
            any(FinremCaseDetails.class))).thenReturn(true);
        when(notificationService.getCaseDataKeysForIntervenerSolicitor(any(IntervenerWrapper.class))).thenReturn(solicitorCaseDataKeysWrapper);
        corresponder.sendCorrespondence(caseDetails);

        verify(notificationService,  times(4))
            .sendContestedConsentGeneralOrderEmailIntervenerSolicitor(any(FinremCaseDetails.class),
            any(SolicitorCaseDataKeysWrapper.class));
    }
}
