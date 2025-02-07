package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.evidence.FileUploadResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementAuditService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;

@ExtendWith(MockitoExtension.class)
class OrderDateServiceTest  {
    @InjectMocks
    private OrderDateService orderDateService;
    @Mock
    private EvidenceManagementAuditService emService;

    @BeforeEach
    void setUp() {
        List<FileUploadResponse> auditResponse = new ArrayList<>();
        FileUploadResponse fileUploadResponse = FileUploadResponse.builder()
            .fileUrl(DOC_URL)
            .fileName(FILE_NAME)
            .mimeType("applicant/pdf")
            .createdBy("courtAdmin")
            .lastModifiedBy("courtAdmin")
            .createdOn(String.valueOf(LocalDateTime.of(2023, 5, 21, 10, 10, 10)))
            .modifiedOn(String.valueOf(LocalDateTime.of(2023, 5, 21, 10, 10, 10)))
            .build();
        auditResponse.add(fileUploadResponse);
        lenient().when(emService.audit(any(), any())).thenReturn(auditResponse);
    }

    @Test
    void addCreatedDateInFinalOrderWhenFinalOrderIsNotEmptyThenSetDate() {
        List<DirectionOrderCollection> orderCollections = new ArrayList<>();
        DirectionOrderCollection orderCollection
            = DirectionOrderCollection.builder().value(DirectionOrder
            .builder().uploadDraftDocument(caseDocument()).build()).build();
        orderCollections.add(orderCollection);

        List<DirectionOrderCollection> directionOrderCollections
            = orderDateService.addCreatedDateInFinalOrder(orderCollections, AUTH_TOKEN);

        DirectionOrder value = directionOrderCollections.get(0).getValue();
        LocalDateTime dateTime = LocalDateTime.of(2023, 5, 21, 10, 10, 10);

        assertEquals(dateTime, value.getOrderDateTime());
        assertEquals(YesOrNo.YES, value.getIsOrderStamped());
        verify(emService).audit(anyList(), any());
    }

    @Test
    void addCreatedDateInFinalOrderWhenFinalOrderIsNotEmptyAndDocumentIsStampedThenReturnOriginalOrder() {
        List<DirectionOrderCollection> orderCollections = new ArrayList<>();
        LocalDateTime orderDateTime = LocalDateTime.of(2023, 11, 1, 17, 10, 10);
        DirectionOrderCollection orderCollection
            = DirectionOrderCollection.builder().value(DirectionOrder
            .builder().uploadDraftDocument(caseDocument()).isOrderStamped(YesOrNo.YES).orderDateTime(orderDateTime).build()).build();
        orderCollections.add(orderCollection);

        List<DirectionOrderCollection> directionOrderCollections
            = orderDateService.addCreatedDateInFinalOrder(orderCollections, AUTH_TOKEN);

        DirectionOrder value = directionOrderCollections.get(0).getValue();

        assertEquals(orderDateTime, value.getOrderDateTime());
        assertEquals(YesOrNo.YES, value.getIsOrderStamped());
        verify(emService).audit(anyList(), any());
    }

    @Test
    void addCreatedDateInFinalOrderWhenFinalOrderIsEmptyThenDoNotCallEvidenceService() {
        List<DirectionOrderCollection> orderCollections = new ArrayList<>();

        List<DirectionOrderCollection> directionOrderCollections
            = orderDateService.addCreatedDateInFinalOrder(orderCollections, AUTH_TOKEN);

        assertThat(directionOrderCollections).isEmpty();
        verify(emService, never()).audit(anyList(), any());
    }

    @Test
    void addCreatedDateInFinalOrderWhenFinalOrderIsNullThenDoNotCallEvidenceService() {
        List<DirectionOrderCollection> directionOrderCollections
            = orderDateService.addCreatedDateInFinalOrder(null, AUTH_TOKEN);

        assertThat(directionOrderCollections).isEmpty();
        verify(emService, never()).audit(anyList(), any());
    }

