package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentgenerator;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.DocmosisPdfGenerationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.DocumentManagementService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementUploadService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.util.TestResource.fileUploadResponse;
import static uk.gov.hmcts.reform.finrem.caseorchestration.util.TestResource.testDocument;

@RunWith(MockitoJUnitRunner.class)
public class DocumentManagementServiceTest {

    public static final String TEMPLATE_NAME = "templateName";
    public static final ImmutableMap<String, Object> PLACEHOLDERS = ImmutableMap.of("key", "value");
    private static final String AUTH_TOKEN = "Bearer BBJHJbbIIBHBLB";
    private static final String FILE_NAME = "kbjh87y8y9JHVKKKJVJ";
    private static final String FILE_URL = "http://dm:80/documents/kbjh87y8y9JHVKKKJVJ";

    @InjectMocks private DocumentManagementService service;

    @Mock private DocmosisPdfGenerationService pdfGenerationService;
    @Mock private EvidenceManagementUploadService evidenceManagementUploadService;
    @Mock private EvidenceManagementDeleteService evidenceManagementDeleteService;

    @Before
    public void setUp() {
        when(pdfGenerationService.generateDocFrom(TEMPLATE_NAME, PLACEHOLDERS)).thenReturn("welcome doc".getBytes());
        when(
            evidenceManagementUploadService.upload(any(), anyString()))
            .thenReturn(fileUploadResponse());
    }

    @Test
    public void storeDocument() {
        Document document = service.storeDocument(TEMPLATE_NAME, FILE_NAME, PLACEHOLDERS, AUTH_TOKEN);
        assertThat(document, is(equalTo(testDocument())));
    }

    @Test
    public void deleteDocument() {
        service.deleteDocument(FILE_URL, AUTH_TOKEN);
        verify(evidenceManagementDeleteService).deleteFile(FILE_URL, AUTH_TOKEN);
    }
}
