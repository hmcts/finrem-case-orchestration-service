package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedGeneralOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedGeneralOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralOrderConsentedData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_ADDRESS_TO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_COLLECTION_CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_COLLECTION_CONSENTED_IN_CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_COLLECTION_CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_LATEST_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_PREVIEW_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

public class GeneralOrderServiceTest extends BaseServiceTest {

    @Autowired
    private GeneralOrderService generalOrderService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private DocumentHelper documentHelper;
    @Autowired
    private DocumentConfiguration documentConfiguration;
    @MockBean
    private FeatureToggleService featureToggleService;
    @MockBean
    private GenericDocumentService genericDocumentService;

    @Captor
    private ArgumentCaptor<CaseDetails> caseDetailsArgumentCaptor;

    private UUID uuid;

    @Before
    public void setUp() {
        uuid = UUID.fromString("2ec43a65-3614-4b53-ab89-18252855f399");
        when(genericDocumentService.generateDocument(any(), any(), any(), any())).thenReturn(caseDocument());
    }

    @Test
    public void generateGeneralOrderConsented() throws Exception {
        Map<String, Object> documentMap = generalOrderService.createGeneralOrder(AUTH_TOKEN, consentedCaseDetails());

        CaseDocument result = (CaseDocument) documentMap.get(GENERAL_ORDER_PREVIEW_DOCUMENT);
        doCaseDocumentAssert(result);

        verifyAdditionalFieldsConsented();
    }

    @Test
    public void generateGeneralOrderContested() throws Exception {
        Map<String, Object> documentMap = generalOrderService.createGeneralOrder(AUTH_TOKEN, contestedCaseDetails());

        CaseDocument result = (CaseDocument) documentMap.get(GENERAL_ORDER_PREVIEW_DOCUMENT);
        doCaseDocumentAssert(result);

        verifyAdditionalFieldsContested();
    }

    @Test
    public void submitContestedGeneralOrder() throws Exception {
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
        Map<String, Object> documentMap = generalOrderService.populateGeneralOrderCollection(contestedCaseDetails());

        List<ContestedGeneralOrderCollection> generalOrders = convertToList(documentMap.get(GENERAL_ORDER_COLLECTION_CONTESTED));
        assertThat(generalOrders, hasSize(2));
        assertThat(generalOrders.get(0).getValue().getAdditionalDocument().getDocumentUrl(), is("http://dm-store/lhjbyuivu87y989hijbb"));
        assertThat(generalOrders.get(0).getValue().getAdditionalDocument().getDocumentFilename(),
            is("generalOrder.pdf"));
        assertThat(generalOrders.get(0).getValue().getAdditionalDocument().getDocumentBinaryUrl(),
            is("http://dm-store/lhjbyuivu87y989hijbb/binary"));

        assertThat(generalOrders.get(1).getValue().getAdditionalDocument().getDocumentUrl(),
            is("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d"));
        assertThat(generalOrders.get(1).getValue().getAdditionalDocument().getDocumentFilename(),
            is("WhatsApp Image 2018-07-24 at 3.05.39 PM.jpeg"));
        assertThat(generalOrders.get(1).getValue().getAdditionalDocument().getDocumentBinaryUrl(),
            is("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d/binary"));
        assertThat(generalOrders.get(1).getValue().getGeneralOrderAddressTo(), is("Applicant"));

        CaseDocument latestGeneralOrder = getCaseDocument(documentMap.get(GENERAL_ORDER_LATEST_DOCUMENT));
        assertThat(latestGeneralOrder.getDocumentUrl(),
            is("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d"));
        assertThat(latestGeneralOrder.getDocumentFilename(),
            is("WhatsApp Image 2018-07-24 at 3.05.39 PM.jpeg"));
        assertThat(latestGeneralOrder.getDocumentBinaryUrl(),
            is("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d/binary"));
        generalOrders.forEach(order -> assertThat(order.getValue().getAdditionalDocument().getCategoryId(),
            is(DocumentCategory.APPROVED_ORDERS_CASE.getDocumentCategoryId())));
    }

    @Test
    public void generateGeneralOrderConsentedInContested() throws Exception {
        Map<String, Object> documentMap = generalOrderService.createGeneralOrder(AUTH_TOKEN, consentedInContestedCaseDetails());

        CaseDocument result = (CaseDocument) documentMap.get(GENERAL_ORDER_PREVIEW_DOCUMENT);
        doCaseDocumentAssert(result);

        verifyAdditionalFieldsContested();
    }

