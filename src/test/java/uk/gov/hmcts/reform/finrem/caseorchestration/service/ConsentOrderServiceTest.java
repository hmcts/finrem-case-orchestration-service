package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.VariationOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.VariationOrderType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.VariationTypeOfDocument;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;

public class ConsentOrderServiceTest extends BaseServiceTest {

    private static final String PATH = "/fixtures/latestConsentedConsentOrder/";
    private static final String AUTH_TOKEN = "token-;";

    @Autowired
    private ConsentOrderService consentOrderService;

    @MockBean
    private BulkPrintDocumentService service;

    private CallbackRequest callbackRequest;

    private void setUpCaseDetails(String fileName) throws Exception {
        try (InputStream resourceAsStream =
                 getClass().getResourceAsStream(PATH + fileName)) {
            callbackRequest = new ObjectMapper().readValue(resourceAsStream, CallbackRequest.class);
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

        VariationOrderCollection orderCollectionBefore = getObj(VariationTypeOfDocument.ORIGINAL_ORDER, true);;

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
    public void shouldSetLatestDraftConsentOrderWhenACaseIsCreated() throws Exception {
        setUpCaseDetails("draft-consent-order.json");
        CaseDocument latestConsentOrderData = consentOrderService.getLatestConsentOrderData(callbackRequest);
        assertThat(latestConsentOrderData.getDocumentBinaryUrl(), is("http://file1.binary"));
        assertThat(latestConsentOrderData.getDocumentFilename(), is("file1"));
        assertThat(latestConsentOrderData.getDocumentUrl(), is("http://file1"));
        assertNull(callbackRequest.getCaseDetails().getData().get("otherVariationCollection"));
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
    public void shouldUpdateLatestDraftConsentOrderWhenACaseIsAmended() throws Exception {
        setUpCaseDetails("amend-consent-order-by-solicitor.json");
        CaseDocument latestConsentOrderData = consentOrderService.getLatestConsentOrderData(callbackRequest);
        assertThat(latestConsentOrderData.getDocumentBinaryUrl(), is("http://file2.binary"));
        assertThat(latestConsentOrderData.getDocumentFilename(), is("file2"));
        assertThat(latestConsentOrderData.getDocumentUrl(), is("http://file2"));
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
    public void shouldReturnLatestAmendedConsentOrderWhenACaseIsAmendedByCaseWorker() throws Exception {
        setUpCaseDetails("amend-consent-order-by-caseworker.json");
        CaseDocument latestConsentOrderData = consentOrderService.getLatestConsentOrderData(callbackRequest);
        assertThat(latestConsentOrderData.getDocumentUrl(),
            is("http://dm-store:8080/documents/0bdc0d68-e654-4faa-848a-8ae3c478838"));
        assertThat(latestConsentOrderData.getDocumentFilename(),
            is("Notification for ABC - Contested.docx"));
        assertThat(latestConsentOrderData.getDocumentBinaryUrl(),
            is("http://dm-store:8080/documents/0bdc0d68-e654-4faa-848a-8ae3c478838/binary"));
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
    public void shouldReturnLatestAmendedConsentOrderWhenACaseIsRespondedBySolicitor() throws Exception {
        setUpCaseDetails("respond-to-order-solicitor.json");
        CaseDocument latestConsentOrderData = consentOrderService.getLatestConsentOrderData(callbackRequest);
        assertThat(latestConsentOrderData.getDocumentUrl(), is("http://doc2"));
        assertThat(latestConsentOrderData.getDocumentFilename(), is("doc2"));
        assertThat(latestConsentOrderData.getDocumentBinaryUrl(), is("http://doc2/binary"));
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
        Map<String, Object> dataBefore = new HashMap<>();
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

    private CallbackRequest buildCallbackRequest() {
        Map<String, Object> caseData = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().id(123L).build();
        CaseDetails caseDetailsBefore = CaseDetails.builder().id(123L).build();
        caseDetails.setData(caseData);
        return CallbackRequest.builder().eventId(EventType.SOLICITOR_CREATE.getCcdType())
            .caseDetails(caseDetails).caseDetailsBefore(caseDetailsBefore).build();
    }
}