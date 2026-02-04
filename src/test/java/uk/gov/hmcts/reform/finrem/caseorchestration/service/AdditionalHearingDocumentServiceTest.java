package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.address.LetterAddresseeGeneratorMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Court;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetail;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetailCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.KentSurreyCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Region;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionSouthEastFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.State;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DefaultCourtListWrapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@ExtendWith(MockitoExtension.class)
class AdditionalHearingDocumentServiceTest {

    private AdditionalHearingDocumentService additionalHearingDocumentService;
    private ObjectMapper objectMapper;
    @Mock
    GenericDocumentService genericDocumentService;
    @Mock
    OrderDateService orderDateService;
    FinremCaseDetailsMapper finremCaseDetailsMapper;
    DocumentHelper documentHelper;
    @Mock
    CaseDataService caseDataService;
    @Mock
    LetterAddresseeGeneratorMapper letterAddresseeGenerator;
    @Mock
    InternationalPostalService postalService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        finremCaseDetailsMapper = new FinremCaseDetailsMapper(objectMapper);

        documentHelper = new DocumentHelper(objectMapper, caseDataService, genericDocumentService, finremCaseDetailsMapper,
            letterAddresseeGenerator, postalService);
        additionalHearingDocumentService = new AdditionalHearingDocumentService(
            genericDocumentService, documentHelper, orderDateService);