    @Test
    public void submitConsentedInContestedGeneralOrder() throws Exception {
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
        Map<String, Object> documentMap = generalOrderService.populateGeneralOrderCollection(consentedInContestedCaseDetails());

        List<ContestedGeneralOrderCollection> generalOrders = convertToList(documentMap.get(GENERAL_ORDER_COLLECTION_CONSENTED_IN_CONTESTED));
        assertThat(generalOrders, hasSize(2));

        assertThat(generalOrders.get(0).getValue().getAdditionalDocument().getDocumentUrl(), is("http://dm-store/lhjbyuivu87y989hijbb"));
        assertThat(generalOrders.get(0).getValue().getAdditionalDocument().getDocumentFilename(),
            is("app_docs.pdf"));
        assertThat(generalOrders.get(0).getValue().getAdditionalDocument().getDocumentBinaryUrl(),
            is("http://dm-store/lhjbyuivu87y989hijbb/binary"));


        assertThat(generalOrders.get(1).getValue().getAdditionalDocument().getDocumentUrl(),
            is("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d"));
        assertThat(generalOrders.get(1).getValue().getAdditionalDocument().getDocumentFilename(),
            is("WhatsApp Image 2018-07-24 at 3.05.39 PM.jpeg"));
        assertThat(generalOrders.get(1).getValue().getAdditionalDocument().getDocumentBinaryUrl(),
            is("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d/binary"));
        assertThat(generalOrders.get(1).getValue().getGeneralOrderAddressTo(), is("Applicant"));

        CaseDocument latestGeneralOrder = getCaseDocument(documentMap.get(GENERAL_ORDER_LATEST_DOCUMENT));
        assertThat(latestGeneralOrder.getDocumentUrl(),
            is("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d"));
        assertThat(latestGeneralOrder.getDocumentFilename(),
            is("WhatsApp Image 2018-07-24 at 3.05.39 PM.jpeg"));
        assertThat(latestGeneralOrder.getDocumentBinaryUrl(),
            is("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d/binary"));
        generalOrders.forEach(order -> assertThat(order.getValue().getAdditionalDocument().getCategoryId(),
            is(DocumentCategory.APPROVED_ORDERS_CONSENT_APPLICATION.getDocumentCategoryId())));
    }

    @Test
    public void submitConsentedGeneralOrder() throws Exception {
        Map<String, Object> documentMap = generalOrderService.populateGeneralOrderCollection(consentedCaseDetails());
        List<GeneralOrderConsentedData> generalOrders = convertToConsentList(documentMap.get(GENERAL_ORDER_COLLECTION_CONSENTED));
        assertThat(generalOrders, hasSize(2));
        assertThat(generalOrders.get(0).getId(), is("1234"));
        assertThat(generalOrders.get(0).getGeneralOrder().getGeneralOrder().getDocumentUrl(),
            is("http://dm-store/lhjbyuivu87y989hijbb"));
        assertThat(generalOrders.get(0).getGeneralOrder().getGeneralOrder().getDocumentFilename(),
            is("app_docs.pdf"));
        assertThat(generalOrders.get(0).getGeneralOrder().getGeneralOrder().getDocumentBinaryUrl(),
            is("http://dm-store/lhjbyuivu87y989hijbb/binary"));

        assertThat(generalOrders.get(1).getId(), notNullValue());
        assertThat(generalOrders.get(1).getGeneralOrder().getGeneralOrder().getDocumentUrl(),
            is("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d"));
        assertThat(generalOrders.get(1).getGeneralOrder().getGeneralOrder().getDocumentFilename(),
            is("WhatsApp Image 2018-07-24 at 3.05.39 PM.jpeg"));
        assertThat(generalOrders.get(1).getGeneralOrder().getGeneralOrder().getDocumentBinaryUrl(),
            is("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d/binary"));
        assertThat(generalOrders.get(1).getGeneralOrder().getAddressTo(), is("Applicant"));

        CaseDocument latestGeneralOrder = (CaseDocument) documentMap.get(GENERAL_ORDER_LATEST_DOCUMENT);
        assertThat(latestGeneralOrder.getDocumentUrl(),
            is("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d"));
        assertThat(latestGeneralOrder.getDocumentFilename(),
            is("WhatsApp Image 2018-07-24 at 3.05.39 PM.jpeg"));
        assertThat(latestGeneralOrder.getDocumentBinaryUrl(),
            is("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d/binary"));
    }

