package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingOrderCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingOrderDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CASE_TYPE_ID_CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FINAL_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_LATEST_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_ORDER_OTHER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LATEST_DRAFT_HEARING_ORDER;

public class ContestedCaseOrderServiceTest extends BaseServiceTest {

    @Autowired private ContestedCaseOrderService contestedCaseOrderService;

    @MockBean private GenericDocumentService genericDocumentService;
    @MockBean private BulkPrintService bulkPrintService;
    @MockBean private PaperNotificationService paperNotificationService;
    @MockBean private DocumentHelper documentHelper;
    @MockBean private CaseDataService caseDataService;

    @Captor
    private ArgumentCaptor<List<BulkPrintDocument>> bulkPrintArgumentCaptor;

    @Test
    public void givenNoGeneralOrderPresent_whenPrintAndMailGeneralOrderTriggered_thenDocumentsAreNotPrinted() {
        CaseDetails caseDetails = generalOrderContestedCaseDetails();
        caseDetails.getData().remove(GENERAL_ORDER_LATEST_DOCUMENT);

        contestedCaseOrderService.printAndMailGeneralOrderToParties(caseDetails, AUTH_TOKEN);

        verify(bulkPrintService, never()).printApplicantDocuments(any(), any(), any());
        verify(bulkPrintService, never()).printRespondentDocuments(any(), any(), any());
    }

    @Test
    public void whenPrintAndMailGeneralOrderTriggered_thenBothApplicantAndRespondentPacksArePrinted() {
        when(paperNotificationService.shouldPrintForApplicant(any())).thenReturn(true);
        when(paperNotificationService.shouldPrintForRespondent(any())).thenReturn(true);
        when(documentHelper.convertToCaseDocument(any())).thenReturn(new CaseDocument());

        CaseDetails caseDetails = generalOrderContestedCaseDetails();
        contestedCaseOrderService.printAndMailGeneralOrderToParties(caseDetails, AUTH_TOKEN);

        verify(bulkPrintService).printApplicantDocuments(any(), any(), any());
        verify(bulkPrintService).printRespondentDocuments(any(), any(), any());
    }

    @Test
    public void givenShouldNotPrintPackForApplicant_whenPrintAndMailGeneralOrderTriggered_thenOnlyRespondentPacksIsPrinted() {
        when(paperNotificationService.shouldPrintForApplicant(any())).thenReturn(false);
        when(paperNotificationService.shouldPrintForRespondent(any())).thenReturn(true);
        when(documentHelper.convertToCaseDocument(any())).thenReturn(new CaseDocument());

        CaseDetails caseDetails = generalOrderContestedCaseDetails();
        contestedCaseOrderService.printAndMailGeneralOrderToParties(caseDetails, AUTH_TOKEN);

        verify(bulkPrintService, never()).printApplicantDocuments(any(), any(), any());
        verify(bulkPrintService).printRespondentDocuments(any(), any(), any());
    }

    @Test
    public void givenAllHearingDocumentsArePresentThenSendToBulkPrintWhenPaperCase() {
        when(caseDataService.isContestedPaperApplication(any())).thenReturn(true);
        when(paperNotificationService.shouldPrintForApplicant(any())).thenReturn(true);
        when(paperNotificationService.shouldPrintForRespondent(any())).thenReturn(true);
        when(documentHelper.hasAnotherHearing(any())).thenReturn(true);
        mockDocumentHelperToReturnDefaultExpectedDocuments();

        contestedCaseOrderService.printAndMailHearingDocuments(CaseDetails.builder().build(), AUTH_TOKEN);

        verify(bulkPrintService).printApplicantDocuments(any(), any(), bulkPrintArgumentCaptor.capture());
        verify(bulkPrintService).printRespondentDocuments(any(), any(), any());

        List<String> expectedBulkPrintDocuments = asList("HearingOrderBinaryURL", "AdditionalHearingDocumentURL", "OtherHearingOrderDocumentsURL");

        assertThat(bulkPrintArgumentCaptor.getAllValues().get(0).stream().map(BulkPrintDocument::getBinaryFileUrl).collect(Collectors.toList()),
            containsInAnyOrder(expectedBulkPrintDocuments.toArray()));
    }

