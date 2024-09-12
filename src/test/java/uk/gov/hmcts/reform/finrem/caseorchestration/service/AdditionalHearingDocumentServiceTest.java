package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Court;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetail;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetailCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingOrderAdditionalDocCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingOrderCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingOrderDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingTypeDirection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.KentSurreyCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Region;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionSouthEastFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.State;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DefaultCourtListWrapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDetailsFromResource;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ADDITIONAL_HEARING_DOCUMENT_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_UPLOADED_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

public class AdditionalHearingDocumentServiceTest extends BaseServiceTest {

    @Autowired
    private AdditionalHearingDocumentService additionalHearingDocumentService;
    @Autowired
    private ObjectMapper objectMapper;

    @Captor
    private ArgumentCaptor<CaseDetails> documentGenerationRequestCaseDetailsCaptor;

    @MockBean
    GenericDocumentService genericDocumentService;
    @MockBean
    BulkPrintService bulkPrintService;
    @MockBean
    NotificationService notificationService;

    @MockBean
    OrderDateService orderDateService;

    @MockBean
    FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Before
    public void setUp() {
        when(genericDocumentService.generateDocument(any(), any(), any(), any())).thenReturn(caseDocument());
    }

    @Test
    public void convertToPdf() {
        when(genericDocumentService.convertDocumentIfNotPdfAlready(any(), eq(AUTH_TOKEN), any())).thenReturn(caseDocument());
        CaseDocument caseDocument = caseDocument(DOC_URL, "app_docs.docx", BINARY_URL);
        CaseDocument toPdf = additionalHearingDocumentService.convertToPdf(caseDocument, AUTH_TOKEN, caseId);
        assertEquals("app_docs.pdf", toPdf.getDocumentFilename());
    }

    @Test
    public void generateAndAddAdditionalHearingDocument() throws JsonProcessingException {
        CaseDetails caseDetails = caseDetailsFromResource("/fixtures/bulkprint/bulk-print-additional-hearing.json", objectMapper);
        caseDetails.getData().put(HEARING_DATE, "2021-01-01");
        additionalHearingDocumentService.createAdditionalHearingDocuments(AUTH_TOKEN, caseDetails);

        verify(genericDocumentService, times(1)).generateDocument(any(),
            documentGenerationRequestCaseDetailsCaptor.capture(), any(), any());

        CaseDetails captorCaseDetails = documentGenerationRequestCaseDetailsCaptor.getValue();
        Map<String, Object> data = captorCaseDetails.getData();

        assertThat(data.get("CCDCaseNumber"), is(1234567890L));
        assertThat(data.get("DivorceCaseNumber"), is("AB01D23456"));
        assertThat(data.get("ApplicantName"), is("Test Applicant"));
        assertThat(data.get("RespondentName"), is("Name Respondent"));

        assertThat(data.get("HearingType"), is("Directions (DIR)"));
        assertThat(data.get("HearingVenue"), is("Chesterfield County Court"));
        assertThat(data.get("HearingDate"), is("2021-01-01"));
        assertThat(data.get("HearingTime"), is("12:00"));
        assertThat(data.get("HearingLength"), is("30 minutes"));
        assertThat(data.get("AnyOtherDirections"), is("N/A"));
        assertThat(data.get("AdditionalHearingDated"), is(notNullValue()));

        assertThat(data.get("CourtName"), is("Chesterfield County Court"));
        assertThat(data.get("CourtAddress"), is("Tapton Lane, Chesterfield S41 7TW"));
        assertThat(data.get("CourtPhone"), is("0115 910 3504"));
        assertThat(data.get("CourtEmail"), is("FRCNottingham@justice.gov.uk"));

        assertThat(caseDetails.getData().get(ADDITIONAL_HEARING_DOCUMENT_COLLECTION), is(notNullValue()));
    }

