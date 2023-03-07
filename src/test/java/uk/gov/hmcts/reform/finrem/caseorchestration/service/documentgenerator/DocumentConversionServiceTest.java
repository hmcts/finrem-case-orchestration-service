package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentgenerator;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.finrem.caseorchestration.CaseOrchestrationApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.DocumentConversionException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.DocumentConversionService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDownloadService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CaseOrchestrationApplication.class)
@TestPropertySource(locations = "/application.properties")
public class DocumentConversionServiceTest {

    public static final String PDF_SERVICE_URI = "http://localhost:4001/rs/convert";
    public static final byte[] CONVERTED_BINARY = "converted".getBytes();

    @Autowired
    private DocumentConversionService documentConversionService;

    @Autowired
    private RestTemplate restTemplate;

    @MockBean
    private EvidenceManagementDownloadService evidenceManagementService;

    private MockRestServiceServer mockServer;

    private Document documentToConvert = new Document();

    @Before
    public void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
        documentToConvert.setFileName("file.docx");
        documentToConvert.setUrl("docurl.com");
        documentToConvert.setBinaryUrl("binaryurl.com");
    }

    @Test
    public void convertWordToPdf() {
        mockServer.expect(requestTo(PDF_SERVICE_URI))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess(CONVERTED_BINARY, MediaType.APPLICATION_OCTET_STREAM));

        when(
            evidenceManagementService.download(ArgumentMatchers.eq(documentToConvert.getBinaryUrl())))
            .thenReturn(ResponseEntity.ok("bytes".getBytes()));

        byte[] result = documentConversionService.convertDocumentToPdf(documentToConvert);
        assertThat(result, is(notNullValue()));
        assertThat(result, is(CONVERTED_BINARY));
    }

    @Test(expected = DocumentConversionException.class)
    public void convertWordToPdfFailsWhenAlreadyPdf() throws Exception {
        mockServer.expect(requestTo(PDF_SERVICE_URI))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess(CONVERTED_BINARY, MediaType.APPLICATION_OCTET_STREAM));

        when(
            evidenceManagementService.download(ArgumentMatchers.eq(documentToConvert.getBinaryUrl())))
            .thenReturn(ResponseEntity.ok("bytes".getBytes()));

        documentToConvert.setFileName("file.pdf");
        byte[] result = documentConversionService.convertDocumentToPdf(documentToConvert);
    }

    @Test
    public void getConvertedFilename() {
        assertThat(documentConversionService.getConvertedFilename("nodot"), is("nodot.pdf"));
        assertThat(documentConversionService.getConvertedFilename("word.docx"), is("word.pdf"));
    }
}
