package uk.gov.hmcts.reform.finrem.caseorchestration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import feign.FeignException;
import feign.Request;
import feign.Response;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.InvalidCaseDataException;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.NoSuchFieldExistsException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ApplicationType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OldCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.TypedCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ClientDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentGenerationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.fee.FeeResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.serialisation.FinremCallbackRequestDeserializer;
import uk.gov.hmcts.reform.finrem.ccd.domain.Address;
import uk.gov.hmcts.reform.finrem.ccd.domain.BristolCourt;
import uk.gov.hmcts.reform.finrem.ccd.domain.CaseType;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.NatureApplication;
import uk.gov.hmcts.reform.finrem.ccd.domain.NottinghamCourt;
import uk.gov.hmcts.reform.finrem.ccd.domain.PaymentDocument;
import uk.gov.hmcts.reform.finrem.ccd.domain.PaymentDocumentCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.PaymentDocumentType;
import uk.gov.hmcts.reform.finrem.ccd.domain.PensionDocumentType;
import uk.gov.hmcts.reform.finrem.ccd.domain.PensionType;
import uk.gov.hmcts.reform.finrem.ccd.domain.PensionTypeCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.Region;
import uk.gov.hmcts.reform.finrem.ccd.domain.RegionMidlandsFrc;
import uk.gov.hmcts.reform.finrem.ccd.domain.UploadOrder;
import uk.gov.hmcts.reform.finrem.ccd.domain.UploadOrderCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.UploadOrderDocumentType;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.argThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CASE_TYPE_ID_CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CASE_TYPE_ID_CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ApplicationType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIDLANDS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIDLANDS_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTTINGHAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTTINGHAM_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REGION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_ADDRESS;

public class TestSetUpUtils {

    public static final String DOC_URL = "http://dm-store/lhjbyuivu87y989hijbb";
    public static final String BINARY_URL = DOC_URL + "/binary";
    public static final String FILE_NAME = "app_docs.pdf";
    public static final String VARIATION_FILE_NAME = "ApprovedVariationOrderLetter.pdf";
    public static final String INTE_DOC_URL = "http://dm-store/documents/e9ca7c4a-1f75-4b46-b0dc-744abc2dc0d3";
    public static final String INTE_BINARY_URL = INTE_DOC_URL + "/binary";
    public static final String INTE_FILE_NAME = "dummy1.pdf";
    public static final String REJECTED_ORDER_TYPE = "General Order";
    public static final String PENSION_TYPE = "PPF1";
    public static final String PENSION_ID = "1";

    public static final int INTERNAL_SERVER_ERROR = HttpStatus.INTERNAL_SERVER_ERROR.value();
    public static final int BAD_REQUEST = HttpStatus.BAD_REQUEST.value();

    public static FeignException feignError() {
        Response response = Response.builder().status(INTERNAL_SERVER_ERROR)
            .headers(ImmutableMap.of())
            .request(Request.create(Request.HttpMethod.GET, "", ImmutableMap.of(), Request.Body.empty(), null))
            .build();
        return FeignException.errorStatus("test", response);
    }

    public static InvalidCaseDataException invalidCaseDataError() {
        return new InvalidCaseDataException(BAD_REQUEST, "Bad request");
    }

    public static NoSuchFieldExistsException noSuchFieldExistsCaseDataError() {
        return new NoSuchFieldExistsException("Field Does not exists");
    }