    @Test
    public void getHearingOrderAdditionalDocuments() {
        Map<String, Object> caseData = baseCaseData();
        caseData.put(HEARING_UPLOADED_DOCUMENT, Collections.EMPTY_LIST);
        List<HearingOrderAdditionalDocCollectionData> hearingOrderAdditionalDocuments
            = additionalHearingDocumentService.getHearingOrderAdditionalDocuments(caseData);
        assertTrue(hearingOrderAdditionalDocuments.isEmpty());
    }

    @Test
    public void givenCreateAndStoreAdditionalHearingDocumentsWhenFinalOrderCollIsEmpty_thenHandlerWillAddNewOrderToFinalOrder()
        throws JsonProcessingException {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();
        List<DirectionOrderCollection> orderCollections = new ArrayList<>();

        when(orderDateService.addCreatedDateInFinalOrder(orderCollections, AUTH_TOKEN)).thenReturn(orderCollections);

        List<DirectionOrderCollection> uploadOrderCollections = new ArrayList<>();
        LocalDateTime uploadOrderDateTime = LocalDateTime.of(2023, 12, 1, 17, 10, 10);
        DirectionOrderCollection uploadOrderCollection
            = DirectionOrderCollection.builder().value(DirectionOrder
            .builder().uploadDraftDocument(caseDocument()).isOrderStamped(YesOrNo.YES).orderDateTime(uploadOrderDateTime).build()).build();
        uploadOrderCollections.add(uploadOrderCollection);
        data.setUploadHearingOrder(uploadOrderCollections);
        when(orderDateService.addCreatedDateInUploadedOrder(uploadOrderCollections, AUTH_TOKEN)).thenReturn(uploadOrderCollections);
        when(genericDocumentService.stampDocument(any(), any(), any(), any())).thenReturn(caseDocument());

        List<DirectionDetailCollection> directionDetailsCollection = new ArrayList<>();

        DirectionDetail directionDetail = DirectionDetail.builder()
            .isAnotherHearingYN(YesOrNo.YES)
            .typeOfHearing(HearingTypeDirection.FH)
            .hearingTime("12")
            .timeEstimate("12")
            .dateOfHearing(LocalDate.of(2020, 1, 1))
            .localCourt(getTestCourt()).build();
        DirectionDetailCollection detailCollection = DirectionDetailCollection.builder().value(directionDetail).build();
        directionDetailsCollection.add(detailCollection);
        data.setDirectionDetailsCollection(directionDetailsCollection);

        Map<String, Object> caseData = baseCaseData();
        List<HearingOrderCollectionData> hearingOrderCollectionData = buildHearingOrderCollectionData();
        caseData.put(HEARING_ORDER_COLLECTION, hearingOrderCollectionData);
        CaseDetails details = CaseDetails
            .builder()
            .id(1234567890L)
            .data(caseData)
            .build();
        when(finremCaseDetailsMapper.mapToCaseDetails(caseDetails)).thenReturn(details);

        additionalHearingDocumentService.createAndStoreAdditionalHearingDocuments(caseDetails, AUTH_TOKEN);

        assertEquals(1, data.getFinalOrderCollection().size());
        assertEquals(1, data.getUploadHearingOrder().size());
        assertEquals(FILE_NAME, data.getLatestDraftHearingOrder().getDocumentFilename());
        assertEquals(caseDocument(), data.getAdditionalHearingDocuments().get(0).getValue().getDocument());
    }

