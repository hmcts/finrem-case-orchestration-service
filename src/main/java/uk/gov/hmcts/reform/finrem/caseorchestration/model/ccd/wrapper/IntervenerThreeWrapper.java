package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IntervenerThreeWrapper extends IntervenerWrapper {
    @JsonProperty("intervener3Name")
    private String intervenerName;
    @JsonProperty("intervener3Address")
    private Address intervenerAddress;
    @JsonProperty("intervener3Email")
    private String intervenerEmail;
    @JsonProperty("intervener3Phone")
    private String intervenerPhone;
    @JsonProperty("intervener3Represented")
    private YesOrNo intervenerRepresented;
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @JsonProperty("intervener3DateAdded")
    private LocalDate intervenerDateAdded;
    @JsonProperty("intervener3SolName")
    private String intervenerSolName;
    @JsonProperty("intervener3SolEmail")
    private String intervenerSolEmail;
    @JsonProperty("intervener3SolPhone")
    private String intervenerSolPhone;
    @JsonProperty("intervener3SolicitorFirm")
    private String intervenerSolicitorFirm;
    @JsonProperty("intervener3SolicitorReference")
    private String intervenerSolicitorReference;
    @JsonProperty("intervener3Organisation")
    private OrganisationPolicy intervenerOrganisation;

    @Override
    @JsonIgnore
    public String getIntervenerLabel() {
        return "Intervener 3";
    }

    @Override
    @JsonIgnore
    public IntervenerType getIntervenerType() {
        return IntervenerType.INTERVENER_THREE;
    }

    @Override
    @JsonIgnore
    public String getAddIntervenerCode() {
        return "addIntervener3";
    }

    @Override
    @JsonIgnore
    public String getAddIntervenerValue() {
        return "Add Intervener 3";
    }

    @Override
    @JsonIgnore
    public String getDeleteIntervenerCode() {
        return "delIntervener3";
    }

    @Override
    @JsonIgnore
    public String getDeleteIntervenerValue() {
        return "Remove Intervener 3";
    }

    @Override
    @JsonIgnore
    public String getUpdateIntervenerValue() {
        return "Amend Intervener 3";
    }

    @Override
    @JsonIgnore
    public CaseRole getIntervenerSolicitorCaseRole() {
        return CaseRole.INTVR_SOLICITOR_3;
    }

    @Override
    @JsonIgnore
    public DocumentHelper.PaperNotificationRecipient getPaperNotificationRecipient() {
        return DocumentHelper.PaperNotificationRecipient.INTERVENER_THREE;
    }

    @Override
    @JsonIgnore
    public IntervenerWrapper getIntervenerWrapperFromCaseData(FinremCaseData caseData) {
        return caseData.getIntervenerThreeWrapper();
    }


    @Override
    @JsonIgnore
    public void removeIntervenerWrapperFromCaseData(FinremCaseData caseData) {
        caseData.setIntervenerThreeWrapper(null);
    }
}
