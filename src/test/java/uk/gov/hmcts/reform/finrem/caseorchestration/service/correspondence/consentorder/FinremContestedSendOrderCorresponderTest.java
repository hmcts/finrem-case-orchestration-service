package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.SolicitorCaseDataKeysWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@ExtendWith(MockitoExtension.class)
class FinremContestedSendOrderCorresponderTest {

    @InjectMocks
    private FinremContestedSendOrderCorresponder corresponder;
    @Mock
    private NotificationService notificationService;
    @Mock
    private BulkPrintService bulkPrintService;


    private FinremCaseDetails caseDetails;

    @BeforeEach
    void setUp() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        caseDetails = finremCallbackRequest.getCaseDetails();
    }

    @Test
    void emailApplicantSolicitor() {
        corresponder.emailApplicantSolicitor(caseDetails);
        verify(notificationService).sendContestOrderApprovedEmailApplicant(caseDetails);
    }

    @Test
    void emailRespondentSolicitor() {
        corresponder.emailRespondentSolicitor(caseDetails);
        verify(notificationService).sendContestOrderApprovedEmailRespondent(caseDetails);
    }

    @Test
    void emailIntervener1Solicitor() {
        FinremCaseData data = caseDetails.getData();
        data.setIntervener2CorrespondenceEnabled(false);
        data.setIntervener3CorrespondenceEnabled(false);
        data.setIntervener4CorrespondenceEnabled(false);
        IntervenerWrapper wrapper = data.getIntervenerOneWrapper();
        SolicitorCaseDataKeysWrapper dataKeysWrapper = SolicitorCaseDataKeysWrapper.builder().build();
        when(notificationService.getCaseDataKeysForIntervenerSolicitor(any(IntervenerWrapper.class))).thenReturn(dataKeysWrapper);
        corresponder.emailIntervenerSolicitor(wrapper, caseDetails);
        verify(notificationService).sendContestOrderApprovedEmailIntervener(caseDetails, dataKeysWrapper);
    }

    @Test
    void emailIntervener2Solicitor() {
        FinremCaseData data = caseDetails.getData();
        data.setIntervener1CorrespondenceEnabled(false);
        data.setIntervener3CorrespondenceEnabled(false);
        data.setIntervener4CorrespondenceEnabled(false);
        IntervenerWrapper wrapper = data.getIntervenerTwoWrapper();
        SolicitorCaseDataKeysWrapper dataKeysWrapper = SolicitorCaseDataKeysWrapper.builder().solicitorEmailKey(TEST_SOLICITOR_EMAIL).build();
        when(notificationService.getCaseDataKeysForIntervenerSolicitor(any(IntervenerWrapper.class))).thenReturn(dataKeysWrapper);
        corresponder.emailIntervenerSolicitor(wrapper, caseDetails);
        verify(notificationService).sendContestOrderApprovedEmailIntervener(caseDetails, dataKeysWrapper);
    }

    @Test
    void emailIntervener3Solicitor() {
        FinremCaseData data = caseDetails.getData();
        data.setIntervener1CorrespondenceEnabled(false);
        data.setIntervener2CorrespondenceEnabled(false);
        data.setIntervener4CorrespondenceEnabled(false);
        IntervenerWrapper wrapper = data.getIntervenerThreeWrapper();
        SolicitorCaseDataKeysWrapper dataKeysWrapper = SolicitorCaseDataKeysWrapper.builder().solicitorEmailKey(TEST_SOLICITOR_EMAIL).build();
        when(notificationService.getCaseDataKeysForIntervenerSolicitor(any(IntervenerWrapper.class))).thenReturn(dataKeysWrapper);
        corresponder.emailIntervenerSolicitor(wrapper, caseDetails);
        verify(notificationService).sendContestOrderApprovedEmailIntervener(caseDetails, dataKeysWrapper);
    }

    @Test
    void emailIntervener4Solicitor() {
        FinremCaseData data = caseDetails.getData();
        data.setIntervener1CorrespondenceEnabled(false);
        data.setIntervener2CorrespondenceEnabled(false);
        data.setIntervener3CorrespondenceEnabled(false);
        IntervenerWrapper wrapper = data.getIntervenerFourWrapper();
        SolicitorCaseDataKeysWrapper dataKeysWrapper = SolicitorCaseDataKeysWrapper.builder().solicitorEmailKey(TEST_SOLICITOR_EMAIL).build();
        when(notificationService.getCaseDataKeysForIntervenerSolicitor(any(IntervenerWrapper.class))).thenReturn(dataKeysWrapper);
        corresponder.emailIntervenerSolicitor(wrapper, caseDetails);
        verify(notificationService).sendContestOrderApprovedEmailIntervener(caseDetails, dataKeysWrapper);
    }

    @Test
    void getDocumentsToPrint() {
        when(bulkPrintService.getBulkPrintDocuments(any()))
            .thenReturn(List.of(BulkPrintDocument.builder().fileName("abc.pdf").binaryFileUrl("binary").build()));
        List<BulkPrintDocument> documentsToPrint = corresponder.getDocumentsToPrint(caseDetails);
        assertEquals(1, documentsToPrint.size());
    }

    private FinremCallbackRequest buildCallbackRequest() {
        return FinremCallbackRequest
            .builder()
            .eventType(EventType.SEND_ORDER)
            .caseDetailsBefore(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(new FinremCaseData()).build())
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(new FinremCaseData()).build())
            .build();
    }
}