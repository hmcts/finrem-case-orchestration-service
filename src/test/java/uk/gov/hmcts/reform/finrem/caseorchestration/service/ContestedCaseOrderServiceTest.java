package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AdditionalHearingDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AdditionalHearingDocumentData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CollectionElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingOrderCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingOrderDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.PAPER_APPLICATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ADDITIONAL_HEARING_DOCUMENT_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FINAL_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_LATEST_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_ORDER_OTHER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LATEST_DRAFT_HEARING_ORDER;

public class ContestedCaseOrderServiceTest extends BaseServiceTest {

    @Autowired
    private ContestedCaseOrderService contestedCaseOrderService;

    @MockBean
    private BulkPrintService bulkPrintService;

    @MockBean
    private CaseDataService caseDataService;

    @MockBean
    private GenericDocumentService genericDocumentService;

    @Captor
    private ArgumentCaptor<List<BulkPrintDocument>> bulkPrintArgumentCaptor;

    @Before
    public void setUp() {
        when(genericDocumentService.stampDocument(isA(CaseDocument.class), eq(AUTH_TOKEN))).thenReturn(caseDocument());
    }

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
        CaseDetails caseDetails = generalOrderContestedCaseDetails();
        when(bulkPrintService.shouldPrintForApplicant(any())).thenReturn(true);

        contestedCaseOrderService.printAndMailGeneralOrderToParties(caseDetails, AUTH_TOKEN);

