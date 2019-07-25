package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.datamigration.controller;

import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ObjectUtils;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

import java.util.Map;

import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.STATE;


@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/ccd-data-migration")
@Slf4j
public class CcdDataMigrationController {

    @PostMapping(value = "/migrate", consumes = APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Callback was processed successFully or in case of an error message is "
                    + "attached to the case", response = CallbackResponse.class)})
    public CallbackResponse migrate(
            @RequestHeader(value = "Authorization") String authorisationToken,
            @RequestBody @ApiParam("CaseData") CallbackRequest ccdRequest) {
        log.info("ccdMigrationRequest >>> authorisationToken {}, ccdRequest {}", authorisationToken, ccdRequest);
        Map<String, Object> caseData = ccdRequest.getCaseDetails().getData();
        boolean shouldMigrateCase = shouldMigrateCase(caseData);
        log.info("shouldMigrateCase >>> {}", shouldMigrateCase);
        if (shouldMigrateCase) {
            caseData.remove(STATE);
            return AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build();
        } else {
            return AboutToStartOrSubmitCallbackResponse.builder().build();
        }
    }

    private boolean shouldMigrateCase(Map<String, Object> caseData) {
        String state = ObjectUtils.toString(caseData.get(STATE));
        log.info("state >>> {}", state);
        return !isEmpty(state);
    }
}
