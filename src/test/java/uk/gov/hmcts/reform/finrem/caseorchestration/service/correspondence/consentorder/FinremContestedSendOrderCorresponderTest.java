package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrderSentToPartiesCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.SendOrderDocuments;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOne;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.SolicitorCaseDataKeysWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@ExtendWith(MockitoExtension.class)
class FinremContestedSendOrderCorresponderTest {

    @InjectMocks
    private FinremContestedSendOrderCorresponder corresponder;
    @Mock
    private NotificationService notificationService;
    @Mock
    private BulkPrintService bulkPrintService;
    @Mock
    private DocumentHelper documentHelper;


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
        data.getIntervenerTwo().setIntervenerCorrespondenceEnabled(false);
        data.getIntervenerThree().setIntervenerCorrespondenceEnabled(false);
        data.getIntervenerFour().setIntervenerCorrespondenceEnabled(false);
        IntervenerWrapper wrapper = data.getIntervenerOne();
        SolicitorCaseDataKeysWrapper dataKeysWrapper = SolicitorCaseDataKeysWrapper.builder().build();
        when(notificationService.getCaseDataKeysForIntervenerSolicitor(any(IntervenerWrapper.class))).thenReturn(dataKeysWrapper);
        corresponder.emailIntervenerSolicitor(wrapper, caseDetails);
        verify(notificationService).sendContestOrderApprovedEmailIntervener(caseDetails, dataKeysWrapper, IntervenerType.INTERVENER_ONE);
    }

    @Test
    void emailIntervener2Solicitor() {
        FinremCaseData data = caseDetails.getData();
        data.getIntervenerOne().setIntervenerCorrespondenceEnabled(false);
        data.getIntervenerThree().setIntervenerCorrespondenceEnabled(false);
        data.getIntervenerFour().setIntervenerCorrespondenceEnabled(false);
        IntervenerWrapper wrapper = data.getIntervenerTwo();
        SolicitorCaseDataKeysWrapper dataKeysWrapper = SolicitorCaseDataKeysWrapper.builder().solicitorEmailKey(TEST_SOLICITOR_EMAIL).build();
        when(notificationService.getCaseDataKeysForIntervenerSolicitor(any(IntervenerWrapper.class))).thenReturn(dataKeysWrapper);
        corresponder.emailIntervenerSolicitor(wrapper, caseDetails);
        verify(notificationService).sendContestOrderApprovedEmailIntervener(caseDetails, dataKeysWrapper, IntervenerType.INTERVENER_TWO);
    }

    @Test
    void emailIntervener3Solicitor() {
        FinremCaseData data = caseDetails.getData();
        data.getIntervenerTwo().setIntervenerCorrespondenceEnabled(false);
        data.getIntervenerOne().setIntervenerCorrespondenceEnabled(false);
        data.getIntervenerFour().setIntervenerCorrespondenceEnabled(false);
        IntervenerWrapper wrapper = data.getIntervenerThree();
        SolicitorCaseDataKeysWrapper dataKeysWrapper = SolicitorCaseDataKeysWrapper.builder().solicitorEmailKey(TEST_SOLICITOR_EMAIL).build();
        when(notificationService.getCaseDataKeysForIntervenerSolicitor(any(IntervenerWrapper.class))).thenReturn(dataKeysWrapper);
        corresponder.emailIntervenerSolicitor(wrapper, caseDetails);
        verify(notificationService).sendContestOrderApprovedEmailIntervener(caseDetails, dataKeysWrapper, IntervenerType.INTERVENER_THREE);
    }

    @Test
    void emailIntervener4Solicitor() {
        FinremCaseData data = caseDetails.getData();
        data.getIntervenerTwo().setIntervenerCorrespondenceEnabled(false);
        data.getIntervenerThree().setIntervenerCorrespondenceEnabled(false);
        data.getIntervenerOne().setIntervenerCorrespondenceEnabled(false);
        IntervenerWrapper wrapper = data.getIntervenerFour();
        wrapper.setIntervenerEmail(TEST_SOLICITOR_EMAIL);
        SolicitorCaseDataKeysWrapper dataKeysWrapper
            = SolicitorCaseDataKeysWrapper.builder().solicitorEmailKey(wrapper.getIntervenerEmail()).build();
        when(notificationService.getCaseDataKeysForIntervenerSolicitor(any(IntervenerWrapper.class))).thenReturn(dataKeysWrapper);
        corresponder.emailIntervenerSolicitor(wrapper, caseDetails);
        verify(notificationService).sendContestOrderApprovedEmailIntervener(caseDetails, dataKeysWrapper, IntervenerType.INTERVENER_FOUR);
    }

    @Test
    void getDocumentsToPrint() {
        List<OrderSentToPartiesCollection> orders = new ArrayList<>();
        orders.add(OrderSentToPartiesCollection.builder().value(SendOrderDocuments.builder().caseDocument(caseDocument()).build()).build());
        caseDetails.getData().setOrdersSentToPartiesCollection(orders);
        List<CaseDocument> documentsToPrint = corresponder.getCaseDocuments(caseDetails);
        assertEquals(1, documentsToPrint.size());
    }


    @Test
    void shouldSendLettersToInterveners() {
        IntervenerOne intervenerOne = IntervenerOne.builder()
            .intervenerName("Intervener 1")
            .intervenerEmail("Intervener email")
            .intervenerCorrespondenceEnabled(Boolean.TRUE)
            .build();

        caseDetails.getData().setIntervenerOne(intervenerOne);

        List<OrderSentToPartiesCollection> orders = new ArrayList<>();
        orders.add(OrderSentToPartiesCollection.builder().value(SendOrderDocuments.builder().caseDocument(caseDocument()).build()).build());
        caseDetails.getData().setOrdersSentToPartiesCollection(orders);

        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(any(IntervenerOne.class),
            any(FinremCaseDetails.class))).thenReturn(false);

        corresponder.sendCorrespondence(caseDetails, "authToken");

        verify(notificationService).isIntervenerSolicitorDigitalAndEmailPopulated(intervenerOne, caseDetails);
        verify(bulkPrintService).printIntervenerDocuments(any(IntervenerOne.class), any(FinremCaseDetails.class),
            anyString(), anyList());
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
