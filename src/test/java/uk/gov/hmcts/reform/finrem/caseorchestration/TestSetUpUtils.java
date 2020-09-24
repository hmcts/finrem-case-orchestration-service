package uk.gov.hmcts.reform.finrem.caseorchestration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import feign.FeignException;
import feign.Request;
import feign.Response;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.InvalidCaseDataException;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.NoSuchFieldExistsException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ApplicationType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentOrderData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralLetter;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralLetterData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.TypedCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentGenerationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.fee.FeeResponse;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_APPLICATION_NOT_APPROVED_PREVIEW_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_LETTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_PREVIEW_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIDLANDS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIDLANDS_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTTINGHAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTTINGHAM_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORDER_REFUSAL_PREVIEW_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REGION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.UPLOAD_ORDER;

public class TestSetUpUtils {

    public static final String DOC_URL = "http://dm-store/lhjbyuivu87y989hijbb";
    public static final String BINARY_URL = DOC_URL + "/binary";
    public static final String FILE_NAME = "app_docs.pdf";
    public static final String REJECTED_ORDER_TYPE = "General Order";
    public static final String PENSION_TYPE = "PPF1";
    public static final String PENSION_ID = "1";

    public static  final int INTERNAL_SERVER_ERROR = HttpStatus.INTERNAL_SERVER_ERROR.value();
    public static  final int BAD_REQUEST = HttpStatus.BAD_REQUEST.value();

    public static FeignException feignError() {
        Response response = Response.builder().status(INTERNAL_SERVER_ERROR)
            .headers(ImmutableMap.of())
            .request(Request.create(Request.HttpMethod.GET, "", ImmutableMap.of(), Request.Body.empty()))
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

    public static Map<String, Object> caseDataWithUploadOrder(String uploadOrderId) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(UPLOAD_ORDER, ImmutableList.of(consentOrderData(uploadOrderId)));
        return caseData;
    }

    public static Map<String, Object> caseDataWithPreviewOrder() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(ORDER_REFUSAL_PREVIEW_COLLECTION, caseDocument());
        return caseData;
    }

    public static Map<String, Object> generalLetterDataMap() {
        return ImmutableMap.of(GENERAL_LETTER, ImmutableList.of(generalLetterData()));
    }

    public static Map<String, Object> caseDataWithGeneralOrder() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(GENERAL_ORDER_PREVIEW_DOCUMENT, caseDocument());
        return caseData;
    }

    private static GeneralLetterData generalLetterData() {
        GeneralLetter generalLetter = new GeneralLetter(caseDocument());

        GeneralLetterData generalLetterData = GeneralLetterData.builder()
            .id(UUID.randomUUID().toString())
            .generalLetter(generalLetter)
            .build();

        return generalLetterData;
    }

    public static Map<String, Object> caseDataWithRefusalOrder() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(CONTESTED_APPLICATION_NOT_APPROVED_PREVIEW_DOCUMENT, caseDocument());
        return caseData;
    }

    private static ConsentOrderData consentOrderData(String id) {
        ConsentOrder consentOrder = new ConsentOrder();
        consentOrder.setDocumentType(REJECTED_ORDER_TYPE);
        consentOrder.setDocumentLink(caseDocument());
        consentOrder.setDocumentDateAdded(new Date());

        ConsentOrderData consentOrderData = new ConsentOrderData();
        consentOrderData.setId(id);
        consentOrderData.setConsentOrder(consentOrder);

        return consentOrderData;
    }

    public static Document document() {
        Document document = new Document();
        document.setBinaryUrl(BINARY_URL);
        document.setFileName(FILE_NAME);
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

    public static PensionCollectionData pensionDocumentData() {
        PensionCollectionData document = new PensionCollectionData();
        document.setTypedCaseDocument(pensionDocument());
        document.setId(PENSION_ID);

        return document;
    }

    public static void assertCaseDocument(CaseDocument caseDocument) {
        assertCaseDocument(caseDocument, BINARY_URL);
    }

    public static void assertCaseDocument(CaseDocument caseDocument, String binaryUrl) {
        assertThat(caseDocument.getDocumentFilename(), is(FILE_NAME));
        assertThat(caseDocument.getDocumentUrl(), is(DOC_URL));
        assertThat(caseDocument.getDocumentBinaryUrl(), is(binaryUrl));
    }

    public static CaseDetails defaultConsentedCaseDetails() {
        Map<String, Object> caseData = new HashMap<>();
        populateApplicantNameAndAddress(caseData);
        populateRespondentNameAndAddressConsented(caseData);

        return CaseDetails.builder()
            .caseTypeId(CASE_TYPE_ID_CONSENTED)
            .id(123456789L)
            .data(caseData)
            .build();
    }

    public static CaseDetails defaultContestedCaseDetails() {
        Map<String, Object> caseData = new HashMap<>();
        populateApplicantNameAndAddress(caseData);
        populateRespondentNameAndAddressContested(caseData);
        populateCourtDetails(caseData);

        return CaseDetails.builder()
            .caseTypeId(CASE_TYPE_ID_CONTESTED)
            .id(987654321L)
            .data(caseData)
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

    private static void populateCourtDetails(Map<String, Object> caseData) {
        caseData.put(REGION, MIDLANDS);
        caseData.put(MIDLANDS_FRC_LIST, NOTTINGHAM);
        caseData.put(NOTTINGHAM_COURTLIST, "FR_s_NottinghamList_7");
    }



    public static CaseDetails caseDetailsFromResource(String resourcePath, ObjectMapper mapper) {
        try (InputStream resourceAsStream = TestSetUpUtils.class.getResourceAsStream(resourcePath)) {
            return mapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
        } catch (Exception exception) {
            throw new IllegalStateException(exception.getMessage(), exception);
        }
    }

    public static CaseDetails caseDetailsBeforeFromResource(String resourcePath, ObjectMapper mapper) {
        try (InputStream resourceAsStream = TestSetUpUtils.class.getResourceAsStream(resourcePath)) {
            return mapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetailsBefore();
        } catch (Exception exception) {
            throw new IllegalStateException(exception.getMessage(), exception);
        }
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
}
