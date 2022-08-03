package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.EvidenceManagementClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.evidence.FileUploadResponse;
import uk.gov.hmcts.reform.finrem.ccd.domain.ConsentOrder;
import uk.gov.hmcts.reform.finrem.ccd.domain.ConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo;

import java.util.Date;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.newDocument;

@ActiveProfiles("test-mock-feign-clients")
public class DocumentOrderingServiceTest extends BaseServiceTest {

    @Autowired
    private EvidenceManagementClient evidenceManagementClientMock;

    @Autowired
    private DocumentOrderingService documentOrderingService;

    @Test(expected = IllegalStateException.class)
    public void givenCheckingForDocumentOrder_whenResponseWithoutTwoElementsReceived_thenIllegalStateExceptionIsThrown() {
        when(evidenceManagementClientMock.auditFileUrls(eq(AUTH_TOKEN), any())).thenReturn(singletonList(FileUploadResponse.builder().build()));
        documentOrderingService.isDocumentModifiedLater(newDocument(), newDocument(), AUTH_TOKEN);
    }

    @Test
    public void givenCheckingForDocumentOrder_whenDocumentAModifiedDateIsLater_thenTruthIsReturned() {
        mockEvidenceManagementClientToReturnFirstDocumentIsLater();

        boolean isDocumentModifiedLater = documentOrderingService.isDocumentModifiedLater(newDocument(), newDocument(), AUTH_TOKEN);

        assertThat(isDocumentModifiedLater, is(true));
    }

    @Test
    public void givenOrderApprovedCollectionModifiedLaterThanNotApproved_whenCheckingOrder_thenExpectedResultIsReceived() {
        mockEvidenceManagementClientToReturnFirstDocumentIsLater();

        FinremCaseDetails caseDetails = getContestedNewCallbackRequest().getCaseDetails();
        FinremCaseData caseData = caseDetails.getCaseData();
        caseData.getConsentOrderWrapper().setConsentD81Question(YesOrNo.YES);
        caseData.getConsentOrderWrapper().setContestedConsentedApprovedOrders(
            singletonList(ConsentOrderCollection.builder()
                .value(ConsentOrder.builder()
                    .consentOrder(newDocument())
                    .build())
                .build()));
        caseData.getConsentOrderWrapper().setConsentedNotApprovedOrders(singletonList(ConsentOrderCollection.builder()
            .value(ConsentOrder.builder()
                .consentOrder(newDocument())
                .build())
            .build()));

        boolean isOrderApprovedCollectionModifiedLaterThanNotApprovedCollection = documentOrderingService
            .isOrderApprovedCollectionModifiedLaterThanNotApprovedCollection(caseDetails, AUTH_TOKEN);

        assertThat(isOrderApprovedCollectionModifiedLaterThanNotApprovedCollection, is(true));
    }

    private void mockEvidenceManagementClientToReturnFirstDocumentIsLater() {
        Date earlier = new Date();
        Date later = new Date(earlier.getTime() + 1);

        when(evidenceManagementClientMock.auditFileUrls(eq(AUTH_TOKEN), any())).thenReturn(asList(
            FileUploadResponse.builder().modifiedOn(later).build(),
            FileUploadResponse.builder().modifiedOn(earlier).build()));
    }
}
