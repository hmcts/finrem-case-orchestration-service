package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.datamigration.controller;


import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.MigrationHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.RemoveRespondentSolOrg;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.Rpet164FrcCourtListMigrationImpl;

import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/ccd-data-migration")
@Slf4j
public class CcdDataMigrationController {

    public final RemoveRespondentSolOrg removeRespondentSolOrg;

    @PostMapping(value = "/migrate", consumes = APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
            description = "Callback was processed successfully or in case of an error message is attached to the case",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = CallbackResponse.class))})})
    public CallbackResponse migrate(
        @RequestHeader(value = AUTHORIZATION_HEADER) final String authorisationToken,
        @RequestBody @Parameter(description = "CaseData") final CallbackRequest ccdRequest) {

        CaseDetails caseDetails = ccdRequest.getCaseDetails();
        log.info("FR case migration for removing respondent org policy. Request received for case {}", caseDetails.getId());

        AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder responseBuilder =
            AboutToStartOrSubmitCallbackResponse.builder();

        // Change the below to point to your new migration service and modify controller tests to match
        Map<String, Object> caseData = removeRespondentSolOrg.migrateCaseData(caseDetails.getData());

        if (caseData != null) {
            responseBuilder.data(caseData);
        }

        return responseBuilder.build();
    }

    @PostMapping(value = "/migrateFrc", consumes = APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
            description = "Callback was processed successfully or in case of an error message is attached to the case",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = CallbackResponse.class))})})
    public CallbackResponse migrateFrc(
        @RequestHeader(value = AUTHORIZATION_HEADER) final String authorisationToken,
        @RequestBody @Parameter(description = "CaseData") final CallbackRequest ccdRequest) {

        CaseDetails caseDetails = ccdRequest.getCaseDetails();
        log.info("FR case migration request received for case {}", caseDetails.getId());

        AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder responseBuilder =
            AboutToStartOrSubmitCallbackResponse.builder();

        // Change the below to point to your new migration service and modify controller tests to match
        MigrationHandler migration = new Rpet164FrcCourtListMigrationImpl();
        Map<String, Object> caseData = migration.migrate(caseDetails);

        if (caseData != null) {
            responseBuilder.data(caseData);
        }

        return responseBuilder.build();
    }
}
