package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.PdfDocumentConfig;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.PdfGenerationException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.PdfDocumentRequest;

import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocmosisPdfGenerationService {

    private static final String CASE_DETAILS = "caseDetails";
    private static final String CASE_DATA = "case_data";

    private final RestTemplate restTemplate;
    private final PdfDocumentConfig pdfDocumentConfig;

    @Value("${service.pdf-service.render-uri}")
    private String pdfServiceEndpoint;

    @Value("${service.pdf-service.accessKey}")
    private String pdfServiceAccessKey;

    @Retryable(
       // Only retry Docmosis request network or server errors
        value = {
            HttpServerErrorException.class,   // 5oos
            ResourceAccessException.class     // timeouts, connection issues
        },
        backoff = @Backoff(
            delay = 1000,      // 1s
            multiplier = 2.0   // 1s, 2s, 4s
        )
    )
    public byte[] generateDocFrom(String templateName, Map<String, Object> placeholders) {
        checkArgument(templateName != null && !templateName.isBlank(),
            "document generation template cannot be empty");
        checkNotNull(placeholders, "placeholders map cannot be null");

        log.info("Calling Docmosis to generate pdf. template=[{}], placeholdersSize=[{}], endpoint=[{}]",
            templateName, placeholders.size(), pdfServiceEndpoint);

        try {
            ResponseEntity<byte[]> response =
                restTemplate.postForEntity(pdfServiceEndpoint, request(templateName, placeholders), byte[].class);

            removePdfConfigEntriesFromCaseData(placeholders);
            return response.getBody();

            // Retryable exceptions – log and rethrow so @Retryable can handle them
        } catch (HttpServerErrorException | ResourceAccessException ex) {
            log.warn("Docmosis transient failure for template [{}], will be retried. Message: {}",
                templateName, ex.getMessage(), ex);
            throw ex; // important: keep the same type so retry happens

            // Non-retryable exceptions – handle once and wrap in PdfGenerationException
        } catch (Exception ex) {
            log.error("Non-retryable error when generating PDF for template [{}]: {}",
                templateName, ex.getMessage(), ex);
            throw new PdfGenerationException(
                String.format("Failed to generate PDF from Docmosis for template [%s]", templateName),
                ex
            );
        }
    }

    private void removePdfConfigEntriesFromCaseData(Map<String, Object> placeholders) {
        Object caseDetails = placeholders.get(CASE_DETAILS);

        Map<String, Object> data = caseDetails instanceof CaseDetails
            ? ((CaseDetails) caseDetails).getData()
            : (Map<String, Object>) ((Map<String, Object>)  caseDetails).get(CASE_DATA);
        data.remove(pdfDocumentConfig.getDisplayTemplateKey());
        data.remove(pdfDocumentConfig.getFamilyCourtImgKey());
        data.remove(pdfDocumentConfig.getHmctsImgKey());
    }

    private PdfDocumentRequest request(String templateName, Map<String, Object> placeholders) {
        return PdfDocumentRequest.builder()
                .accessKey(pdfServiceAccessKey)
                .templateName(templateName)
                .outputName("result.pdf")
                .data(caseData(placeholders)).build();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> caseData(Map<String, Object> placeholders) {

        Object caseDetails = placeholders.get(CASE_DETAILS);

        Map<String, Object> data = caseDetails instanceof CaseDetails
            ? ((CaseDetails) caseDetails).getData()
            : (Map<String, Object>) ((Map<String, Object>)  caseDetails).get(CASE_DATA);
        data.put(pdfDocumentConfig.getDisplayTemplateKey(), pdfDocumentConfig.getDisplayTemplateVal());
        data.put(pdfDocumentConfig.getFamilyCourtImgKey(), pdfDocumentConfig.getFamilyCourtImgVal());
        data.put(pdfDocumentConfig.getHmctsImgKey(), pdfDocumentConfig.getHmctsImgVal());

        return data;
    }

    // Called when retries for HttpServerErrorException are exhausted
    @Recover
    public byte[] recover(HttpServerErrorException ex,
                          String templateName,
                          Map<String, Object> placeholders) {
        log.error("Docmosis returned 5xx after retries for template [{}]. Status: {}, Body: {}",
            templateName, ex.getStatusCode(), ex.getResponseBodyAsString(), ex);

        throw new PdfGenerationException(
            String.format("Failed to generate PDF from Docmosis after retries for template [%s]", templateName),
            ex
        );
    }

    // Called when retries for ResourceAccessException are exhausted
    @Recover
    public byte[] recover(ResourceAccessException ex,
                          String templateName,
                          Map<String, Object> placeholders) {
        log.error("Docmosis network issue after retries for template [{}]. Message: {}",
            templateName, ex.getMessage(), ex);

        throw new PdfGenerationException(
            String.format("Failed to generate PDF from Docmosis due to network issues for template [%s]", templateName),
            ex
        );
    }
}
