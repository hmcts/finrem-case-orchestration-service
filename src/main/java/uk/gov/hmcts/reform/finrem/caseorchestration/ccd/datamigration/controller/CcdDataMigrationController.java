package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.datamigration.controller;

import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.datamigration.model.CCDMigrationCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.datamigration.model.CCDMigrationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.datamigration.model.v1.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.datamigration.service.MigrationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDCallbackResponse;
import java.util.ArrayList;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@RestController
@RequestMapping(value = "/ccd-data-migration")
@Slf4j
public class CcdDataMigrationController {
    @Autowired
    private MigrationService migrationService;

    @PostMapping(value = "/migrate", consumes = APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Callback was processed successFully or in case of an error message is "
                    + "attached to the case",
                    response = CCDCallbackResponse.class),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<CCDMigrationCallbackResponse> migrate(
            @RequestHeader(value = "Authorization") String authorisationToken,
            @RequestBody @ApiParam("ProdCaseData") CCDMigrationRequest ccdRequest) {
        log.info("ccdMigrationRequest >>> case Id {}", ccdRequest.getCaseDetails().getCaseId());
        CaseDetails migratedCaseDetails = migrationService.migrateTov1(ccdRequest.getCaseDetails());
        return ResponseEntity.ok(new CCDMigrationCallbackResponse(migratedCaseDetails.getCaseData(),
                new ArrayList<>(), new ArrayList<>()));
    }

}