    @Test
    public void givenCreateAndStoreAdditionalHearingDocumentsWhenFinalOrderHasSameOrder_thenHandlerWillNotAddNewOrderToFinalOrder()
        throws JsonProcessingException {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();
        List<DirectionOrderCollection> orderCollections = new ArrayList<>();
        LocalDateTime orderDateTime = LocalDateTime.of(2022, 11, 1, 17, 10, 10);
        DirectionOrderCollection orderCollection
            = DirectionOrderCollection.builder().value(DirectionOrder
            .builder().uploadDraftDocument(caseDocument()).isOrderStamped(YesOrNo.YES).orderDateTime(orderDateTime).build()).build();
        orderCollections.add(orderCollection);
        data.setFinalOrderCollection(orderCollections);
        when(orderDateService.addCreatedDateInFinalOrder(orderCollections, AUTH_TOKEN)).thenReturn(orderCollections);

        List<DirectionOrderCollection> uploadOrderCollections = new ArrayList<>();
        LocalDateTime uploadOrderDateTime = LocalDateTime.of(2023, 12, 1, 17, 10, 10);
        DirectionOrderCollection uploadOrderCollection
            = DirectionOrderCollection.builder().value(DirectionOrder
            .builder().uploadDraftDocument(caseDocument()).isOrderStamped(YesOrNo.YES).orderDateTime(uploadOrderDateTime).build()).build();
        uploadOrderCollections.add(uploadOrderCollection);
        data.setUploadHearingOrder(uploadOrderCollections);
        when(orderDateService.addCreatedDateInUploadedOrder(uploadOrderCollections, AUTH_TOKEN)).thenReturn(uploadOrderCollections);
        when(genericDocumentService.stampDocument(any(), any(), any(), any())).thenReturn(caseDocument());

        List<DirectionDetailCollection> directionDetailsCollection = new ArrayList<>();
        DirectionDetail directionDetail = DirectionDetail.builder()
            .isAnotherHearingYN(YesOrNo.YES)
            .typeOfHearing(HearingTypeDirection.FH)
            .hearingTime("12")
            .timeEstimate("12")
            .dateOfHearing(LocalDate.of(2020, 1, 1))
            .localCourt(getTestCourt()).build();
        DirectionDetailCollection detailCollection = DirectionDetailCollection.builder().value(directionDetail).build();
        directionDetailsCollection.add(detailCollection);
        data.setDirectionDetailsCollection(directionDetailsCollection);

        Map<String, Object> caseData = baseCaseData();
        List<HearingOrderCollectionData> hearingOrderCollectionData = buildHearingOrderCollectionData();
        caseData.put(HEARING_ORDER_COLLECTION, hearingOrderCollectionData);
        CaseDetails details = CaseDetails
            .builder()
            .id(1234567890L)
            .data(caseData)
            .build();
        when(finremCaseDetailsMapper.mapToCaseDetails(caseDetails)).thenReturn(details);

        additionalHearingDocumentService.createAndStoreAdditionalHearingDocuments(caseDetails, AUTH_TOKEN);

        assertEquals(1, data.getFinalOrderCollection().size());
        assertEquals(1, data.getUploadHearingOrder().size());
        assertEquals(FILE_NAME, data.getLatestDraftHearingOrder().getDocumentFilename());
        assertEquals(caseDocument(), data.getAdditionalHearingDocuments().get(0).getValue().getDocument());
    }

