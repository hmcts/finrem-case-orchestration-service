package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDataContested;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerHearingNoticeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType;

import java.time.LocalDate;
import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;

@JsonIgnoreProperties(ignoreUnknown = true)
@SuperBuilder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
public abstract class IntervenerWrapper implements IntervenerDetails {

    private String intervenerName;

    private Address intervenerAddress;

    private String intervenerEmail;

    private String intervenerPhone;
    private YesOrNo intervenerRepresented;

    @JsonIgnore
    private Boolean intervenerCorrespondenceEnabled;


    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate intervenerDateAdded;

    private String intervenerSolName;

    private String intervenerSolEmail;

    private String intervenerSolPhone;

    private String intervenerSolicitorFirm;

    private String intervenerSolicitorReference;

    private OrganisationPolicy intervenerOrganisation;

    protected IntervenerWrapper() {
        intervenerCorrespondenceEnabled = Boolean.FALSE;
    }

    public abstract String getIntervenerLabel();

    public abstract IntervenerType getIntervenerType();

    public abstract String getAddIntervenerCode();

    public abstract String getAddIntervenerValue();

    public abstract String getDeleteIntervenerCode();

    public abstract String getDeleteIntervenerValue();

    public abstract String getUpdateIntervenerValue();

    public abstract CaseRole getIntervenerSolicitorCaseRole();

    public abstract List<IntervenerHearingNoticeCollection> getIntervenerHearingNoticesCollection(FinremCaseData caseData);

    public abstract String getIntervenerHearingNoticesCollectionName();

    public abstract DocumentHelper.PaperNotificationRecipient getPaperNotificationRecipient();

    public abstract IntervenerWrapper getIntervenerWrapperFromCaseData(FinremCaseDataContested caseData);

    public abstract void removeIntervenerWrapperFromCaseData(FinremCaseDataContested caseData);

    @JsonIgnore
    public boolean isIntervenerSolicitorPopulated() {
        return StringUtils.isNotEmpty(nullToEmpty(this.getIntervenerSolEmail()));
    }
}