        verify(bulkPrintService, times(1)).printApplicantDocuments(any(), any(), any());
        verify(bulkPrintService, times(1)).printRespondentDocuments(any(), any(), any());
    }

    @Test
    public void givenShouldNotPrintPackForApplicant_whenPrintAndMailGeneralOrderTriggered_thenOnlyRespondentPacksIsPrinted() {
        CaseDetails caseDetails = generalOrderContestedCaseDetails();
        when(bulkPrintService.shouldPrintForApplicant(any())).thenReturn(false);

        contestedCaseOrderService.printAndMailGeneralOrderToParties(caseDetails, AUTH_TOKEN);

        verify(bulkPrintService, never()).printApplicantDocuments(any(), any(), any());
        verify(bulkPrintService, times(1)).printRespondentDocuments(any(), any(), any());
    }

    private CaseDetails generalOrderContestedCaseDetails() {
        try (InputStream resourceAsStream = getClass().getResourceAsStream("/fixtures/general-order-contested.json")) {
            return mapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void givenAllHearingDocumentsArePresentThenSendToBulkPrintWhenPaperCase() throws JsonProcessingException {
        CaseDetails caseDetails = buildHearingPackDocumentTestData();
        when(bulkPrintService.shouldPrintForApplicant(any())).thenReturn(true);
        when(caseDataService.hasAnotherHearing(caseDetails.getData())).thenReturn(true);

        contestedCaseOrderService.printAndMailHearingDocuments(caseDetails, AUTH_TOKEN);

        verify(bulkPrintService, times(1)).printApplicantDocuments(any(), any(), bulkPrintArgumentCaptor.capture());
        verify(bulkPrintService, times(1)).printRespondentDocuments(any(), any(), any());

        List<String> expectedBulkPrintDocuments = new ArrayList<>();
        expectedBulkPrintDocuments.add("HearingOrderBinaryURL");
        expectedBulkPrintDocuments.add("AdditionalHearingDocumentURL");
        expectedBulkPrintDocuments.add("OtherHearingOrderDocumentsURL");

        assertThat(bulkPrintArgumentCaptor.getAllValues().get(0).stream().map(
            doc -> doc.getBinaryFileUrl()).collect(Collectors.toList()).containsAll(expectedBulkPrintDocuments),  is(true));
    }

    @Test
    public void givenAllHearingDocumentsArePresentThenSendToBulkPrintWhenPaperCase_noNextHearing() throws JsonProcessingException {
        CaseDetails caseDetails = buildHearingPackDocumentTestData();
        when(bulkPrintService.shouldPrintForApplicant(any())).thenReturn(true);
        when(caseDataService.hasAnotherHearing(caseDetails.getData())).thenReturn(false);

        contestedCaseOrderService.printAndMailHearingDocuments(caseDetails, AUTH_TOKEN);

        verify(bulkPrintService, times(1)).printApplicantDocuments(any(), any(), bulkPrintArgumentCaptor.capture());
        verify(bulkPrintService, times(1)).printRespondentDocuments(any(), any(), any());

        List<String> notExpectedBulkPrintDocuments = new ArrayList<>();
        notExpectedBulkPrintDocuments.add("AdditionalHearingDocumentURL");

        assertThat(bulkPrintArgumentCaptor.getAllValues().get(0).stream().map(
            doc -> doc.getBinaryFileUrl()).collect(Collectors.toList()).contains(notExpectedBulkPrintDocuments),  is(false));
    }

    @Test
    public void givenAllHearingDocumentsArePresentThenDoNotSendToBulkPrintWhenDigitalCase() throws JsonProcessingException {
        CaseDetails caseDetails = buildHearingPackDocumentTestData();
        caseDetails.getData().remove(PAPER_APPLICATION);

        contestedCaseOrderService.printAndMailHearingDocuments(caseDetails, AUTH_TOKEN);

        verifyNoInteractions(bulkPrintService);
    }

    @Test
    public void latestDraftedHearingOrderDocumentIsNotAddedToPack() throws JsonProcessingException {
        CaseDetails caseDetails = buildHearingPackDocumentTestData();
        caseDetails.getData().remove(LATEST_DRAFT_HEARING_ORDER);

        when(caseDataService.hasAnotherHearing(caseDetails.getData())).thenReturn(true);
        when(bulkPrintService.shouldPrintForApplicant(any())).thenReturn(true);
        contestedCaseOrderService.printAndMailHearingDocuments(caseDetails, AUTH_TOKEN);

        verify(bulkPrintService, times(1)).printApplicantDocuments(any(), any(), bulkPrintArgumentCaptor.capture());

        List<String> expectedBulkPrintDocuments = new ArrayList<>();
        expectedBulkPrintDocuments.add("AdditionalHearingDocumentURL");
        expectedBulkPrintDocuments.add("OtherHearingOrderDocumentsURL");

        assertThat(bulkPrintArgumentCaptor.getAllValues().get(0).stream().map(
            o -> o.getBinaryFileUrl()).collect(Collectors.toList()).containsAll(expectedBulkPrintDocuments),  is(true));
    }

    @Test
    public void latestDraftedHearingOrderDocumentIsNotAddedToPack_noNextHearing() throws JsonProcessingException {
        CaseDetails caseDetails = buildHearingPackDocumentTestData();
        caseDetails.getData().remove(LATEST_DRAFT_HEARING_ORDER);

        when(caseDataService.hasAnotherHearing(caseDetails.getData())).thenReturn(false);
        when(bulkPrintService.shouldPrintForApplicant(any())).thenReturn(true);
        contestedCaseOrderService.printAndMailHearingDocuments(caseDetails, AUTH_TOKEN);

        verify(bulkPrintService, times(1)).printApplicantDocuments(any(), any(), bulkPrintArgumentCaptor.capture());

        List<String> notExpectedBulkPrintDocuments = new ArrayList<>();
        notExpectedBulkPrintDocuments.add("AdditionalHearingDocumentURL");

        assertThat(bulkPrintArgumentCaptor.getAllValues().get(0).stream().map(
            o -> o.getBinaryFileUrl()).collect(Collectors.toList()).contains(notExpectedBulkPrintDocuments),  is(false));
    }

    @Test
    public void latestAdditionalHearingDocumentIsNotAddedToPack() throws JsonProcessingException {
        CaseDetails caseDetails = buildHearingPackDocumentTestData();
        caseDetails.getData().remove(ADDITIONAL_HEARING_DOCUMENT_COLLECTION);

        when(bulkPrintService.shouldPrintForApplicant(any())).thenReturn(true);
        contestedCaseOrderService.printAndMailHearingDocuments(caseDetails, AUTH_TOKEN);

        verify(bulkPrintService, times(1)).printApplicantDocuments(any(), any(), bulkPrintArgumentCaptor.capture());

        List<String> expectedBulkPrintDocuments = new ArrayList<>();
        expectedBulkPrintDocuments.add("HearingOrderBinaryURL");
        expectedBulkPrintDocuments.add("OtherHearingOrderDocumentsURL");

        assertThat(bulkPrintArgumentCaptor.getAllValues().get(0).stream().map(
            o -> o.getBinaryFileUrl()).collect(Collectors.toList()).containsAll(expectedBulkPrintDocuments),  is(true));
    }


    private CaseDetails buildHearingPackDocumentTestData() throws JsonProcessingException {
        CallbackRequest callbackRequest = getContestedCallbackRequest();
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        caseDetails.getData().put(PAPER_APPLICATION, "Yes");

        CaseDocument latestHearingOrder = buildCaseDocument("TestURL", "HearingOrderBinaryURL", "TestName");
        caseDetails.getData().put(LATEST_DRAFT_HEARING_ORDER, latestHearingOrder);

        CaseDocument hearingDocument = buildCaseDocument("TestURL", "AdditionalHearingDocumentURL", "TestName");
        AdditionalHearingDocumentData generatedDocumentData = AdditionalHearingDocumentData.builder()
            .additionalHearingDocument(AdditionalHearingDocument.builder()
                .document(hearingDocument)
                .build())
            .build();
        List<AdditionalHearingDocumentData> additionalHearingDocumentDataList = new ArrayList<>();
        additionalHearingDocumentDataList.add(generatedDocumentData);
        caseDetails.getData().put(ADDITIONAL_HEARING_DOCUMENT_COLLECTION, additionalHearingDocumentDataList);

        CaseDocument otherDocument = buildCaseDocument("TestURL", "OtherHearingOrderDocumentsURL", "TestName");
        List<CollectionElement<CaseDocument>> otherHearingDocuments = new ArrayList<>();
        otherHearingDocuments.add(CollectionElement.<CaseDocument>builder().value(otherDocument).build());
        caseDetails.getData().put(HEARING_ORDER_OTHER_COLLECTION, otherHearingDocuments);

        caseDetails.getData().put(HEARING_ORDER_COLLECTION, buildHearingOrderCollectionData());

        //Force update case data with JSON properties
        Map<String, Object> caseData = caseDetails.getData();
        caseData = mapper.readValue(mapper.writeValueAsString(caseData), HashMap.class);
        caseDetails.setData(caseData);

        return caseDetails;
    }

    private List<HearingOrderCollectionData> buildHearingOrderCollectionData() {
        HearingOrderDocument hearingOrderDocument = HearingOrderDocument
            .builder()
            .uploadDraftDocument(caseDocument())
            .build();

        HearingOrderCollectionData hearingOrderCollectionData = HearingOrderCollectionData
            .builder()
            .id(UUID.randomUUID().toString())
            .hearingOrderDocuments(hearingOrderDocument)
            .build();

        List<HearingOrderCollectionData> hearingOrderCollectionList = new ArrayList<>();
        hearingOrderCollectionList.add(hearingOrderCollectionData);

        return hearingOrderCollectionList;
    }

    @Test
    public void finalOrderSuccess() throws Exception {
        CaseDetails caseDetails = buildHearingPackDocumentTestData();

        contestedCaseOrderService.stampFinalOrder(caseDetails, AUTH_TOKEN);

        verify(genericDocumentService, times(1)).stampDocument(caseDocument(), AUTH_TOKEN);

        List<HearingOrderCollectionData> expectedFinalOrderCollection =
            (List<HearingOrderCollectionData>) caseDetails.getData().get(FINAL_ORDER_COLLECTION);

        assertEquals(expectedFinalOrderCollection.size(), 1);
        assertEquals(expectedFinalOrderCollection.get(0).getHearingOrderDocuments().getUploadDraftDocument(), caseDocument());
    }

    @Test
    public void finalOrderSuccessWithoutAnyHearingOrder() throws Exception {
        CaseDetails caseDetails = buildHearingPackDocumentTestData();

        caseDetails.getData().put(HEARING_ORDER_COLLECTION, null);

        contestedCaseOrderService.stampFinalOrder(caseDetails, AUTH_TOKEN);

        assertTrue(!caseDetails.getData().containsKey(FINAL_ORDER_COLLECTION));
    }

    @Test
    public void finalOrderSuccessWithFinalOrder() throws Exception {
        CaseDetails caseDetails = buildHearingPackDocumentTestData();

        List<HearingOrderCollectionData> finalOrderCollection = Arrays.asList(HearingOrderCollectionData.builder()
            .hearingOrderDocuments(HearingOrderDocument
                .builder()
                .uploadDraftDocument(CaseDocument.builder().build())
                .build())
            .build());

        caseDetails.getData().put(FINAL_ORDER_COLLECTION, finalOrderCollection);

        contestedCaseOrderService.stampFinalOrder(caseDetails, AUTH_TOKEN);

        verify(genericDocumentService, times(1)).stampDocument(caseDocument(), AUTH_TOKEN);

        List<HearingOrderCollectionData> expectedFinalOrderCollection =
            (List<HearingOrderCollectionData>) caseDetails.getData().get(FINAL_ORDER_COLLECTION);

        assertEquals(expectedFinalOrderCollection.size(), 2);
        assertEquals(expectedFinalOrderCollection.get(1).getHearingOrderDocuments().getUploadDraftDocument(), caseDocument());
    }
}
