package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.address.LetterAddresseeGeneratorMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedGeneralOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedGeneralOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralOrderCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Yes;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.FinalisedOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.FinalisedOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed.AgreedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed.AgreedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.AttachmentToShare;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.AttachmentToShareCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralOrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.OrderToShare;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.OrderToShareCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.OrdersToSend;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.GeneralOrderDocumentCategoriser;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_LATEST_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_PREVIEW_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus.APPROVED_BY_JUDGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus.PROCESSED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus.REFUSED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus.TO_BE_REVIEWED;

@ExtendWith(MockitoExtension.class)
class GeneralOrderServiceTest {

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    @Mock
    private DocumentConfiguration documentConfiguration;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private GenericDocumentService genericDocumentService;
    @Mock
    private LetterAddresseeGeneratorMapper letterAddresseeGeneratorMapper;
    @Mock
    private InternationalPostalService postalService;
    @Spy
    private CaseDataService caseDataService = new CaseDataService(objectMapper);
    @Spy
    @InjectMocks
    private GeneralOrderDocumentCategoriser generalOrderDocumentCategoriser;
    @Captor
    private ArgumentCaptor<CaseDetails> caseDetailsArgumentCaptor;

    @Spy
    private DocumentHelper documentHelper = new DocumentHelper(objectMapper, caseDataService, genericDocumentService,
        new FinremCaseDetailsMapper(objectMapper), letterAddresseeGeneratorMapper, postalService);

    @InjectMocks
    private GeneralOrderService generalOrderService;

    @BeforeEach
    void setUp() {
        lenient().when(genericDocumentService.generateDocument(any(), any(), any(), any())).thenReturn(caseDocument());
        lenient().when(documentConfiguration.getGeneralOrderFileName()).thenReturn("generalOrder.pdf");
        lenient().when(documentConfiguration.getGeneralOrderTemplate(any(CaseDetails.class))).thenReturn("FL-FRM-GOR-ENG-00484.docx");

        // Inject the manually created instance into the service
        ReflectionTestUtils.setField(generalOrderService, "generalOrderDocumentCategoriser", generalOrderDocumentCategoriser);
    }

    @Test
    void generateGeneralOrderConsented() throws Exception {
        Map<String, Object> documentMap = generalOrderService.createGeneralOrder(AUTH_TOKEN, consentedCaseDetails());

        CaseDocument result = (CaseDocument) documentMap.get(GENERAL_ORDER_PREVIEW_DOCUMENT);
        doCaseDocumentAssert(result);

        verifyAdditionalFieldsConsented();
    }

    @Test
    void generateGeneralOrderContested() throws Exception {
        Map<String, Object> documentMap = generalOrderService.createGeneralOrder(AUTH_TOKEN, contestedCaseDetails());

        CaseDocument result = (CaseDocument) documentMap.get(GENERAL_ORDER_PREVIEW_DOCUMENT);
        doCaseDocumentAssert(result);

        verifyAdditionalFieldsContested();
    }

    @Test
    void testAddContestedGeneralOrderToCollection() throws Exception {
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
        FinremCaseDetails caseDetails = contestedFinremCaseDetails();

        generalOrderService.addContestedGeneralOrderToCollection(caseDetails.getData());

        GeneralOrderWrapper generalOrderWrapper = caseDetails.getData().getGeneralOrderWrapper();
        List<ContestedGeneralOrderCollection> generalOrders = generalOrderWrapper.getGeneralOrders();
        assertThat(generalOrders).hasSize(2);
        assertEquals("http://dm-store/lhjbyuivu87y989hijbb", generalOrders.get(0).getValue().getAdditionalDocument().getDocumentUrl());
        assertEquals("generalOrder.pdf", generalOrders.get(0).getValue().getAdditionalDocument().getDocumentFilename());
        assertEquals("http://dm-store/lhjbyuivu87y989hijbb/binary", generalOrders.get(0).getValue().getAdditionalDocument().getDocumentBinaryUrl());

        assertEquals("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d",
            generalOrders.get(1).getValue().getAdditionalDocument().getDocumentUrl());
        assertEquals("WhatsApp Image 2018-07-24 at 3.05.39 PM.jpeg", generalOrders.get(1).getValue().getAdditionalDocument().getDocumentFilename());
        assertEquals("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d/binary",
            generalOrders.get(1).getValue().getAdditionalDocument().getDocumentBinaryUrl());
        assertEquals("Applicant", generalOrders.get(1).getValue().getGeneralOrderAddressTo());

        CaseDocument latestGeneralOrder = generalOrderWrapper.getGeneralOrderLatestDocument();
        assertEquals("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d",
            latestGeneralOrder.getDocumentUrl());
        assertEquals("WhatsApp Image 2018-07-24 at 3.05.39 PM.jpeg", latestGeneralOrder.getDocumentFilename());
        assertEquals("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d/binary",
            latestGeneralOrder.getDocumentBinaryUrl());
        generalOrders.forEach(order -> assertEquals(DocumentCategory.APPROVED_ORDERS.getDocumentCategoryId(),
            order.getValue().getAdditionalDocument().getCategoryId()));
    }

