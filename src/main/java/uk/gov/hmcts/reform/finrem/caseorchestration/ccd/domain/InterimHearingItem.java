package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.wrapper.InterimRegionWrapper;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterimHearingItem {
    @JsonProperty("interimHearingDate")
    private String interimHearingDate;
    @JsonProperty("interimHearingTime")
    private String interimHearingTime;
    @JsonProperty("interimHearingType")
    private String interimHearingType;
    @JsonProperty("interimHearingTimeEstimate")
    private String interimHearingTimeEstimate;
    @JsonProperty("interimPromptForAnyDocument")
    private String interimPromptForAnyDocument;
    @JsonProperty("interimUploadAdditionalDocument")
    private Document interimUploadAdditionalDocument;
    @JsonProperty("interimAdditionalInformationAboutHearing")
    private String interimAdditionalInformationAboutHearing;
    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private InterimRegionWrapper interimRegionWrapper;

    @JsonIgnore
    public InterimRegionWrapper getInterimRegionWrapper() {
        if (interimRegionWrapper == null) {
            interimRegionWrapper = new InterimRegionWrapper();
        }

        return interimRegionWrapper;
    }
}
