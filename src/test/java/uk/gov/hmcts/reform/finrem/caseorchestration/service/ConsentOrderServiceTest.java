package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

import java.io.InputStream;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ConsentOrderServiceTest extends BaseServiceTest {

    private static final String PATH = "/fixtures/latestConsentedConsentOrder/";

    @Autowired
    private ConsentOrderService consentOrderService;

    private CallbackRequest callbackRequest;
    private ObjectMapper objectMapper = new ObjectMapper();

    private void setUpCaseDetails(String fileName) throws Exception {
        try (InputStream resourceAsStream =
                 getClass().getResourceAsStream(PATH + fileName)) {
            callbackRequest = objectMapper.readValue(resourceAsStream, CallbackRequest.class);
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
}