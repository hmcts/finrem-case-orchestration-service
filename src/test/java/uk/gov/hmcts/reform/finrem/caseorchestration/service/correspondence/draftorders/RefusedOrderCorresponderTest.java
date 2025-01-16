package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.draftorders;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.OrderFiledBy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.RefusedOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFour;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOne;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThree;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
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
    private ArgumentCaptor<List<BulkPrintDocument>> intervenerBulkPrintDocumentsCaptor;
    @Captor
    private ArgumentCaptor<NotificationRequest> notificationRequestCaptor;

    @Test
    void givenApplicantSolicitorIsDigital_whenSendRefusedOrder_thenEmailSent() {
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(FinremCaseDetails.class)))
            .thenReturn(true);
        List<RefusedOrder> refusedOrders = List.of(createRefusedOrder(OrderFiledBy.APPLICANT));
        RefusedOrderCorrespondenceRequest request = createRequest(createCaseData(), refusedOrders);
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
        List<RefusedOrder> refusedOrders = List.of(createRefusedOrder(OrderFiledBy.RESPONDENT));
        RefusedOrderCorrespondenceRequest request = createRequest(createCaseData(), refusedOrders);
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

        List<RefusedOrder> refusedOrders = List.of(createRefusedOrder(OrderFiledBy.APPLICANT));
        RefusedOrderCorrespondenceRequest request = createRequest(createCaseData(), refusedOrders);

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

        List<RefusedOrder> refusedOrders = List.of(createRefusedOrder(OrderFiledBy.RESPONDENT));
        RefusedOrderCorrespondenceRequest request = createRequest(createCaseData(), refusedOrders);

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
    void givenRefusedOrderForApplicantBarrister_whenSendRefusedOrder_thenEmailSent() {
        List<RefusedOrder> refusedOrders = List.of(createRefusedOrder(OrderFiledBy.APPLICANT_BARRISTER));
        RefusedOrderCorrespondenceRequest request = createRequest(createCaseData(), refusedOrders);
        NotificationRequest notificationRequest = mock(NotificationRequest.class);
        when(notificationRequestMapper.buildRefusedDraftOrderOrPsaNotificationRequest(request.caseDetails(), refusedOrders.get(0)))
            .thenReturn(notificationRequest);

        refusedOrderCorresponder.sendRefusedOrder(request);

        verify(notificationService, times(1)).sendRefusedDraftOrderOrPsa(notificationRequest);
        verifyNoInteractions(bulkPrintService);
    }

    @ParameterizedTest
    @MethodSource("provideInterveners")
    void givenIntervenerSolicitorIsDigital_whenSendRefusedOrder_thenEmailSent(FinremCaseData caseData,
                                                                              OrderFiledBy orderFiledBy) {
        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(any(IntervenerWrapper.class),
            any(FinremCaseDetails.class))).thenReturn(true);

        List<RefusedOrder> refusedOrders = List.of(createRefusedOrder(orderFiledBy));
        RefusedOrderCorrespondenceRequest request = createRequest(caseData, refusedOrders);
        NotificationRequest notificationRequest = mock(NotificationRequest.class);
        when(notificationRequestMapper.buildRefusedDraftOrderOrPsaNotificationRequest(request.caseDetails(), refusedOrders.get(0)))
            .thenReturn(notificationRequest);

        refusedOrderCorresponder.sendRefusedOrder(request);

        verify(notificationService, times(1)).sendRefusedDraftOrderOrPsa(notificationRequest);
        verifyNoInteractions(bulkPrintService);
    }

    @ParameterizedTest
    @MethodSource("provideInterveners")
    void givenIntervenerIsNotDigital_whenSendRefusedOrder_thenLetterSent(FinremCaseData caseData,
                                                                         OrderFiledBy orderFiledBy) {
        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(any(IntervenerWrapper.class),
            any(FinremCaseDetails.class))).thenReturn(false);
        when(documentHelper.getBulkPrintDocumentFromCaseDocument(any(CaseDocument.class))).thenCallRealMethod();

        List<RefusedOrder> refusedOrders = List.of(createRefusedOrder(orderFiledBy));
        RefusedOrderCorrespondenceRequest request = createRequest(caseData, refusedOrders);

        refusedOrderCorresponder.sendRefusedOrder(request);

        verify(notificationService, never()).sendRefusedDraftOrderOrPsa(any(NotificationRequest.class));
        verify(bulkPrintService, never()).printApplicantDocuments(any(FinremCaseDetails.class), anyString(),
            anyList());
        verify(bulkPrintService, never()).printRespondentDocuments(any(FinremCaseDetails.class), anyString(),
            anyList());
        verify(bulkPrintService, times(1)).printIntervenerDocuments(any(IntervenerWrapper.class),
            any(FinremCaseDetails.class),
            anyString(),
            intervenerBulkPrintDocumentsCaptor.capture());
        List<BulkPrintDocument> bulkPrintDocuments = intervenerBulkPrintDocumentsCaptor.getValue();
        assertThat(bulkPrintDocuments).hasSize(1);
    }

    private static Stream<Arguments> provideInterveners() {
        return Stream.of(
            Arguments.of(createCaseDataIntervener1(), OrderFiledBy.INTERVENER_1),
            Arguments.of(createCaseDataIntervener2(), OrderFiledBy.INTERVENER_2),
            Arguments.of(createCaseDataIntervener3(), OrderFiledBy.INTERVENER_3),
            Arguments.of(createCaseDataIntervener4(), OrderFiledBy.INTERVENER_4)
        );
    }

    @Test
    void givenRefusedOrderForRespondentBarrister_whenSendRefusedOrder_thenEmailSent() {
        List<RefusedOrder> refusedOrders = List.of(createRefusedOrder(OrderFiledBy.RESPONDENT_BARRISTER));
        RefusedOrderCorrespondenceRequest request = createRequest(createCaseData(), refusedOrders);
        NotificationRequest notificationRequest = mock(NotificationRequest.class);
        when(notificationRequestMapper.buildRefusedDraftOrderOrPsaNotificationRequest(request.caseDetails(), refusedOrders.get(0)))
            .thenReturn(notificationRequest);

        refusedOrderCorresponder.sendRefusedOrder(request);

        verify(notificationService, times(1)).sendRefusedDraftOrderOrPsa(notificationRequest);
        verifyNoInteractions(bulkPrintService);
    }

    @Test
    void givenSolicitorsDigitalAndMultipleRefusedOrders_whenSendRefusedOrder_thenMultipleEmailsSent() {
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(FinremCaseDetails.class)))
            .thenReturn(true);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(any(FinremCaseDetails.class)))
            .thenReturn(true);
        List<RefusedOrder> refusedOrders = List.of(
            createRefusedOrder(OrderFiledBy.RESPONDENT),
            createRefusedOrder(OrderFiledBy.APPLICANT),
            createRefusedOrder(OrderFiledBy.APPLICANT),
            createRefusedOrder(OrderFiledBy.RESPONDENT));
        RefusedOrderCorrespondenceRequest request = createRequest(createCaseData(), refusedOrders);
        when(notificationRequestMapper.buildRefusedDraftOrderOrPsaNotificationRequest(request.caseDetails(), refusedOrders.get(0)))
            .thenReturn(createNotificationRequest("respondent1"));
        when(notificationRequestMapper.buildRefusedDraftOrderOrPsaNotificationRequest(request.caseDetails(), refusedOrders.get(1)))
            .thenReturn(createNotificationRequest("applicant1"));
        when(notificationRequestMapper.buildRefusedDraftOrderOrPsaNotificationRequest(request.caseDetails(), refusedOrders.get(2)))
            .thenReturn(createNotificationRequest("applicant2"));
        when(notificationRequestMapper.buildRefusedDraftOrderOrPsaNotificationRequest(request.caseDetails(), refusedOrders.get(3)))
            .thenReturn(createNotificationRequest("respondent2"));

        refusedOrderCorresponder.sendRefusedOrder(request);

        // Verify all email requests sent
        verify(notificationService, times(4)).sendRefusedDraftOrderOrPsa(notificationRequestCaptor.capture());
        List<NotificationRequest> notificationRequests = notificationRequestCaptor.getAllValues();
        assertThat(notificationRequests)
            .extracting(NotificationRequest::getNotificationEmail)
            .containsExactlyInAnyOrder("applicant1", "applicant2", "respondent1", "respondent2");

        verifyNoInteractions(bulkPrintService);
    }

    @Test
    void givenSolicitorsNotDigitalAndMultipleRefusedOrders_whenSendRefusedOrder_thenMultiDocumentLetterSent() {
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(FinremCaseDetails.class)))
            .thenReturn(false);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(any(FinremCaseDetails.class)))
            .thenReturn(false);
        List<RefusedOrder> refusedOrders = List.of(
            createRefusedOrder(OrderFiledBy.RESPONDENT),
            createRefusedOrder(OrderFiledBy.APPLICANT),
            createRefusedOrder(OrderFiledBy.APPLICANT),
            createRefusedOrder(OrderFiledBy.RESPONDENT));
        RefusedOrderCorrespondenceRequest request = createRequest(createCaseData(), refusedOrders);

        refusedOrderCorresponder.sendRefusedOrder(request);

        verify(notificationService, never()).sendRefusedDraftOrderOrPsa(any(NotificationRequest.class));
        verify(bulkPrintService, times(1)).printApplicantDocuments(any(FinremCaseDetails.class), anyString(),
            applicantBulkPrintDocumentsCaptor.capture());
        verify(bulkPrintService, times(1)).printRespondentDocuments(any(FinremCaseDetails.class), anyString(),
            respondentBulkPrintDocumentsCaptor.capture());

        assertThat(applicantBulkPrintDocumentsCaptor.getValue()).hasSize(2);
        assertThat(respondentBulkPrintDocumentsCaptor.getValue()).hasSize(2);
    }

    private RefusedOrderCorrespondenceRequest createRequest(FinremCaseData caseData, List<RefusedOrder> refusedOrders) {
        return RefusedOrderCorrespondenceRequest.builder()
            .caseDetails(FinremCaseDetails.builder().data(caseData).build())
            .refusedOrders(refusedOrders)
            .authorisationToken(TestConstants.AUTH_TOKEN)
            .build();
    }

    private static FinremCaseData createCaseDataIntervener1() {
        return FinremCaseData.builder()
            .intervenerOne(IntervenerOne.builder()
                .build())
            .build();
    }

    private static FinremCaseData createCaseDataIntervener2() {
        return FinremCaseData.builder()
            .intervenerTwo(IntervenerTwo.builder()
                .build())
            .build();
    }

    private static FinremCaseData createCaseDataIntervener3() {
        return FinremCaseData.builder()
            .intervenerThree(IntervenerThree.builder()
                .build())
            .build();
    }

    private static FinremCaseData createCaseDataIntervener4() {
        return FinremCaseData.builder()
            .intervenerFour(IntervenerFour.builder()
                .build())
            .build();
    }

    private FinremCaseData createCaseData() {
        return FinremCaseData.builder()
            .build();
    }

    private RefusedOrder createRefusedOrder(OrderFiledBy orderFiledBy) {
        return RefusedOrder.builder()
            .refusedDate(LocalDateTime.now())
            .refusalOrder(CaseDocument.builder().build())
            .orderFiledBy(orderFiledBy)
            .build();
    }

    private NotificationRequest createNotificationRequest(String email) {
        return NotificationRequest.builder().notificationEmail(email).build();
    }
}