    @Test
    public void givenCreateAndStoreAdditionalHearingDocumentsWhenFinalOrderIsNotSameOrder_thenHandlerWillAddNewOrderToFinalOrder()
        throws JsonProcessingException {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();
        List<DirectionOrderCollection> orderCollections = new ArrayList<>();
        LocalDateTime orderDateTime = LocalDateTime.of(2022, 11, 1, 17, 10, 10);
        DirectionOrderCollection orderCollection
            = DirectionOrderCollection.builder().value(DirectionOrder
            .builder().uploadDraftDocument(caseDocument("url", "abc","binary"))
            .isOrderStamped(YesOrNo.YES).orderDateTime(orderDateTime).build()).build();
        orderCollections.add(orderCollection);
        data.setFinalOrderCollection(orderCollections);
        when(orderDateService.addCreatedDateInFinalOrder(orderCollections, AUTH_TOKEN)).thenReturn(orderCollections);

        List<DirectionOrderCollection> uploadOrderCollections = new ArrayList<>();
        LocalDateTime uploadOrderDateTime = LocalDateTime.of(2023, 12, 1, 17, 10, 10);
        DirectionOrderCollection uploadOrderCollection
            = DirectionOrderCollection.builder().value(DirectionOrder
            .builder().uploadDraftDocument(caseDocument()).isOrderStamped(YesOrNo.YES).orderDateTime(uploadOrderDateTime).build()).build();
        uploadOrderCollections.add(uploadOrderCollection);
        data.setUploadHearingOrder(uploadOrderCollections);
        when(orderDateService.addCreatedDateInUploadedOrder(uploadOrderCollections, AUTH_TOKEN)).thenReturn(uploadOrderCollections);
        when(genericDocumentService.stampDocument(any(), any(), any(), any())).thenReturn(caseDocument());

        List<DirectionDetailCollection> directionDetailsCollection = new ArrayList<>();

        DirectionDetail directionDetail = DirectionDetail.builder()
            .isAnotherHearingYN(YesOrNo.YES)
            .typeOfHearing(HearingTypeDirection.FH)
            .hearingTime("12")
            .timeEstimate("12")
            .dateOfHearing(LocalDate.of(2020, 1, 1))
            .localCourt(getTestCourt()).build();
        DirectionDetailCollection detailCollection = DirectionDetailCollection.builder().value(directionDetail).build();
        directionDetailsCollection.add(detailCollection);
        data.setDirectionDetailsCollection(directionDetailsCollection);

        Map<String, Object> caseData = baseCaseData();
        List<HearingOrderCollectionData> hearingOrderCollectionData = buildHearingOrderCollectionData();
        caseData.put(HEARING_ORDER_COLLECTION, hearingOrderCollectionData);
        CaseDetails details = CaseDetails
            .builder()
            .id(1234567890L)
            .data(caseData)
            .build();
        when(finremCaseDetailsMapper.mapToCaseDetails(caseDetails)).thenReturn(details);

        additionalHearingDocumentService.createAndStoreAdditionalHearingDocuments(caseDetails, AUTH_TOKEN);

        assertEquals(2, data.getFinalOrderCollection().size());
        assertEquals(1, data.getUploadHearingOrder().size());
        assertEquals(FILE_NAME, data.getLatestDraftHearingOrder().getDocumentFilename());
        assertEquals(caseDocument(), data.getAdditionalHearingDocuments().get(0).getValue().getDocument());
    }

    @Test
    public void givenCreateAndStoreAdditionalHearingDocumentsWhenFinalOrderIsNotSameOrderAndNoAnotherHearing_thenHandle()
        throws JsonProcessingException {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();
        List<DirectionOrderCollection> orderCollections = new ArrayList<>();
        LocalDateTime orderDateTime = LocalDateTime.of(2022, 11, 1, 17, 10, 10);
        DirectionOrderCollection orderCollection
            = DirectionOrderCollection.builder().value(DirectionOrder
            .builder().uploadDraftDocument(caseDocument("url", "abc","binary"))
            .isOrderStamped(YesOrNo.YES).orderDateTime(orderDateTime).build()).build();
        orderCollections.add(orderCollection);
        data.setFinalOrderCollection(orderCollections);
        when(orderDateService.addCreatedDateInFinalOrder(orderCollections, AUTH_TOKEN)).thenReturn(orderCollections);

        List<DirectionOrderCollection> uploadOrderCollections = new ArrayList<>();
        LocalDateTime uploadOrderDateTime = LocalDateTime.of(2023, 12, 1, 17, 10, 10);
        DirectionOrderCollection uploadOrderCollection
            = DirectionOrderCollection.builder().value(DirectionOrder
            .builder().uploadDraftDocument(caseDocument()).isOrderStamped(YesOrNo.YES).orderDateTime(uploadOrderDateTime).build()).build();
        uploadOrderCollections.add(uploadOrderCollection);
        data.setUploadHearingOrder(uploadOrderCollections);
        when(orderDateService.addCreatedDateInUploadedOrder(uploadOrderCollections, AUTH_TOKEN)).thenReturn(uploadOrderCollections);
        when(genericDocumentService.stampDocument(any(), any(), any(), any())).thenReturn(caseDocument());

        List<DirectionDetailCollection> directionDetailsCollection = new ArrayList<>();


        DirectionDetail directionDetail = DirectionDetail.builder()
            .isAnotherHearingYN(YesOrNo.NO)
            .typeOfHearing(HearingTypeDirection.FH)
            .hearingTime("12")
            .timeEstimate("12")
            .dateOfHearing(LocalDate.of(2020, 1, 1))
            .localCourt(getTestCourt()).build();
        DirectionDetailCollection detailCollection = DirectionDetailCollection.builder().value(directionDetail).build();
        directionDetailsCollection.add(detailCollection);
        data.setDirectionDetailsCollection(directionDetailsCollection);

        Map<String, Object> caseData = baseCaseData();
        List<HearingOrderCollectionData> hearingOrderCollectionData = buildHearingOrderCollectionData();
        caseData.put(HEARING_ORDER_COLLECTION, hearingOrderCollectionData);

        additionalHearingDocumentService.createAndStoreAdditionalHearingDocuments(caseDetails, AUTH_TOKEN);

        assertEquals(2, data.getFinalOrderCollection().size());
        assertEquals(1, data.getUploadHearingOrder().size());
        assertEquals(FILE_NAME, data.getLatestDraftHearingOrder().getDocumentFilename());
        assertNull(data.getAdditionalHearingDocuments());
    }

