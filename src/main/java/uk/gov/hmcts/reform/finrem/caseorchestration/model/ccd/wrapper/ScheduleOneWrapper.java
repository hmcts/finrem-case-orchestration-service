package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChildDetailsCollectionElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NatureOfApplicationSchedule;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Schedule1OrMatrimonialAndCpList;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.DefaultAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerApproverCrudCaseworkerCaaCudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesUoltzlAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerApproverCaseworkerCaaCrudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Schedule1OrMatrimonialAndCPList;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScheduleOneWrapper {

    @CCD(
            label = " ",
            typeParameterOverride = "schedule1OrMatrimonialAndCPList",
            typeParameterClass = Schedule1OrMatrimonialAndCPList.class,
            access = {DefaultAccess.class, CaseworkerApproverCrudCaseworkerCaaCudAccess.class}
    )
    private Schedule1OrMatrimonialAndCpList typeOfApplication;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_childrenCollection",
            access = {DefaultAccess.class, CaseworkerApproverCrudCaseworkerCaaCudAccess.class}
    )
    private List<ChildDetailsCollectionElement> childrenCollection;
    @CCD(
            label = "  ",
            hint = "Select all that apply",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "FR_ms_natureApplication_sch",
            access = {DefaultAccess.class, CaseworkerApproverCrudCaseworkerCaaCudAccess.class}
    )
    private List<NatureOfApplicationSchedule> natureOfApplicationChecklistSchedule;
    @CCD(
            label = "The application is for:",
            hint = "The applicant is applying for an order by consent in terms of written agreement (a consent order). Within the draft consent order, the Applicant is applying to Court for;",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "FR_ms_natureApplication_sch",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesUoltzlAccess.class, CaseworkerApproverCaseworkerCaaCrudAccess.class}
    )
    private List<NatureOfApplicationSchedule> consentNatureOfApplicationChecklistSchedule;
}
