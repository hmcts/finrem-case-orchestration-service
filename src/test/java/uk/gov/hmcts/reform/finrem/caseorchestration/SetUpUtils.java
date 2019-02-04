package uk.gov.hmcts.reform.finrem.caseorchestration;

import com.google.common.collect.ImmutableMap;
import feign.FeignException;
import feign.Response;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentOrderData;

import java.util.Date;
import java.util.UUID;

public class SetUpUtils {

    public static final String AUTH_TOKEN = "Bearer eyJhbGJbpjciOiJIUzI1NiJ9";
    public static final String DOC_URL = "http://dm-store/lhjbyuivu87y989hijbb";
    public static final String BINARY_URL = DOC_URL + "/binary";
    public static final String FILE_NAME = "app_docs.pdf";
    public static final String REJECTED_ORDER_TYPE = "General Order";

    public static  final int STATUS_CODE = HttpStatus.INTERNAL_SERVER_ERROR.value();

    public static FeignException feignError() {
        Response response = Response.builder().status(STATUS_CODE).headers(ImmutableMap.of()).build();
        return FeignException.errorStatus("test", response);
    }

    public static ConsentOrderData consentOrderData(String id) {
        ConsentOrder consentOrder = new ConsentOrder();
        consentOrder.setDocumentType(REJECTED_ORDER_TYPE);
        consentOrder.setDocumentLink(caseDocument());
        consentOrder.setDocumentDateAdded(new Date());

        ConsentOrderData consentOrderData = new ConsentOrderData();
        consentOrderData.setId(id);
        consentOrderData.setConsentOrder(consentOrder);

        return consentOrderData;
    }

    public static CaseDocument caseDocument() {
        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setDocumentUrl(DOC_URL);
        caseDocument.setDocumentFilename(FILE_NAME);
        caseDocument.setDocumentBinaryUrl(BINARY_URL);

        return caseDocument;
    }
}
