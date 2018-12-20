package uk.gov.hmcts.reform.finrem.caseorchestration;

import com.google.common.collect.ImmutableMap;
import feign.FeignException;
import feign.Response;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;

public class SetUpUtils {

    public static  final int STATUS_CODE = HttpStatus.INTERNAL_SERVER_ERROR.value();
    public static final String URL = "http://test/file";
    public static final String BINARY_URL = URL + "/binary";
    public static final String FILE_NAME = "doc_name";

    public static final String CREATED_ON = "2nd October";
    public static final String MIME_TYPE = "app/text";

    public static FeignException feignError() {
        Response response = Response.builder().status(STATUS_CODE).headers(ImmutableMap.of()).build();
        return FeignException.errorStatus("test", response);
    }

    public static CaseDocument caseDocument(String url, String binaryUrl) {
        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setDocumentUrl(url);
        caseDocument.setDocumentFilename(FILE_NAME);
        caseDocument.setDocumentBinaryUrl(binaryUrl);

        return caseDocument;
    }

    public static CaseDocument caseDocument() {
        return caseDocument(URL, BINARY_URL);
    }

    public static CaseData caseDataWithMiniFormA() {
        CaseData caseData = new CaseData();
        caseData.setMiniFormA(caseDocument());

        return caseData;
    }

    public static Document document() {
        Document document = new Document();
        document.setBinaryUrl(BINARY_URL);
        document.setCreatedOn(CREATED_ON);
        document.setFileName(FILE_NAME);
        document.setMimeType(MIME_TYPE);
        document.setUrl(URL);

        return document;
    }
}
