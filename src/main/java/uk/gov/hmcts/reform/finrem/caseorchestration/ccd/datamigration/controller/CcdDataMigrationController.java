package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.datamigration.controller;

import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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

import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.YES;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/ccd-data-migration")
@Slf4j
public class CcdDataMigrationController {

    @PostMapping(value = "/migrate", consumes = APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is "
                                                       + "attached to the case", response = CallbackResponse.class)})
    public CallbackResponse migrate(
            @RequestHeader(value = "Authorization") final String authorisationToken,
            @RequestBody @ApiParam("CaseData") final CallbackRequest ccdRequest) {

        log.info("FRMig: ccdMigrationRequest >>> authorisationToken {}, ccdRequest {}",
                authorisationToken, ccdRequest);

        final Map<String, Object> caseData = ccdRequest.getCaseDetails().getData();
        boolean migrationRequired = false;
        final Object caseId = ccdRequest.getCaseDetails().getId();
        final boolean applicantRepresentedExist = caseData.containsKey(APPLICANT_REPRESENTED);

        log.info("FR Migration: {} ,applicantRepresentedExist : {}", caseId, applicantRepresentedExist);

        if (!applicantRepresentedExist) {
            caseData.put(APPLICANT_REPRESENTED, YES);
            log.info("FR Migration: {} setting applicantRepresented to Yes.", caseId);
            migrationRequired = true;
        }

        if (migrationRequired) {
            log.info("FR Migration: {} End of case migration {} ", caseId, caseData);

            return AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build();
        } else {
            log.info("FR Migration: {} Returning without migration", caseId);

            return AboutToStartOrSubmitCallbackResponse.builder().build();
        }
    }
}
