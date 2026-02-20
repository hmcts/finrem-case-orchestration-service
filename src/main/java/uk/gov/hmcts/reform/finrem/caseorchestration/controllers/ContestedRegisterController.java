package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseResource;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.financialremedy.ContestedRegisterService;

import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@Slf4j
@RequestMapping(value = "/case-orchestration")
@RequiredArgsConstructor
public class ContestedRegisterController extends BaseController {

    private static final String OK = "OK";

    private final ObjectMapper objectMapper;
    private final ContestedRegisterService contestedRegisterService;

    @Operation(description = "Retrieve case details identified by its case reference.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = OK, content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = CaseResource.class))})
    })
    @GetMapping(path = "/{case-reference}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> getCaseDetails(@PathVariable("case-reference") String caseReference) {
        final CaseResource caseDetails = contestedRegisterService.getCaseDetails(caseReference);

        final Map<String, Object> caseDataMappedValue = objectMapper.convertValue(caseDetails.getData(), new TypeReference<>() {
        });

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseDataMappedValue).build());
    }
}
