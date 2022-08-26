package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OldCallbackRequest;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest;
import uk.gov.hmcts.reform.finrem.ccd.domain.AmendedConsentOrder;
import uk.gov.hmcts.reform.finrem.ccd.domain.AmendedConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.EventType;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.RespondToOrderDocument;
import uk.gov.hmcts.reform.finrem.ccd.domain.RespondToOrderDocumentCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.RespondToOrderDocumentType;

import java.io.InputStream;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ConsentOrderServiceTest extends BaseServiceTest {

    private static final String PATH = "/fixtures/latestConsentedConsentOrder/";

    @Autowired
    private ConsentOrderService consentOrderService;

    private OldCallbackRequest callbackRequest;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private void setUpCaseDetails(String fileName) throws Exception {
        try (InputStream resourceAsStream =
                 getClass().getResourceAsStream(PATH + fileName)) {
            callbackRequest = objectMapper.readValue(resourceAsStream, OldCallbackRequest.class);
        }
    }

    @Test
    public void shouldSetLatestDraftConsentOrderWhenACaseIsCreated() throws Exception {
        setUpCaseDetails("draft-consent-order.json");
        CaseDocument latestConsentOrderData = consentOrderService.getLatestConsentOrderData(callbackRequest);
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
    public void shouldReturnLatestAmendedConsentOrderWhenACaseIsRespondedBySolicitor() throws Exception {
        setUpCaseDetails("respond-to-order-solicitor.json");
        CaseDocument latestConsentOrderData = consentOrderService.getLatestConsentOrderData(callbackRequest);
        assertThat(latestConsentOrderData.getDocumentUrl(), is("http://doc2"));
        assertThat(latestConsentOrderData.getDocumentFilename(), is("doc2"));
        assertThat(latestConsentOrderData.getDocumentBinaryUrl(), is("http://doc2/binary"));
    }

    @Test
    public void givenValidCaseDataAndRespondToOrderEvent_whenGetLatestConsentOrder_thenReturnLatestOrder() {
        CallbackRequest callback = setUpCallbackRequest(EventType.RESPOND_TO_ORDER);

        Document latestConsentOrderData = consentOrderService.getLatestConsentOrderData(callback);

        assertThat(latestConsentOrderData.getFilename(), is("testFilename"));
    }

    @Test
    public void givenRespondToOrderEventAndNoRespondToOrderDocCollection_whenGetLatestConsentOrder_thenReturnLatestOrder() {
        CallbackRequest callback = setUpCallbackRequest(EventType.RESPOND_TO_ORDER);
        callback.getCaseDetails().getCaseData().setRespondToOrderDocuments(null);
        callback.getCaseDetails().getCaseData().setLatestConsentOrder(Document.builder().filename("latestOrder").build());

        Document latestConsentOrderData = consentOrderService.getLatestConsentOrderData(callback);

        assertThat(latestConsentOrderData.getFilename(), is("latestOrder"));
    }

    @Test
    public void givenAmendedConsentOrderEvent_whenGetLatestConsentOrderData_thenReturnLatestOrder() {
        CallbackRequest callback = setUpCallbackRequest(EventType.AMENDED_CONSENT_ORDER);
        callback.getCaseDetails().getCaseData().setAmendedConsentOrderCollection(getAmendedConsentOrderCollection());

        Document latestConsentOrderData = consentOrderService.getLatestConsentOrderData(callback);

        assertThat(latestConsentOrderData.getFilename(), is("amendedConsentOrderLatest"));
    }

    @Test
    public void givenAmendedConsentOrderEventAndNoAmendedConsentOrders_whenGetLatestConsentOrderData_thenReturnLatestOrder() {
        CallbackRequest callback = setUpCallbackRequest(EventType.AMENDED_CONSENT_ORDER);
        callback.getCaseDetails().getCaseData().setLatestConsentOrder(Document.builder().filename("latestConsentOrder").build());

        Document latestConsentOrderData = consentOrderService.getLatestConsentOrderData(callback);

        assertThat(latestConsentOrderData.getFilename(), is("latestConsentOrder"));
    }

    private List<AmendedConsentOrderCollection> getAmendedConsentOrderCollection() {
        return List.of(
            AmendedConsentOrderCollection.builder()
                .value(AmendedConsentOrder.builder()
                    .amendedConsentOrder(Document.builder().filename("amendedConsentOrder").build())
                    .build())
                .build(),
            AmendedConsentOrderCollection.builder()
                .value(AmendedConsentOrder.builder()
                    .amendedConsentOrder(Document.builder().filename("amendedConsentOrderLatest").build())
                    .build())
                .build()
        );
    }

    private CallbackRequest setUpCallbackRequest(EventType eventType) {
        FinremCaseData caseData = new FinremCaseData();
        caseData.setRespondToOrderDocuments(List.of(
                RespondToOrderDocumentCollection.builder()
                    .value(RespondToOrderDocument.builder()
                        .documentType(RespondToOrderDocumentType.AMENDED_CONSENT_ORDER)
                        .documentLink(Document.builder().filename("testFilename").build())
                        .build())
                    .build(),
            RespondToOrderDocumentCollection.builder()
                .value(RespondToOrderDocument.builder()
                    .documentType(RespondToOrderDocumentType.OTHER)
                    .documentLink(Document.builder().filename("otherFilename").build())
                    .build())
                .build()
        ));

        return CallbackRequest.builder()
            .eventType(eventType)
            .caseDetails(FinremCaseDetails.builder().caseData(caseData).build())
            .build();
    }
}