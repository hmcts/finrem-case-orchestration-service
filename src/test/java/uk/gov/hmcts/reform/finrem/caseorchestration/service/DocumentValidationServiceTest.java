package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.AMENDED_CONSENT_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_CONSENT_PENSION_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PENSION_DOCS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPOND_TO_ORDER_DOCUMENTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentValidationResponse.builder;

@ActiveProfiles("test-mock-feign-clients")
public class DocumentValidationServiceTest extends BaseServiceTest {

    private static final String PATH = "/fixtures/latestConsentedConsentOrder/";
    private static final String APPLICATION_PDF = "application/pdf";
    private static final String DRAFT_CONSENT_ORDER_JSON = "draft-consent-order.json";
    private static final String VALIDATE_PENSION_COLLECTION_JSON = "validate-pension-collection.json";
    private static final String VALIDATE_PENSION_COLLECTION_WITHOUT_DATA_JSON = "validate-pension-collection-without-pension-data.json";
    private static final String RESPOND_TO_ORDER_SOL_JSON = "respond-to-order-solicitor.json";
    private static final String CONSENT_IN_CONTESTED = "consented-in-consented.json";
    private static final String CONSENT_IN_CONTESTED_NO_PENSION_COLLECTION = "consented-in-consented-no-pension-collection.json";

    @Autowired private DocumentValidationService documentValidationService;
    @Autowired private DocumentClient documentClientMock;
    @Autowired private ObjectMapper objectMapper;

    private CallbackRequest callbackRequest;

    private void setUpCaseDetails(String fileName) throws Exception {
        try (InputStream resourceAsStream =
                 getClass().getResourceAsStream(PATH + fileName)) {
            callbackRequest = objectMapper.readValue(resourceAsStream, CallbackRequest.class);
        }
    }

    @Test
    public void shouldSuccessWhenFileTypeValidationForConsentOrderField() throws Exception {
        setUpCaseDetails(DRAFT_CONSENT_ORDER_JSON);
        DocumentValidationResponse documentValidationResponse = builder()
            .mimeType(APPLICATION_PDF)
            .build();

        when(documentClientMock.checkUploadedFileType(AUTH_TOKEN, "http://file1.binary"))
            .thenReturn(documentValidationResponse);
        DocumentValidationResponse response = documentValidationService.validateDocument(callbackRequest,
            CONSENT_ORDER, AUTH_TOKEN);
        assertThat(response.getMimeType(), is(APPLICATION_PDF));
    }

    @Test
    public void shouldThrowErrorWhenFileTypeValidationForConsentOrderField() throws Exception {
        setUpCaseDetails(DRAFT_CONSENT_ORDER_JSON);
        DocumentValidationResponse documentValidationResponse = builder()
            .errors(singletonList("Invalid file type"))
            .build();

        when(documentClientMock.checkUploadedFileType(AUTH_TOKEN, "http://file1.binary"))
            .thenReturn(documentValidationResponse);
        DocumentValidationResponse response = documentValidationService.validateDocument(callbackRequest,
            CONSENT_ORDER, AUTH_TOKEN);
        assertThat(response.getErrors(), hasItem("Invalid file type"));
    }

    @Test
    public void shouldSuccessWhenFileTypeValidationForAmendConsentOrderCollection() throws Exception {
        setUpCaseDetails("amend-consent-order-by-caseworker.json");
        DocumentValidationResponse documentValidationResponse = builder().mimeType(APPLICATION_PDF).build();

        when(documentClientMock.checkUploadedFileType(AUTH_TOKEN,
            "http://dm-store:8080/documents/0bdc0d68-e654-4faa-848a-8ae3c478838/binary"))
            .thenReturn(documentValidationResponse);
        DocumentValidationResponse response = documentValidationService.validateDocument(callbackRequest,
            AMENDED_CONSENT_ORDER_COLLECTION, AUTH_TOKEN);
        assertThat(response.getMimeType(), is(APPLICATION_PDF));
    }

