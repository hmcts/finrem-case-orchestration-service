package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.PdfDocumentRequest;

public class ContestedOnlineFormTest extends GenerateMiniFormATest {

    @Override
    protected String apiUrl() {
        return "/case-orchestration/documents/generate-contested-mini-form-a";
    }

    @Override
    protected String getTestFixture() {
        return "/fixtures/contested/generate-contested-form-A.json";
    }

    @Override
    protected PdfDocumentRequest pdfRequest() {
        return PdfDocumentRequest.builder()
            .accessKey("TESTPDFACCESS")
            .outputName("result.pdf")
            .templateName(documentConfiguration.getContestedMiniFormTemplate(request.getCaseDetails()))
            .data(copyWithOptionValueTranslation(request.getCaseDetails()).getData())
            .build();
    }
}
