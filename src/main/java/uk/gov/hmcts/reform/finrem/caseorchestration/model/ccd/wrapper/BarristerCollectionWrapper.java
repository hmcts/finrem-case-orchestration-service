package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesSvbfxpAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.APPBARRISTERRAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.APPSOLICITORCrudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.RESPBARRISTERRAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.RESPSOLICITORRAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.APPBARRISTERAPPSOLICITORRESPBARRISTERRAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.RESPSOLICITORCrudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminRPlus1RolesEhdmahAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.INTVRSOLICITOR1CrudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.INTVRSOLICITOR2RAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.INTVRSOLICITOR3RAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.INTVRSOLICITOR4RAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.INTVRSOLICITOR1RAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.INTVRSOLICITOR2CrudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.INTVRSOLICITOR3CrudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.INTVRSOLICITOR4CrudAccess;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BarristerCollectionWrapper {
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_BarristerCollection",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesSvbfxpAccess.class, APPBARRISTERRAccess.class, APPSOLICITORCrudAccess.class, RESPBARRISTERRAccess.class, RESPSOLICITORRAccess.class}
    )
    @JsonProperty("appBarristerCollection")
    private List<BarristerCollectionItem> applicantBarristers;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_BarristerCollection",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesSvbfxpAccess.class, APPBARRISTERAPPSOLICITORRESPBARRISTERRAccess.class, RESPSOLICITORCrudAccess.class}
    )
    @JsonProperty("respBarristerCollection")
    private List<BarristerCollectionItem> respondentBarristers;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_BarristerCollection",
            access = {CaseworkerDivorceFinancialremedyCourtadminRPlus1RolesEhdmahAccess.class, INTVRSOLICITOR1CrudAccess.class, INTVRSOLICITOR2RAccess.class, INTVRSOLICITOR3RAccess.class, INTVRSOLICITOR4RAccess.class}
    )
    @JsonProperty("intvr1BarristerCollection")
    private List<BarristerCollectionItem> intvr1Barristers;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_BarristerCollection",
            access = {CaseworkerDivorceFinancialremedyCourtadminRPlus1RolesEhdmahAccess.class, INTVRSOLICITOR1RAccess.class, INTVRSOLICITOR2CrudAccess.class, INTVRSOLICITOR3RAccess.class, INTVRSOLICITOR4RAccess.class}
    )
    @JsonProperty("intvr2BarristerCollection")
    private List<BarristerCollectionItem> intvr2Barristers;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_BarristerCollection",
            access = {CaseworkerDivorceFinancialremedyCourtadminRPlus1RolesEhdmahAccess.class, INTVRSOLICITOR1RAccess.class, INTVRSOLICITOR2RAccess.class, INTVRSOLICITOR3CrudAccess.class, INTVRSOLICITOR4RAccess.class}
    )
    @JsonProperty("intvr3BarristerCollection")
    private List<BarristerCollectionItem> intvr3Barristers;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_BarristerCollection",
            access = {CaseworkerDivorceFinancialremedyCourtadminRPlus1RolesEhdmahAccess.class, INTVRSOLICITOR1RAccess.class, INTVRSOLICITOR2RAccess.class, INTVRSOLICITOR3RAccess.class, INTVRSOLICITOR4CrudAccess.class}
    )
    @JsonProperty("intvr4BarristerCollection")
    private List<BarristerCollectionItem> intvr4Barristers;

    @JsonIgnore
    public List<BarristerCollectionItem> getIntervenerBarristersByIndex(int index) {
        return switch (index) {
            case 1 -> intvr1Barristers;
            case 2 -> intvr2Barristers;
            case 3 -> intvr3Barristers;
            case 4 -> intvr4Barristers;
            default -> null;
        };
    }

    @JsonIgnore
    public List<BarristerCollectionItem> getIntervenerBarristers(IntervenerType intervenerType) {
        return switch (intervenerType) {
            case INTERVENER_ONE -> intvr1Barristers;
            case INTERVENER_TWO -> intvr2Barristers;
            case INTERVENER_THREE -> intvr3Barristers;
            case INTERVENER_FOUR -> intvr4Barristers;
        };
    }
}
