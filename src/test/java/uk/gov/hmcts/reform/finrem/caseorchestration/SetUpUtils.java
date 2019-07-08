package uk.gov.hmcts.reform.finrem.caseorchestration;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import feign.FeignException;
import feign.Response;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.InvalidCaseDataException;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.NoSuchFieldExistsException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ApplicationType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentOrderData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralLetter;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralLetterData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.fee.FeeResponse;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ApplicationType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_LETTER;

public class SetUpUtils {

    public static final String AUTH_TOKEN = "Bearer eyJhbGJbpjciOiJIUzI1NiJ9";
    public static final String DOC_URL = "http://dm-store/lhjbyuivu87y989hijbb";
    public static final String BINARY_URL = DOC_URL + "/binary";
    public static final String FILE_NAME = "app_docs.pdf";
    public static final String REJECTED_ORDER_TYPE = "General Order";

    public static  final int INTERNAL_SERVER_ERROR = HttpStatus.INTERNAL_SERVER_ERROR.value();
    public static  final int BAD_REQUEST = HttpStatus.BAD_REQUEST.value();

    public static FeignException feignError() {
        Response response = Response.builder().status(INTERNAL_SERVER_ERROR).headers(ImmutableMap.of()).build();
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
        caseData.put("uploadOrder", ImmutableList.of(consentOrderData(uploadOrderId)));
        return caseData;
    }

    public static Map<String, Object> generalLetterDataMap() {
        return ImmutableMap.of(GENERAL_LETTER, ImmutableList.of(generalLetterData()));
    }

    private static GeneralLetterData generalLetterData() {
        GeneralLetter generalLetter = new GeneralLetter();
        generalLetter.setGeneratedLetter(caseDocument());

        GeneralLetterData generalLetterData = new GeneralLetterData();
        generalLetterData.setId(UUID.randomUUID().toString());
        generalLetterData.setGeneralLetter(generalLetter);

        return generalLetterData;
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
        document.setCreatedOn("22 Oct 2018");
        document.setFileName(FILE_NAME);
        document.setMimeType("application/pdf");
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

    public static void doCaseDocumentAssert(CaseDocument result) {
        assertThat(result.getDocumentFilename(), is(FILE_NAME));
        assertThat(result.getDocumentUrl(), is(DOC_URL));
        assertThat(result.getDocumentBinaryUrl(), is(BINARY_URL));
    }
}