    @Test
    void addCreatedDateInUploadedOrderWhenCollectionIsNotEmptyThenSetDate() {
        List<DirectionOrderCollection> orderCollections = new ArrayList<>();
        DirectionOrderCollection orderCollection
            = DirectionOrderCollection.builder().value(DirectionOrder
            .builder().uploadDraftDocument(caseDocument()).build()).build();
        orderCollections.add(orderCollection);

        List<DirectionOrderCollection> directionOrderCollections
            = orderDateService.addCreatedDateInUploadedOrder(orderCollections, AUTH_TOKEN);

        DirectionOrder value = directionOrderCollections.get(0).getValue();
        LocalDateTime dateTime = LocalDateTime.of(2023, 5, 21, 10, 10, 10);
        assertEquals(dateTime, value.getOrderDateTime());
        assertEquals(YesOrNo.NO, value.getIsOrderStamped());
        verify(emService).audit(anyList(), any());
    }

    @Test
    void addCreatedDateInUploadedOrderWhenCollectionIsNotEmptyAndStampedIsNoThenSetDate() {
        List<DirectionOrderCollection> orderCollections = new ArrayList<>();
        DirectionOrderCollection orderCollection
            = DirectionOrderCollection.builder().value(DirectionOrder
            .builder().uploadDraftDocument(caseDocument()).isOrderStamped(YesOrNo.NO)
                .build()).build();
        orderCollections.add(orderCollection);

        List<DirectionOrderCollection> directionOrderCollections
            = orderDateService.addCreatedDateInUploadedOrder(orderCollections, AUTH_TOKEN);

        DirectionOrder value = directionOrderCollections.get(0).getValue();
        LocalDateTime dateTime = LocalDateTime.of(2023, 5, 21, 10, 10, 10);
        assertEquals(dateTime, value.getOrderDateTime());
        assertEquals(YesOrNo.NO, value.getIsOrderStamped());
        verify(emService).audit(anyList(), any());
    }

    @Test
    void addCreatedDateInUploadedOrderWhenCollectionIsEmptyThenDoNotCallEvidenceService() {
        List<DirectionOrderCollection> orderCollections = new ArrayList<>();

        List<DirectionOrderCollection> directionOrderCollections
            = orderDateService.addCreatedDateInUploadedOrder(orderCollections, AUTH_TOKEN);

        assertThat(directionOrderCollections).isEmpty();
        verify(emService, never()).audit(anyList(), any());
    }

    @Test
    void addCreatedDateInUploadedOrderWhenCollectionIsNullThenDoNotCallEvidenceService() {
        List<DirectionOrderCollection> directionOrderCollections
            = orderDateService.addCreatedDateInUploadedOrder(null, AUTH_TOKEN);

        assertThat(directionOrderCollections).isEmpty();
        verify(emService, never()).audit(anyList(), any());
    }

    @Test
    void shouldRetainAdditionalDocuments() {
        List<DirectionOrderCollection> orderCollections = new ArrayList<>();
        List<DocumentCollection> listOfAdditionalDocuments = List.of(
            DocumentCollection.builder().value(caseDocument("attachment", "attachment")).build()
        );
        DirectionOrderCollection orderCollection
            = DirectionOrderCollection.builder().value(DirectionOrder
            .builder().uploadDraftDocument(caseDocument()).additionalDocuments(listOfAdditionalDocuments).build()).build();
        orderCollections.add(orderCollection);

        List<DirectionOrderCollection> directionOrderCollections
            = orderDateService.addCreatedDateInUploadedOrder(orderCollections, AUTH_TOKEN);

        DirectionOrder value = directionOrderCollections.get(0).getValue();
        LocalDateTime dateTime = LocalDateTime.of(2023, 5, 21, 10, 10, 10);
        assertEquals(dateTime, value.getOrderDateTime());
        assertEquals(YesOrNo.NO, value.getIsOrderStamped());
        assertEquals(listOfAdditionalDocuments, value.getAdditionalDocuments());
        verify(emService).audit(anyList(), any());
    }
}