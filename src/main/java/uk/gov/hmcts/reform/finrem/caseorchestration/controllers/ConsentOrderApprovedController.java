package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionDocumentData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderApprovedDocumentService;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse.builder;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.*;

@RestController
@RequestMapping(value = "/case-orchestration")
@Slf4j
public class ConsentOrderApprovedController implements BaseController {

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private ConsentOrderApprovedDocumentService service;

    @PostMapping(path = "/documents/consent-order-approved", consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Handles Consent order approved generation. Serves as a callback from CCD")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Callback was processed successFully or in case of an error message is "
                    + "attached to the case",
                    response = AboutToStartOrSubmitCallbackResponse.class),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })

    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> consentOrderApproved(
            @RequestHeader(value = "Authorization") String authorisationToken,
            @NotNull @RequestBody @ApiParam("Callback") CallbackRequest callback) {

        log.info("Received request for approved order generation. Auth token: {}, Case request : {}",
                authorisationToken, callback);

        validateCaseData(callback);

        Map<String, Object> caseData = callback.getCaseDetails().getData();
        List<PensionDocumentData> pensionDocuments = getPensionDocuments(caseData);

        if (!isEmpty(pensionDocuments)) {
            List<PensionDocumentData> stampedPensionList = service.stampDocument(pensionDocuments, authorisationToken);
            caseData.put(PENSION_COLLECTION_STAMPED, stampedPensionList);
        }

        return ResponseEntity.ok(builder().data(callback.getCaseDetails().getData()).build());
    }

    private List<PensionDocumentData> getPensionDocuments(Map<String, Object> caseData) {
        return mapper.convertValue(caseData.get(PENSION_COLLECTION),
                new TypeReference<List<PensionDocumentData>>() {
                });
    }
}