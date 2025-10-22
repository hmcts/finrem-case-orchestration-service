package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.datamigration.controller.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.bsp.common.model.document.CtscContactDetails;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.address.LetterAddresseeGeneratorMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AdditionalHearingDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AdditionalHearingDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetail;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetailCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetailsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetailsCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Region;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFour;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOne;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThree;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerChangeDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.AddresseeDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.StampType;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_CARE_OF;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_OPENING_HOURS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_PHONE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_POSTCODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_PO_BOX;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_SERVICE_CENTRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_TOWN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.defaultConsentedCaseDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.defaultConsentedFinremCaseDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.defaultConsentedFinremCaseDetailsWithNonUkRespondent;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.defaultContestedFinremCaseDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.ADDRESSEE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.CTSC_CONTACT_DETAILS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.DOCUMENT_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.INTERVENER_FOUR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.INTERVENER_ONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.INTERVENER_THREE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.INTERVENER_TWO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DIRECTION_DETAILS_COLLECTION_CT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_PREVIEW_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HIGHCOURT_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.util.TestResource.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.util.TestResource.FILE_NAME;

@ExtendWith(MockitoExtension.class)
class DocumentHelperTest {

    private static final String PATH = "/fixtures/latestConsentedConsentOrder/";
    private ObjectMapper objectMapper;
    private DocumentHelper documentHelper;
    @Mock
    private GenericDocumentService service;