    @Test
    void testAddConsentedInContestedGeneralOrderInCollection() throws Exception {
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
        FinremCaseDetails caseDetails = consentedInContestedFinremCaseDetails();

        generalOrderService.addConsentedInContestedGeneralOrderToCollection(caseDetails.getData());

        GeneralOrderWrapper generalOrderWrapper = caseDetails.getData().getGeneralOrderWrapper();
        List<ContestedGeneralOrderCollection> generalOrders = generalOrderWrapper.getGeneralOrdersConsent();

        assertThat(generalOrders).hasSize(2);
        assertEquals("http://dm-store/lhjbyuivu87y989hijbb", generalOrders.get(0).getValue().getAdditionalDocument().getDocumentUrl());
        assertEquals("app_docs.pdf", generalOrders.get(0).getValue().getAdditionalDocument().getDocumentFilename());
        assertEquals("http://dm-store/lhjbyuivu87y989hijbb/binary",
            generalOrders.get(0).getValue().getAdditionalDocument().getDocumentBinaryUrl());

        assertEquals("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d",
            generalOrders.get(1).getValue().getAdditionalDocument().getDocumentUrl());
        assertEquals("WhatsApp Image 2018-07-24 at 3.05.39 PM.jpeg",
            generalOrders.get(1).getValue().getAdditionalDocument().getDocumentFilename());
        assertEquals("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d/binary",
            generalOrders.get(1).getValue().getAdditionalDocument().getDocumentBinaryUrl());
        assertEquals("Applicant", generalOrders.get(1).getValue().getGeneralOrderAddressTo());

        CaseDocument latestGeneralOrder = generalOrderWrapper.getGeneralOrderLatestDocument();
        assertEquals("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d",
            latestGeneralOrder.getDocumentUrl());
        assertEquals("WhatsApp Image 2018-07-24 at 3.05.39 PM.jpeg",
            latestGeneralOrder.getDocumentFilename());
        assertEquals("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d/binary",
            latestGeneralOrder.getDocumentBinaryUrl());
        generalOrders.forEach(order -> assertEquals(DocumentCategory.APPROVED_ORDERS_CONSENT_ORDER_TO_FINALISE_PROCEEDINGS.getDocumentCategoryId(),
            order.getValue().getAdditionalDocument().getCategoryId()));
    }

    @Test
    void testAddConsentedGeneralOrderToCollection() throws Exception {
        FinremCaseDetails caseDetails = consentedFinremCaseDetails();

        generalOrderService.addConsentedGeneralOrderToCollection(caseDetails.getData());

        GeneralOrderWrapper generalOrderWrapper = caseDetails.getData().getGeneralOrderWrapper();
        List<GeneralOrderCollectionItem> generalOrders = generalOrderWrapper.getGeneralOrderCollection();
        assertThat(generalOrders).hasSize(2);
        assertEquals("1234", generalOrders.get(0).getId());
        assertEquals("http://dm-store/lhjbyuivu87y989hijbb",
            generalOrders.get(0).getGeneralOrder().getGeneralOrderDocumentUpload().getDocumentUrl());
        assertEquals("app_docs.pdf",
            generalOrders.get(0).getGeneralOrder().getGeneralOrderDocumentUpload().getDocumentFilename());
        assertEquals("http://dm-store/lhjbyuivu87y989hijbb/binary",
            generalOrders.get(0).getGeneralOrder().getGeneralOrderDocumentUpload().getDocumentBinaryUrl());

        assertNotNull(generalOrders.get(1).getId());
        assertEquals("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d",
            generalOrders.get(1).getGeneralOrder().getGeneralOrderDocumentUpload().getDocumentUrl());
        assertEquals("WhatsApp Image 2018-07-24 at 3.05.39 PM.jpeg",
            generalOrders.get(1).getGeneralOrder().getGeneralOrderDocumentUpload().getDocumentFilename());
        assertEquals("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d/binary",
            generalOrders.get(1).getGeneralOrder().getGeneralOrderDocumentUpload().getDocumentBinaryUrl());
        assertEquals("Applicant", generalOrders.get(1).getGeneralOrder().getGeneralOrderAddressTo());

        CaseDocument latestGeneralOrder = generalOrderWrapper.getGeneralOrderLatestDocument();
        assertEquals("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d",
            latestGeneralOrder.getDocumentUrl());
        assertEquals("WhatsApp Image 2018-07-24 at 3.05.39 PM.jpeg",
            latestGeneralOrder.getDocumentFilename());
        assertEquals("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d/binary",
            latestGeneralOrder.getDocumentBinaryUrl());
    }

    @Test
    void generateGeneralOrderConsentedInContested() throws Exception {
        Map<String, Object> documentMap = generalOrderService.createGeneralOrder(AUTH_TOKEN, consentedInContestedCaseDetails());

        CaseDocument result = (CaseDocument) documentMap.get(GENERAL_ORDER_PREVIEW_DOCUMENT);
        doCaseDocumentAssert(result);

        verifyAdditionalFieldsContested();
    }

    @Test
    void getsCorrectGeneralOrdersForPrintingConsented() throws Exception {
        CaseDetails details = consentedCaseDetails();
        CaseDocument caseDocument = documentHelper.convertToCaseDocument(details.getData().get(GENERAL_ORDER_LATEST_DOCUMENT));
        CaseDocument pdfDoc = caseDocument("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d/",
            "test.pdf",
            "http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d/binary");
        when(genericDocumentService.convertDocumentIfNotPdfAlready(caseDocument, AUTH_TOKEN, CASE_ID)).thenReturn(pdfDoc);
        BulkPrintDocument latestGeneralOrder = generalOrderService.getLatestGeneralOrderAsBulkPrintDocument(details.getData(), AUTH_TOKEN, CASE_ID);
        assertEquals("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d/binary",
            latestGeneralOrder.getBinaryFileUrl());
    }

    @Test
    void getsZeroGeneralOrdersForPrintingWhenNoneConsented() throws Exception {
        CaseDetails details = consentedCaseDetails();
        details.getData().put(GENERAL_ORDER_LATEST_DOCUMENT, null);
        BulkPrintDocument latestGeneralOrder = generalOrderService.getLatestGeneralOrderAsBulkPrintDocument(details.getData(), AUTH_TOKEN, CASE_ID);
        assertNull(latestGeneralOrder);
    }

    @Test
    void whenRequestedOrderList_and_notShared_before_returnList() {
        FinremCaseData data = FinremCaseData.builder().build();

        List<DirectionOrderCollection> hearingOrderDocuments = List.of(
            DirectionOrderCollection.builder()
                .value(DirectionOrder.builder().uploadDraftDocument(caseDocument()).build())
                .build());
        data.setUploadHearingOrder(hearingOrderDocuments);

        generalOrderService.setOrderList(FinremCaseDetails.builder().data(data).build());

        assertThat(data.getSendOrderWrapper().getOrdersToSend())
            .as("Orders to send should not be null and should contain exactly one document to share with other parties.")
            .isNotNull()
            .extracting(OrdersToSend::getValue)
            .satisfies(value -> assertThat(value).hasSize(1));
    }