    @Test
    public void addressToFormattedCorrectly() {
        Map<String, String> parties = Map.of("applicant", "Applicant", "applicantSolicitor",
            "Applicant Solicitor", "respondentSolicitor", "Respondent Solicitor", "invalid", "");
        parties.forEach((key, value) -> {
            try {
                CaseDetails details = consentedCaseDetails();
                details.getData().put(GENERAL_ORDER_ADDRESS_TO, key);
                Map<String, Object> documentMap = generalOrderService.populateGeneralOrderCollection(details);
                List<GeneralOrderConsentedData> generalOrders = convertToConsentList(documentMap.get(GENERAL_ORDER_COLLECTION_CONSENTED));
                assertThat(generalOrders.get(1).getGeneralOrder().getAddressTo(), is(value));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    public void getsCorrectGeneralOrdersForPrintingConsented() throws Exception {
        CaseDetails details = consentedCaseDetails();
        CaseDocument caseDocument = documentHelper.convertToCaseDocument(details.getData().get(GENERAL_ORDER_LATEST_DOCUMENT));
        CaseDocument pdfDoc = buildCaseDocument("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d/",
            "http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d/binary",
            "test.pdf");
        when(genericDocumentService.convertDocumentIfNotPdfAlready(caseDocument, AUTH_TOKEN, caseId)).thenReturn(pdfDoc);
        BulkPrintDocument latestGeneralOrder = generalOrderService.getLatestGeneralOrderAsBulkPrintDocument(details.getData(), AUTH_TOKEN, caseId);
        assertThat(latestGeneralOrder.getBinaryFileUrl(),
            is("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d/binary"));
    }

    @Test
    public void getsZeroGeneralOrdersForPrintingWhenNoneConsented() throws Exception {
        CaseDetails details = consentedCaseDetails();
        details.getData().put(GENERAL_ORDER_LATEST_DOCUMENT, null);
        BulkPrintDocument latestGeneralOrder = generalOrderService.getLatestGeneralOrderAsBulkPrintDocument(details.getData(), AUTH_TOKEN, caseId);
        assertNull(latestGeneralOrder);
    }

    @Test
    public void whenRequestedOrderList_and_notshared_before_returnList() throws Exception {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();

        List<DirectionOrderCollection> hearingOrderDocuments = List.of(
            DirectionOrderCollection.builder()
                .id(uuid.toString())
                .value(DirectionOrder.builder().uploadDraftDocument(caseDocument()).build())
                .build());

        data.setUploadHearingOrder(hearingOrderDocuments);
        generalOrderService.setOrderList(caseDetails);

        assertEquals("One document available to share with other parties", 1, data.getOrdersToShare().getListItems().size());
    }


    @Test
    public void whenRequestedGeneralOrderListPresentButGeneralOrderIsNotThere_and_shared_before_returnList() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();

        data.getGeneralOrderWrapper().setGeneralOrders(getGeneralOrderCollectionNoAdditionalDocument());

        List<DynamicMultiSelectListElement> dynamicElementList = List.of(getDynamicElementList(
            caseDocument("url", "moj.pdf", "binaryurl")));

        DynamicMultiSelectList selectList = DynamicMultiSelectList.builder()
            .value(dynamicElementList)
            .listItems(dynamicElementList)
            .build();

        data.setOrdersToShare(selectList);

        generalOrderService.setOrderList(caseDetails);

        assertEquals("Not document available to share with other parties", 0,
            data.getOrdersToShare().getListItems().size());
    }

    @Test
    public void whenRequestedGeneralOrderListIsEmptyPresentButGeneralOrderIsNotThere_and_shared_before_returnList() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();

        ContestedGeneralOrderCollection collection = ContestedGeneralOrderCollection.builder().value(null).build();
        List<ContestedGeneralOrderCollection> collections = new ArrayList<>();
        collections.add(collection);

        data.getGeneralOrderWrapper().setGeneralOrders(collections);

        List<DynamicMultiSelectListElement> dynamicElementList = List.of(getDynamicElementList(
            caseDocument("url", "moj.pdf", "binaryurl")));

        DynamicMultiSelectList selectList = DynamicMultiSelectList.builder()
            .value(dynamicElementList)
            .listItems(dynamicElementList)
            .build();

        data.setOrdersToShare(selectList);

        generalOrderService.setOrderList(caseDetails);

        assertEquals("Not document available to share with other parties", 0,
            data.getOrdersToShare().getListItems().size());
    }

    @Test
    public void whenRequestedOrderList_and_shared_before_returnList() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();

        List<DirectionOrderCollection> hearingOrderDocuments = List.of(
            DirectionOrderCollection.builder()
                .id(uuid.toString())
                .value(DirectionOrder.builder().uploadDraftDocument(caseDocument()).build())
                .build());

        data.setUploadHearingOrder(hearingOrderDocuments);
        data.getGeneralOrderWrapper().setGeneralOrders(getGeneralOrderCollection());

        List<DynamicMultiSelectListElement> dynamicElementList = List.of(getDynamicElementList(
            caseDocument("url", "moj.pdf", "binaryurl")));

        DynamicMultiSelectList selectList = DynamicMultiSelectList.builder()
            .value(dynamicElementList)
            .listItems(dynamicElementList)
            .build();

        data.setOrdersToShare(selectList);

        generalOrderService.setOrderList(caseDetails);

        assertEquals("One document available to share with other parties", 2,
            data.getOrdersToShare().getListItems().size());
    }

    @Test
    public void givenContestedCase_whenRequestedParies_thenReturnParties() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();

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

        List<String> partyList = generalOrderService.getParties(caseDetails);
        assertEquals("6 parties availablle ", 6, partyList.size());
    }


