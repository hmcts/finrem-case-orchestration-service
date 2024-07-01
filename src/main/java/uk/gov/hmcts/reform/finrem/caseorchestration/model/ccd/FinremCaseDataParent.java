package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ConsentOrderScannedDocWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.FormAScannedDocWrapper;

import java.util.List;

@Data
public abstract class FinremCaseDataParent {
    @Getter(AccessLevel.NONE)
    private FormAScannedDocWrapper formAScannedDocWrapper;
    @Getter(AccessLevel.NONE)
    private ConsentOrderScannedDocWrapper consentOrderScannedDocWrapper;
    private List<ScannedD81WithInfo> scannedD81WithInfos;

    @JsonIgnore
    public FormAScannedDocWrapper getFormAScannedDocWrapper() {
        if (formAScannedDocWrapper == null) {
            this.formAScannedDocWrapper = new FormAScannedDocWrapper();
        }

        return formAScannedDocWrapper;
    }

    @JsonIgnore
    public ConsentOrderScannedDocWrapper getConsentOrderScannedDocWrapper() {
        if (consentOrderScannedDocWrapper == null) {
            this.consentOrderScannedDocWrapper = new ConsentOrderScannedDocWrapper();
        }

        return consentOrderScannedDocWrapper;
    }
}