    @Test
    void whenRequestedGeneralOrderListPresentButGeneralOrderIsNotThere_and_shared_before_returnList() {
        FinremCaseData data = FinremCaseData.builder().build();

        data.getGeneralOrderWrapper().setGeneralOrders(getGeneralOrderCollectionNoAdditionalDocument());
        data.getSendOrderWrapper().setOrdersToSend(toOrdersToSend(caseDocument()));

        generalOrderService.setOrderList(FinremCaseDetails.builder().data(data).build());

        assertThat(data.getSendOrderWrapper().getOrdersToSend())
            .as("No document available to share with other parties")
            .isNotNull()
            .extracting(OrdersToSend::getValue)
            .satisfies(value -> assertThat(value).isEmpty());
    }

    @Test
    void whenRequestedGeneralOrderListIsEmptyPresentButGeneralOrderIsNotThere_and_shared_before_returnList() {
        FinremCaseData data = FinremCaseData.builder().build();

        ContestedGeneralOrderCollection collection = ContestedGeneralOrderCollection.builder().value(null).build();
        List<ContestedGeneralOrderCollection> collections = new ArrayList<>();
        collections.add(collection);
        data.getGeneralOrderWrapper().setGeneralOrders(collections);
        data.getSendOrderWrapper().setOrdersToSend(toOrdersToSend(caseDocument()));

        generalOrderService.setOrderList(FinremCaseDetails.builder().data(data).build());

        assertThat(data.getSendOrderWrapper().getOrdersToSend())
            .as("No document available to share with other parties")
            .isNotNull()
            .extracting(OrdersToSend::getValue)
            .satisfies(value -> assertThat(value).isEmpty());
    }

    @Test
    void whenRequestedOrderList_and_shared_before_returnList() {
        FinremCaseData data = FinremCaseData.builder().build();

        List<DirectionOrderCollection> hearingOrderDocuments = List.of(
            DirectionOrderCollection.builder()
                .value(DirectionOrder.builder().uploadDraftDocument(caseDocument()).build())
                .build());
        data.setUploadHearingOrder(hearingOrderDocuments);
        data.getGeneralOrderWrapper().setGeneralOrders(getGeneralOrderCollection());
        data.getSendOrderWrapper().setOrdersToSend(toOrdersToSend(caseDocument()));

        generalOrderService.setOrderList(FinremCaseDetails.builder().data(data).build());

        assertThat(data.getSendOrderWrapper().getOrdersToSend())
            .as("One document available to share with other parties")
            .isNotNull()
            .extracting(OrdersToSend::getValue)
            .satisfies(value -> assertThat(value).hasSize(2));
    }

    @Test
    void givenContestedCase_whenRequestedParties_thenReturnParties() {
        FinremCaseData data = FinremCaseData.builder().build();

        List<DynamicMultiSelectListElement> dynamicElementList = List.of(getDynamicElementList(CaseRole.APP_SOLICITOR.getCcdCode()),
            getDynamicElementList(CaseRole.RESP_SOLICITOR.getCcdCode()),
            getDynamicElementList(CaseRole.INTVR_SOLICITOR_1.getCcdCode()),
            getDynamicElementList(CaseRole.INTVR_SOLICITOR_2.getCcdCode()),
            getDynamicElementList(CaseRole.INTVR_SOLICITOR_3.getCcdCode()),
            getDynamicElementList(CaseRole.INTVR_SOLICITOR_4.getCcdCode()));

        DynamicMultiSelectList parties = DynamicMultiSelectList.builder()
            .value(dynamicElementList)
            .listItems(dynamicElementList)
            .build();
        data.setPartiesOnCase(parties);

        List<String> partyList = generalOrderService.getParties(FinremCaseDetails.builder().data(data).build());
        assertThat(partyList).as("6 parties available")
            .containsExactlyInAnyOrder(CaseRole.APP_SOLICITOR.getCcdCode(), CaseRole.RESP_SOLICITOR.getCcdCode(),
                CaseRole.INTVR_SOLICITOR_1.getCcdCode(), CaseRole.INTVR_SOLICITOR_2.getCcdCode(), CaseRole.INTVR_SOLICITOR_3.getCcdCode(),
                CaseRole.INTVR_SOLICITOR_4.getCcdCode());
    }

    @Test
    void isOrderSharedWithApplicant() {
        FinremCaseData data = FinremCaseData.builder().build();

        List<DynamicMultiSelectListElement> dynamicElementList = List.of(
            getDynamicElementList(CaseRole.APP_SOLICITOR.getCcdCode()), getDynamicElementList(CaseRole.APP_BARRISTER.getCcdCode()),
            getDynamicElementList(CaseRole.RESP_SOLICITOR.getCcdCode()), getDynamicElementList(CaseRole.RESP_BARRISTER.getCcdCode()),
            getDynamicElementList(CaseRole.INTVR_SOLICITOR_1.getCcdCode()), getDynamicElementList(CaseRole.INTVR_BARRISTER_1.getCcdCode()),
            getDynamicElementList(CaseRole.INTVR_SOLICITOR_2.getCcdCode()), getDynamicElementList(CaseRole.INTVR_BARRISTER_2.getCcdCode()),
            getDynamicElementList(CaseRole.INTVR_SOLICITOR_3.getCcdCode()), getDynamicElementList(CaseRole.INTVR_BARRISTER_3.getCcdCode()),
            getDynamicElementList(CaseRole.INTVR_SOLICITOR_4.getCcdCode()), getDynamicElementList(CaseRole.INTVR_BARRISTER_4.getCcdCode())
        );

        DynamicMultiSelectList parties = DynamicMultiSelectList.builder()
            .value(dynamicElementList)
            .listItems(dynamicElementList)
            .build();

        data.setPartiesOnCase(parties);

        FinremCaseDetails caseDetails = FinremCaseDetails.builder().data(data).build();
        assertTrue(generalOrderService.isOrderSharedWithApplicant(caseDetails));
        assertTrue(generalOrderService.isOrderSharedWithRespondent(caseDetails));
        assertTrue(generalOrderService.isOrderSharedWithIntervener1(caseDetails));
        assertTrue(generalOrderService.isOrderSharedWithIntervener2(caseDetails));
        assertTrue(generalOrderService.isOrderSharedWithIntervener3(caseDetails));
        assertTrue(generalOrderService.isOrderSharedWithIntervener4(caseDetails));
    }