    @Test
    public void shouldThrowErrorWhenFileTypeValidationForAmendConsentOrderCollection() throws Exception {
        setUpCaseDetails("amend-consent-order-by-caseworker.json");
        DocumentValidationResponse documentValidationResponse = builder()
            .errors(singletonList("Invalid file type"))
            .build();

        when(documentClientMock.checkUploadedFileType(AUTH_TOKEN,
            "http://dm-store:8080/documents/0bdc0d68-e654-4faa-848a-8ae3c478838/binary"))
            .thenReturn(documentValidationResponse);
        DocumentValidationResponse response = documentValidationService.validateDocument(callbackRequest,
            AMENDED_CONSENT_ORDER_COLLECTION, AUTH_TOKEN);
        assertThat(response.getErrors(), hasItem("Invalid file type"));
    }

    @Test
    public void shouldSuccessWhenFileTypeValidationForPensionCollection() throws Exception {
        setUpCaseDetails(VALIDATE_PENSION_COLLECTION_JSON);
        DocumentValidationResponse documentValidationResponse = builder().mimeType(APPLICATION_PDF).build();

        when(documentClientMock.checkUploadedFileType(AUTH_TOKEN,
            "http://file1.binary"))
            .thenReturn(documentValidationResponse);
        when(documentClientMock.checkUploadedFileType(AUTH_TOKEN,
            "http://file2.binary"))
            .thenReturn(documentValidationResponse);
        DocumentValidationResponse response = documentValidationService.validateDocument(callbackRequest,
            PENSION_DOCS_COLLECTION, AUTH_TOKEN);
        assertThat(response.getErrors(), nullValue());
    }

    @Test
    public void shouldReturnErrorWhenFileTypeValidationForPensionCollection() throws Exception {
        setUpCaseDetails(VALIDATE_PENSION_COLLECTION_JSON);
        DocumentValidationResponse documentValidationResponse1 = builder().mimeType(APPLICATION_PDF).build();
        DocumentValidationResponse documentValidationResponse2 = builder()
            .errors(singletonList("Invalid file type")).build();

        when(documentClientMock.checkUploadedFileType(AUTH_TOKEN,
            "http://file1.binary"))
            .thenReturn(documentValidationResponse1);
        when(documentClientMock.checkUploadedFileType(AUTH_TOKEN,
            "http://file2.binary"))
            .thenReturn(documentValidationResponse2);
        DocumentValidationResponse response = documentValidationService.validateDocument(callbackRequest,
            PENSION_DOCS_COLLECTION, AUTH_TOKEN);
        assertThat(response.getErrors(), hasItem("Invalid file type"));
    }

    @Test
    public void shouldNotThrowErrorWhenFileTypeValidationForEmptyPensionCollection() throws Exception {
        setUpCaseDetails(VALIDATE_PENSION_COLLECTION_WITHOUT_DATA_JSON);
        DocumentValidationResponse response = documentValidationService.validateDocument(callbackRequest,
            PENSION_DOCS_COLLECTION, AUTH_TOKEN);

        assertThat(response.getErrors(), nullValue());
    }

    @Test
    public void shouldReturnSuccessWhenFileTypeValidationForRespondToOrderCollection() throws Exception {
        setUpCaseDetails(RESPOND_TO_ORDER_SOL_JSON);
        DocumentValidationResponse documentValidationResponse = builder().mimeType(APPLICATION_PDF).build();
        when(documentClientMock.checkUploadedFileType(AUTH_TOKEN,
            "http://doc2/binary"))
            .thenReturn(documentValidationResponse);

        DocumentValidationResponse response = documentValidationService.validateDocument(callbackRequest,
            RESPOND_TO_ORDER_DOCUMENTS, AUTH_TOKEN);
        assertThat(response.getErrors(), nullValue());
    }

    @Test
    public void shouldReturnErrorWhenFileTypeValidationForRespondToOrderCollection() throws Exception {
        setUpCaseDetails(RESPOND_TO_ORDER_SOL_JSON);
        DocumentValidationResponse response = documentValidationService.validateDocument(callbackRequest,
            RESPOND_TO_ORDER_DOCUMENTS, AUTH_TOKEN);
        assertThat(response.getErrors(), nullValue());
    }

