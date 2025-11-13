package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingTypeDirection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.KentSurreyCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Region;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionSouthEastFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.State;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DefaultCourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing.FinremAdditionalHearingCorresponder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDetailsFromResource;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ADDITIONAL_HEARING_DOCUMENT_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@ExtendWith(MockitoExtension.class)
class AdditionalHearingDocumentServiceTest {

    private AdditionalHearingDocumentService additionalHearingDocumentService;
    private ObjectMapper objectMapper;
    @Captor
    private ArgumentCaptor<CaseDetails> documentGenerationRequestCaseDetailsCaptor;
    @Mock
    GenericDocumentService genericDocumentService;
    @Mock
    BulkPrintService bulkPrintService;
    @Mock
    NotificationService notificationService;
    @Mock
    OrderDateService orderDateService;
    FinremCaseDetailsMapper finremCaseDetailsMapper;
    DocumentHelper documentHelper;
    @Mock
    FinremAdditionalHearingCorresponder finremAdditionalHearingCorresponder;
    @Mock
    DocumentConfiguration documentConfiguration;
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
            genericDocumentService, documentConfiguration, documentHelper, objectMapper, bulkPrintService, caseDataService,
            notificationService, finremAdditionalHearingCorresponder, finremCaseDetailsMapper, orderDateService);

        lenient().when(genericDocumentService.generateDocument(any(), any(), any(), any())).thenReturn(caseDocument());
    }

    @Test
    void convertToPdf() {
        when(genericDocumentService.convertDocumentIfNotPdfAlready(any(), eq(AUTH_TOKEN), any())).thenReturn(caseDocument());
        CaseDocument caseDocument = caseDocument(DOC_URL, "app_docs.docx", BINARY_URL);
        CaseDocument toPdf = additionalHearingDocumentService.convertToPdf(caseDocument, AUTH_TOKEN, CONTESTED);
        assertEquals("app_docs.pdf", toPdf.getDocumentFilename());
    }

    @Test
    void generateAndAddAdditionalHearingDocument() throws JsonProcessingException {
        CaseDetails caseDetails = caseDetailsFromResource("/fixtures/bulkprint/bulk-print-additional-hearing.json", objectMapper);
        caseDetails.getData().put(HEARING_DATE, "2021-01-01");

        when(caseDataService.buildFullApplicantName(any(CaseDetails.class))).thenReturn("Test Applicant");
        when(caseDataService.buildFullRespondentName(any(CaseDetails.class))).thenReturn("Name Respondent");

        additionalHearingDocumentService.createAdditionalHearingDocuments(AUTH_TOKEN, caseDetails);

        verify(genericDocumentService, times(1)).generateDocument(any(),
            documentGenerationRequestCaseDetailsCaptor.capture(), any(), any());

        CaseDetails captorCaseDetails = documentGenerationRequestCaseDetailsCaptor.getValue();
        Map<String, Object> data = captorCaseDetails.getData();

        assertThat(data).extracting("CCDCaseNumber").isEqualTo(1234567890L);
        assertThat(data).extracting("DivorceCaseNumber").isEqualTo("AB01D23456");
        assertThat(data).extracting("ApplicantName").isEqualTo("Test Applicant");
        assertThat(data).extracting("RespondentName").isEqualTo("Name Respondent");

        assertThat(data).extracting("HearingType").isEqualTo("Directions (DIR)");
        assertThat(data).extracting("HearingVenue").isEqualTo("Chesterfield County Court");
        assertThat(data).extracting("HearingDate").isEqualTo("2021-01-01");
        assertThat(data).extracting("HearingTime").isEqualTo("12:00");
        assertThat(data).extracting("HearingLength").isEqualTo("30 minutes");
        assertThat(data).extracting("AnyOtherDirections").isEqualTo("N/A");
        assertThat(data).extracting("AdditionalHearingDated").isNotNull();

        assertThat(data).extracting("CourtName").isEqualTo("Chesterfield County Court");
        assertThat(data).extracting("CourtAddress").isEqualTo("Tapton Lane, Chesterfield S41 7TW");
        assertThat(data).extracting("CourtPhone").isEqualTo("0300 123 5577");
        assertThat(data).extracting("CourtEmail").isEqualTo("FRCNottingham@justice.gov.uk");

        assertThat(data).extracting(ADDITIONAL_HEARING_DOCUMENT_COLLECTION).isNotNull();
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

        verify(genericDocumentService).stampDocument(any(), eq(AUTH_TOKEN), any(), eq(CASE_ID));
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

        verify(genericDocumentService).stampDocument(any(), eq(AUTH_TOKEN), any(), eq(CASE_ID));
    }

    @Test
    void givenAnotherHearingIsYes_whenStoreHearingNotice_shouldStoreHearingNotice() throws Exception {
        // Arrange
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        DirectionDetail directionDetail = DirectionDetail.builder()
            .isAnotherHearingYN(YesOrNo.YES)
            .typeOfHearing(HearingTypeDirection.FH)
            .dateOfHearing(LocalDate.of(2020, 1, 1))
            .hearingTime("10:00")
            .timeEstimate("12")
            .localCourt(getTestCourt())
            .build();

        DirectionDetailCollection detailCollection = DirectionDetailCollection.builder()
            .value(directionDetail)
            .build();

        FinremCaseData caseData = new FinremCaseData();
        caseData.setDirectionDetailsCollection(new ArrayList<>(List.of(detailCollection)));
        caseDetails.setData(caseData);

        // Mock dependencies
        CaseDocument additionalDocument = caseDocument("url", "doc.pdf");
        when(genericDocumentService.generateDocument(any(), any(), any(), any()))
            .thenReturn(additionalDocument);

        // Act
        additionalHearingDocumentService.storeHearingNotice(caseDetails, AUTH_TOKEN);

        // Assert
        // Verify that an additional document was generated and added
        verify(genericDocumentService).generateDocument(any(), any(), any(), any());
        assertThat(caseData.getListForHearingWrapper().getAdditionalHearingDocuments())
            .hasSize(1)
            .extracting(docCollection -> docCollection.getValue().getDocument())
            .containsExactly(additionalDocument);
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
        when(genericDocumentService.convertDocumentIfNotPdfAlready(any(), eq(AUTH_TOKEN), eq(CASE_ID)))
            .thenReturn(originalDoc);
        when(genericDocumentService.stampDocument(any(CaseDocument.class), eq(AUTH_TOKEN), any(StampType.class), eq(CASE_ID)))
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

    private Court getTestCourt() {
        return Court.builder()
            .region(Region.SOUTHEAST)
            .southEastList(RegionSouthEastFrc.KENT)
            .courtListWrapper(DefaultCourtListWrapper.builder()
                .kentSurreyCourtList(KentSurreyCourt.FR_kent_surreyList_9)
                .build())
            .build();
    }

    @Test
    void printAdditionalHearingDocuments_forBothSolicitors() throws JsonProcessingException {
        CaseDetails caseDetails = caseDetailsFromResource("/fixtures/bulkprint/bulk-print-additional-hearing.json", objectMapper);
        additionalHearingDocumentService.createAdditionalHearingDocuments(AUTH_TOKEN, caseDetails);

        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(false);
        when(notificationService.isRespondentSolicitorRegisteredAndEmailCommunicationEnabled(any())).thenReturn(false);

        additionalHearingDocumentService.bulkPrintAdditionalHearingDocuments(caseDetails, AUTH_TOKEN);

        verify(bulkPrintService, timeout(100).times(1))
            .printRespondentDocuments(any(CaseDetails.class), any(), any());
        verify(bulkPrintService, timeout(100).times(1))
            .printApplicantDocuments(any(CaseDetails.class), any(), any());
    }

    @Test
    void printAdditionalHearingDocuments_forNeitherSolicitor() throws JsonProcessingException {
        CaseDetails caseDetails = caseDetailsFromResource("/fixtures/bulkprint/bulk-print-additional-hearing.json", objectMapper);
        additionalHearingDocumentService.createAdditionalHearingDocuments(AUTH_TOKEN, caseDetails);

        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(true);
        when(notificationService.isRespondentSolicitorRegisteredAndEmailCommunicationEnabled(any())).thenReturn(true);

        additionalHearingDocumentService.bulkPrintAdditionalHearingDocuments(caseDetails, AUTH_TOKEN);

        verify(bulkPrintService, timeout(100).times(0))
            .printRespondentDocuments(any(CaseDetails.class), any(), any());
        verify(bulkPrintService, timeout(100).times(0))
            .printApplicantDocuments(any(CaseDetails.class), any(), any());
    }

    @Test
    void printAdditionalHearingDocuments_forRespondentSolicitor() throws JsonProcessingException {
        CaseDetails caseDetails = caseDetailsFromResource("/fixtures/bulkprint/bulk-print-additional-hearing.json", objectMapper);
        additionalHearingDocumentService.createAdditionalHearingDocuments(AUTH_TOKEN, caseDetails);

        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(true);
        when(notificationService.isRespondentSolicitorRegisteredAndEmailCommunicationEnabled(any())).thenReturn(false);

        additionalHearingDocumentService.bulkPrintAdditionalHearingDocuments(caseDetails, AUTH_TOKEN);

        verify(bulkPrintService, timeout(100).times(1))
            .printRespondentDocuments(any(CaseDetails.class), any(), any());
        verify(bulkPrintService, timeout(100).times(0))
            .printApplicantDocuments(any(CaseDetails.class), any(), any());
    }

    @Test
    void printAdditionalHearingDocuments_forContestedAppSolicitor() throws JsonProcessingException {
        CaseDetails caseDetails = caseDetailsFromResource("/fixtures/bulkprint/bulk-print-additional-hearing.json", objectMapper);
        additionalHearingDocumentService.createAdditionalHearingDocuments(AUTH_TOKEN, caseDetails);

        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(false);
        when(notificationService.isRespondentSolicitorRegisteredAndEmailCommunicationEnabled(any())).thenReturn(true);

        additionalHearingDocumentService.bulkPrintAdditionalHearingDocuments(caseDetails, AUTH_TOKEN);

        verify(bulkPrintService, timeout(100).times(0))
            .printRespondentDocuments(any(CaseDetails.class), any(), any());
        verify(bulkPrintService, timeout(100).times(1))
            .printApplicantDocuments(any(CaseDetails.class), any(), any());
    }

    @Test
    void givenCase_whenGetApprovedHearingOrdersCalledButNoHearingOrderAvailable_thenReturnEmptyList() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();

        List<DirectionOrderCollection> approvedHearingOrders
            = additionalHearingDocumentService.getApprovedHearingOrders(caseDetails, AUTH_TOKEN);

        assertThat(approvedHearingOrders).isEmpty();
        verify(orderDateService).syncCreatedDateAndMarkDocumentNotStamped(any(),any());
    }

    @Test
    void givenCase_whenGetApprovedHearingOrdersCalledHearingOrderAvailable_thenReturnEmptyList() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();

        DirectionOrder directionOrder = DirectionOrder.builder()
            .uploadDraftDocument(caseDocument()).build();
        DirectionOrderCollection orderCollection = DirectionOrderCollection.builder().value(directionOrder).build();
        List<DirectionOrderCollection> uploadHearingOrder = new ArrayList<>();
        uploadHearingOrder.add(orderCollection);

        FinremCaseData data = caseDetails.getData();
        data.setUploadHearingOrder(uploadHearingOrder);

        DirectionOrder directionOrder1 = DirectionOrder.builder()
            .uploadDraftDocument(caseDocument()).orderDateTime(LocalDateTime.now()).isOrderStamped(YesOrNo.YES).build();
        DirectionOrderCollection orderCollection1 = DirectionOrderCollection.builder().value(directionOrder1).build();
        List<DirectionOrderCollection> mockOrder = new ArrayList<>();
        mockOrder.add(orderCollection1);

        when(orderDateService.syncCreatedDateAndMarkDocumentNotStamped(uploadHearingOrder, AUTH_TOKEN)).thenReturn(mockOrder);

        List<DirectionOrderCollection> approvedHearingOrders
            = additionalHearingDocumentService.getApprovedHearingOrders(caseDetails, AUTH_TOKEN);

        assertThat(approvedHearingOrders).hasSize(1);
        assertThat(approvedHearingOrders)
            .extracting(order -> order.getValue().getIsOrderStamped())
            .first()
            .isEqualTo(YesOrNo.YES);
        verify(orderDateService).syncCreatedDateAndMarkDocumentNotStamped(any(),any());
    }

    @Test
    void givenCase_whenAddToFinalOrderCollection_thenReturnUpdatedList() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();

        DirectionOrder directionOrder = DirectionOrder.builder()
            .uploadDraftDocument(caseDocument()).orderDateTime(LocalDateTime.now()).isOrderStamped(YesOrNo.YES).build();
        DirectionOrderCollection orderCollection = DirectionOrderCollection.builder().value(directionOrder).build();
        List<DirectionOrderCollection> uploadHearingOrder = new ArrayList<>();
        uploadHearingOrder.add(orderCollection);

        FinremCaseData data = caseDetails.getData();
        data.setUploadHearingOrder(uploadHearingOrder);

        when(orderDateService.syncCreatedDateAndMarkDocumentStamped(isNull(), eq(AUTH_TOKEN))).thenReturn(new ArrayList<>());

        additionalHearingDocumentService.addToFinalOrderCollection(caseDetails, AUTH_TOKEN);

        assertThat(data.getFinalOrderCollection()).hasSize(1);
        assertThat(data.getFinalOrderCollection())
            .extracting(order -> order.getValue().getIsOrderStamped())
            .first()
            .isEqualTo(YesOrNo.YES);
        verify(orderDateService).syncCreatedDateAndMarkDocumentStamped(any(),any());
    }

    @Test
    void givenCase_whenAddToFinalOrderCollectionCalledButOrderAlreadyInCollection_thenReturnOriginalList() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();

        DirectionOrder directionOrder = DirectionOrder.builder()
            .uploadDraftDocument(caseDocument()).orderDateTime(LocalDateTime.now()).isOrderStamped(YesOrNo.YES).build();
        DirectionOrderCollection orderCollection = DirectionOrderCollection.builder().value(directionOrder).build();
        List<DirectionOrderCollection> uploadHearingOrder = new ArrayList<>();
        uploadHearingOrder.add(orderCollection);

        FinremCaseData data = caseDetails.getData();
        data.setUploadHearingOrder(uploadHearingOrder);
        data.setFinalOrderCollection(uploadHearingOrder);

        DirectionOrder directionOrder1 = DirectionOrder.builder()
            .uploadDraftDocument(caseDocument()).orderDateTime(LocalDateTime.now()).isOrderStamped(YesOrNo.YES).build();
        DirectionOrderCollection orderCollection1 = DirectionOrderCollection.builder().value(directionOrder1).build();
        List<DirectionOrderCollection> mockOrder = new ArrayList<>();
        mockOrder.add(orderCollection1);

        when(orderDateService.syncCreatedDateAndMarkDocumentStamped(uploadHearingOrder, AUTH_TOKEN)).thenReturn(mockOrder);

        additionalHearingDocumentService.addToFinalOrderCollection(caseDetails, AUTH_TOKEN);

        assertThat(data.getFinalOrderCollection())
            .hasSize(1)
            .extracting(order -> order.getValue().getIsOrderStamped())
            .first()
            .isEqualTo(YesOrNo.YES);

        verify(orderDateService).syncCreatedDateAndMarkDocumentStamped(any(),any());
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
