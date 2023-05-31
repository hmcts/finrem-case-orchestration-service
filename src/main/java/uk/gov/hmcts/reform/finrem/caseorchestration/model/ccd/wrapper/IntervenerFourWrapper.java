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
public class IntervenerFourWrapper extends IntervenerWrapper {
    @JsonProperty("intervener4Name")
    private String intervenerName;
    @JsonProperty("intervener4Address")
    private Address intervenerAddress;
    @JsonProperty("intervener4Email")
    private String intervenerEmail;
    @JsonProperty("intervener4Phone")
    private String intervenerPhone;
    @JsonProperty("intervener4Represented")
    private YesOrNo intervenerRepresented;
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @JsonProperty("intervener4DateAdded")
    private LocalDate intervenerDateAdded;
    @JsonProperty("intervener4SolName")
    private String intervenerSolName;
    @JsonProperty("intervener4SolEmail")
    private String intervenerSolEmail;
    @JsonProperty("intervener4SolPhone")
    private String intervenerSolPhone;
    @JsonProperty("intervener4SolicitorFirm")
    private String intervenerSolicitorFirm;
    @JsonProperty("intervener4SolicitorReference")
    private String intervenerSolicitorReference;
    @JsonProperty("intervener4Organisation")
    private OrganisationPolicy intervenerOrganisation;

    @Override
    @JsonIgnore
    public String getIntervenerLabel() {
        return "Intervener 4";
    }

    @Override
    @JsonIgnore
    public IntervenerType getIntervenerType() {
        return IntervenerType.INTERVENER_FOUR;
    }

    @Override
    @JsonIgnore
    public String getAddIntervenerCode() {
        return "addIntervener4";
    }

    @Override
    @JsonIgnore
    public String getAddIntervenerValue() {
        return "Add Intervener 4";
    }

    @Override
    @JsonIgnore
    public String getDeleteIntervenerCode() {
        return "delIntervener4";
    }

    @Override
    @JsonIgnore
    public String getDeleteIntervenerValue() {
        return "Remove Intervener 4";
    }

    @Override
    @JsonIgnore
    public String getUpdateIntervenerValue() {
        return "Amend Intervener 4";
    }

    @Override
    @JsonIgnore
    public CaseRole getIntervenerSolicitorCaseRole() {
        return CaseRole.INTVR_SOLICITOR_4;
    }

    @Override
    @JsonIgnore
    public DocumentHelper.PaperNotificationRecipient getPaperNotificationRecipient() {
        return DocumentHelper.PaperNotificationRecipient.INTERVENER_FOUR;
    }

    @Override
    @JsonIgnore
    public IntervenerWrapper getIntervenerWrapperFromCaseData(FinremCaseData caseData) {
        return caseData.getIntervenerFourWrapper();
    }

    @Override
    @JsonIgnore
    public void removeIntervenerWrapperFromCaseData(FinremCaseData caseData) {
        caseData.setIntervenerFourWrapper(null);
    }
}
