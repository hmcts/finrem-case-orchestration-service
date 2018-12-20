package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.hamcrest.Matchers;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentRequest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.caseDataWithMiniFormA;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.document;

public class DocumentServiceTest {

    private static final String AUTH_TOKEN = "Bearer nkjnYBJB";

    private DocumentService service;

    @Test
    public void generateMiniFormA() {
        service = new DocumentService(new DocumentClientStub(new CountDownLatch(1)));

        CaseDocument result = service.generateMiniFormA(AUTH_TOKEN, new CaseDetails());
        assertThat(result.getDocumentFilename(), Matchers.is(FILE_NAME));
        assertThat(result.getDocumentUrl(), Matchers.is(URL));
        assertThat(result.getDocumentBinaryUrl(), Matchers.is(BINARY_URL));
    }

    @Test
    public void deleteExistingMiniFormAToGenerateNew() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);
        service = new DocumentService(new DocumentClientStub(latch));

        CaseDocument result = service.generateMiniFormA(AUTH_TOKEN, caseDetails());
        latch.await(2000, TimeUnit.MILLISECONDS);

        assertThat(result.getDocumentFilename(), Matchers.is(FILE_NAME));
        assertThat(result.getDocumentUrl(), Matchers.is(URL));
        assertThat(result.getDocumentBinaryUrl(), Matchers.is(BINARY_URL));
    }

    private CaseDetails caseDetails() {
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseDataWithMiniFormA());
        return caseDetails;
    }

    private static class DocumentClientStub implements DocumentClient {

        private final CountDownLatch latch;

        public DocumentClientStub(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public Document generatePDF(DocumentRequest generateDocumentRequest, String authorizationToken) {
            latch.countDown();
            return document();
        }

        @Override
        public void deleteDocument(String fileUrl, String authorizationToken) {
            latch.countDown();
        }
    }
}