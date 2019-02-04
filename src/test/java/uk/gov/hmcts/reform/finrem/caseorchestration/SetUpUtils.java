package uk.gov.hmcts.reform.finrem.caseorchestration;

import com.google.common.collect.ImmutableMap;
import feign.FeignException;
import feign.Response;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

public class SetUpUtils {

    public static final String AUTH_TOKEN = "Bearer eyJhbGJbpjciOiJIUzI1NiJ9";
    public static final String DOC_URL = "http://test/file";
    public static final String BIN_DOC_URL = DOC_URL + "/binary";
    public static final String DOC_NAME = "doc_name";
    public static final String REJECTED_ORDER_TYPE = "General Order";

    public static  final int STATUS_CODE = HttpStatus.INTERNAL_SERVER_ERROR.value();

    public static FeignException feignError() {
        Response response = Response.builder().status(STATUS_CODE).headers(ImmutableMap.of()).build();
        return FeignException.errorStatus("test", response);
    }

    public static CaseDocument caseDocument() {
        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setDocumentUrl(DOC_URL);
        caseDocument.setDocumentFilename(DOC_NAME);
        caseDocument.setDocumentBinaryUrl(BIN_DOC_URL);

        return caseDocument;
    }
}
