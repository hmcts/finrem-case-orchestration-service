package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentgenerator;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.evidence.FileUploadResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.DocumentOrderingService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementAuditService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;

@ActiveProfiles("test-mock-feign-clients")
public class FinremDocumentOrderingServiceTest extends BaseServiceTest {

    @MockBean
    private EvidenceManagementAuditService evidenceManagementAuditService;

    @Autowired
    private DocumentOrderingService documentOrderingService;

    @Test(expected = IllegalStateException.class)
    public void givenCheckingForDocumentOrder_whenResponseWithoutTwoElementsReceived_thenIllegalStateExceptionIsThrown() {
        when(evidenceManagementAuditService.audit(any(), eq(AUTH_TOKEN))).thenReturn(singletonList(FileUploadResponse.builder().build()));
        documentOrderingService.isDocumentModifiedLater(anyCaseDocument(), anyCaseDocument(), AUTH_TOKEN);
    }

    @Test
    public void givenCheckingForDocumentOrder_whenDocumentAModifiedDateIsLater_thenTruthIsReturned() {
        mockEvidenceManagementClientToReturnFirstDocumentIsLater();

        boolean isDocumentModifiedLater = documentOrderingService.isDocumentModifiedLater(anyCaseDocument(), anyCaseDocument(), AUTH_TOKEN);

        assertThat(isDocumentModifiedLater, is(true));
    }

    @Test
    public void givenOrderApprovedCollectionModifiedLaterThanNotApproved_whenCheckingOrder_thenExpectedResultIsReceived() throws Exception {
        mockEvidenceManagementClientToReturnFirstDocumentIsLater();
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest();

        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();
        caseData.getConsentOrderWrapper().setConsentD81Question(YesOrNo.YES);
        ConsentOrderCollection collection = ConsentOrderCollection.builder().build();
        collection.setApprovedOrder(ApprovedOrder.builder().consentOrder(caseDocument("url", "approvedFile", "binary")).build());
        caseData.getConsentOrderWrapper().setContestedConsentedApprovedOrders(List.of(collection));
        collection.setApprovedOrder(ApprovedOrder.builder().consentOrder(caseDocument("url", "notApprovedFile", "binary")).build());
        caseData.getConsentOrderWrapper().setConsentedNotApprovedOrders(List.of(collection));

        boolean isOrderApprovedCollectionModifiedLaterThanNotApprovedCollection = documentOrderingService
            .isOrderApprovedCollectionModifiedLaterThanNotApprovedCollection(caseDetails, AUTH_TOKEN);

        assertThat(isOrderApprovedCollectionModifiedLaterThanNotApprovedCollection, is(true));
    }

    private void mockEvidenceManagementClientToReturnFirstDocumentIsLater() {

        when(evidenceManagementAuditService.audit(any(), eq(AUTH_TOKEN))).thenReturn(asList(
            FileUploadResponse.builder().modifiedOn(LocalDateTime.now().plusDays(2).toString()).build(),
            FileUploadResponse.builder().modifiedOn(LocalDateTime.now().toString()).build()));
    }

    private FinremCallbackRequest buildFinremCallbackRequest() {
        FinremCaseData caseData = FinremCaseData.builder().build();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(123L).caseType(CaseType.CONTESTED).data(caseData).build();
        return FinremCallbackRequest.builder().caseDetails(caseDetails).build();
    }

    private CaseDocument anyCaseDocument() {
        return buildCaseDocument("url", "binaryUrl", "filename");
    }
}