    @Test
    void givenContestedCaseWhenRequestedHearingOrderToProcess_thenReturnList() {
        FinremCaseData data = FinremCaseData.builder().build();

        CaseDocument caseDocument = caseDocument();
        List<DirectionOrderCollection> hearingOrderDocuments = List.of(
            DirectionOrderCollection.builder()
                .value(DirectionOrder.builder().uploadDraftDocument(caseDocument).build())
                .build());

        data.setUploadHearingOrder(hearingOrderDocuments);
        data.getGeneralOrderWrapper().setGeneralOrders(getGeneralOrderCollection());
        List<OrderToShare> selectList = new ArrayList<>();
        data.getSendOrderWrapper().setOrdersToSend(toOrdersToSend(caseDocument, selectList));

        FinremCaseDetails caseDetails = FinremCaseDetails.builder().data(data).build();
        Triple<List<CaseDocument>, List<CaseDocument>, Map<CaseDocument, List<CaseDocument>>> documentList
            = generalOrderService.hearingOrdersToShare(caseDetails, selectList);

        assertThat(documentList.getLeft()).as("One document available to share with other parties").hasSize(1);
        assertThat(documentList.getMiddle()).as("No document available to share with other parties").isEmpty();
        assertThat(documentList.getRight()).as("One document available to share with other parties").isEmpty();
    }

    @Test
    void isSelectedGeneralOrderMatchesReturnTrue() {
        List<OrderToShare> selectList = new ArrayList<>();
        toOrdersToSend(caseDocument(), selectList);
        ContestedGeneralOrder contestedGeneralOrder
            = ContestedGeneralOrder.builder().additionalDocument(caseDocument()).build();
        assertTrue(generalOrderService.isSelectedOrderMatches(selectList, contestedGeneralOrder));
    }

    @Test
    void isSelectedGeneralOrderNotMatchesReturnFalse() {
        List<OrderToShare> selectList = new ArrayList<>();
        toOrdersToSend(caseDocument(), selectList);
        ContestedGeneralOrder contestedGeneralOrder = ContestedGeneralOrder.builder()
            .additionalDocument(caseDocument("http://nxjsjsjksn/documenturl", "test.pdf",
                "http://nxjsjsjksn/documenturl/binary")).build();
        assertFalse(generalOrderService.isSelectedOrderMatches(selectList, contestedGeneralOrder));
    }

    @Test
    void isSelectedGeneralOrderNotMatchesSelectedDocumentIsNullReturnFalse() {
        List<OrderToShare> selectList = new ArrayList<>();
        toOrdersToSend(caseDocument(), selectList);
        assertFalse(generalOrderService.isSelectedOrderMatches(selectList, null));
    }

    @Test
    void isCommunicationEnabledCorrectlyForSelectedParties() {
        FinremCaseData data = FinremCaseData.builder()
            .partiesOnCase(buildDynamicSelectableParties())
            .build();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().data(data).build();

        generalOrderService.setPartiesToReceiveCommunication(caseDetails, List.of(CaseRole.APP_SOLICITOR.getCcdCode(),
            CaseRole.RESP_SOLICITOR.getCcdCode(), CaseRole.INTVR_SOLICITOR_1.getCcdCode(),
            CaseRole.INTVR_SOLICITOR_2.getCcdCode(), CaseRole.INTVR_SOLICITOR_3.getCcdCode(),
            CaseRole.INTVR_SOLICITOR_4.getCcdCode()));

        assertEquals(data.isApplicantCorrespondenceEnabled(),
            generalOrderService.isOrderSharedWithApplicant(caseDetails));
        assertEquals(data.isRespondentCorrespondenceEnabled(),
            generalOrderService.isOrderSharedWithRespondent(caseDetails));
        assertEquals(data.getIntervenerOne().getIntervenerCorrespondenceEnabled(),
            generalOrderService.isOrderSharedWithIntervener1(caseDetails));
        assertEquals(data.getIntervenerTwo().getIntervenerCorrespondenceEnabled(),
            generalOrderService.isOrderSharedWithIntervener2(caseDetails));
        assertEquals(data.getIntervenerThree().getIntervenerCorrespondenceEnabled(),
            generalOrderService.isOrderSharedWithIntervener3(caseDetails));
        assertEquals(data.getIntervenerFour().getIntervenerCorrespondenceEnabled(),
            generalOrderService.isOrderSharedWithIntervener4(caseDetails));
    }

