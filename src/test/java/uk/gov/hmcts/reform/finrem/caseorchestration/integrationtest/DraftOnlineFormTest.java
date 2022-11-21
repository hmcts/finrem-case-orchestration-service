package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest;

import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.PdfDocumentRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentGenerationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;


import java.io.InputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class DraftOnlineFormTest extends GenerateMiniFormATest {

    @Override
    protected String apiUrl() {
        return "/case-orchestration/documents/draft-contested-mini-form-a";
    }

    @Override
    protected String getTestFixture() {
        return "/fixtures/contested/with-mini-form-A.json";
    }

    @Override
    protected PdfDocumentRequest pdfRequest() {

        CallbackRequest translatedRequest;
        try (InputStream resourceAsStream = getClass().getResourceAsStream("/fixtures/contested/with-mini-form-A-translated.json")) {
            translatedRequest = objectMapper.readValue(resourceAsStream, CallbackRequest.class);
        } catch (Exception e) {
            throw new RuntimeException("Couldn't load the request json file for the test");
        }

        return PdfDocumentRequest.builder()
            .accessKey("TESTPDFACCESS")
            .outputName("result.pdf")
            .templateName(documentConfiguration.getContestedDraftMiniFormTemplate())
            .data(translatedRequest.getCaseDetails().getData())
            .build();
    }

    @MockBean
    private IdamService idamService;

    @Test
    public void deleteExistingMiniFormAWithSuccess() throws Exception {
        doTestDeleteMiniFormA(HttpStatus.OK);
    }

    @Test
    public void deleteMiniFormAErrorShouldNotAffectNewMiniFormGeneration() throws Exception {
        doTestDeleteMiniFormA(HttpStatus.NOT_FOUND);
    }

    private void doTestDeleteMiniFormA(HttpStatus miniFormAServiceStatus) throws Exception {
        generateDocumentServiceSuccessStub();
        generateEvidenceUploadServiceSuccessStub();
        deleteDocumentServiceStubWith(miniFormAServiceStatus);
        when(idamService.isUserRoleAdmin(any())).thenReturn(true);
        generateDocument();
    }
}
