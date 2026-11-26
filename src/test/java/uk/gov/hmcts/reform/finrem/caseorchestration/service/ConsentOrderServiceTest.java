package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AmendedConsentOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AmendedConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RespondToOrderDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RespondToOrderDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RespondToOrderDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.VariationOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.VariationOrderType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.VariationTypeOfDocument;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CONSENT_ORDER_CAMELCASE_LABEL_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CONSENT_ORDER_LOWERCASE_LABEL_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CONSENT_OTHER_DOC_LABEL_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CV_LOWERCASE_LABEL_FIELD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CV_ORDER_CAMELCASE_LABEL_FIELD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CV_OTHER_DOC_LABEL_FIELD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CV_OTHER_DOC_LABEL_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.VARIATION_ORDER_CAMELCASE_LABEL_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.VARIATION_ORDER_LOWERCASE_LABEL_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestObjectMapperFactory.createObjectMapper;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;

public class ConsentOrderServiceTest extends BaseServiceTest {

    private static final String PATH = "/fixtures/latestConsentedConsentOrder/";

    @Autowired
    private ConsentOrderService consentOrderService;

    @MockitoBean
    private BulkPrintDocumentService service;

    private CallbackRequest callbackRequest;

    private void setUpCaseDetails(String fileName) throws Exception {
        try (InputStream resourceAsStream =
                 getClass().getResourceAsStream(PATH + fileName)) {
            callbackRequest = createObjectMapper().readValue(resourceAsStream, CallbackRequest.class);
            callbackRequest.setCaseDetailsBefore(CaseDetails.builder().id(123L).data(new HashMap<>()).build());
        }
    }

    @Test
    public void checkIfDocumentIsEncrypted() throws Exception {
        setUpCaseDetails("draft-consent-order.json");
        Map<String, Object> data = callbackRequest.getCaseDetails().getData();
        Map<String, Object> beforeData = callbackRequest.getCaseDetailsBefore().getData();
        List<CaseDocument> caseDocuments = consentOrderService.checkIfD81DocumentContainsEncryption(data,
            beforeData);
        assertEquals(5, caseDocuments.size());
    }

    @Test
    public void variationOrderDocumentsCheckIfDocumentIsEncrypted() throws Exception {
        setUpCaseDetails("draft-consent-order.json");
        Map<String, Object> data = callbackRequest.getCaseDetails().getData();

        List<VariationOrderCollection> variationList = new ArrayList<>();

        VariationOrderCollection orderCollection = getObj(VariationTypeOfDocument.ORIGINAL_ORDER, true);

        variationList.add(orderCollection);
        VariationOrderCollection orderCollection2 = getObj(VariationTypeOfDocument.OTHER_DOCUMENTS, false);

        variationList.add(orderCollection2);
        data.put("otherVariationCollection", variationList);

        VariationOrderCollection orderCollectionBefore = getObj(VariationTypeOfDocument.ORIGINAL_ORDER, true);

        Map<String, Object> beforeData = callbackRequest.getCaseDetailsBefore().getData();
        List<VariationOrderCollection> variationListB = new ArrayList<>();
        variationListB.add(orderCollectionBefore);
        beforeData.put("otherVariationCollection", variationListB);

        List<CaseDocument> caseDocuments = consentOrderService.checkIfD81DocumentContainsEncryption(data, beforeData);
        assertEquals(6, caseDocuments.size());

    }

    private VariationOrderCollection getObj(VariationTypeOfDocument type, boolean orignalName) {
        return VariationOrderCollection.builder().id(UUID.randomUUID().toString()).typeOfDocument(VariationOrderType.builder()
            .typeOfDocument(type)
            .uploadedDocument(orignalName ? caseDocument() : caseDocument("http://url",
                "name.pdf", "http://url/binary")).build()).build();
    }

    @Test
    public void shouldSetLatestDraftConsentOrderWhenAFinremCaseIsCreated() throws Exception {
        FinremCallbackRequest finremCallbackRequest = buildFinremCallbackRequest(PATH + "draft-consent-order.json");
        CaseDocument latestConsentOrderData = consentOrderService.getLatestConsentOrderData(finremCallbackRequest);
        assertThat(latestConsentOrderData.getDocumentBinaryUrl(), is("http://file1.binary"));
        assertThat(latestConsentOrderData.getDocumentFilename(), is("file1"));
        assertThat(latestConsentOrderData.getDocumentUrl(), is("http://file1"));
    }