    @Test
    void shouldPopulateFinalisedOrderToOrdersToShareList() {
        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .data(FinremCaseData.builder()
                .draftOrdersWrapper(DraftOrdersWrapper.builder()
                    .finalisedOrdersCollection(List.of(
                        FinalisedOrderCollection.builder()
                            .value(FinalisedOrder.builder()
                                .finalisedDocument(caseDocument("https://fakeurl/documentUrl1", "finalisedOrderOne.pdf", "binaryUrl1"))
                                .build())
                            .build(),
                        FinalisedOrderCollection.builder()
                            .value(FinalisedOrder.builder()
                                .finalisedDocument(caseDocument("https://fakeurl/documentUrl2", "finalisedOrderTwo.pdf", "binaryUrl2"))
                                .build())
                            .build()
                    ))
                    .build())
                .build())
            .build();

        generalOrderService.setOrderList(caseDetails);

        assertThat(caseDetails.getData().getSendOrderWrapper().getOrdersToSend())
            .as("The finalised orders should appear in ordersToShare.")
            .isNotNull()
            .extracting(OrdersToSend::getValue)
            .isEqualTo(List.of(
                OrderToShareCollection.builder()
                    .value(OrderToShare.builder()
                        .documentId("documentUrl1")
                        .documentName("Finalised order - finalisedOrderOne.pdf")
                        .documentToShare(YesOrNo.NO)
                        .hasSupportingDocuments(YesOrNo.NO)
                        .attachmentsToShare(List.of())
                        .build())
                    .build(),
                OrderToShareCollection.builder()
                    .value(OrderToShare.builder()
                        .documentId("documentUrl2")
                        .documentName("Finalised order - finalisedOrderTwo.pdf")
                        .documentToShare(YesOrNo.NO)
                        .hasSupportingDocuments(YesOrNo.NO)
                        .attachmentsToShare(List.of())
                        .build())
                    .build()
            ));

    }

    @Test
    void testGeneralOrderServiceHearingOrdersToShareIfFinalisedOrdersCollectionIsNull() {
        CaseDocument expectedCaseDocument = caseDocument(
            "http://document-management-store:8080/documents/22222222-c524-4614-86e5-c569f82c718d",
            "TEST3.pdf");
        CaseDocument expectedAttachment = caseDocument(
            "http://document-management-store:8080/documents/55555555-c524-4614-86e5-c569f82c718d",
            "ATTACHMENT_6.pdf");

        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .data(FinremCaseData.builder()
                .draftOrdersWrapper(DraftOrdersWrapper.builder()
                    .finalisedOrdersCollection(null)
                    .agreedDraftOrderCollection(List.of(
                        AgreedDraftOrderCollection.builder()
                            .value(AgreedDraftOrder.builder()
                                .attachments(List.of(DocumentCollectionItem.builder().value(expectedAttachment).build()))
                                .pensionSharingAnnex(expectedCaseDocument)
                                .orderStatus(PROCESSED)
                                .build())
                            .build()
                    ))
                    .build())
                .build())
            .build();

        Triple<List<CaseDocument>, List<CaseDocument>, Map<CaseDocument, List<CaseDocument>>> actual = generalOrderService
            .hearingOrdersToShare(caseDetails, List.of(
                OrderToShare.builder().documentToShare(YesOrNo.YES)
                    .documentId("22222222-c524-4614-86e5-c569f82c718d")
                    .includeSupportingDocument(List.of(Yes.YES))
                    .attachmentsToShare(List.of(
                        AttachmentToShareCollection.builder()
                            .value(
                                AttachmentToShare.builder().documentToShare(YesOrNo.YES).documentId("55555555-c524-4614-86e5-c569f82c718d").build())
                            .build()
                    ))
                    .build()
            ));

        assertThat(actual.getLeft()).isEmpty();
        assertThat(actual.getMiddle()).containsExactly(expectedCaseDocument);
        assertThat(actual.getRight()).containsEntry(expectedCaseDocument, List.of(expectedAttachment));
    }

