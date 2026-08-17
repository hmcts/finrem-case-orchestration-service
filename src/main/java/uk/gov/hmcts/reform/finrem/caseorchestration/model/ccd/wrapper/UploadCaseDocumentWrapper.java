package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HasCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.INTVRBARRISTER1CrudPlus7RolesQwwlocAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.RESPBARRISTERRESPSOLICITORCudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.APPBARRISTERAPPSOLICITORCudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyJudiciaryUAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedySuperuserCrudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesZbqgyjAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedySolicitorCrudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedySolicitorCudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedySolicitorCrudPlus1RolesAvwswjAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.INTVRBARRISTER1CudPlus2RolesUmycfuAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.INTVRBARRISTER2CudPlus2RolesOsrpjfAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.INTVRBARRISTER3CudPlus2RolesIykcoqAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.INTVRBARRISTER4CudPlus2RolesCeepsbAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesMhrsdiAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.INTVRBARRISTER1INTVRSOLICITOR1CudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.INTVRBARRISTER2INTVRSOLICITOR2CudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.INTVRBARRISTER3INTVRSOLICITOR3CudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.INTVRBARRISTER4INTVRSOLICITOR4CudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FRUploadCaseDocument;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class UploadCaseDocumentWrapper implements HasCaseDocument {
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER1CrudPlus7RolesQwwlocAccess.class, RESPBARRISTERRESPSOLICITORCudAccess.class, APPBARRISTERAPPSOLICITORCudAccess.class, CaseworkerDivorceFinancialremedyJudiciaryUAccess.class, CaseworkerDivorceFinancialremedySuperuserCrudAccess.class}
    )
    private List<UploadCaseDocumentCollection> uploadCaseDocument;
    @CCD(
            label = "                                                                                      ",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesZbqgyjAccess.class, CaseworkerDivorceFinancialremedySolicitorCrudAccess.class}
    )
    private List<UploadCaseDocumentCollection> fdrCaseDocumentCollection;
    @CCD(
            label = "Correspondence",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesZbqgyjAccess.class, CaseworkerDivorceFinancialremedySolicitorCudAccess.class}
    )
    private List<UploadCaseDocumentCollection> appCorrespondenceCollection;
    @CCD(
            label = "FR Forms",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesZbqgyjAccess.class, CaseworkerDivorceFinancialremedySolicitorCudAccess.class}
    )
    @JsonProperty("appFRFormsCollection")
    private List<UploadCaseDocumentCollection> appFrFormsCollection;
    @CCD(
            label = "Evidence In Support",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesZbqgyjAccess.class, CaseworkerDivorceFinancialremedySolicitorCudAccess.class}
    )
    private List<UploadCaseDocumentCollection> appEvidenceCollection;
    @CCD(
            label = "Trial Bundle",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesZbqgyjAccess.class, CaseworkerDivorceFinancialremedySolicitorCudAccess.class}
    )
    private List<UploadCaseDocumentCollection> appTrialBundleCollection;
    @CCD(
            label = "Confidential Applicant Documents",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesZbqgyjAccess.class, APPBARRISTERAPPSOLICITORCudAccess.class}
    )
    private List<UploadCaseDocumentCollection> appConfidentialDocsCollection;
    @CCD(
            label = "Correspondence",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesZbqgyjAccess.class, CaseworkerDivorceFinancialremedySolicitorCudAccess.class}
    )
    private List<UploadCaseDocumentCollection> respCorrespondenceCollection;
    @CCD(
            label = "FR Forms",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesZbqgyjAccess.class, CaseworkerDivorceFinancialremedySolicitorCudAccess.class}
    )
    @JsonProperty("respFRFormsCollection")
    private List<UploadCaseDocumentCollection> respFrFormsCollection;
    @CCD(
            label = "Evidence In Support",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesZbqgyjAccess.class, CaseworkerDivorceFinancialremedySolicitorCudAccess.class}
    )
    private List<UploadCaseDocumentCollection> respEvidenceCollection;
    @CCD(
            label = "Trial Bundle",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesZbqgyjAccess.class, CaseworkerDivorceFinancialremedySolicitorCudAccess.class}
    )
    private List<UploadCaseDocumentCollection> respTrialBundleCollection;
    @CCD(
            label = "Confidential Respondent Documents",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesZbqgyjAccess.class, RESPBARRISTERRESPSOLICITORCudAccess.class}
    )
    private List<UploadCaseDocumentCollection> respConfidentialDocsCollection;
    @CCD(
            label = "Hearing Bundles",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesZbqgyjAccess.class, APPBARRISTERAPPSOLICITORCudAccess.class}
    )
    private List<UploadCaseDocumentCollection> appHearingBundlesCollection;
    @CCD(
            label = "Forms E & Exhibits",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesZbqgyjAccess.class, APPBARRISTERAPPSOLICITORCudAccess.class}
    )
    private List<UploadCaseDocumentCollection> appFormEExhibitsCollection;
    @CCD(
            label = "Chronologies and Statements of Issues",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesZbqgyjAccess.class, APPBARRISTERAPPSOLICITORCudAccess.class}
    )
    private List<UploadCaseDocumentCollection> appChronologiesCollection;
    @CCD(
            label = "Questionnaires & Answers to Questionnaires & Exhibits",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesZbqgyjAccess.class, APPBARRISTERAPPSOLICITORCudAccess.class}
    )
    @JsonProperty("appQACollection")
    private List<UploadCaseDocumentCollection> appQaCollection;
    @CCD(
            label = "Statements & Exhibits",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesZbqgyjAccess.class, APPBARRISTERAPPSOLICITORCudAccess.class}
    )
    private List<UploadCaseDocumentCollection> appStatementsExhibitsCollection;
    @CCD(
            label = "Case Summaries",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesZbqgyjAccess.class, APPBARRISTERAPPSOLICITORCudAccess.class}
    )
    private List<UploadCaseDocumentCollection> appCaseSummariesCollection;
    @CCD(
            label = "Forms H",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesZbqgyjAccess.class, APPBARRISTERAPPSOLICITORCudAccess.class}
    )
    private List<UploadCaseDocumentCollection> appFormsHCollection;
    @CCD(
            label = "Expert Evidence",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesZbqgyjAccess.class, APPBARRISTERAPPSOLICITORCudAccess.class}
    )
    private List<UploadCaseDocumentCollection> appExpertEvidenceCollection;
    @CCD(
            label = "Correspondence",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesZbqgyjAccess.class, APPBARRISTERAPPSOLICITORCudAccess.class}
    )
    private List<UploadCaseDocumentCollection> appCorrespondenceDocsCollection;
    @CCD(
            label = "Other",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesZbqgyjAccess.class, APPBARRISTERAPPSOLICITORCudAccess.class}
    )
    private List<UploadCaseDocumentCollection> appOtherCollection;
    @CCD(
            label = "Hearing Bundles",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesZbqgyjAccess.class, RESPBARRISTERRESPSOLICITORCudAccess.class}
    )
    private List<UploadCaseDocumentCollection> respHearingBundlesCollection;
    @CCD(
            label = "Forms E & Exhibits",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesZbqgyjAccess.class, RESPBARRISTERRESPSOLICITORCudAccess.class}
    )
    private List<UploadCaseDocumentCollection> respFormEExhibitsCollection;
    @CCD(
            label = "Chronologies and Statements of Issues",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesZbqgyjAccess.class, RESPBARRISTERRESPSOLICITORCudAccess.class}
    )
    private List<UploadCaseDocumentCollection> respChronologiesCollection;
    @CCD(
            label = "Questionnaires & Answers to Questionnaires & Exhibits",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesZbqgyjAccess.class, RESPBARRISTERRESPSOLICITORCudAccess.class}
    )
    @JsonProperty("respQACollection")
    private List<UploadCaseDocumentCollection> respQaCollection;
    @CCD(
            label = "Statements & Exhibits",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesZbqgyjAccess.class, RESPBARRISTERRESPSOLICITORCudAccess.class}
    )
    private List<UploadCaseDocumentCollection> respStatementsExhibitsCollection;
    @CCD(
            label = "Case Summaries",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesZbqgyjAccess.class, RESPBARRISTERRESPSOLICITORCudAccess.class}
    )
    private List<UploadCaseDocumentCollection> respCaseSummariesCollection;
    @CCD(
            label = "Forms H",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesZbqgyjAccess.class, RESPBARRISTERRESPSOLICITORCudAccess.class}
    )
    private List<UploadCaseDocumentCollection> respFormsHCollection;
    @CCD(
            label = "Expert Evidence",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesZbqgyjAccess.class, RESPBARRISTERRESPSOLICITORCudAccess.class}
    )
    private List<UploadCaseDocumentCollection> respExpertEvidenceCollection;
    @CCD(
            label = "Correspondence",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {RESPBARRISTERRESPSOLICITORCudAccess.class, CaseworkerDivorceFinancialremedyCourtadminCudAccess.class}
    )
    private List<UploadCaseDocumentCollection> respCorrespondenceDocsColl;
    @CCD(
            label = "Other",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesZbqgyjAccess.class, RESPBARRISTERRESPSOLICITORCudAccess.class}
    )
    private List<UploadCaseDocumentCollection> respOtherCollection;
    @CCD(
            label = "Hearing Bundles",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {CaseworkerDivorceFinancialremedySolicitorCrudPlus1RolesAvwswjAccess.class}
    )
    private List<UploadCaseDocumentCollection> appHearingBundlesCollectionShared;
    @CCD(
            label = "Forms E & Exhibits",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {CaseworkerDivorceFinancialremedySolicitorCrudPlus1RolesAvwswjAccess.class}
    )
    private List<UploadCaseDocumentCollection> appFormEExhibitsCollectionShared;
    @CCD(
            label = "Chronologies and Statements of Issues",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {CaseworkerDivorceFinancialremedySolicitorCrudPlus1RolesAvwswjAccess.class}
    )
    private List<UploadCaseDocumentCollection> appChronologiesCollectionShared;
    @CCD(
            label = "Questionnaires & Answers to Questionnaires & Exhibits",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {CaseworkerDivorceFinancialremedySolicitorCrudPlus1RolesAvwswjAccess.class}
    )
    @JsonProperty("appQACollectionShared")
    private List<UploadCaseDocumentCollection> appQaCollectionShared;
    @CCD(
            label = "Statements & Exhibits",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {CaseworkerDivorceFinancialremedySolicitorCrudPlus1RolesAvwswjAccess.class}
    )
    private List<UploadCaseDocumentCollection> appStatementsExhibitsCollShared;
    @CCD(
            label = "Case Summaries",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {CaseworkerDivorceFinancialremedySolicitorCrudPlus1RolesAvwswjAccess.class}
    )
    private List<UploadCaseDocumentCollection> appCaseSummariesCollectionShared;
    @CCD(
            label = "Forms H",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {CaseworkerDivorceFinancialremedySolicitorCrudPlus1RolesAvwswjAccess.class}
    )
    private List<UploadCaseDocumentCollection> appFormsHCollectionShared;
    @CCD(
            label = "Expert Evidence",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {CaseworkerDivorceFinancialremedySolicitorCrudPlus1RolesAvwswjAccess.class}
    )
    private List<UploadCaseDocumentCollection> appExpertEvidenceCollectionShared;
    @CCD(
            label = "Correspondence",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {CaseworkerDivorceFinancialremedySolicitorCrudPlus1RolesAvwswjAccess.class}
    )
    private List<UploadCaseDocumentCollection> appCorrespondenceDocsCollShared;
    @CCD(
            label = "Other",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {CaseworkerDivorceFinancialremedySolicitorCrudPlus1RolesAvwswjAccess.class}
    )
    private List<UploadCaseDocumentCollection> appOtherCollectionShared;
    @CCD(
            label = "Hearing Bundles",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {CaseworkerDivorceFinancialremedySolicitorCrudPlus1RolesAvwswjAccess.class}
    )
    private List<UploadCaseDocumentCollection> respHearingBundlesCollShared;
    @CCD(
            label = "Forms E & Exhibits",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {CaseworkerDivorceFinancialremedySolicitorCrudPlus1RolesAvwswjAccess.class}
    )
    private List<UploadCaseDocumentCollection> respFormEExhibitsCollectionShared;
    @CCD(
            label = "Chronologies and Statements of Issues",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {CaseworkerDivorceFinancialremedySolicitorCrudPlus1RolesAvwswjAccess.class}
    )
    private List<UploadCaseDocumentCollection> respChronologiesCollectionShared;
    @CCD(
            label = "Questionnaires & Answers to Questionnaires & Exhibits",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {CaseworkerDivorceFinancialremedySolicitorCrudPlus1RolesAvwswjAccess.class}
    )
    @JsonProperty("respQACollectionShared")
    private List<UploadCaseDocumentCollection> respQaCollectionShared;
    @CCD(
            label = "Statements & Exhibits",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {CaseworkerDivorceFinancialremedySolicitorCrudPlus1RolesAvwswjAccess.class}
    )
    private List<UploadCaseDocumentCollection> respStatementsExhibitsCollShared;
    @CCD(
            label = "Case Summaries",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {CaseworkerDivorceFinancialremedySolicitorCrudPlus1RolesAvwswjAccess.class}
    )
    private List<UploadCaseDocumentCollection> respCaseSummariesCollectionShared;
    @CCD(
            label = "Forms H",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {CaseworkerDivorceFinancialremedySolicitorCrudPlus1RolesAvwswjAccess.class}
    )
    private List<UploadCaseDocumentCollection> respFormsHCollectionShared;
    @CCD(
            label = "Expert Evidence",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {CaseworkerDivorceFinancialremedySolicitorCrudPlus1RolesAvwswjAccess.class}
    )
    private List<UploadCaseDocumentCollection> respExpertEvidenceCollShared;
    @CCD(
            label = "Correspondence",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {CaseworkerDivorceFinancialremedySolicitorCrudPlus1RolesAvwswjAccess.class}
    )
    private List<UploadCaseDocumentCollection> respCorrespondenceDocsCollShared;
    @CCD(
            label = "Other",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {CaseworkerDivorceFinancialremedySolicitorCrudPlus1RolesAvwswjAccess.class}
    )
    private List<UploadCaseDocumentCollection> respOtherCollectionShared;

    @CCD(
            label = "Case Summaries",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER1CudPlus2RolesUmycfuAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv1Summaries;
    @CCD(
            label = "Chronologies and Statements of Issues",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER1CudPlus2RolesUmycfuAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv1Chronologies;
    @CCD(
            label = "Correspondence",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER1CudPlus2RolesUmycfuAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv1CorrespDocs;
    @CCD(
            label = "Expert Evidence",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER1CudPlus2RolesUmycfuAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv1ExpertEvidence;
    @CCD(
            label = "Forms E & Exhibits",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER1CudPlus2RolesUmycfuAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv1FormEsExhibits;
    @CCD(
            label = "Forms H",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER1CudPlus2RolesUmycfuAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv1FormHs;
    @CCD(
            label = "Hearing Bundles",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER1CudPlus2RolesUmycfuAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv1HearingBundles;
    @CCD(
            label = "Other",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER1CudPlus2RolesUmycfuAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv1Other;
    @CCD(
            label = "Questionnaires & Answers to Questionnaires & Exhibits",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER1CudPlus2RolesUmycfuAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv1Qa;
    @CCD(
            label = "Statements & Exhibits",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER1CudPlus2RolesUmycfuAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv1StmtsExhibits;
    @CCD(
            label = "Case Summaries",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER2CudPlus2RolesOsrpjfAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv2Summaries;
    @CCD(
            label = "Chronologies and Statements of Issues",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER2CudPlus2RolesOsrpjfAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv2Chronologies;
    @CCD(
            label = "Correspondence",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER2CudPlus2RolesOsrpjfAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv2CorrespDocs;
    @CCD(
            label = "Expert Evidence",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER2CudPlus2RolesOsrpjfAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv2ExpertEvidence;
    @CCD(
            label = "Forms E & Exhibits",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER2CudPlus2RolesOsrpjfAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv2FormEsExhibits;
    @CCD(
            label = "Forms H",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER2CudPlus2RolesOsrpjfAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv2FormHs;
    @CCD(
            label = "Hearing Bundles",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER2CudPlus2RolesOsrpjfAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv2HearingBundles;
    @CCD(
            label = "Other",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER2CudPlus2RolesOsrpjfAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv2Other;
    @CCD(
            label = "Questionnaires & Answers to Questionnaires & Exhibits",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER2CudPlus2RolesOsrpjfAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv2Qa;
    @CCD(
            label = "Statements & Exhibits",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER2CudPlus2RolesOsrpjfAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv2StmtsExhibits;
    @CCD(
            label = "Case Summaries",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER3CudPlus2RolesIykcoqAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv3Summaries;
    @CCD(
            label = "Chronologies and Statements of Issues",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER3CudPlus2RolesIykcoqAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv3Chronologies;
    @CCD(
            label = "Correspondence",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER3CudPlus2RolesIykcoqAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv3CorrespDocs;
    @CCD(
            label = "Expert Evidence",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER3CudPlus2RolesIykcoqAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv3ExpertEvidence;
    @CCD(
            label = "Forms E & Exhibits",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER3CudPlus2RolesIykcoqAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv3FormEsExhibits;
    @CCD(
            label = "Forms H",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER3CudPlus2RolesIykcoqAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv3FormHs;
    @CCD(
            label = "Hearing Bundles",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER3CudPlus2RolesIykcoqAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv3HearingBundles;
    @CCD(
            label = "Other",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER3CudPlus2RolesIykcoqAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv3Other;
    @CCD(
            label = "Questionnaires & Answers to Questionnaires & Exhibits",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER3CudPlus2RolesIykcoqAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv3Qa;
    @CCD(
            label = "Statements & Exhibits",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER3CudPlus2RolesIykcoqAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv3StmtsExhibits;
    @CCD(
            label = "Case Summaries",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER4CudPlus2RolesCeepsbAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv4Summaries;
    @CCD(
            label = "Chronologies and Statements of Issues",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER4CudPlus2RolesCeepsbAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv4Chronologies;
    @CCD(
            label = "Correspondence",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER4CudPlus2RolesCeepsbAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv4CorrespDocs;
    @CCD(
            label = "Expert Evidence",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER4CudPlus2RolesCeepsbAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv4ExpertEvidence;
    @CCD(
            label = "Forms E & Exhibits",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER4CudPlus2RolesCeepsbAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv4FormEsExhibits;
    @CCD(
            label = "Forms H",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER4CudPlus2RolesCeepsbAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv4FormHs;
    @CCD(
            label = "Hearing Bundles",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER4CudPlus2RolesCeepsbAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv4HearingBundles;
    @CCD(
            label = "Other",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER4CudPlus2RolesCeepsbAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv4Other;
    @CCD(
            label = "Questionnaires & Answers to Questionnaires & Exhibits",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER4CudPlus2RolesCeepsbAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv4Qa;
    @CCD(
            label = "Statements & Exhibits",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER4CudPlus2RolesCeepsbAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv4StmtsExhibits;
    @CCD(
            label = "FDR Case documents",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER1CudPlus2RolesUmycfuAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv1FdrCaseDocuments;
    @CCD(
            label = "FDR Case documents",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER2CudPlus2RolesOsrpjfAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv2FdrCaseDocuments;
    @CCD(
            label = "FDR Case documents",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER3CudPlus2RolesIykcoqAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv3FdrCaseDocuments;
    @CCD(
            label = "FDR Case documents",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER4CudPlus2RolesCeepsbAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv4FdrCaseDocuments;
    @CCD(
            label = "Confidential documents",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesMhrsdiAccess.class}
    )
    private List<UploadCaseDocumentCollection> confidentialDocumentCollection;

    @CCD(
            label = "Hearing Bundles",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER1INTVRSOLICITOR1CudAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv1HearingBundlesShared;
    @CCD(
            label = "Forms E & Exhibits",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER1INTVRSOLICITOR1CudAccess.class}
    )
    @JsonProperty("intv1FormEExhibitsShared")
    private List<UploadCaseDocumentCollection> intv1FormEsExhibitsShared;
    @CCD(
            label = "Chronologies and Statements of Issues",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER1INTVRSOLICITOR1CudAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv1ChronologiesShared;
    @CCD(
            label = "Questionnaires & Answers to Questionnaires & Exhibits",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER1INTVRSOLICITOR1CudAccess.class}
    )
    @JsonProperty("intv1QAShared")
    private List<UploadCaseDocumentCollection> intv1QaShared;
    @CCD(
            label = "Statements & Exhibits",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER1INTVRSOLICITOR1CudAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv1StmtsExhibitsShared;
    @CCD(
            label = "Case Summaries",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER1INTVRSOLICITOR1CudAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv1SummariesShared;
    @CCD(
            label = "Forms H",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER1INTVRSOLICITOR1CudAccess.class}
    )
    @JsonProperty("intv1FormsHShared")
    private List<UploadCaseDocumentCollection> intv1FormHsShared;
    @CCD(
            label = "Expert Evidence",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER1INTVRSOLICITOR1CudAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv1ExpertEvidenceShared;
    @CCD(
            label = "Correspondence",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER1INTVRSOLICITOR1CudAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv1CorrespDocsShared;
    @CCD(
            label = "Other",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER1INTVRSOLICITOR1CudAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv1OtherShared;

    @CCD(
            label = "Hearing Bundles",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER2INTVRSOLICITOR2CudAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv2HearingBundlesShared;
    @CCD(
            label = "Forms E & Exhibits",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER2INTVRSOLICITOR2CudAccess.class}
    )
    @JsonProperty("intv2FormEExhibitsShared")
    private List<UploadCaseDocumentCollection> intv2FormEsExhibitsShared;
    @CCD(
            label = "Chronologies and Statements of Issues",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER2INTVRSOLICITOR2CudAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv2ChronologiesShared;
    @CCD(
            label = "Questionnaires & Answers to Questionnaires & Exhibits",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER2INTVRSOLICITOR2CudAccess.class}
    )
    @JsonProperty("intv2QAShared")
    private List<UploadCaseDocumentCollection> intv2QaShared;
    @CCD(
            label = "Statements & Exhibits",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER2INTVRSOLICITOR2CudAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv2StmtsExhibitsShared;
    @CCD(
            label = "Case Summaries",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER2INTVRSOLICITOR2CudAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv2SummariesShared;
    @CCD(
            label = "Forms H",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER2INTVRSOLICITOR2CudAccess.class}
    )
    @JsonProperty("intv2FormsHShared")
    private List<UploadCaseDocumentCollection> intv2FormHsShared;
    @CCD(
            label = "Expert Evidence",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER2INTVRSOLICITOR2CudAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv2ExpertEvidenceShared;
    @CCD(
            label = "Correspondence",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER2INTVRSOLICITOR2CudAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv2CorrespDocsShared;
    @CCD(
            label = "Other",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER2INTVRSOLICITOR2CudAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv2OtherShared;

    @CCD(
            label = "Hearing Bundles",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER3INTVRSOLICITOR3CudAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv3HearingBundlesShared;
    @CCD(
            label = "Forms E & Exhibits",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER3INTVRSOLICITOR3CudAccess.class}
    )
    @JsonProperty("intv3FormEExhibitsShared")
    private List<UploadCaseDocumentCollection> intv3FormEsExhibitsShared;
    @CCD(
            label = "Chronologies and Statements of Issues",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER3INTVRSOLICITOR3CudAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv3ChronologiesShared;
    @CCD(
            label = "Questionnaires & Answers to Questionnaires & Exhibits",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER3INTVRSOLICITOR3CudAccess.class}
    )
    @JsonProperty("intv3QAShared")
    private List<UploadCaseDocumentCollection> intv3QaShared;
    @CCD(
            label = "Statements & Exhibits",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER3INTVRSOLICITOR3CudAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv3StmtsExhibitsShared;
    @CCD(
            label = "Case Summaries",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER3INTVRSOLICITOR3CudAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv3SummariesShared;
    @CCD(
            label = "Forms H",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER3INTVRSOLICITOR3CudAccess.class}
    )
    @JsonProperty("intv3FormsHShared")
    private List<UploadCaseDocumentCollection> intv3FormHsShared;
    @CCD(
            label = "Expert Evidence",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER3INTVRSOLICITOR3CudAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv3ExpertEvidenceShared;
    @CCD(
            label = "Correspondence",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER3INTVRSOLICITOR3CudAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv3CorrespDocsShared;
    @CCD(
            label = "Other",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER3INTVRSOLICITOR3CudAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv3OtherShared;

    @CCD(
            label = "Hearing Bundles",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER4INTVRSOLICITOR4CudAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv4HearingBundlesShared;
    @CCD(
            label = "Forms E & Exhibits",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER4INTVRSOLICITOR4CudAccess.class}
    )
    @JsonProperty("intv4FormEExhibitsShared")
    private List<UploadCaseDocumentCollection> intv4FormEsExhibitsShared;
    @CCD(
            label = "Chronologies and Statements of Issues",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER4INTVRSOLICITOR4CudAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv4ChronologiesShared;
    @CCD(
            label = "Questionnaires & Answers to Questionnaires & Exhibits",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER4INTVRSOLICITOR4CudAccess.class}
    )
    @JsonProperty("intv4QAShared")
    private List<UploadCaseDocumentCollection> intv4QaShared;
    @CCD(
            label = "Statements & Exhibits",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER4INTVRSOLICITOR4CudAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv4StmtsExhibitsShared;
    @CCD(
            label = "Case Summaries",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER4INTVRSOLICITOR4CudAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv4SummariesShared;
    @CCD(
            label = "Forms H",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER4INTVRSOLICITOR4CudAccess.class}
    )
    @JsonProperty("intv4FormsHShared")
    private List<UploadCaseDocumentCollection> intv4FormHsShared;
    @CCD(
            label = "Expert Evidence",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER4INTVRSOLICITOR4CudAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv4ExpertEvidenceShared;
    @CCD(
            label = "Correspondence",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER4INTVRSOLICITOR4CudAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv4CorrespDocsShared;
    @CCD(
            label = "Other",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_uploadCaseDocument",
            typeParameterClass = FRUploadCaseDocument.class,
            access = {INTVRBARRISTER4INTVRSOLICITOR4CudAccess.class}
    )
    private List<UploadCaseDocumentCollection> intv4OtherShared;

    @JsonIgnore
    @SuppressWarnings({"java:S6204", "java:S1121"})
    public List<UploadCaseDocumentCollection> getAllManageableCollections() {
        return Stream.of(uploadCaseDocument, fdrCaseDocumentCollection, appCorrespondenceCollection,
                appHearingBundlesCollection, appFormEExhibitsCollection, appChronologiesCollection,
                appQaCollection, appStatementsExhibitsCollection, appCaseSummariesCollection,
                appFormsHCollection, appExpertEvidenceCollection, appOtherCollection,
                respHearingBundlesCollection, respFormEExhibitsCollection, respChronologiesCollection, respQaCollection,
                respStatementsExhibitsCollection, respCaseSummariesCollection, respFormsHCollection,
                respExpertEvidenceCollection, respCorrespondenceDocsColl, respOtherCollection, intv1Summaries,
                intv1Chronologies, intv1CorrespDocs, intv1ExpertEvidence, intv1FormEsExhibits, intv1FormHs,
                intv1HearingBundles, intv1Other, intv1Qa, intv1StmtsExhibits, intv2Summaries, intv2Chronologies,
                intv2CorrespDocs, intv2ExpertEvidence, intv2FormEsExhibits, intv2FormHs, intv2HearingBundles,
                intv2Other, intv2Qa, intv2StmtsExhibits, intv3Summaries, intv3Chronologies, intv3CorrespDocs,
                intv3ExpertEvidence, intv3FormEsExhibits, intv3FormHs, intv3HearingBundles, intv3Other, intv3Qa,
                intv3StmtsExhibits, intv4Summaries, intv4Chronologies, intv4CorrespDocs, intv4ExpertEvidence,
                intv4FormEsExhibits, intv4FormHs, intv4HearingBundles, intv4Other, intv4Qa, intv4StmtsExhibits,
                intv1FdrCaseDocuments, intv2FdrCaseDocuments, intv3FdrCaseDocuments, intv4FdrCaseDocuments,
                confidentialDocumentCollection)
            .filter(Objects::nonNull)
            .flatMap(Collection::stream).collect(Collectors.toList());
    }

    @JsonIgnore
    @SuppressWarnings("java:S1121")
    public List<UploadCaseDocumentCollection> getDocumentCollectionPerType(
        CaseDocumentCollectionType collectionType) {

        return switch (collectionType) {
            case APPLICANT_CORRESPONDENCE_COLLECTION -> appCorrespondenceCollection = getNonNull(appCorrespondenceCollection);
            case APPLICANT_CORRESPONDENCE_DOC_COLLECTION -> appCorrespondenceDocsCollection = getNonNull(appCorrespondenceDocsCollection);
            case APPLICANT_FR_FORM_COLLECTION -> appFrFormsCollection = getNonNull(appFrFormsCollection);
            case APPLICANT_EVIDENCE_COLLECTION -> appEvidenceCollection = getNonNull(appEvidenceCollection);
            case APPLICANT_TRIAL_BUNDLE_COLLECTION -> appTrialBundleCollection = getNonNull(appTrialBundleCollection);
            case APPLICANT_CONFIDENTIAL_DOCS_COLLECTION ->
                appConfidentialDocsCollection = getNonNull(appConfidentialDocsCollection);
            case RESPONDENT_CORRESPONDENCE_COLLECTION -> respCorrespondenceCollection = getNonNull(respCorrespondenceCollection);
            case RESPONDENT_FR_FORM_COLLECTION -> respFrFormsCollection = getNonNull(respFrFormsCollection);
            case RESPONDENT_EVIDENCE_COLLECTION -> respEvidenceCollection = getNonNull(respEvidenceCollection);
            case RESPONDENT_TRIAL_BUNDLE_COLLECTION ->
                respTrialBundleCollection = getNonNull(respTrialBundleCollection);
            case RESPONDENT_CONFIDENTIAL_DOCS_COLLECTION ->
                respConfidentialDocsCollection = getNonNull(respConfidentialDocsCollection);
            case APP_HEARING_BUNDLES_COLLECTION ->
                appHearingBundlesCollection = getNonNull(appHearingBundlesCollection);
            case APP_FORM_E_EXHIBITS_COLLECTION -> appFormEExhibitsCollection = getNonNull(appFormEExhibitsCollection);
            case APP_CHRONOLOGIES_STATEMENTS_COLLECTION ->
                appChronologiesCollection = getNonNull(appChronologiesCollection);
            case APP_QUESTIONNAIRES_ANSWERS_COLLECTION -> appQaCollection = getNonNull(appQaCollection);
            case APP_STATEMENTS_EXHIBITS_COLLECTION ->
                appStatementsExhibitsCollection = getNonNull(appStatementsExhibitsCollection);
            case APP_CASE_SUMMARIES_COLLECTION -> appCaseSummariesCollection = getNonNull(appCaseSummariesCollection);
            case APP_FORMS_H_COLLECTION -> appFormsHCollection = getNonNull(appFormsHCollection);
            case APP_EXPERT_EVIDENCE_COLLECTION ->
                appExpertEvidenceCollection = getNonNull(appExpertEvidenceCollection);
            case APP_OTHER_COLLECTION -> appOtherCollection = getNonNull(appOtherCollection);
            case RESP_HEARING_BUNDLES_COLLECTION ->
                respHearingBundlesCollection = getNonNull(respHearingBundlesCollection);
            case RESP_FORM_E_EXHIBITS_COLLECTION ->
                respFormEExhibitsCollection = getNonNull(respFormEExhibitsCollection);
            case RESP_CHRONOLOGIES_STATEMENTS_COLLECTION ->
                respChronologiesCollection = getNonNull(respChronologiesCollection);
            case RESP_QUESTIONNAIRES_ANSWERS_COLLECTION -> respQaCollection = getNonNull(respQaCollection);
            case RESP_STATEMENTS_EXHIBITS_COLLECTION ->
                respStatementsExhibitsCollection = getNonNull(respStatementsExhibitsCollection);
            case RESP_CASE_SUMMARIES_COLLECTION ->
                respCaseSummariesCollection = getNonNull(respCaseSummariesCollection);
            case RESP_FORM_H_COLLECTION -> respFormsHCollection = getNonNull(respFormsHCollection);
            case RESP_EXPERT_EVIDENCE_COLLECTION ->
                respExpertEvidenceCollection = getNonNull(respExpertEvidenceCollection);
            case RESP_CORRESPONDENCE_COLLECTION -> respCorrespondenceDocsColl = getNonNull(respCorrespondenceDocsColl);
            case RESP_OTHER_COLLECTION -> respOtherCollection = getNonNull(respOtherCollection);
            case CONTESTED_UPLOADED_DOCUMENTS -> uploadCaseDocument = getNonNull(uploadCaseDocument);
            case CONTESTED_FDR_CASE_DOCUMENT_COLLECTION ->
                fdrCaseDocumentCollection = getNonNull(fdrCaseDocumentCollection);
            case INTERVENER_ONE_SUMMARIES_COLLECTION -> intv1Summaries = getNonNull(intv1Summaries);
            case INTERVENER_ONE_CHRONOLOGIES_STATEMENTS_COLLECTION -> intv1Chronologies = getNonNull(intv1Chronologies);
            case INTERVENER_ONE_CORRESPONDENCE_COLLECTION -> intv1CorrespDocs = getNonNull(intv1CorrespDocs);
            case INTERVENER_ONE_EXPERT_EVIDENCE_COLLECTION -> intv1ExpertEvidence = getNonNull(intv1ExpertEvidence);
            case INTERVENER_ONE_FORM_E_EXHIBITS_COLLECTION -> intv1FormEsExhibits = getNonNull(intv1FormEsExhibits);
            case INTERVENER_ONE_FORM_H_COLLECTION -> intv1FormHs = getNonNull(intv1FormHs);
            case INTERVENER_ONE_HEARING_BUNDLES_COLLECTION -> intv1HearingBundles = getNonNull(intv1HearingBundles);
            case INTERVENER_ONE_OTHER_COLLECTION -> intv1Other = getNonNull(intv1Other);
            case INTERVENER_ONE_QUESTIONNAIRES_ANSWERS_COLLECTION -> intv1Qa = getNonNull(intv1Qa);
            case INTERVENER_ONE_STATEMENTS_EXHIBITS_COLLECTION -> intv1StmtsExhibits = getNonNull(intv1StmtsExhibits);
            case INTERVENER_TWO_SUMMARIES_COLLECTION -> intv2Summaries = getNonNull(intv2Summaries);
            case INTERVENER_TWO_CHRONOLOGIES_STATEMENTS_COLLECTION -> intv2Chronologies = getNonNull(intv2Chronologies);
            case INTERVENER_TWO_CORRESPONDENCE_COLLECTION -> intv2CorrespDocs = getNonNull(intv2CorrespDocs);
            case INTERVENER_TWO_EXPERT_EVIDENCE_COLLECTION -> intv2ExpertEvidence = getNonNull(intv2ExpertEvidence);
            case INTERVENER_TWO_FORM_E_EXHIBITS_COLLECTION -> intv2FormEsExhibits = getNonNull(intv2FormEsExhibits);
            case INTERVENER_TWO_FORM_H_COLLECTION -> intv2FormHs = getNonNull(intv2FormHs);
            case INTERVENER_TWO_HEARING_BUNDLES_COLLECTION -> intv2HearingBundles = getNonNull(intv2HearingBundles);
            case INTERVENER_TWO_OTHER_COLLECTION -> intv2Other = getNonNull(intv2Other);
            case INTERVENER_TWO_QUESTIONNAIRES_ANSWERS_COLLECTION -> intv2Qa = getNonNull(intv2Qa);
            case INTERVENER_TWO_STATEMENTS_EXHIBITS_COLLECTION -> intv2StmtsExhibits = getNonNull(intv2StmtsExhibits);
            case INTERVENER_THREE_SUMMARIES_COLLECTION -> intv3Summaries = getNonNull(intv3Summaries);
            case INTERVENER_THREE_CHRONOLOGIES_STATEMENTS_COLLECTION -> intv3Chronologies = getNonNull(intv3Chronologies);
            case INTERVENER_THREE_CORRESPONDENCE_COLLECTION -> intv3CorrespDocs = getNonNull(intv3CorrespDocs);
            case INTERVENER_THREE_EXPERT_EVIDENCE_COLLECTION -> intv3ExpertEvidence = getNonNull(intv3ExpertEvidence);
            case INTERVENER_THREE_FORM_E_EXHIBITS_COLLECTION -> intv3FormEsExhibits = getNonNull(intv3FormEsExhibits);
            case INTERVENER_THREE_FORM_H_COLLECTION -> intv3FormHs = getNonNull(intv3FormHs);
            case INTERVENER_THREE_HEARING_BUNDLES_COLLECTION -> intv3HearingBundles = getNonNull(intv3HearingBundles);
            case INTERVENER_THREE_OTHER_COLLECTION -> intv3Other = getNonNull(intv3Other);
            case INTERVENER_THREE_QUESTIONNAIRES_ANSWERS_COLLECTION -> intv3Qa = getNonNull(intv3Qa);
            case INTERVENER_THREE_STATEMENTS_EXHIBITS_COLLECTION -> intv3StmtsExhibits = getNonNull(intv3StmtsExhibits);
            case INTERVENER_FOUR_SUMMARIES_COLLECTION -> intv4Summaries = getNonNull(intv4Summaries);
            case INTERVENER_FOUR_CHRONOLOGIES_STATEMENTS_COLLECTION -> intv4Chronologies = getNonNull(intv4Chronologies);
            case INTERVENER_FOUR_CORRESPONDENCE_COLLECTION -> intv4CorrespDocs = getNonNull(intv4CorrespDocs);
            case INTERVENER_FOUR_EXPERT_EVIDENCE_COLLECTION -> intv4ExpertEvidence = getNonNull(intv4ExpertEvidence);
            case INTERVENER_FOUR_FORM_E_EXHIBITS_COLLECTION -> intv4FormEsExhibits = getNonNull(intv4FormEsExhibits);
            case INTERVENER_FOUR_FORM_H_COLLECTION -> intv4FormHs = getNonNull(intv4FormHs);
            case INTERVENER_FOUR_HEARING_BUNDLES_COLLECTION -> intv4HearingBundles = getNonNull(intv4HearingBundles);
            case INTERVENER_FOUR_OTHER_COLLECTION -> intv4Other = getNonNull(intv4Other);
            case INTERVENER_FOUR_QUESTIONNAIRES_ANSWERS_COLLECTION -> intv4Qa = getNonNull(intv4Qa);
            case INTERVENER_FOUR_STATEMENTS_EXHIBITS_COLLECTION -> intv4StmtsExhibits = getNonNull(intv4StmtsExhibits);
            case INTERVENER_ONE_FDR_DOCS_COLLECTION -> intv1FdrCaseDocuments = getNonNull(intv1FdrCaseDocuments);
            case INTERVENER_TWO_FDR_DOCS_COLLECTION -> intv2FdrCaseDocuments = getNonNull(intv2FdrCaseDocuments);
            case INTERVENER_THREE_FDR_DOCS_COLLECTION -> intv3FdrCaseDocuments = getNonNull(intv3FdrCaseDocuments);
            case INTERVENER_FOUR_FDR_DOCS_COLLECTION -> intv4FdrCaseDocuments = getNonNull(intv4FdrCaseDocuments);
            case CONFIDENTIAL_DOCS_COLLECTION -> confidentialDocumentCollection =
                getNonNull(confidentialDocumentCollection);
        };
    }

    private List<UploadCaseDocumentCollection> getNonNull(List<UploadCaseDocumentCollection> collection) {
        if (collection == null) {
            collection = new ArrayList<>();
        }
        return collection;
    }
}

