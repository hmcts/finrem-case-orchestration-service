package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerHearingNoticeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerHearingNoticeCollectionName;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType;

import java.util.ArrayList;
import java.util.List;


@SuperBuilder
@NoArgsConstructor
public class IntervenerThree extends IntervenerWrapper {
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
        return caseData.getIntervenerThree();
    }


    @Override
    @JsonIgnore
    public void removeIntervenerWrapperFromCaseData(FinremCaseData caseData) {
        caseData.setIntervenerThree(null);
    }


    @Override
    @JsonIgnore
    public IntervenerHearingNoticeCollectionName getIntervenerHearingNoticesCollectionName() {
        return IntervenerHearingNoticeCollectionName.INTV_3;
    }

    @Override
    @JsonIgnore
    public List<IntervenerHearingNoticeCollection> getIntervenerHearingNoticesCollection(FinremCaseData caseData) {
        if (caseData.getIntv3HearingNoticesCollection() == null) {
            caseData.setIntv3HearingNoticesCollection(new ArrayList<>());
        }
        return caseData.getIntv3HearingNoticesCollection();
    }
}
