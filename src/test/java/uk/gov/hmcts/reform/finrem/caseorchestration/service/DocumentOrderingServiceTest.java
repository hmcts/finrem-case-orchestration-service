package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.EvidenceManagementClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.evidence.FileUploadResponse;

import java.util.Date;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@ActiveProfiles("test-mock-feign-clients")
public class DocumentOrderingServiceTest extends BaseServiceTest {

    @Autowired
    private EvidenceManagementClient evidenceManagementClientMock;

    @Autowired
    private DocumentOrderingService documentOrderingService;

    @Test(expected = IllegalStateException.class)
    public void givenCheckingForDocumentOrder_whenResponseWithoutTwoElementsReceived_thenIllegalStateExceptionIsThrown() {
        when(evidenceManagementClientMock.auditFileUrls(eq(AUTH_TOKEN), any())).thenReturn(singletonList(FileUploadResponse.builder().build()));
        documentOrderingService.isDocumentModifiedLater(anyCaseDocument(), anyCaseDocument(), AUTH_TOKEN);
    }

    @Test
    public void givenCheckingForDocumentOrder_whenDocumentAModifiedDateIsLater_thenTruthIsReturned() {
        Date earlier = new Date();
        Date later = new Date(earlier.getTime() + 1);

        when(evidenceManagementClientMock.auditFileUrls(eq(AUTH_TOKEN), any())).thenReturn(asList(
            FileUploadResponse.builder().modifiedOn(later).build(),
            FileUploadResponse.builder().modifiedOn(earlier).build()));
        boolean isDocumentModifiedLater = documentOrderingService.isDocumentModifiedLater(anyCaseDocument(), anyCaseDocument(), AUTH_TOKEN);

        assertThat(isDocumentModifiedLater, is(true));
    }

    private CaseDocument anyCaseDocument() {
        return buildCaseDocument("url", "binaryUrl", "filename");
    }
}
