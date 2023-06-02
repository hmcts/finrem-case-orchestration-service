package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType;

import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;

public abstract  class IntervenerWrapper implements IntervenerDetails {

    public abstract String getIntervenerLabel();

    public abstract IntervenerType getIntervenerType();

    public abstract String getAddIntervenerCode();

    public abstract String getAddIntervenerValue();

    public abstract String getDeleteIntervenerCode();

    public abstract String getDeleteIntervenerValue();

    public abstract String getUpdateIntervenerValue();

    public abstract CaseRole getIntervenerSolicitorCaseRole();

    public abstract DocumentHelper.PaperNotificationRecipient getPaperNotificationRecipient();

    public abstract IntervenerWrapper getIntervenerWrapperFromCaseData(FinremCaseData caseData);

    public abstract void removeIntervenerWrapperFromCaseData(FinremCaseData caseData);

    @JsonIgnore
    public boolean isIntervenerSolicitorPopulated() {
        return StringUtils.isNotEmpty(nullToEmpty(this.getIntervenerSolEmail()));
    }
}
