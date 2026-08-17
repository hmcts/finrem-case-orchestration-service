package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetailInterimCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HasCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingBulkPrintDocumentsData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingCollectionItemData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimTypeOfHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.time.LocalDate;
import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesBtwqpnAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesSfvgopAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesEnaquoAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedySolicitorRAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCruAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyJudiciaryCruAccess;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InterimWrapper implements HasCaseDocument {
    @CCD(
            label = "Next Hearing Details",
            hint = "Direction Orders Details ",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_ct_directionDetailCollectionInterim",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesBtwqpnAccess.class}
    )
    private List<DirectionDetailInterimCollection> directionDetailsCollectionInterim;
    @CCD(
            label = "  ",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesSfvgopAccess.class}
    )
    private String interimTimeEstimate;
    @CCD(
            label = "  ",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesSfvgopAccess.class}
    )
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate interimHearingDate;
    @CCD(
            label = "  ",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesSfvgopAccess.class}
    )
    private String interimHearingTime;
    @CCD(
            label = "  ",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesSfvgopAccess.class}
    )
    private String interimAdditionalInformationAboutHearing;
    @CCD(
            label = "          ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesSfvgopAccess.class}
    )
    private YesOrNo interimPromptForAnyDocument;
    @CCD(
            label = "Type of Hearing",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_fl_interimHearingTypeDirections",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesEnaquoAccess.class, CaseworkerDivorceFinancialremedySolicitorRAccess.class}
    )
    private InterimTypeOfHearing interimHearingType;
    @CCD(
            label = " ",
            hint = "Please upload any additional documents related to your application.",
            categoryID = "hearingNotices",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesBtwqpnAccess.class}
    )
    private CaseDocument interimUploadAdditionalDocument;
    @CCD(
            label = "Interim Hearing",
            categoryID = "hearingNotices",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesSfvgopAccess.class}
    )
    private CaseDocument interimHearingDirectionsDocument;
    @CCD(
            label = "Interim Hearings",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "interimHearingsCollection",
            access = {CaseworkerDivorceFinancialremedyCourtadminCruAccess.class, CaseworkerDivorceFinancialremedyJudiciaryCruAccess.class, CaseworkerDivorceFinancialremedySolicitorRAccess.class}
    )
    private List<InterimHearingCollection> interimHearings;
    @CCD(
            label = "New Interim Hearings",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "interimHearingsCollection",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesEnaquoAccess.class, CaseworkerDivorceFinancialremedySolicitorRAccess.class}
    )
    private List<InterimHearingCollection> interimHearingsScreenField;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_iHCollectionItemIds",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesEnaquoAccess.class, CaseworkerDivorceFinancialremedySolicitorRAccess.class}
    )
    @JsonProperty("iHCollectionItemIds")
    private List<InterimHearingCollectionItemData> interimHearingCollectionItemIds;
    @CCD(
            label = "Interim Hearing Documents",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_iHBulkPrintDocument",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesSfvgopAccess.class}
    )
    private List<InterimHearingBulkPrintDocumentsData> interimHearingDocuments;
}
