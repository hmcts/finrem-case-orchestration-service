package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrderConsolidateCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HasCaseDocument;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.APPBARRISTERRAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.APPSOLICITORRAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCrudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.RESPBARRISTERRAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.RESPSOLICITORRAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.INTVRBARRISTER1RAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.INTVRSOLICITOR1RAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.INTVRBARRISTER2RAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.INTVRSOLICITOR2RAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.INTVRBARRISTER3RAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.INTVRSOLICITOR3RAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.INTVRBARRISTER4RAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.INTVRSOLICITOR4RAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FROrderCollection;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderWrapper implements HasCaseDocument {
    @CCD(
            label = "Approve Orders",
            hint = "Approve Orders",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_orderCollections",
            access = {CaseworkerDivorceFinancialremedyCourtadminCudAccess.class}
    )
    private List<ApprovedOrderConsolidateCollection> appOrderCollections;
    @CCD(
            label = "Approve Orders",
            hint = "Approve Orders",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_orderCollections",
            access = {CaseworkerDivorceFinancialremedyCourtadminCudAccess.class}
    )
    private List<ApprovedOrderConsolidateCollection> respOrderCollections;
    @CCD(
            label = "Approve Orders",
            hint = "Approve Orders",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_orderCollections",
            access = {CaseworkerDivorceFinancialremedyCourtadminCudAccess.class}
    )
    private List<ApprovedOrderConsolidateCollection> intv1OrderCollections;
    @CCD(
            label = "Approve Orders",
            hint = "Approve Orders",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_orderCollections",
            access = {CaseworkerDivorceFinancialremedyCourtadminCudAccess.class}
    )
    private List<ApprovedOrderConsolidateCollection> intv2OrderCollections;
    @CCD(
            label = "Approve Orders",
            hint = "Approve Orders",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_orderCollections",
            access = {CaseworkerDivorceFinancialremedyCourtadminCudAccess.class}
    )
    private List<ApprovedOrderConsolidateCollection> intv3OrderCollections;
    @CCD(
            label = "Approve Orders",
            hint = "Approve Orders",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_orderCollections",
            access = {CaseworkerDivorceFinancialremedyCourtadminCudAccess.class}
    )
    private List<ApprovedOrderConsolidateCollection> intv4OrderCollections;
    @CCD(
            label = "Approve Orders",
            hint = "Approve Orders",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_orderCollection",
            typeParameterClass = FROrderCollection.class,
            access = {APPBARRISTERRAccess.class, APPSOLICITORRAccess.class, CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    private List<ApprovedOrderCollection> appOrderCollection;
    @CCD(
            label = "Approve Orders",
            hint = "Approve Orders",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_orderCollection",
            typeParameterClass = FROrderCollection.class,
            access = {RESPBARRISTERRAccess.class, RESPSOLICITORRAccess.class, CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    private List<ApprovedOrderCollection> respOrderCollection;
    @CCD(
            label = "Approve Orders",
            hint = "Approve Orders",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_orderCollection",
            typeParameterClass = FROrderCollection.class,
            access = {INTVRBARRISTER1RAccess.class, INTVRSOLICITOR1RAccess.class, CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    private List<ApprovedOrderCollection> intv1OrderCollection;
    @CCD(
            label = "Approve Orders",
            hint = "Approve Orders",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_orderCollection",
            typeParameterClass = FROrderCollection.class,
            access = {INTVRBARRISTER2RAccess.class, INTVRSOLICITOR2RAccess.class, CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    private List<ApprovedOrderCollection> intv2OrderCollection;
    @CCD(
            label = "Approve Orders",
            hint = "Approve Orders",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_orderCollection",
            typeParameterClass = FROrderCollection.class,
            access = {INTVRBARRISTER3RAccess.class, INTVRSOLICITOR3RAccess.class, CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    private List<ApprovedOrderCollection> intv3OrderCollection;
    @CCD(
            label = "Approve Orders",
            hint = "Approve Orders",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_orderCollection",
            typeParameterClass = FROrderCollection.class,
            access = {INTVRBARRISTER4RAccess.class, INTVRSOLICITOR4RAccess.class, CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    private List<ApprovedOrderCollection> intv4OrderCollection;
}
