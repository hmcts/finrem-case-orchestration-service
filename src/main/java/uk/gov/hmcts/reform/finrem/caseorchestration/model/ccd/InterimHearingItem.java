package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.InterimRegionWrapper;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterimHearingItem {
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public LocalDate interimHearingDate;
    public String interimHearingTime;
    public InterimTypeOfHearing interimHearingType;
    public YesOrNo interimPromptForAnyDocument;
    public String interimHearingTimeEstimate;
    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    InterimRegionWrapper interimRegionWrapper;

    public CaseDocument interimUploadAdditionalDocument;
    public String interimAdditionalInformationAboutHearing;

    @JsonIgnore
    public InterimRegionWrapper getInterimRegionWrapper() {
        if (interimRegionWrapper == null) {
            this.interimRegionWrapper = new InterimRegionWrapper();
        }
        return interimRegionWrapper;
    }
}
