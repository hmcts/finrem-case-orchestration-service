package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentGenerationRequest;

import java.util.Collections;

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
    protected DocumentGenerationRequest documentRequest() {
        return DocumentGenerationRequest.builder()
                .template(documentConfiguration.getContestedMiniFormTemplate())
                .fileName(documentConfiguration.getContestedMiniFormFileName())
                .values(Collections.singletonMap("caseDetails",
                        copyWithOptionValueTranslation(request.getCaseDetails())))
                .build();
    }
}