        lenient().when(genericDocumentService.generateDocument(any(), any(), any(), any())).thenReturn(caseDocument());
    }

    @Test
    void givenNewOrder_whenUnprocessedOrdersExist_shouldStampAndUpdateOrderCollections() {
        // Arrange
        CaseDocument originalDoc = caseDocument("url1", "file1.pdf");
        CaseDocument stampedDoc  = caseDocument("url1", "file1-stamped.pdf");

        LocalDateTime uploadOrderDateTime = LocalDateTime.of(2023, 12, 1, 17, 10, 10);

        FinremCaseData caseData = new FinremCaseData();
        caseWithUnprocessed(caseData, originalDoc, uploadOrderDateTime);

        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .id(Long.valueOf(CASE_ID))
            .caseType(CONTESTED)
            .data(caseData)
            .build();

        mockConversionAndStamp(originalDoc, stampedDoc);

        // Act
        additionalHearingDocumentService.stampAndUpdateOrderCollections(caseDetails, AUTH_TOKEN);

        // Assert
        assertThat(caseData.getUploadHearingOrder())
            .hasSize(1)
            .extracting(DirectionOrderCollection::getValue)
            .extracting(DirectionOrder::getUploadDraftDocument)
            .isEqualTo(List.of(stampedDoc));

        assertThat(caseData.getFinalOrderCollection())
            .hasSize(1)
            .extracting(DirectionOrderCollection::getValue)
            .extracting(DirectionOrder::getUploadDraftDocument)
            .isEqualTo(List.of(stampedDoc));

        assertThat(caseData.getLatestDraftHearingOrder()).isEqualTo(stampedDoc);

        verify(genericDocumentService).stampDocument(any(), eq(AUTH_TOKEN), any(), eq(CONTESTED));
    }

    @Test
    void givenModifiedOrder_whenUnprocessedOrdersExist_shouldReplaceAndStampAndUpdateOrderCollections() {
        // Arrange
        CaseDocument originalDoc = caseDocument("url1", "file1.pdf");
        CaseDocument stampedDoc  = caseDocument("url1", "file1-stamped.pdf");

        LocalDateTime uploadOrderDateTime = LocalDateTime.of(2023, 12, 1, 17, 10, 10);

        FinremCaseData caseData = new FinremCaseData();

        //pre-populate existing uploadHearingOrder with same URL to trigger replace
        setUploadHearingOrder(caseData, originalDoc);
        //also add unprocessed with same original (so it should replace)
        caseWithUnprocessed(caseData, originalDoc, uploadOrderDateTime);

        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .id(Long.valueOf(CASE_ID))
            .caseType(CONTESTED)
            .data(caseData)
            .build();

        mockConversionAndStamp(originalDoc, stampedDoc);

        // Act
        additionalHearingDocumentService.stampAndUpdateOrderCollections(caseDetails, AUTH_TOKEN);

        // Assert
        assertThat(caseData.getUploadHearingOrder())
            .hasSize(1)
            .extracting(DirectionOrderCollection::getValue)
            .extracting(DirectionOrder::getUploadDraftDocument)
            .isEqualTo(List.of(stampedDoc));

        assertThat(caseData.getFinalOrderCollection())
            .hasSize(1)
            .extracting(DirectionOrderCollection::getValue)
            .extracting(DirectionOrder::getUploadDraftDocument)
            .isEqualTo(List.of(stampedDoc));

        assertThat(caseData.getLatestDraftHearingOrder()).isEqualTo(stampedDoc);

        verify(genericDocumentService).stampDocument(any(), eq(AUTH_TOKEN), any(), eq(CONTESTED));
    }

    private void caseWithUnprocessed(FinremCaseData caseData, CaseDocument originalDoc, LocalDateTime uploadOrderDateTime) {

        DirectionOrder directionOrder = DirectionOrder.builder()
            .uploadDraftDocument(originalDoc)
            .orderDateTime(uploadOrderDateTime)
            .isOrderStamped(YesOrNo.NO)
            .build();
        DirectionOrderCollection unprocessedOrder = DirectionOrderCollection.builder()
            .value(directionOrder)
            .build();

        List<DirectionOrderCollection> unprocessedOrders = List.of(unprocessedOrder);

        caseData.setUnprocessedUploadHearingDocuments(unprocessedOrders);
    }

    private void mockConversionAndStamp(CaseDocument originalDoc, CaseDocument stampedDoc) {
        when(orderDateService.syncCreatedDateAndMarkDocumentStamped(any(), eq(AUTH_TOKEN)))
            .thenReturn(new ArrayList<>());
        when(genericDocumentService.convertDocumentIfNotPdfAlready(any(), eq(AUTH_TOKEN), eq(CONTESTED)))
            .thenReturn(originalDoc);
        when(genericDocumentService.stampDocument(any(CaseDocument.class), eq(AUTH_TOKEN), any(StampType.class), eq(CONTESTED)))
            .thenReturn(stampedDoc);
    }

    private void setUploadHearingOrder(FinremCaseData caseData, CaseDocument originalDoc) {
        DirectionOrder directionOrder = DirectionOrder.builder()
            .uploadDraftDocument(originalDoc)
            .isOrderStamped(YesOrNo.NO)
            .build();
        DirectionOrderCollection orderCollection = DirectionOrderCollection.builder()
            .value(directionOrder)
            .build();
        caseData.setUploadHearingOrder(new ArrayList<>(List.of(orderCollection)));
    }

    @Test
    void sortDirectionDetailsCollection() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();

        List<DirectionDetailCollection> orderCollections = new ArrayList<>();
        data.setDirectionDetailsCollection(orderCollections);

        additionalHearingDocumentService.sortDirectionDetailsCollection(data);

        List<DirectionDetailCollection> directionDetailsList = data.getDirectionDetailsCollection();
        assertTrue(directionDetailsList.isEmpty());

        LocalDate firstDate = LocalDate.of(2000, 1, 2);
        DirectionDetailCollection firstOrder
            = DirectionDetailCollection.builder().value(DirectionDetail.builder().dateOfHearing(firstDate).build())
            .build();
        orderCollections.add(firstOrder);

        LocalDate secondDate = LocalDate.of(2023, 1, 2);
        DirectionDetailCollection secondOrder
            = DirectionDetailCollection.builder().value(DirectionDetail.builder().dateOfHearing(secondDate).build())
            .build();
        orderCollections.add(secondOrder);

        DirectionDetailCollection thirdOrder
            = DirectionDetailCollection.builder().value(DirectionDetail.builder().dateOfHearing(null).build())
            .build();
        orderCollections.add(thirdOrder);

        LocalDate fourthDate = LocalDate.of(2021, 1, 2);
        DirectionDetailCollection fourthOrder
            = DirectionDetailCollection.builder().value(DirectionDetail.builder().dateOfHearing(fourthDate).build())
            .build();
        orderCollections.add(fourthOrder);

        DirectionDetailCollection fifthOrder
            = DirectionDetailCollection.builder().value(DirectionDetail.builder().dateOfHearing(null).build())
            .build();
        orderCollections.add(fifthOrder);

        data.setDirectionDetailsCollection(orderCollections);

        additionalHearingDocumentService.sortDirectionDetailsCollection(data);

        List<DirectionDetailCollection> directionDetailsCollection = data.getDirectionDetailsCollection();
        assertThat(directionDetailsCollection)
            .hasSize(5);
        assertThat(directionDetailsCollection)
            .extracting(detail -> detail.getValue().getDateOfHearing())
            .containsExactly(secondDate, fourthDate, firstDate, null, null);
    }

    private FinremCallbackRequest buildCallbackRequest() {
        return FinremCallbackRequest
            .builder()
            .eventType(EventType.DIRECTION_UPLOAD_ORDER)
            .caseDetailsBefore(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(new FinremCaseData()).state(State.APPLICATION_ISSUED).build())
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(new FinremCaseData()).state(State.APPLICATION_ISSUED).build())
            .build();
    }
}
