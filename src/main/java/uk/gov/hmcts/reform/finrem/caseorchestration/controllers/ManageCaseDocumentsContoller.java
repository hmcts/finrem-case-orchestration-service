package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocumentData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentDetailsData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UploadCaseFilesAboutToSubmitHandler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ALL_DOCUMENTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_CORRESPONDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_APPLICANT_DOCUMENTS_UPLOADED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_CASE_DOCUMENTS_UPLOADED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_DOCUMENTS_UPLOADED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_UPLOADED_DOCUMENTS;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/case-orchestration")
public class ManageCaseDocumentsContoller extends BaseController{

    private final UploadCaseFilesAboutToSubmitHandler uploadCaseFilesAboutToSubmitHandler;
    private final ObjectMapper mapper;

    @PostMapping(path = "/manage-case-documents", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Handles update Contested Case details and cleans up the data fields based on the options chosen for Contested Cases")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
            response = AboutToStartOrSubmitCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> manageCaseDocuments(
        @RequestHeader(value = AUTHORIZATION_HEADER, required = false)
        @RequestBody CallbackRequest ccdRequest) {

        validateCaseData(ccdRequest);

        CaseDetails caseDetails = ccdRequest.getCaseDetails();
        Long caseId = caseDetails.getId();
        log.info("Received request to upload Contested case documents for Case ID: {}", caseId);

        AboutToStartOrSubmitCallbackResponse response =
            uploadCaseFilesAboutToSubmitHandler.handle(caseDetails.getData());

        caseDetails.getData().put(CONTESTED_APPLICANT_DOCUMENTS_UPLOADED, caseDetails.getData().get(CONTESTED_APPLICANT_DOCUMENTS_UPLOADED));
        caseDetails.getData().put(CONTESTED_RESPONDENT_DOCUMENTS_UPLOADED, caseDetails.getData().get(CONTESTED_RESPONDENT_DOCUMENTS_UPLOADED));
        caseDetails.getData().put(CONTESTED_CASE_DOCUMENTS_UPLOADED, caseDetails.getData().get(CONTESTED_CASE_DOCUMENTS_UPLOADED));

        log.info("Successfully filtered documents to relevant party for Case ID: {}", caseId);

        return ResponseEntity.ok(response);
    }

    @PostMapping(path = "/manage-case-documents-submit", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Handles update Contested Case details and cleans up the data fields based on the options chosen for Contested Cases")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
            response = AboutToStartOrSubmitCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> manageCaseDocumentsSubmit(
        @RequestHeader(value = AUTHORIZATION_HEADER, required = false)
        @RequestBody CallbackRequest ccdRequest) {

        validateCaseData(ccdRequest);

        CaseDetails caseDetails = ccdRequest.getCaseDetails();
        Long caseId = caseDetails.getId();
        log.info("Received request to upload Contested case documents for Case ID: {}", caseId);

        uploadCaseFilesAboutToSubmitHandler.removeDeletedFilesFromCaseData(caseDetails);

        log.info("Successfully filtered documents to relevant party for Case ID: {}", caseId);

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseDetails.getData()).build());
    }
}
