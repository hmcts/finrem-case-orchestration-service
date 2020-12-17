package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.apache.http.auth.AUTH;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import scala.App;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.EvidenceManagementClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CollectionElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedConsentOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedConsentOrderData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.evidence.FileUploadResponse;

import java.util.Date;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENT_D81_QUESTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_CONSENT_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_CONSENT_ORDER_NOT_APPROVED_COLLECTION;

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
        mockEvidenceManagementClientToReturnFirstDocumentIsLater();
        
        boolean isDocumentModifiedLater = documentOrderingService.isDocumentModifiedLater(anyCaseDocument(), anyCaseDocument(), AUTH_TOKEN);

        assertThat(isDocumentModifiedLater, is(true));
    }

    @Test
    public void givenOrderApprovedCollectionModifiedLaterThanNotApproved_whenCheckingOrder_thenExpectedResultIsReceived() {
        mockEvidenceManagementClientToReturnFirstDocumentIsLater();

        CaseDetails caseDetails = getContestedCallbackRequest().getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();
        caseData.put(CONSENT_D81_QUESTION, YES_VALUE);
        caseData.put(CONTESTED_CONSENT_ORDER_COLLECTION, singletonList(CollectionElement.<ApprovedOrder>builder()
            .value(ApprovedOrder.builder().consentOrder(anyCaseDocument()).build())
            .build()));
        caseData.put(CONTESTED_CONSENT_ORDER_NOT_APPROVED_COLLECTION, singletonList(
            new ContestedConsentOrderData("anyId", new ContestedConsentOrder(anyCaseDocument()))));

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

    private CaseDocument anyCaseDocument() {
        return buildCaseDocument("url", "binaryUrl", "filename");
    }
}
