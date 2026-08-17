package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralLetterAddressToType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralLetterCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HasCaseDocument;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesEnaquoAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesMhrsdiAccess;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeneralLetterWrapper implements HasCaseDocument {
    @CCD(
            label = "Address to",
            searchable = false,
            typeOverride = FieldType.DynamicRadioList,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesEnaquoAccess.class}
    )
    private DynamicRadioList generalLetterAddressee;
    @CCD(
            label = "Address to",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesEnaquoAccess.class}
    )
    private GeneralLetterAddressToType generalLetterAddressTo;
    @CCD(
            label = "Recipient's name",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesEnaquoAccess.class}
    )
    private String generalLetterRecipient;
    @CCD(
            label = "Recipient's address ",
            searchable = false,
            typeOverride = FieldType.AddressUK,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesEnaquoAccess.class}
    )
    private Address generalLetterRecipientAddress;
    @CCD(
            label = "Letter created by",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesEnaquoAccess.class}
    )
    private String generalLetterCreatedBy;
    @CCD(
            label = "Please fill in the body of the text",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesEnaquoAccess.class}
    )
    private String generalLetterBody;
    @CCD(ignore = true)
    private CaseDocument generalLetterPreview;
    @CCD(
            label = "Upload Document",
            categoryID = "administrativeDocumentsTransitional",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesEnaquoAccess.class}
    )
    private CaseDocument generalLetterUploadedDocument;
    @CCD(
            label = "Upload Document(s)",
            categoryID = "administrativeDocumentsTransitional",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Document",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesEnaquoAccess.class}
    )
    private List<DocumentCollectionItem> generalLetterUploadedDocuments;
    @CCD(
            label = "General Letter",
            hint = "General Letter",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_GeneralLetterDocument",
            access = {CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesMhrsdiAccess.class}
    )
    private List<GeneralLetterCollection> generalLetterCollection;
}
