package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.TemporaryField;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HasCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managecasedocuments.ManageCaseDocumentsAction;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCrudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesGpeopvAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyJudiciaryRuAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FRManageCaseDocuments;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FRManageCaseDocumentInput;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ManageCaseDocumentsWrapper implements HasCaseDocument {

    @CCD(
            label = "What do you want to do?",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    @TemporaryField
    private ManageCaseDocumentsAction manageCaseDocumentsActionSelection;

    // It was used for capturing user input in the old event.
    // It’s kept to maintain compatibility with the existing document handler logic.
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_manageCaseDocuments",
            typeParameterClass = FRManageCaseDocuments.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesGpeopvAccess.class, CaseworkerDivorceFinancialremedyJudiciaryRuAccess.class}
    )
    private List<UploadCaseDocumentCollection> manageCaseDocumentCollection;

    // it's used for capturing user's input in FR_newManageCaseDocuments event.
    @CCD(
            label = "Add new case document(s)",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_manageCaseDocumentInput",
            typeParameterClass = FRManageCaseDocumentInput.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    @TemporaryField
    private List<UploadCaseDocumentCollection> inputManageCaseDocumentCollection;
}
