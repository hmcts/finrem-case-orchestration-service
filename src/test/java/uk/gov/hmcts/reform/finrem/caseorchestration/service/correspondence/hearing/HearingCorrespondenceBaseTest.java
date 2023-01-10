package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing;

import org.junit.Test;
import org.mockito.Mock;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.MultiLetterOrEmailAllPartiesCorresponder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public abstract class HearingCorrespondenceBaseTest {

    @Mock
    NotificationService notificationService;
    @Mock
    BulkPrintService bulkPrintService;

    @Mock
    DocumentHelper documentHelper;
    CaseDetails caseDetails;
    MultiLetterOrEmailAllPartiesCorresponder applicantAndRespondentMultiLetterCorresponder;


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

        verify(bulkPrintService).printRespondentDocuments(any(CaseDetails.class), anyString(), anyList());
    }

    @Test
    public void shouldSendLettersToApplicantAndEmailToRespondent() {

        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);

        applicantAndRespondentMultiLetterCorresponder.sendCorrespondence(caseDetails, "authToken");

        verify(bulkPrintService).printApplicantDocuments(any(CaseDetails.class), anyString(), anyList());
        verify(notificationService).sendPrepareForHearingEmailRespondent(caseDetails);
    }


    @Test
    public void shouldEmailToApplicantAndSendLetterToRespondent() {

        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);

        applicantAndRespondentMultiLetterCorresponder.sendCorrespondence(caseDetails, "authToken");

        verify(bulkPrintService).printRespondentDocuments(any(CaseDetails.class), anyString(), anyList());
        verify(notificationService).sendPrepareForHearingEmailApplicant(caseDetails);
    }

    protected BulkPrintDocument getBulkPrintDocument() {
        return BulkPrintDocument.builder().build();
    }
}