    public static HttpServerErrorException httpServerError() {
        return new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static FeeResponse fee(ApplicationType applicationType) {
        FeeResponse feeResponse = new FeeResponse();
        feeResponse.setCode("FEE0640");
        feeResponse.setDescription("finrem");
        feeResponse.setFeeAmount(applicationType == CONSENTED ? BigDecimal.valueOf(10) : BigDecimal.valueOf(255));
        feeResponse.setVersion("v1");
        return feeResponse;
    }

    public static FinremCaseData finremCaseDataWithUploadOrder() {
        FinremCaseData caseData = new FinremCaseData();
        caseData.setUploadOrder(List.of(uploadOrderData()));
        return caseData;
    }

    public static FinremCaseData finremCaseDataWithPreviewOrder() {
        FinremCaseData caseData = new FinremCaseData();
        caseData.setOrderRefusalPreviewDocument(newDocument());
        return caseData;
    }

    public static FinremCaseData finremCaseDataWithRefusalOrder() {
        FinremCaseData caseData = new FinremCaseData();
        caseData.setRefusalOrderPreviewDocument(newDocument());
        return caseData;
    }

    private static UploadOrderCollection uploadOrderData() {
        return UploadOrderCollection.builder()
            .value(UploadOrder.builder()
                .documentType(UploadOrderDocumentType.GENERAL_ORDER)
                .documentLink(newDocument())
                .documentDateAdded(LocalDate.now())
                .build())
            .build();
    }

    public static Document document() {
        Document document = new Document();
        document.setBinaryUrl(BINARY_URL);
        document.setFileName(FILE_NAME);
        document.setUrl(DOC_URL);

        return document;
    }

    public static uk.gov.hmcts.reform.finrem.ccd.domain.Document variationDocument() {
        uk.gov.hmcts.reform.finrem.ccd.domain.Document document = new uk.gov.hmcts.reform.finrem.ccd.domain.Document();
        document.setBinaryUrl(BINARY_URL);
        document.setFilename(VARIATION_FILE_NAME);
        document.setUrl(DOC_URL);
        return document;
    }

    public static ClientDocument variationDocumentClientDocument() {
        ClientDocument document = new ClientDocument();
        document.setBinaryUrl(BINARY_URL);
        document.setFileName(VARIATION_FILE_NAME);
        document.setUrl(DOC_URL);
        return document;
    }

    public static CaseDocument caseDocument() {
        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setDocumentUrl(DOC_URL);
        caseDocument.setDocumentFilename(FILE_NAME);
        caseDocument.setDocumentBinaryUrl(BINARY_URL);

        return caseDocument;
    }

    public static CaseDocument caseDocument(String documentName, String filename, String binaryUrl) {
        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setDocumentUrl(documentName);
        caseDocument.setDocumentFilename(filename);
        caseDocument.setDocumentBinaryUrl(binaryUrl);

        return caseDocument;
    }

    public static TypedCaseDocument pensionDocument() {
        TypedCaseDocument document = new TypedCaseDocument();
        document.setPensionDocument(caseDocument());
        document.setTypeOfDocument(PENSION_TYPE);

        return document;
    }

    public static PensionType pensionType() {
        return PensionType.builder()
            .uploadedDocument(uk.gov.hmcts.reform.finrem.ccd.domain.Document.builder()
                .url(DOC_URL)
                .filename(FILE_NAME)
                .binaryUrl(BINARY_URL)
                .build())
            .typeOfDocument(PensionDocumentType.FORM_PPF1)
            .build();
    }

    public static PensionCollectionData pensionDocumentData() {
        PensionCollectionData document = new PensionCollectionData();
        document.setTypedCaseDocument(pensionDocument());
        document.setId(PENSION_ID);

        return document;
    }

    public static PaymentDocumentCollection paymentDocumentData() {
        return PaymentDocumentCollection.builder().value(
            PaymentDocument.builder()
                .typeOfDocument(PaymentDocumentType.COPY_OF_PAPER_FORM_A)
                .uploadedDocument(newDocument())
                .build()
        ).build();
    }

    public static PensionTypeCollection pensionTypeCollection() {
        return PensionTypeCollection.builder()
            .value(pensionType())
            .build();
    }

    public static void assertCaseDocument(CaseDocument caseDocument) {
        assertCaseDocument(caseDocument, BINARY_URL);
    }

    public static void assertCaseDocument(CaseDocument caseDocument, String binaryUrl) {
        assertThat(caseDocument.getDocumentFilename(), is(FILE_NAME));
        assertThat(caseDocument.getDocumentUrl(), is(DOC_URL));
        assertThat(caseDocument.getDocumentBinaryUrl(), is(binaryUrl));
    }

    public static void assertCaseDocument(uk.gov.hmcts.reform.finrem.ccd.domain.Document caseDocument) {
        assertCaseDocument(caseDocument, BINARY_URL);
    }

    public static void assertCaseDocument(uk.gov.hmcts.reform.finrem.ccd.domain.Document caseDocument, String binaryUrl) {
        assertThat(caseDocument.getFilename(), is(FILE_NAME));
        assertThat(caseDocument.getUrl(), is(DOC_URL));
        assertThat(caseDocument.getBinaryUrl(), is(binaryUrl));
    }

    @Deprecated
    public static CaseDetails defaultConsentedCaseDetails() {
        Map<String, Object> caseData = new HashMap<>();
        List<String> natureOfApplication =  List.of("Lump Sum Order",
            "Periodical Payment Order",
            "Pension Sharing Order",
            "Pension Attachment Order",
            "Pension Compensation Sharing Order",
            "Pension Compensation Attachment Order",
            "A settlement or a transfer of property",
            "Property Adjustment Order");
        caseData.put("natureOfApplication2", natureOfApplication);
        populateApplicantNameAndAddress(caseData);
        populateRespondentNameAndAddressConsented(caseData);

        return CaseDetails.builder()
            .caseTypeId(CASE_TYPE_ID_CONSENTED)
            .id(123456789L)
            .data(caseData)
            .build();
    }

    public static FinremCaseDetails defaultConsentedCaseDetailsForVariationOrder() {
        FinremCaseData caseData = new FinremCaseData();
        List<NatureApplication> natureOfApplication =  List.of(NatureApplication.LUMP_SUM_ORDER,
            NatureApplication.PERIODICAL_PAYMENT_ORDER,
            NatureApplication.PENSION_SHARING_ORDER,
            NatureApplication.PENSION_ATTACHMENT_ORDER,
            NatureApplication.PENSION_COMPENSATION_SHARING_ORDER,
            NatureApplication.PENSION_COMPENSATION_ATTACHMENT_ORDER,
            NatureApplication.A_SETTLEMENT_OR_A_TRANSFER_OF_PROPERTY,
            NatureApplication.VARIATION_ORDER,
            NatureApplication.PROPERTY_ADJUSTMENT_ORDER);
        caseData.getNatureApplicationWrapper().setNatureOfApplication2(natureOfApplication);
        populateApplicantNameAndAddress(caseData);
        populateRespondentNameAndAddressConsented(caseData);

        return FinremCaseDetails.builder()
            .caseType(CaseType.CONSENTED)
            .id(123456789L)
            .caseData(caseData)
            .build();
    }

    public static FinremCaseDetails defaultConsentedFinremCaseDetails() {
        FinremCaseData caseData = new FinremCaseData();
        caseData.setCcdCaseType(CaseType.CONSENTED);
        caseData.getRegionWrapper().getDefaultRegionWrapper().getDefaultCourtListWrapper()
            .setBristolCourtList(BristolCourt.FR_bristolList_1);
        populateApplicantNameAndAddress(caseData);
        populateRespondentNameAndAddressConsented(caseData);

        return FinremCaseDetails.builder()
            .caseType(CaseType.CONSENTED)
            .id(123456789L)
            .caseData(caseData)
            .build();
    }

    public static CaseDetails defaultContestedCaseDetails() {
        Map<String, Object> caseData = new HashMap<>();
        List<String> natureOfApplication =  List.of("Lump Sum Order",
            "Periodical Payment Order",
            "Pension Sharing Order",
            "Pension Attachment Order",
            "Pension Compensation Sharing Order",
            "Pension Compensation Attachment Order",
            "A settlement or a transfer of property",
            "Property Adjustment Order");
        caseData.put("natureOfApplication2", natureOfApplication);
        populateApplicantNameAndAddress(caseData);
        populateRespondentNameAndAddressContested(caseData);
        populateCourtDetails(caseData);

        return CaseDetails.builder()
            .caseTypeId(CASE_TYPE_ID_CONTESTED)
            .id(987654321L)
            .data(caseData)
            .build();
    }

    public static FinremCaseDetails defaultContestedFinremCaseDetails() {
        FinremCaseData caseData = new FinremCaseData();
        caseData.setCcdCaseType(CaseType.CONTESTED);
        populateApplicantNameAndAddress(caseData);
        populateRespondentNameAndAddressContested(caseData);
        populateCourtDetails(caseData);

        return FinremCaseDetails.builder()
            .caseType(CaseType.CONTESTED)
            .id(987654321L)
            .caseData(caseData)
            .build();
    }

    private static void populateApplicantNameAndAddress(Map<String, Object> caseData) {
        Map<String, Object> applicantAddress = new HashMap<>();
        applicantAddress.put("AddressLine1", "50 Applicant Street");
        applicantAddress.put("AddressLine2", "Second Address Line");
        applicantAddress.put("AddressLine3", "Third Address Line");
        applicantAddress.put("County", "London");
        applicantAddress.put("Country", "England");
        applicantAddress.put("PostTown", "London");
        applicantAddress.put("PostCode", "SW1");

        caseData.put(APPLICANT_FIRST_MIDDLE_NAME, "James");
        caseData.put(APPLICANT_LAST_NAME, "Joyce");
        caseData.put(APPLICANT_ADDRESS, applicantAddress);
        caseData.put(APPLICANT_REPRESENTED, null);
    }

    private static void populateApplicantNameAndAddress(FinremCaseData caseData) {
        Address applicantAddress = Address.builder()
            .addressLine1("50 Applicant Street")
            .addressLine2("Second Address Line")
            .addressLine3("Third Address Line")
            .county("London")
            .country("England")
            .postTown("London")
            .postCode("SW1")
            .build();
        caseData.getContactDetailsWrapper().setApplicantFmName("James");
        caseData.getContactDetailsWrapper().setApplicantLname("Joyce");
        caseData.getContactDetailsWrapper().setApplicantAddress(applicantAddress);
        caseData.getContactDetailsWrapper().setApplicantRepresented(null);
    }

    private static void populateCourtDetails(Map<String, Object> caseData) {
        caseData.put(REGION, MIDLANDS);
        caseData.put(MIDLANDS_FRC_LIST, NOTTINGHAM);
        caseData.put(NOTTINGHAM_COURTLIST, "FR_s_NottinghamList_7");
    }

    private static void populateCourtDetails(FinremCaseData caseData) {
        caseData.getRegionWrapper().getDefaultRegionWrapper().setRegionList(Region.MIDLANDS);
        caseData.getRegionWrapper().getDefaultRegionWrapper().setMidlandsFrcList(RegionMidlandsFrc.NOTTINGHAM);
        caseData.getRegionWrapper().getDefaultCourtList().setNottinghamCourtList(NottinghamCourt.MANSFIELD_MAGISTRATES_AND_COUNTY_COURT);
    }

    public static CaseDetails caseDetailsFromResource(String resourcePath, ObjectMapper mapper) {
        try (InputStream resourceAsStream = TestSetUpUtils.class.getResourceAsStream(resourcePath)) {
            return mapper.readValue(resourceAsStream, OldCallbackRequest.class).getCaseDetails();
        } catch (Exception exception) {
            throw new IllegalStateException(exception.getMessage(), exception);
        }
    }

    public static FinremCaseDetails finremCaseDetailsFromResource(String json, ObjectMapper mapper) {
        FinremCallbackRequestDeserializer deserializer = new FinremCallbackRequestDeserializer(mapper);
        return deserializer.deserialize(json).getCaseDetails();
    }

    private static void populateRespondentNameAndAddressConsented(Map<String, Object> caseData) {
        Map<String, Object> respondentAddress = new HashMap<>();
        respondentAddress.put("AddressLine1", "50 Respondent Street");
        respondentAddress.put("AddressLine2", "Consented");
        respondentAddress.put("AddressLine3", "Third Address Line");
        respondentAddress.put("County", "London");
        respondentAddress.put("Country", "England");
        respondentAddress.put("PostTown", "London");
        respondentAddress.put("PostCode", "SW1");

        caseData.put(CONSENTED_RESPONDENT_FIRST_MIDDLE_NAME, "Jane");
        caseData.put(CONSENTED_RESPONDENT_LAST_NAME, "Doe");
        caseData.put(RESPONDENT_ADDRESS, respondentAddress);
        caseData.put(CONSENTED_RESPONDENT_REPRESENTED, null);
    }

    private static void populateRespondentNameAndAddressConsented(FinremCaseData caseData) {
        Address respondentAddress = Address.builder()
            .addressLine1("50 Respondent Street")
            .addressLine2("Consented")
            .addressLine3("Third Address Line")
            .county("London")
            .country("England")
            .postTown("London")
            .postCode("SW1")
            .build();

        caseData.getContactDetailsWrapper().setAppRespondentFmName("Jane");
        caseData.getContactDetailsWrapper().setAppRespondentLName("Doe");
        caseData.getContactDetailsWrapper().setRespondentAddress(respondentAddress);
        caseData.getContactDetailsWrapper().setConsentedRespondentRepresented(null);
    }

    private static void populateRespondentNameAndAddressContested(Map<String, Object> caseData) {
        Map<String, Object> respondentAddress = new HashMap<>();
        respondentAddress.put("AddressLine1", "50 Respondent Street");
        respondentAddress.put("AddressLine2", "Contested");
        respondentAddress.put("AddressLine3", "Third Address Line");
        respondentAddress.put("County", "London");
        respondentAddress.put("Country", "England");
        respondentAddress.put("PostTown", "London");
        respondentAddress.put("PostCode", "SW1");

        caseData.put(CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME, "Jane");
        caseData.put(CONTESTED_RESPONDENT_LAST_NAME, "Doe");
        caseData.put(RESPONDENT_ADDRESS, respondentAddress);
        caseData.put(CONTESTED_RESPONDENT_REPRESENTED, null);
    }

    private static void populateRespondentNameAndAddressContested(FinremCaseData caseData) {
        Address respondentAddress = Address.builder()
            .addressLine1("50 Respondent Street")
            .addressLine2("Contested")
            .addressLine3("Third Address Line")
            .county("London")
            .country("England")
            .postTown("London")
            .postCode("SW1")
            .build();

        caseData.getContactDetailsWrapper().setRespondentFmName("Jane");
        caseData.getContactDetailsWrapper().setRespondentLname("Doe");
        caseData.getContactDetailsWrapper().setRespondentAddress(respondentAddress);
        caseData.getContactDetailsWrapper().setContestedRespondentRepresented(null);
    }

    public static DocumentGenerationRequest matchDocumentGenerationRequestTemplateAndFilename(String template, String filename) {
        return argThat(
            documentGenerationRequest -> documentGenerationRequest != null
                && template.equals(documentGenerationRequest.getTemplate())
                && filename.equals(documentGenerationRequest.getFileName()));
    }

    public static List<BulkPrintDocument> bulkPrintDocumentList() {
        List<BulkPrintDocument> bulkPrintDocuments = new ArrayList<>();
        bulkPrintDocuments.add(BulkPrintDocument.builder().binaryFileUrl("http://dm-store-aat.service.core-compute-aat.internal/documents/967103ad-0b95-4f0f-9712-4bf5770fb196/binary").build());
        return bulkPrintDocuments;
    }

    public static Map<String, Object> caseDataWithUploadHearingOrder() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("uploadHearingOrderRO", new ArrayList<>() {
            {
                add(new CaseDocument());
                add(new CaseDocument());
            }
        });
        caseData.put("uploadHearingOrder", new ArrayList<>() {
            {
                add(new CaseDocument());
            }
        });
        return caseData;
    }

