package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bsp.common.error.InvalidDataException;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.ExceptionRecord;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.OcrDataField;
import uk.gov.hmcts.reform.bsp.common.model.validation.out.OcrValidationResult;
import uk.gov.hmcts.reform.bsp.common.model.validation.out.ValidationStatus;
import uk.gov.hmcts.reform.bsp.common.service.BulkScanFormValidator;
import uk.gov.hmcts.reform.bsp.common.service.transformation.BulkScanFormTransformer;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.transformation.FinRemBulkScanFormTransformerFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.validation.FinRemBulkScanFormValidatorFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.validation.FormAValidator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.bsp.common.utils.BulkScanCommonHelper.produceMapWithoutEmptyEntries;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CHANGE_ORGANISATION_REQUEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.DIVORCE_CASE_NUMBER;

@RequiredArgsConstructor
@Service
@Slf4j
public class BulkScanService {

    public static final String CONSENTED_IN_CONTESTED_MESSAGE =
        "A Contested Financial Remedy case already exists for the supplied divorce case number. "
            + "Consented within Contested applications must be progressed on the existing Contested case.";
    private static final String CCD_CASE_REFERENCE_REGEX = "\\d{16}";

    private final FinRemBulkScanFormValidatorFactory finRemBulkScanFormValidatorFactory;

    private final FinRemBulkScanFormTransformerFactory finRemBulkScanFormTransformerFactory;

    private final FormAValidator formAValidator;

    private final CcdService ccdService;

    private final SystemUserService systemUserService;

    /**
     * /** Validates OCR data extracted from a bulk scan form for the supplied form type.
     * Adds a warning when a valid divorce case reference matches an existing contested case.
     *
     * @param formType      the bulk scan form type to validate
     * @param ocrDataFields the OCR data fields extracted from the scanned form
     * @return the OCR validation result containing validation status, warnings, and errors
     */

    public OcrValidationResult validateBulkScanForm(String formType, List<OcrDataField> ocrDataFields) {
        BulkScanFormValidator formValidator = finRemBulkScanFormValidatorFactory.getValidator(formType);
        OcrValidationResult ocrValidationResult = formValidator.validateBulkScanForm(ocrDataFields);
        return addContestedRefWarningIfApplicable(ocrValidationResult, ocrDataFields);
    }

    /**
     * Transforms a validated bulk scan exception record into CCD case data and enriches it with default
     * organisation policy metadata.     *
     *
     * @param exceptionRecord the exception record containing form type and OCR data to be transformed
     * @return a mutable map of transformed case data including change organisation request and default
     * organisation policies
     */
    public Map<String, Object> transformBulkScanForm(ExceptionRecord exceptionRecord) {
        validateForTransformation(exceptionRecord);

        BulkScanFormTransformer bulkScanFormTransformer =
            finRemBulkScanFormTransformerFactory.getTransformer(exceptionRecord.getFormType());
        Map<String, Object> transformIntoCaseData = bulkScanFormTransformer.transformIntoCaseData(exceptionRecord);
        Map<String, Object> caseData = new HashMap<>(transformIntoCaseData);
        addChangeOrganisationRequestAndDefaultOrganisationPolicies(caseData);
        return caseData;
    }

    private void addChangeOrganisationRequestAndDefaultOrganisationPolicies(Map<String, Object> caseData) {
        ChangeOrganisationRequest defaultChangeRequest = ChangeOrganisationRequest
            .builder()
            .requestTimestamp(null)
            .approvalRejectionTimestamp(null)
            .caseRoleId(null)
            .approvalStatus(null)
            .organisationToAdd(null)
            .organisationToRemove(null)
            .reason(null)
            .build();
        caseData.put(CHANGE_ORGANISATION_REQUEST, defaultChangeRequest);

        OrganisationPolicy applicantOrganisationPolicy = OrganisationPolicy.builder()
            .orgPolicyCaseAssignedRole(APP_SOLICITOR_POLICY)
            .build();
        caseData.put(APPLICANT_ORGANISATION_POLICY, applicantOrganisationPolicy);

        OrganisationPolicy respondentOrganisationPolicy = OrganisationPolicy.builder()
            .orgPolicyCaseAssignedRole(RESP_SOLICITOR_POLICY)
            .build();
        caseData.put(RESPONDENT_ORGANISATION_POLICY, respondentOrganisationPolicy);
    }

    private void validateForTransformation(ExceptionRecord exceptionRecord) {

        OcrValidationResult ocrDataFieldsValidationResult
            = validateBulkScanForm(exceptionRecord.getFormType(), exceptionRecord.getOcrDataFields());

        if (!ocrDataFieldsValidationResult.getStatus().equals(ValidationStatus.SUCCESS)) {
            throwInvalidDataException(exceptionRecord, ocrDataFieldsValidationResult);
        }

        OcrValidationResult bulkScanFormsValidationResult = formAValidator.validateFormAScannedDocuments(exceptionRecord);

        if (!bulkScanFormsValidationResult.getStatus().equals(ValidationStatus.SUCCESS)) {
            String docValidationError = String.format("Validation of Exception Record attached documents %s finished with status %s",
                exceptionRecord.getId(), bulkScanFormsValidationResult.getStatus());

            log.info(docValidationError);
            throw new InvalidDataException(docValidationError,
                bulkScanFormsValidationResult.getWarnings(),
                bulkScanFormsValidationResult.getErrors()
            );
        }
    }

    private OcrValidationResult addContestedRefWarningIfApplicable(OcrValidationResult validationResult,
                                                                   List<OcrDataField> ocrDataFields) {
        String divorceCaseNumber = produceMapWithoutEmptyEntries(ocrDataFields).get(DIVORCE_CASE_NUMBER);

        if (isCcdCaseReference(divorceCaseNumber)
            && ccdService.contestedCaseExistsWithReference(divorceCaseNumber, systemUserService.getSysUserToken())) {
            return addContestedRefWarning(validationResult);
        }

        return validationResult;
    }

    private OcrValidationResult addContestedRefWarning(OcrValidationResult validationResult) {
        OcrValidationResult.Builder builder = OcrValidationResult.builder();
        validationResult.getWarnings().forEach(builder::addWarning);
        validationResult.getErrors().forEach(builder::addError);
        return builder.addWarning(BulkScanService.CONSENTED_IN_CONTESTED_MESSAGE).build();
    }

    private boolean isCcdCaseReference(String caseReference) {
        return StringUtils.isNotBlank(caseReference) && caseReference.matches(CCD_CASE_REFERENCE_REGEX);
    }

    private void throwInvalidDataException(ExceptionRecord exceptionRecord, OcrValidationResult validationResult) {
        String ocrValidationError = String.format("Validation of Exception Record %s finished with status %s",
            exceptionRecord.getId(), validationResult.getStatus());

        log.info(ocrValidationError);
        throw new InvalidDataException(
            ocrValidationError,
            validationResult.getWarnings(),
            validationResult.getErrors()
        );
    }
}