    @Test
    public void shouldUpdateLatestDraftConsentOrderWhenAFinremCaseIsAmended() throws Exception {
        FinremCallbackRequest finremCallbackRequest = buildFinremCallbackRequest(PATH + "amend-consent-order-by-solicitor.json");
        CaseDocument latestConsentOrderData = consentOrderService.getLatestConsentOrderData(finremCallbackRequest);
        assertThat(latestConsentOrderData.getDocumentBinaryUrl(), is("http://file2.binary"));
        assertThat(latestConsentOrderData.getDocumentFilename(), is("file2"));
        assertThat(latestConsentOrderData.getDocumentUrl(), is("http://file2"));
    }

    @Test
    public void shouldReturnLatestAmendedConsentOrderWhenAFinremCaseIsAmendedByCaseWorker() throws Exception {
        FinremCallbackRequest finremCallbackRequest = buildFinremCallbackRequest(PATH + "amend-consent-order-by-caseworker.json");
        CaseDocument latestConsentOrderData = consentOrderService.getLatestConsentOrderData(finremCallbackRequest);
        assertThat(latestConsentOrderData.getDocumentUrl(),
            is("http://dm-store:8080/documents/0bdc0d68-e654-4faa-848a-8ae3c478838"));
        assertThat(latestConsentOrderData.getDocumentFilename(),
            is("Notification for ABC - Contested.docx"));
        assertThat(latestConsentOrderData.getDocumentBinaryUrl(),
            is("http://dm-store:8080/documents/0bdc0d68-e654-4faa-848a-8ae3c478838/binary"));
    }

    @Test
    public void shouldReturnLatestAmendedConsentOrderWhenAFinremCaseIsRespondedBySolicitor() throws Exception {
        FinremCallbackRequest finremCallbackRequest = buildFinremCallbackRequest(PATH + "respond-to-order-solicitor.json");
        CaseDocument latestConsentOrderData = consentOrderService.getLatestConsentOrderData(finremCallbackRequest);
        assertThat(latestConsentOrderData.getDocumentUrl(), is("http://doc2"));
        assertThat(latestConsentOrderData.getDocumentFilename(), is("doc2"));
        assertThat(latestConsentOrderData.getDocumentBinaryUrl(), is("http://doc2/binary"));
    }

    @Test
    public void given_case_checkIfUploadedConsentOrderIsNotEncrypted() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        Map<String, Object> data = callbackRequest.getCaseDetails().getData();
        List<String> orderList = List.of("Variation Order", "Property Adjustment Order");
        data.put("natureOfApplication2", orderList);
        data.put("consentOrder", caseDocument());

        List<String> errors = consentOrderService.performCheck(callbackRequest, AUTH_TOKEN);
        assertTrue(errors.isEmpty());

        final String camelCaseLabel = (String) data.get(CV_ORDER_CAMELCASE_LABEL_FIELD);
        assertEquals(VARIATION_ORDER_CAMELCASE_LABEL_VALUE, camelCaseLabel);
        final String lowerCaseLabel = (String) data.get(CV_LOWERCASE_LABEL_FIELD);
        assertEquals(VARIATION_ORDER_LOWERCASE_LABEL_VALUE, lowerCaseLabel);
        final String docLabel = (String) data.get(CV_OTHER_DOC_LABEL_FIELD);
        assertEquals(CV_OTHER_DOC_LABEL_VALUE, docLabel);

