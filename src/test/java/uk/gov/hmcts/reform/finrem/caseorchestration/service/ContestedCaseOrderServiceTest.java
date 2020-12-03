package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.PAPER_APPLICATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ADDITIONAL_HEARING_DOCUMENT_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_LATEST_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_ORDER_OTHER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LATEST_DRAFT_HEARING_ORDER;

public class ContestedCaseOrderServiceTest extends BaseServiceTest {

    @Autowired
    private ContestedCaseOrderService contestedCaseOrderService;

    @MockBean
    private BulkPrintService bulkPrintService;

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

        //Force update case data with JSON properties
        Map<String, Object> caseData = caseDetails.getData();
        caseData = mapper.readValue(mapper.writeValueAsString(caseData), HashMap.class);
        caseDetails.setData(caseData);

        return caseDetails;
    }
}
