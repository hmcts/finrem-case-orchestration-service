package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest;

import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentGenerationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import java.util.Collections;

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
    protected DocumentGenerationRequest documentRequest() {
        return DocumentGenerationRequest.builder()
            .template(documentConfiguration.getContestedDraftMiniFormTemplate())
            .fileName(documentConfiguration.getContestedDraftMiniFormFileName())
            .values(Collections.singletonMap("caseDetails",
                copyWithOptionValueTranslation(request.getCaseDetails())))
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
        deleteDocumentServiceStubWith(miniFormAServiceStatus);
        when(idamService.isUserRoleAdmin(any())).thenReturn(true);
        generateDocument();
    }
}
