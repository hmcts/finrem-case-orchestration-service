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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrderSentToPartiesCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.SendOrderDocuments;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ConsentOrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOneWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderApprovedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderNotApprovedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@ExtendWith(MockitoExtension.class)
class FinremConsentInContestedSendOrderCorresponderTest {
    private static final String AUTH_TOKEN = "tolkien";

    @InjectMocks
    private FinremConsentInContestedSendOrderCorresponder corresponder;
    @Mock
    private NotificationService notificationService;
    @Mock
    private BulkPrintService bulkPrintService;
    @Mock
    private DocumentHelper documentHelper;
    @Mock
    private ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;

    @Mock
    private ConsentOrderNotApprovedDocumentService consentOrderNotApprovedDocumentService;
    private FinremCaseDetails caseDetails;

    @BeforeEach
    void setUp() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        caseDetails = finremCallbackRequest.getCaseDetails();
    }

    @Test
    void givenConsentLettersToSend_whenUnapprovedOrderModifiedLatest_thenShouldGetDocumentsToPrint() {
        List<OrderSentToPartiesCollection> orders = new ArrayList<>();
        orders.add(OrderSentToPartiesCollection.builder().value(SendOrderDocuments.builder().caseDocument(caseDocument()).build()).build());
        when(consentOrderApprovedDocumentService.getApprovedOrderModifiedAfterNotApprovedOrder(any(), any())).thenReturn(false);
        caseDetails.getData().setOrdersSentToPartiesCollection(orders);
        List<BulkPrintDocument> documentsToPrint = corresponder.getDocumentsToPrint(
            caseDetails, AUTH_TOKEN, DocumentHelper.PaperNotificationRecipient.INTERVENER_ONE);
        assertEquals(1, documentsToPrint.size());
        verify(consentOrderNotApprovedDocumentService).addNotApprovedConsentCoverLetter(any(FinremCaseDetails.class), anyList(), any(), any());
    }

    @Test
    void givenConsentLettersToSend_whenApprovedOrderModifiedLatest_thenShouldGetDocumentsToPrint() {
        List<OrderSentToPartiesCollection> orders = new ArrayList<>();
        orders.add(OrderSentToPartiesCollection.builder().value(SendOrderDocuments.builder().caseDocument(caseDocument()).build()).build());
        when(consentOrderApprovedDocumentService.getApprovedOrderModifiedAfterNotApprovedOrder(any(), any())).thenReturn(true);
        caseDetails.getData().setOrdersSentToPartiesCollection(orders);
        List<BulkPrintDocument> documentsToPrint = corresponder.getDocumentsToPrint(
            caseDetails, AUTH_TOKEN, DocumentHelper.PaperNotificationRecipient.INTERVENER_ONE);
        assertEquals(1, documentsToPrint.size());
        verify(consentOrderApprovedDocumentService).addApprovedConsentCoverLetter(any(), any(), any(), any());
    }

    @Test
    void shouldSendLettersToParties() {
        IntervenerOneWrapper intervenerOneWrapper = IntervenerOneWrapper.builder()
            .intervenerName("Intervener 1")
            .intervenerEmail("intervener1@gmail.com")
            .intervenerCorrespondenceEnabled(Boolean.TRUE)
            .build();

        caseDetails.getData().setIntervenerOneWrapper(intervenerOneWrapper);

        List<OrderSentToPartiesCollection> orders = new ArrayList<>();
        orders.add(OrderSentToPartiesCollection.builder().value(SendOrderDocuments.builder().caseDocument(caseDocument()).build()).build());
        caseDetails.getData().setOrdersSentToPartiesCollection(orders);

        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(any(IntervenerOneWrapper.class),
            any(FinremCaseDetails.class))).thenReturn(false);
        when(consentOrderApprovedDocumentService.getApprovedOrderModifiedAfterNotApprovedOrder(any(ConsentOrderWrapper.class), anyString()))
            .thenReturn(true);
        corresponder.sendCorrespondence(caseDetails, "authToken");

        verify(notificationService).isIntervenerSolicitorDigitalAndEmailPopulated(intervenerOneWrapper, caseDetails);
        verify(notificationService).isApplicantSolicitorDigitalAndEmailPopulated(caseDetails);
        verify(notificationService).isRespondentSolicitorDigitalAndEmailPopulated(caseDetails);
        verify(bulkPrintService).printIntervenerDocuments(any(IntervenerOneWrapper.class), any(FinremCaseDetails.class),
            anyString(), anyList());
        verify(bulkPrintService).printApplicantDocuments(any(FinremCaseDetails.class),
            anyString(), anyList());
        verify(bulkPrintService).printRespondentDocuments(any(FinremCaseDetails.class),
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
