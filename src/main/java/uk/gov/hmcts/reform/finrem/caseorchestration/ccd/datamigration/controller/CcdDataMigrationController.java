package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.datamigration.controller;

import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.datamigration.model.prod.CCDMigrationCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.datamigration.model.prod.CCDMigrationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.datamigration.model.prod.CaseData;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/ccd-data-migration")
@Slf4j
public class CcdDataMigrationController {

    @PostMapping(value = "/migrate", consumes = APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Callback was processed successFully or in case of an error message is "
                    + "attached to the case", response = CCDMigrationCallbackResponse.class)})
    public ResponseEntity<CCDMigrationCallbackResponse> migrate(
            @RequestHeader(value = "Authorization") String authorisationToken,
            @RequestBody @ApiParam("CaseData") CCDMigrationRequest ccdRequest) {
        log.info("ccdMigrationRequest >>> authorisationToken {}, ccdRequest {}", authorisationToken, ccdRequest);
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        return ResponseEntity.ok(CCDMigrationCallbackResponse.builder().data(caseData).build());
    }
}
