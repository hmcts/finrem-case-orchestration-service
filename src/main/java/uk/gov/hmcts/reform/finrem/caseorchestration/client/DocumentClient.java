package uk.gov.hmcts.reform.finrem.caseorchestration.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentRequest;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@FeignClient(name = "document-client", url = "${document.generator.service.api.baseurl}")
public interface DocumentClient {

    @PostMapping(
            path = "/version/1/generatePDF",
            headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE
    )
    Document generatePDF(
            @RequestBody DocumentRequest generateDocumentRequest,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationToken);

    @PostMapping(
        path = "/version/1/bulk-print",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE
    )
    void bulkPrint(
        @RequestBody BulkPrintRequest bulkPrintRequest);

    @DeleteMapping(path = "/version/1/delete-pdf-document")
    void deleteDocument(
            @RequestParam("fileUrl") String fileUrl,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationToken);
}