    @Test
    void testGeneralOrderServiceHearingOrdersToShare() {
        CaseDocument expectedCaseDocument1 = caseDocument(
            "http://document-management-store:8080/documents/00000000-c524-4614-86e5-c569f82c718d",
            "TEST1.pdf");
        CaseDocument expectedCaseDocument2 = caseDocument(
            "http://document-management-store:8080/documents/11111111-c524-4614-86e5-c569f82c718d",
            "TEST2.pdf");
        CaseDocument expectedCaseDocument3 = caseDocument(
            "http://document-management-store:8080/documents/22222222-c524-4614-86e5-c569f82c718d",
            "TEST3.pdf");
        CaseDocument expectedCaseDocument4 = caseDocument(
            "http://document-management-store:8080/documents/33333333-c524-4614-86e5-c569f82c718d",
            "TEST4.pdf");
        CaseDocument expectedCaseDocument5 = caseDocument(
            "http://document-management-store:8080/documents/2222222a-c524-4614-86e5-c569f82c718d",
            "TEST5.pdf");
        CaseDocument expectedAttachment1 = caseDocument(
            "http://document-management-store:8080/documents/44444444-c524-4614-86e5-c569f82c718d",
            "ATTACHMENT_5.pdf");
        CaseDocument expectedAttachment2 = caseDocument(
            "http://document-management-store:8080/documents/55555555-c524-4614-86e5-c569f82c718d",
            "ATTACHMENT_6.pdf");

        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .data(FinremCaseData.builder()
                .draftOrdersWrapper(DraftOrdersWrapper.builder()
                    .finalisedOrdersCollection(List.of(
                        FinalisedOrderCollection.builder().value(FinalisedOrder.builder().finalisedDocument(expectedCaseDocument1).build()).build(),
                        FinalisedOrderCollection.builder().value(FinalisedOrder.builder().finalisedDocument(expectedCaseDocument2)
                            .attachments(List.of(DocumentCollectionItem.builder().value(expectedAttachment1).build())).build()).build()
                    ))
                    .agreedDraftOrderCollection(List.of(
                        AgreedDraftOrderCollection.builder()
                            .value(AgreedDraftOrder.builder()
                                .attachments(List.of(DocumentCollectionItem.builder().value(expectedAttachment2).build()))
                                .pensionSharingAnnex(expectedCaseDocument3)
                                .orderStatus(PROCESSED)
                                .build())
                            .build(), // agreedDraftOrder with attachment selected
                        AgreedDraftOrderCollection.builder()
                            .value(AgreedDraftOrder.builder()
                                .pensionSharingAnnex(expectedCaseDocument5)
                                .orderStatus(PROCESSED)
                                .build())
                            .build(), // agreedDraftOrder without attachment

                        AgreedDraftOrderCollection.builder()
                            .value(AgreedDraftOrder.builder()
                                .pensionSharingAnnex(caseDocument(
                                    "http://document-management-store:8080/documents/10000000-c524-4614-86e5-c569f82c718d",
                                    "APPROVED_BY_JUDGE.pdf"))
                                .orderStatus(APPROVED_BY_JUDGE)
                                .build())
                            .build(), // non processed should be ignored.

                        AgreedDraftOrderCollection.builder()
                            .value(AgreedDraftOrder.builder()
                                .pensionSharingAnnex(caseDocument(
                                    "http://document-management-store:8080/documents/20000000-c524-4614-86e5-c569f82c718d",
                                    "REFUSED.pdf"))
                                .orderStatus(REFUSED)
                                .build())
                            .build(), // non processed should be ignored.

                        AgreedDraftOrderCollection.builder()
                            .value(AgreedDraftOrder.builder()
                                .pensionSharingAnnex(caseDocument(
                                    "http://document-management-store:8080/documents/30000000-c524-4614-86e5-c569f82c718d",
                                    "TO_BE_REVIEWED.pdf"))
                                .orderStatus(TO_BE_REVIEWED)
                                .build())
                            .build() // non processed should be ignored.
                    ))

                    .build())
                .uploadHearingOrder(List.of(
                    DirectionOrderCollection.builder().value(DirectionOrder.builder().uploadDraftDocument(expectedCaseDocument4).build()).build()
                ))
                .build())
            .build();

        Triple<List<CaseDocument>, List<CaseDocument>, Map<CaseDocument, List<CaseDocument>>> actual = generalOrderService
            .hearingOrdersToShare(caseDetails, List.of(
                OrderToShare.builder().documentToShare(YesOrNo.YES).documentId("00000000-c524-4614-86e5-c569f82c718d").build(),
                OrderToShare.builder().documentToShare(YesOrNo.YES)
                    .documentId("11111111-c524-4614-86e5-c569f82c718d")
                    .includeSupportingDocument(List.of(Yes.YES))
                    .attachmentsToShare(List.of(
                        AttachmentToShareCollection.builder()
                            .value(
                                AttachmentToShare.builder().documentToShare(YesOrNo.YES).documentId("44444444-c524-4614-86e5-c569f82c718d").build())
                            .build()
                    ))
                    .build(),
                OrderToShare.builder().documentToShare(YesOrNo.YES)
                    .documentId("22222222-c524-4614-86e5-c569f82c718d")
                    .includeSupportingDocument(List.of(Yes.YES))
                    .attachmentsToShare(List.of(
                        AttachmentToShareCollection.builder()
                            .value(
                                AttachmentToShare.builder().documentToShare(YesOrNo.YES).documentId("55555555-c524-4614-86e5-c569f82c718d").build())
                            .build()
                    ))
                    .build(),
                OrderToShare.builder().documentToShare(YesOrNo.YES)
                    .documentId("2222222a-c524-4614-86e5-c569f82c718d")
                    .build(),
                OrderToShare.builder().documentToShare(YesOrNo.YES).documentId("33333333-c524-4614-86e5-c569f82c718d").build(),
                OrderToShare.builder().documentToShare(YesOrNo.NO).documentId("99999999-c524-4614-86e5-c569f82c718d").build()
            ));

        assertThat(actual.getLeft())
            .containsExactly(expectedCaseDocument4);
        assertThat(actual.getMiddle())
            .containsExactly(expectedCaseDocument1, expectedCaseDocument2, expectedCaseDocument3, expectedCaseDocument5);
        assertThat(actual.getRight()).containsExactlyInAnyOrderEntriesOf(
            Map.of(expectedCaseDocument2, List.of(expectedAttachment1),
                expectedCaseDocument3, List.of(expectedAttachment2)));
    }

    @Test
    void shouldPopulateProcessedApprovedDocumentsToOrdersToShareList() {
        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .data(FinremCaseData.builder()
                .draftOrdersWrapper(DraftOrdersWrapper.builder()
                    .agreedDraftOrderCollection(List.of(
                        AgreedDraftOrderCollection.builder()
                            .value(AgreedDraftOrder.builder()
                                .orderStatus(PROCESSED)
                                .draftOrder(caseDocument("https://fakeurl/documentUrl", "processedFileName.pdf", "binaryUrl")).build())
                            .build(),
                        agreedDraftOrderCollection(TO_BE_REVIEWED),
                        agreedDraftOrderCollection(APPROVED_BY_JUDGE),
                        agreedDraftOrderCollection(REFUSED)
                    ))
                    .build())
                .build())
            .build();
        generalOrderService.setOrderList(caseDetails);

        assertThat(caseDetails.getData().getSendOrderWrapper().getOrdersToSend())
            .as("Orders to send should not be null and should contain the processed order.")
            .isNotNull()
            .satisfies(ordersToSend -> assertThat(ordersToSend.getValue())
                .containsExactly(OrderToShareCollection.builder()
                    .value(OrderToShare.builder()
                        .documentId("documentUrl")
                        .documentName("Approved order - processedFileName.pdf")
                        .documentToShare(YesOrNo.NO)
                        .hasSupportingDocuments(YesOrNo.NO)
                        .attachmentsToShare(List.of())
                        .build())
                    .build())
            );
    }