    @Test
    public void givenAllHearingDocumentsArePresentThenSendToBulkPrintWhenPaperCase_noNextHearing() {
        when(caseDataService.isContestedPaperApplication(any())).thenReturn(true);
        when(paperNotificationService.shouldPrintForApplicant(any())).thenReturn(true);
        when(paperNotificationService.shouldPrintForRespondent(any())).thenReturn(true);
        when(documentHelper.hasAnotherHearing(any())).thenReturn(false);

        contestedCaseOrderService.printAndMailHearingDocuments(CaseDetails.builder().build(), AUTH_TOKEN);

        verify(bulkPrintService).printApplicantDocuments(any(), any(), bulkPrintArgumentCaptor.capture());
        verify(bulkPrintService).printRespondentDocuments(any(), any(), any());

        String notExpectedBulkPrintDocument = "AdditionalHearingDocumentURL";

        assertThat(bulkPrintArgumentCaptor.getAllValues().get(0).stream().map(BulkPrintDocument::getBinaryFileUrl).collect(Collectors.toList()),
            not(contains(notExpectedBulkPrintDocument)));
    }

    @Test
    public void givenAllHearingDocumentsArePresentThenDoNotSendToBulkPrintWhenDigitalCase() {
        contestedCaseOrderService.printAndMailHearingDocuments(CaseDetails.builder().build(), AUTH_TOKEN);

        verifyNoInteractions(bulkPrintService);
    }

    @Test
    public void latestDraftedHearingOrderDocumentIsNotAddedToPack() {
        when(caseDataService.isContestedPaperApplication(any())).thenReturn(true);
        when(documentHelper.hasAnotherHearing(any())).thenReturn(true);
        mockDocumentHelperToReturnDefaultExpectedDocuments();
        when(documentHelper.getDocumentLinkAsBulkPrintDocument(any(), eq(LATEST_DRAFT_HEARING_ORDER))).thenReturn(Optional.empty());
        when(paperNotificationService.shouldPrintForApplicant(any())).thenReturn(true);

        contestedCaseOrderService.printAndMailHearingDocuments(CaseDetails.builder().build(), AUTH_TOKEN);

        verify(bulkPrintService).printApplicantDocuments(any(), any(), bulkPrintArgumentCaptor.capture());

        List<String> expectedBulkPrintDocuments = asList("AdditionalHearingDocumentURL", "OtherHearingOrderDocumentsURL");

        assertThat(bulkPrintArgumentCaptor.getAllValues().get(0).stream().map(BulkPrintDocument::getBinaryFileUrl).collect(Collectors.toList()),
            containsInAnyOrder(expectedBulkPrintDocuments.toArray()));
    }

    @Test
    public void latestDraftedHearingOrderDocumentIsNotAddedToPack_noNextHearing() {
        when(caseDataService.isContestedPaperApplication(any())).thenReturn(true);
        when(documentHelper.hasAnotherHearing(any())).thenReturn(false);
        when(paperNotificationService.shouldPrintForApplicant(any())).thenReturn(true);

        contestedCaseOrderService.printAndMailHearingDocuments(CaseDetails.builder().build(), AUTH_TOKEN);

        verify(bulkPrintService).printApplicantDocuments(any(), any(), bulkPrintArgumentCaptor.capture());

        String notExpectedBulkPrintDocument = "AdditionalHearingDocumentURL";

        assertThat(bulkPrintArgumentCaptor.getAllValues().get(0).stream().map(BulkPrintDocument::getBinaryFileUrl).collect(Collectors.toList()),
            not(contains(notExpectedBulkPrintDocument)));
    }

    @Test
    public void latestAdditionalHearingDocumentIsNotAddedToPack() {
        when(caseDataService.isContestedPaperApplication(any())).thenReturn(true);
        when(paperNotificationService.shouldPrintForApplicant(any())).thenReturn(true);
        when(documentHelper.hasAnotherHearing(any())).thenReturn(true);
        mockDocumentHelperToReturnDefaultExpectedDocuments();
        when(documentHelper.getLatestAdditionalHearingDocument(any())).thenReturn(Optional.empty());

        contestedCaseOrderService.printAndMailHearingDocuments(CaseDetails.builder().build(), AUTH_TOKEN);

        verify(bulkPrintService).printApplicantDocuments(any(), any(), bulkPrintArgumentCaptor.capture());

        List<String> expectedBulkPrintDocuments = asList("HearingOrderBinaryURL", "OtherHearingOrderDocumentsURL");

        assertThat(bulkPrintArgumentCaptor.getAllValues().get(0).stream().map(BulkPrintDocument::getBinaryFileUrl).collect(Collectors.toList()),
            containsInAnyOrder(expectedBulkPrintDocuments.toArray()));
    }