    @Test
    public void givenAdditionalDocumentsToBeStored_whenCreateAndStoreAdditionalHearingDocumentsFromApprovedOrder_thenStore() {
        FinremCallbackRequest request = buildCallbackRequest();
        FinremCaseDetails finremCaseDetails = request.getCaseDetails();
        CaseDocument expectedDocument = CaseDocument.builder().documentBinaryUrl(BINARY_URL).documentFilename(FILE_NAME)
            .documentUrl(DOC_URL).build();
        when(genericDocumentService.convertDocumentIfNotPdfAlready(any(), any(), any())).thenReturn(expectedDocument);
        Map<String, Object> caseData = baseCaseData();
        List<HearingOrderCollectionData> hearingOrderCollectionData = buildHearingOrderCollectionData();
        caseData.put(HEARING_ORDER_COLLECTION, hearingOrderCollectionData);

        FinremCaseData data = finremCaseDetails.getData();
        List<DirectionOrderCollection> uploadHearingOrder = new ArrayList<>();
        DirectionOrder directionOrder = DirectionOrder.builder().uploadDraftDocument(caseDocument())
            .orderDateTime(LocalDateTime.now()).isOrderStamped(YesOrNo.YES).build();
        DirectionOrderCollection orderCollection = DirectionOrderCollection.builder().value(directionOrder).build();
        uploadHearingOrder.add(orderCollection);
        data.setUploadHearingOrder(uploadHearingOrder);

        when(genericDocumentService.stampDocument(any(), any(), any(), any())).thenReturn(expectedDocument);

        additionalHearingDocumentService.createAndStoreAdditionalHearingDocumentsFromApprovedOrder(AUTH_TOKEN, finremCaseDetails);

        CaseDocument actualDocument = data.getLatestDraftHearingOrder();
        assertEquals(expectedDocument, actualDocument);
        verify(genericDocumentService).stampDocument(any(), any(), any(), any());
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

    private Map<String, Object> baseCaseData() {
        Map<String, Object> caseData = new HashMap<>();

        caseData.put(DIVORCE_CASE_NUMBER, "AB01D23456");
        caseData.put(APPLICANT_FIRST_MIDDLE_NAME, "Test");
        caseData.put(APPLICANT_LAST_NAME, "Applicant");
        caseData.put(CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME, "Name");
        caseData.put(CONTESTED_RESPONDENT_LAST_NAME, "Respondent");

        return caseData;
    }

    private List<HearingOrderCollectionData> buildHearingOrderCollectionData() {
        CaseDocument caseDocument = CaseDocument
            .builder()
            .documentBinaryUrl("docBin")
            .documentFilename("docFilename")
            .documentUrl("docUrl")
            .build();

        HearingOrderDocument hearingOrderDocument = HearingOrderDocument
            .builder()
            .uploadDraftDocument(caseDocument)
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
    public void printAdditionalHearingDocuments_forBothSolicitors() throws JsonProcessingException {
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
    public void printAdditionalHearingDocuments_forNeitherSolicitor() throws JsonProcessingException {
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
    public void printAdditionalHearingDocuments_forRespondentSolicitor() throws JsonProcessingException {
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
    public void printAdditionalHearingDocuments_forContestedAppSolicitor() throws JsonProcessingException {
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
    public void givenCase_whenGetApprovedHearingOrdersCalledButNoHearingOrderAvailalle_thenReturnEmptyList() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();


        List<DirectionOrderCollection> approvedHearingOrders
            = additionalHearingDocumentService.getApprovedHearingOrders(caseDetails, AUTH_TOKEN);

        assertTrue(approvedHearingOrders.isEmpty());
        verify(orderDateService).addCreatedDateInUploadedOrder(any(),any());
    }


    @Test
    public void givenCase_whenGetApprovedHearingOrdersCalledHearingOrderAvailalle_thenReturnEmptyList() {
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

        when(orderDateService.addCreatedDateInUploadedOrder(uploadHearingOrder, AUTH_TOKEN)).thenReturn(mockOrder);

        List<DirectionOrderCollection> approvedHearingOrders
            = additionalHearingDocumentService.getApprovedHearingOrders(caseDetails, AUTH_TOKEN);

        assertEquals(1, approvedHearingOrders.size());
        assertEquals(YesOrNo.YES, approvedHearingOrders.get(0).getValue().getIsOrderStamped());
        verify(orderDateService).addCreatedDateInUploadedOrder(any(),any());
    }

    @Test
    public void givenCase_whenAddToFinalOrderCollection_thenReturnUpdatedList() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();


        DirectionOrder directionOrder = DirectionOrder.builder()
            .uploadDraftDocument(caseDocument()).orderDateTime(LocalDateTime.now()).isOrderStamped(YesOrNo.YES).build();
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

        when(orderDateService.addCreatedDateInFinalOrder(uploadHearingOrder, AUTH_TOKEN)).thenReturn(mockOrder);
        DocumentHelper mockHelper = mock(DocumentHelper.class);
        when(mockHelper.checkIfOrderAlreadyInFinalOrderCollection(anyList(), any())).thenReturn(false);

        additionalHearingDocumentService.addToFinalOrderCollection(caseDetails, AUTH_TOKEN);

        assertEquals(1, data.getFinalOrderCollection().size());
        assertEquals(YesOrNo.YES, data.getFinalOrderCollection().get(0).getValue().getIsOrderStamped());
        verify(orderDateService).addCreatedDateInFinalOrder(any(),any());
    }


    @Test
    public void givenCase_whenAddToFinalOrderCollectionCalledButOrderAlreadyInCollection_thenReturnOriginalList() {
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

        when(orderDateService.addCreatedDateInFinalOrder(uploadHearingOrder, AUTH_TOKEN)).thenReturn(mockOrder);
        DocumentHelper mockHelper = mock(DocumentHelper.class);
        when(mockHelper.checkIfOrderAlreadyInFinalOrderCollection(anyList(), any())).thenReturn(true);

        additionalHearingDocumentService.addToFinalOrderCollection(caseDetails, AUTH_TOKEN);

        assertEquals(1, data.getFinalOrderCollection().size());
        assertEquals(YesOrNo.YES, data.getFinalOrderCollection().get(0).getValue().getIsOrderStamped());
        verify(orderDateService).addCreatedDateInFinalOrder(any(),any());
    }

    @Test
    public void sortDirectionDetailsCollection() {
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
        assertEquals(5, directionDetailsCollection.size());
        assertEquals(secondDate, directionDetailsCollection.get(0).getValue().getDateOfHearing());
        assertEquals(fourthDate, directionDetailsCollection.get(1).getValue().getDateOfHearing());
        assertEquals(firstDate, directionDetailsCollection.get(2).getValue().getDateOfHearing());
        assertNull(directionDetailsCollection.get(3).getValue().getDateOfHearing());
        assertNull(directionDetailsCollection.get(4).getValue().getDateOfHearing());
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
