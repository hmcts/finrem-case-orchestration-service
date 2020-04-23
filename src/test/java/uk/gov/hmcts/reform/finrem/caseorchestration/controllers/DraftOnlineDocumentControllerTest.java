package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import org.mockito.stubbing.OngoingStubbing;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@WebMvcTest(DraftOnlineDocumentController.class)
public class DraftOnlineDocumentControllerTest extends MiniFormAControllerTest {

    @Override
    public String endpoint() {
        return "/case-orchestration/documents/draft-contested-mini-form-a";
    }

    @Override
    public OngoingStubbing<CaseDocument> whenServiceGeneratesDocument() {
        return when(documentService.generateDraftContestedMiniFormA(eq(AUTH_TOKEN), isA(CaseDetails.class)));
    }

    @Override
    String jsonFixture() {
        return "/fixtures/contested/validate-hearing-with-fastTrackDecision.json";
    }
}