    @Test
    void shouldPopulateProcessedApprovedDocumentsWithAttachmentToOrdersToShareList() {
        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .data(FinremCaseData.builder()
                .draftOrdersWrapper(DraftOrdersWrapper.builder()
                    .agreedDraftOrderCollection(List.of(
                        AgreedDraftOrderCollection.builder()
                            .value(AgreedDraftOrder.builder()
                                .orderStatus(PROCESSED)
                                .attachments(List.of(DocumentCollectionItem.builder()
                                    .value(caseDocument("https://fakeurl/attachment1", "attachment1.pdf", "attachment1"))
                                    .build()))
                                .draftOrder(caseDocument("https://fakeurl/documentUrl", "processedFileName.pdf", "binaryUrl")).build())
                            .build(),
                        agreedDraftOrderCollection(TO_BE_REVIEWED),
                        agreedDraftOrderCollection(APPROVED_BY_JUDGE),
                        agreedDraftOrderCollection(REFUSED)
                    ))
                    .build())
                .build())
            .build();
        generalOrderService.setOrderList(caseDetails);

        assertThat(caseDetails.getData().getSendOrderWrapper().getOrdersToSend())
            .as("Orders to send should not be null and should contain the processed order.")
            .isNotNull()
            .satisfies(ordersToSend -> assertThat(ordersToSend.getValue())
                .containsExactly(OrderToShareCollection.builder()
                    .value(OrderToShare.builder()
                        .documentId("documentUrl")
                        .documentName("Approved order - processedFileName.pdf")
                        .documentToShare(YesOrNo.NO)
                        .hasSupportingDocuments(YesOrNo.YES)
                        .attachmentsToShare(List.of(
                            AttachmentToShareCollection.builder()
                                .value(AttachmentToShare.builder()
                                    .attachment(caseDocument("https://fakeurl/attachment1", "attachment1.pdf", "attachment1"))
                                    .attachmentName("attachment1.pdf")
                                    .documentId("attachment1")
                                    .documentToShare(YesOrNo.NO)
                                    .build())
                                .build()
                        ))
                        .build())
                    .build())
            );
    }

    private DynamicMultiSelectListElement getDynamicElementList(String role) {
        return DynamicMultiSelectListElement.builder()
            .code(role)
            .label(role)
            .build();
    }

    private Map<String, Object> convertToMap(Object object) {
        return new ObjectMapper().convertValue(object, new TypeReference<>() {
        });
    }

    private CaseDetails consentedCaseDetails() throws Exception {
        try (InputStream resourceAsStream = getClass().getResourceAsStream("/fixtures/general-order-consented.json")) {
            return objectMapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
        }
    }

    private CaseDetails contestedCaseDetails() throws Exception {
        try (InputStream resourceAsStream = getClass().getResourceAsStream("/fixtures/general-order-contested.json")) {
            return objectMapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
        }
    }

    private FinremCaseDetails contestedFinremCaseDetails() throws Exception {
        return readRequestJson("/fixtures/general-order-contested.json").getCaseDetails();
    }

    private FinremCaseDetails consentedInContestedFinremCaseDetails() throws Exception {
        return readRequestJson("/fixtures/general-order-consented-in-contested.json").getCaseDetails();
    }

    private FinremCaseDetails consentedFinremCaseDetails() throws Exception {
        return readRequestJson("/fixtures/general-order-consented.json").getCaseDetails();
    }

