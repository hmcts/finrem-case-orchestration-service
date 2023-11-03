package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDataContested;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerHearingNoticeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType;

import java.util.ArrayList;
import java.util.List;

@SuperBuilder
@NoArgsConstructor
public class IntervenerTwoWrapper extends IntervenerWrapper {

    @Override
    @JsonIgnore
    public String getIntervenerLabel() {
        return "Intervener 2";
    }

    @Override
    @JsonIgnore
    public IntervenerType getIntervenerType() {
        return IntervenerType.INTERVENER_TWO;
    }

    @Override
    @JsonIgnore
    public String getAddIntervenerCode() {
        return "addIntervener2";
    }

    @Override
    @JsonIgnore
    public String getAddIntervenerValue() {
        return "Add Intervener 2";
    }

    @Override
    @JsonIgnore
    public String getDeleteIntervenerCode() {
        return "delIntervener2";
    }

    @Override
    @JsonIgnore
    public String getDeleteIntervenerValue() {
        return "Remove Intervener 2";
    }

    @Override
    @JsonIgnore
    public String getUpdateIntervenerValue() {
        return "Amend Intervener 2";
    }

    @Override
    @JsonIgnore
    public CaseRole getIntervenerSolicitorCaseRole() {
        return CaseRole.INTVR_SOLICITOR_2;
    }

    @Override
    @JsonIgnore
    public DocumentHelper.PaperNotificationRecipient getPaperNotificationRecipient() {
        return DocumentHelper.PaperNotificationRecipient.INTERVENER_TWO;
    }

    @Override
    @JsonIgnore
    public IntervenerWrapper getIntervenerWrapperFromCaseData(FinremCaseDataContested caseData) {
        return caseData.getIntervenerTwoWrapper();
    }

    @Override
    @JsonIgnore
    public void removeIntervenerWrapperFromCaseData(FinremCaseDataContested caseData) {
        caseData.setIntervenerTwoWrapper(null);
    }


    @Override
    @JsonIgnore
    public String getIntervenerHearingNoticesCollectionName() {
        return "intv2HearingNoticesCollection";
    }

    @Override
    @JsonIgnore
    public List<IntervenerHearingNoticeCollection> getIntervenerHearingNoticesCollection(FinremCaseData caseData) {
        if (caseData.getIntv2HearingNoticesCollection() == null) {
            caseData.setIntv2HearingNoticesCollection(new ArrayList<>());
        }
        return caseData.getIntv2HearingNoticesCollection();
    }
}