    @Test
    public void shouldReturnErrorWhenFileTypeValidationWithInvalidField() throws Exception {
        setUpCaseDetails(RESPOND_TO_ORDER_SOL_JSON);
        DocumentValidationResponse response = documentValidationService.validateDocument(callbackRequest,
            "ssss", AUTH_TOKEN);
        assertThat(response.getErrors(), nullValue());
    }

    @Test
    public void shouldValidateConsentInContestedConsentOrder() throws Exception {
        setUpCaseDetails(CONSENT_IN_CONTESTED);
        DocumentValidationResponse documentValidationResponse1 = builder().mimeType(APPLICATION_PDF).build();
        when(documentClientMock.checkUploadedFileType(AUTH_TOKEN,
            "http://file1.binary"))
            .thenReturn(documentValidationResponse1);
        DocumentValidationResponse response = documentValidationService.validateDocument(callbackRequest,
            CONSENT_ORDER, AUTH_TOKEN);
        assertThat(response.getErrors(), nullValue());
    }

    @Test
    public void shouldReturnErrorInConsentInContestedConsentOrderWhenInvalidType() throws Exception {
        setUpCaseDetails(CONSENT_IN_CONTESTED);
        DocumentValidationResponse documentValidationResponse1 = builder()
            .errors(singletonList("Invalid file type")).build();
        when(documentClientMock.checkUploadedFileType(AUTH_TOKEN,
            "http://file1.binary"))
            .thenReturn(documentValidationResponse1);
        DocumentValidationResponse response = documentValidationService.validateDocument(callbackRequest,
            CONSENT_ORDER, AUTH_TOKEN);
        assertThat(response.getErrors(), hasItem("Invalid file type"));
    }

    @Test
    public void shouldValidateConsentInContestedPensionCollection() throws Exception {
        setUpCaseDetails(CONSENT_IN_CONTESTED);

        DocumentValidationResponse documentValidationResponse1 = builder().mimeType(APPLICATION_PDF).build();
        when(documentClientMock.checkUploadedFileType(AUTH_TOKEN,
            "http://file1.binary"))
            .thenReturn(documentValidationResponse1);
        DocumentValidationResponse response = documentValidationService.validateDocument(callbackRequest,
            CONTESTED_CONSENT_PENSION_COLLECTION, AUTH_TOKEN);
        assertThat(response.getErrors(), nullValue());
    }

    @Test
    public void shouldReturnErrorInConsentInContestedPensionCollectionWhenInvalidtype() throws Exception {
        setUpCaseDetails(CONSENT_IN_CONTESTED);
        DocumentValidationResponse documentValidationResponse1 = builder()
            .errors(singletonList("Invalid file type")).build();
        DocumentValidationResponse documentValidationResponse2 = builder().mimeType(APPLICATION_PDF).build();

        when(documentClientMock.checkUploadedFileType(AUTH_TOKEN,
            "http://file1.binary"))
            .thenReturn(documentValidationResponse1);
        when(documentClientMock.checkUploadedFileType(AUTH_TOKEN,
            "http://file2.binary"))
            .thenReturn(documentValidationResponse2);
        DocumentValidationResponse response = documentValidationService.validateDocument(callbackRequest,
            CONTESTED_CONSENT_PENSION_COLLECTION, AUTH_TOKEN);
        assertThat(response.getErrors(), hasItem("Invalid file type"));
    }

    @Test
    public void shouldReturnValidForConsentInContestedPensionCollectionWhenNonePresent() throws Exception {
        setUpCaseDetails(CONSENT_IN_CONTESTED_NO_PENSION_COLLECTION);
        DocumentValidationResponse response = documentValidationService.validateDocument(callbackRequest,
            CONTESTED_CONSENT_PENSION_COLLECTION, AUTH_TOKEN);
        assertThat(response.getErrors(), nullValue());
    }

    @Test
    public void shouldNotValidateWhenFieldNotPresent() throws Exception {
        setUpCaseDetails(CONSENT_IN_CONTESTED_NO_PENSION_COLLECTION);
        DocumentValidationResponse response = documentValidationService.validateDocument(callbackRequest,
            "ssss", AUTH_TOKEN);
        assertThat(response.getErrors(), nullValue());
    }
}