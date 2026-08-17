package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HasCaseDocument;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.APPBARRISTERAPPSOLICITORCudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerCrudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.RESPBARRISTERRESPSOLICITORCudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.INTVRBARRISTER1CuAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.INTVRSOLICITOR1CuAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.INTVRBARRISTER2CuAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.INTVRSOLICITOR2CuAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.INTVRBARRISTER3CuAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.INTVRSOLICITOR3CuAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.INTVRBARRISTER4CuAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.INTVRSOLICITOR4CuAccess;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BulkPrintCoversheetWrapper implements HasCaseDocument {
    @CCD(
            label = "Bulk Print Cover Sheet For Applicant / Solicitor",
            hint = "Generated Bulk Print Cover Sheet from Docmosis",
            categoryID = "administrativeDocumentsCoversheets",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {APPBARRISTERAPPSOLICITORCudAccess.class, CaseworkerDivorceFinancialremedyCourtadminCudAccess.class, CaseworkerCrudAccess.class}
    )
    private CaseDocument bulkPrintCoverSheetApp;
    @CCD(
            label = "Bulk Print Cover Sheet For Respondent / Solicitor",
            hint = "Generated Bulk Print Cover Sheet from Docmosis",
            categoryID = "administrativeDocumentsCoversheets",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {RESPBARRISTERRESPSOLICITORCudAccess.class, CaseworkerDivorceFinancialremedyCourtadminCudAccess.class, CaseworkerCrudAccess.class}
    )
    private CaseDocument bulkPrintCoverSheetRes;
    @CCD(
            label = "Bulk Print Cover Sheet For Intervener1 / Solicitor",
            hint = "Generated Bulk Print Cover Sheet from Docmosis",
            categoryID = "administrativeDocumentsCoversheets",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {INTVRBARRISTER1CuAccess.class, INTVRSOLICITOR1CuAccess.class, CaseworkerDivorceFinancialremedyCourtadminCudAccess.class}
    )
    private CaseDocument bulkPrintCoverSheetIntv1;
    @CCD(
            label = "Bulk Print Cover Sheet For Intervener2 / Solicitor",
            hint = "Generated Bulk Print Cover Sheet from Docmosis",
            categoryID = "administrativeDocumentsCoversheets",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {INTVRBARRISTER2CuAccess.class, INTVRSOLICITOR2CuAccess.class, CaseworkerDivorceFinancialremedyCourtadminCudAccess.class}
    )
    private CaseDocument bulkPrintCoverSheetIntv2;
    @CCD(
            label = "Bulk Print Cover Sheet For Intervener3 / Solicitor",
            hint = "Generated Bulk Print Cover Sheet from Docmosis",
            categoryID = "administrativeDocumentsCoversheets",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {INTVRBARRISTER3CuAccess.class, INTVRSOLICITOR3CuAccess.class, CaseworkerDivorceFinancialremedyCourtadminCudAccess.class}
    )
    private CaseDocument bulkPrintCoverSheetIntv3;
    @CCD(
            label = "Bulk Print Cover Sheet For Intervener4 / Solicitor",
            hint = "Generated Bulk Print Cover Sheet from Docmosis",
            categoryID = "administrativeDocumentsCoversheets",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {INTVRBARRISTER4CuAccess.class, INTVRSOLICITOR4CuAccess.class, CaseworkerDivorceFinancialremedyCourtadminCudAccess.class}
    )
    private CaseDocument bulkPrintCoverSheetIntv4;
    @CCD(ignore = true)
    private CaseDocument bulkPrintCoverSheetAppConfidential;
    @CCD(ignore = true)
    private CaseDocument bulkPrintCoverSheetResConfidential;
}
