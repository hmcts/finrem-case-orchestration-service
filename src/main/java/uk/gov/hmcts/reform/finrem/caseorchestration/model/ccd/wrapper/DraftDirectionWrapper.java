package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionDetailsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HasCaseDocument;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesEnaquoAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.APPBARRISTERAPPSOLICITORRESPBARRISTERRESPSOLICITORCrudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyJudiciaryCrudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminRPlus1RolesYcfimvAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.APPBARRISTERAPPSOLICITORRESPBARRISTERRAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.RESPSOLICITORRAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedySuperuserCrudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCrudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FRCtDraftDirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FRCwApprovedOrder;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DraftDirectionWrapper implements HasCaseDocument {
    @CCD(
            label = "Upload approved order",
            hint = "You must upload Microsoft Word documents. Document names should clearly reflect the party name, the type of hearing and the date of the hearing. For example “JonesFDA11Jul24”",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_ct_draftDirectionOrder",
            typeParameterClass = FRCtDraftDirectionOrder.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesEnaquoAccess.class, APPBARRISTERAPPSOLICITORRESPBARRISTERRESPSOLICITORCrudAccess.class}
    )
    private List<DraftDirectionOrderCollection> draftDirectionOrderCollection;
    @CCD(
            label = "Latest Draft Direction Order  ",
            hint = "PLEASE NOTE: you must upload an editable version of the draft order (for example a 'word' document) - please do not upload a PDF",
            searchable = false,
            typeParameterClass = FRCtDraftDirectionOrder.class,
            access = {CaseworkerDivorceFinancialremedyJudiciaryCrudAccess.class}
    )
    private DraftDirectionOrder latestDraftDirectionOrder;
    @CCD(
            label = "Judge's amended direction orders",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_ct_draftDirectionOrder",
            typeParameterClass = FRCtDraftDirectionOrder.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminRPlus1RolesYcfimvAccess.class, APPBARRISTERAPPSOLICITORRESPBARRISTERRAccess.class, RESPSOLICITORRAccess.class, CaseworkerDivorceFinancialremedySuperuserCrudAccess.class}
    )
    private List<DraftDirectionOrderCollection> judgesAmendedOrderCollection;
    @CCD(
            label = "Draft Direction Orders Details ",
            hint = "Draft Direction Orders Details ",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_ct_draftDirectionDetailsCollection",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesEnaquoAccess.class}
    )
    private List<DraftDirectionDetailsCollection> draftDirectionDetailsCollection;
    @CCD(
            label = "Draft Direction Orders Details (Judge)",
            hint = "Draft Direction Orders Details ",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_ct_draftDirectionDetailsCollection",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesEnaquoAccess.class}
    )
    private List<DraftDirectionDetailsCollection> draftDirectionDetailsCollectionRO;

    // temporary field for capturing judge's uploaded approved order
    @CCD(
            label = "Upload approved order",
            hint = "You must upload Microsoft Word or PDF documents. Document names should clearly reflect the party name, the type of hearing and the date of the hearing. For example “JonesFDA11Jul24”",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_ct_draftDirectionOrder",
            typeParameterClass = FRCtDraftDirectionOrder.class,
            access = {CaseworkerDivorceFinancialremedyJudiciaryCrudAccess.class}
    )
    private List<DraftDirectionOrderCollection> judgeApprovedOrderCollection;
    // temporary field for capturing cw's uploaded approved order
    @CCD(
            label = "Upload approved order",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_cwApprovedOrder",
            typeParameterClass = FRCwApprovedOrder.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    private List<DirectionOrderCollection> cwApprovedOrderCollection;
}
