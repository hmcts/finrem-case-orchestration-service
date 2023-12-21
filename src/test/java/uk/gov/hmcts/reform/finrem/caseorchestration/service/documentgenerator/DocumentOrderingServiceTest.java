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
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;

@ActiveProfiles("test-mock-feign-clients")
public class DocumentOrderingServiceTest extends BaseServiceTest {

    @MockBean
    private EvidenceManagementAuditService evidenceManagementAuditService;

    @Autowired
    private DocumentOrderingService documentOrderingService;

    @Test(expected = IllegalStateException.class)
    public void givenCheckingForDocumentOrder_whenResponseWithoutTwoElementsReceived_thenIllegalStateExceptionIsThrown() {
        when(evidenceManagementAuditService.audit(any(), eq(AUTH_TOKEN))).thenReturn(singletonList(FileUploadResponse.builder().build()));
        documentOrderingService.isDocumentModifiedLater(caseDocument(), caseDocument(), AUTH_TOKEN);
    }

    @Test
    public void givenCheckingForDocumentOrder_whenDocumentAModifiedDateIsLater_thenTruthIsReturned() {
        mockEvidenceManagementClientToReturnFirstDocumentIsLater();

        boolean isDocumentModifiedLater = documentOrderingService.isDocumentModifiedLater(caseDocument(), caseDocument(), AUTH_TOKEN);

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

    @Test
    public void isGeneralOrderRecentThanApprovedOrder_returnFalse() {
        CaseDocument generalOrder = caseDocument();
        List<ConsentOrderCollection> collections = new ArrayList<>();
        boolean recentThanApprovedOrder = documentOrderingService
            .isGeneralOrderRecentThanApprovedOrder(generalOrder, collections, AUTH_TOKEN);
        assertFalse(recentThanApprovedOrder);
    }

    @Test
    public void whenGeneralOrderIsOlderThenApprovedOrder_thenReturnFalse() {
        List<FileUploadResponse> responseList = new ArrayList<>();
        FileUploadResponse fileUploadResponse1 = FileUploadResponse.builder()
            .fileUrl(DOC_URL).createdOn(LocalDateTime.now().plusDays(-23).toString()).build();
        FileUploadResponse fileUploadResponse2 = FileUploadResponse.builder()
            .fileUrl(DOC_URL).createdOn(LocalDateTime.now().plusDays(-22).toString()).build();

        responseList.add(fileUploadResponse1);
        responseList.add(fileUploadResponse2);

        when(evidenceManagementAuditService.audit(anyList(), eq(AUTH_TOKEN))).thenReturn(responseList);

        CaseDocument generalOrder = caseDocument();
        ApprovedOrder approvedOrder = ApprovedOrder.builder()
            .orderLetter(caseDocument()).consentOrder(caseDocument()).build();
        ConsentOrderCollection collection = ConsentOrderCollection.builder().approvedOrder(approvedOrder).build();
        List<ConsentOrderCollection> collections = new ArrayList<>();
        collections.add(collection);
        boolean recentThanApprovedOrder = documentOrderingService
            .isGeneralOrderRecentThanApprovedOrder(generalOrder, collections, AUTH_TOKEN);
        assertFalse(recentThanApprovedOrder);
    }

    @Test
    public void whenGeneralOrderIsNewerThenApprovedOrder_thenReturnTrue() {
        List<FileUploadResponse> responseList = new ArrayList<>();
        FileUploadResponse fileUploadResponse1 = FileUploadResponse.builder().fileUrl(DOC_URL)
            .createdOn(LocalDateTime.now().plusDays(-13).toString()).build();
        FileUploadResponse fileUploadResponse2 = FileUploadResponse.builder().fileUrl(DOC_URL)
            .createdOn(LocalDateTime.now().plusDays(-22).toString()).build();

        responseList.add(fileUploadResponse1);
        responseList.add(fileUploadResponse2);

        when(evidenceManagementAuditService.audit(anyList(), eq(AUTH_TOKEN))).thenReturn(responseList);

        CaseDocument generalOrder = caseDocument();
        ApprovedOrder approvedOrder = ApprovedOrder.builder()
            .orderLetter(caseDocument()).consentOrder(caseDocument()).build();
        ConsentOrderCollection collection = ConsentOrderCollection.builder().approvedOrder(approvedOrder).build();
        List<ConsentOrderCollection> collections = new ArrayList<>();
        collections.add(collection);
        boolean recentThanApprovedOrder = documentOrderingService
            .isGeneralOrderRecentThanApprovedOrder(generalOrder, collections, AUTH_TOKEN);
        assertTrue(recentThanApprovedOrder);
    }

    @Test(expected = IllegalStateException.class)
    public void whenGeneralOrderIsNewerThenApprovedOrderButEvidenceMgmtDoNotHaveInfo_thenReturnError() {
        List<FileUploadResponse> responseList = new ArrayList<>();
        FileUploadResponse fileUploadResponse1 = FileUploadResponse.builder().fileUrl(DOC_URL)
            .createdOn(LocalDateTime.now().plusDays(-13).toString()).build();
        responseList.add(fileUploadResponse1);

        when(evidenceManagementAuditService.audit(anyList(), eq(AUTH_TOKEN))).thenReturn(responseList);

        CaseDocument generalOrder = caseDocument();
        ApprovedOrder approvedOrder = ApprovedOrder.builder()
            .orderLetter(caseDocument()).consentOrder(caseDocument()).build();
        ConsentOrderCollection collection = ConsentOrderCollection.builder().approvedOrder(approvedOrder).build();
        List<ConsentOrderCollection> collections = new ArrayList<>();
        collections.add(collection);
        documentOrderingService
            .isGeneralOrderRecentThanApprovedOrder(generalOrder, collections, AUTH_TOKEN);
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

}
