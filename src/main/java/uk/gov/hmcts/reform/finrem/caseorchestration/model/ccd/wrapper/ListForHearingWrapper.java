package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AdditionalHearingDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HasCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingTypeDirection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.time.LocalDate;
import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesEnaquoAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedySolicitorRAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesSfvgopAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesGjmikxAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceSystemupdateRAccess;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ListForHearingWrapper implements HasCaseDocument {
    @CCD(
            label = "Type of Hearing",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_fl_hearingTypeDirections",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesEnaquoAccess.class, CaseworkerDivorceFinancialremedySolicitorRAccess.class}
    )
    private HearingTypeDirection hearingType;
    @CCD(
            label = "  ",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesSfvgopAccess.class}
    )
    private String timeEstimate;
    @CCD(
            label = "  ",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesSfvgopAccess.class}
    )
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate hearingDate;
    @CCD(
            label = "  ",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesSfvgopAccess.class}
    )
    private String hearingTime;
    @JsonUnwrapped
    private HearingRegionWrapper hearingRegionWrapper;
    @CCD(
            label = "  ",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesSfvgopAccess.class}
    )
    private String additionalInformationAboutHearing;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesSfvgopAccess.class}
    )
    private YesOrNo additionalHearingDocumentsOption;
    @CCD(
            label = " ",
            showCondition = "additionalHearingDocumentsOption=\"Yes\"",
            categoryID = "hearingNotices",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesSfvgopAccess.class}
    )
    private CaseDocument additionalListOfHearingDocuments;

    @CCD(
            label = "Form C",
            hint = "Form C",
            categoryID = "hearingNotices",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesGjmikxAccess.class}
    )
    private CaseDocument formC;
    @CCD(
            label = "Form G",
            hint = "Form G",
            categoryID = "hearingNotices",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesGjmikxAccess.class}
    )
    private CaseDocument formG;
    @CCD(
            label = "PFD NCDR Compliance Letter",
            hint = "PFD NCDR Compliance Letter",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesGjmikxAccess.class, CaseworkerDivorceSystemupdateRAccess.class}
    )
    private CaseDocument pfdNcdrComplianceLetter;
    @CCD(
            label = "PFD NCDR Cover Letter",
            hint = "PFD NCDR Cover Letter",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesGjmikxAccess.class, CaseworkerDivorceSystemupdateRAccess.class}
    )
    private CaseDocument pfdNcdrCoverLetter;
    @CCD(
            label = "Additional Hearing Documents",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_AdditionalHearingCollection",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesSfvgopAccess.class}
    )
    private List<AdditionalHearingDocumentCollection> additionalHearingDocuments;
}