    @Test
    public void isOrderSharedWithApplicant() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();

        List<DynamicMultiSelectListElement> dynamicElementList =
            List.of(getDynamicElementList(CaseRole.APP_SOLICITOR.getCcdCode()), getDynamicElementList(CaseRole.APP_BARRISTER.getCcdCode()),
                getDynamicElementList(CaseRole.RESP_SOLICITOR.getCcdCode()), getDynamicElementList(CaseRole.RESP_BARRISTER.getCcdCode()),
                getDynamicElementList(CaseRole.INTVR_SOLICITOR_1.getCcdCode()), getDynamicElementList(CaseRole.INTVR_BARRISTER_1.getCcdCode()),
                getDynamicElementList(CaseRole.INTVR_SOLICITOR_2.getCcdCode()), getDynamicElementList(CaseRole.INTVR_BARRISTER_2.getCcdCode()),
                getDynamicElementList(CaseRole.INTVR_SOLICITOR_3.getCcdCode()), getDynamicElementList(CaseRole.INTVR_BARRISTER_3.getCcdCode()),
                getDynamicElementList(CaseRole.INTVR_SOLICITOR_4.getCcdCode()), getDynamicElementList(CaseRole.INTVR_BARRISTER_4.getCcdCode()));

        DynamicMultiSelectList parties = DynamicMultiSelectList.builder()
            .value(dynamicElementList)
            .listItems(dynamicElementList)
            .build();

        data.setPartiesOnCase(parties);

