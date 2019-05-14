package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentRequest;

import java.util.Collections;

public class ContestedOnlineFormTest extends GenerateMiniFormATest {

    @Override
    protected String apiUrl() {
        return "/case-orchestration/documents/generate-contested-mini-form-a";
    }

    @Override
    protected String getTestFixture() {
        return "/fixtures/contested/validate-hearing-with-fastTrackDecision.json";
    }

    @Override
    protected DocumentRequest documentRequest() {
        return DocumentRequest.builder()
                .template(documentConfiguration.getContestedMiniFormTemplate())
                .fileName(documentConfiguration.getContestedMiniFormFileName())
                .values(Collections.singletonMap("caseDetails", request.getCaseDetails()))
                .build();
    }
}
