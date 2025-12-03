package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.PdfDocumentConfig;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.PdfGenerationException;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocmosisPdfGenerationServiceTest {

    private static final String FILE_CONTENT = "Welcome to PDF document service";
    private static final String TEMPLATE_NAME = "template name";
    private static final String PDF_SERVICE_URI = "https://docmosis.platform.hmcts.net/rs/render";
    private static final String ACCESS_KEY = "dummy-access-key";

    private static final ImmutableMap<String, Object> PLACEHOLDERS_WITH_CASE_DETAILS =
        ImmutableMap.of("caseDetails", CaseDetails.builder().data(caseDataMap()).build());

    private static Map<String, Object> caseDataMap() {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("PBANumber", "PBA123456");

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("case_data", dataMap);

        return caseDataMap;
    }

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private PdfDocumentConfig pdfDocumentConfig;

    @InjectMocks
    private DocmosisPdfGenerationService pdfGenerationService;

    @BeforeEach
    void setUp() throws Exception {
        setField(pdfGenerationService, "pdfServiceEndpoint", PDF_SERVICE_URI);
        setField(pdfGenerationService, "pdfServiceAccessKey", ACCESS_KEY);
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    // ---------- Happy path tests ----------

    @Test
    void generatePdfDocument_withCaseDetails_success() {
        byte[] body = FILE_CONTENT.getBytes(StandardCharsets.UTF_8);
        ResponseEntity<byte[]> response = new ResponseEntity<>(body, HttpStatus.OK);

        when(restTemplate.postForEntity(eq(PDF_SERVICE_URI), any(), eq(byte[].class)))
            .thenReturn(response);

        byte[] result = pdfGenerationService.generateDocFrom(TEMPLATE_NAME, PLACEHOLDERS_WITH_CASE_DETAILS);

        assertThat(result, is(notNullValue()));
        assertArrayEquals(body, result);

        // RestTemplate called once (no retry in this pure unit test)
        verify(restTemplate, times(1))
            .postForEntity(eq(PDF_SERVICE_URI), any(), eq(byte[].class));

        // After success, config keys should have been added then removed again
        CaseDetails caseDetails = (CaseDetails) PLACEHOLDERS_WITH_CASE_DETAILS.get("caseDetails");
        Map<String, Object> innerData =
            (Map<String, Object>) caseDetails.getData().get("case_data");

        // Final state should not contain the config keys
        assertThat(innerData.containsKey("displayTemplateKey"), is(false));
        assertThat(innerData.containsKey("familyCourtImgKey"), is(false));
        assertThat(innerData.containsKey("hmctsImgKey"), is(false));
    }

    @Test
    void generatePdfDocument_withRawMapCaseDetails_success() {
        // Covers branch where caseDetails is a Map, not CaseDetails
        Map<String, Object> innerData = new HashMap<>();
        innerData.put("PBANumber", "PBA123456");

        Map<String, Object> caseData = new HashMap<>();
        caseData.put("case_data", innerData);

        Map<String, Object> placeholders =
            ImmutableMap.<String, Object>builder()
                .put("caseDetails", caseData)
                .build();

        byte[] body = FILE_CONTENT.getBytes(StandardCharsets.UTF_8);
        ResponseEntity<byte[]> response = new ResponseEntity<>(body, HttpStatus.OK);

        when(restTemplate.postForEntity(eq(PDF_SERVICE_URI), any(), eq(byte[].class)))
            .thenReturn(response);

        byte[] result = pdfGenerationService.generateDocFrom(TEMPLATE_NAME, placeholders);

        assertThat(result, is(notNullValue()));
        assertArrayEquals(body, result);

        Map<String, Object> caseDetailsMap = (Map<String, Object>) placeholders.get("caseDetails");
        Map<String, Object> updatedInnerData = (Map<String, Object>) caseDetailsMap.get("case_data");

        assertThat(updatedInnerData.containsKey("displayTemplateKey"), is(false));
        assertThat(updatedInnerData.containsKey("familyCourtImgKey"), is(false));
        assertThat(updatedInnerData.containsKey("hmctsImgKey"), is(false));
    }

    // ---------- Non-retryable error (4xx) ----------

    @Test
    void generatePdfDocument_4xx_shouldWrapInPdfGenerationException() {
        HttpClientErrorException badRequest = HttpClientErrorException.create(
            HttpStatus.BAD_REQUEST,
            "Bad Request",
            null,
            null,
            null
        );

        when(restTemplate.postForEntity(eq(PDF_SERVICE_URI), any(), eq(byte[].class)))
            .thenThrow(badRequest);

        PdfGenerationException ex = assertThrows(
            PdfGenerationException.class,
            () -> pdfGenerationService.generateDocFrom(TEMPLATE_NAME, PLACEHOLDERS_WITH_CASE_DETAILS)
        );

        assertInstanceOf(HttpClientErrorException.class, ex.getCause());
        HttpClientErrorException cause = (HttpClientErrorException) ex.getCause();
        assertThat(cause.getStatusCode(), is(HttpStatus.BAD_REQUEST));

        // No retry in pure unit test → single call
        verify(restTemplate, times(1))
            .postForEntity(eq(PDF_SERVICE_URI), any(), eq(byte[].class));

    }

    // ---------- "Retryable" exception types, plain behaviour here ----------

    @Test
    void generatePdfDocument_500_shouldRethrowHttpServerErrorException() {
        HttpServerErrorException serverError = HttpServerErrorException.create(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Ouch, 500",
            null,
            "<html>...</html>".getBytes(StandardCharsets.UTF_8),
            null
        );

        when(restTemplate.postForEntity(eq(PDF_SERVICE_URI), any(), eq(byte[].class)))
            .thenThrow(serverError);

        // In this pure unit test (no @Retryable), the method should just rethrow the HttpServerErrorException
        HttpServerErrorException ex = assertThrows(
            HttpServerErrorException.class,
            () -> pdfGenerationService.generateDocFrom(TEMPLATE_NAME, PLACEHOLDERS_WITH_CASE_DETAILS)
        );

        assertThat(ex.getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));

        verify(restTemplate, times(1))
            .postForEntity(eq(PDF_SERVICE_URI), any(), eq(byte[].class));
    }

    @Test
    void generatePdfDocument_networkIssue_shouldRethrowResourceAccessException() {
        ResourceAccessException networkIssue = new ResourceAccessException("Timeout");

        when(restTemplate.postForEntity(eq(PDF_SERVICE_URI), any(), eq(byte[].class)))
            .thenThrow(networkIssue);

        ResourceAccessException ex = assertThrows(
            ResourceAccessException.class,
            () -> pdfGenerationService.generateDocFrom(TEMPLATE_NAME, PLACEHOLDERS_WITH_CASE_DETAILS)
        );

        assertThat(ex.getMessage(), equalTo("Timeout"));

        verify(restTemplate, times(1))
            .postForEntity(eq(PDF_SERVICE_URI), any(), eq(byte[].class));
    }

    @Test
    void recoverFromHttpServerErrorException_shouldWrapInPdfGenerationException() {
        // Arrange
        HttpServerErrorException ex = HttpServerErrorException.create(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "500",
            null,
            "<html>server-error</html>".getBytes(StandardCharsets.UTF_8),
            null
        );

        // Act
        PdfGenerationException thrown = assertThrows(
            PdfGenerationException.class,
            () -> pdfGenerationService.recover(ex, TEMPLATE_NAME, PLACEHOLDERS_WITH_CASE_DETAILS)
        );

        // Assert
        assertInstanceOf(HttpServerErrorException.class, thrown.getCause());
        assertThat(thrown.getMessage(),
            equalTo("Failed to generate PDF from Docmosis after retries for template [template name]"));
    }

    @Test
    void recoverFromResourceAccessException_shouldWrapInPdfGenerationException() {
        // Arrange
        ResourceAccessException ex = new ResourceAccessException("Timeout");

        // Act
        PdfGenerationException thrown = assertThrows(
            PdfGenerationException.class,
            () -> pdfGenerationService.recover(ex, TEMPLATE_NAME, PLACEHOLDERS_WITH_CASE_DETAILS)
        );

        // Assert
        assertInstanceOf(ResourceAccessException.class, thrown.getCause());
        assertThat(thrown.getMessage(),
            equalTo("Failed to generate PDF from Docmosis due to network issues for template [template name]"));
    }
    
    // ---------- Validation tests ----------

    @Test
    void emptyTemplateName_shouldThrowIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> pdfGenerationService.generateDocFrom("", PLACEHOLDERS_WITH_CASE_DETAILS)
        );
        assertThat(ex.getMessage(), equalTo("document generation template cannot be empty"));
    }

    @Test
    void nullTemplateName_shouldThrowIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> pdfGenerationService.generateDocFrom(null, PLACEHOLDERS_WITH_CASE_DETAILS)
        );
        assertThat(ex.getMessage(), equalTo("document generation template cannot be empty"));
    }

    @Test
    void nullPlaceHoldersMap_shouldThrowNullPointerException() {
        assertThrows(
            NullPointerException.class,
            () -> pdfGenerationService.generateDocFrom(TEMPLATE_NAME, null)
        );
    }
}
