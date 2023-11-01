package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.evidence.FileUploadResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementAuditService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;

public class OrderDateServiceTest extends BaseServiceTest {
    public static final String TOKEN = "Token";
    @Autowired
    private OrderDateService orderDateService;
    @MockBean
    private EvidenceManagementAuditService emService;

    @Before
    public void setUp() {
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
        when(emService.audit(any(), any())).thenReturn(auditResponse);
    }

    @Test
    public void addCreatedDateInFinalOrderWhenFinalOrderIsNotEmptyThenSetDate() {
        List<DirectionOrderCollection> orderCollections = new ArrayList<>();
        DirectionOrderCollection orderCollection
            = DirectionOrderCollection.builder().value(DirectionOrder
            .builder().uploadDraftDocument(caseDocument()).build()).build();
        orderCollections.add(orderCollection);

        List<DirectionOrderCollection> directionOrderCollections
            = orderDateService.addCreatedDateInFinalOrder(orderCollections, TOKEN);

        DirectionOrder value = directionOrderCollections.get(0).getValue();
        LocalDateTime dateTime = LocalDateTime.of(2023, 5, 21, 10, 10, 10);

        assertEquals(dateTime, value.getOrderDateTime());
        assertEquals(YesOrNo.YES, value.getIsOrderStamped());
        verify(emService).audit(anyList(), any());
    }

    @Test
    public void addCreatedDateInFinalOrderWhenFinalOrderIsEmptyThenDoNotCallEvidenceService() {
        List<DirectionOrderCollection> orderCollections = new ArrayList<>();

        List<DirectionOrderCollection> directionOrderCollections
            = orderDateService.addCreatedDateInFinalOrder(orderCollections, TOKEN);

        assertTrue(directionOrderCollections.isEmpty());
        verify(emService, never()).audit(anyList(), any());
    }

    @Test
    public void addCreatedDateInUploadedOrderWhenCollectionIsNotEmptyThenSetDate() {
        List<DirectionOrderCollection> orderCollections = new ArrayList<>();
        DirectionOrderCollection orderCollection
            = DirectionOrderCollection.builder().value(DirectionOrder
            .builder().uploadDraftDocument(caseDocument()).build()).build();
        orderCollections.add(orderCollection);

        List<DirectionOrderCollection> directionOrderCollections
            = orderDateService.addCreatedDateInUploadedOrder(orderCollections, TOKEN);

        DirectionOrder value = directionOrderCollections.get(0).getValue();
        LocalDateTime dateTime = LocalDateTime.of(2023, 5, 21, 10, 10, 10);
        assertEquals(dateTime, value.getOrderDateTime());
        assertEquals(YesOrNo.NO, value.getIsOrderStamped());
        verify(emService).audit(anyList(), any());
    }

    @Test
    public void addCreatedDateInUploadedOrderWhenCollectionIsEmptyThenDoNotCallEvidenceService() {
        List<DirectionOrderCollection> orderCollections = new ArrayList<>();

        List<DirectionOrderCollection> directionOrderCollections
            = orderDateService.addCreatedDateInUploadedOrder(orderCollections, TOKEN);

        assertTrue(directionOrderCollections.isEmpty());
        verify(emService, never()).audit(anyList(), any());
    }
}