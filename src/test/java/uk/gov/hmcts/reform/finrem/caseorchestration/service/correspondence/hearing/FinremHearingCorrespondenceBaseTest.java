package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing;

import org.junit.Test;
import org.mockito.Mock;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOneWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.SolicitorCaseDataKeysWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.FinremMultiLetterOrEmailAllPartiesCorresponder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public abstract class FinremHearingCorrespondenceBaseTest {

    @Mock
    NotificationService notificationService;
    @Mock
    BulkPrintService bulkPrintService;

    @Mock
    DocumentHelper documentHelper;
    FinremCaseDetails caseDetails;
    FinremMultiLetterOrEmailAllPartiesCorresponder applicantAndRespondentMultiLetterCorresponder;


    @Test
    public void shouldEmailApplicantAndRespondent() {

        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);

        applicantAndRespondentMultiLetterCorresponder.sendCorrespondence(caseDetails, "authToken");

        verify(notificationService).sendPrepareForHearingEmailRespondent(caseDetails);
        verify(notificationService).sendPrepareForHearingEmailApplicant(caseDetails);
        verifyNoInteractions(bulkPrintService);
    }

    @Test
    public void shouldSendLettersToApplicantAndRespondent() {

        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);

        applicantAndRespondentMultiLetterCorresponder.sendCorrespondence(caseDetails, "authToken");

        verify(bulkPrintService).printRespondentDocuments(any(FinremCaseDetails.class), anyString(), anyList());
    }

    @Test
    public void shouldSendLettersToApplicantAndEmailToRespondent() {

        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);

        applicantAndRespondentMultiLetterCorresponder.sendCorrespondence(caseDetails, "authToken");

        verify(bulkPrintService).printApplicantDocuments(any(FinremCaseDetails.class), anyString(), anyList());
        verify(notificationService).sendPrepareForHearingEmailRespondent(caseDetails);
    }


    @Test
    public void shouldEmailToApplicantAndSendLetterToRespondent() {

        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);

        applicantAndRespondentMultiLetterCorresponder.sendCorrespondence(caseDetails, "authToken");

        verify(bulkPrintService).printRespondentDocuments(any(FinremCaseDetails.class), anyString(), anyList());
        verify(notificationService).sendPrepareForHearingEmailApplicant(caseDetails);
    }

    @Test
    public void shouldEmailInterveners() {

        IntervenerOneWrapper intervenerOneWrapper = IntervenerOneWrapper.builder()
            .intervenerName("Intervener 1")
            .intervenerEmail("Intervener email")
            .build();

        caseDetails.getData().setIntervenerOneWrapper(intervenerOneWrapper);
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(any(IntervenerOneWrapper.class),
            any(FinremCaseDetails.class))).thenReturn(true);
        when(notificationService.getFinremCaseDataKeysForIntervenerSolicitor(intervenerOneWrapper))
            .thenReturn(SolicitorCaseDataKeysWrapper.builder().build());

        applicantAndRespondentMultiLetterCorresponder.sendCorrespondence(caseDetails, "authToken");

        verify(notificationService).sendPrepareForHearingEmailRespondent(caseDetails);
        verify(notificationService).sendPrepareForHearingEmailApplicant(caseDetails);
        verify(notificationService).sendPrepareForHearingEmailIntervener(any(FinremCaseDetails.class), any(SolicitorCaseDataKeysWrapper.class));
        verifyNoInteractions(bulkPrintService);
    }


    @Test
    public void shouldSendLettersToInterveners() {

        IntervenerOneWrapper intervenerOneWrapper = IntervenerOneWrapper.builder()
            .intervenerName("Intervener 1")
            .intervenerEmail("Intervener email")
            .build();

        caseDetails.getData().setIntervenerOneWrapper(intervenerOneWrapper);

        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        FinremCaseDetails finremCaseDetails =
            FinremCaseDetails.builder().data(FinremCaseData.builder().intervenerOneWrapper(intervenerOneWrapper).build()).build();
        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(any(IntervenerOneWrapper.class),
            any(FinremCaseDetails.class))).thenReturn(false);

        applicantAndRespondentMultiLetterCorresponder.sendCorrespondence(caseDetails, "authToken");

        verify(notificationService).sendPrepareForHearingEmailRespondent(caseDetails);
        verify(notificationService).sendPrepareForHearingEmailApplicant(caseDetails);
        verify(bulkPrintService).printIntervenerDocuments(any(IntervenerOneWrapper.class), any(FinremCaseDetails.class), anyString(), anyList());
    }


    protected BulkPrintDocument getBulkPrintDocument() {
        return BulkPrintDocument.builder().build();
    }
}
