package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.datamigration.controller.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.bsp.common.model.document.CtscContactDetails;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AdditionalHearingDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AdditionalHearingDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetail;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetailCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Region;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFourWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOneWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThreeWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwoWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerChangeDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.StampType;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_PREVIEW_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HIGHCOURT_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@RunWith(MockitoJUnitRunner.class)
public class DocumentHelperTest {

    private static final String PATH = "/fixtures/latestConsentedConsentOrder/";
    private static final String DOC_URL = "http://dm-store:8080/documents/d607c045-878e-475f-ab8e-b2f667d8af64";
    private static final String BINARY_URL = DOC_URL + "/binary";
    private static final String FILE_NAME = "app_docs.docx";
    private ObjectMapper objectMapper;
    private DocumentHelper documentHelper;
    @Mock
    private GenericDocumentService service;
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Before
    public void setup() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        CaseDataService caseDataService = new CaseDataService(objectMapper);
        finremCaseDetailsMapper = new FinremCaseDetailsMapper(objectMapper);
        documentHelper = new DocumentHelper(objectMapper, caseDataService, service, finremCaseDetailsMapper);
    }

    @Test
    public void shouldGetLatestAmendedConsentOrder() throws Exception {
        CallbackRequest callbackRequest = prepareCallbackRequestForLatestConsentedConsentOrder("amend-consent-order-by-caseworker.json");
        CaseDocument latestAmendedConsentOrder = documentHelper.getLatestAmendedConsentOrder(
            callbackRequest.getCaseDetails().getData());
        assertThat(latestAmendedConsentOrder.getDocumentBinaryUrl(),
            is("http://dm-store:8080/documents/0bdc0d68-e654-4faa-848a-8ae3c478838/binary"));
    }

    @Test
    public void shouldGetLatestFinremAmendedConsentOrder() throws Exception {
        FinremCallbackRequest callbackRequest = prepareFinremCallbackRequestForLatestConsentedConsentOrder("amend-consent-order-by-caseworker.json");
        CaseDocument latestAmendedConsentOrder = documentHelper.getLatestAmendedConsentOrder(
            callbackRequest.getCaseDetails().getData());
        assertThat(latestAmendedConsentOrder.getDocumentBinaryUrl(),
            is("http://dm-store:8080/documents/0bdc0d68-e654-4faa-848a-8ae3c478838/binary"));
    }

    @Test
    public void shouldGetPensionDocuments() throws Exception {
        CallbackRequest callbackRequest = prepareCallbackRequestForLatestConsentedConsentOrder("validate-pension-collection.json");
        List<CaseDocument> pensionDocuments = documentHelper.getPensionDocumentsData(
            callbackRequest.getCaseDetails().getData());
        assertThat(pensionDocuments.size(), is(2));
    }


    @Test
    public void castToList() throws Exception {
        CallbackRequest callbackRequest = prepareCallbackRequestForLatestConsentedConsentOrder("validate-pension-collection.json");
        List<String> natureList = documentHelper.convertToList(
            callbackRequest.getCaseDetails().getData().get("natureOfApplication6"));
        assertThat(natureList.size(), is(2));
    }

    @Test
    public void shouldGetFormADocuments() throws Exception {
        CallbackRequest callbackRequest = prepareCallbackRequestForLatestConsentedConsentOrder("validate-form-a-collection.json");
        List<CaseDocument> formADocuments = documentHelper.getFormADocumentsData(
            callbackRequest.getCaseDetails().getData());
        assertThat(formADocuments.size(), is(2));
    }


    @Test
    public void shouldGetFormADocumentsFinrem() throws Exception {
        FinremCallbackRequest callbackRequest = prepareFinremCallbackRequestForLatestConsentedConsentOrder("validate-form-a-collection.json");
        List<CaseDocument> pensionDocuments = documentHelper.getFormADocumentsData(
            callbackRequest.getCaseDetails().getData());
        assertThat(pensionDocuments.size(), is(2));
    }

    @Test
    public void shouldGetConsentedInContestedPensionDocuments() throws Exception {
        CallbackRequest callbackRequest = prepareCallbackRequestForLatestConsentedConsentOrder("consented-in-consented.json");
        List<CaseDocument> pensionDocuments = documentHelper.getConsentedInContestedPensionDocumentsData(
            callbackRequest.getCaseDetails().getData());
        assertThat(pensionDocuments.size(), is(2));
    }

    @Test
    public void hasAnotherHearing_shouldReturnTrue() {
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
    public void hasAnotherHearing_noDirectionDetails() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData caseData = finremCallbackRequest.getCaseDetails().getData();
        caseData.setDirectionDetailsCollection(emptyList());
        assertFalse(documentHelper.hasAnotherHearing(caseData));
    }

    @Test
    public void getLatestAdditionalHearingDocument() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData caseData = finremCallbackRequest.getCaseDetails().getData();

        Optional<CaseDocument> latestDocumentNotAvailable = documentHelper.getLatestAdditionalHearingDocument(caseData);
        assertFalse(latestDocumentNotAvailable.isPresent());

        List<AdditionalHearingDocumentCollection> additionalHearingDocuments = new ArrayList<>();
        AdditionalHearingDocumentCollection doc1
            = AdditionalHearingDocumentCollection.builder().value(AdditionalHearingDocument
            .builder().document(caseDocument()).build()).build();
        AdditionalHearingDocumentCollection doc2
            = AdditionalHearingDocumentCollection.builder().value(AdditionalHearingDocument
            .builder().document(caseDocument("url","abc.pdf","binaryURL")).build()).build();

        additionalHearingDocuments.add(doc1);
        additionalHearingDocuments.add(doc2);

        caseData.setAdditionalHearingDocuments(additionalHearingDocuments);

        Optional<CaseDocument> latestDocumentAvailable = documentHelper.getLatestAdditionalHearingDocument(caseData);

        assertTrue(latestDocumentAvailable.isPresent());
        assertEquals("abc.pdf", latestDocumentAvailable.get().getDocumentFilename());
    }

    @Test
    public void getHearingDocumentsAsBulkPrintDocuments() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();

        DocumentCollection dc = DocumentCollection
            .builder()
            .value(caseDocument(DOCUMENT_URL, FILE_NAME, BINARY_URL))
            .build();
        List<DocumentCollection> documentCollections = new ArrayList<>();
        documentCollections.add(dc);
        caseData.setHearingOrderOtherDocuments(documentCollections);

        when(service.convertDocumentIfNotPdfAlready(any(), any(), anyString())).thenReturn(caseDocument());

        List<CaseDocument> hearingDocuments2 = documentHelper.getHearingDocumentsAsPdfDocuments(caseDetails, AUTHORIZATION_HEADER);
        assertEquals("app_docs.pdf", hearingDocuments2.get(0).getDocumentFilename());
        assertEquals(BINARY_URL, hearingDocuments2.get(0).getDocumentBinaryUrl());

        verify(service).convertDocumentIfNotPdfAlready(any(), any(), anyString());
    }


    @Test
    public void shouldGetRespondToOrderDocuments() throws Exception {
        CallbackRequest callbackRequest = prepareCallbackRequestForLatestConsentedConsentOrder("respond-to-order-solicitor.json");
        Optional<CaseDocument> latestRespondToOrderDocuments = documentHelper.getLatestRespondToOrderDocuments(
            callbackRequest.getCaseDetails().getData());
        assertThat(latestRespondToOrderDocuments.isPresent(), is(true));
        assertThat(latestRespondToOrderDocuments.get().getDocumentBinaryUrl(), is("http://doc2/binary"));
    }

    @Test
    public void shouldGetFinremRespondToOrderDocuments() throws Exception {
        FinremCallbackRequest callbackRequest = prepareFinremCallbackRequestForLatestConsentedConsentOrder("respond-to-order-solicitor.json");
        Optional<CaseDocument> latestRespondToOrderDocuments = documentHelper.getLatestRespondToOrderDocuments(
            callbackRequest.getCaseDetails().getData());
        assertThat(latestRespondToOrderDocuments.isPresent(), is(true));
        assertThat(latestRespondToOrderDocuments.get().getDocumentBinaryUrl(), is("http://doc2/binary"));
    }

    @Test
    public void shouldNotGetRespondToOrderDocuments() throws Exception {
        CallbackRequest callbackRequest = prepareCallbackRequestForLatestConsentedConsentOrder("respond-to-order-without-consent-order.json");
        Optional<CaseDocument> latestRespondToOrderDocuments = documentHelper.getLatestRespondToOrderDocuments(
            callbackRequest.getCaseDetails().getData());
        assertThat(latestRespondToOrderDocuments.isPresent(), is(false));
    }

    @Test
    public void shouldNotGetFinremRespondToOrderDocuments() throws Exception {
        FinremCallbackRequest callbackRequest =
            prepareFinremCallbackRequestForLatestConsentedConsentOrder("respond-to-order-without-consent-order.json");
        Optional<CaseDocument> latestRespondToOrderDocuments = documentHelper.getLatestRespondToOrderDocuments(
            callbackRequest.getCaseDetails().getData());
        assertThat(latestRespondToOrderDocuments.isPresent(), is(false));
    }

    @Test
    public void shouldGetCaseDocument() throws Exception {
        CallbackRequest callbackRequest = prepareCallbackRequestForLatestConsentedConsentOrder("draft-consent-order.json");
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();
        CaseDocument caseDocument = documentHelper.convertToCaseDocument(data.get(CONSENT_ORDER));

        assertThat(caseDocument.getDocumentBinaryUrl(), is("http://file1.binary"));
        assertThat(caseDocument.getDocumentUrl(), is("http://file1"));
        assertThat(caseDocument.getDocumentFilename(), is("file1"));
    }

    @Test
    public void shouldGetFinremCaseDocument() throws Exception {
        FinremCallbackRequest callbackRequest = prepareFinremCallbackRequestForLatestConsentedConsentOrder("draft-consent-order.json");
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();
        CaseDocument caseDocument = documentHelper.convertToCaseDocument(data.getConsentOrder());

        assertThat(caseDocument.getDocumentBinaryUrl(), is("http://file1.binary"));
        assertThat(caseDocument.getDocumentUrl(), is("http://file1"));
        assertThat(caseDocument.getDocumentFilename(), is("file1"));
    }

    @Test
    public void testAddressIsCorrectlyFormatterForLetterPrinting() {
        Map<String, Object> testAddressMap = new HashMap<>();
        testAddressMap.put("AddressLine1", "50 Applicant Street");
        testAddressMap.put("AddressLine2", "Second Address Line");
        testAddressMap.put("AddressLine3", "Third Address Line");
        testAddressMap.put("County", "Greater London");
        testAddressMap.put("Country", "England");
        testAddressMap.put("PostTown", "London");
        testAddressMap.put("PostCode", "SW1");

        String formattedAddress = documentHelper.formatAddressForLetterPrinting(testAddressMap);

        String expectedAddress = """
            50 Applicant Street
            Second Address Line
            Third Address Line
            Greater London
            London
            SW1""";

        assertThat(formattedAddress, is(expectedAddress));
    }

    @Test
    public void testAddressWithNullValuesIsCorrectlyFormatterForLetterPrinting() {
        Map<String, Object> testAddressMap = new HashMap<>();
        testAddressMap.put("AddressLine1", "50 Applicant Street");
        testAddressMap.put("AddressLine2", "");
        testAddressMap.put("AddressLine3", "null");
        testAddressMap.put("County", null);
        testAddressMap.put("Country", "England");
        testAddressMap.put("PostTown", null);
        testAddressMap.put("PostCode", "SW1");

        String formattedAddress = documentHelper.formatAddressForLetterPrinting(testAddressMap);
        String expectedAddress = "50 Applicant Street" + "\n" + "SW1";

        assertThat(formattedAddress, is(expectedAddress));
    }

    @Test
    public void testAddressWithCountryAndAddressLine3AreNotInOutputForLetterPrinting() {

        Map<String, Object> testAddressMap = new HashMap<>();
        testAddressMap.put("AddressLine1", "50 Applicant Street");
        testAddressMap.put("AddressLine2", "Second Address Line");
        testAddressMap.put("AddressLine3", "Third Address Line");
        testAddressMap.put("County", "Greater London");
        testAddressMap.put("Country", "England");
        testAddressMap.put("PostTown", "London");
        testAddressMap.put("PostCode", "SW1");

        String formattedAddress = documentHelper.formatAddressForLetterPrinting(testAddressMap);
        String expectedAddress = """
            50 Applicant Street
            Second Address Line
            Third Address Line
            Greater London
            London
            SW1""";

        assertThat(formattedAddress, is(expectedAddress));
    }

    @Test
    public void testAddressWithMissingFieldsAndEmptyValuesIsCorrectlyFormatterForLetterPrinting() {
        Map<String, Object> testAddressMap = new HashMap<>();
        testAddressMap.put("AddressLine1", "null");
        testAddressMap.put("AddressLine2", "");
        testAddressMap.put("PostCode", null);

        String formattedAddress = documentHelper.formatAddressForLetterPrinting(testAddressMap);
        String expectedAddress = "";

        assertThat(formattedAddress, is(expectedAddress));
    }

    @Test
    public void testAddressWithAllNullValuesIsCorrectlyFormatterForLetterPrinting() {
        Map<String, Object> testAddressMap = new HashMap<>();
        testAddressMap.put("AddressLine1", null);
        testAddressMap.put("AddressLine2", null);
        testAddressMap.put("AddressLine3", null);
        testAddressMap.put("County", null);
        testAddressMap.put("Country", null);
        testAddressMap.put("PostTown", null);
        testAddressMap.put("PostCode", null);

        String formattedAddress = documentHelper.formatAddressForLetterPrinting(testAddressMap);
        String expectedAddress = "";

        assertThat(formattedAddress, is(expectedAddress));
    }

    @Test
    public void whenPreparingLetterToApplicantTemplateData_CtscDataIsPopulated() {
        CaseDetails preparedCaseDetails = documentHelper.prepareLetterTemplateData(defaultConsentedCaseDetails(), APPLICANT);

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
    public void whenPreparingLetterToApplicantTemplateData_CtscDataIsPopulated_finrem() {
        CaseDetails preparedCaseDetails = documentHelper.prepareLetterTemplateData(defaultConsentedFinremCaseDetails(), APPLICANT);

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
    public void whenPreparingLetterToRespondentTemplateData_CtscDataIsPopulated() {
        CaseDetails preparedCaseDetails = documentHelper.prepareLetterTemplateData(defaultConsentedCaseDetails(), RESPONDENT);

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
    public void whenPreparingLetterToRespondentTemplateData_CtscDataIsPopulated_finrem() {
        CaseDetails preparedCaseDetails = documentHelper.prepareLetterTemplateData(defaultConsentedFinremCaseDetails(), RESPONDENT);

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
    public void whenRecipientIsNondigitallyRepresentedApplicant_AndIntervenerRepresented_setAddressee() {
        FinremCaseDetails caseDetails = defaultContestedFinremCaseDetails();
        caseDetails.getData().getContactDetailsWrapper().setApplicantRepresented(YesOrNo.YES);

        Address address = Address.builder().addressLine1("Applicant Sol Address").postCode("SW11 6HL").build();
        caseDetails.getData().getContactDetailsWrapper().setApplicantSolicitorAddress(address);
        caseDetails.getData().getContactDetailsWrapper().setApplicantFmName("Tracy");
        caseDetails.getData().getContactDetailsWrapper().setApplicantLname("Applicant");

        Address otherAddress = Address.builder().addressLine1("Other Address").postCode("E14 6HL").build();
        IntervenerOneWrapper wrapper = new IntervenerOneWrapper();
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
    public void whenRecipientIsNondigitallyRepresentedRespondent_AndIntervenerRepresented_setAddressee() {
        FinremCaseDetails caseDetails = defaultContestedFinremCaseDetails();

        Address address = Address.builder().addressLine1("Respondent Sol Address").postCode("SW11 6HL").build();
        caseDetails.getData().getContactDetailsWrapper().setRespondentSolicitorAddress(address);
        caseDetails.getData().getContactDetailsWrapper().setRespondentFmName("Tracy");
        caseDetails.getData().getContactDetailsWrapper().setRespondentLname("Respondent");
        caseDetails.getData().getContactDetailsWrapper().setContestedRespondentRepresented(YesOrNo.YES);

        Address otherAddress = Address.builder().addressLine1("Other Address").postCode("E14 6HL").build();
        IntervenerOneWrapper wrapper = new IntervenerOneWrapper();
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
    public void whenRecipientIsUnrepresentedApplicant_AndIntervenerRepresented_setAddressee() {
        FinremCaseDetails caseDetails = defaultContestedFinremCaseDetails();
        caseDetails.getData().getContactDetailsWrapper().setApplicantRepresented(YesOrNo.NO);

        Address address = Address.builder().addressLine1("Applicant Address").postCode("SW11 6HL").build();
        caseDetails.getData().getContactDetailsWrapper().setApplicantAddress(address);
        caseDetails.getData().getContactDetailsWrapper().setApplicantFmName("Tracy");
        caseDetails.getData().getContactDetailsWrapper().setApplicantLname("Applicant");

        Address otherAddress = Address.builder().addressLine1("Other Address").postCode("E14 6HL").build();
        IntervenerOneWrapper wrapper = new IntervenerOneWrapper();
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
    public void whenRecipientIsUnrepresentedRespondent_AndIntervenerRepresented_setAddressee() {
        FinremCaseDetails caseDetails = defaultContestedFinremCaseDetails();
        caseDetails.getData().getContactDetailsWrapper().setContestedRespondentRepresented(YesOrNo.NO);

        Address address = Address.builder().addressLine1("Respondent Address").postCode("SW11 6HL").build();
        caseDetails.getData().getContactDetailsWrapper().setRespondentAddress(address);
        caseDetails.getData().getContactDetailsWrapper().setRespondentFmName("Tracy");
        caseDetails.getData().getContactDetailsWrapper().setRespondentLname("Respondent");

        Address otherAddress = Address.builder().addressLine1("Other Address").postCode("E14 6HL").build();
        IntervenerOneWrapper wrapper = new IntervenerOneWrapper();
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
    public void whenRecipientIsIntervenerOne_AndIntervenerNotRepresented_setAddressee() {
        Address address = Address.builder().addressLine1("addressLine1").postCode("SW1 1TE").build();
        IntervenerOneWrapper wrapper = new IntervenerOneWrapper();
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
    public void whenRecipientIsIntervenerTwo_AndIntervenerNotRepresented_setAddressee() {
        Address address = Address.builder().addressLine1("addressLine1").postCode("SW1 1TE").build();
        IntervenerTwoWrapper wrapper = new IntervenerTwoWrapper();
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
    public void whenRecipientIsIntervenerThree_AndIntervenerNotRepresented_setAddressee() {
        Address address = Address.builder().addressLine1("addressLine1").postCode("SW1 1TE").build();
        IntervenerThreeWrapper wrapper = new IntervenerThreeWrapper();
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
    public void whenRecipientIsIntervenerFour_AndIntervenerNotRepresented_setAddressee() {
        Address address = Address.builder().addressLine1("addressLine1").postCode("SW1 1TE").build();
        IntervenerFourWrapper wrapper = new IntervenerFourWrapper();
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
    public void shouldReturnTrueWhenCourtIsHighCourt() {
        CaseDetails preparedCaseDetails = defaultConsentedCaseDetails();
        preparedCaseDetails.getData().put(HIGHCOURT_COURTLIST, "highcourt");
        boolean isHighCourt = documentHelper.isHighCourtSelected(preparedCaseDetails.getData());
        assertTrue(isHighCourt);
    }

    @Test
    public void shouldReturnTrueWhenCourtIsHighCourtInFinremCaseData() {
        FinremCaseDetails preparedCaseDetails = defaultConsentedFinremCaseDetails();
        preparedCaseDetails.getData().getRegionWrapper().getDefaultRegionWrapper().setRegionList(Region.HIGHCOURT);
        boolean isHighCourt = documentHelper.isHighCourtSelected(preparedCaseDetails.getData());
        assertTrue(isHighCourt);
    }

    @Test
    public void shouldReturnHighCourtStampWhenCourtIsHighCourt() {
        CaseDetails preparedCaseDetails = defaultConsentedCaseDetails();
        preparedCaseDetails.getData().put(HIGHCOURT_COURTLIST, "highcourt");
        StampType actualStampType = documentHelper.getStampType(preparedCaseDetails.getData());
        assertEquals(StampType.HIGH_COURT_STAMP, actualStampType);
    }

    @Test
    public void shouldReturnFamilyCourtStampWhenCourtIsLondon() {
        CaseDetails preparedCaseDetails = defaultConsentedCaseDetails();
        preparedCaseDetails.getData().put(LONDON_COURTLIST, "london");
        StampType actualStampType = documentHelper.getStampType(preparedCaseDetails.getData());
        assertEquals(StampType.FAMILY_COURT_STAMP, actualStampType);
    }

    @Test
    public void shouldReturnHighCourtStampWhenCourtIsHighCourtInFinremCaseData() {
        FinremCaseDetails preparedCaseDetails = defaultConsentedFinremCaseDetails();
        preparedCaseDetails.getData().getRegionWrapper().getDefaultRegionWrapper().setRegionList(Region.HIGHCOURT);
        StampType actualStampType = documentHelper.getStampType(preparedCaseDetails.getData());
        assertEquals(StampType.HIGH_COURT_STAMP, actualStampType);
    }

    @Test
    public void shouldReturnFamilyCourtStampWhenCourtIsLondonInFinremCaseData() {
        FinremCaseDetails preparedCaseDetails = defaultConsentedFinremCaseDetails();
        preparedCaseDetails.getData().getRegionWrapper().getDefaultRegionWrapper().setRegionList(Region.LONDON);
        StampType actualStampType = documentHelper.getStampType(preparedCaseDetails.getData());
        assertEquals(StampType.FAMILY_COURT_STAMP, actualStampType);
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

    @Test
    public void convertToCaseDocumentIfObjNotNull() throws Exception {
        CallbackRequest callbackRequest = prepareCallbackRequestForLatestConsentedConsentOrder("draft-consent-order.json");
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();
        CaseDocument caseDocument = documentHelper.convertToCaseDocumentIfObjNotNull(data.get(CONSENT_ORDER));

        assertThat(caseDocument.getDocumentBinaryUrl(), is("http://file1.binary"));
        assertThat(caseDocument.getDocumentUrl(), is("http://file1"));
        assertThat(caseDocument.getDocumentFilename(), is("file1"));
    }

    @Test
    public void convertToCaseDocumentIfObjNotNullIfNullReturnNull() throws Exception {
        CallbackRequest callbackRequest = prepareCallbackRequestForLatestConsentedConsentOrder("draft-consent-order.json");
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();
        CaseDocument caseDocument = documentHelper.convertToCaseDocumentIfObjNotNull(data.get(GENERAL_ORDER_PREVIEW_DOCUMENT));
        assertNull(caseDocument);
    }

    @Test
    public void convertToCaseDocument() throws Exception {
        CallbackRequest callbackRequest = prepareCallbackRequestForLatestConsentedConsentOrder("draft-consent-order.json");
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();
        CaseDocument caseDocument = documentHelper.convertToCaseDocument(data.get(CONSENT_ORDER), CaseDocument.class);

        assertThat(caseDocument.getDocumentBinaryUrl(), is("http://file1.binary"));
        assertThat(caseDocument.getDocumentUrl(), is("http://file1"));
        assertThat(caseDocument.getDocumentFilename(), is("file1"));
    }

    @Test
    public void getIntervenerOnePaperNotificationRecipient() {
        IntervenerOneWrapper intervenerOneWrapper = IntervenerOneWrapper.builder().build();
        DocumentHelper.PaperNotificationRecipient recipient = DocumentHelper.getIntervenerPaperNotificationRecipient(intervenerOneWrapper);
        assertThat(recipient, is(INTERVENER_ONE));
    }

    @Test
    public void getIntervenerTwoPaperNotificationRecipient() {
        IntervenerTwoWrapper intervenerTwoWrapper = IntervenerTwoWrapper.builder().build();
        DocumentHelper.PaperNotificationRecipient recipient = DocumentHelper.getIntervenerPaperNotificationRecipient(intervenerTwoWrapper);
        assertThat(recipient, is(INTERVENER_TWO));
    }

    @Test
    public void getIntervenerThreePaperNotificationRecipient() {
        IntervenerThreeWrapper intervenerThreeWrapper = IntervenerThreeWrapper.builder().build();
        DocumentHelper.PaperNotificationRecipient recipient = DocumentHelper.getIntervenerPaperNotificationRecipient(intervenerThreeWrapper);
        assertThat(recipient, is(INTERVENER_THREE));
    }

    @Test
    public void getIntervenerFourPaperNotificationRecipient() {
        IntervenerFourWrapper intervenerFourWrapper = IntervenerFourWrapper.builder().build();
        DocumentHelper.PaperNotificationRecipient recipient = DocumentHelper.getIntervenerPaperNotificationRecipient(intervenerFourWrapper);
        assertThat(recipient, is(INTERVENER_FOUR));
    }

    private FinremCallbackRequest buildCallbackRequest() {
        return FinremCallbackRequest.builder()
            .caseDetailsBefore(FinremCaseDetails.builder().id(123L).caseType(CONTESTED).data(new FinremCaseData()).build())
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONTESTED).data(new FinremCaseData()).build())
            .build();
    }
}
