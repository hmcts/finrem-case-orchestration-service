package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.draftorders;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.DraftOrdersNotificationRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.OrderParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.RefusedOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefusedOrderCorresponderTest {

    @InjectMocks
    private RefusedOrderCorresponder refusedOrderCorresponder;
    @Mock
    private NotificationService notificationService;
    @Mock
    private DraftOrdersNotificationRequestMapper notificationRequestMapper;
    @Mock
    private DocumentHelper documentHelper;
    @Mock
    private BulkPrintService bulkPrintService;

    @Captor
    private ArgumentCaptor<List<BulkPrintDocument>> applicantBulkPrintDocumentsCaptor;
    @Captor
    private ArgumentCaptor<List<BulkPrintDocument>> respondentBulkPrintDocumentsCaptor;
    @Captor
    private ArgumentCaptor<NotificationRequest> notificationRequestCaptor;

    @Test
    void givenApplicantCorrespondenceDisabled_whenSendRefusedOrder_thenNoCorrespondenceSent() {
        RefusedOrderCorrespondenceRequest request = createRequest(createCaseData(false, true));

        refusedOrderCorresponder.sendRefusedOrder(request);

        verifyNoInteractions(notificationService);
        verifyNoInteractions(bulkPrintService);
    }

    @Test
    void givenRespondentCorrespondenceDisabled_whenSendRefusedOrder_thenNoCorrespondenceSent() {
        RefusedOrderCorrespondenceRequest request = createRequest(createCaseData(true, false));

        refusedOrderCorresponder.sendRefusedOrder(request);

        verifyNoInteractions(notificationService);
        verifyNoInteractions(bulkPrintService);
    }

    @Test
    void givenApplicantSolicitorIsDigital_whenSendRefusedOrder_thenEmailSent() {
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(FinremCaseDetails.class)))
            .thenReturn(true);
        List<RefusedOrder> refusedOrders = List.of(createRefusedOrder(OrderParty.APPLICANT));
        RefusedOrderCorrespondenceRequest request = createRequest(createCaseData(true, true), refusedOrders);
        when(notificationRequestMapper.buildRefusedDraftOrderOrPsaNotificationRequest(request.caseDetails(), refusedOrders.get(0)))
            .thenReturn(NotificationRequest.builder().build());

        refusedOrderCorresponder.sendRefusedOrder(request);

        verify(notificationService, times(1)).sendRefusedDraftOrderOrPsa(any(NotificationRequest.class));
        verifyNoInteractions(bulkPrintService);
    }

    @Test
    void givenRespondentSolicitorIsDigital_whenSendRefusedOrder_thenEmailSent() {
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(any(FinremCaseDetails.class)))
            .thenReturn(true);
        List<RefusedOrder> refusedOrders = List.of(createRefusedOrder(OrderParty.RESPONDENT));
        RefusedOrderCorrespondenceRequest request = createRequest(createCaseData(true, true), refusedOrders);
        when(notificationRequestMapper.buildRefusedDraftOrderOrPsaNotificationRequest(request.caseDetails(), refusedOrders.get(0)))
            .thenReturn(NotificationRequest.builder().build());

        refusedOrderCorresponder.sendRefusedOrder(request);

        verify(notificationService, times(1)).sendRefusedDraftOrderOrPsa(any(NotificationRequest.class));
        verifyNoInteractions(bulkPrintService);
    }

    @Test
    void givenApplicantSolicitorNotDigital_whenSendRefusedOrder_thenLetterSent() {
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(FinremCaseDetails.class)))
            .thenReturn(false);
        when(documentHelper.getBulkPrintDocumentFromCaseDocument(any(CaseDocument.class))).thenCallRealMethod();

        List<RefusedOrder> refusedOrders = List.of(createRefusedOrder(OrderParty.APPLICANT));
        RefusedOrderCorrespondenceRequest request = createRequest(createCaseData(true, true), refusedOrders);

        refusedOrderCorresponder.sendRefusedOrder(request);

        verify(notificationService, never()).sendRefusedDraftOrderOrPsa(any(NotificationRequest.class));
        verify(bulkPrintService, never()).printRespondentDocuments(any(FinremCaseDetails.class), anyString(),
            anyList());
        verify(bulkPrintService, times(1)).printApplicantDocuments(any(FinremCaseDetails.class), anyString(),
            applicantBulkPrintDocumentsCaptor.capture());
        List<BulkPrintDocument> bulkPrintDocuments = applicantBulkPrintDocumentsCaptor.getValue();
        assertThat(bulkPrintDocuments).hasSize(1);
    }

    @Test
    void givenRespondentSolicitorNotDigital_whenSendRefusedOrder_thenLetterSent() {
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(any(FinremCaseDetails.class)))
            .thenReturn(false);
        when(documentHelper.getBulkPrintDocumentFromCaseDocument(any(CaseDocument.class))).thenCallRealMethod();

        List<RefusedOrder> refusedOrders = List.of(createRefusedOrder(OrderParty.RESPONDENT));
        RefusedOrderCorrespondenceRequest request = createRequest(createCaseData(true, true), refusedOrders);

        refusedOrderCorresponder.sendRefusedOrder(request);

        verify(notificationService, never()).sendRefusedDraftOrderOrPsa(any(NotificationRequest.class));
        verify(bulkPrintService, never()).printApplicantDocuments(any(FinremCaseDetails.class), anyString(),
            anyList());
        verify(bulkPrintService, times(1)).printRespondentDocuments(any(FinremCaseDetails.class), anyString(),
            respondentBulkPrintDocumentsCaptor.capture());

        List<BulkPrintDocument> bulkPrintDocuments = respondentBulkPrintDocumentsCaptor.getValue();
        assertThat(bulkPrintDocuments).hasSize(1);
    }

    @Test
    void givenSolicitorsDigitalAndMultipleRefusedOrders_whenSendRefusedOrder_thenMultipleEmailsSent() {
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(FinremCaseDetails.class)))
            .thenReturn(true);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(any(FinremCaseDetails.class)))
            .thenReturn(true);
        List<RefusedOrder> refusedOrders = List.of(
            createRefusedOrder(OrderParty.RESPONDENT),
            createRefusedOrder(OrderParty.APPLICANT),
            createRefusedOrder(OrderParty.APPLICANT),
            createRefusedOrder(OrderParty.RESPONDENT));
        RefusedOrderCorrespondenceRequest request = createRequest(createCaseData(true, true), refusedOrders);
        when(notificationRequestMapper.buildRefusedDraftOrderOrPsaNotificationRequest(request.caseDetails(), refusedOrders.get(0)))
            .thenReturn(createNotificationRequest("respondent1"));
        when(notificationRequestMapper.buildRefusedDraftOrderOrPsaNotificationRequest(request.caseDetails(), refusedOrders.get(1)))
            .thenReturn(createNotificationRequest("applicant1"));
        when(notificationRequestMapper.buildRefusedDraftOrderOrPsaNotificationRequest(request.caseDetails(), refusedOrders.get(2)))
            .thenReturn(createNotificationRequest("applicant2"));
        when(notificationRequestMapper.buildRefusedDraftOrderOrPsaNotificationRequest(request.caseDetails(), refusedOrders.get(3)))
            .thenReturn(createNotificationRequest("respondent2"));

        refusedOrderCorresponder.sendRefusedOrder(request);

        verify(notificationService, times(4)).sendRefusedDraftOrderOrPsa(notificationRequestCaptor.capture());
        List<NotificationRequest> notificationRequests = notificationRequestCaptor.getAllValues();
        assertThat(notificationRequests).filteredOn(nr -> "applicant1".equals(nr.getNotificationEmail())).hasSize(1);
        assertThat(notificationRequests).filteredOn(nr -> "applicant2".equals(nr.getNotificationEmail())).hasSize(1);
        assertThat(notificationRequests).filteredOn(nr -> "respondent1".equals(nr.getNotificationEmail())).hasSize(1);
        assertThat(notificationRequests).filteredOn(nr -> "respondent2".equals(nr.getNotificationEmail())).hasSize(1);

        verifyNoInteractions(bulkPrintService);
    }

    @Test
    void givenSolicitorsNotDigitalAndMultipleRefusedOrders_whenSendRefusedOrder_thenMultiDocumentLetterSent() {
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(FinremCaseDetails.class)))
            .thenReturn(false);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(any(FinremCaseDetails.class)))
            .thenReturn(false);
        List<RefusedOrder> refusedOrders = List.of(
            createRefusedOrder(OrderParty.RESPONDENT),
            createRefusedOrder(OrderParty.APPLICANT),
            createRefusedOrder(OrderParty.APPLICANT),
            createRefusedOrder(OrderParty.RESPONDENT));
        RefusedOrderCorrespondenceRequest request = createRequest(createCaseData(true, true), refusedOrders);

        refusedOrderCorresponder.sendRefusedOrder(request);

        verify(notificationService, never()).sendRefusedDraftOrderOrPsa(any(NotificationRequest.class));
        verify(bulkPrintService, times(1)).printApplicantDocuments(any(FinremCaseDetails.class), anyString(),
            applicantBulkPrintDocumentsCaptor.capture());
        verify(bulkPrintService, times(1)).printRespondentDocuments(any(FinremCaseDetails.class), anyString(),
            respondentBulkPrintDocumentsCaptor.capture());

        assertThat(applicantBulkPrintDocumentsCaptor.getValue()).hasSize(2);
        assertThat(respondentBulkPrintDocumentsCaptor.getValue()).hasSize(2);
    }

    private RefusedOrderCorrespondenceRequest createRequest(FinremCaseData caseData) {
        return createRequest(caseData, Collections.emptyList());
    }

    private RefusedOrderCorrespondenceRequest createRequest(FinremCaseData caseData, List<RefusedOrder> refusedOrders) {
        return RefusedOrderCorrespondenceRequest.builder()
            .caseDetails(FinremCaseDetails.builder().data(caseData).build())
            .refusedOrders(refusedOrders)
            .authorisationToken(TestConstants.AUTH_TOKEN)
            .build();
    }

    private FinremCaseData createCaseData(boolean applicantCorrespondenceEnabled, boolean respondentCorrespondenceEnabled) {
        return FinremCaseData.builder()
            .applicantCorrespondenceEnabled(applicantCorrespondenceEnabled)
            .respondentCorrespondenceEnabled(respondentCorrespondenceEnabled)
            .build();
    }

    private RefusedOrder createRefusedOrder(OrderParty orderParty) {
        return RefusedOrder.builder()
            .refusedDate(LocalDateTime.now())
            .refusalOrder(CaseDocument.builder().build())
            .orderParty(orderParty)
            .build();
    }

    private NotificationRequest createNotificationRequest(String email) {
        return NotificationRequest.builder().notificationEmail(email).build();
    }
}