    public static uk.gov.hmcts.reform.finrem.ccd.domain.Document newDocument(String documentName,
                                                                             String filename,
                                                                             String binaryUrl) {
        return uk.gov.hmcts.reform.finrem.ccd.domain.Document.builder()
            .filename(filename)
            .url(documentName)
            .binaryUrl(binaryUrl)
            .build();
    }

    public static uk.gov.hmcts.reform.finrem.ccd.domain.Document newDocument() {
        uk.gov.hmcts.reform.finrem.ccd.domain.Document caseDocument =
            new uk.gov.hmcts.reform.finrem.ccd.domain.Document();
        caseDocument.setUrl(DOC_URL);
        caseDocument.setFilename(FILE_NAME);
        caseDocument.setBinaryUrl(BINARY_URL);

        return caseDocument;
    }

    public static ClientDocument newDocumentClientDocument() {
        ClientDocument caseDocument =
            new ClientDocument();
        caseDocument.setUrl(DOC_URL);
        caseDocument.setFileName(FILE_NAME);
        caseDocument.setBinaryUrl(BINARY_URL);

        return caseDocument;
    }

    public static uk.gov.hmcts.reform.finrem.ccd.domain.Document wordDoc() {
        uk.gov.hmcts.reform.finrem.ccd.domain.Document caseDocument =
            new uk.gov.hmcts.reform.finrem.ccd.domain.Document();
        caseDocument.setUrl(DOC_URL);
        caseDocument.setFilename("doc.docx");
        caseDocument.setBinaryUrl(BINARY_URL);

        return caseDocument;
    }

    public static ClientDocument docClientWordDocument() {
        return ClientDocument.builder()
            .url(DOC_URL)
            .fileName("doc.docx")
            .binaryUrl(BINARY_URL)
            .build();
    }
}
