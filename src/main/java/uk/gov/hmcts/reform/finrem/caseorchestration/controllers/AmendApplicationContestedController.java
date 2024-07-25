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
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_ADDRESS;

@RestController
@RequestMapping(value = "/case-orchestration")
@Slf4j
@RequiredArgsConstructor
public class AmendApplicationContestedController extends BaseController {

    private static final String POST_CODE = "PostCode";

    private final FinremCaseDetailsMapper finremCaseDetailsMapper;

    private final InternationalPostalService postalService;

    @PostMapping(path = "/amend-application-validate-applicant-solicitor-address", consumes = MediaType.APPLICATION_JSON_VALUE,
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

    @PostMapping(path = "/amend-application-validate-applicant-address", consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Validate postcode on applicant details page")
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> amendApplicationApp(
        @RequestBody CallbackRequest callbackRequest,
        @RequestHeader(value = AUTHORIZATION_HEADER, required = false) final String authToken
    ) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseData = callbackRequest.getCaseDetails().getData();
        List<String> errors = new ArrayList<>();
        validateApplicantPostcodeDetails(caseData, errors);

        FinremCaseDetails finremCaseDetails = finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails);
        errors.addAll(postalService.validate(finremCaseDetails.getData()));

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).errors(errors).build());
    }

    @PostMapping(path = "/amend-application-validate-respondent-solicitor", consumes = MediaType.APPLICATION_JSON_VALUE,
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

        if (YES_VALUE.equals(caseData.get(APPLICANT_REPRESENTED))) {
            Map<String, Object> applicantSolicitorAddress = (Map<String, Object>) caseData.get(CONTESTED_SOLICITOR_ADDRESS);
            String applicantSolicitorPostCode = (String) applicantSolicitorAddress.get(POST_CODE);

            if (StringUtils.isBlank(applicantSolicitorPostCode)) {
                errors.add("Postcode field is required for applicant solicitor address.");
            }
        }

    }

    private void validateApplicantPostcodeDetails(Map<String, Object> caseData, List<String> errors) {

        Map<String, Object> applicantAddress = (Map<String, Object>) caseData.get(APPLICANT_ADDRESS);
        String applicantPostCode = (String) applicantAddress.get(POST_CODE);

        if (StringUtils.isBlank(applicantPostCode)) {
            errors.add("Postcode field is required for applicant address.");
        }
    }

    private void validateRespondentPostcodeDetails(Map<String, Object> caseData, List<String> errors) {

        if (YES_VALUE.equals(caseData.get(CONTESTED_RESPONDENT_REPRESENTED))) {
            Map<String, Object> respondentSolicitorAddress = (Map<String, Object>) caseData.get(RESP_SOLICITOR_ADDRESS);
            String respondentPostCode = (String) respondentSolicitorAddress.get(POST_CODE);

            if (StringUtils.isBlank(respondentPostCode)) {
                errors.add("Postcode field is required for respondent solicitor address.");
            }
        } else {
            Map<String, Object> respondentAddress = (Map<String, Object>) caseData.get(RESPONDENT_ADDRESS);
            String respondentPostCode = (String) respondentAddress.get(POST_CODE);

            if (StringUtils.isBlank(respondentPostCode)) {
                errors.add("Postcode field is required for respondent address.");
            }
        }
    }

}
