package uk.gov.hmcts.reform.finrem.caseorchestration;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintDocumentGeneratorService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.DocmosisPdfGenerationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.DocumentGeneratorValidationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.EvidenceManagementUploadService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamAuthService;

@Profile("test-mock-feign-clients")
@Configuration
public class MockFeignClientsTestConfiguration {

    @Bean
    @Primary
    public DocumentClient documentClient() {
        return Mockito.mock(DocumentClient.class);
    }

    @Bean
    @Primary
    public DocumentGeneratorValidationService documentGeneratorValidationService() {
        return Mockito.mock(DocumentGeneratorValidationService.class);
    }

    @Bean
    @Primary
    public DocmosisPdfGenerationService pdfGenerationService() {
        return Mockito.mock(DocmosisPdfGenerationService.class);
    }

    @Bean
    @Primary
    public IdamAuthService getIdamAuthService() {
        return Mockito.mock(IdamAuthService.class);
    }

    @Bean
    @Primary
    public EvidenceManagementUploadService getEvidenceManagementUploadService() {
        return Mockito.mock(EvidenceManagementUploadService.class);
    }

    @Bean
    @Primary
    public BulkPrintDocumentGeneratorService getBulkPrintDocumentGeneratorService() {
        return Mockito.mock(BulkPrintDocumentGeneratorService.class);
    }

    @Bean
    @Primary
    public BulkPrintDocumentService getBulkPrintDocumentService() {
        return Mockito.mock(BulkPrintDocumentService.class);
    }
}
