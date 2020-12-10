package uk.gov.hmcts.reform.finrem.caseorchestration.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.evidence.FileUploadResponse;

import java.util.List;

@FeignClient(name = "finrem-evidence-management-client", url = "${evidence.management.client.api.baseurl}")
public interface EvidenceManagementClient {

    @GetMapping(path = "/version/1/audit")
    List<FileUploadResponse> auditFileUrls(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationToken,
        @RequestParam("fileUrls") List<String> fileUrls);
}
