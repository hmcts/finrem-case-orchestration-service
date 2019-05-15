package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentRequest;

import java.util.Collections;

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
    protected DocumentRequest documentRequest() {
        return DocumentRequest.builder()
                .template(documentConfiguration.getContestedDraftMiniFormTemplate())
                .fileName(documentConfiguration.getContestedDraftMiniFormFileName())
                .values(Collections.singletonMap("caseDetails",
                        copyWithOptionValueTranslation(request.getCaseDetails())))
                .build();
    }

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

        generateDocument();
    }
}