        verify(service).validateEncryptionOnUploadedDocument(any(), any(), any(), any());
    }

    @Test
    public void given_case_checkIfUploadedConsentOrderIsNotEncryptedIfSameDocumentAlreadyUploaded() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        Map<String, Object> data = callbackRequest.getCaseDetails().getData();
        List<String> orderList = List.of("Variation Order", "Property Adjustment Order");
        data.put("natureOfApplication2", orderList);
        data.put("consentOrder", caseDocument());

        List<String> errors = consentOrderService.performCheck(callbackRequest, AUTH_TOKEN);
        assertTrue(errors.isEmpty());

        final String camelCaseLabel = (String) data.get(CV_ORDER_CAMELCASE_LABEL_FIELD);
        assertEquals(VARIATION_ORDER_CAMELCASE_LABEL_VALUE, camelCaseLabel);
        final String lowerCaseLabel = (String) data.get(CV_LOWERCASE_LABEL_FIELD);
        assertEquals(VARIATION_ORDER_LOWERCASE_LABEL_VALUE, lowerCaseLabel);
        final String docLabel = (String) data.get(CV_OTHER_DOC_LABEL_FIELD);
        assertEquals(CV_OTHER_DOC_LABEL_VALUE, docLabel);

        verify(service).validateEncryptionOnUploadedDocument(any(), any(), any(), any());

    }

    @Test
    public void given_case_when_natureOfApplicationIsVariation_thenReturnVariationOrderLabels() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        Map<String, Object> data = callbackRequest.getCaseDetails().getData();
        List<String> orderList = List.of("Variation Order", "Property Adjustment Order");
        data.put("natureOfApplication2", orderList);

        List<String> errors = consentOrderService.performCheck(callbackRequest, AUTH_TOKEN);
        assertTrue(errors.isEmpty());

        final String camelCaseLabel = (String) data.get(CV_ORDER_CAMELCASE_LABEL_FIELD);
        assertEquals(VARIATION_ORDER_CAMELCASE_LABEL_VALUE, camelCaseLabel);
        final String lowerCaseLabel = (String) data.get(CV_LOWERCASE_LABEL_FIELD);
        assertEquals(VARIATION_ORDER_LOWERCASE_LABEL_VALUE, lowerCaseLabel);
        final String docLabel = (String) data.get(CV_OTHER_DOC_LABEL_FIELD);
        assertEquals(CV_OTHER_DOC_LABEL_VALUE, docLabel);
        verify(service, never()).validateEncryptionOnUploadedDocument(any(), any(), any(), any());
    }

    @Test
    public void given_case_when_natureOfApplicationDoNotContainsVariation_thenReturnConsentOrderLabels() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        Map<String, Object> data = callbackRequest.getCaseDetails().getData();
        List<String> orderList = List.of("Property Adjustment Order");
        data.put("natureOfApplication2", orderList);

        List<String> errors = consentOrderService.performCheck(callbackRequest, AUTH_TOKEN);
        assertTrue(errors.isEmpty());

        final String camelCaseLabel = (String) data.get(CV_ORDER_CAMELCASE_LABEL_FIELD);
        assertEquals(CONSENT_ORDER_CAMELCASE_LABEL_VALUE, camelCaseLabel);
        final String lowerCaseLabel = (String) data.get(CV_LOWERCASE_LABEL_FIELD);
        assertEquals(CONSENT_ORDER_LOWERCASE_LABEL_VALUE, lowerCaseLabel);
        final String docLabel = (String) data.get(CV_OTHER_DOC_LABEL_FIELD);
        assertEquals(CONSENT_OTHER_DOC_LABEL_VALUE, docLabel);
    }

    //Amended Consent Order (caseworker route)
    @Test
    public void shouldArchivePreviousAmendedOrderWithItsAmendedDateWhenNewAmendmentUploaded() {
        CaseDocument previousOrder = consentOrderDocument("previous");
        CaseDocument newOrder = consentOrderDocument("new");
        LocalDate previousAmendedDate = LocalDate.of(2026, 10, 20);
        FinremCallbackRequest request = buildArchiveRequest(previousOrder, newOrder);
        request.getFinremCaseDataBefore().setAmendedConsentOrderCollection(
            List.of(amendedConsentOrder(previousOrder, previousAmendedDate)));

        consentOrderService.archiveAndUpdateLatestConsentOrder(request);

        FinremCaseData caseData = request.getFinremCaseData();
        assertSame(newOrder, caseData.getLatestConsentOrder());
        assertEquals(1, caseData.getRespondToOrderDocuments().size());
        RespondToOrderDocument archived = caseData.getRespondToOrderDocuments().getFirst().getValue();
        assertEquals(RespondToOrderDocumentType.AMEND_CONSENT_ORDER, archived.getDocumentType());
        assertEquals(previousOrder, archived.getDocumentLink());
        assertNotSame(previousOrder, archived.getDocumentLink());
        assertEquals("previous.pdf", archived.getDocumentFileName());
        assertEquals(previousAmendedDate, archived.getDocumentDateAdded());
    }

    @Test
    public void shouldArchiveOriginalOrderUsingUploadDateWhenNotAnAmendedOrder() {
        LocalDate uploadDate = LocalDate.of(2026, 11, 1);
        CaseDocument previousOrder = consentOrderDocument("previous").toBuilder()
            .uploadTimestamp(uploadDate.atTime(15, 30)).build();
        FinremCallbackRequest request = buildArchiveRequest(previousOrder, consentOrderDocument("new"));
        request.getFinremCaseDataBefore().setAmendedConsentOrderCollection(
            List.of(amendedConsentOrder(consentOrderDocument("unrelated"), uploadDate.plusDays(1))));

        consentOrderService.archiveAndUpdateLatestConsentOrder(request);

        RespondToOrderDocument archived = request.getFinremCaseData().getRespondToOrderDocuments().getFirst().getValue();
        assertEquals(RespondToOrderDocumentType.AMEND_CONSENT_ORDER, archived.getDocumentType());
        assertEquals(uploadDate, archived.getDocumentDateAdded());
    }

    @Test
    public void shouldArchiveWithTodaysDateWhenAmendmentDateAndUploadTimestampAreMissing() {
        CaseDocument previousOrder = consentOrderDocument("previous");
        CaseDocument newOrder = consentOrderDocument("new");
        FinremCallbackRequest request = buildArchiveRequest(previousOrder, newOrder);
        request.getFinremCaseDataBefore().setAmendedConsentOrderCollection(
            List.of(amendedConsentOrder(previousOrder, null)));

        consentOrderService.archiveAndUpdateLatestConsentOrder(request);

        RespondToOrderDocument archived = request.getFinremCaseData().getRespondToOrderDocuments().getFirst().getValue();
        assertEquals(previousOrder, archived.getDocumentLink());
        assertEquals(LocalDate.now(), archived.getDocumentDateAdded());
        assertSame(newOrder, request.getFinremCaseData().getLatestConsentOrder());
    }

    @Test
    public void shouldLeaveLatestConsentOrderAndHistoryUnchangedWhenDocumentUrlHasNotChanged() {
        CaseDocument previousOrder = consentOrderDocument("previous");
        CaseDocument sameOrder = previousOrder.toBuilder()
            .documentFilename("renamed.pdf")
            .build();
        FinremCallbackRequest request = buildArchiveRequest(previousOrder, sameOrder);

        List<RespondToOrderDocumentCollection> existingDocuments =
            List.of(archivedConsentOrder(consentOrderDocument("older")));
        request.getFinremCaseData().setRespondToOrderDocuments(existingDocuments);

        consentOrderService.archiveAndUpdateLatestConsentOrder(request);

        assertEquals(existingDocuments, request.getFinremCaseData().getRespondToOrderDocuments());
    }

    @Test
    public void shouldSetLatestWithoutArchivingWhenThereIsNoPreviousOrder() {
        CaseDocument newOrder = consentOrderDocument("new");
        FinremCallbackRequest request = buildArchiveRequest(null, newOrder);

        consentOrderService.archiveAndUpdateLatestConsentOrder(request);

        assertSame(newOrder, request.getFinremCaseData().getLatestConsentOrder());
        assertNull(request.getFinremCaseData().getRespondToOrderDocuments());
    }

    @Test
    public void shouldNotArchiveTwiceWhenPreviousOrderIsAlreadyInRespondToOrderDocuments() {
        CaseDocument previousOrder = consentOrderDocument("previous");
        FinremCallbackRequest request = buildArchiveRequest(previousOrder, consentOrderDocument("new"));
        request.getFinremCaseData().setRespondToOrderDocuments(List.of(archivedConsentOrder(previousOrder)));

        consentOrderService.archiveAndUpdateLatestConsentOrder(request);

        assertEquals(1, request.getFinremCaseData().getRespondToOrderDocuments().size());
    }

    @Test
    public void shouldAppendToExistingHistoryInChronologicalOrder() {
        CaseDocument olderOrder = consentOrderDocument("older");
        CaseDocument previousOrder = consentOrderDocument("previous");
        FinremCallbackRequest request = buildArchiveRequest(previousOrder, consentOrderDocument("new"));
        request.getFinremCaseData().setRespondToOrderDocuments(List.of(archivedConsentOrder(olderOrder)));

        consentOrderService.archiveAndUpdateLatestConsentOrder(request);

        List<RespondToOrderDocumentCollection> history = request.getFinremCaseData().getRespondToOrderDocuments();
        assertEquals(2, history.size());
        assertEquals(olderOrder, history.get(0).getValue().getDocumentLink());
        assertEquals(previousOrder, history.get(1).getValue().getDocumentLink());
    }

    @Test
    public void shouldDoNothingWhenNoOrderCanBeResolved() {
        CaseDocument previousOrder = consentOrderDocument("previous");
        FinremCaseData caseData = FinremCaseData.builder().latestConsentOrder(previousOrder).build();
        FinremCallbackRequest request = buildRequest(EventType.RESPOND_TO_ORDER, caseData,
            FinremCaseData.builder().latestConsentOrder(previousOrder).build());
        caseData.setLatestConsentOrder(null);

        consentOrderService.archiveAndUpdateLatestConsentOrder(request);

        assertNull(caseData.getLatestConsentOrder());
        assertNull(caseData.getRespondToOrderDocuments());
    }

    //Respond To Order (solicitor route)
    @Test
    public void givenRespondToOrderWithNewAmendedOrder_thenPromoteItAndArchivePreviousOrder() {
        CaseDocument originalOrder = consentOrderDocument("original");
        CaseDocument uploaded = consentOrderDocument("solicitor-upload");
        FinremCallbackRequest request = buildRespondToOrderRequest(originalOrder,
            List.of(),
            List.of(respondToOrderDocument(RespondToOrderDocumentType.AMEND_CONSENT_ORDER, uploaded)));

        consentOrderService.archiveAndUpdateLatestConsentOrder(request);

        FinremCaseData caseData = request.getFinremCaseData();
        assertSame(uploaded, caseData.getLatestConsentOrder());
        List<RespondToOrderDocumentCollection> history = caseData.getRespondToOrderDocuments();
        assertEquals(2, history.size());
        assertEquals(uploaded, history.get(0).getValue().getDocumentLink());
        assertEquals(originalOrder, history.get(1).getValue().getDocumentLink());
        assertEquals(RespondToOrderDocumentType.AMEND_CONSENT_ORDER, history.get(1).getValue().getDocumentType());
    }

    @Test
    public void givenRespondToOrderWithoutNewAmendedOrder_thenArchivedOrdersAreNotPromoted() {
        CaseDocument archivedOrder = consentOrderDocument("archived");
        CaseDocument currentOrder = consentOrderDocument("current");
        List<RespondToOrderDocumentCollection> before =
            List.of(respondToOrderDocument(RespondToOrderDocumentType.AMEND_CONSENT_ORDER, archivedOrder));
        FinremCallbackRequest request = buildRespondToOrderRequest(currentOrder, before,
            List.of(before.getFirst(),
                respondToOrderDocument(RespondToOrderDocumentType.APPLICANT_LETTER_EMAIL, consentOrderDocument("letter"))));

        consentOrderService.archiveAndUpdateLatestConsentOrder(request);

        assertSame(currentOrder, request.getFinremCaseData().getLatestConsentOrder());
        assertEquals(2, request.getFinremCaseData().getRespondToOrderDocuments().size());
    }

    @Test
    public void givenSecondRespondToOrder_thenNewUploadWinsOverEarlierUploadAndArchivedEntries() {
        CaseDocument firstUpload = consentOrderDocument("first-upload");
        CaseDocument archivedOriginal = consentOrderDocument("original");
        CaseDocument secondUpload = consentOrderDocument("second-upload");
        List<RespondToOrderDocumentCollection> before = List.of(
            respondToOrderDocument(RespondToOrderDocumentType.AMEND_CONSENT_ORDER, firstUpload),
            respondToOrderDocument(RespondToOrderDocumentType.AMEND_CONSENT_ORDER, archivedOriginal));
        List<RespondToOrderDocumentCollection> after = new ArrayList<>(before);
        after.add(respondToOrderDocument(RespondToOrderDocumentType.AMEND_CONSENT_ORDER, secondUpload));
        FinremCallbackRequest request = buildRespondToOrderRequest(firstUpload, before, after);

        consentOrderService.archiveAndUpdateLatestConsentOrder(request);

        assertSame(secondUpload, request.getFinremCaseData().getLatestConsentOrder());
        // firstUpload is already in the collection so it is not archived again
        assertEquals(3, request.getFinremCaseData().getRespondToOrderDocuments().size());
    }

    private FinremCallbackRequest buildArchiveRequest(CaseDocument previousOrder, CaseDocument newOrder) {
        FinremCaseData caseData = FinremCaseData.builder()
            .latestConsentOrder(previousOrder)
            .amendedConsentOrderCollection(List.of(amendedConsentOrder(newOrder, LocalDate.of(2026, 11, 2))))
            .build();
        FinremCaseData beforeData = FinremCaseData.builder().latestConsentOrder(previousOrder).build();
        return buildRequest(EventType.AMEND_CONSENT_ORDER, caseData, beforeData);
    }

    private FinremCallbackRequest buildRespondToOrderRequest(CaseDocument previousOrder,
                                                             List<RespondToOrderDocumentCollection> before,
                                                             List<RespondToOrderDocumentCollection> after) {
        FinremCaseData caseData = FinremCaseData.builder()
            .latestConsentOrder(previousOrder)
            .respondToOrderDocuments(after)
            .build();
        FinremCaseData beforeData = FinremCaseData.builder()
            .latestConsentOrder(previousOrder)
            .respondToOrderDocuments(before)
            .build();
        return buildRequest(EventType.RESPOND_TO_ORDER, caseData, beforeData);
    }

    private FinremCallbackRequest buildRequest(EventType eventType, FinremCaseData caseData, FinremCaseData beforeData) {
        return FinremCallbackRequest.builder()
            .eventType(eventType)
            .caseDetails(FinremCaseDetails.builder().caseType(CaseType.CONSENTED).data(caseData).build())
            .caseDetailsBefore(FinremCaseDetails.builder().caseType(CaseType.CONSENTED).data(beforeData).build())
            .build();
    }

    private CaseDocument consentOrderDocument(String name) {
        return caseDocument("http://dm-store/documents/" + name, name + ".pdf",
            "http://dm-store/documents/" + name + "/binary");
    }

    private AmendedConsentOrderCollection amendedConsentOrder(CaseDocument document, LocalDate date) {
        return AmendedConsentOrderCollection.builder()
            .value(AmendedConsentOrder.builder()
                .amendedConsentOrder(document)
                .amendedConsentOrderDate(date)
                .build())
            .build();
    }

    private RespondToOrderDocumentCollection archivedConsentOrder(CaseDocument document) {
        return respondToOrderDocument(RespondToOrderDocumentType.AMEND_CONSENT_ORDER, document);
    }

    private RespondToOrderDocumentCollection respondToOrderDocument(RespondToOrderDocumentType type, CaseDocument document) {
        return RespondToOrderDocumentCollection.builder()
            .value(RespondToOrderDocument.builder()
                .documentType(type)
                .documentLink(document)
                .documentFileName(document.getDocumentFilename())
                .build())
            .build();
    }

    private CallbackRequest buildCallbackRequest() {
        Map<String, Object> caseData = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().id(123L).build();
        CaseDetails caseDetailsBefore = CaseDetails.builder().id(123L).build();
        caseDetails.setData(caseData);
        return CallbackRequest.builder().eventId(EventType.SOLICITOR_CREATE.getCcdType())
            .caseDetails(caseDetails).caseDetailsBefore(caseDetailsBefore).build();
    }
}