        assertTrue(generalOrderService.isOrderSharedWithApplicant(caseDetails));
        assertTrue(generalOrderService.isOrderSharedWithRespondent(caseDetails));
        assertTrue(generalOrderService.isOrderSharedWithIntervener1(caseDetails));
        assertTrue(generalOrderService.isOrderSharedWithIntervener2(caseDetails));
        assertTrue(generalOrderService.isOrderSharedWithIntervener3(caseDetails));
        assertTrue(generalOrderService.isOrderSharedWithIntervener4(caseDetails));

    }

    @Test
    public void givenContestedCaseWhenRequestedHearingOrderToProcess_thenReturnList() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();

        List<DirectionOrderCollection> hearingOrderDocuments = List.of(
            DirectionOrderCollection.builder()
                .id(uuid.toString())
                .value(DirectionOrder.builder().uploadDraftDocument(caseDocument()).build())
                .build());

        data.setUploadHearingOrder(hearingOrderDocuments);
        data.getGeneralOrderWrapper().setGeneralOrders(getGeneralOrderCollection());

        List<DynamicMultiSelectListElement> dynamicElementList = List.of(getDynamicElementList(
            caseDocument()));

        DynamicMultiSelectList selectList = DynamicMultiSelectList.builder()
            .value(dynamicElementList)
            .listItems(dynamicElementList)
            .build();

        data.setOrdersToShare(selectList);

        List<CaseDocument> documentList = generalOrderService.hearingOrdersToShare(caseDetails, selectList);

        assertEquals("One document available to share with other parties", 1, documentList.size());
    }


    private DynamicMultiSelectListElement getDynamicElementList(CaseDocument caseDocument) {
        return DynamicMultiSelectListElement.builder()
            .code(getDocumentId(caseDocument))
            .label(caseDocument.getDocumentFilename())
            .build();
    }

    private DynamicMultiSelectListElement getDynamicElementList(String role) {
        return DynamicMultiSelectListElement.builder()
            .code(role)
            .label(role)
            .build();
    }

    private FinremCallbackRequest buildCallbackRequest() {
        return FinremCallbackRequest
            .builder()
            .eventType(EventType.SEND_ORDER)
            .caseDetailsBefore(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(new FinremCaseData()).build())
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(new FinremCaseData()).build())
            .build();
    }

    public List<ContestedGeneralOrderCollection> convertToList(Object object) {
        return new ObjectMapper().registerModule(new JavaTimeModule()).convertValue(object, new TypeReference<>() {
        });
    }

    public List<GeneralOrderConsentedData> convertToConsentList(Object object) {
        return new ObjectMapper().registerModule(new JavaTimeModule()).convertValue(object, new TypeReference<>() {
        });
    }

    public Map<String, Object> convertToMap(Object object) {
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

    private static void doCaseDocumentAssert(CaseDocument result) {
        assertThat(result.getDocumentFilename(), is(FILE_NAME));
        assertThat(result.getDocumentUrl(), is(DOC_URL));
        assertThat(result.getDocumentBinaryUrl(), is(BINARY_URL));
    }

    private CaseDetails consentedInContestedCaseDetails() throws Exception {
        try (InputStream resourceAsStream = getClass().getResourceAsStream("/fixtures/general-order-consented-in-contested.json")) {
            return objectMapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
        }
    }

    void verifyAdditionalFieldsConsented() {
        verify(genericDocumentService, times(1))
            .generateDocument(eq(AUTH_TOKEN), caseDetailsArgumentCaptor.capture(),
                eq(documentConfiguration.getGeneralOrderTemplate(CaseDetails.builder().build())),
                eq(getGeneralOrderFileNameWithDateTimeStamp()));

        Map<String, Object> data = caseDetailsArgumentCaptor.getValue().getData();
        assertThat(data.get("DivorceCaseNumber"), is("DD12D12345"));
        assertThat(data.get("ApplicantName"), is("Consented Applicant Name"));
        assertThat(data.get("RespondentName"), is("Consented Respondent Name"));
        assertThat(data.get("GeneralOrderCourt"), is("SITTING in private"));
        assertThat(data.get("GeneralOrderJudgeDetails"), is("His Honour Judge Consented"));
        assertThat(data.get("GeneralOrderRecitals"), is("Consented Recitals"));
        assertThat(data.get("GeneralOrderDate"), is("2020-01-01"));
        assertThat(data.get("GeneralOrderBodyText"), is("Test is dummy text for consented"));
        assertThat(data.get("GeneralOrderHeaderOne"), is("Sitting in the Family Court"));
    }

    private String getGeneralOrderFileNameWithDateTimeStamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
        LocalDateTime now = LocalDateTime.now();
        String dateTimeString = now.format(formatter);
        String str = documentConfiguration.getGeneralOrderFileName();
        return new StringBuilder(str).insert(str.length() - 4, "-" + dateTimeString).toString();
    }

    void verifyAdditionalFieldsContested() {
        verify(genericDocumentService, times(1))
            .generateDocument(eq(AUTH_TOKEN), caseDetailsArgumentCaptor.capture(),
                eq(documentConfiguration.getGeneralOrderTemplate(CaseDetails.builder().build())),
                eq(getGeneralOrderFileNameWithDateTimeStamp()));

        Map<String, Object> data = caseDetailsArgumentCaptor.getValue().getData();
        assertThat(data.get("DivorceCaseNumber"), is("DD98D76543"));
        assertThat(data.get("ApplicantName"), is("Contested Applicant Name"));
        assertThat(data.get("RespondentName"), is("Contested Respondent Name"));
        assertThat(data.get("GeneralOrderCourt"), is("Nottingham County Court and Family Court"));
        assertThat(data.get("GeneralOrderJudgeDetails"), is("Her Honour Judge Contested"));
        assertThat(data.get("GeneralOrderRecitals"), is("Contested Recitals"));
        assertThat(data.get("GeneralOrderDate"), is("2020-06-01"));
        assertThat(data.get("GeneralOrderBodyText"), is("Test is dummy text for contested"));
        assertThat(data.get("GeneralOrderHeaderOne"), is("In the Family Court"));
        assertThat(data.get("GeneralOrderHeaderTwo"), is("sitting in the"));
        assertThat(data.get("GeneralOrderCourtSitting"), is("SITTING AT the Family Court at the "));
        Map<String, Object> court = convertToMap(data.get("courtDetails"));

        assertThat(court.get("courtName"), is("Nottingham County Court And Family Court"));
        assertThat(court.get("courtAddress"), is("60 Canal Street, Nottingham NG1 7EJ"));
        assertThat(court.get("phoneNumber"), is("0115 910 3504"));
        assertThat(court.get("email"), is("FRCNottingham@justice.gov.uk"));
    }

    @Test
    public void isSelectedGeneralOrderMatchesReturnTrue() {
        DynamicMultiSelectListElement listElement
            = DynamicMultiSelectListElement.builder().code(getDocumentId(caseDocument()))
            .label(caseDocument().getDocumentFilename()).build();
        List<DynamicMultiSelectListElement> dynamicElementList = List.of(listElement);

        DynamicMultiSelectList selectList = DynamicMultiSelectList.builder()
            .value(dynamicElementList)
            .listItems(dynamicElementList)
            .build();
        ContestedGeneralOrder contestedGeneralOrder
            = ContestedGeneralOrder.builder().additionalDocument(caseDocument()).build();
        assertTrue(generalOrderService.isSelectedOrderMatches(selectList, contestedGeneralOrder));
    }

    private String getDocumentId(CaseDocument caseDocument) {
        String documentUrl = caseDocument.getDocumentUrl();
        return documentUrl.substring(documentUrl.lastIndexOf("/") + 1);
    }


    @Test
    public void isSelectedGeneralOrderNotMatchesReturnFalse() {
        List<DynamicMultiSelectListElement> dynamicElementList = List.of(getDynamicElementList(caseDocument()));

        DynamicMultiSelectList selectList = DynamicMultiSelectList.builder()
            .value(dynamicElementList)
            .listItems(dynamicElementList)
            .build();
        ContestedGeneralOrder contestedGeneralOrder
            = ContestedGeneralOrder.builder()
            .additionalDocument(caseDocument("http://nxjsjsjksn/documenturl", "test.pdf",
                "http://nxjsjsjksn/documenturl/binary")).build();
        assertFalse(generalOrderService.isSelectedOrderMatches(selectList, contestedGeneralOrder));
    }

    @Test
    public void isSelectedGeneralOrderNotMatchesSelectedDocumentIsNullReturnFalse() {
        List<DynamicMultiSelectListElement> dynamicElementList = List.of(getDynamicElementList(caseDocument()));

        DynamicMultiSelectList selectList = DynamicMultiSelectList.builder()
            .value(dynamicElementList)
            .listItems(dynamicElementList)
            .build();
        assertFalse(generalOrderService.isSelectedOrderMatches(selectList, null));
    }

    @Test
    public void isCommunicationEnabledCorrectlyForSelectedParties() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();
        data.setPartiesOnCase(buildDynamicSelectableParties());
        generalOrderService.setPartiesToReceiveCommunication(caseDetails, List.of(CaseRole.APP_SOLICITOR.getCcdCode(),
            CaseRole.RESP_SOLICITOR.getCcdCode(), CaseRole.INTVR_SOLICITOR_1.getCcdCode(),
            CaseRole.INTVR_SOLICITOR_2.getCcdCode(), CaseRole.INTVR_SOLICITOR_3.getCcdCode(),
            CaseRole.INTVR_SOLICITOR_4.getCcdCode()));
        assertThat(data.isApplicantCorrespondenceEnabled(),
            equalTo(generalOrderService.isOrderSharedWithApplicant(caseDetails)));
        assertThat(data.isRespondentCorrespondenceEnabled(),
            equalTo(generalOrderService.isOrderSharedWithRespondent(caseDetails)));
        assertThat(data.getIntervenerOneWrapper().getIntervenerCorrespondenceEnabled(),
            equalTo(generalOrderService.isOrderSharedWithIntervener1(caseDetails)));
        assertThat(data.getIntervenerTwoWrapper().getIntervenerCorrespondenceEnabled(),
            equalTo(generalOrderService.isOrderSharedWithIntervener2(caseDetails)));
        assertThat(data.getIntervenerThreeWrapper().getIntervenerCorrespondenceEnabled(),
            equalTo(generalOrderService.isOrderSharedWithIntervener3(caseDetails)));
        assertThat(data.getIntervenerFourWrapper().getIntervenerCorrespondenceEnabled(),
            equalTo(generalOrderService.isOrderSharedWithIntervener4(caseDetails)));
    }

    private CaseDocument getCaseDocument(Object documentMap) {
        return objectMapper.convertValue(documentMap, new TypeReference<>() {
        });
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

}