    @Test
    public void finalOrderSuccess() {
        mockDocumentHelperToReturnDefaultExpectedDocuments();
        when(genericDocumentService.stampDocument(isA(CaseDocument.class), eq(AUTH_TOKEN), anyString())).thenReturn(caseDocument());

        CaseDetails caseDetails = CaseDetails.builder().caseTypeId(CASE_TYPE_ID_CONTESTED).data(new HashMap<>()).build();
        contestedCaseOrderService.stampFinalOrder(caseDetails, AUTH_TOKEN);

        verify(genericDocumentService).stampDocument(caseDocument(), AUTH_TOKEN, CASE_TYPE_ID_CONTESTED);

        List<HearingOrderCollectionData> expectedFinalOrderCollection =
            (List<HearingOrderCollectionData>) caseDetails.getData().get(FINAL_ORDER_COLLECTION);

        assertThat(expectedFinalOrderCollection, hasSize(1));
        assertThat(expectedFinalOrderCollection.get(0).getHearingOrderDocuments().getUploadDraftDocument(), is(caseDocument()));
    }

    @Test
    public void finalOrderSuccessWithoutAnyHearingOrder() {
        CaseDetails caseDetails = CaseDetails.builder().data(new HashMap<>()).build();

        contestedCaseOrderService.stampFinalOrder(caseDetails, AUTH_TOKEN);

        assertThat(caseDetails.getData(), not(hasKey(FINAL_ORDER_COLLECTION)));
    }

    @Test
    public void finalOrderSuccessWithFinalOrder() {
        mockDocumentHelperToReturnDefaultExpectedDocuments();
        when(documentHelper.getFinalOrderDocuments(any())).thenReturn(new ArrayList<>(List.of(HearingOrderCollectionData.builder().build())));
        when(genericDocumentService.stampDocument(isA(CaseDocument.class), eq(AUTH_TOKEN), anyString())).thenReturn(caseDocument());

        CaseDetails caseDetails = CaseDetails.builder().caseTypeId(CASE_TYPE_ID_CONTESTED).data(new HashMap<>()).build();

        contestedCaseOrderService.stampFinalOrder(caseDetails, AUTH_TOKEN);

        verify(genericDocumentService).stampDocument(caseDocument(), AUTH_TOKEN, CASE_TYPE_ID_CONTESTED);

        List<HearingOrderCollectionData> expectedFinalOrderCollection =
            (List<HearingOrderCollectionData>) caseDetails.getData().get(FINAL_ORDER_COLLECTION);

        assertThat(expectedFinalOrderCollection, hasSize(2));
        assertThat(expectedFinalOrderCollection.get(1).getHearingOrderDocuments().getUploadDraftDocument(), is(caseDocument()));
    }

    private void mockDocumentHelperToReturnDefaultExpectedDocuments() {
        when(documentHelper.getDocumentLinkAsBulkPrintDocument(any(), eq(LATEST_DRAFT_HEARING_ORDER))).thenReturn(
            Optional.of(BulkPrintDocument.builder().binaryFileUrl("HearingOrderBinaryURL").build()));
        when(documentHelper.getCollectionOfDocumentLinksAsBulkPrintDocuments(any(), eq(HEARING_ORDER_OTHER_COLLECTION))).thenReturn(
            singletonList(BulkPrintDocument.builder().binaryFileUrl("OtherHearingOrderDocumentsURL").build()));

        CaseDocument additionalHearingDocument = CaseDocument.builder().documentBinaryUrl("AdditionalHearingDocumentURL").build();
        when(documentHelper.getLatestAdditionalHearingDocument(any())).thenReturn(Optional.of(additionalHearingDocument));
        when(documentHelper.getCaseDocumentAsBulkPrintDocument(eq(additionalHearingDocument))).thenReturn(
            BulkPrintDocument.builder().binaryFileUrl(additionalHearingDocument.getDocumentBinaryUrl()).build());

        when(documentHelper.getHearingOrderDocuments(any())).thenReturn(singletonList(HearingOrderCollectionData.builder()
            .hearingOrderDocuments(HearingOrderDocument.builder()
                .uploadDraftDocument(caseDocument())
                .build())
            .build()));
    }

    private CaseDetails generalOrderContestedCaseDetails() {
        try (InputStream resourceAsStream = getClass().getResourceAsStream("/fixtures/general-order-contested.json")) {
            return mapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
