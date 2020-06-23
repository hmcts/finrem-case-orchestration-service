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
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/ccd-data-migration")
@Slf4j
public class CcdDataMigrationController {

    @PostMapping(value = "/migrate", consumes = APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
                response = CallbackResponse.class)})
    public CallbackResponse migrate(
            @RequestHeader(value = AUTHORIZATION_HEADER) final String authorisationToken,
            @RequestBody @ApiParam("CaseData") final CallbackRequest ccdRequest) {

        CaseDetails caseDetails = ccdRequest.getCaseDetails();
        log.info("FR case migration request received for case {}", caseDetails.getId());

        AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder responseBuilder =
            AboutToStartOrSubmitCallbackResponse.builder();

        if (migrationRequired(caseDetails)) {
            Map<String, Object> caseData = migrateCaseData(caseDetails.getData());
            responseBuilder.data(caseData);
        }

        return responseBuilder.build();
    }

    private boolean migrationRequired(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();
        if (caseData.get("natureOfApplication2") != null) {
            List<String> list = (List) caseData.get("natureOfApplication2");
            return list.contains("Property Adjustment  Order");
        } else {
            return false;
        }
    }

    private Map<String, Object> migrateCaseData(Map<String, Object> caseData) {
        if (caseData.get("natureOfApplication2") != null) {
            List<String> list = (List) caseData.get("natureOfApplication2");
            int elementToFixIndex = list.indexOf("Property Adjustment  Order");
            if (elementToFixIndex >= 0) {
                list.set(elementToFixIndex, "Property Adjustment Order");
            }
        }
        return caseData;
    }
}