    private FinremCallbackRequest readRequestJson(String filename) throws IOException {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(filename)) {
            return objectMapper.readValue(resourceAsStream, FinremCallbackRequest.class);
        }
    }

    private static void doCaseDocumentAssert(CaseDocument result) {
        assertEquals(FILE_NAME, result.getDocumentFilename());
        assertEquals(DOC_URL, result.getDocumentUrl());
        assertEquals(BINARY_URL, result.getDocumentBinaryUrl());
    }

    private CaseDetails consentedInContestedCaseDetails() throws Exception {
        try (InputStream resourceAsStream = getClass().getResourceAsStream("/fixtures/general-order-consented-in-contested.json")) {
            return objectMapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
        }
    }

    private void verifyAdditionalFieldsConsented() {
        verify(genericDocumentService, times(1))
            .generateDocument(eq(AUTH_TOKEN), caseDetailsArgumentCaptor.capture(),
                eq("FL-FRM-GOR-ENG-00484.docx"),
                argThat(fileName -> fileName.matches("generalOrder-\\d{8}-\\d{6}\\.pdf")));

        Map<String, Object> data = caseDetailsArgumentCaptor.getValue().getData();
        assertEquals("DD12D12345", data.get("DivorceCaseNumber"));
        assertEquals("Consented Applicant Name", data.get("ApplicantName"));
        assertEquals("Consented Respondent Name", data.get("RespondentName"));
        assertEquals("SITTING in private", data.get("GeneralOrderCourt"));
        assertEquals("His Honour Judge Consented", data.get("GeneralOrderJudgeDetails"));
        assertEquals("Consented Recitals", data.get("GeneralOrderRecitals"));
        assertEquals("2020-01-01", data.get("GeneralOrderDate"));
        assertEquals("Test is dummy text for consented", data.get("GeneralOrderBodyText"));
        assertEquals("Sitting in the Family Court", data.get("GeneralOrderHeaderOne"));
    }

    private void verifyAdditionalFieldsContested() {
        verify(genericDocumentService, times(1))
            .generateDocument(eq(AUTH_TOKEN), caseDetailsArgumentCaptor.capture(),
                eq("FL-FRM-GOR-ENG-00484.docx"),
                argThat(fileName ->
                    fileName.matches("generalOrder-\\d{8}-\\d{6}\\.pdf"))
            );

        Map<String, Object> data = caseDetailsArgumentCaptor.getValue().getData();
        assertEquals("DD98D76543", data.get("DivorceCaseNumber"));
        assertEquals("Contested Applicant Name", data.get("ApplicantName"));
        assertEquals("Contested Respondent Name", data.get("RespondentName"));
        assertEquals("Nottingham County Court and Family Court", data.get("GeneralOrderCourt"));
        assertEquals("Her Honour Judge Contested", data.get("GeneralOrderJudgeDetails"));
        assertEquals("Contested Recitals", data.get("GeneralOrderRecitals"));
        assertEquals("2020-06-01", data.get("GeneralOrderDate"));
        assertEquals("Test is dummy text for contested", data.get("GeneralOrderBodyText"));
        assertEquals("In the Family Court", data.get("GeneralOrderHeaderOne"));
        assertEquals("sitting in the", data.get("GeneralOrderHeaderTwo"));
        assertEquals("SITTING AT the Family Court at the ", data.get("GeneralOrderCourtSitting"));
        Map<String, Object> court = convertToMap(data.get("courtDetails"));

        assertEquals("Nottingham County Court And Family Court", court.get("courtName"));
        assertEquals("60 Canal Street, Nottingham NG1 7EJ", court.get("courtAddress"));
        assertEquals("0300 123 5577", court.get("phoneNumber"));
        assertEquals("FRCNottingham@justice.gov.uk", court.get("email"));
    }

    private String getDocumentId(CaseDocument caseDocument) {
        String documentUrl = caseDocument.getDocumentUrl();
        return caseDocument.getDocumentUrl().substring(documentUrl.lastIndexOf("/") + 1);
    }

    private DynamicMultiSelectList buildDynamicSelectableParties() {
        return DynamicMultiSelectList.builder()
            .value(List.of(DynamicMultiSelectListElement.builder()
                .code(CaseRole.APP_SOLICITOR.getCcdCode())
                .label(CaseRole.APP_SOLICITOR.getCcdCode())
                .build(), DynamicMultiSelectListElement.builder()
                .code(CaseRole.RESP_SOLICITOR.getCcdCode())
                .label(CaseRole.RESP_SOLICITOR.getCcdCode())
                .build(), DynamicMultiSelectListElement.builder()
                .code(CaseRole.INTVR_SOLICITOR_1.getCcdCode())
                .label(CaseRole.INTVR_SOLICITOR_1.getCcdCode())
                .build(), DynamicMultiSelectListElement.builder()
                .code(CaseRole.INTVR_SOLICITOR_2.getCcdCode())
                .label(CaseRole.INTVR_SOLICITOR_2.getCcdCode())
                .build(), DynamicMultiSelectListElement.builder()
                .code(CaseRole.INTVR_SOLICITOR_3.getCcdCode())
                .label(CaseRole.INTVR_SOLICITOR_3.getCcdCode())
                .build(), DynamicMultiSelectListElement.builder()
                .code(CaseRole.INTVR_SOLICITOR_4.getCcdCode())
                .label(CaseRole.INTVR_SOLICITOR_4.getCcdCode())
                .build()))
            .listItems(List.of(DynamicMultiSelectListElement.builder()
                .code(CaseRole.APP_SOLICITOR.getCcdCode())
                .label(CaseRole.APP_SOLICITOR.getCcdCode())
                .build(), DynamicMultiSelectListElement.builder()
                .code(CaseRole.RESP_SOLICITOR.getCcdCode())
                .label(CaseRole.RESP_SOLICITOR.getCcdCode())
                .build(), DynamicMultiSelectListElement.builder()
                .code(CaseRole.INTVR_SOLICITOR_1.getCcdCode())
                .label(CaseRole.INTVR_SOLICITOR_1.getCcdCode())
                .build(), DynamicMultiSelectListElement.builder()
                .code(CaseRole.INTVR_SOLICITOR_2.getCcdCode())
                .label(CaseRole.INTVR_SOLICITOR_2.getCcdCode())
                .build(), DynamicMultiSelectListElement.builder()
                .code(CaseRole.INTVR_SOLICITOR_3.getCcdCode())
                .label(CaseRole.INTVR_SOLICITOR_3.getCcdCode())
                .build(), DynamicMultiSelectListElement.builder()
                .code(CaseRole.INTVR_SOLICITOR_4.getCcdCode())
                .label(CaseRole.INTVR_SOLICITOR_4.getCcdCode())
                .build()))
            .build();
    }

    private List<ContestedGeneralOrderCollection> getGeneralOrderCollection() {
        ContestedGeneralOrder generalOrder = ContestedGeneralOrder
            .builder()
            .dateOfOrder(LocalDate.of(2002, 2, 5))
            .judge("Moj")
            .generalOrderText("general order")
            .additionalDocument(caseDocument())
            .build();

        ContestedGeneralOrderCollection collection = ContestedGeneralOrderCollection.builder().value(generalOrder).build();
        List<ContestedGeneralOrderCollection> collections = new ArrayList<>();
        collections.add(collection);
        return collections;
    }

    private List<ContestedGeneralOrderCollection> getGeneralOrderCollectionNoAdditionalDocument() {
        ContestedGeneralOrder generalOrder = ContestedGeneralOrder
            .builder()
            .dateOfOrder(LocalDate.of(2002, 2, 5))
            .judge("Moj")
            .generalOrderText("general order")
            .build();

        ContestedGeneralOrderCollection collection = ContestedGeneralOrderCollection.builder().value(generalOrder).build();
        List<ContestedGeneralOrderCollection> collections = new ArrayList<>();
        collections.add(collection);
        return collections;
    }

    private static AgreedDraftOrderCollection agreedDraftOrderCollection(OrderStatus orderStatus) {
        return AgreedDraftOrderCollection.builder()
            .value(AgreedDraftOrder.builder().orderStatus(orderStatus).draftOrder(caseDocument()).build())
            .build();
    }

    private OrdersToSend toOrdersToSend(CaseDocument caseDocument) {
        return toOrdersToSend(caseDocument, null);
    }

    private OrdersToSend toOrdersToSend(CaseDocument caseDocument, List<OrderToShare> selectedOrderToShare) {
        OrderToShare selected = null;
        try {
            return OrdersToSend.builder()
                .value(List.of(
                    OrderToShareCollection.builder()
                        .value(selected = OrderToShare.builder()
                            .documentToShare(YesOrNo.YES)
                            .documentId(getDocumentId(caseDocument))
                            .documentName(caseDocument.getDocumentFilename())
                            .build())
                        .build()
                ))
                .build();
        } finally {
            if (selected != null && selectedOrderToShare != null) {
                selectedOrderToShare.add(selected);
            }
        }
    }
}
