package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EvidenceFile;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.evidence.FileUploadResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.EvidenceManagementUploadService;

import java.util.List;

@RestController
@RequestMapping(path = "/case-orchestration/emclientapi")
@Validated
@RequiredArgsConstructor
public class EvidenceManagementClientController {

    private final EvidenceManagementUploadService evidenceManagementUploadService;


    @Operation(summary = "Handles file upload to Evidence Management Document Store")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Files uploaded successfully"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @PostMapping(value = "/upload", produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public List<FileUploadResponse> upload(
        @RequestHeader(value = "Authorization", required = false) String authorizationToken,
        @RequestHeader(value = "requestId", required = false) String requestId,
        @RequestParam("file") List<@EvidenceFile MultipartFile> files) {

        return evidenceManagementUploadService.upload(files, authorizationToken, requestId);
    }
}
