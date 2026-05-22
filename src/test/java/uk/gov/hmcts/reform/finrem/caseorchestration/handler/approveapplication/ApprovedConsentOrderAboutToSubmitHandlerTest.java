package uk.gov.hmcts.reform.finrem.caseorchestration.handler.approveapplication;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionTypeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderApprovedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.StampType;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogs;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID_IN_LONG;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class ApprovedConsentOrderAboutToSubmitHandlerTest {

    @TestLogs
    private final TestLogger logs = new TestLogger(ApprovedConsentOrderAboutToSubmitHandler.class);

    @InjectMocks
    private ApprovedConsentOrderAboutToSubmitHandler handler;
    @Mock
    private ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;
    @Mock
    private GenericDocumentService genericDocumentService;
    @Mock
    private ConsentOrderPrintService consentOrderPrintService;
    @Mock
    private DocumentHelper documentHelper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String APPROVE_ORDER_VALID_JSON = "/fixtures/submit-general-application.json";
    private static final String APPROVE_ORDER_NO_PENSION_VALID_JSON = "/fixtures/bulkprint/bulk-print-no-pension-collection.json";
    private static final String NO_PENSION_VALID_JSON = "/fixtures/bulkprint/bulk-print-no-pension-collection.json";

    @Test
    void testCanHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.APPROVE_ORDER);
    }

    @Test
    void givenLatestConsentOrderAbsent_whenHandled_thenDoNothingAndLog() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(CASE_ID_IN_LONG,
            FinremCaseData.builder()
                .ccdCaseType(CaseType.CONSENTED)
                .build()
        );

        var response = handler.handle(callbackRequest, AUTH_TOKEN);

        assertAll(
            () -> assertEquals(callbackRequest.getFinremCaseData(), response.getData()),
            () -> verifyNoInteractions(genericDocumentService),
            () -> assertThat(logs.getInfos())
                .contains("Failed to handle 'Consent Order Approved' callback because 'latestConsentOrder' is empty for Case ID: %s"
                    .formatted(CASE_ID))
        );
    }

    @Test
    void givenLatestConsentOrderProvided_whenHandled_thenConvertedToPdf() {
        CaseDocument latestConsentOrder = mock(CaseDocument.class);

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(
            FinremCaseData.builder()
                .ccdCaseType(CaseType.CONSENTED)
                .latestConsentOrder(latestConsentOrder)
                .build()
        );

        CaseDocument latestConsentOrderInPdf = mock(CaseDocument.class);
        when(genericDocumentService.convertDocumentIfNotPdfAlready(latestConsentOrder, AUTH_TOKEN, CaseType.CONSENTED))
            .thenReturn(latestConsentOrderInPdf);

        var response = handler.handle(callbackRequest, AUTH_TOKEN);

        assertAll(
            () -> verify(genericDocumentService).convertDocumentIfNotPdfAlready(latestConsentOrder, AUTH_TOKEN, CaseType.CONSENTED),
            () -> assertThat(response.getData()).extracting(FinremCaseData::getLatestConsentOrder).isEqualTo(latestConsentOrderInPdf)
        );
    }

    @Test
    void givenLatestConsentOrderProvided_whenNoPension_thenShouldUpdateStateToConsentOrderMade() {
        CaseDocument latestConsentOrder = mock(CaseDocument.class);

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(
            FinremCaseData.builder()
                .ccdCaseType(CaseType.CONSENTED)
                .latestConsentOrder(latestConsentOrder)
                .build()
        );
        mockEmptyPensionDocument(callbackRequest, true);

        var response = handler.handle(callbackRequest, AUTH_TOKEN);

        assertEquals("CONSENT_ORDER_MADE", response.getData().getState());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void givenLatestConsentOrderProvided_whenHandled_thenApproveOrderCreated(boolean isPensionDocumentEmpty) {
        CaseDocument latestConsentOrder = mock(CaseDocument.class);
        LocalDate approvalDate = mock(LocalDate.class);
        List<PensionTypeCollection> pensionCollection = mock(List.class);
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(
            FinremCaseData.builder()
                .orderDirectionDate(approvalDate)
                .pensionCollection(pensionCollection)
                .ccdCaseType(CaseType.CONSENTED)
                .latestConsentOrder(latestConsentOrder)
                .build()
        );
        mockEmptyPensionDocument(callbackRequest, isPensionDocumentEmpty);
        StampType stampType = mock(StampType.class);
        when(documentHelper.getStampType(callbackRequest.getFinremCaseData())).thenReturn(stampType);

        CaseDocument latestConsentOrderInPdf = mock(CaseDocument.class);
        when(genericDocumentService.convertDocumentIfNotPdfAlready(latestConsentOrder, AUTH_TOKEN, CaseType.CONSENTED))
            .thenReturn(latestConsentOrderInPdf);

        CaseDocument expectedGeneratedDocument = mock(CaseDocument.class);
        when(consentOrderApprovedDocumentService.generateApprovedConsentOrderLetter(callbackRequest.getCaseDetails(), AUTH_TOKEN))
            .thenReturn(expectedGeneratedDocument);
        CaseDocument expectedConsentOrderAnnexStamped = mock(CaseDocument.class);
        when(genericDocumentService
            .annexStampDocument(latestConsentOrderInPdf, AUTH_TOKEN, stampType, CaseType.CONSENTED))
            .thenReturn(expectedConsentOrderAnnexStamped);

        List<PensionTypeCollection> stampedPensionDocs = mock(List.class);
        if (!isPensionDocumentEmpty) {
            when(consentOrderApprovedDocumentService.stampPensionDocuments(pensionCollection, AUTH_TOKEN, stampType, approvalDate, CaseType.CONSENTED))
                .thenReturn(stampedPensionDocs);
        }

        var response = handler.handle(callbackRequest, AUTH_TOKEN);

        assertAll(
            () -> verify(consentOrderApprovedDocumentService).generateApprovedConsentOrderLetter(callbackRequest.getCaseDetails(), AUTH_TOKEN),
            () -> verify(genericDocumentService).annexStampDocument(latestConsentOrderInPdf, AUTH_TOKEN, stampType, CaseType.CONSENTED),
            () -> assertThat(response.getData().getApprovedOrderCollection())
                .extracting("approvedOrder.orderLetter")
                .containsOnlyOnce(expectedGeneratedDocument),
            () -> assertThat(response.getData().getApprovedOrderCollection())
                .extracting("approvedOrder.consentOrder")
                .containsOnlyOnce(expectedConsentOrderAnnexStamped)
//            ,
//            () -> {
//                if (isPensionDocumentEmpty) {
//                    assertThat(response.getData().getApprovedOrderCollection())
//                        .extracting("approvedOrder.pensionDocuments")
//                        .containsOnlyOnce(stampedPensionDocs);
//                }
//            }
        );
    }

//    @Test
//    void givenLatestConsentOrderProvided_whenNoPension_thenShouldUpdateStateToConsentOrderMadeAndBulkPrint() {
//        CaseDocument latestConsentOrder = mock(CaseDocument.class);
//
//        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(
//            FinremCaseData.builder()
//                .ccdCaseType(CaseType.CONSENTED)
//                .latestConsentOrder(latestConsentOrder)
//                .build()
//        );
//
//
////        FinremCallbackRequest callbackRequest1 = FinremCallbackRequest.builder()
////            .caseDetails(callbackRequest.getCaseDetails())
////            .caseDetailsBefore(callbackRequest.getCaseDetails())
////            .eventId(EventType.APPROVE_ORDER.getCcdType()).build();
//
//        var response = handler.handle(callbackRequest1, AUTH_TOKEN);
//
//        assertEquals(response.getData().get(STATE), CONSENT_ORDER_MADE.toString());
//
//        verify(consentOrderPrintService).sendConsentOrderToBulkPrint(any(CaseDetails.class),
//            any(CaseDetails.class),
//            any(EventType.class),
//            any());
//    }
//
//    @Test
//    void shouldUpdateStateToConsentOrderMadeAndBulkPrint_noEmails() {
//        FinremCallbackRequest callbackRequest =
//            doValidCaseDataSetUp(APPROVE_ORDER_NO_PENSION_VALID_JSON);
//        whenServiceGeneratesDocument().thenReturn(caseDocument());
//
//        CallbackRequest callbackRequest1 = CallbackRequest.builder()
//            .caseDetails(callbackRequest.getCaseDetails())
//            .caseDetailsBefore(callbackRequest.getCaseDetails())
//            .eventId(EventType.APPROVE_ORDER.getCcdType()).build();
//
//        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> response = handler.handle(callbackRequest1, AUTH_TOKEN);
//
//        assertEquals(response.getData().get(STATE), CONSENT_ORDER_MADE.toString());
//
//        verify(consentOrderPrintService).sendConsentOrderToBulkPrint(any(CaseDetails.class),
//            any(CaseDetails.class),
//            any(EventType.class),
//            any());
//    }
//
//    @Test
//    void shouldDateStampPensionSharingAnnexContested() {
//        whenServiceGeneratesDocument().thenReturn(caseDocument());
//        when(documentHelper.getPensionDocumentsData(any(Map.class))).thenReturn(singletonList(caseDocument()));
//        LocalDate approvedDate = LocalDate.of(2024, 12, 01);
//
//        FinremCallbackRequest callbackRequest =
//            doValidCaseDataSetUp(APPROVE_ORDER_VALID_JSON);
//        callbackRequest.getCaseDetails().getData().put(CONTESTED_ORDER_DIRECTION_DATE, approvedDate);
//        callbackRequest.getCaseDetails().getData().put(CONSENTED_ORDER_DIRECTION_DATE, null);
//        handler.handle(callbackRequest, AUTH_TOKEN);
//
//        verify(consentOrderApprovedDocumentService).stampPensionDocuments(anyList(),
//            any(),
//            any(),
//            any(),
//            any());
//    }
//
//    @Test
//    void shouldDateStampPensionSharingAnnexConsented() {
//        whenServiceGeneratesDocument().thenReturn(caseDocument());
//        when(documentHelper.getPensionDocumentsData(any(Map.class))).thenReturn(singletonList(caseDocument()));
//        LocalDate approvedDate = LocalDate.of(2024, 12, 1);
//
//        FinremCallbackRequest callbackRequest =
//            doValidCaseDataSetUp(APPROVE_ORDER_VALID_JSON);
//        callbackRequest.getCaseDetails().getData().put(CONTESTED_ORDER_DIRECTION_DATE, null);
//        callbackRequest.getCaseDetails().getData().put(CONSENTED_ORDER_DIRECTION_DATE, approvedDate);
//        handler.handle(callbackRequest, AUTH_TOKEN);
//
//        verify(consentOrderApprovedDocumentService).stampPensionDocuments(anyList(),
//            any(),
//            any(),
//            any(),
//            any());
//    }
//
    private FinremCallbackRequest doValidCaseDataSetUp(final String path) {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(path)) {
            return objectMapper.readValue(resourceAsStream, FinremCallbackRequest.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private OngoingStubbing<CaseDocument> whenServiceGeneratesDocument() {
        return when(consentOrderApprovedDocumentService.generateApprovedConsentOrderLetter(isA(FinremCaseDetails.class), eq(AUTH_TOKEN)));
    }

    private void mockEmptyPensionDocument(FinremCallbackRequest callbackRequest, boolean isPensionDocumentEmpty) {
        when(documentHelper.getPensionDocumentsData(callbackRequest.getFinremCaseData())).thenReturn(
            isPensionDocumentEmpty ? List.of() : List.of(caseDocument()));
    }
}
