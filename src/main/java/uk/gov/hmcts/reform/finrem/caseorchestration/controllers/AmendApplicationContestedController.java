package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;

@RestController
@RequestMapping(value = "/case-orchestration")
@Slf4j
@RequiredArgsConstructor
public class AmendApplicationContestedController extends BaseController {

    @PostMapping(path = "/amend-application-app-sol", consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Validate postcode on applicant solicitor page")
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> amendApplicationAppSolicitor(
        @RequestBody CallbackRequest callbackRequest,
        @RequestHeader(value = AUTHORIZATION_HEADER, required = false) final String authToken
    ) {
        Map<String, Object> caseData = callbackRequest.getCaseDetails().getData();

        List<String> errors = new ArrayList<>();
        validateApplicantSolicitorPostcodeDetails(caseData, errors);

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).errors(errors).build());
    }

    @PostMapping(path = "/amend-application-app", consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Validate postcode on applicant details page")
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> amendApplicationApp(
        @RequestBody CallbackRequest callbackRequest,
        @RequestHeader(value = AUTHORIZATION_HEADER, required = false) final String authToken
    ) {
        Map<String, Object> caseData = callbackRequest.getCaseDetails().getData();
        List<String> errors = new ArrayList<>();
        validateApplicantPostcodeDetails(caseData, errors);

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).errors(errors).build());
    }

    @PostMapping(path = "/amend-application-res-sol", consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Validate postcode on respondent solicitor page")
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> amendApplicationRespondentSolicitor(
        @RequestBody CallbackRequest callbackRequest,
        @RequestHeader(value = AUTHORIZATION_HEADER, required = false) final String authToken
    ) {
        Map<String, Object> caseData = callbackRequest.getCaseDetails().getData();
        List<String> errors = new ArrayList<>();
        validateRespondentPostcodeDetails(caseData, errors);

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).errors(errors).build());
    }

    private void validateApplicantSolicitorPostcodeDetails(Map<String, Object> caseData, List<String> errors) {

        if (YES_VALUE.equals(caseData.get("applicantRepresented"))) {
            Map<String, Object> applicantSolicitorAddress = (Map<String, Object>) caseData.get("applicantSolicitorAddress");
            String solicitorPostCode = (String) applicantSolicitorAddress.get("PostCode");

            if (StringUtils.isBlank(solicitorPostCode)) {
                errors.add("Postcode field is required for applicant solicitor address.");
            }
        }

    }

    private void validateApplicantPostcodeDetails(Map<String, Object> caseData, List<String> errors) {
        String postCode = null;

        Map<String, Object> applicantAddress = (Map<String, Object>) caseData.get("applicantAddress");
        postCode = (String) applicantAddress.get("PostCode");

        if (StringUtils.isBlank(postCode)) {
            errors.add("Postcode field is required for applicant address.");
        }
    }

    private void validateRespondentPostcodeDetails(Map<String, Object> caseData, List<String> errors) {
        String postCode = null;

        if (YES_VALUE.equals(caseData.get("respondentRepresented"))) {
            Map<String, Object> respondentSolicitorAddress = (Map<String, Object>) caseData.get("rSolicitorAddress");
            postCode = (String) respondentSolicitorAddress.get("PostCode");

            if (StringUtils.isBlank(postCode)) {
                errors.add("Postcode field is required for respondent solicitor address.");
            }
        } else {
            Map<String, Object> respondentAddress = (Map<String, Object>) caseData.get("respondentAddress");
            postCode = (String) respondentAddress.get("PostCode");

            if (StringUtils.isBlank(postCode)) {
                errors.add("Postcode field is required for respondent address.");
            }
        }
    }

}