    @Mock
    private LetterAddresseeGeneratorMapper letterAddresseeGenerator;
    private FinremCaseDetailsMapper finremCaseDetailsMapper;
    @Spy
    private InternationalPostalService postalService;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        CaseDataService caseDataService = new CaseDataService(objectMapper);
        finremCaseDetailsMapper = new FinremCaseDetailsMapper(objectMapper);
        documentHelper = new DocumentHelper(objectMapper, caseDataService,
            service, finremCaseDetailsMapper, letterAddresseeGenerator, postalService);
    }

    @Test
    void shouldGetLatestAmendedConsentOrder() throws Exception {
        CallbackRequest callbackRequest = prepareCallbackRequestForLatestConsentedConsentOrder("amend-consent-order-by-caseworker.json");
        CaseDocument latestAmendedConsentOrder = documentHelper.getLatestAmendedConsentOrder(
            callbackRequest.getCaseDetails().getData());
        assertThat(latestAmendedConsentOrder.getDocumentBinaryUrl())
            .isEqualTo("http://dm-store:8080/documents/0bdc0d68-e654-4faa-848a-8ae3c478838/binary");
    }

    @Test
    void shouldGetLatestFinremAmendedConsentOrder() throws Exception {
        FinremCallbackRequest callbackRequest = prepareFinremCallbackRequestForLatestConsentedConsentOrder("amend-consent-order-by-caseworker.json");
        CaseDocument latestAmendedConsentOrder = documentHelper.getLatestAmendedConsentOrder(
            callbackRequest.getCaseDetails().getData());
        assertThat(latestAmendedConsentOrder.getDocumentBinaryUrl())
            .isEqualTo("http://dm-store:8080/documents/0bdc0d68-e654-4faa-848a-8ae3c478838/binary");
    }

    @Test
    void shouldGetPensionDocuments() throws Exception {
        CallbackRequest callbackRequest = prepareCallbackRequestForLatestConsentedConsentOrder("validate-pension-collection.json");
        List<CaseDocument> pensionDocuments = documentHelper.getPensionDocumentsData(
            callbackRequest.getCaseDetails().getData());
        assertThat(pensionDocuments).hasSize(2);
    }

    @Test
    void returnNewListWhenCaseDataIsNullPensionDocuments() {
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetailsBefore(CaseDetails.builder().build()).build();
        List<CaseDocument> pensionDocuments = documentHelper.getPensionDocumentsData(
            callbackRequest.getCaseDetailsBefore().getData());
        assertThat(pensionDocuments).isEmpty();
    }

    @Test
    void shouldGetPensionDocumentsFinrem() throws Exception {
        FinremCallbackRequest callbackRequest = prepareFinremCallbackRequestForLatestConsentedConsentOrder("validate-pension-collection.json");
        List<CaseDocument> pensionDocuments = documentHelper.getPensionDocumentsData(
            callbackRequest.getCaseDetails().getData());
        assertThat(pensionDocuments).hasSize(2);
    }

    @Test
    void shouldGetVariationOrderDocuments() throws Exception {
        CallbackRequest callbackRequest = prepareCallbackRequestForLatestConsentedConsentOrder("validate-pension-collection.json");
        List<CaseDocument> pensionDocuments = documentHelper.getVariationOrderDocumentsData(
            callbackRequest.getCaseDetails().getData());
        assertThat(pensionDocuments).hasSize(1);
        callbackRequest = CallbackRequest.builder().caseDetailsBefore(CaseDetails.builder().build()).build();
        List<CaseDocument> pensionDocuments1 = documentHelper.getVariationOrderDocumentsData(
            callbackRequest.getCaseDetailsBefore().getData());
        assertThat(pensionDocuments1).isEmpty();
    }

    @Test
    void shouldGetConsentOrderOtherDocuments() throws Exception {
        CallbackRequest callbackRequest = prepareCallbackRequestForLatestConsentedConsentOrder("validate-pension-collection.json");
        List<CaseDocument> pensionDocuments = documentHelper.getConsentOrderOtherDocumentsData(
            callbackRequest.getCaseDetails().getData());
        assertThat(pensionDocuments).hasSize(1);
        callbackRequest = CallbackRequest.builder().caseDetailsBefore(CaseDetails.builder().build()).build();
        List<CaseDocument> pensionDocuments1 = documentHelper.getConsentOrderOtherDocumentsData(
            callbackRequest.getCaseDetailsBefore().getData());
        assertThat(pensionDocuments1).isEmpty();
    }

    @Test
    void hasAnotherHearingFalse() {
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder().id(123L).data(new HashMap<>()).build()).build();
        assertFalse(documentHelper.hasAnotherHearing(callbackRequest.getCaseDetails().getData()));
    }

    @Test
    void hasAnotherHearingTrue() {
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder().id(123L).data(new HashMap<>()).build()).build();

        DirectionDetailsCollection ddc = DirectionDetailsCollection.builder().isAnotherHearingYN("Yes").build();
        DirectionDetailsCollectionData dt = DirectionDetailsCollectionData.builder().directionDetailsCollection(ddc).build();
        List<DirectionDetailsCollectionData> list = new ArrayList<>();
        list.add(dt);
        Map<String, Object> data = callbackRequest.getCaseDetails().getData();
        data.put(DIRECTION_DETAILS_COLLECTION_CT, list);
        assertTrue(documentHelper.hasAnotherHearing(data));
    }

    @Test
    void castToList() throws Exception {
        CallbackRequest callbackRequest = prepareCallbackRequestForLatestConsentedConsentOrder("validate-pension-collection.json");
        List<String> natureList = documentHelper.convertToList(
            callbackRequest.getCaseDetails().getData().get("natureOfApplication6"));
        assertThat(natureList).hasSize(2);
    }

    @Test
    void shouldGetFormADocuments() throws Exception {
        CallbackRequest callbackRequest = prepareCallbackRequestForLatestConsentedConsentOrder("validate-form-a-collection.json");
        List<CaseDocument> formADocuments = documentHelper.getFormADocumentsData(
            callbackRequest.getCaseDetails().getData());
        assertThat(formADocuments).hasSize(2);
    }

    @Test
    void shouldGetFormADocumentsFinrem() throws Exception {
        FinremCallbackRequest callbackRequest = prepareFinremCallbackRequestForLatestConsentedConsentOrder("validate-form-a-collection.json");
        List<CaseDocument> pensionDocuments = documentHelper.getFormADocumentsData(
            callbackRequest.getCaseDetails().getData());
        assertThat(pensionDocuments).hasSize(2);
    }

    @Test
    void shouldGetConsentedInContestedPensionDocuments() throws Exception {
        CallbackRequest callbackRequest = prepareCallbackRequestForLatestConsentedConsentOrder("consented-in-consented.json");
        List<CaseDocument> pensionDocuments = documentHelper.getConsentedInContestedPensionDocumentsData(
            callbackRequest.getCaseDetails().getData());
        assertThat(pensionDocuments).hasSize(2);
    }

    @Test
    void hasAnotherHearing_shouldReturnTrue() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData caseData = finremCallbackRequest.getCaseDetails().getData();
        DirectionDetailCollection directionDetailsCollection = DirectionDetailCollection.builder()
            .value(DirectionDetail.builder().isAnotherHearingYN(YesOrNo.YES).build()).build();

        caseData.setDirectionDetailsCollection(singletonList(directionDetailsCollection));
        assertTrue(documentHelper.hasAnotherHearing(caseData));

        directionDetailsCollection = DirectionDetailCollection.builder()
            .value(DirectionDetail.builder().isAnotherHearingYN(YesOrNo.NO).build()).build();

        caseData.setDirectionDetailsCollection(singletonList(directionDetailsCollection));
        assertFalse(documentHelper.hasAnotherHearing(caseData));
    }

    @Test
    void hasAnotherHearing_noDirectionDetails() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData caseData = finremCallbackRequest.getCaseDetails().getData();
        caseData.setDirectionDetailsCollection(emptyList());
        assertFalse(documentHelper.hasAnotherHearing(caseData));
    }

    @Test
    void getLatestAdditionalHearingDocument() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData caseData = finremCallbackRequest.getCaseDetails().getData();

        Optional<CaseDocument> latestDocumentNotAvailable = documentHelper.getLatestAdditionalHearingDocument(caseData);
        assertFalse(latestDocumentNotAvailable.isPresent());

        List<AdditionalHearingDocumentCollection> additionalHearingDocuments = new ArrayList<>();
        AdditionalHearingDocumentCollection doc1
            = AdditionalHearingDocumentCollection.builder().value(AdditionalHearingDocument
            .builder().document(caseDocument()).additionalHearingDocumentDate(LocalDateTime.now()).build()).build();
        AdditionalHearingDocumentCollection doc2
            = AdditionalHearingDocumentCollection.builder().value(AdditionalHearingDocument
            .builder().document(caseDocument("url","abc.pdf","binaryURL"))
            .additionalHearingDocumentDate(LocalDateTime.now()).build()).build();

        additionalHearingDocuments.add(doc1);
        additionalHearingDocuments.add(doc2);

        caseData.getListForHearingWrapper().setAdditionalHearingDocuments(additionalHearingDocuments);

        Optional<CaseDocument> latestDocumentAvailable = documentHelper.getLatestAdditionalHearingDocument(caseData);

        assertTrue(latestDocumentAvailable.isPresent());
        assertEquals("abc.pdf", latestDocumentAvailable.get().getDocumentFilename());
    }

    @Test
    void getHearingDocumentsAsBulkPrintDocuments() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();

        DocumentCollectionItem dc = DocumentCollectionItem
            .builder()
            .value(caseDocument(DOCUMENT_URL, FILE_NAME, BINARY_URL))
            .build();
        List<DocumentCollectionItem> documentCollectionItems = new ArrayList<>();
        documentCollectionItems.add(dc);
        caseData.setHearingOrderOtherDocuments(documentCollectionItems);

        when(service.convertDocumentIfNotPdfAlready(any(), any(), eq(CONTESTED))).thenReturn(caseDocument());

        List<CaseDocument> hearingDocuments2 = documentHelper.getHearingDocumentsAsPdfDocuments(caseDetails, AUTHORIZATION_HEADER);
        assertEquals("app_docs.pdf", hearingDocuments2.getFirst().getDocumentFilename());
        assertEquals(BINARY_URL, hearingDocuments2.getFirst().getDocumentBinaryUrl());

        verify(service).convertDocumentIfNotPdfAlready(any(), any(), eq(CONTESTED));
    }

    @Test
    void shouldGetRespondToOrderDocuments() throws Exception {
        CallbackRequest callbackRequest = prepareCallbackRequestForLatestConsentedConsentOrder("respond-to-order-solicitor.json");
        Optional<CaseDocument> latestRespondToOrderDocuments = documentHelper.getLatestRespondToOrderDocuments(
            callbackRequest.getCaseDetails().getData());
        assertThat(latestRespondToOrderDocuments).isPresent();
        assertThat(latestRespondToOrderDocuments.get().getDocumentBinaryUrl()).isEqualTo("http://doc2/binary");
    }

    @Test
    void shouldGetFinremRespondToOrderDocuments() throws Exception {
        FinremCallbackRequest callbackRequest = prepareFinremCallbackRequestForLatestConsentedConsentOrder("respond-to-order-solicitor.json");
        Optional<CaseDocument> latestRespondToOrderDocuments = documentHelper.getLatestRespondToOrderDocuments(
            callbackRequest.getCaseDetails().getData());
        assertThat(latestRespondToOrderDocuments).isPresent();
        assertThat(latestRespondToOrderDocuments.get().getDocumentBinaryUrl()).isEqualTo("http://doc2/binary");
    }

    @Test
    void shouldNotGetRespondToOrderDocuments() throws Exception {
        CallbackRequest callbackRequest = prepareCallbackRequestForLatestConsentedConsentOrder("respond-to-order-without-consent-order.json");
        Optional<CaseDocument> latestRespondToOrderDocuments = documentHelper.getLatestRespondToOrderDocuments(
            callbackRequest.getCaseDetails().getData());
        assertThat(latestRespondToOrderDocuments).isNotPresent();
    }

    @Test
    void shouldNotGetFinremRespondToOrderDocuments() throws Exception {
        FinremCallbackRequest callbackRequest =
            prepareFinremCallbackRequestForLatestConsentedConsentOrder("respond-to-order-without-consent-order.json");
        Optional<CaseDocument> latestRespondToOrderDocuments = documentHelper.getLatestRespondToOrderDocuments(
            callbackRequest.getCaseDetails().getData());
        assertThat(latestRespondToOrderDocuments).isNotPresent();
    }

    @Test
    void shouldGetCaseDocument() throws Exception {
        CallbackRequest callbackRequest = prepareCallbackRequestForLatestConsentedConsentOrder("draft-consent-order.json");
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();
        CaseDocument caseDocument = documentHelper.convertToCaseDocument(data.get(CONSENT_ORDER));

        assertThat(caseDocument.getDocumentBinaryUrl()).isEqualTo("http://file1.binary");
        assertThat(caseDocument.getDocumentUrl()).isEqualTo("http://file1");
        assertThat(caseDocument.getDocumentFilename()).isEqualTo("file1");
    }

    @Test
    void shouldGetFinremCaseDocument() throws Exception {
        FinremCallbackRequest callbackRequest = prepareFinremCallbackRequestForLatestConsentedConsentOrder("draft-consent-order.json");
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();
        CaseDocument caseDocument = documentHelper.convertToCaseDocument(data.getConsentOrder());

        assertThat(caseDocument.getDocumentBinaryUrl()).isEqualTo("http://file1.binary");
        assertThat(caseDocument.getDocumentUrl()).isEqualTo("http://file1");
        assertThat(caseDocument.getDocumentFilename()).isEqualTo("file1");
    }

    @Test
    void testAddressIsCorrectlyFormatterForLetterPrinting() {
        Map<String, Object> testAddressMap = new HashMap<>();
        testAddressMap.put("AddressLine1", "50 Applicant Street");
        testAddressMap.put("AddressLine2", "Second Address Line");
        testAddressMap.put("AddressLine3", "Third Address Line");
        testAddressMap.put("County", "Greater London");
        testAddressMap.put("Country", "United kingdom");
        testAddressMap.put("PostTown", "London");
        testAddressMap.put("PostCode", "SW1");

        String formattedAddress = documentHelper.formatAddressForLetterPrinting(testAddressMap, true);

        String expectedAddress = """
            50 Applicant Street
            Second Address Line
            Third Address Line
            Greater London
            London
            SW1
            United kingdom""";

        assertEquals(expectedAddress, formattedAddress);
    }

    @Test
    void testAddressWithNullValuesIsCorrectlyFormatterForLetterPrinting() {
        Map<String, Object> testAddressMap = new HashMap<>();
        testAddressMap.put("AddressLine1", "50 Applicant Street");
        testAddressMap.put("AddressLine2", "");
        testAddressMap.put("AddressLine3", "null");
        testAddressMap.put("County", null);
        testAddressMap.put("Country", "England");
        testAddressMap.put("PostTown", null);
        testAddressMap.put("PostCode", "SW1");

        String formattedAddress = documentHelper.formatAddressForLetterPrinting(testAddressMap, false);
        String expectedAddress = "50 Applicant Street" + "\n" + "SW1";

        assertEquals(expectedAddress, formattedAddress);
    }

    @Test
    void testAddressWithCountryAndAddressLine3AreNotInOutputForLetterPrinting() {
        Map<String, Object> testAddressMap = new HashMap<>();
        testAddressMap.put("AddressLine1", "50 Applicant Street");
        testAddressMap.put("AddressLine2", "Second Address Line");
        testAddressMap.put("AddressLine3", "Third Address Line");
        testAddressMap.put("County", "Greater London");
        testAddressMap.put("Country", "England");
        testAddressMap.put("PostTown", "London");
        testAddressMap.put("PostCode", "SW1");

        String formattedAddress = documentHelper.formatAddressForLetterPrinting(testAddressMap, true);
        String expectedAddress = """
            50 Applicant Street
            Second Address Line
            Third Address Line
            Greater London
            London
            SW1
            England""";

        assertEquals(expectedAddress, formattedAddress);
    }

    @Test
    void testAddressWithMissingFieldsAndEmptyValuesIsCorrectlyFormatterForLetterPrinting() {
        Map<String, Object> testAddressMap = new HashMap<>();
        testAddressMap.put("AddressLine1", "null");
        testAddressMap.put("AddressLine2", "");
        testAddressMap.put("PostCode", null);

        String formattedAddress = documentHelper.formatAddressForLetterPrinting(testAddressMap, false);
        String expectedAddress = "";

        assertEquals(expectedAddress, formattedAddress);
    }

    @Test
    void testAddressWithAllNullValuesIsCorrectlyFormatterForLetterPrinting() {
        Map<String, Object> testAddressMap = new HashMap<>();
        testAddressMap.put("AddressLine1", null);
        testAddressMap.put("AddressLine2", null);
        testAddressMap.put("AddressLine3", null);
        testAddressMap.put("County", null);
        testAddressMap.put("Country", null);
        testAddressMap.put("PostTown", null);
        testAddressMap.put("PostCode", null);

        String formattedAddress = documentHelper.formatAddressForLetterPrinting(testAddressMap, false);
        String expectedAddress = "";

        assertEquals(expectedAddress, formattedAddress);
    }

    @Test
    void whenPreparingLetterToApplicantTemplateData_CtscDataIsPopulated() {
        CaseDetails preparedCaseDetails = defaultConsentedCaseDetails();

        when(letterAddresseeGenerator.generate(preparedCaseDetails, APPLICANT)).thenReturn(
            AddresseeDetails.builder()
                .addresseeName("addresseeName")
                .reference("reference")
                .addressToSendTo(buildAddress()).build());
        preparedCaseDetails = documentHelper.prepareLetterTemplateData(defaultConsentedCaseDetails(), APPLICANT);

        CtscContactDetails ctscContactDetails = CtscContactDetails.builder()
            .serviceCentre(CTSC_SERVICE_CENTRE)
            .careOf(CTSC_CARE_OF)
            .poBox(CTSC_PO_BOX)
            .town(CTSC_TOWN)
            .postcode(CTSC_POSTCODE)
            .emailAddress(CTSC_EMAIL_ADDRESS)
            .phoneNumber(CTSC_PHONE_NUMBER)
            .openingHours(CTSC_OPENING_HOURS)
            .build();

        assertEquals(ctscContactDetails, preparedCaseDetails.getData().get(CTSC_CONTACT_DETAILS));
    }

    @Test
    void whenPreparingLetterToApplicantTemplateData_CtscDataIsPopulated_finrem() {
        FinremCaseDetails finremCaseDetails = defaultConsentedFinremCaseDetails();

        when(letterAddresseeGenerator.generate(finremCaseDetails, APPLICANT)).thenReturn(
            AddresseeDetails.builder()
                .addresseeName("addresseeName")
                .reference("reference")
                .finremAddressToSendTo(buildFinremAddress()).build());

        when(letterAddresseeGenerator.generate(finremCaseDetails, APPLICANT)).thenReturn(
            AddresseeDetails.builder()
                .addresseeName("addresseeName")
                .reference("reference")
                .finremAddressToSendTo(buildFinremAddress()).build());

        CaseDetails preparedCaseDetails = documentHelper.prepareLetterTemplateData(finremCaseDetails, APPLICANT);

        CtscContactDetails ctscContactDetails = CtscContactDetails.builder()
            .serviceCentre(CTSC_SERVICE_CENTRE)
            .careOf(CTSC_CARE_OF)
            .poBox(CTSC_PO_BOX)
            .town(CTSC_TOWN)
            .postcode(CTSC_POSTCODE)
            .emailAddress(CTSC_EMAIL_ADDRESS)
            .phoneNumber(CTSC_PHONE_NUMBER)
            .openingHours(CTSC_OPENING_HOURS)
            .build();

        assertEquals(ctscContactDetails, preparedCaseDetails.getData().get(CTSC_CONTACT_DETAILS));
    }

    @Test
    void whenPreparingLetterToRespondentTemplateData_CtscDataIsPopulated() {

        CaseDetails caseDetails = defaultConsentedCaseDetails();

        when(letterAddresseeGenerator.generate(caseDetails, RESPONDENT)).thenReturn(
            AddresseeDetails.builder()
                .addresseeName("addresseeName")
                .reference("reference")
                .addressToSendTo(buildAddress()).build());

        CaseDetails preparedCaseDetails = documentHelper.prepareLetterTemplateData(caseDetails, RESPONDENT);

        CtscContactDetails ctscContactDetails = CtscContactDetails.builder()
            .serviceCentre(CTSC_SERVICE_CENTRE)
            .careOf(CTSC_CARE_OF)
            .poBox(CTSC_PO_BOX)
            .town(CTSC_TOWN)
            .postcode(CTSC_POSTCODE)
            .emailAddress(CTSC_EMAIL_ADDRESS)
            .phoneNumber(CTSC_PHONE_NUMBER)
            .openingHours(CTSC_OPENING_HOURS)
            .build();

        assertEquals(ctscContactDetails, preparedCaseDetails.getData().get(CTSC_CONTACT_DETAILS));
    }

    @Test
    void whenPreparingLetterToRespondentTemplateData_CtscDataIsPopulated_finrem() {
        FinremCaseDetails finremCaseDetails = defaultConsentedFinremCaseDetails();

        when(letterAddresseeGenerator.generate(finremCaseDetails, RESPONDENT)).thenReturn(
            AddresseeDetails.builder()
                .addresseeName("addresseeName")
                .reference("reference")
                .finremAddressToSendTo(buildFinremAddress()).build());

        CaseDetails preparedCaseDetails = documentHelper.prepareLetterTemplateData(finremCaseDetails, RESPONDENT);

        CtscContactDetails ctscContactDetails = CtscContactDetails.builder()
            .serviceCentre(CTSC_SERVICE_CENTRE)
            .careOf(CTSC_CARE_OF)
            .poBox(CTSC_PO_BOX)
            .town(CTSC_TOWN)
            .postcode(CTSC_POSTCODE)
            .emailAddress(CTSC_EMAIL_ADDRESS)
            .phoneNumber(CTSC_PHONE_NUMBER)
            .openingHours(CTSC_OPENING_HOURS)
            .build();

        assertEquals(ctscContactDetails, preparedCaseDetails.getData().get(CTSC_CONTACT_DETAILS));
    }

    @Test
    void whenPreparingLetterToNonUkRespondentTemplateData_CtscDataIsPopulated_finrem() {
        FinremCaseDetails finremCaseDetails = defaultConsentedFinremCaseDetailsWithNonUkRespondent();

        when(letterAddresseeGenerator.generate(finremCaseDetails, RESPONDENT)).thenReturn(
            AddresseeDetails.builder()
                .addresseeName("addresseeName")
                .reference("reference")
                .finremAddressToSendTo(buildFinremAddressWithEmptyPostCode()).build());

        CaseDetails preparedCaseDetails = documentHelper.prepareLetterTemplateData(finremCaseDetails, RESPONDENT);

        CtscContactDetails ctscContactDetails = CtscContactDetails.builder()
            .serviceCentre(CTSC_SERVICE_CENTRE)
            .careOf(CTSC_CARE_OF)
            .poBox(CTSC_PO_BOX)
            .town(CTSC_TOWN)
            .postcode(CTSC_POSTCODE)
            .emailAddress(CTSC_EMAIL_ADDRESS)
            .phoneNumber(CTSC_PHONE_NUMBER)
            .openingHours(CTSC_OPENING_HOURS)
            .build();

        assertEquals(ctscContactDetails, preparedCaseDetails.getData().get(CTSC_CONTACT_DETAILS));
    }

    @Test
    void whenRecipientIsNondigitallyRepresentedApplicant_AndIntervenerRepresented_setAddressee() {
        FinremCaseDetails caseDetails = defaultContestedFinremCaseDetails();
        caseDetails.getData().getContactDetailsWrapper().setApplicantRepresented(YesOrNo.YES);

        Address address = Address.builder().addressLine1("Applicant Sol Address").postCode("SW11 6HL").build();
        caseDetails.getData().getContactDetailsWrapper().setApplicantSolicitorAddress(address);
        caseDetails.getData().getContactDetailsWrapper().setApplicantFmName("Tracy");
        caseDetails.getData().getContactDetailsWrapper().setApplicantLname("Applicant");

        Address otherAddress = Address.builder().addressLine1("Other Address").postCode("E14 6HL").build();
        IntervenerOne wrapper = new IntervenerOne();
        wrapper.setIntervenerName("name");
        wrapper.setIntervenerRepresented(YesOrNo.YES);
        wrapper.setIntervenerAddress(otherAddress);
        caseDetails.getData().setCurrentIntervenerChangeDetails(new IntervenerChangeDetails());
        caseDetails.getData().getCurrentIntervenerChangeDetails().setIntervenerDetails(wrapper);

        CaseDetails result = documentHelper.prepareIntervenerLetterTemplateData(caseDetails, APPLICANT);
        Addressee expected = Addressee.builder().name("Tracy Applicant").formattedAddress("Applicant Sol Address" + "\nSW11 6HL").build();
        assertEquals(result.getData().get(ADDRESSEE), expected);
    }

    @Test
    void whenRecipientIsNondigitallyRepresentedRespondent_AndIntervenerRepresented_setAddressee() {
        FinremCaseDetails caseDetails = defaultContestedFinremCaseDetails();

        Address address = Address.builder().addressLine1("Respondent Sol Address").postCode("SW11 6HL").build();
        caseDetails.getData().getContactDetailsWrapper().setRespondentSolicitorAddress(address);
        caseDetails.getData().getContactDetailsWrapper().setRespondentFmName("Tracy");
        caseDetails.getData().getContactDetailsWrapper().setRespondentLname("Respondent");
        caseDetails.getData().getContactDetailsWrapper().setContestedRespondentRepresented(YesOrNo.YES);

        Address otherAddress = Address.builder().addressLine1("Other Address").postCode("E14 6HL").build();
        IntervenerOne wrapper = new IntervenerOne();
        wrapper.setIntervenerName("name");
        wrapper.setIntervenerRepresented(YesOrNo.YES);
        wrapper.setIntervenerAddress(otherAddress);
        caseDetails.getData().setCurrentIntervenerChangeDetails(new IntervenerChangeDetails());
        caseDetails.getData().getCurrentIntervenerChangeDetails().setIntervenerDetails(wrapper);

        CaseDetails result = documentHelper.prepareIntervenerLetterTemplateData(caseDetails, RESPONDENT);
        Addressee expected = Addressee.builder().name("Tracy Respondent").formattedAddress("Respondent Sol Address" + "\nSW11 6HL").build();
        assertEquals(result.getData().get(ADDRESSEE), expected);
    }

    @Test
    void whenRecipientIsUnrepresentedApplicant_AndIntervenerRepresented_setAddressee() {
        FinremCaseDetails caseDetails = defaultContestedFinremCaseDetails();
        caseDetails.getData().getContactDetailsWrapper().setApplicantRepresented(YesOrNo.NO);

        Address address = Address.builder().addressLine1("Applicant Address").postCode("SW11 6HL").build();
        caseDetails.getData().getContactDetailsWrapper().setApplicantAddress(address);
        caseDetails.getData().getContactDetailsWrapper().setApplicantFmName("Tracy");
        caseDetails.getData().getContactDetailsWrapper().setApplicantLname("Applicant");

        Address otherAddress = Address.builder().addressLine1("Other Address").postCode("E14 6HL").build();
        IntervenerOne wrapper = new IntervenerOne();
        wrapper.setIntervenerName("name");
        wrapper.setIntervenerRepresented(YesOrNo.YES);
        wrapper.setIntervenerAddress(otherAddress);
        caseDetails.getData().setCurrentIntervenerChangeDetails(new IntervenerChangeDetails());
        caseDetails.getData().getCurrentIntervenerChangeDetails().setIntervenerDetails(wrapper);

        CaseDetails result = documentHelper.prepareIntervenerLetterTemplateData(caseDetails, APPLICANT);
        Addressee expected = Addressee.builder().name("Tracy Applicant").formattedAddress("Applicant Address" + "\nSW11 6HL").build();
        assertEquals(result.getData().get(ADDRESSEE), expected);
    }

    @Test
    void whenRecipientIsUnrepresentedRespondent_AndIntervenerRepresented_setAddressee() {
        FinremCaseDetails caseDetails = defaultContestedFinremCaseDetails();
        caseDetails.getData().getContactDetailsWrapper().setContestedRespondentRepresented(YesOrNo.NO);

        Address address = Address.builder().addressLine1("Respondent Address").postCode("SW11 6HL").build();
        caseDetails.getData().getContactDetailsWrapper().setRespondentAddress(address);
        caseDetails.getData().getContactDetailsWrapper().setRespondentFmName("Tracy");
        caseDetails.getData().getContactDetailsWrapper().setRespondentLname("Respondent");

        Address otherAddress = Address.builder().addressLine1("Other Address").postCode("E14 6HL").build();
        IntervenerOne wrapper = new IntervenerOne();
        wrapper.setIntervenerName("name");
        wrapper.setIntervenerRepresented(YesOrNo.YES);
        wrapper.setIntervenerAddress(otherAddress);
        caseDetails.getData().setCurrentIntervenerChangeDetails(new IntervenerChangeDetails());
        caseDetails.getData().getCurrentIntervenerChangeDetails().setIntervenerDetails(wrapper);

        CaseDetails result = documentHelper.prepareIntervenerLetterTemplateData(caseDetails, RESPONDENT);
        Addressee expected = Addressee.builder().name("Tracy Respondent").formattedAddress("Respondent Address" + "\nSW11 6HL").build();
        assertEquals(result.getData().get(ADDRESSEE), expected);
    }

    @Test
    void whenRecipientIsIntervenerOne_AndIntervenerNotRepresented_setAddressee() {
        Address address = Address.builder().addressLine1("addressLine1").postCode("SW1 1TE").build();
        IntervenerOne wrapper = new IntervenerOne();
        wrapper.setIntervenerAddress(address);
        wrapper.setIntervenerName("Name");
        wrapper.setIntervenerRepresented(YesOrNo.NO);
        FinremCaseDetails caseDetails = defaultContestedFinremCaseDetails();
        caseDetails.getData().setCurrentIntervenerChangeDetails(new IntervenerChangeDetails());
        caseDetails.getData().getCurrentIntervenerChangeDetails().setIntervenerDetails(wrapper);
        Addressee expected = Addressee.builder().name("Name").formattedAddress("addressLine1"
            + "\nSW1 1TE").build();

        CaseDetails result = documentHelper.prepareIntervenerLetterTemplateData(caseDetails, INTERVENER_ONE);

        assertEquals(result.getData().get(ADDRESSEE), expected);
    }

    @Test
    void whenRecipientIsIntervenerTwo_AndIntervenerNotRepresented_setAddressee() {
        Address address = Address.builder().addressLine1("addressLine1").postCode("SW1 1TE").build();
        IntervenerTwo wrapper = new IntervenerTwo();
        wrapper.setIntervenerAddress(address);
        wrapper.setIntervenerName("Name");
        wrapper.setIntervenerRepresented(YesOrNo.NO);
        FinremCaseDetails caseDetails = defaultContestedFinremCaseDetails();
        caseDetails.getData().setCurrentIntervenerChangeDetails(new IntervenerChangeDetails());
        caseDetails.getData().getCurrentIntervenerChangeDetails().setIntervenerDetails(wrapper);
        Addressee expected = Addressee.builder().name("Name").formattedAddress("addressLine1"
            + "\nSW1 1TE").build();

        CaseDetails result = documentHelper.prepareIntervenerLetterTemplateData(caseDetails, INTERVENER_TWO);

        assertEquals(result.getData().get(ADDRESSEE), expected);
    }

    @Test
    void whenRecipientIsIntervenerThree_AndIntervenerNotRepresented_setAddressee() {
        Address address = Address.builder().addressLine1("addressLine1").postCode("SW1 1TE").build();
        IntervenerThree wrapper = new IntervenerThree();
        wrapper.setIntervenerAddress(address);
        wrapper.setIntervenerName("Name");
        wrapper.setIntervenerRepresented(YesOrNo.NO);
        FinremCaseDetails caseDetails = defaultContestedFinremCaseDetails();
        caseDetails.getData().setCurrentIntervenerChangeDetails(new IntervenerChangeDetails());
        caseDetails.getData().getCurrentIntervenerChangeDetails().setIntervenerDetails(wrapper);
        Addressee expected = Addressee.builder().name("Name").formattedAddress("addressLine1"
            + "\nSW1 1TE").build();

        CaseDetails result = documentHelper.prepareIntervenerLetterTemplateData(caseDetails, INTERVENER_THREE);

        assertEquals(result.getData().get(ADDRESSEE), expected);
    }

    @Test
    void whenRecipientIsIntervenerFour_AndIntervenerNotRepresented_setAddressee() {
        Address address = Address.builder().addressLine1("addressLine1").postCode("SW1 1TE").build();
        IntervenerFour wrapper = new IntervenerFour();
        wrapper.setIntervenerAddress(address);
        wrapper.setIntervenerName("Name");
        wrapper.setIntervenerRepresented(YesOrNo.NO);
        FinremCaseDetails caseDetails = defaultContestedFinremCaseDetails();
        caseDetails.getData().setCurrentIntervenerChangeDetails(new IntervenerChangeDetails());
        caseDetails.getData().getCurrentIntervenerChangeDetails().setIntervenerDetails(wrapper);
        Addressee expected = Addressee.builder().name("Name").formattedAddress("addressLine1"
            + "\nSW1 1TE").build();

        CaseDetails result = documentHelper.prepareIntervenerLetterTemplateData(caseDetails, INTERVENER_FOUR);

        assertEquals(result.getData().get(ADDRESSEE), expected);
    }

    @Test
    void shouldReturnTrueWhenCourtIsHighCourt() {
        CaseDetails preparedCaseDetails = defaultConsentedCaseDetails();
        preparedCaseDetails.getData().put(HIGHCOURT_COURTLIST, "highcourt");
        boolean isHighCourt = documentHelper.isHighCourtSelected(preparedCaseDetails.getData());
        assertTrue(isHighCourt);
    }

    @Test
    void shouldReturnTrueWhenCourtIsHighCourtInFinremCaseData() {
        FinremCaseDetails preparedCaseDetails = defaultConsentedFinremCaseDetails();
        preparedCaseDetails.getData().getRegionWrapper().getAllocatedRegionWrapper().setRegionList(Region.HIGHCOURT);
        boolean isHighCourt = documentHelper.isHighCourtSelected(preparedCaseDetails.getData());
        assertTrue(isHighCourt);
    }

    @Test
    void shouldReturnHighCourtStampWhenCourtIsHighCourt() {
        CaseDetails preparedCaseDetails = defaultConsentedCaseDetails();
        preparedCaseDetails.getData().put(HIGHCOURT_COURTLIST, "highcourt");
        StampType actualStampType = documentHelper.getStampType(preparedCaseDetails.getData());
        assertEquals(StampType.HIGH_COURT_STAMP, actualStampType);
    }

    @Test
    void shouldReturnFamilyCourtStampWhenCourtIsLondon() {
        CaseDetails preparedCaseDetails = defaultConsentedCaseDetails();
        preparedCaseDetails.getData().put(LONDON_COURTLIST, "london");
        StampType actualStampType = documentHelper.getStampType(preparedCaseDetails.getData());
        assertEquals(StampType.FAMILY_COURT_STAMP, actualStampType);
    }

    @Test
    void shouldReturnHighCourtStampWhenCourtIsHighCourtInFinremCaseData() {
        FinremCaseDetails preparedCaseDetails = defaultConsentedFinremCaseDetails();
        preparedCaseDetails.getData().getRegionWrapper().getAllocatedRegionWrapper().setRegionList(Region.HIGHCOURT);
        StampType actualStampType = documentHelper.getStampType(preparedCaseDetails.getData());
        assertEquals(StampType.HIGH_COURT_STAMP, actualStampType);
    }

    @Test
    void shouldReturnFamilyCourtStampWhenCourtIsLondonInFinremCaseData() {
        FinremCaseDetails preparedCaseDetails = defaultConsentedFinremCaseDetails();
        preparedCaseDetails.getData().getRegionWrapper().getAllocatedRegionWrapper().setRegionList(Region.LONDON);
        StampType actualStampType = documentHelper.getStampType(preparedCaseDetails.getData());
        assertEquals(StampType.FAMILY_COURT_STAMP, actualStampType);
    }

    @Test
    void convertToCaseDocumentIfObjNotNull() throws Exception {
        CallbackRequest callbackRequest = prepareCallbackRequestForLatestConsentedConsentOrder("draft-consent-order.json");
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();
        CaseDocument caseDocument = documentHelper.convertToCaseDocumentIfObjNotNull(data.get(CONSENT_ORDER));

        assertThat(caseDocument.getDocumentBinaryUrl()).isEqualTo("http://file1.binary");
        assertThat(caseDocument.getDocumentUrl()).isEqualTo("http://file1");
        assertThat(caseDocument.getDocumentFilename()).isEqualTo("file1");
    }

    @Test
    void convertToCaseDocumentIfObjNotNullIfNullReturnNull() throws Exception {
        CallbackRequest callbackRequest = prepareCallbackRequestForLatestConsentedConsentOrder("draft-consent-order.json");
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();
        CaseDocument caseDocument = documentHelper.convertToCaseDocumentIfObjNotNull(data.get(GENERAL_ORDER_PREVIEW_DOCUMENT));
        assertNull(caseDocument);
    }

    @Test
    void convertToCaseDocument() throws Exception {
        CallbackRequest callbackRequest = prepareCallbackRequestForLatestConsentedConsentOrder("draft-consent-order.json");
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();
        CaseDocument caseDocument = documentHelper.convertToCaseDocument(data.get(CONSENT_ORDER), CaseDocument.class);

        assertThat(caseDocument.getDocumentBinaryUrl()).isEqualTo("http://file1.binary");
        assertThat(caseDocument.getDocumentUrl()).isEqualTo("http://file1");
        assertThat(caseDocument.getDocumentFilename()).isEqualTo("file1");
    }

    @Test
    void whenIntervenerOneOnCase_thenGetIntervenerOnePaperNotificationRecipient() {
        IntervenerOne intervenerOne = IntervenerOne.builder().build();
        DocumentHelper.PaperNotificationRecipient recipient = DocumentHelper.getIntervenerPaperNotificationRecipient(intervenerOne);
        assertEquals(INTERVENER_ONE, recipient);
    }

    @Test
    void whenIntervenerTwoOnCase_thenGetIntervenerTwoPaperNotificationRecipient() {
        IntervenerTwo intervenerTwo = IntervenerTwo.builder().build();
        DocumentHelper.PaperNotificationRecipient recipient = DocumentHelper.getIntervenerPaperNotificationRecipient(intervenerTwo);
        assertEquals(INTERVENER_TWO, recipient);
    }

    @Test
    void whenIntervenerThreeOnCase_thenGetIntervenerThreePaperNotificationRecipient() {
        IntervenerThree intervenerThree = IntervenerThree.builder().build();
        DocumentHelper.PaperNotificationRecipient recipient = DocumentHelper.getIntervenerPaperNotificationRecipient(intervenerThree);
        assertEquals(INTERVENER_THREE, recipient);
    }

    @Test
    void whenIntervenerFourOnCase_thenGetIntervenerFourPaperNotificationRecipient() {
        IntervenerFour intervenerFour = IntervenerFour.builder().build();
        DocumentHelper.PaperNotificationRecipient recipient = DocumentHelper.getIntervenerPaperNotificationRecipient(intervenerFour);
        assertEquals(INTERVENER_FOUR, recipient);
    }

    @Test
    void whenNoLatestGeneralOrder_thenReturnNull() {
        FinremCaseDetails caseDetails = defaultContestedFinremCaseDetails();
        assertNull(documentHelper.getLatestGeneralOrder(caseDetails.getData()));
    }

    @ParameterizedTest
    @MethodSource("provideOrderCollections")
    void checkIfOrderAlreadyInFinalOrderCollection(List<DirectionOrderCollection> list, CaseDocument document, boolean expected) {
        assertEquals(expected, documentHelper.checkIfOrderAlreadyInFinalOrderCollection(list, document));
    }

    @Test
    void prepareFinalOrder() {
        DirectionOrderCollection orderCollection = documentHelper.prepareFinalOrder(caseDocument());
        assertEquals(YesOrNo.YES, orderCollection.getValue().getIsOrderStamped());
        assertNotNull(orderCollection.getValue().getOrderDateTime());
    }

    private static Stream<Arguments> provideOrderCollections() {
        CaseDocument doc1 = caseDocument();
        CaseDocument doc2 = caseDocument("url", "name.pdf", "binary");
        DirectionOrderCollection orderCollection = DirectionOrderCollection.builder()
            .value(DirectionOrder.builder().uploadDraftDocument(doc2).build())
            .build();

        return Stream.of(
            Arguments.of(null, doc1, false),
            Arguments.of(new ArrayList<>(), doc1, false),
            Arguments.of(List.of(orderCollection), doc1, false),
            Arguments.of(List.of(orderCollection), doc2, true)
        );
    }

    private FinremCallbackRequest buildCallbackRequest() {
        return FinremCallbackRequest.builder()
            .caseDetailsBefore(FinremCaseDetails.builder().id(123L).caseType(CONTESTED).data(new FinremCaseData()).build())
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONTESTED).data(new FinremCaseData()).build())
            .build();
    }

    private CallbackRequest prepareCallbackRequestForLatestConsentedConsentOrder(String fileName) throws Exception {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(PATH + fileName)) {
            return objectMapper.readValue(resourceAsStream, CallbackRequest.class);
        }
    }

    private FinremCallbackRequest prepareFinremCallbackRequestForLatestConsentedConsentOrder(String fileName) throws Exception {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(PATH + fileName)) {

            CallbackRequest callbackRequest = objectMapper.readValue(resourceAsStream, CallbackRequest.class);
            FinremCaseDetails finremCaseDetails = finremCaseDetailsMapper.mapToFinremCaseDetails(callbackRequest.getCaseDetails());
            return FinremCallbackRequest.builder()
                .caseDetails(finremCaseDetails)
                .build();
        }
    }

    private static Map<String, Object> buildAddress() {
        Map<String, Object> solicitorAddress = new HashMap<>();
        solicitorAddress.put("AddressLine1", "First Address Line");
        solicitorAddress.put("AddressLine2", "Second Address Line");
        solicitorAddress.put("AddressLine3", "Third Address Line");
        solicitorAddress.put("County", "London");
        solicitorAddress.put("Country", "England");
        solicitorAddress.put("PostTown", "London");
        solicitorAddress.put("PostCode", "SE1");
        return solicitorAddress;
    }

    private static Address buildFinremAddress() {
        return buildFinremAddress("SE1");
    }

    private static Address buildFinremAddress(String postCode) {
        return Address.builder()
            .addressLine1("First Address Line").addressLine2("Second Address Line")
            .addressLine3("Third Address Line")
            .county("London")
            .country("England")
            .postTown("London")
            .postCode(postCode)
            .build();
    }

    private static Address buildFinremAddressWithEmptyPostCode() {
        return buildFinremAddress(null);
    }
}
