package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentValidationResponse;

import java.io.InputStream;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentValidationResponse.builder;

public class DocumentValidationServiceTest extends BaseServiceTest {
    private static final String PATH = "/fixtures/latestConsentedConsentOrder/";
    private static final String BEARER_TOKEN = "Bearer eyJhbGciOiJIUzI1NiJ9";

    @Autowired
    private DocumentValidationService documentValidationService;

    @MockBean
    private DocumentClient documentClient;


    private CallbackRequest callbackRequest;
    private ObjectMapper objectMapper = new ObjectMapper();

    private void setUpCaseDetails(String fileName) throws Exception {
        try (InputStream resourceAsStream =
                     getClass().getResourceAsStream(PATH + fileName)) {
            callbackRequest = objectMapper.readValue(resourceAsStream, CallbackRequest.class);
        }
    }

    @Test
    public void shouldSuccessWhenFileTypeValidationForConsentOrderField() throws Exception {
        setUpCaseDetails("draft-consent-order.json");
        DocumentValidationResponse documentValidationResponse = builder()
                .mimeType("application/pdf")
                .build();

        when(documentClient.checkUploadedFileType(BEARER_TOKEN, "http://file1.binary"))
                .thenReturn(documentValidationResponse);
        DocumentValidationResponse response = documentValidationService.validateDocument(callbackRequest,
                "consentOrder", BEARER_TOKEN);
        assertThat(response.getMimeType(), is("application/pdf"));
    }

    @Test
    public void shouldThrowErrorWhenFileTypeValidationForConsentOrderField() throws Exception {
        setUpCaseDetails("draft-consent-order.json");
        DocumentValidationResponse documentValidationResponse = builder()
                .errors(singletonList("Invalid file type"))
                .build();

        when(documentClient.checkUploadedFileType(BEARER_TOKEN, "http://file1.binary"))
                .thenReturn(documentValidationResponse);
        DocumentValidationResponse response = documentValidationService.validateDocument(callbackRequest,
                "consentOrder", BEARER_TOKEN);
        assertThat(response.getErrors(), hasItem("Invalid file type"));
    }

    @Test
    public void shouldSuccessWhenFileTypeValidationForAmendConsentOrderCollection() throws Exception {
        setUpCaseDetails("amend-consent-order-by-caseworker.json");
        DocumentValidationResponse documentValidationResponse = builder().mimeType("application/pdf").build();

        when(documentClient.checkUploadedFileType(BEARER_TOKEN,
                "http://dm-store:8080/documents/0bdc0d68-e654-4faa-848a-8ae3c478838/binary"))
                .thenReturn(documentValidationResponse);
        DocumentValidationResponse response = documentValidationService.validateDocument(callbackRequest,
                "amendedConsentOrderCollection", BEARER_TOKEN);
        assertThat(response.getMimeType(), is("application/pdf"));
    }

    @Test
    public void shouldThrowErrorWhenFileTypeValidationForAmendConsentOrderCollection() throws Exception {
        setUpCaseDetails("amend-consent-order-by-caseworker.json");
        DocumentValidationResponse documentValidationResponse = builder()
                .errors(singletonList("Invalid file type"))
                .build();

        when(documentClient.checkUploadedFileType(BEARER_TOKEN,
                "http://dm-store:8080/documents/0bdc0d68-e654-4faa-848a-8ae3c478838/binary"))
                .thenReturn(documentValidationResponse);
        DocumentValidationResponse response = documentValidationService.validateDocument(callbackRequest,
                "amendedConsentOrderCollection", BEARER_TOKEN);
        assertThat(response.getErrors(), hasItem("Invalid file type"));
    }

    @Test
    public void shouldSuccessWhenFileTypeValidationForPensionCollection() throws Exception {
        setUpCaseDetails("validate-pension-collection.json");
        DocumentValidationResponse documentValidationResponse = builder().mimeType("application/pdf").build();

        when(documentClient.checkUploadedFileType(BEARER_TOKEN,
                "http://file1.binary"))
                .thenReturn(documentValidationResponse);
        when(documentClient.checkUploadedFileType(BEARER_TOKEN,
                "http://file2.binary"))
                .thenReturn(documentValidationResponse);
        DocumentValidationResponse response = documentValidationService.validateDocument(callbackRequest,
                "pensionCollection", BEARER_TOKEN);
        assertThat(response.getErrors(), nullValue());
    }

    @Test
    public void shouldReturnErrorWhenFileTypeValidationForPensionCollection() throws Exception {
        setUpCaseDetails("validate-pension-collection.json");
        DocumentValidationResponse documentValidationResponse1 = builder().mimeType("application/pdf").build();
        DocumentValidationResponse documentValidationResponse2 = builder()
                .errors(singletonList("Invalid file type")).build();

        when(documentClient.checkUploadedFileType(BEARER_TOKEN,
                "http://file1.binary"))
                .thenReturn(documentValidationResponse1);
        when(documentClient.checkUploadedFileType(BEARER_TOKEN,
                "http://file2.binary"))
                .thenReturn(documentValidationResponse2);
        DocumentValidationResponse response = documentValidationService.validateDocument(callbackRequest,
                "pensionCollection", BEARER_TOKEN);
        assertThat(response.getErrors(), hasItem("Invalid file type"));
    }

    @Test
    public void shouldNotThrowErrorWhenFileTypeValidationForEmptyPensionCollection() throws Exception {
        setUpCaseDetails("validate-pension-collection-without-pension-data.json");
        DocumentValidationResponse response = documentValidationService.validateDocument(callbackRequest,
                "pensionCollection", BEARER_TOKEN);

        assertThat(response.getErrors(), nullValue());
    }

    @Test
    public void shouldReturnSuccessWhenFileTypeValidationForRespondToOrderCollection() throws Exception {
        setUpCaseDetails("respond-to-order-solicitor.json");
        DocumentValidationResponse documentValidationResponse = builder().mimeType("application/pdf").build();
        when(documentClient.checkUploadedFileType(BEARER_TOKEN,
                "http://doc2/binary"))
                .thenReturn(documentValidationResponse);

        DocumentValidationResponse response = documentValidationService.validateDocument(callbackRequest,
                "respondToOrderDocuments", BEARER_TOKEN);
        assertThat(response.getErrors(), nullValue());
    }

    @Test
    public void shouldReturnErrorWhenFileTypeValidationForRespondToOrderCollection() throws Exception {
        setUpCaseDetails("respond-to-order-solicitor.json");
        DocumentValidationResponse response = documentValidationService.validateDocument(callbackRequest,
                "respondToOrderDocuments", BEARER_TOKEN);
        assertThat(response.getErrors(), nullValue());
    }

    @Test
    public void shouldReturnErrorWhenFileTypeValidationWithInvalidField() throws Exception {
        setUpCaseDetails("respond-to-order-solicitor.json");
        DocumentValidationResponse response = documentValidationService.validateDocument(callbackRequest,
                "ssss", BEARER_TOKEN);
        assertThat(response.getErrors(), nullValue());
    }

}